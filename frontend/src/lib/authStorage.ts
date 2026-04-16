const TOKEN_KEY = 'shortlink_access_token';
const USER_KEY = 'shortlink_auth_user';
const AUTH_USER_EVENT = 'shortlink:auth-user-updated';

export type StoredAuthUser = {
  userPublicId: string;
  email: string;
  displayName: string;
};

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function getAuthUser(): StoredAuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<StoredAuthUser>;
    if (
      typeof parsed.userPublicId === 'string' &&
      typeof parsed.email === 'string' &&
      typeof parsed.displayName === 'string'
    ) {
      return parsed as StoredAuthUser;
    }
  } catch {
    localStorage.removeItem(USER_KEY);
  }

  return null;
}

export function setAuthUser(user: StoredAuthUser): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
  window.dispatchEvent(new CustomEvent(AUTH_USER_EVENT));
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  window.dispatchEvent(new CustomEvent(AUTH_USER_EVENT));
}

export function subscribeAuthUser(listener: () => void): () => void {
  window.addEventListener(AUTH_USER_EVENT, listener);
  return () => window.removeEventListener(AUTH_USER_EVENT, listener);
}
