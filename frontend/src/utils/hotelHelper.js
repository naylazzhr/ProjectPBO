export const hitungHargaPerMalam = (tipe, hargaDasar) => {
  if (tipe === 'DELUXE') return hargaDasar * 1.30;
  if (tipe === 'SUITE') return hargaDasar * 1.75;
  return hargaDasar;
};

export const formatRupiah = (angka) => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
  }).format(angka);
};