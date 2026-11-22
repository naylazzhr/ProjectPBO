import React, { useState } from 'react';
import { formatRupiah, hitungHargaPerMalam } from '../utils/hotelHelper';

export default function ReservationsPage({
  guests,
  rooms,
  setRooms,
  reservations,
  setReservations,
  searchQuery = '',
  onRefresh,
}) {
  const [form, setForm] = useState({
    guestId: '',
    roomNumber: '',
    checkIn: '2026-11-23',
    checkOut: '2026-11-25',
  });
  const [filterTipe, setFilterTipe] = useState('ALL');
  const [submitting, setSubmitting] = useState(false);

  const availableRooms = rooms.filter((r) => {
    const isAvail = r.status === 'AVAILABLE';
    const matchTipe = filterTipe === 'ALL' || r.tipe === filterTipe;
    return isAvail && matchTipe;
  });

  const handleSelectRoomCard = (roomNumber) => {
    setForm((prev) => ({ ...prev, roomNumber }));
  };

  const handleCreateReservation = async (e) => {
    e.preventDefault();
    const payload = {
      guestId: form.guestId,
      roomNumber: form.roomNumber,
      checkIn: form.checkIn,
      checkOut: form.checkOut,
    };

    setSubmitting(true);
    try {
      const res = await fetch('http://localhost:8080/api/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        alert(err.error || 'Gagal membuat reservasi di backend Java');
        return;
      }

      if (onRefresh) await onRefresh();
      setForm({ guestId: '', roomNumber: '', checkIn: '2026-11-23', checkOut: '2026-11-25' });
    } catch {
      const room = rooms.find((r) => r.nomor === payload.roomNumber);
      const malam = Math.max(
        1,
        Math.ceil((new Date(payload.checkOut) - new Date(payload.checkIn)) / (1000 * 60 * 60 * 24))
      );
      const totalBiaya = room ? hitungHargaPerMalam(room.tipe, room.hargaDasar) * malam : 300000;
      const nextId = `R${String(reservations.length + 1).padStart(3, '0')}`;
      setReservations([...reservations, { reservationId: nextId, ...payload, status: 'BOOKED', totalBiaya }]);
      setRooms(rooms.map((r) => (r.nomor === payload.roomNumber ? { ...r, status: 'RESERVED' } : r)));
    } finally {
      setSubmitting(false);
    }
  };

  const filteredReservations = reservations.filter((res) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      String(res.reservationId || '').toLowerCase().includes(q) ||
      String(res.guestId || '').toLowerCase().includes(q) ||
      String(res.roomNumber || '').toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#011C40]">Reservasi & Cek Ketersediaan Kamar</h1>
          <p className="text-sm text-slate-500">
            Pilih kamar yang tersedia dan buat reservasi baru untuk tamu terdaftar.
          </p>
        </div>

        <div className="flex gap-2">
          {['ALL', 'STANDARD', 'DELUXE', 'SUITE'].map((tipe) => (
            <button
              key={tipe}
              type="button"
              onClick={() => setFilterTipe(tipe)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition ${
                filterTipe === tipe
                  ? 'bg-[#011C40] text-white border-[#011C40]'
                  : 'bg-white text-slate-600 border-slate-300 hover:bg-slate-50'
              }`}
            >
              {tipe === 'ALL' ? 'Semua' : tipe}
            </button>
          ))}
        </div>
      </div>

      {/* Kartu Kamar Tersedia */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {availableRooms.map((room) => {
          const tarif = room.hargaPerMalam ?? hitungHargaPerMalam(room.tipe, room.hargaDasar);
          const selected = form.roomNumber === room.nomor;
          return (
            <div
              key={room.nomor}
              className={`bg-white p-4 rounded-xl border transition flex flex-col justify-between ${
                selected ? 'border-[#0FB5BA] ring-2 ring-[#0FB5BA]/30' : 'border-slate-200'
              }`}
            >
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold uppercase text-cyan-700">{room.tipe}</span>
                  <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700">
                    AVAILABLE
                  </span>
                </div>
                <div className="text-lg font-bold text-[#011C40] mt-1">
                  Kamar {room.nomor} <span className="text-xs font-normal text-slate-500">(Lt. {room.lantai})</span>
                </div>
                <p className="text-xs text-slate-500 mt-1">{room.fasilitas}</p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                <div className="font-bold text-[#011C40] text-sm">{formatRupiah(tarif)}/malam</div>
                <button
                  type="button"
                  onClick={() => handleSelectRoomCard(room.nomor)}
                  className="bg-[#011C40] hover:bg-[#023859] text-white text-xs font-semibold px-3 py-1.5 rounded-lg transition"
                >
                  {selected ? 'Dipilih' : 'Pilih Kamar'}
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Form Reservasi */}
      <form
        onSubmit={handleCreateReservation}
        className="bg-white p-5 rounded-xl border border-slate-200 grid grid-cols-1 md:grid-cols-5 gap-4"
      >
        <select
          required
          value={form.guestId}
          onChange={(e) => setForm({ ...form, guestId: e.target.value })}
          className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white"
        >
          <option value="">-- Pilih Tamu --</option>
          {guests.map((g) => (
            <option key={g.guestId} value={g.guestId}>
              {g.guestId} - {g.nama}
            </option>
          ))}
        </select>

        <select
          required
          value={form.roomNumber}
          onChange={(e) => setForm({ ...form, roomNumber: e.target.value })}
          className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white"
        >
          <option value="">-- Pilih Kamar Kosong --</option>
          {rooms
            .filter((r) => r.status === 'AVAILABLE')
            .map((r) => (
              <option key={r.nomor} value={r.nomor}>
                Kamar {r.nomor} ({r.tipe})
              </option>
            ))}
        </select>

        <input
          type="date"
          required
          value={form.checkIn}
          onChange={(e) => setForm({ ...form, checkIn: e.target.value })}
          className="border border-slate-300 rounded-lg px-3 py-2 text-sm"
        />

        <input
          type="date"
          required
          value={form.checkOut}
          onChange={(e) => setForm({ ...form, checkOut: e.target.value })}
          className="border border-slate-300 rounded-lg px-3 py-2 text-sm"
        />

        <button
          type="submit"
          disabled={submitting}
          className="bg-[#0FB5BA] hover:bg-[#023859] text-white font-semibold rounded-lg px-4 py-2 text-sm transition disabled:opacity-60"
        >
          {submitting ? 'Memproses...' : 'Buat Reservasi'}
        </button>
      </form>

      {/* Tabel Reservasi */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-x-auto">
        <table className="w-full text-left border-collapse text-sm">
          <thead className="bg-slate-50 border-b text-xs uppercase text-slate-500">
            <tr>
              <th className="p-3.5">No. Reservasi</th>
              <th className="p-3.5">ID Tamu</th>
              <th className="p-3.5">Kamar</th>
              <th className="p-3.5">Check-In</th>
              <th className="p-3.5">Check-Out</th>
              <th className="p-3.5">Total Tagihan</th>
              <th className="p-3.5">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {filteredReservations.map((res) => (
              <tr key={res.reservationId} className="hover:bg-slate-50/70">
                <td className="p-3.5 font-bold text-[#011C40]">{res.reservationId}</td>
                <td className="p-3.5">{res.guestId}</td>
                <td className="p-3.5 font-semibold">Kamar {res.roomNumber}</td>
                <td className="p-3.5">{res.checkIn}</td>
                <td className="p-3.5">{res.checkOut}</td>
                <td className="p-3.5 font-bold text-emerald-700">{formatRupiah(res.totalBiaya)}</td>
                <td className="p-3.5">
                  <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-800">
                    {res.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}