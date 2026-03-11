import { api } from './axios';
import { SearchResult, SearchHistory, PageResponse } from '../types';

export const searchApi = {
  analyzeDocument: async (documentId: number): Promise<SearchResult> => {
    const response = await api.post(`/search/analyze/${documentId}`);
    return response.data;
  },

  getDocumentResults: async (documentId: number): Promise<SearchResult> => {
    const response = await api.get(`/search/results/${documentId}`);
    return response.data;
  },

  getHistory: async (page = 0, size = 10): Promise<PageResponse<SearchHistory>> => {
    const response = await api.get('/search/history', { params: { page, size } });
    return response.data;
  },
};
