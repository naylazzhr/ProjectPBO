import React, { useState } from 'react';

export default function GuestsPage({ guests, setGuests, searchQuery = '', onRefresh }) {
  const [form, setForm] = useState({
    nama: '',
    nik: '',
    noTelepon: '',
    email: '',
  });
  const [submitting, setSubmitting] = useState(false);

  const handleAddGuest = async (e) => {
    e.preventDefault();
    const payload = {
      nama: form.nama.trim(),
      nik: form.nik.trim(),
      noTelepon: form.noTelepon.trim(),
      email: form.email.trim(),
    };

    if (!/^\d{16}$/.test(payload.nik)) {
      return alert('NIK harus 16 digit angka.');
    }

    setSubmitting(true);
    try {
      const res = await fetch('http://localhost:8080/api/guests', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        alert(err.error || 'Gagal menyimpan data tamu ke backend Java');
        return;
      }

      if (onRefresh) await onRefresh();
      setForm({ nama: '', nik: '', noTelepon: '', email: '' });
    } catch {
      const nextId = `G${String(guests.length + 1).padStart(3, '0')}`;
      setGuests([...guests, { guestId: nextId, ...payload }]);
      setForm({ nama: '', nik: '', noTelepon: '', email: '' });
    } finally {
      setSubmitting(false);
    }
  };

  const filteredGuests = guests.filter((g) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      String(g.nama || '').toLowerCase().includes(q) ||
      String(g.nik || '').toLowerCase().includes(q) ||
      String(g.guestId || '').toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-[#011C40]">Manajemen Data Tamu</h1>
        <p className="text-sm text-slate-500">
          Registrasi profil tamu baru dan lihat daftar tamu yang tersimpan di sistem.
        </p>
      </div>

      {/* Stat Ringkas */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200">
          <div className="text-xs font-semibold text-slate-500 uppercase">Total Tamu Terdaftar</div>
          <div className="text-2xl font-bold text-[#011C40] mt-1">{guests.length} Profil</div>
        </div>
        <div className="bg-white p-4 rounded-xl border border-slate-200">
          <div className="text-xs font-semibold text-slate-500 uppercase">ID Tamu Berikutnya</div>
          <div className="text-2xl font-bold text-cyan-700 mt-1">
            G{String(guests.length + 1).padStart(3, '0')}
          </div>
        </div>
      </div>

      {/* Form Registrasi Tamu */}
      <form
        onSubmit={handleAddGuest}
        className="bg-white p-5 rounded-xl border border-slate-200 grid grid-cols-1 md:grid-cols-2 gap-4"
      >
        <h2 className="md:col-span-2 font-bold text-[#011C40] text-base">Form Registrasi Tamu Baru</h2>

        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">Nama Lengkap *</label>
          <input
            type="text"
            required
            placeholder="Contoh: Hendra Gunawan"
            value={form.nama}
            onChange={(e) => setForm({ ...form, nama: e.target.value })}
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">
            NIK (16 Digit Angka) *
          </label>
          <input
            type="text"
            required
            maxLength={16}
            placeholder="3524010101900001"
            value={form.nik}
            onChange={(e) => setForm({ ...form, nik: e.target.value.replace(/\D/g, '') })}
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">No. Telepon *</label>
          <input
            type="text"
            required
            placeholder="081234567890"
            value={form.noTelepon}
            onChange={(e) => setForm({ ...form, noTelepon: e.target.value })}
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">Email *</label>
          <input
            type="email"
            required
            placeholder="tamu@mail.com"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm"
          />
        </div>

        <div className="md:col-span-2 flex justify-end">
          <button
            type="submit"
            disabled={submitting}
            className="bg-[#0FB5BA] hover:bg-[#023859] text-white font-semibold px-5 py-2.5 rounded-lg text-sm transition disabled:opacity-60"
          >
            {submitting ? 'Mendaftarkan...' : '+ Daftarkan Tamu Baru'}
          </button>
        </div>
      </form>

      {/* Tabel Tamu */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-x-auto">
        <table className="w-full text-left border-collapse text-sm">
          <thead className="bg-slate-50 border-b text-xs uppercase text-slate-500">
            <tr>
              <th className="p-3.5">ID Tamu</th>
              <th className="p-3.5">Nama Lengkap</th>
              <th className="p-3.5">NIK</th>
              <th className="p-3.5">No. Telepon</th>
              <th className="p-3.5">Email</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {filteredGuests.map((guest) => (
              <tr key={guest.guestId} className="hover:bg-slate-50/70">
                <td className="p-3.5 font-bold text-[#011C40]">{guest.guestId}</td>
                <td className="p-3.5 font-semibold">{guest.nama}</td>
                <td className="p-3.5">{guest.nik}</td>
                <td className="p-3.5">{guest.noTelepon}</td>
                <td className="p-3.5">{guest.email}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}