import fetch from "node-fetch";
import dotenv from "dotenv";
dotenv.config();

const BASE_URL = process.env.API_URL || "https://fendly-api.onrender.com";
const AUTH_TOKEN = process.env.TEST_AUTH_TOKEN || "test_token_bypass"; // Or your test token

async function runTests() {
  console.log(`\n📱 Starting IMEI Matching Terminal Tests on ${BASE_URL}\n`);

  // --- [Test 1/4] Invalid IMEI Rejection ---
  console.log("--- [Test 1/4] Testing Invalid IMEI Rejection (Luhn Check) ---");
  try {
    const res = await fetch(`${BASE_URL}/api/items`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${AUTH_TOKEN}`
      },
      body: JSON.stringify({
        title: "Test Phone Invalid IMEI",
        description: "Testing Luhn validation",
        type: "lost",
        category: "electronics",
        lat: 19.0760,
        lng: 72.8777,
        imei: "123456789012345", // Invalid Luhn
        payment_id: "test_bypass"
      })
    });
    const data = await res.json();
    if (res.status === 400) {
      console.log(`✅ PASSED: Server correctly rejected invalid IMEI (HTTP 400):`, JSON.stringify(data.detail || data));
    } else {
      console.log(`❌ FAILED: Expected HTTP 400, got HTTP ${res.status}:`, data);
    }
  } catch (err) {
    console.log(`❌ ERROR in Test 1:`, err.message);
  }

  // --- [Test 2/4] Valid IMEI Item Creation ---
  console.log("\n--- [Test 2/4] Posting Lost Phone with Valid IMEI ---");
  let createdId = null;
  const validImei = "490154203237518"; // Valid 15-digit Luhn IMEI
  try {
    const res = await fetch(`${BASE_URL}/api/items`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${AUTH_TOKEN}`
      },
      body: JSON.stringify({
        title: "Test Lost Phone Valid IMEI",
        description: "Testing exact IMEI search",
        type: "lost",
        category: "electronics",
        lat: 19.0760,
        lng: 72.8777,
        imei: validImei,
        payment_id: "test_bypass"
      })
    });
    const data = await res.json();
    if (res.status === 201) {
      createdId = data.id;
      console.log(`✅ PASSED: Item created successfully (HTTP 201). Item ID: ${createdId}`);
    } else {
      console.log(`❌ FAILED: Expected HTTP 201, got HTTP ${res.status}:`, data);
    }
  } catch (err) {
    console.log(`❌ ERROR in Test 2:`, err.message);
  }

  // --- [Test 3/4] Searching Item by Exact IMEI ---
  console.log("\n--- [Test 3/4] Searching Item by Exact IMEI ---");
  try {
    const res = await fetch(`${BASE_URL}/api/items/match`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${AUTH_TOKEN}`
      },
      body: JSON.stringify({
        imei: validImei,
        targetType: "lost"
      })
    });
    const data = await res.json();
    if (res.status === 200 && Array.isArray(data) && data.length > 0) {
      console.log(`✅ PASSED: Found exact IMEI match (HTTP 200). Results count: ${data.length}`);
    } else {
      console.log(`❌ FAILED: Expected exact IMEI match (HTTP 200), got HTTP ${res.status}:`, data);
    }
  } catch (err) {
    console.log(`❌ ERROR in Test 3:`, err.message);
  }

  // --- [Test 4/4] Checking IMEI Privacy Masking ---
  console.log("\n--- [Test 4/4] Checking IMEI Privacy Masking ---");
  try {
    const res = await fetch(`${BASE_URL}/api/items/match`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${AUTH_TOKEN}`
      },
      body: JSON.stringify({
        imei: validImei,
        targetType: "lost"
      })
    });
    const data = await res.json();
    if (res.status === 200 && data.length > 0) {
      const returnedImei = data[0].item.imei;
      if (returnedImei.includes("******")) {
        console.log(`✅ PASSED: IMEI is securely masked in response: "${returnedImei}"`);
      } else {
        console.log(`❌ FAILED: IMEI was not masked: "${returnedImei}"`);
      }
    } else {
      console.log(`ℹ SKIP: No match results available to check privacy masking.`);
    }
  } catch (err) {
    console.log(`❌ ERROR in Test 4:`, err.message);
  }

  console.log("\n=============================================================");
  console.log("🎉 IMEI Test Suite Execution Complete!\n");
}

runTests();