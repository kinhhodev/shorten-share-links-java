import { apiClient } from './client';
import type { AuthResponse, CurrentUserResponse, LoginRequest, RegisterRequest } from './types';

export async function login(body: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/v1/auth/login', body);
  return data;
}

export async function register(body: RegisterRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/v1/auth/register', body);
  return data;
}

export async function getCurrentUser(): Promise<CurrentUserResponse> {
  const { data } = await apiClient.get<CurrentUserResponse>('/api/v1/auth/me');
  return data;
}
