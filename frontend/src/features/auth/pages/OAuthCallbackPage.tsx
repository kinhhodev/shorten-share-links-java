import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { setAuthUser, setToken } from '@/lib/authStorage';
import { authApi } from '@/services/api';

export function OAuthCallbackPage() {
  const navigate = useNavigate();
  const [message, setMessage] = useState('Signing in…');

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if (!token) {
      setMessage('Missing token. Redirecting…');
      window.setTimeout(() => navigate('/', { replace: true }), 800);
      return;
    }

    setToken(token);
    void authApi
      .getCurrentUser()
      .then((user) => {
        setAuthUser(user);
        navigate('/dashboard', { replace: true });
      })
      .catch(() => {
        setMessage('Could not load user. Redirecting…');
        window.setTimeout(() => navigate('/', { replace: true }), 1200);
      });
  }, [navigate]);

  return (
    <div className="mx-auto max-w-md px-4 py-16 text-center font-semibold text-neutral-800">
      <p>{message}</p>
    </div>
  );
}
