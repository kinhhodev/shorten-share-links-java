import { apiClient } from './client';
import type { CreateTopicRequest, ShareTopicRequest, Topic, TopicShare } from './types';

export async function listTopics(): Promise<Topic[]> {
  const { data } = await apiClient.get<Topic[]>('/api/v1/topics');
  return data;
}

export async function createTopic(body: CreateTopicRequest): Promise<Topic> {
  const { data } = await apiClient.post<Topic>('/api/v1/topics', body);
  return data;
}

export async function deleteTopic(topicPublicId: string): Promise<void> {
  await apiClient.delete(`/api/v1/topics/${topicPublicId}`);
}

export async function listShares(topicPublicId: string): Promise<TopicShare[]> {
  const { data } = await apiClient.get<TopicShare[]>(`/api/v1/topics/${topicPublicId}/shares`);
  return data;
}

export async function shareTopic(topicPublicId: string, body: ShareTopicRequest): Promise<TopicShare> {
  const { data } = await apiClient.post<TopicShare>(`/api/v1/topics/${topicPublicId}/shares`, body);
  return data;
}
