import { useEffect } from 'react';
import { BrowserRouter, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { AppProviders } from '@/app/providers';
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage';
import { HomePage } from '@/features/home/pages/HomePage';
import { getToken } from '@/lib/authStorage';

function AuthLostListener() {
  const navigate = useNavigate();
  useEffect(() => {
    const fn = () => navigate('/', { replace: true });
    window.addEventListener('shortlink:auth-lost', fn);
    return () => window.removeEventListener('shortlink:auth-lost', fn);
  }, [navigate]);
  return null;
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}

export default function App() {
  return (
    <AppProviders>
      <BrowserRouter>
        <AuthLostListener />
        <Routes>
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<HomePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AppProviders>
  );
}
