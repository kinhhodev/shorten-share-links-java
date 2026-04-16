import clsx from 'clsx';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/Button';
import { Card, CardTitle } from '@/components/ui/Card';
import { Modal } from '@/components/ui/Modal';
import { AuthForm } from '@/features/auth/components/AuthForm';
import type { AuthMode } from '@/features/auth/components/AuthForm';
import { clearToken, getToken } from '@/lib/authStorage';
import { GuestCreateLinkForm } from '../components/GuestCreateLinkForm';

const linkBtn =
  'inline-flex flex-1 items-center justify-center border-4 border-black px-4 py-2 text-center font-bold uppercase tracking-wide shadow-brutal-sm transition-transform hover:translate-x-0.5 hover:translate-y-0.5 hover:shadow-[2px_2px_0_0_#000] active:translate-x-1 active:translate-y-1 active:shadow-none';

export function HomePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isAuthenticated, setIsAuthenticated] = useState(() => Boolean(getToken()));
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [authMode, setAuthMode] = useState<AuthMode>('login');

  function logout() {
    clearToken();
    void queryClient.clear();
    setIsAuthenticated(false);
    navigate('/', { replace: true });
  }

  return (
    <div className="mx-auto min-h-screen max-w-4xl px-4 py-10">
      <header className="mb-10 text-center">
        <h1 className="font-display text-4xl uppercase tracking-tight text-black md:text-5xl">Shortlink</h1>
        <p className="mt-2 text-sm font-semibold text-neutral-800">
          Neobrutalist URL shortener — fast redirects at <span className="font-mono">/r/topic/slug</span>.
        </p>
      </header>

      {isAuthenticated ? (
        <div className="grid gap-8">
          <Card>
            <div className="mb-4 flex items-center justify-between gap-3">
              <CardTitle className="mb-0">New short link</CardTitle>
              <div className="flex items-center gap-2">
                <Button type="button" variant="primary" onClick={() => navigate('/dashboard')}>
                  Dashboard
                </Button>
                <Button type="button" variant="ghost" onClick={logout}>
                  Logout
                </Button>
              </div>
            </div>
            <GuestCreateLinkForm authenticated />
          </Card>
        </div>
      ) : (
        <div className="mx-auto w-full max-w-4xl space-y-10">
          <Card className="w-full">
            <CardTitle className="mb-4">New short link</CardTitle>
            <GuestCreateLinkForm />
          </Card>
          <div className="flex w-full flex-col gap-4">
            <button
              type="button"
              className={clsx(linkBtn, 'bg-[#FFE156] text-black hover:bg-[#ffed90]')}
              onClick={() => {
                setAuthMode('login');
                setShowLoginModal(true);
              }}
            >
              Login
            </button>
            <div className="flex w-full flex-col gap-4 sm:flex-row sm:items-stretch">
              <a
                href="/oauth2/authorization/google"
                className={clsx(linkBtn, 'bg-white text-black hover:bg-neutral-100')}
              >
                <span className="mr-2 inline-flex items-center" aria-hidden="true">
                  <svg viewBox="0 0 24 24" className="h-5 w-5">
                    <path
                      d="M21.35 12.27c0-.79-.07-1.54-.2-2.27H12v4.29h5.24a4.48 4.48 0 0 1-1.95 2.94v2.44h3.15c1.84-1.69 2.91-4.18 2.91-7.4Z"
                      fill="#4285F4"
                    />
                    <path
                      d="M12 21.75c2.63 0 4.84-.87 6.45-2.36l-3.15-2.44c-.87.58-1.98.93-3.3.93-2.54 0-4.69-1.72-5.46-4.03H3.28v2.52A9.74 9.74 0 0 0 12 21.75Z"
                      fill="#34A853"
                    />
                    <path
                      d="M6.54 13.85a5.84 5.84 0 0 1 0-3.7V7.63H3.28a9.74 9.74 0 0 0 0 8.74l3.26-2.52Z"
                      fill="#FBBC05"
                    />
                    <path
                      d="M12 6.12c1.43 0 2.71.49 3.72 1.45l2.79-2.8A9.29 9.29 0 0 0 12 2.25a9.74 9.74 0 0 0-8.72 5.38l3.26 2.52C7.31 7.84 9.46 6.12 12 6.12Z"
                      fill="#EA4335"
                    />
                  </svg>
                </span>
              </a>
              <a
                href="/oauth2/authorization/github"
                className={clsx(linkBtn, 'bg-white text-black hover:bg-neutral-100')}
              >
                <span className="mr-2 inline-flex items-center" aria-hidden="true">
                  <svg viewBox="0 0 24 24" className="h-5 w-5 fill-current">
                    <path d="M12 .5C5.65.5.5 5.76.5 12.25c0 5.2 3.3 9.62 7.88 11.18.58.11.79-.26.79-.57 0-.28-.01-1.03-.02-2.03-3.2.72-3.88-1.58-3.88-1.58-.52-1.36-1.28-1.72-1.28-1.72-1.04-.73.08-.71.08-.71 1.15.08 1.76 1.21 1.76 1.21 1.02 1.79 2.67 1.27 3.32.97.1-.76.4-1.27.73-1.57-2.56-.3-5.26-1.31-5.26-5.84 0-1.29.46-2.34 1.2-3.16-.12-.31-.52-1.54.11-3.21 0 0 .98-.32 3.2 1.21a10.93 10.93 0 0 1 5.84 0c2.22-1.53 3.2-1.21 3.2-1.21.63 1.67.23 2.9.11 3.21.75.82 1.2 1.87 1.2 3.16 0 4.54-2.7 5.53-5.28 5.83.41.36.78 1.06.78 2.14 0 1.54-.01 2.78-.01 3.16 0 .31.2.69.8.57A11.8 11.8 0 0 0 23.5 12.25C23.5 5.76 18.35.5 12 .5Z" />
                  </svg>
                </span>
              </a>
            </div>
          </div>
        </div>
      )}

      {showLoginModal && (
        <Modal
          open
          onClose={() => setShowLoginModal(false)}
          className="max-w-md"
          title={authMode === 'login' ? 'Welcome back' : 'Create account'}
        >
          <AuthForm
            idPrefix="home-login-modal"
            initialMode="login"
            onModeChange={setAuthMode}
            showCardTitle={false}
            onAuthenticated={() => {
              setShowLoginModal(false);
              setIsAuthenticated(true);
            }}
          />
        </Modal>
      )}
    </div>
  );
}
