import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { precedentsApi } from '../api/precedents';
import { searchApi } from '../api/search';
import type { PrecedentMatch, SimilarPrecedentsResult } from '../types';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import SimilarityBadge from '../components/ui/SimilarityBadge';
import { Scale, Search, Building2, Calendar, ChevronLeft, ChevronRight, BookOpen } from 'lucide-react';

const PRECEDENT_PREVIEW_LIMIT = 280;

export default function PrecedentsPage() {
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [expandedTextById, setExpandedTextById] = useState<Record<number, boolean>>({});
  const [expandedSimilarTextById, setExpandedSimilarTextById] = useState<Record<string, boolean>>({});
  const [similarResultsById, setSimilarResultsById] = useState<Record<number, SimilarPrecedentsResult>>({});
  const [expandedSimilarById, setExpandedSimilarById] = useState<Record<number, boolean>>({});

  const { data, isLoading } = useQuery({
    queryKey: ['precedents', page],
    queryFn: () => precedentsApi.getAll(page, 12),
  });

  const { data: searchResults, isLoading: isSearching } = useQuery({
    queryKey: ['precedents-search', searchQuery],
    queryFn: () => precedentsApi.search(searchQuery),
    enabled: searchQuery.length > 2,
  });

  const similarMutation = useMutation({
    mutationFn: (precedentId: number) => searchApi.getSimilarPrecedents(precedentId),
    onSuccess: (result) => {
      setSimilarResultsById((prev) => ({
        ...prev,
        [result.precedentId]: result,
      }));
      setExpandedSimilarById((prev) => ({
        ...prev,
        [result.precedentId]: true,
      }));
    },
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
              {(() => {
                const sourceText = (precedent.summary?.trim() || precedent.content?.trim() || '');
                const isTextExpanded = !!expandedTextById[precedent.id];
                const needsTruncation = sourceText.length > PRECEDENT_PREVIEW_LIMIT;
                const previewText = isTextExpanded || !needsTruncation
                  ? sourceText
                  : `${sourceText.slice(0, PRECEDENT_PREVIEW_LIMIT)}...`;
                const similarData = similarResultsById[precedent.id];
                const similarExpanded = !!expandedSimilarById[precedent.id];

                return (
                  <>
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

                  {sourceText && (
                    <div>
                      <p className="text-sm text-gray-600">{previewText}</p>
                      {needsTruncation && (
                        <button
                          type="button"
                          onClick={() =>
                            setExpandedTextById((prev) => ({
                              ...prev,
                              [precedent.id]: !isTextExpanded,
                            }))
                          }
                          className="mt-2 text-xs font-medium text-blue-600 hover:text-blue-700"
                        >
                          {isTextExpanded ? 'Свернуть' : 'Смотреть больше'}
                        </button>
                      )}
                    </div>
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

                  <div className="mt-4 flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => similarMutation.mutate(precedent.id)}
                      disabled={similarMutation.isPending && similarMutation.variables === precedent.id}
                      className="btn-secondary"
                    >
                      Найти похожие
                    </button>
                    {similarData && (
                      <button
                        type="button"
                        onClick={() =>
                          setExpandedSimilarById((prev) => ({
                            ...prev,
                            [precedent.id]: !similarExpanded,
                          }))
                        }
                        className="text-xs font-medium text-blue-600 hover:text-blue-700"
                      >
                        {similarExpanded ? 'Скрыть похожие' : 'Показать похожие'}
                      </button>
                    )}
                  </div>
                </div>
              </div>

              {similarData && similarExpanded && (
                <div className="mt-4 border-t border-gray-100 pt-4">
                  <p className="text-sm font-medium text-gray-900 mb-3">
                    Похожие прецеденты: {similarData.totalMatches}
                  </p>
                  {similarData.matches.length === 0 ? (
                    <p className="text-sm text-gray-500">Похожие прецеденты не найдены.</p>
                  ) : (
                    <div className="space-y-3">
                      {similarData.matches.map((match: PrecedentMatch) => {
                        const detailsText = (match.summary?.trim() || match.content?.trim() || '');
                        const rowKey = `${precedent.id}-${match.precedentId}`;
                        const isExpanded = !!expandedSimilarTextById[rowKey];
                        const hasLongText = detailsText.length > 180;
                        const previewText = isExpanded || !hasLongText
                          ? detailsText
                          : `${detailsText.slice(0, 180)}...`;

                        return (
                          <div key={match.precedentId} className="rounded-lg border border-gray-100 p-3">
                            <div className="flex items-center gap-2 mb-1">
                              <SimilarityBadge score={match.similarityScore} />
                              {match.caseNumber && (
                                <span className="text-xs text-gray-500">#{match.caseNumber}</span>
                              )}
                            </div>
                            <p className="text-sm font-medium text-gray-900">{match.title}</p>
                            {detailsText && (
                              <div className="mt-1">
                                <p className="text-xs text-gray-600 whitespace-pre-wrap">{previewText}</p>
                                {hasLongText && (
                                  <button
                                    type="button"
                                    onClick={() =>
                                      setExpandedSimilarTextById((prev) => ({
                                        ...prev,
                                        [rowKey]: !isExpanded,
                                      }))
                                    }
                                    className="mt-1 text-xs font-medium text-blue-600 hover:text-blue-700"
                                  >
                                    {isExpanded ? 'Свернуть' : 'Показать больше'}
                                  </button>
                                )}
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}
                  </>
                );
              })()}
            </div>
          ))}
        </div>
      )}

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
