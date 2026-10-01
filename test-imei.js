import dotenv from 'dotenv';
dotenv.config();

const BASE_URL = 'https://fendly-api.onrender.com';
const TOKEN = process.env.FIREBASE_ID_TOKEN;

const VALID_IMEI = '358241091234567';
const INVALID_IMEI = '111111111111111';

if (!TOKEN) {
  console.error('❌ ERROR: FIREBASE_ID_TOKEN missing in .env. Run node create-test-user.js first.');
  process.exit(1);
}

async function runImeiTests() {
  console.log(`\n📱 Starting IMEI Matching Terminal Tests on ${BASE_URL}\n`);

  console.log('--- [Test 1/4] Testing Invalid IMEI Rejection (Luhn Check) ---');
  try {
    const res1 = await fetch(`${BASE_URL}/api/items`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${TOKEN}`
      },
      body: JSON.stringify({
        title: 'Test Phone Invalid IMEI',
        description: 'Testing Luhn validation',
        type: 'lost',
        category: 'electronics',
        imei: INVALID_IMEI
      })
    });

    const data1 = await res1.json();
    if (res1.status === 400) {
      console.log(`✅ PASSED: Server correctly rejected invalid IMEI (HTTP 400): "${data1.detail || data1.message}"`);
    } else {
      console.error(`❌ FAILED: Expected HTTP 400 for bad IMEI, but got HTTP ${res1.status}`);
    }
  } catch (err) {
    console.error('❌ Error in Test 1:', err.message);
  }

  console.log('\n--- [Test 2/4] Posting Lost Phone with Valid IMEI ---');
  try {
    const res2 = await fetch(`${BASE_URL}/api/items`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${TOKEN}`
      },
      body: JSON.stringify({
        title: 'Lost Galaxy Phone',
        description: 'Black phone lost near burdi road',
        type: 'lost',
        category: 'electronics',
        imei: VALID_IMEI
      })
    });

    const data2 = await res2.json();
    if (res2.status === 201) {
      console.log(`✅ PASSED: Item created successfully (HTTP 201). Item ID: ${data2.id || data2.item?.id}`);
    } else {
      console.error(`❌ FAILED: Expected HTTP 201, got HTTP ${res2.status}:`, data2);
    }
  } catch (err) {
    console.error('❌ Error in Test 2:', err.message);
  }

  console.log('\n--- [Test 3/4] Searching Item by Exact IMEI ---');
  let matchResults = null;
  try {
    const res3 = await fetch(`${BASE_URL}/api/items/match`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${TOKEN}`
      },
      body: JSON.stringify({
        imei: VALID_IMEI,
        targetType: 'lost'
      })
    });

    matchResults = await res3.json();
    if (res3.status === 200 && Array.isArray(matchResults) && matchResults.length > 0) {
      console.log(`✅ PASSED: Exact IMEI match found! Returned ${matchResults.length} match(es).`);
      console.log(`   Match Type: ${matchResults[0].matchType || 'EXACT_IMEI'}`);
      console.log(`   Score: ${matchResults[0].score}`);
    } else {
      console.error(`❌ FAILED: Expected exact IMEI match (HTTP 200), got HTTP ${res3.status}:`, matchResults);
    }
  } catch (err) {
    console.error('❌ Error in Test 3:', err.message);
  }

  console.log('\n--- [Test 4/4] Checking IMEI Privacy Masking ---');
  if (matchResults && matchResults[0] && matchResults[0].item) {
    const returnedImei = matchResults[0].item.imei;
    console.log(`   Raw Returned IMEI field: "${returnedImei}"`);
    if (returnedImei && returnedImei.includes('*')) {
      console.log(`✅ PASSED: IMEI field is properly masked for privacy! (${returnedImei})`);
    } else if (returnedImei === VALID_IMEI) {
      console.warn(`⚠️ WARNING: Full raw IMEI returned without masking. Ensure privacy masking is applied.`);
    } else {
      console.log(`ℹ️ INFO: IMEI field returned value: ${returnedImei}`);
    }
  } else {
    console.log(`ℹ️ SKIP: No match results available to check privacy masking.`);
  }

  console.log('\n=============================================================');
  console.log('🎉 IMEI Test Suite Execution Complete!');
  console.log('=============================================================\n');
}

runImeiTests();
