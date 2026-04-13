import { apiClient } from './client';
import type { CreateLinkRequest, Link } from './types';

export async function listByTopic(topicPublicId: string): Promise<Link[]> {
  const { data } = await apiClient.get<Link[]>(`/api/v1/links/by-topic/${topicPublicId}`);
  return data;
}

export async function createLink(body: CreateLinkRequest): Promise<Link> {
  const { data } = await apiClient.post<Link>('/api/v1/links', body);
  return data;
}

export async function deleteLink(linkPublicId: string): Promise<void> {
  await apiClient.delete(`/api/v1/links/${linkPublicId}`);
}
