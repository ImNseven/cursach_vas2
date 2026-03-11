import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { searchApi } from '../api/search';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { History, FileText, ChevronLeft, ChevronRight, Search } from 'lucide-react';

export default function HistoryPage() {
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['search-history', page],
    queryFn: () => searchApi.getHistory(page, 15),
  });

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });

  const history = data?.content || [];
  const totalPages = data?.totalPages || 0;

  if (isLoading) return <LoadingSpinner size="lg" className="py-20" />;

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">История поисков</h1>
        <p className="text-gray-500">
          Все ваши предыдущие запросы. Всего: {data?.totalElements || 0}
        </p>
      </div>

      {history.length === 0 ? (
        <div className="card text-center py-16">
          <History className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-700 mb-2">История пуста</h3>
          <p className="text-gray-400 text-sm">
            Загрузите документ для поиска прецедентов, и он появится здесь
          </p>
        </div>
      ) : (
        <div className="card divide-y divide-gray-100">
          {history.map((item) => (
            <div key={item.id} className="py-4 first:pt-0 last:pb-0">
              <div className="flex items-center justify-between gap-4">
                <div className="flex items-center gap-3 min-w-0">
                  <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0">
                    <FileText className="w-4 h-4 text-blue-500" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">
                      {item.documentTitle}
                    </p>
                    <p className="text-xs text-gray-500">{formatDate(item.searchedAt)}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2 flex-shrink-0">
                  <div className="flex items-center gap-1.5 text-xs text-gray-600 bg-gray-50 px-2.5 py-1 rounded-full">
                    <Search className="w-3 h-3" />
                    {item.resultsCount} прецедентов
                  </div>
                </div>
              </div>
            </div>
          ))}
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
