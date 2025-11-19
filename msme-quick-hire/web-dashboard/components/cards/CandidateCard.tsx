import React from 'react';
import { UserProfile, JobPersona } from '@/types';
import { Phone, Video, Star, ShieldCheck, CheckCircle, Eye } from 'lucide-react';

interface Props {
  candidate: UserProfile;
  persona: JobPersona;
  onCall: (type: 'audio' | 'video' | 'phone') => void;
  onHire: () => void;
  onViewProfile: () => void; // NEW PROP
}

export default function CandidateCard({ candidate, persona, onCall, onHire, onViewProfile }: Props) {
  return (
    <div className="bg-white p-4 rounded-xl shadow-lg w-80 border border-gray-100">
      {/* Header: Image & Name */}
      <div className="flex items-start gap-3">
        <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-xl font-bold text-blue-600">
          {candidate.name ? candidate.name[0] : "U"}
        </div>
        <div>
          <h3 className="font-bold text-lg flex items-center gap-1 text-slate-800">
            {candidate.name}
            {candidate.isIdVerified && (
              <ShieldCheck size={16} className="text-green-500" />
            )}
          </h3>
          <p className="text-sm text-gray-500">{persona.role}</p>
          <div className="flex items-center text-yellow-500 text-sm mt-1">
            <Star size={14} fill="currentColor" />
            <span className="ml-1 font-medium">{candidate.rating}</span>
          </div>
        </div>
      </div>

      {/* Skills Tags */}
      <div className="flex flex-wrap gap-2 my-3">
        {persona.skills.map((skill) => (
          <span key={skill} className="text-xs bg-gray-100 px-3 py-1 rounded-full text-gray-600 border border-gray-200">
            {skill}
          </span>
        ))}
      </div>

      {/* Communication Buttons */}
      <div className="grid grid-cols-3 gap-2 mt-4">
        {/* In-App Audio */}
        <button onClick={() => onCall('audio')} className="flex flex-col items-center justify-center p-2 bg-slate-50 hover:bg-blue-50 border border-slate-200 rounded-lg">
          <Phone size={18} className="text-blue-600" />
          <span className="text-[10px] font-bold mt-1">In-App</span>
        </button>

        {/* Video Call */}
        {candidate.privacySettings.allowVideoCalls ? (
          <button onClick={() => onCall('video')} className="flex flex-col items-center justify-center p-2 bg-slate-50 hover:bg-purple-50 border border-slate-200 rounded-lg">
            <Video size={18} className="text-purple-600" />
            <span className="text-[10px] font-bold mt-1">Video</span>
          </button>
        ) : (
          <div className="flex flex-col items-center justify-center p-2 bg-gray-50 border border-gray-100 rounded-lg opacity-50 cursor-not-allowed">
            <Video size={18} />
            <span className="text-[10px] font-medium">Hidden</span>
          </div>
        )}

        {/* Phone Call */}
        {candidate.privacySettings.showPhoneNumber ? (
          <button onClick={() => onCall('phone')} className="flex flex-col items-center justify-center p-2 bg-slate-50 hover:bg-green-50 border border-slate-200 rounded-lg">
            <Phone size={18} className="text-green-600" />
            <span className="text-[10px] font-bold mt-1">Call +91</span>
          </button>
        ) : (
          <div className="flex flex-col items-center justify-center p-2 bg-gray-50 border border-gray-100 rounded-lg opacity-50 cursor-not-allowed">
            <Phone size={18} />
            <span className="text-[10px] font-medium">Private</span>
          </div>
        )}
      </div>

      {/* Profile View & Hire Buttons */}
      <div className="grid grid-cols-2 gap-2 mt-3">
        {/* 👁️ NEW PROFILE BUTTON 👁️ */}
        <button 
          onClick={onViewProfile}
          className="py-3 px-2 rounded-lg font-bold flex items-center justify-center gap-2 transition-all border border-blue-400 text-blue-600 hover:bg-blue-50"
        >
          <Eye size={18} />
          View Profile
        </button>

        {/* HIRE BUTTON */}
        <button 
          onClick={onHire}
          className="py-3 px-2 bg-green-600 hover:bg-green-700 text-white rounded-lg font-bold flex items-center justify-center gap-2 shadow-md transition-all active:scale-95"
        >
          <CheckCircle size={18} />
          HIRE
        </button>
      </div>
    </div>
  );
}