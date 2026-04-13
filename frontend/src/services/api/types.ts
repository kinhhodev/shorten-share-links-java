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

export interface Topic {
  publicId: string;
  name: string;
  slug: string;
  description: string | null;
  createdAt: string;
}

export interface Link {
  publicId: string;
  shortSlug: string;
  shortUrl: string;
  originalUrl: string;
  topicPublicId: string;
  topicSlug: string;
  createdAt: string;
}

export type TopicPermission = 'VIEW' | 'EDIT';

export interface TopicShare {
  shareId: string;
  userEmail: string;
  permission: TopicPermission;
}

export interface CreateTopicRequest {
  name: string;
  slug: string;
  description?: string;
}

export interface CreateLinkRequest {
  shortSlug: string;
  originalUrl: string;
  topicPublicId: string;
}

export interface ShareTopicRequest {
  userEmail: string;
  permission: TopicPermission;
}

export interface ProblemDetailBody {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  [key: string]: unknown;
}
