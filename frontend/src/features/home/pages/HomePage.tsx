import clsx from 'clsx';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Modal } from '@/components/ui/Modal';
import { Card, CardTitle } from '@/components/ui/Card';
import { AuthForm } from '@/features/auth/components/AuthForm';
import type { AuthMode } from '@/features/auth/components/AuthForm';
import { CreateLinkForm } from '@/features/dashboard/components/CreateLinkForm';
import { getToken } from '@/lib/authStorage';
import { GuestCreateLinkForm } from '../components/GuestCreateLinkForm';

const linkBtn =
  'inline-flex flex-1 items-center justify-center border-4 border-black px-4 py-2 text-center font-bold uppercase tracking-wide shadow-brutal-sm transition-transform hover:translate-x-0.5 hover:translate-y-0.5 hover:shadow-[2px_2px_0_0_#000] active:translate-x-1 active:translate-y-1 active:shadow-none';

export function HomePage() {
  const navigate = useNavigate();
  const token = getToken();
  const [authModal, setAuthModal] = useState<AuthMode | null>(null);

  function onAuthSuccess() {
    setAuthModal(null);
    navigate('/dashboard', { replace: true });
  }

  return (
    <div className="mx-auto min-h-screen max-w-4xl px-4 py-10">
      <header className="mb-10 text-center">
        <h1 className="font-display text-4xl uppercase tracking-tight text-black md:text-5xl">Shortlink</h1>
        <p className="mt-2 text-sm font-semibold text-neutral-800">
          Neobrutalist URL shortener — fast redirects at <span className="font-mono">/r/topic/slug</span>.
        </p>
      </header>

      {token ? (
        <div className="grid gap-8">
          <Card>
            <CardTitle className="mb-4">New short link</CardTitle>
            <CreateLinkForm />
          </Card>
          <p className="text-center text-sm font-bold text-neutral-700">
            <Link to="/dashboard" className="underline">
              Open full dashboard
            </Link>{' '}
            to list and delete links, or log out.
          </p>
        </div>
      ) : (
        <div className="mx-auto w-full max-w-4xl space-y-10">
          <Card className="w-full">
            <CardTitle className="mb-4">New short link</CardTitle>
            <GuestCreateLinkForm />
          </Card>
          <div className="flex w-full flex-col gap-4 sm:flex-row sm:items-stretch">
            <button
              type="button"
              className={clsx(linkBtn, 'bg-[#FFE156] text-black hover:bg-[#ffed90]')}
              onClick={() => setAuthModal('login')}
            >
              Login
            </button>
            <button
              type="button"
              className={clsx(linkBtn, 'bg-white text-black hover:bg-neutral-100')}
              onClick={() => setAuthModal('register')}
            >
              Register
            </button>
          </div>
        </div>
      )}

      {authModal !== null && (
        <Modal open onClose={() => setAuthModal(null)} className="max-w-md">
          <AuthForm
            key={authModal}
            idPrefix="home-auth"
            initialMode={authModal}
            showCardTitle
            onAuthenticated={onAuthSuccess}
          />
        </Modal>
      )}
      <p className="mt-8 text-center text-xs font-bold uppercase text-neutral-600">
        API: Spring Boot · SPA: React + Vite
      </p>
    </div>
  );
}
