/** Matches backend DTOs (Jackson uses camelCase in JSON). */

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  userPublicId: string;
  email: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName?: string;
}

export interface Link {
  publicId: string;
  topic: string;
  slug: string;
  shortUrl: string;
  originalUrl: string;
  createdAt: string;
}

export interface CreateLinkRequest {
  topic?: string;
  slug: string;
  originalUrl: string;
}

/** Public guest create — matches `CreateGuestLinkRequest` (Jackson camelCase). */
export interface CreateGuestLinkRequest {
  /** Omit or blank → backend uses "_" */
  topic?: string;
  slug: string;
  originalUrl: string;
}

export interface GuestLinkCreatedResponse {
  shortUrl: string;
}

export interface ProblemDetailBody {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  [key: string]: unknown;
}
