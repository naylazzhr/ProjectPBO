import React, { useState } from 'react';
import { Hotel } from 'lucide-react';

export default function LoginPage({ users, onLoginSuccess }) {
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [errorMsg, setErrorMsg] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    const user = users.find(
      (u) => u.username === loginForm.username && u.password === loginForm.password
    );
    if (user) {
      setErrorMsg('');
      onLoginSuccess(user);
    } else {
      setErrorMsg('Username atau password salah! (Coba: admin/123 atau resepsionis/123)');
    }
  };

  return (
    <div className="min-h-screen bg-[#011C40] flex items-center justify-center p-4 font-sans">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-8 border-t-4 border-[#0FB5BA]">
        <div className="flex flex-col items-center mb-6">
          <div className="bg-[#023859] p-3 rounded-xl text-[#A7EBF2] mb-3">
            <Hotel size={36} />
          </div>
          <h1 className="text-2xl font-bold text-[#011C40]">Hotel GrandLuxury</h1>
          <p className="text-sm text-slate-500">Sistem Manajemen Reservasi & OOP Backend</p>
        </div>

        {errorMsg && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm p-3 rounded-lg mb-4">
            {errorMsg}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-1">Username</label>
            <input
              type="text"
              required
              placeholder="admin atau resepsionis"
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:border-[#0FB5BA]"
              value={loginForm.username}
              onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })}
            />
          </div>
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-1">Password</label>
            <input
              type="password"
              required
              placeholder="123"
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:border-[#0FB5BA]"
              value={loginForm.password}
              onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
            />
          </div>
          <button
            type="submit"
            className="w-full bg-[#023859] hover:bg-[#011C40] text-white font-semibold py-2.5 rounded-lg transition"
          >
            Masuk ke Sistem
          </button>
        </form>
      </div>
    </div>
  );
}