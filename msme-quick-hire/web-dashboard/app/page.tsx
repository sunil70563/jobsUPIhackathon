"use client";

import dynamic from 'next/dynamic';
import { useState, useEffect } from 'react';
import { UserProfile, JobPersona } from '@/types';
import { Mic, Search, X, Siren } from 'lucide-react';
import { db } from '@/lib/firebase'; 
import { collection, onSnapshot, query, where, addDoc } from 'firebase/firestore';
import BroadcastModal from '@/components/BroadcastModal';
import CandidateProfile from '@/components/CandidateProfile'; 

// Define a Mock Candidate to ensure the map is always rendered (The Fix)
const MOCK_CANDIDATE: { user: UserProfile; persona: JobPersona }[] = [
  {
    user: { 
        userId: "MOCK_001", 
        name: "Mock Demo", 
        phone: "919876543210", 
        role: "SEEKER", 
        rating: 4.0, 
        isIdVerified: true,
        privacySettings: { showPhoneNumber: true, allowVideoCalls: true }
    },
    persona: {
        personaId: "MOCK_P_001",
        userId: "MOCK_001",
        role: "Demo Worker",
        skills: ["Mocked Profile", "No Database Needed"],
        location: { lat: 12.9716, lng: 77.5946, address: "Bangalore Center" },
        isActive: true,
        videoIntro: "" // Placeholder for video URL
    } as JobPersona
  }
];

// Dynamic Map Import
const MapView = dynamic(() => import('@/components/map/Map'), { 
  ssr: false,
  loading: () => <div className="h-full w-full bg-slate-100 flex items-center justify-center">Loading Map...</div>
});

export default function Dashboard() {
  const [isListening, setIsListening] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [isBroadcastOpen, setIsBroadcastOpen] = useState(false); 
  
  // Initialize state with MOCK_CANDIDATE to ensure map rendering
  const [candidates, setCandidates] = useState<{ user: UserProfile; persona: JobPersona }[]>(MOCK_CANDIDATE);
  const [filteredCandidates, setFilteredCandidates] = useState<{ user: UserProfile; persona: JobPersona }[]>(MOCK_CANDIDATE);
  
  const [selectedCandidate, setSelectedCandidate] = useState<{ user: UserProfile; persona: JobPersona } | null>(null);

  // 1. Fetch Data from Firestore Real-time
  useEffect(() => {
    const q = query(collection(db, "personas"), where("isActive", "==", true));
    
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const fetchedData: { user: UserProfile; persona: JobPersona }[] = [];
      
      snapshot.forEach((doc) => {
        const data = doc.data();
        fetchedData.push({
            user: { 
                userId: data.userId, 
                name: "Candidate " + data.userId.slice(-4), 
                phone: data.phone || "+91 98765 43210", 
                role: "SEEKER", 
                rating: 4.8, 
                isIdVerified: true,
                privacySettings: { 
                    showPhoneNumber: data.showPhoneNumber !== undefined ? data.showPhoneNumber : true, 
                    allowVideoCalls: data.allowVideoCalls !== undefined ? data.allowVideoCalls : true 
                }
            },
            persona: {
                personaId: doc.id,
                userId: data.userId,
                role: data.role,
                skills: data.skills || [],
                location: data.location,
                isActive: data.isActive,
                videoIntro: data.videoIntro
            } as JobPersona 
        });
      });
      
      // If real data exists, use it. Otherwise, use mock.
      if (fetchedData.length > 0) {
        setCandidates(fetchedData);
        setFilteredCandidates(fetchedData);
      } else {
        setCandidates(MOCK_CANDIDATE);
        setFilteredCandidates(MOCK_CANDIDATE);
      }
    });

    return () => unsubscribe();
  }, []);

  // 2. Search & Filter Logic
  useEffect(() => {
    if (!searchQuery) {
      setFilteredCandidates(candidates);
      return;
    }
    const lowerQuery = searchQuery.toLowerCase();
    const filtered = candidates.filter(item => 
      item.persona.role.toLowerCase().includes(lowerQuery) || 
      item.persona.skills.some((s: string) => s.toLowerCase().includes(lowerQuery))
    );
    setFilteredCandidates(filtered);
  }, [searchQuery, candidates]);

  // 3. Handle Broadcast Submission
  const handleSendBroadcast = async (role: string, wage: string) => {
    try {
      await addDoc(collection(db, "broadcasts"), {
        role: role,
        wage: wage,
        status: "ACTIVE",
        timestamp: new Date(),
        location: { lat: 12.9716, lng: 77.5946 } 
      });
    } catch (e) {
      console.error("Error broadcasting:", e);
    }
  };

  return (
    <div className="flex flex-col h-screen bg-slate-50">
      {/* Top Bar (Shared between Map and Profile) */}
      <div className="h-16 bg-white border-b border-gray-200 flex items-center px-6 justify-between z-20 shadow-sm">
        <h1 className="text-xl font-bold text-blue-700 flex items-center gap-2">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold text-lg">Q</div>
            QuickHire 
            <span className="text-slate-400 text-xs font-normal border-l border-slate-300 pl-2 ml-2 hidden sm:inline">Employer Dashboard</span>
        </h1>
        
        {/* Search Bar is visible only on Map View */}
        {!selectedCandidate && (
          <div className="flex items-center gap-2 bg-slate-100 px-4 py-2 rounded-full w-full max-w-md border border-slate-200 mx-4">
            <Search size={18} className="text-slate-400" />
            <input 
              type="text" 
              placeholder='Search "Driver", "Cook"...' 
              className="bg-transparent outline-none flex-1 text-sm"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            {searchQuery && <button onClick={() => setSearchQuery("")}><X size={16} className="text-slate-400 hover:text-slate-600" /></button>}
            <button className="bg-white shadow-sm text-blue-600 p-1.5 rounded-full hover:bg-blue-50 transition-colors"><Mic size={16} /></button>
          </div>
        )}
        <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-blue-700 rounded-full flex items-center justify-center text-white font-bold shadow-lg border-2 border-white ring-2 ring-blue-100">
           E
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 relative z-0">
        
        {/* Conditional Rendering: Map View or Profile View */}
        {selectedCandidate ? (
          <CandidateProfile 
            candidate={selectedCandidate.user}
            persona={selectedCandidate.persona}
            onBack={() => setSelectedCandidate(null)} // Go back to Map
          />
        ) : (
          <>
            <div className="absolute top-4 left-4 z-[1000] bg-white/90 backdrop-blur px-4 py-2 rounded-full shadow-lg border border-slate-200 text-xs font-bold text-slate-700 flex items-center gap-2">
              <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
              Live Candidates: {filteredCandidates.length}
            </div>

            <MapView 
              candidates={filteredCandidates} 
              center={[12.9716, 77.5946]} 
              onPinClick={setSelectedCandidate} 
            />
            
            {/* Urgent Broadcast Button */}
            <div className="absolute bottom-8 right-8 z-[1000]">
              <button 
                onClick={() => setIsBroadcastOpen(true)}
                className="group bg-red-600 hover:bg-red-700 text-white font-bold py-4 px-8 rounded-full shadow-xl shadow-red-600/30 flex items-center gap-3 transition-all hover:scale-105 active:scale-95"
              >
                <Siren size={24} className="group-hover:animate-pulse" />
                URGENT BROADCAST
              </button>
            </div>
          </>
        )}
      </div>

      {/* Broadcast Modal */}
      <BroadcastModal 
        isOpen={isBroadcastOpen} 
        onClose={() => setIsBroadcastOpen(false)} 
        onSend={handleSendBroadcast}
      />
    </div>
  );
}