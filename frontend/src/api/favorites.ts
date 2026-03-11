import { api } from './axios';
import { Favorite, PageResponse } from '../types';

export const favoritesApi = {
  add: async (precedentId: number): Promise<Favorite> => {
    const response = await api.post(`/favorites/${precedentId}`);
    return response.data;
  },

  getAll: async (page = 0, size = 10): Promise<PageResponse<Favorite>> => {
    const response = await api.get('/favorites', { params: { page, size } });
    return response.data;
  },

  remove: async (precedentId: number): Promise<void> => {
    await api.delete(`/favorites/${precedentId}`);
  },
};
