/* eslint-disable require-jsdoc, max-len */

const {createClient} = require("@supabase/supabase-js");
const {env, RawImage, pipeline} = require("@xenova/transformers");

const MODEL_ID = "Xenova/clip-vit-base-patch32";
const EMBEDDING_DIMENSIONS = 512;
const MAX_IMAGE_BYTES = 10 * 1024 * 1024;
const ALLOWED_FIXED_HOSTS = new Set(["res.cloudinary.com", "fendly-api.onrender.com"]);

env.cacheDir = "/tmp/transformers-cache";
env.allowLocalModels = false;

let extractorPromise;
let supabaseClient;

function getSupabaseClient() {
  if (supabaseClient) return supabaseClient;

  const supabaseUrl = process.env.SUPABASE_URL;
  const serviceRoleKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
  if (!supabaseUrl || !serviceRoleKey) {
    throw new Error("SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY must be configured");
  }

  supabaseClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: {persistSession: false, autoRefreshToken: false},
  });
  return supabaseClient;
}

function validateImageUrl(imageUrl, additionalAllowedHosts = []) {
  let parsed;
  try {
    parsed = new URL(imageUrl);
  } catch (error) {
    throw new Error("A valid image URL is required");
  }

  if (parsed.protocol !== "https:") {
    throw new Error("Image URLs must use HTTPS");
  }

  const allowedHosts = new Set(ALLOWED_FIXED_HOSTS);
  additionalAllowedHosts.forEach((host) => allowedHosts.add(String(host).toLowerCase()));
  const supabaseUrl = process.env.SUPABASE_URL;
  if (supabaseUrl) allowedHosts.add(new URL(supabaseUrl).hostname);
  const customHosts = (process.env.IMAGE_MATCH_ALLOWED_HOSTS || "")
      .split(",")
      .map((host) => host.trim().toLowerCase())
      .filter(Boolean);
  customHosts.forEach((host) => allowedHosts.add(host));

  const hostAllowed = allowedHosts.has(parsed.hostname.toLowerCase()) ||
      parsed.hostname.toLowerCase().endsWith(".cloudinary.com");
  if (!hostAllowed) {
    throw new Error("Image URL host is not allowlisted for matching");
  }

  return parsed.toString();
}

async function getImageFeatureExtractor() {
  if (!extractorPromise) {
    extractorPromise = pipeline("image-feature-extraction", MODEL_ID, {quantized: true});
  }
  return extractorPromise;
}

function normalizeEmbedding(values) {
  if (!values || values.length !== EMBEDDING_DIMENSIONS) {
    throw new Error(`CLIP must return exactly ${EMBEDDING_DIMENSIONS} image features`);
  }

  const vector = Array.from(values, Number);
  const norm = Math.sqrt(vector.reduce((sum, value) => sum + value * value, 0));
  if (!Number.isFinite(norm) || norm === 0 || vector.some((value) => !Number.isFinite(value))) {
    throw new Error("CLIP returned an invalid image embedding");
  }
  return vector.map((value) => value / norm);
}

async function generateImageEmbedding(imageUrl, options = {}) {
  const safeImageUrl = validateImageUrl(imageUrl, options.allowedHosts);
  const response = await fetch(safeImageUrl, {
    signal: AbortSignal.timeout(15000),
    redirect: "error",
  });
  if (!response.ok) throw new Error(`Image download failed with HTTP ${response.status}`);

  const contentType = response.headers.get("content-type") || "";
  if (!contentType.toLowerCase().startsWith("image/")) {
    throw new Error("Image URL did not return an image");
  }
  const contentLength = Number(response.headers.get("content-length") || 0);
  if (contentLength > MAX_IMAGE_BYTES) throw new Error("Image exceeds the 10 MB matching limit");

  const imageBytes = Buffer.from(await response.arrayBuffer());
  if (imageBytes.length === 0 || imageBytes.length > MAX_IMAGE_BYTES) {
    throw new Error("Image is empty or exceeds the 10 MB matching limit");
  }

  const extractor = options.extractor || await getImageFeatureExtractor();
  const imageBlob = new Blob([imageBytes], {type: contentType.split(";")[0]});
  const image = await RawImage.fromBlob(imageBlob);
  const features = await extractor(image);
  return normalizeEmbedding(features.data);
}

async function addItemWithImage(itemMetadata, imageUrl, options = {}) {
  if (!itemMetadata || !["lost", "found"].includes(String(itemMetadata.type || "").toLowerCase())) {
    throw new Error("Item type must be 'lost' or 'found'");
  }
  if (!itemMetadata.title || !imageUrl) throw new Error("Item title and image URL are required");

  const embedding = await generateImageEmbedding(imageUrl, options);
  const client = options.client || getSupabaseClient();
  const row = {
    source_id: itemMetadata.source_id || itemMetadata.id || null,
    title: String(itemMetadata.title).slice(0, 500),
    description: String(itemMetadata.description || ""),
    image_url: imageUrl,
    type: String(itemMetadata.type).toLowerCase(),
    created_at: itemMetadata.created_at || new Date().toISOString(),
    embedding,
  };

  let query = client.from("items").insert(row);
  if (row.source_id) {
    query = client.from("items").upsert(row, {onConflict: "source_id"});
  }
  const {data, error} = await query.select("id, source_id, title, description, image_url, type, created_at").single();
  if (error) throw new Error(`Supabase item save failed: ${error.message}`);
  return {...data, embedding};
}

async function findMatchingItems(imageUrl, searchType, options = {}) {
  const normalizedType = String(searchType || "").toLowerCase();
  if (!["lost", "found"].includes(normalizedType)) {
    throw new Error("Search type must be 'lost' or 'found'");
  }

  const queryEmbedding = options.queryEmbedding || await generateImageEmbedding(imageUrl, options);
  const client = options.client || getSupabaseClient();
  const filterType = normalizedType === "lost" ? "found" : "lost";
  const {data, error} = await client.rpc("match_items", {
    query_embedding: queryEmbedding,
    match_threshold: 0.70,
    match_count: 10,
    filter_type: filterType,
  });
  if (error) throw new Error(`Supabase image matching failed: ${error.message}`);
  return data || [];
}

async function indexMissingReportImages(candidateType, options = {}) {
  const normalizedType = String(candidateType || "").toLowerCase();
  if (!["lost", "found"].includes(normalizedType)) {
    throw new Error("Candidate type must be 'lost' or 'found'");
  }

  const client = options.client || getSupabaseClient();
  const tableName = `${normalizedType}_items`;
  const maxRows = Math.min(Math.max(Number(process.env.IMAGE_MATCH_MAX_INDEX_ROWS || 200), 1), 1000);
  const [{data: reports, error: reportsError}, {data: indexedRows, error: indexedError}] = await Promise.all([
    client.from(tableName)
        .select("id, title, description, image_url, created_at")
        .not("image_url", "is", null)
        .order("created_at", {ascending: false})
        .limit(maxRows),
    client.from("items")
        .select("source_id")
        .eq("type", normalizedType)
        .not("source_id", "is", null)
        .limit(maxRows),
  ]);
  if (reportsError) throw new Error(`Could not load ${normalizedType} image reports: ${reportsError.message}`);
  if (indexedError) throw new Error(`Could not load indexed image reports: ${indexedError.message}`);

  const indexedIds = new Set((indexedRows || []).map((row) => String(row.source_id)));
  const missing = (reports || []).filter((report) => !indexedIds.has(String(report.id)));
  let indexedCount = 0;
  for (let offset = 0; offset < missing.length; offset += 2) {
    const batch = missing.slice(offset, offset + 2);
    const indexed = await Promise.all(batch.map((report) => addItemWithImage({
      id: report.id,
      source_id: report.id,
      title: report.title || "Untitled item",
      description: report.description || "",
      type: normalizedType,
      created_at: report.created_at,
    }, report.image_url, {client})));
    indexedCount += indexed.length;
  }
  return indexedCount;
}

async function searchSimilarImages(imageUrl, candidateType, options = {}) {
  const normalizedType = String(candidateType || "").toLowerCase();
  if (!["lost", "found"].includes(normalizedType)) {
    throw new Error("Candidate type must be 'lost' or 'found'");
  }
  const searchType = normalizedType === "lost" ? "found" : "lost";
  return findMatchingItems(imageUrl, searchType, options);
}

module.exports = {
  addItemWithImage,
  findMatchingItems,
  generateImageEmbedding,
  indexMissingReportImages,
  searchSimilarImages,
};
