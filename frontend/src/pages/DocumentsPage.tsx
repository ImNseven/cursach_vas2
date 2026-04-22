import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { documentsApi } from '../api/documents';
import { searchApi } from '../api/search';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import {
  FileText,
  Trash2,
  Search,
  CheckCircle,
  Clock,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

export default function DocumentsPage() {
  const [page, setPage] = useState(0);
  const [expandedById, setExpandedById] = useState<Record<number, boolean>>({});
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const PREVIEW_LIMIT = 260;

  const { data, isLoading } = useQuery({
    queryKey: ['documents', page],
    queryFn: () => documentsApi.getAll(page, 10),
  });

  const deleteMutation = useMutation({
    mutationFn: documentsApi.delete,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['documents'] }),
  });

  const analyzeMutation = useMutation({
    mutationFn: searchApi.analyzeDocument,
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      navigate('/dashboard', {
        state: { reanalyzeResult: result },
      });
    },
    onError: (err: any) => {
      if (err?.response?.status === 404) {
        alert('К сожалению, в базе нет этого документа.');
        return;
      }
      alert(err?.response?.data?.message || 'Не удалось повторно выполнить поиск.');
    },
  });

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });

  const formatSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  if (isLoading) return <LoadingSpinner size="lg" className="py-20" />;

  const docs = data?.content || [];
  const totalPages = data?.totalPages || 0;

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Мои документы</h1>
        <p className="text-gray-500">
          Все загруженные вами документы. Всего: {data?.totalElements || 0}
        </p>
      </div>

      {docs.length === 0 ? (
        <div className="card text-center py-16">
          <FileText className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-700 mb-2">Нет загруженных документов</h3>
          <p className="text-gray-400 text-sm">
            Перейдите на главную страницу, чтобы загрузить документ
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {docs.map((doc) => (
            <div key={doc.id} className="card hover:shadow-md transition-shadow">
              <div className="flex items-center justify-between gap-4">
                <div className="flex items-center gap-3 min-w-0">
                  <div className="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0">
                    <FileText className="w-5 h-5 text-blue-600" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{doc.title}</p>
                    <p className="text-xs text-gray-500">
                      {doc.fileName} • {formatSize(doc.fileSize)} • {formatDate(doc.uploadedAt)}
                    </p>
                    {doc.content && (
                      <div className="mt-2">
                        <p className="text-sm text-gray-600 whitespace-pre-wrap">
                          {expandedById[doc.id] || doc.content.length <= PREVIEW_LIMIT
                            ? doc.content
                            : `${doc.content.slice(0, PREVIEW_LIMIT)}...`}
                        </p>
                        {doc.content.length > PREVIEW_LIMIT && (
                          <button
                            type="button"
                            className="mt-1 text-xs font-medium text-blue-600 hover:text-blue-700"
                            onClick={() =>
                              setExpandedById((prev) => ({
                                ...prev,
                                [doc.id]: !prev[doc.id],
                              }))
                            }
                          >
                            {expandedById[doc.id] ? 'Свернуть' : 'Показать больше'}
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-2 flex-shrink-0">
                  {doc.isAnalyzed ? (
                    <span className="flex items-center gap-1.5 text-xs text-green-600 bg-green-50 px-2.5 py-1 rounded-full">
                      <CheckCircle className="w-3.5 h-3.5" />
                      {doc.matchedPrecedentsCount} прецедентов
                    </span>
                  ) : (
                    <span className="flex items-center gap-1.5 text-xs text-gray-500 bg-gray-50 px-2.5 py-1 rounded-full">
                      <Clock className="w-3.5 h-3.5" />
                      Не проанализирован
                    </span>
                  )}

                  <button
                    onClick={() => analyzeMutation.mutate(doc.id)}
                    disabled={analyzeMutation.isPending}
                    className="btn-secondary p-2"
                    title="Повторный анализ"
                  >
                    <Search className="w-4 h-4" />
                  </button>

                  <button
                    onClick={() => {
                      if (confirm('Удалить этот документ?')) {
                        deleteMutation.mutate(doc.id);
                      }
                    }}
                    disabled={deleteMutation.isPending}
                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Удалить"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
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
          <span className="text-sm text-gray-600">
            {page + 1} / {totalPages}
          </span>
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
