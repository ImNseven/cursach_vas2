import { api } from './axios';
import { Document, PageResponse } from '../types';

export const documentsApi = {
  upload: async (file: File): Promise<Document> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/documents', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  getAll: async (page = 0, size = 10): Promise<PageResponse<Document>> => {
    const response = await api.get('/documents', { params: { page, size } });
    return response.data;
  },

  getById: async (id: number): Promise<Document> => {
    const response = await api.get(`/documents/${id}`);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/documents/${id}`);
  },
};
