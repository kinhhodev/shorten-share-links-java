import { apiClient } from './client';
import type { Link, TopicSummary } from './types';

export async function deleteTopic(topicName: string): Promise<void> {
  await apiClient.delete(`/api/v1/topics/${encodeURIComponent(topicName)}`);
}

export async function listTopicsByStatus(status: 'ACTIVE' | 'DELETED'): Promise<TopicSummary[]> {
  const { data } = await apiClient.get<TopicSummary[]>('/api/v1/topics', {
    params: { status },
  });
  return data;
}

export async function restoreTopic(topicName: string): Promise<void> {
  await apiClient.post(`/api/v1/topics/${encodeURIComponent(topicName)}/restore`);
}

export async function listTopicLinksByStatus(
  topicName: string,
  status: 'ACTIVE' | 'DELETED' = 'DELETED'
): Promise<Link[]> {
  const { data } = await apiClient.get<Link[]>(`/api/v1/topics/${encodeURIComponent(topicName)}/links`, {
    params: { status },
  });
  return data;
}
