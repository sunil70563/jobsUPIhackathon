// web-dashboard/lib/firebase.ts
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";

const firebaseConfig = {
  apiKey: "AIzaSyBG--pzwXydeqs4V592EciqxqIrSDYsne0",
  authDomain: "msme-quick-hire.firebaseapp.com",
  projectId: "msme-quick-hire",
  storageBucket: "msme-quick-hire.firebasestorage.app",
  messagingSenderId: "934444239278",
  appId: "1:934444239278:web:ae7842a9f0b94f17d7603f",
  measurementId: "G-HYE20JDVY8"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Export services
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);