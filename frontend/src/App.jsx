import React, { useState, useEffect } from 'react';
import {
  LayoutDashboard,
  BedDouble,
  Users,
  CalendarPlus,
  KeyRound,
  LogOut,
  Search,
  Bell,
} from 'lucide-react';
import InvoiceModal from './components/InvoiceModal';
import LoginPage from './pages/LoginPage';
import RoomsPage from './pages/RoomsPage';
import GuestsPage from './pages/GuestsPage';
import ReservationsPage from './pages/ReservationsPage';
import CheckoutPage from './pages/CheckoutPage';

const INITIAL_USERS = [
  { userId: 'U001', username: 'admin', password: '123', nama: 'Nayla (Admin)', role: 'ADMIN' },
  { userId: 'U002', username: 'resepsionis', password: '123', nama: 'Ceva (Resepsionis)', role: 'RECEPTIONIST' },
];

const INITIAL_ROOMS = [
  { nomor: '101', lantai: 1, tipe: 'STANDARD', hargaDasar: 300000, status: 'OCCUPIED', fasilitas: 'AC, TV, Kamar Mandi Dalam' },
  { nomor: '102', lantai: 1, tipe: 'STANDARD', hargaDasar: 300000, status: 'AVAILABLE', fasilitas: 'AC, TV, Kamar Mandi Dalam' },
  { nomor: '201', lantai: 2, tipe: 'DELUXE', hargaDasar: 300000, status: 'AVAILABLE', fasilitas: 'AC, TV, Minibar, Balkon' },
  { nomor: '202', lantai: 2, tipe: 'DELUXE', hargaDasar: 300000, status: 'RESERVED', fasilitas: 'AC, TV, Minibar, Balkon' },
  { nomor: '301', lantai: 3, tipe: 'SUITE', hargaDasar: 300000, status: 'AVAILABLE', fasilitas: 'AC, Smart TV, Minibar, Bathtub, Ruang Tamu' },
];

const INITIAL_GUESTS = [
  { guestId: 'G001', nama: 'Budi Santoso', nik: '3524010101900001', noTelepon: '081234567801', email: 'budi@mail.com' },
  { guestId: 'G002', nama: 'Siti Aminah', nik: '3524010202920002', noTelepon: '081234567802', email: 'siti@mail.com' },
];

const INITIAL_RESERVATIONS = [
  { reservationId: 'R001', guestId: 'G001', roomNumber: '101', checkIn: '2026-11-21', checkOut: '2026-11-23', status: 'CHECKED_IN', totalBiaya: 600000 },
  { reservationId: 'R002', guestId: 'G002', roomNumber: '202', checkIn: '2026-11-22', checkOut: '2026-11-24', status: 'BOOKED', totalBiaya: 780000 },
];

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [activeTab, setActiveTab] = useState('rooms');
  const [globalSearch, setGlobalSearch] = useState('');
  const [invoiceModal, setInvoiceModal] = useState(null);

  const [rooms, setRooms] = useState(INITIAL_ROOMS);
  const [guests, setGuests] = useState(INITIAL_GUESTS);
  const [reservations, setReservations] = useState(INITIAL_RESERVATIONS);
  const [payments, setPayments] = useState([]);

  const refreshAllFromBackend = async () => {
    const endpoints = [
      { path: '/api/rooms', setter: setRooms, allowEmpty: false },
      { path: '/api/guests', setter: setGuests, allowEmpty: false },
      { path: '/api/reservations', setter: setReservations, allowEmpty: false },
      { path: '/api/payments', setter: setPayments, allowEmpty: true },
    ];

    await Promise.all(
      endpoints.map(async ({ path, setter, allowEmpty }) => {
        try {
          const res = await fetch(`http://localhost:8080${path}`);
          if (!res.ok) return;
          const data = await res.json();
          if (Array.isArray(data) && (data.length > 0 || allowEmpty)) {
            setter(data);
          }
        } catch {
          // Pertahankan state awal jika server Java belum aktif
        }
      })
    );
  };

  useEffect(() => {
    refreshAllFromBackend();
  }, []);

  const handleLoginSuccess = (user) => {
    setCurrentUser(user);
    setActiveTab(user.role === 'ADMIN' ? 'rooms' : 'checkout');
  };

  if (!currentUser) {
    return <LoginPage users={INITIAL_USERS} onLoginSuccess={handleLoginSuccess} />;
  }

  const totalRevenue = payments.reduce((sum, p) => sum + Number(p.amount || 0), 0);
  const availableCount = rooms.filter((r) => r.status === 'AVAILABLE').length;
  const reservedCount = rooms.filter((r) => r.status === 'RESERVED').length;
  const occupiedCount = rooms.filter((r) => r.status === 'OCCUPIED').length;

  const navItems = [
    { id: 'overview', label: 'Dashboard Utama', icon: LayoutDashboard },
    { id: 'rooms', label: 'Manajemen Kamar', icon: BedDouble },
    { id: 'guests', label: 'Data Tamu', icon: Users },
    { id: 'reservations', label: 'Reservasi & Cek Kamar', icon: CalendarPlus },
    { id: 'checkout', label: 'Check-In, Check-Out & Invoice', icon: KeyRound },
  ];

  return (
    <div className="min-h-screen bg-[#F4F7FB] text-slate-800 flex">
      {/* Sidebar Kiri */}
      <aside className="w-64 bg-[#011C40] text-white flex flex-col justify-between shrink-0">
        <div>
          <div className="p-5 border-b border-white/10">
            <div className="font-bold text-lg tracking-wide">GrandLuxury</div>
            <div className="text-[11px] uppercase tracking-widest text-cyan-300">
              Hotel & Residences
            </div>
          </div>

          <div className="px-4 pt-5 pb-2 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
            Menu Operasional
          </div>

          <nav className="px-3 space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const active = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium text-left transition ${
                    active
                      ? 'bg-[#0FB5BA] text-white shadow-sm'
                      : 'text-slate-300 hover:bg-white/10 hover:text-white'
                  }`}
                >
                  <Icon className="w-4 h-4 shrink-0" />
                  <span className="leading-snug">{item.label}</span>
                </button>
              );
            })}
          </nav>
        </div>

        <div className="p-4 border-t border-white/10">
          <button
            onClick={() => setCurrentUser(null)}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-lg text-xs font-semibold bg-white/10 hover:bg-rose-600 transition"
          >
            <LogOut className="w-4 h-4" />
            Keluar Sesi
          </button>
        </div>
      </aside>

      {/* Area Konten Utama */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Topbar */}
        <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between gap-4">
          <div className="relative w-full max-w-md">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={globalSearch}
              onChange={(e) => setGlobalSearch(e.target.value)}
              placeholder="Cari tamu, no. kamar, atau ID reservasi..."
              className="w-full pl-10 pr-4 py-2 text-sm bg-slate-100 rounded-lg border border-transparent focus:bg-white focus:border-cyan-500 outline-none transition"
            />
          </div>

          <div className="flex items-center gap-4">
            <button
              onClick={refreshAllFromBackend}
              title="Sinkronkan ulang dari Backend Java"
              className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 transition relative"
            >
              <Bell className="w-4 h-4" />
            </button>
            <div className="text-right">
              <div className="text-sm font-bold text-[#011C40]">{currentUser.nama}</div>
              <div className="text-[11px] font-semibold uppercase text-cyan-700">
                {currentUser.role}
              </div>
            </div>
          </div>
        </header>

        <main className="p-6 max-w-7xl w-full mx-auto space-y-6">
          {activeTab === 'overview' && (
            <div className="space-y-6">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <h1 className="text-2xl font-bold text-[#011C40]">
                    Selamat Datang, {currentUser.nama}
                  </h1>
                  <p className="text-sm text-slate-500">
                    Ringkasan status unit kamar, reservasi aktif, dan settlement kasir.
                  </p>
                </div>
                <button
                  onClick={() => setActiveTab('reservations')}
                  className="bg-[#011C40] hover:bg-[#023859] text-white text-sm font-semibold px-4 py-2.5 rounded-lg transition"
                >
                  + Buat Reservasi Baru
                </button>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
                <div className="bg-white p-5 rounded-xl border border-slate-200">
                  <div className="text-xs font-semibold text-slate-500 uppercase">Kamar Tersedia</div>
                  <div className="text-3xl font-bold text-emerald-600 mt-1">{availableCount}</div>
                  <div className="text-xs text-slate-400 mt-1">Dari {rooms.length} total unit</div>
                </div>
                <div className="bg-white p-5 rounded-xl border border-slate-200">
                  <div className="text-xs font-semibold text-slate-500 uppercase">Kamar Dipesan</div>
                  <div className="text-3xl font-bold text-amber-600 mt-1">{reservedCount}</div>
                  <div className="text-xs text-slate-400 mt-1">Status RESERVED</div>
                </div>
                <div className="bg-white p-5 rounded-xl border border-slate-200">
                  <div className="text-xs font-semibold text-slate-500 uppercase">Sedang Menginap</div>
                  <div className="text-3xl font-bold text-blue-700 mt-1">{occupiedCount}</div>
                  <div className="text-xs text-slate-400 mt-1">Status OCCUPIED</div>
                </div>
                <div className="bg-white p-5 rounded-xl border border-slate-200">
                  <div className="text-xs font-semibold text-slate-500 uppercase">Settlement Kasir</div>
                  <div className="text-2xl font-bold text-[#011C40] mt-1">
                    Rp {totalRevenue.toLocaleString('id-ID')}
                  </div>
                  <div className="text-xs text-slate-400 mt-1">{payments.length} transaksi tercatat</div>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'rooms' && (
            <RoomsPage
              rooms={rooms}
              setRooms={setRooms}
              searchQuery={globalSearch}
              onRefresh={refreshAllFromBackend}
            />
          )}

          {activeTab === 'guests' && (
            <GuestsPage
              guests={guests}
              setGuests={setGuests}
              rooms={rooms}
              searchQuery={globalSearch}
              onRefresh={refreshAllFromBackend}
            />
          )}

          {activeTab === 'reservations' && (
            <ReservationsPage
              guests={guests}
              rooms={rooms}
              setRooms={setRooms}
              reservations={reservations}
              setReservations={setReservations}
              searchQuery={globalSearch}
              onRefresh={refreshAllFromBackend}
            />
          )}

          {activeTab === 'checkout' && (
            <CheckoutPage
              guests={guests}
              reservations={reservations}
              setReservations={setReservations}
              rooms={rooms}
              setRooms={setRooms}
              payments={payments}
              setPayments={setPayments}
              searchQuery={globalSearch}
              onShowInvoice={setInvoiceModal}
              onRefresh={refreshAllFromBackend}
            />
          )}
        </main>
      </div>

      <InvoiceModal invoiceData={invoiceModal} onClose={() => setInvoiceModal(null)} />
    </div>
  );
}