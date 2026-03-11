import { api } from './axios';
import { Precedent, PageResponse } from '../types';

export const precedentsApi = {
  getAll: async (page = 0, size = 10, categoryId?: number): Promise<PageResponse<Precedent>> => {
    const response = await api.get('/precedents', {
      params: { page, size, categoryId },
    });
    return response.data;
  },

  getById: async (id: number): Promise<Precedent> => {
    const response = await api.get(`/precedents/${id}`);
    return response.data;
  },

  search: async (query: string): Promise<Precedent[]> => {
    const response = await api.get('/precedents/search', { params: { query } });
    return response.data;
  },
};
