import express from "express";
import {createClient} from "@supabase/supabase-js";
import {env, RawImage, pipeline} from "@xenova/transformers";

const PORT = Number(process.env.PORT || 3000);
const MAX_IMAGE_BYTES = 10 * 1024 * 1024;
const MAX_REDIRECTS = 3;
const MODEL_ID = "Xenova/clip-vit-base-patch32";
const allowedImageHosts = new Set([
  "res.cloudinary.com",
  ...(process.env.IMAGE_MATCH_ALLOWED_HOSTS || "")
      .split(",")
      .map((host) => host.trim().toLowerCase())
      .filter(Boolean),
]);

const supabaseUrl = process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_ANON_KEY;
if (!supabaseUrl || !supabaseKey) {
  throw new Error("Set SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY (or SUPABASE_ANON_KEY)");
}

const supabase = createClient(supabaseUrl, supabaseKey, {
  auth: {persistSession: false, autoRefreshToken: false},
});

env.cacheDir = "/tmp/fendly-transformers-cache";
env.allowLocalModels = false;

const app = express();
app.use(express.json({limit: "1mb"}));

let imageExtractorPromise;

function normalizeType(value) {
  const type = String(value || "").trim().toLowerCase();
  return type === "lost" || type === "found" ? type : null;
}

function validateImageUrl(value) {
  let parsed;
  try {
    parsed = new URL(value);
  } catch (error) {
    throw new Error("imageUrl must be a valid HTTPS URL");
  }

  const hostname = parsed.hostname.toLowerCase();
  const isCloudinary = hostname.endsWith(".cloudinary.com");
  if (parsed.protocol !== "https:" || (!isCloudinary && !allowedImageHosts.has(hostname))) {
    throw new Error("imageUrl must use HTTPS and an allowlisted image host");
  }
  return parsed.toString();
}

async function downloadImage(imageUrl) {
  let currentUrl = validateImageUrl(imageUrl);
  const signal = AbortSignal.timeout(15000);

  for (let redirectCount = 0; redirectCount <= MAX_REDIRECTS; redirectCount++) {
    const response = await fetch(currentUrl, {redirect: "manual", signal});
    if (response.status >= 300 && response.status < 400) {
      const location = response.headers.get("location");
      if (!location || redirectCount === MAX_REDIRECTS) {
        throw new Error("Image URL redirected too many times");
      }
      currentUrl = validateImageUrl(new URL(location, currentUrl).toString());
      continue;
    }
    if (!response.ok) throw new Error(`Image download failed with HTTP ${response.status}`);

    const contentType = response.headers.get("content-type") || "";
    if (!contentType.toLowerCase().startsWith("image/")) {
      throw new Error("imageUrl did not return an image");
    }
    const contentLength = Number(response.headers.get("content-length") || 0);
    if (contentLength > MAX_IMAGE_BYTES) throw new Error("Image exceeds the 10 MB limit");

    const reader = response.body?.getReader();
    if (!reader) throw new Error("Image response had no body");
    const chunks = [];
    let totalBytes = 0;
    while (true) {
      const {done, value} = await reader.read();
      if (done) break;
      totalBytes += value.byteLength;
      if (totalBytes > MAX_IMAGE_BYTES) {
        await reader.cancel();
        throw new Error("Image exceeds the 10 MB limit");
      }
      chunks.push(Buffer.from(value));
    }
    if (totalBytes === 0) throw new Error("Downloaded image is empty");
    return {
      bytes: Buffer.concat(chunks),
      contentType: contentType.split(";")[0],
    };
  }

  throw new Error("Unable to download image");
}

async function getImageExtractor() {
  if (!imageExtractorPromise) {
    imageExtractorPromise = pipeline("image-feature-extraction", MODEL_ID, {quantized: true});
  }
  return imageExtractorPromise;
}

async function imageEmbeddingFromUrl(imageUrl) {
  const {bytes, contentType} = await downloadImage(imageUrl);
  const image = await RawImage.fromBlob(new Blob([bytes], {type: contentType}));
  const extractor = await getImageExtractor();
  const output = await extractor(image);
  const values = Array.from(output.data, Number);
  if (values.length !== 512 || values.some((value) => !Number.isFinite(value))) {
    throw new Error("CLIP did not return a valid 512-dimensional embedding");
  }

  const norm = Math.sqrt(values.reduce((sum, value) => sum + value * value, 0));
  if (!Number.isFinite(norm) || norm === 0) throw new Error("CLIP returned a zero embedding");
  return values.map((value) => value / norm);
}

app.post("/api/items", async (request, response) => {
  try {
    const {title, description = "", imageUrl, type: rawType} = request.body || {};
    const type = normalizeType(rawType);
    if (typeof title !== "string" || !title.trim() || typeof description !== "string" || !type) {
      return response.status(400).json({error: "title, description, imageUrl, and type (lost|found) are required"});
    }
    if (typeof imageUrl !== "string" || !imageUrl.trim()) {
      return response.status(400).json({error: "imageUrl is required"});
    }

    const embedding = await imageEmbeddingFromUrl(imageUrl);
    const {data, error} = await supabase
        .from("items")
        .insert({
          title: title.trim(),
          description: description.trim(),
          image_url: imageUrl,
          type,
          embedding,
        })
        .select("id, title, description, image_url, type, created_at")
        .single();
    if (error) {
      console.error("Supabase item insert failed:", error.message);
      return response.status(502).json({error: "Could not save item to Supabase"});
    }
    return response.status(201).json({item: data});
  } catch (error) {
    console.error("Item creation failed:", error);
    const isClientError = /imageUrl|image exceeds|image response|did not return|redirected/i.test(error.message);
    return response.status(isClientError ? 400 : 500).json({error: error.message || "Item creation failed"});
  }
});

app.post("/api/items/match", async (request, response) => {
  try {
    const {imageUrl, targetType: rawTargetType} = request.body || {};
    const targetType = normalizeType(rawTargetType);
    if (typeof imageUrl !== "string" || !imageUrl.trim() || !targetType) {
      return response.status(400).json({error: "imageUrl and targetType (lost|found) are required"});
    }

    const queryEmbedding = await imageEmbeddingFromUrl(imageUrl);
    const filterType = targetType === "found" ? "lost" : "found";
    const {data, error} = await supabase.rpc("match_items", {
      query_embedding: queryEmbedding,
      match_threshold: 0.70,
      match_count: 10,
      filter_type: filterType,
    });
    if (error) {
      console.error("Supabase image match RPC failed:", error.message);
      return response.status(502).json({error: "Could not search matching items"});
    }
    return response.json({matches: data || []});
  } catch (error) {
    console.error("Image matching failed:", error);
    const isClientError = /imageUrl|image exceeds|image response|did not return|redirected|targetType/i.test(error.message);
    return response.status(isClientError ? 400 : 500).json({error: error.message || "Image matching failed"});
  }
});

app.use((error, request, response, next) => {
  console.error("Unhandled server error:", error);
  if (response.headersSent) return next(error);
  return response.status(500).json({error: "Internal server error"});
});

app.listen(PORT, () => {
  console.log(`Fendly image matching API listening on port ${PORT}`);
});
