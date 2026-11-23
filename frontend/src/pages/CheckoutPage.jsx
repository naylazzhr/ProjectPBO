import React, { useState } from 'react';
import { formatRupiah } from '../utils/hotelHelper';

export default function CheckoutPage({
  guests = [],
  reservations,
  setReservations,
  rooms,
  setRooms,
  payments,
  setPayments,
  searchQuery = '',
  onShowInvoice,
  onRefresh,
}) {
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [paymentMethod, setPaymentMethod] = useState('QRIS');
  const [processingId, setProcessingId] = useState(null);

  const getGuestName = (guestId) => {
    const found = guests.find((g) => g.guestId === guestId);
    return found ? found.nama : guestId;
  };

  const handleAction = async (res, action) => {
    setProcessingId(res.reservationId);
    const tanggalHariIni = new Date().toLocaleDateString('id-ID');

    try {
      const putRes = await fetch('http://localhost:8080/api/reservations', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reservationId: res.reservationId, action }),
      });

      if (!putRes.ok) {
        const err = await putRes.json().catch(() => ({}));
        alert(err.error || `Gagal memproses ${action}`);
        return;
      }

      let createdPayment = null;
      if (action === 'CHECK_OUT') {
        const payRes = await fetch('http://localhost:8080/api/payments', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            reservationId: res.reservationId,
            amount: res.totalBiaya,
            method: paymentMethod,
            date: tanggalHariIni,
          }),
        });
        if (payRes.ok) {
          createdPayment = await payRes.json();
        }
      }

      if (onRefresh) await onRefresh();

      if (action === 'CHECK_OUT') {
        onShowInvoice({
          payment: createdPayment || {
            paymentId: `P${String(payments.length + 1).padStart(3, '0')}`,
            reservationId: res.reservationId,
            amount: res.totalBiaya,
            method: paymentMethod,
            date: tanggalHariIni,
          },
          reservation: { ...res, status: 'CHECKED_OUT' },
        });
      }
    } catch {
      const nextStatus = action === 'CHECK_IN' ? 'CHECKED_IN' : 'CHECKED_OUT';
      const nextRoomStatus = action === 'CHECK_IN' ? 'OCCUPIED' : 'AVAILABLE';

      setReservations(
        reservations.map((r) =>
          r.reservationId === res.reservationId ? { ...r, status: nextStatus } : r
        )
      );
      setRooms(
        rooms.map((rm) => (rm.nomor === res.roomNumber ? { ...rm, status: nextRoomStatus } : rm))
      );

      if (action === 'CHECK_OUT') {
        const newPayment = {
          paymentId: `P${String(payments.length + 1).padStart(3, '0')}`,
          reservationId: res.reservationId,
          amount: res.totalBiaya,
          method: paymentMethod,
          date: tanggalHariIni,
        };
        setPayments([...payments, newPayment]);
        onShowInvoice({ payment: newPayment, reservation: { ...res, status: 'CHECKED_OUT' } });
      }
    } finally {
      setProcessingId(null);
    }
  };

  const pendingCheckIn = reservations.filter((r) => r.status === 'BOOKED').length;
  const inHouse = reservations.filter((r) => r.status === 'CHECKED_IN').length;
  const completed = reservations.filter((r) => r.status === 'CHECKED_OUT').length;

  const visibleReservations = reservations.filter((res) => {
    const matchTab = statusFilter === 'ALL' || res.status === statusFilter;
    const guestName = getGuestName(res.guestId).toLowerCase();
    const q = searchQuery.toLowerCase();
    const matchSearch =
      !q ||
      String(res.reservationId).toLowerCase().includes(q) ||
      String(res.roomNumber).toLowerCase().includes(q) ||
      guestName.includes(q);
    return matchTab && matchSearch;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="text-xs font-bold uppercase tracking-wider text-cyan-700">
            Front Office & Kasir Resepsionis
          </div>
          <h1 className="text-2xl font-bold text-[#011C40]">
            Operasional Check-In, Check-Out & Invoice
          </h1>
          <p className="text-sm text-slate-500">
            Kelola kedatangan tamu, pelepasan kamar, settlement tagihan, dan cetak faktur.
          </p>
        </div>

        <div className="flex items-center gap-2 bg-white px-3.5 py-2 rounded-lg border border-slate-200 text-sm">
          <span className="text-slate-500">Metode Bayar:</span>
          <select
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
            className="font-semibold text-[#011C40] bg-transparent outline-none"
          >
            <option value="QRIS">QRIS</option>
            <option value="TRANSFER_BANK">TRANSFER BANK</option>
            <option value="CASH">CASH / TUNAI</option>
            <option value="CREDIT_CARD">CREDIT CARD</option>
          </select>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200">
          <div className="text-xs font-semibold text-slate-500 uppercase">Menunggu Check-In</div>
          <div className="text-2xl font-bold text-amber-600 mt-1">{pendingCheckIn}</div>
        </div>
        <div className="bg-white p-4 rounded-xl border border-slate-200">
          <div className="text-xs font-semibold text-slate-500 uppercase">Sedang Menginap</div>
          <div className="text-2xl font-bold text-blue-700 mt-1">{inHouse}</div>
        </div>
        <div className="bg-white p-4 rounded-xl border border-slate-200">
          <div className="text-xs font-semibold text-slate-500 uppercase">Selesai Check-Out</div>
          <div className="text-2xl font-bold text-emerald-600 mt-1">{completed}</div>
        </div>
      </div>

      {/* Tab Filter & Tabel */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-200 flex flex-wrap gap-2">
          {[
            { key: 'ALL', label: `Semua (${reservations.length})` },
            { key: 'BOOKED', label: `Siap Check-In (${pendingCheckIn})` },
            { key: 'CHECKED_IN', label: `In-House (${inHouse})` },
            { key: 'CHECKED_OUT', label: `Selesai (${completed})` },
          ].map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setStatusFilter(tab.key)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition ${
                statusFilter === tab.key
                  ? 'bg-[#011C40] text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-sm">
            <thead className="bg-slate-50 border-b text-xs uppercase text-slate-500">
              <tr>
                <th className="p-3.5">No. Reservasi</th>
                <th className="p-3.5">Nama Tamu</th>
                <th className="p-3.5">No. Kamar</th>
                <th className="p-3.5">Periode</th>
                <th className="p-3.5">Total Tagihan</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5">Aksi Cepat</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {visibleReservations.map((res) => (
                <tr key={res.reservationId} className="hover:bg-slate-50/70">
                  <td className="p-3.5 font-bold text-[#011C40]">{res.reservationId}</td>
                  <td className="p-3.5">
                    <div className="font-semibold">{getGuestName(res.guestId)}</div>
                    <div className="text-xs text-slate-400">{res.guestId}</div>
                  </td>
                  <td className="p-3.5 font-semibold">Kamar {res.roomNumber}</td>
                  <td className="p-3.5 text-xs">
                    {res.checkIn} s/d {res.checkOut}
                  </td>
                  <td className="p-3.5 font-bold text-emerald-700">
                    {formatRupiah(res.totalBiaya)}
                  </td>
                  <td className="p-3.5">
                    <span
                      className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                        res.status === 'BOOKED'
                          ? 'bg-amber-100 text-amber-800'
                          : res.status === 'CHECKED_IN'
                          ? 'bg-blue-100 text-blue-800'
                          : 'bg-emerald-100 text-emerald-800'
                      }`}
                    >
                      {res.status}
                    </span>
                  </td>
                  <td className="p-3.5">
                    {res.status === 'BOOKED' && (
                      <button
                        disabled={processingId === res.reservationId}
                        onClick={() => handleAction(res, 'CHECK_IN')}
                        className="bg-[#0FB5BA] hover:bg-[#023859] text-white px-3 py-1.5 rounded-lg text-xs font-semibold transition disabled:opacity-60"
                      >
                        Check-In
                      </button>
                    )}
                    {res.status === 'CHECKED_IN' && (
                      <button
                        disabled={processingId === res.reservationId}
                        onClick={() => handleAction(res, 'CHECK_OUT')}
                        className="bg-[#011C40] hover:bg-emerald-700 text-white px-3 py-1.5 rounded-lg text-xs font-semibold transition disabled:opacity-60"
                      >
                        Check-Out & Bayar
                      </button>
                    )}
                    {res.status === 'CHECKED_OUT' && (
                      <span className="text-xs font-medium text-slate-400">Lunas / Selesai</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}