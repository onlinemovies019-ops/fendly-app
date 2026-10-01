/* eslint-disable require-jsdoc, max-len, indent */

const {logger} = require("firebase-functions");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {defineSecret} = require("firebase-functions/params");
const admin = require("firebase-admin");
const {createClient} = require("@supabase/supabase-js");
const {addItemWithImage, findMatchingItems} = require("./imageMatching");

admin.initializeApp();

const SUPABASE_URL = defineSecret("SUPABASE_URL");
const SUPABASE_SERVICE_ROLE_KEY = defineSecret("SUPABASE_SERVICE_ROLE_KEY");
const BREVO_API_KEY = defineSecret("BREVO_API_KEY");
const ADMIN_EMAIL = defineSecret("ADMIN_EMAIL");
const SENDER_EMAIL = defineSecret("SENDER_EMAIL");
const MATCH_SECRETS = [
  SUPABASE_URL,
  SUPABASE_SERVICE_ROLE_KEY,
  BREVO_API_KEY,
  ADMIN_EMAIL,
  SENDER_EMAIL,
];
const MATCH_THRESHOLD = 0.70;

async function handleReportCreated(event, reportType) {
  const report = event.data && event.data.data();
  if (!report) return;

  const imageUrl = report.image_url || report.imageUrl || report.photoUrl;
  if (!imageUrl) {
    logger.info("Skipping image matching for report without an image", {
      reportId: event.params.itemId,
      reportType,
    });
    return;
  }

  const supabaseUrl = SUPABASE_URL.value();
  const client = createClient(supabaseUrl, SUPABASE_SERVICE_ROLE_KEY.value(), {
    auth: {persistSession: false, autoRefreshToken: false},
  });
  const imageOptions = {client, allowedHosts: [new URL(supabaseUrl).hostname]};
  const item = await addItemWithImage({
    id: event.params.itemId,
    source_id: event.params.itemId,
    title: report.title || "Untitled item",
    description: report.description || "",
    type: reportType,
    created_at: report.created_at && typeof report.created_at.toDate === "function" ?
      report.created_at.toDate().toISOString() : undefined,
  }, imageUrl, imageOptions);

  const matches = await findMatchingItems(imageUrl, reportType, {
    client,
    queryEmbedding: item.embedding,
  });

  for (const match of matches) {
    const similarity = Number(match.similarity);
    if (!Number.isFinite(similarity) || similarity < MATCH_THRESHOLD) continue;

    const foundItem = reportType === "found" ? item : match;
    const lostItem = reportType === "lost" ? item : match;
    const {data: insertedAlert, error: insertError} = await client.from("admin_match_alerts").insert({
      found_item_id: foundItem.source_id,
      lost_item_id: lostItem.source_id,
      found_title: foundItem.title,
      lost_title: lostItem.title,
      confidence: similarity,
      reason: `CLIP image similarity is ${Math.round(similarity * 100)}%.`,
      is_read: false,
      email_sent: false,
    }).select("id").single();

    let alert = insertedAlert;
    if (insertError && insertError.code === "23505") {
      const {data: existingAlert, error: lookupError} = await client
          .from("admin_match_alerts")
          .select("id, email_sent")
          .eq("found_item_id", foundItem.source_id)
          .eq("lost_item_id", lostItem.source_id)
          .single();
      if (lookupError) throw new Error(`Could not load existing match alert: ${lookupError.message}`);
      if (existingAlert.email_sent) continue;
      alert = existingAlert;
    } else if (insertError) {
      throw new Error(`Could not save admin image-match alert: ${insertError.message}`);
    }

    try {
      const emailResponse = await fetch("https://api.brevo.com/v3/smtp/email", {
        method: "POST",
        headers: {
          "api-key": BREVO_API_KEY.value(),
          "content-type": "application/json",
        },
        body: JSON.stringify({
          sender: {name: "Fendly Notification Bot", email: SENDER_EMAIL.value()},
          to: [{email: ADMIN_EMAIL.value()}],
          subject: `Fendly image match: ${foundItem.title}`,
          htmlContent: `<h2>Fendly image match</h2><p>Found item: ${escapeHtml(foundItem.title)}</p><p>Possible lost item: ${escapeHtml(lostItem.title)}</p><p>Image similarity: ${Math.round(similarity * 100)}%</p>`,
        }),
      });
      if (!emailResponse.ok) throw new Error(`Brevo returned HTTP ${emailResponse.status}`);
      const {error: updateError} = await client
          .from("admin_match_alerts")
          .update({email_sent: true})
          .eq("id", alert.id);
      if (updateError) throw new Error(`Could not mark alert email sent: ${updateError.message}`);
    } catch (error) {
      logger.error("Unable to send admin image-match email", {
        alertId: alert.id,
        error: error.message,
      });
      throw error;
    }
  }
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "\"": "&quot;",
    "'": "&#39;",
  })[character]);
}

exports.onFoundItemCreated = onDocumentCreated({
  document: "found_items/{itemId}",
  secrets: MATCH_SECRETS,
  region: "us-central1",
  memory: "1GiB",
  timeoutSeconds: 300,
  maxInstances: 1,
  concurrency: 1,
  retry: true,
}, async (event) => handleReportCreated(event, "found"));

exports.onLostReportCreated = onDocumentCreated({
  document: "lost_items/{itemId}",
  secrets: MATCH_SECRETS,
  region: "us-central1",
  memory: "1GiB",
  timeoutSeconds: 300,
  maxInstances: 1,
  concurrency: 1,
  retry: true,
}, async (event) => handleReportCreated(event, "lost"));
