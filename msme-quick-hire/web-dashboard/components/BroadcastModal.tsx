import React, { useState } from 'react';
import { AlertTriangle, Send, X } from 'lucide-react';

interface BroadcastModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSend: (role: string, wage: string) => void;
}

export default function BroadcastModal({ isOpen, onClose, onSend }: BroadcastModalProps) {
  const [role, setRole] = useState('');
  const [wage, setWage] = useState('');
  const [isSending, setIsSending] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = () => {
    if (!role || !wage) return;
    
    setIsSending(true);
    
    // Simulate a short "network" delay for effect
    setTimeout(() => {
      onSend(role, wage);
      setIsSending(false);
      setRole('');
      setWage('');
      onClose();
    }, 800);
  };

  return (
    <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-white w-full max-w-md rounded-2xl shadow-2xl overflow-hidden transform transition-all scale-100">
        
        {/* Header - Red Panic Style */}
        <div className="bg-red-600 p-6 flex items-center justify-between">
          <div className="flex items-center gap-3 text-white">
            <div className="p-2 bg-red-500 rounded-lg border border-red-400 animate-pulse">
               <AlertTriangle size={24} />
            </div>
            <div>
              <h2 className="text-xl font-bold">Urgent Broadcast</h2>
              <p className="text-red-100 text-xs">Notify all candidates within 2km</p>
            </div>
          </div>
          <button onClick={onClose} className="text-white/80 hover:text-white">
            <X size={24} />
          </button>
        </div>

        {/* Body */}
        <div className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Who do you need?</label>
            <input
              type="text"
              placeholder="e.g. Security Guard, Dishwasher"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 outline-none transition-all"
              value={role}
              onChange={(e) => setRole(e.target.value)}
              autoFocus
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Hourly Rate (₹)</label>
            <input
              type="number"
              placeholder="e.g. 500"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 outline-none transition-all"
              value={wage}
              onChange={(e) => setWage(e.target.value)}
            />
          </div>

          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 flex gap-2 items-start">
            <div className="text-yellow-600 mt-0.5">⚠️</div>
            <p className="text-xs text-yellow-800 leading-relaxed">
              This will trigger a high-priority system notification on all candidate devices nearby. Use only for emergencies.
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="p-6 pt-0">
          <button
            onClick={handleSubmit}
            disabled={!role || !wage || isSending}
            className={`w-full py-4 rounded-xl font-bold text-white flex items-center justify-center gap-2 transition-all
              ${!role || !wage 
                ? 'bg-gray-300 cursor-not-allowed' 
                : 'bg-red-600 hover:bg-red-700 shadow-lg hover:shadow-red-500/30 active:scale-[0.98]'
              }
            `}
          >
            {isSending ? (
              <>Broadcasting...</>
            ) : (
              <>
                <Send size={20} />
                SEND ALERT
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}