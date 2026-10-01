const RENDER_BASE_URL = (process.env.RENDER_API_URL || "https://fendly-api.onrender.com").replace(/\/+$/, "");
const IMAGE_URL = "https://res.cloudinary.com/demo/image/upload/sample.jpg";
const REQUEST_TIMEOUT_MS = 120000;
const FIREBASE_ID_TOKEN = process.env.FIREBASE_ID_TOKEN || process.env.RENDER_API_TOKEN;

async function postJson(path, payload) {
  const response = await fetch(`${RENDER_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${FIREBASE_ID_TOKEN}`,
    },
    body: JSON.stringify(payload),
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
  });
  const responseText = await response.text();
  let data;
  try {
    data = responseText ? JSON.parse(responseText) : null;
  } catch {
    data = responseText;
  }

  console.log(`POST ${path}: HTTP ${response.status}`);
  if (!response.ok) {
    console.error("Response:", data);
    return {ok: false, data};
  }
  return {ok: true, data};
}

async function main() {
  if (!FIREBASE_ID_TOKEN) {
    throw new Error("Set FIREBASE_ID_TOKEN to a fresh Firebase ID token before testing the protected API");
  }

  console.log(`Testing Fendly Render API at ${RENDER_BASE_URL}`);

  let itemResult;
  try {
    itemResult = await postJson("/api/items", {
      title: "Test Watch",
      description: "Silver wrist watch",
      imageUrl: IMAGE_URL,
      type: "found",
      category: "accessories",
      lat: Number(process.env.TEST_LAT || 0),
      lng: Number(process.env.TEST_LNG || 0),
    });
  } catch (error) {
    console.error("Item request failed:", error.message);
    process.exitCode = 1;
  }

  let matchResult;
  if (itemResult?.ok) {
    const foundItemId = itemResult.data?.id;
    if (!foundItemId) {
      console.error("Item creation succeeded but the response did not include its id");
      process.exitCode = 1;
    } else {
      try {
        matchResult = await postJson("/api/items/match", {
          found_item_id: foundItemId,
          radius_degrees: Number(process.env.MATCH_RADIUS_DEGREES || 10),
        });
      } catch (error) {
        console.error("Match request failed:", error.message);
        process.exitCode = 1;
      }
    }
  }

  if (matchResult?.ok) {
    const matches = Array.isArray(matchResult.data)
      ? matchResult.data
      : matchResult.data?.matches || [];
    console.log("Matching results:");
    console.log(JSON.stringify(matches, null, 2));
    for (const match of matches) {
      const similarity = match.similarity ?? match.score ?? match.confidence;
      if (similarity !== undefined) {
        const item = match.item || match;
        console.log(`${item.title || item.id || "Match"}: similarity ${similarity}`);
      }
    }
  }

  if (itemResult && !itemResult.ok) process.exitCode = 1;
  if (matchResult && !matchResult.ok) process.exitCode = 1;
}

main().catch((error) => {
  console.error("Render API test failed:", error);
  process.exitCode = 1;
});
