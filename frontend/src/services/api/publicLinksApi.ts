import { apiClient } from './client';
import type { CreateGuestLinkRequest, GuestLinkCreatedResponse } from './types';

export async function createGuestLink(body: CreateGuestLinkRequest): Promise<GuestLinkCreatedResponse> {
  const { data } = await apiClient.post<GuestLinkCreatedResponse>('/api/public/links', body);
  return data;
}
