import "dotenv/config";
import {chmod, readFile, writeFile} from "node:fs/promises";
import {fileURLToPath} from "node:url";
import {initializeApp} from "firebase/app";
import {
  createUserWithEmailAndPassword,
  getIdToken,
  inMemoryPersistence,
  initializeAuth,
  signInWithEmailAndPassword,
  signOut,
} from "firebase/auth";

const TEST_EMAIL = process.env.FIREBASE_TEST_EMAIL || "testuser@fendly.app";
const TEST_PASSWORD = process.env.FIREBASE_TEST_PASSWORD || "TestPassword123!";
const ENV_FILE_PATH = fileURLToPath(new URL(".env", import.meta.url));
const GOOGLE_SERVICES_PATH = fileURLToPath(new URL("app/google-services.json", import.meta.url));

async function loadFirebaseConfig() {
  let androidConfig = {};
  try {
    androidConfig = JSON.parse(await readFile(GOOGLE_SERVICES_PATH, "utf8"));
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
  }

  const client = androidConfig.client?.[0];
  const config = {
    apiKey: process.env.FIREBASE_API_KEY || client?.api_key?.[0]?.current_key,
    authDomain: process.env.FIREBASE_AUTH_DOMAIN,
    projectId: process.env.FIREBASE_PROJECT_ID || androidConfig.project_info?.project_id,
    appId: process.env.FIREBASE_APP_ID || client?.client_info?.mobilesdk_app_id,
  };
  if (!config.authDomain && config.projectId) {
    config.authDomain = `${config.projectId}.firebaseapp.com`;
  }

  const missing = Object.entries(config)
      .filter(([key, value]) => key !== "authDomain" && !value)
      .map(([key]) => key);
  if (missing.length > 0) {
    throw new Error(`Firebase config is incomplete (${missing.join(", ")}); set FIREBASE_API_KEY, FIREBASE_PROJECT_ID, and FIREBASE_APP_ID`);
  }
  return config;
}

async function saveIdTokenToEnv(idToken) {
  let content = "";
  try {
    content = await readFile(ENV_FILE_PATH, "utf8");
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
  }

  const lines = content.split(/\r?\n/);
  const tokenLine = `FIREBASE_ID_TOKEN=${idToken}`;
  const existingIndex = lines.findIndex((line) => line.startsWith("FIREBASE_ID_TOKEN="));
  if (existingIndex >= 0) {
    lines[existingIndex] = tokenLine;
  } else {
    while (lines.length > 0 && lines[lines.length - 1] === "") lines.pop();
    lines.push(tokenLine);
  }

  await writeFile(ENV_FILE_PATH, `${lines.join("\n")}\n`, {mode: 0o600});
  await chmod(ENV_FILE_PATH, 0o600);
}

async function main() {
  const firebaseApp = initializeApp(await loadFirebaseConfig(), "fendly-test-user");
  const auth = initializeAuth(firebaseApp, {persistence: inMemoryPersistence});

  let userCredential;
  try {
    userCredential = await createUserWithEmailAndPassword(auth, TEST_EMAIL, TEST_PASSWORD);
    console.log(`Created Firebase test user ${TEST_EMAIL}.`);
  } catch (error) {
    if (error.code !== "auth/email-already-in-use" && error.code !== "auth/email-already-exists") {
      throw error;
    }
    userCredential = await signInWithEmailAndPassword(auth, TEST_EMAIL, TEST_PASSWORD);
    console.log(`Signed in existing Firebase test user ${TEST_EMAIL}.`);
  }

  try {
    const idToken = await getIdToken(userCredential.user, true);
    await saveIdTokenToEnv(idToken);
    console.log(`Saved FIREBASE_ID_TOKEN to ${ENV_FILE_PATH} (file permissions set to 600).`);
    console.log("Firebase ID token:");
    console.log(idToken);
  } finally {
    await signOut(auth);
  }
}

main().catch((error) => {
  console.error("Could not create/sign in the Firebase test user:", error.message || error);
  process.exitCode = 1;
});
