import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { favoritesApi } from '../api/favorites';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { Heart, Scale, Building2, Calendar, Trash2, ChevronLeft, ChevronRight } from 'lucide-react';

export default function FavoritesPage() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['favorites', page],
    queryFn: () => favoritesApi.getAll(page, 10),
  });

  const removeFav = useMutation({
    mutationFn: (precedentId: number) => favoritesApi.remove(precedentId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['favorites'] }),
  });

  const favorites = data?.content || [];
  const totalPages = data?.totalPages || 0;

  if (isLoading) return <LoadingSpinner size="lg" className="py-20" />;

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Избранные прецеденты</h1>
        <p className="text-gray-500">
          Сохраненные вами прецеденты. Всего: {data?.totalElements || 0}
        </p>
      </div>

      {favorites.length === 0 ? (
        <div className="card text-center py-16">
          <Heart className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-700 mb-2">Нет избранных прецедентов</h3>
          <p className="text-gray-400 text-sm">
            При анализе документов нажмите на иконку сердца, чтобы сохранить прецедент
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {favorites.map((fav) => {
            const p = fav.precedent;
            return (
              <div key={fav.id} className="card hover:shadow-md transition-shadow">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 bg-red-50 rounded-lg flex items-center justify-center flex-shrink-0">
                      <Scale className="w-5 h-5 text-red-500" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2 mb-1.5 flex-wrap">
                        {p.caseNumber && (
                          <span className="badge badge-gray">#{p.caseNumber}</span>
                        )}
                        {p.category && (
                          <span className="badge badge-blue">{p.category.name}</span>
                        )}
                      </div>
                      <h3 className="text-sm font-semibold text-gray-900 mb-1">{p.title}</h3>
                      <div className="flex items-center gap-3 text-xs text-gray-500 mb-2">
                        {p.courtName && (
                          <span className="flex items-center gap-1">
                            <Building2 className="w-3 h-3" />
                            {p.courtName}
                          </span>
                        )}
                        {p.decisionDate && (
                          <span className="flex items-center gap-1">
                            <Calendar className="w-3 h-3" />
                            {new Date(p.decisionDate).toLocaleDateString('ru-RU')}
                          </span>
                        )}
                      </div>
                      {p.summary && (
                        <p className="text-sm text-gray-600 line-clamp-2">{p.summary}</p>
                      )}
                      {p.tags.length > 0 && (
                        <div className="flex flex-wrap gap-1 mt-2">
                          {p.tags.map((tag) => (
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

                  <button
                    onClick={() => removeFav.mutate(p.id)}
                    disabled={removeFav.isPending}
                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors flex-shrink-0"
                    title="Убрать из избранного"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {totalPages > 1 && (
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
