// web-dashboard/types/index.ts

export interface UserProfile {
  userId: string;
  name: string;
  phone: string; // Masked in UI if privacy is ON
  role: "EMPLOYER" | "SEEKER";
  rating: number;
  isIdVerified: boolean;
  privacySettings: {
    showPhoneNumber: boolean;
    allowVideoCalls: boolean;
  };
}

export interface JobPersona {
  personaId: string;
  userId: string;
  role: string; // e.g., "Cook", "Driver"
  skills: string[];
  location: {
    lat: number;
    lng: number;
    address: string;
  };
  isActive: boolean; // Is he available right now?
}

export interface UrgentBroadcast {
  broadcastId: string;
  employerId: string;
  role: string;
  targetCount: number; // Mass hiring target (e.g., 5)
  hiredCount: number;
  status: "ACTIVE" | "FILLED";
  location: {
    lat: number;
    lng: number;
  };
}