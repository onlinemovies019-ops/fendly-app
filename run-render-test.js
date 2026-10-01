import {initializeApp} from "firebase/app";
import {
  getIdToken,
  inMemoryPersistence,
  initializeAuth,
  signInAnonymously,
  signInWithEmailAndPassword,
  signOut,
} from "firebase/auth";
import {runRenderTest} from "./test-render.js";

function requiredEnvironment(name) {
  const value = process.env[name];
  if (!value) throw new Error(`Set ${name} before running this test`);
  return value;
}

async function main() {
  const projectId = requiredEnvironment("FIREBASE_PROJECT_ID");
  const firebaseApp = initializeApp({
    apiKey: requiredEnvironment("FIREBASE_API_KEY"),
    authDomain: process.env.FIREBASE_AUTH_DOMAIN || `${projectId}.firebaseapp.com`,
    projectId,
    appId: requiredEnvironment("FIREBASE_APP_ID"),
  }, "fendly-render-api-test");
  const auth = initializeAuth(firebaseApp, {persistence: inMemoryPersistence});

  let userCredential;
  const email = process.env.FIREBASE_TEST_EMAIL;
  const password = process.env.FIREBASE_TEST_PASSWORD;
  if (Boolean(email) !== Boolean(password)) {
    throw new Error("Set both FIREBASE_TEST_EMAIL and FIREBASE_TEST_PASSWORD, or set neither to use anonymous auth");
  }
  if (email && password) {
    userCredential = await signInWithEmailAndPassword(auth, email, password);
    console.log("Signed in with the configured Firebase test account.");
  } else {
    userCredential = await signInAnonymously(auth);
    console.log("Signed in anonymously. Firebase Anonymous Authentication must be enabled.");
  }

  try {
    const idToken = await getIdToken(userCredential.user, true);
    console.log("Fetched a fresh Firebase ID token; token value will not be logged.");
    await runRenderTest(idToken);
  } finally {
    await signOut(auth);
  }
}

main().catch((error) => {
  console.error("Render API test failed:", error.message || error);
  process.exitCode = 1;
});
