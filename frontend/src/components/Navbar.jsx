import React from 'react';
import { Hotel, LogOut, BedDouble, FileText, Users, CalendarCheck, CreditCard } from 'lucide-react';

export default function Navbar({ currentUser, activeTab, setActiveTab, onLogout }) {
  return (
    <>
      <header className="bg-[#011C40] text-white shadow-md">
        <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <Hotel className="text-[#0FB5BA]" size={28} />
            <div>
              <h1 className="font-bold text-lg leading-tight">Hotel GrandLuxury</h1>
              <span className="text-xs text-[#A7EBF2]">
                Login sebagai: {currentUser.nama} ({currentUser.role})
              </span>
            </div>
          </div>
          <button
            onClick={onLogout}
            className="flex items-center gap-2 bg-red-600 hover:bg-red-700 px-4 py-2 rounded-lg text-sm font-medium transition"
          >
            <LogOut size={16} /> Logout
          </button>
        </div>
      </header>

      <div className="bg-[#023859] text-white border-b border-slate-700">
        <div className="max-w-7xl mx-auto px-6 flex gap-2 overflow-x-auto">
          {currentUser.role === 'ADMIN' ? (
            <>
              <TabButton active={activeTab === 'rooms'} onClick={() => setActiveTab('rooms')} icon={<BedDouble size={18} />} label="Manajemen Kamar" />
              <TabButton active={activeTab === 'reports'} onClick={() => setActiveTab('reports')} icon={<FileText size={18} />} label="Laporan Pendapatan" />
            </>
          ) : (
            <>
              <TabButton active={activeTab === 'guests'} onClick={() => setActiveTab('guests')} icon={<Users size={18} />} label="Data Tamu" />
              <TabButton active={activeTab === 'reservations'} onClick={() => setActiveTab('reservations')} icon={<CalendarCheck size={18} />} label="Reservasi Kamar" />
              <TabButton active={activeTab === 'checkout'} onClick={() => setActiveTab('checkout')} icon={<CreditCard size={18} />} label="Check-in & Check-out" />
            </>
          )}
        </div>
      </div>
    </>
  );
}

function TabButton({ active, onClick, icon, label }) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 transition ${
        active ? 'border-[#0FB5BA] text-[#A7EBF2]' : 'border-transparent text-slate-300 hover:text-white'
      }`}
    >
      {icon} {label}
    </button>
  );
}