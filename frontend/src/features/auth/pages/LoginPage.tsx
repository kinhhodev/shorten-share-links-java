import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import { Card, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { getErrorMessage, authApi } from '@/services/api';
import { getToken, setToken } from '@/lib/authStorage';

type Mode = 'login' | 'register';

export function LoginPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  useEffect(() => {
    if (getToken()) {
      navigate('/dashboard', { replace: true });
    }
  }, [navigate]);
  const [mode, setMode] = useState<Mode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const loginMutation = useMutation({
    mutationFn: () => authApi.login({ email, password }),
    onSuccess: (data) => {
      setToken(data.accessToken);
      queryClient.clear();
      navigate('/dashboard', { replace: true });
    },
    onError: (e) => setFormError(getErrorMessage(e)),
  });

  const registerMutation = useMutation({
    mutationFn: () =>
      authApi.register({
        email,
        password,
        displayName: displayName.trim() || undefined,
      }),
    onSuccess: (data) => {
      setToken(data.accessToken);
      queryClient.clear();
      navigate('/dashboard', { replace: true });
    },
    onError: (e) => setFormError(getErrorMessage(e)),
  });

  const busy = loginMutation.isPending || registerMutation.isPending;

  function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFormError(null);
    if (mode === 'login') {
      loginMutation.mutate();
    } else {
      registerMutation.mutate();
    }
  }

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
        <div className="mb-6 flex gap-2 border-b-4 border-black pb-4">
          <button
            type="button"
            className={`flex-1 border-4 border-black py-2 text-sm font-black uppercase ${
              mode === 'login' ? 'bg-[#FFE156] shadow-brutal-sm' : 'bg-white'
            }`}
            onClick={() => {
              setMode('login');
              setFormError(null);
            }}
          >
            Log in
          </button>
          <button
            type="button"
            className={`flex-1 border-4 border-black py-2 text-sm font-black uppercase ${
              mode === 'register' ? 'bg-[#7dd3fc] shadow-brutal-sm' : 'bg-white'
            }`}
            onClick={() => {
              setMode('register');
              setFormError(null);
            }}
          >
            Register
          </button>
        </div>

        <CardTitle className="mb-4">
          {mode === 'login' ? 'Welcome back' : 'Create account'}
        </CardTitle>

        <form onSubmit={onSubmit} className="space-y-4">
          {mode === 'register' && (
            <div>
              <Label htmlFor="displayName">Display name (optional)</Label>
              <Input
                id="displayName"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                autoComplete="nickname"
              />
            </div>
          )}
          <div>
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
          </div>
          <div>
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              required
              minLength={8}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            />
          </div>

          {formError && (
            <p className="border-2 border-black bg-[#fecaca] px-3 py-2 text-sm font-semibold text-black">
              {formError}
            </p>
          )}

          <Button type="submit" className="w-full" disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'Log in' : 'Register'}
          </Button>
        </form>
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
