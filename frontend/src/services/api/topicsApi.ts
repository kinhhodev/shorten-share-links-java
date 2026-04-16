import { apiClient } from './client';

export async function deleteTopic(topicName: string): Promise<void> {
  await apiClient.delete(`/api/v1/topics/${encodeURIComponent(topicName)}`);
}
