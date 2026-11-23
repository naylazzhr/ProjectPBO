import React from 'react';
import { formatRupiah } from '../utils/hotelHelper';

export default function ReportsPage({ payments }) {
  return (
    <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
      <h2 className="text-lg font-bold text-[#011C40] mb-2">Ringkasan Pendapatan Hotel</h2>
      <p className="text-2xl font-bold text-emerald-600 mb-6">
        Total Pendapatan: {formatRupiah(payments.reduce((acc, p) => acc + p.amount, 0))}
      </p>
      <table className="w-full text-left border-collapse text-sm">
        <thead className="bg-slate-50 border-b">
          <tr>
            <th className="p-3">ID Pembayaran</th>
            <th className="p-3">ID Reservasi</th>
            <th className="p-3">Tanggal</th>
            <th className="p-3">Metode</th>
            <th className="p-3">Nominal</th>
          </tr>
        </thead>
        <tbody className="divide-y">
          {payments.length === 0 ? (
            <tr>
              <td colSpan="5" className="p-4 text-center text-slate-400">Belum ada transaksi check-out.</td>
            </tr>
          ) : (
            payments.map((p) => (
              <tr key={p.paymentId}>
                <td className="p-3 font-semibold">{p.paymentId}</td>
                <td className="p-3">{p.reservationId}</td>
                <td className="p-3">{p.date}</td>
                <td className="p-3">{p.method}</td>
                <td className="p-3 font-bold text-emerald-600">{formatRupiah(p.amount)}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}