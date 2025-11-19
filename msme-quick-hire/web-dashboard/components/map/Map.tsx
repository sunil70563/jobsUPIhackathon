// web-dashboard/components/map/Map.tsx
"use client";

import { doc, updateDoc } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { Icon } from 'leaflet';
import 'leaflet/dist/leaflet.css';
import CandidateCard from '@/components/cards/CandidateCard'; 
import { UserProfile, JobPersona } from '@/types';
import { Crosshair } from 'lucide-react'; 

// Fix for Leaflet default icon
const icon = new Icon({
  iconUrl: "https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

interface MapProps {
  candidates: { user: UserProfile; persona: JobPersona }[];
  center: [number, number];
}

// 1. New Component to Handle "Fly To Center" Logic
function RecenterControl({ center }: { center: [number, number] }) {
  const map = useMap(); // This hook gives us access to the map instance

  const handleRecenter = () => {
    map.flyTo(center, 15, {
      duration: 1.5 // Smooth animation speed
    });
  };

  return (
    <button
      onClick={handleRecenter}
      className="absolute top-4 right-4 z-[1000] bg-white p-3 rounded-lg shadow-md hover:bg-slate-50 border border-slate-200 text-blue-600"
      title="Reset View"
    >
      <Crosshair size={24} />
    </button>
  );
}

export default function MapView({ candidates, center }: MapProps) {
  return (
    // Force specific height calculation so it doesn't collapse
    <div className="h-[calc(100vh-64px)] w-full relative">
      <MapContainer 
        center={center} 
        zoom={15} 
        scrollWheelZoom={true}
        style={{ height: "100%", width: "100%" }}
      >
        {/* Map Tiles */}
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap contributors'
        />

        {/* The Recenter Button (Must be inside MapContainer) */}
        <RecenterControl center={center} />

        {/* Candidate Markers */}
        {candidates.map((data) => (
          <Marker 
            key={data.persona.personaId} 
            position={[data.persona.location.lat, data.persona.location.lng]}
            icon={icon}
          >
            <Popup className="min-w-[350px]">
               <CandidateCard 
                 candidate={data.user} 
                 persona={data.persona} 
                 onCall={(type) => console.log("Calling via:", type)} 
                 
                 // 🚀 THE HIRING LOGIC
                 onHire={async () => {
                    // 1. Update Firestore to trigger the Android "You're Hired" Screen
                    try {
                       const personaRef = doc(db, "personas", data.persona.personaId);
                       
                       await updateDoc(personaRef, {
                         status: "HIRED",
                         hiredBy: "Ramesh Sweets", // Mock Employer Name
                         hiredAt: new Date().toISOString()
                       });
                       
                       alert("Offer Sent! The candidate will see a pop-up instantly. 🚀");
                    } catch (e) {
                       console.error("Hire error:", e);
                       alert("Failed to update status. Check console.");
                    }

                    // 2. WhatsApp Link (Backup)
                    const text = `Hello ${data.user.name}! You are hired as a ${data.persona.role}!`;
                    // Use regex to strip non-numeric chars from phone, fallback to dummy
                    const cleanPhone = data.user.phone.replace(/[^0-9]/g, '') || "919876543210";
                    const whatsappUrl = `https://wa.me/${cleanPhone}?text=${encodeURIComponent(text)}`;
                    
                    // Open WhatsApp in new tab
                    window.open(whatsappUrl, '_blank');
                  }} 
               />
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}