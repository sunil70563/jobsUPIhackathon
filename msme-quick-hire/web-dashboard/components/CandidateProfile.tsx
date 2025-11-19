import React from 'react';
import { UserProfile, JobPersona } from '@/types';
import { ArrowLeft, Star, Video, Briefcase, Phone, MapPin, ShieldCheck, Mail } from 'lucide-react';

interface CandidateProfileProps {
  candidate: UserProfile;
  persona: JobPersona;
  onBack: () => void;
}

// Dummy Reviews Data
const DUMMY_REVIEWS = [
  { id: 1, employer: "Ramesh Sweets", rating: 5, comment: "Excellent work ethic, very polite and always on time.", role: "Cook" },
  { id: 2, employer: "Saroj Textiles", rating: 4, comment: "Reliable driver, knows the city well. Gave 4 stars for being 5 mins late one day.", role: "Driver" },
  { id: 3, employer: "Kiran Cafe", rating: 5, comment: "Great attitude! Solved a kitchen issue instantly.", role: "Cook" },
];

export default function CandidateProfile({ candidate, persona, onBack }: CandidateProfileProps) {
  const videoUrl = (persona as any).videoIntro; // Access the video URL

  return (
    <div className="flex flex-col h-[calc(100vh-64px)] overflow-y-auto bg-gray-50 p-6 md:p-8">
      {/* Back Button */}
      <button 
        onClick={onBack}
        className="text-blue-600 hover:text-blue-800 flex items-center gap-2 mb-6 self-start font-medium transition-colors"
      >
        <ArrowLeft size={20} />
        Back to Map
      </button>

      {/* Main Profile Header */}
      <div className="bg-white p-6 rounded-xl shadow-lg border border-gray-200 mb-6">
        <div className="flex items-center gap-6 mb-4">
          <div className="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center text-3xl font-bold text-blue-600">
            {candidate.name[0]}
          </div>
          <div>
            <h1 className="text-3xl font-extrabold text-gray-800 flex items-center gap-2">
              {candidate.name}
              {candidate.isIdVerified && <ShieldCheck size={20} className="text-green-500" />}
            </h1>
            <p className="text-xl text-blue-600 flex items-center gap-2 mt-1">
              <Briefcase size={18} /> {persona.role}
            </p>
          </div>
        </div>

        {/* Rating and Contact Info */}
        <div className="grid grid-cols-2 gap-4 mt-4 border-t pt-4">
          <div className="flex items-center text-yellow-500">
            <Star size={24} fill="currentColor" />
            <span className="ml-2 text-2xl font-bold">{candidate.rating}</span>
            <span className="ml-1 text-gray-500 text-sm">/ 5.0 (3 Reviews)</span>
          </div>
          <div className="flex flex-col">
             <div className="flex items-center text-gray-600">
                <MapPin size={16} />
                <span className="ml-2 text-sm">{persona.location.address || "Local Area"}</span>
             </div>
             <div className="flex items-center text-gray-600 mt-1">
                <Phone size={16} />
                <span className="ml-2 text-sm">{candidate.phone}</span>
             </div>
          </div>
        </div>
      </div>
      
      {/* Video Check & Skills */}
      <div className="grid md:grid-cols-2 gap-6 mb-6">
        {/* Video Check */}
        <div className="bg-white p-5 rounded-xl shadow-md border border-gray-200">
          <h3 className="text-lg font-bold mb-3 text-pink-600 flex items-center gap-2">
            <Video size={20} /> Video Introduction
          </h3>
          {videoUrl ? (
            <a href={videoUrl} target="_blank" rel="noopener noreferrer" className="block text-center bg-pink-600 text-white py-3 rounded-lg hover:bg-pink-700 transition-colors">
              ▶️ Play Vibe Check Video
            </a>
          ) : (
            <p className="text-sm text-gray-500">No video provided by candidate.</p>
          )}
        </div>
        
        {/* Skills */}
        <div className="bg-white p-5 rounded-xl shadow-md border border-gray-200">
          <h3 className="text-lg font-bold mb-3 text-blue-600 flex items-center gap-2">
            <Briefcase size={20} /> Core Skills
          </h3>
          <div className="flex flex-wrap gap-2">
            {persona.skills.map((skill, index) => (
              <span key={index} className="bg-blue-100 text-blue-800 px-3 py-1 text-sm rounded-full font-medium">
                {skill}
              </span>
            ))}
          </div>
        </div>
      </div>
      
      {/* Reviews Section */}
      <h2 className="text-2xl font-bold text-gray-800 mb-4">Reviews & History</h2>
      <div className="space-y-4">
        {DUMMY_REVIEWS.map((review) => (
          <div key={review.id} className="bg-white p-5 rounded-xl shadow border border-gray-200">
            <div className="flex justify-between items-center mb-2">
              <span className="text-md font-semibold">{review.employer}</span>
              <div className="flex items-center text-yellow-500">
                <Star size={16} fill="currentColor" />
                <span className="ml-1 font-bold">{review.rating}.0</span>
              </div>
            </div>
            <p className="text-gray-700 italic">"{review.comment}"</p>
            <p className="text-xs text-gray-500 mt-2">Hired as: {review.role}</p>
          </div>
        ))}
      </div>
    </div>
  );
}