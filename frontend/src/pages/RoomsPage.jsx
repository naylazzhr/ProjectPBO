import React, { useState } from 'react';
import { formatRupiah, hitungHargaPerMalam } from '../utils/hotelHelper';

export default function RoomsPage({ rooms, setRooms, searchQuery = '', onRefresh }) {
  const [form, setForm] = useState({
    nomor: '',
    lantai: 1,
    tipe: 'STANDARD',
    hargaDasar: 300000,
  });
  const [filterTipe, setFilterTipe] = useState('ALL');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [submitting, setSubmitting] = useState(false);

  const handleAddRoom = async (e) => {
    e.preventDefault();
    const payload = {
      nomor: form.nomor.trim(),
      lantai: Number(form.lantai),
      tipe: form.tipe,
      hargaDasar: Number(form.hargaDasar),
    };

    setSubmitting(true);
    try {
      const res = await fetch('http://localhost:8080/api/rooms', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        alert(err.error || 'Gagal menambah kamar ke backend Java');
        return;
      }

      if (onRefresh) await onRefresh();
      setForm({ nomor: '', lantai: 1, tipe: 'STANDARD', hargaDasar: 300000 });
    } catch {
      setRooms([
        ...rooms,
        {
          ...payload,
          status: 'AVAILABLE',
          fasilitas: 'AC, TV, Kamar Mandi Dalam',
        },
      ]);
      setForm({ nomor: '', lantai: 1, tipe: 'STANDARD', hargaDasar: 300000 });
    } finally {
      setSubmitting(false);
    }
  };

  const filteredRooms = rooms.filter((r) => {
    const matchSearch =
      !searchQuery.trim() ||
      String(r.nomor).toLowerCase().includes(searchQuery.toLowerCase()) ||
      String(r.tipe).toLowerCase().includes(searchQuery.toLowerCase());
    const matchTipe = filterTipe === 'ALL' || r.tipe === filterTipe;
    const matchStatus = filterStatus === 'ALL' || r.status === filterStatus;
    return matchSearch && matchTipe && matchStatus;
  });

  const tersedia = rooms.filter((r) => r.status === 'AVAILABLE').length;
  const terisi = rooms.filter((r) => r.status === 'OCCUPIED').length;
  const dipesan = rooms.filter((r) => r.status === 'RESERVED').length;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-[#011C40]">Konfigurasi & Inventaris Unit Kamar</h1>
        <p className="text-sm text-slate-500">
          Kelola data kamar, tarif dasar per malam, dan status ketersediaan unit.
        </p>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200">
          <div className="text-xs font-semibold text-slate-500 uppercase">Tersedia</div>
          <div className="text-2xl font-bold text-emerald-600 mt-1">{tersedia} Unit</div>
        </div>
        <div className="bg-white p-4 rounded-xl border border-slate-200">
          <div className="text-xs font-semibold text-slate-500 uppercase">Dipesan</div>
          <div className="text-2xl font-bold text-amber-600 mt-1">{dipesan} Unit</div>
        </div>
        <div className="bg-white p-4 rounded-xl border border-slate-200">
          <div className="text-xs font-semibold text-slate-500 uppercase">Terisi</div>
          <div className="text-2xl font-bold text-blue-700 mt-1">{terisi} Unit</div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Form Tambah Kamar */}
        <form
          onSubmit={handleAddRoom}
          className="lg:col-span-4 bg-white p-5 rounded-xl border border-slate-200 space-y-4 h-fit"
        >
          <h2 className="font-bold text-[#011C40] text-base">Data Unit Kamar Baru</h2>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Nomor Kamar *</label>
            <input
              type="text"
              required
              placeholder="Contoh: 302"
              value={form.nomor}
              onChange={(e) => setForm({ ...form, nomor: e.target.value })}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Lantai *</label>
            <input
              type="number"
              min="1"
              required
              value={form.lantai}
              onChange={(e) => setForm({ ...form, lantai: e.target.value })}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Tipe Kamar *</label>
            <select
              value={form.tipe}
              onChange={(e) => setForm({ ...form, tipe: e.target.value })}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white"
            >
              <option value="STANDARD">STANDARD</option>
              <option value="DELUXE">DELUXE</option>
              <option value="SUITE">SUITE</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">
              Harga Dasar per Malam (Rp) *
            </label>
            <input
              type="number"
              min="10000"
              step="10000"
              required
              value={form.hargaDasar}
              onChange={(e) => setForm({ ...form, hargaDasar: e.target.value })}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm"
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-[#011C40] hover:bg-[#023859] text-white font-semibold py-2.5 rounded-lg text-sm transition disabled:opacity-60"
          >
            {submitting ? 'Menyimpan...' : 'Simpan Data Kamar'}
          </button>
        </form>

        {/* Tabel Daftar Kamar */}
        <div className="lg:col-span-8 bg-white rounded-xl border border-slate-200 overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex flex-wrap gap-3 justify-between items-center">
            <div className="flex gap-2">
              <select
                value={filterTipe}
                onChange={(e) => setFilterTipe(e.target.value)}
                className="border border-slate-300 rounded-lg px-3 py-1.5 text-xs font-medium bg-white"
              >
                <option value="ALL">Semua Tipe</option>
                <option value="STANDARD">STANDARD</option>
                <option value="DELUXE">DELUXE</option>
                <option value="SUITE">SUITE</option>
              </select>

              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="border border-slate-300 rounded-lg px-3 py-1.5 text-xs font-medium bg-white"
              >
                <option value="ALL">Semua Status</option>
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="RESERVED">RESERVED</option>
                <option value="OCCUPIED">OCCUPIED</option>
              </select>
            </div>
            <span className="text-xs text-slate-500">{filteredRooms.length} unit tampil</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-sm">
              <thead className="bg-slate-50 border-b text-xs uppercase text-slate-500">
                <tr>
                  <th className="p-3.5">No. Kamar</th>
                  <th className="p-3.5">Lantai</th>
                  <th className="p-3.5">Tipe</th>
                  <th className="p-3.5">Tarif / Malam</th>
                  <th className="p-3.5">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredRooms.map((room) => {
                  const tarif =
                    room.hargaPerMalam ?? hitungHargaPerMalam(room.tipe, room.hargaDasar);
                  return (
                    <tr key={room.nomor} className="hover:bg-slate-50/70">
                      <td className="p-3.5 font-bold text-[#011C40]">{room.nomor}</td>
                      <td className="p-3.5">Lantai {room.lantai}</td>
                      <td className="p-3.5 font-semibold">{room.tipe}</td>
                      <td className="p-3.5 font-semibold text-emerald-700">
                        {formatRupiah(tarif)}
                      </td>
                      <td className="p-3.5">
                        <span
                          className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                            room.status === 'AVAILABLE'
                              ? 'bg-emerald-100 text-emerald-800'
                              : room.status === 'RESERVED'
                              ? 'bg-amber-100 text-amber-800'
                              : 'bg-blue-100 text-blue-800'
                          }`}
                        >
                          {room.status}
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}