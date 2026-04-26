import axios, { type AxiosError } from 'axios';
import { clearToken, getToken } from '@/lib/authStorage';
import type { ProblemDetailBody } from './types';

const baseURL = import.meta.env.API_BASE_URL ?? '';

export const apiClient = axios.create({
  baseURL,
  withCredentials: false,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ProblemDetailBody>) => {
    if (error.response?.status === 401) {
      clearToken();
      window.dispatchEvent(new CustomEvent('shortlink:auth-lost'));
    }
    return Promise.reject(error);
  }
);

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data;
    if (data && typeof data === 'object' && 'detail' in data && typeof data.detail === 'string') {
      return data.detail;
    }
    if (data && typeof data === 'object' && 'title' in data && typeof data.title === 'string') {
      return data.title;
    }
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Something went wrong';
}
