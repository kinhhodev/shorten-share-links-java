import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card } from '@/components/ui/Card';
import { AuthForm } from '@/features/auth/components/AuthForm';
import { getToken } from '@/lib/authStorage';

export function LoginPage() {
  const navigate = useNavigate();

  useEffect(() => {
    if (getToken()) {
      navigate('/dashboard', { replace: true });
    }
  }, [navigate]);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center p-4">
      <div className="mb-8 text-center">
        <h1 className="font-display text-4xl uppercase tracking-tight text-black md:text-5xl">
          Shortlink
        </h1>
        <p className="mt-2 max-w-md text-sm font-semibold text-neutral-800">
          Neobrutalist URL shortener — topics, shares, and fast redirects.
        </p>
      </div>

      <Card className="w-full max-w-md">
        <AuthForm
          idPrefix="login"
          initialMode="login"
          onAuthenticated={() => navigate('/dashboard', { replace: true })}
        />
      </Card>

      <p className="mt-8 text-center text-xs font-bold uppercase text-neutral-600">
        API: Spring Boot ·{' '}
        <Link to="/dashboard" className="underline">
          Dashboard
        </Link>{' '}
        (requires login)
      </p>
    </div>
  );
}
