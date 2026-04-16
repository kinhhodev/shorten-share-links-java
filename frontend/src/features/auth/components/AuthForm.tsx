import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { getErrorMessage, authApi } from '@/services/api';
import { setToken } from '@/lib/authStorage';

export type AuthMode = 'login' | 'register';

type AuthFormProps = {
  initialMode?: AuthMode;
  onAuthenticated: () => void;
  onModeChange?: (mode: AuthMode) => void;
  idPrefix: string;
  showCardTitle?: boolean;
};

export function AuthForm({
  initialMode = 'login',
  onAuthenticated,
  onModeChange,
  idPrefix,
  showCardTitle = true,
}: AuthFormProps) {
  const queryClient = useQueryClient();
  const [mode, setMode] = useState<AuthMode>(initialMode);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const loginMutation = useMutation({
    mutationFn: () => authApi.login({ email, password }),
    onSuccess: (data) => {
      setToken(data.accessToken);
      queryClient.clear();
      onAuthenticated();
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
      onAuthenticated();
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

  const pid = (name: string) => `${idPrefix}-${name}`;

  return (
    <div>
      <div className="mb-6 flex gap-2 border-b-4 border-black pb-4">
        <button
          type="button"
          className={`flex-1 border-4 border-black py-2 text-sm font-black uppercase ${
            mode === 'login' ? 'bg-[#FFE156] shadow-brutal-sm' : 'bg-white'
          }`}
          onClick={() => {
            setMode('login');
            onModeChange?.('login');
            setFormError(null);
          }}
        >
          Log in
        </button>
        <button
          type="button"
          className={`flex-1 border-4 border-black py-2 text-sm font-black uppercase ${
            mode === 'register' ? 'bg-[#FFE156] shadow-brutal-sm' : 'bg-white'
          }`}
          onClick={() => {
            setMode('register');
            onModeChange?.('register');
            setFormError(null);
          }}
        >
          Register
        </button>
      </div>

      {showCardTitle && (
        <CardTitle className="mb-4">{mode === 'login' ? 'Login' : 'Create account'}</CardTitle>
      )}

      <form onSubmit={onSubmit} className="space-y-4">
        {mode === 'register' && (
          <div>
            <Label htmlFor={pid('displayName')}>Display name (optional)</Label>
            <Input
              id={pid('displayName')}
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              autoComplete="nickname"
            />
          </div>
        )}
        <div>
          <Label htmlFor={pid('email')}>Email</Label>
          <Input
            id={pid('email')}
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
        </div>
        <div>
          <Label htmlFor={pid('password')}>Password</Label>
          <Input
            id={pid('password')}
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
    </div>
  );
}
