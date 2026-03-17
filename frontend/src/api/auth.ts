import { api } from './axios';
import { AuthResponse, User } from '../types';

export const authApi = {
  register: async (data: { email: string; password: string; fullName: string }): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  login: async (data: { email: string; password: string }): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },

  getMe: async (): Promise<User> => {
    const response = await api.get('/auth/me');
    return response.data;
  },

  updateProfile: async (data: { fullName: string }): Promise<User> => {
    const response = await api.put('/users/me', data);
    return response.data;
  },

  loginWithGitHub: async (code: string): Promise<AuthResponse> => {
    const response = await api.post('/auth/github', { code });
    return response.data;
  },
};
