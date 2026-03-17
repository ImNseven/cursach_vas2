export interface User {
  id: number;
  email: string;
  fullName: string;
  avatarUrl?: string;
  role: string;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
}

export interface Tag {
  id: number;
  name: string;
  color?: string;
}

export interface Document {
  id: number;
  title: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  category?: Category;
  tags: Tag[];
  uploadedAt: string;
  isAnalyzed: boolean;
  matchedPrecedentsCount: number;
  content?: string;
}

export interface Precedent {
  id: number;
  caseNumber: string;
  title: string;
  content: string;
  summary?: string;
  decisionDate?: string;
  courtName?: string;
  decision?: string;
  category?: Category;
  tags: Tag[];
  createdAt: string;
}

export interface PrecedentMatch {
  precedentId: number;
  caseNumber: string;
  title: string;
  summary?: string;
  content?: string;
  courtName?: string;
  decisionDate?: string;
  decision?: string;
  similarityScore: number;
  category?: Category;
  tags: Tag[];
}

export interface SearchResult {
  documentId: number;
  documentTitle: string;
  documentContent?: string;
  totalMatches: number;
  matches: PrecedentMatch[];
  analyzedAt: string;
}

export interface SimilarPrecedentsResult {
  precedentId: number;
  precedentTitle: string;
  totalMatches: number;
  matches: PrecedentMatch[];
  analyzedAt: string;
}

export interface SearchHistory {
  id: number;
  documentId?: number;
  documentTitle: string;
  resultsCount: number;
  searchedAt: string;
}

export interface Annotation {
  id: number;
  documentId: number;
  content: string;
  startPosition?: number;
  endPosition?: number;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  fieldErrors?: Record<string, string>;
}
