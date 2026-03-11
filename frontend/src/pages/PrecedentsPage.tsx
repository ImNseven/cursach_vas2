import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { precedentsApi } from '../api/precedents';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { Scale, Search, Building2, Calendar, ChevronLeft, ChevronRight, BookOpen } from 'lucide-react';

export default function PrecedentsPage() {
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchInput, setSearchInput] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['precedents', page],
    queryFn: () => precedentsApi.getAll(page, 12),
  });

  const { data: searchResults, isLoading: isSearching } = useQuery({
    queryKey: ['precedents-search', searchQuery],
    queryFn: () => precedentsApi.search(searchQuery),
    enabled: searchQuery.length > 2,
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearchQuery(searchInput);
  };

  const displayedPrecedents = searchQuery.length > 2
    ? (searchResults || [])
    : (data?.content || []);

  const totalPages = data?.totalPages || 0;
  const totalElements = data?.totalElements || 0;

  return (
    <div className="max-w-5xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">База прецедентов</h1>
        <p className="text-gray-500">
          Судебные решения и прецеденты. Всего в базе: {totalElements}
        </p>
      </div>

      {/* Search */}
      <div className="card mb-6">
        <form onSubmit={handleSearch} className="flex gap-3">
          <input
            type="text"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="input flex-1"
            placeholder="Поиск по тексту прецедентов..."
          />
          <button type="submit" className="btn-primary">
            <Search className="w-4 h-4 mr-2" />
            Найти
          </button>
          {searchQuery && (
            <button
              type="button"
              onClick={() => { setSearchQuery(''); setSearchInput(''); }}
              className="btn-secondary"
            >
              Сбросить
            </button>
          )}
        </form>
      </div>

      {(isLoading || isSearching) ? (
        <LoadingSpinner size="lg" className="py-20" />
      ) : displayedPrecedents.length === 0 ? (
        <div className="card text-center py-16">
          <BookOpen className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-700">Ничего не найдено</h3>
        </div>
      ) : (
        <div className="grid gap-4">
          {displayedPrecedents.map((precedent) => (
            <div key={precedent.id} className="card hover:shadow-md transition-shadow">
              <div className="flex items-start gap-4">
                <div className="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Scale className="w-5 h-5 text-blue-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-2 flex-wrap">
                    {precedent.caseNumber && (
                      <span className="badge badge-gray">#{precedent.caseNumber}</span>
                    )}
                    {precedent.category && (
                      <span className="badge badge-blue">{precedent.category.name}</span>
                    )}
                    {precedent.decision && (
                      <span className="badge badge-green">{precedent.decision}</span>
                    )}
                  </div>

                  <h3 className="text-base font-semibold text-gray-900 mb-2">
                    {precedent.title}
                  </h3>

                  <div className="flex items-center gap-4 text-xs text-gray-500 mb-3">
                    {precedent.courtName && (
                      <span className="flex items-center gap-1">
                        <Building2 className="w-3 h-3" />
                        {precedent.courtName}
                      </span>
                    )}
                    {precedent.decisionDate && (
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {new Date(precedent.decisionDate).toLocaleDateString('ru-RU')}
                      </span>
                    )}
                  </div>

                  {precedent.summary && (
                    <p className="text-sm text-gray-600 line-clamp-2">{precedent.summary}</p>
                  )}

                  {precedent.tags.length > 0 && (
                    <div className="flex flex-wrap gap-1 mt-3">
                      {precedent.tags.map((tag) => (
                        <span
                          key={tag.id}
                          className="px-2 py-0.5 rounded-full text-xs font-medium text-white"
                          style={{ backgroundColor: tag.color || '#6b7280' }}
                        >
                          {tag.name}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {!searchQuery && totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-6">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="btn-secondary p-2"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <span className="text-sm text-gray-600">{page + 1} / {totalPages}</span>
          <button
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="btn-secondary p-2"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  );
}
