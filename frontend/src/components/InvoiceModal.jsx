import React from 'react';
import { formatRupiah } from '../utils/hotelHelper';

export default function InvoiceModal({ invoiceData, onClose }) {
  if (!invoiceData) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl max-w-sm w-full p-6 shadow-xl font-mono text-sm">
        <h3 className="text-center font-bold text-base border-b pb-2 mb-3">
          === INVOICE HOTEL GRANDLUXURY ===
        </h3>
        <div className="space-y-1.5 text-slate-700">
          <p>No. Pembayaran : <strong>{invoiceData.payment.paymentId}</strong></p>
          <p>ID Reservasi   : {invoiceData.reservation.reservationId}</p>
          <p>Kamar          : {invoiceData.reservation.roomNumber}</p>
          <p>Check-in       : {invoiceData.reservation.checkIn}</p>
          <p>Check-out      : {invoiceData.reservation.checkOut}</p>
          <p>Metode Bayar   : {invoiceData.payment.method}</p>
          <div className="border-t border-dashed my-2 pt-2 text-base font-bold text-emerald-700">
            TOTAL BAYAR    : {formatRupiah(invoiceData.payment.amount)}
          </div>
        </div>
        <button
          onClick={onClose}
          className="mt-4 w-full bg-[#023859] text-white py-2 rounded-lg font-sans font-semibold hover:bg-[#011C40] transition"
        >
          Tutup Invoice
        </button>
      </div>
    </div>
  );
}