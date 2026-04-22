import { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useLocation, useNavigate } from 'react-router-dom';
import { documentsApi } from '../api/documents';
import { searchApi } from '../api/search';
import { SearchResult } from '../types';
import FileUpload from '../components/ui/FileUpload';
import PrecedentCard from '../components/ui/PrecedentCard';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import {
  Scale,
  CheckCircle,
  Info,
  AlertTriangle,
  ArrowRight,
} from 'lucide-react';

type UploadStep = 'upload' | 'analyzing' | 'results';

export default function DashboardPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [step, setStep] = useState<UploadStep>('upload');
  const [results, setResults] = useState<SearchResult | null>(null);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [documentExpanded, setDocumentExpanded] = useState(false);
  const queryClient = useQueryClient();
  const DOCUMENT_PREVIEW_LIMIT = 380;

  useEffect(() => {
    const state = location.state as { reanalyzeResult?: SearchResult } | null;
    if (state?.reanalyzeResult) {
      setResults(state.reanalyzeResult);
      setStep('results');
      setUploadError(null);
      setDocumentExpanded(false);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

  const uploadAndAnalyze = useMutation({
    mutationFn: async (file: File) => {
      setUploadError(null);
      setStep('analyzing');
      const doc = await documentsApi.upload(file);
      const searchResult = await searchApi.analyzeDocument(doc.id);
      return searchResult;
    },
    onSuccess: (data) => {
      setResults(data);
      setStep('results');
      setDocumentExpanded(false);
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      queryClient.invalidateQueries({ queryKey: ['search-history'] });
    },
    onError: (err: any) => {
      setUploadError(err.response?.data?.message || 'Ошибка при загрузке документа');
      setStep('upload');
    },
  });

  const handleReset = () => {
    setStep('upload');
    setResults(null);
    setUploadError(null);
    setDocumentExpanded(false);
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Анализ документа</h1>
        <p className="text-gray-500">
          Загрузите юридический документ (.txt или .docx) для поиска похожих прецедентов
        </p>
      </div>

      <div className="flex items-center gap-2 mb-8">
        {[
          { key: 'upload', label: '1. Загрузка' },
          { key: 'analyzing', label: '2. Анализ' },
          { key: 'results', label: '3. Результаты' },
        ].map((s, i) => {
          const isActive = s.key === step;
          const isDone =
            (step === 'analyzing' && i === 0) ||
            (step === 'results' && i <= 1);
          return (
            <div key={s.key} className="flex items-center gap-2">
              <div
                className={`flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-medium ${
                  isDone
                    ? 'bg-green-100 text-green-700'
                    : isActive
                    ? 'bg-blue-100 text-blue-700'
                    : 'bg-gray-100 text-gray-500'
                }`}
              >
                {isDone && <CheckCircle className="w-3.5 h-3.5" />}
                {s.label}
              </div>
              {i < 2 && <ArrowRight className="w-3 h-3 text-gray-300" />}
            </div>
          );
        })}
      </div>

      {step === 'upload' && (
        <div className="card">
          <FileUpload
            onUpload={(file) => uploadAndAnalyze.mutate(file)}
            isLoading={uploadAndAnalyze.isPending}
            error={uploadError}
          />

          <div className="mt-6 p-4 bg-blue-50 rounded-lg">
            <div className="flex items-start gap-3">
              <Info className="w-4 h-4 text-blue-600 mt-0.5 flex-shrink-0" />
              <div className="text-sm text-blue-800">
                <p className="font-medium mb-1">Как это работает:</p>
                <ol className="list-decimal list-inside space-y-1 text-blue-700">
                  <li>Загрузите документ (.txt или .docx)</li>
                  <li>Система проанализирует текст с помощью алгоритма TF-IDF</li>
                  <li>Найдет похожие юридические прецеденты из базы данных</li>
                  <li>Отсортирует результаты по степени сходства</li>
                </ol>
              </div>
            </div>
          </div>
        </div>
      )}

      {step === 'analyzing' && (
        <div className="card text-center py-16">
          <LoadingSpinner size="lg" className="mb-6" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Анализируем документ</h3>
          <p className="text-gray-500">
            Идет поиск похожих прецедентов в базе данных...
          </p>
          <div className="mt-6 flex flex-col items-center gap-2 text-sm text-gray-400">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
              <div className="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
              <div className="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
            </div>
          </div>
        </div>
      )}

      {step === 'results' && results && (
        <div>
          <div className="card mb-6">
            <div className="flex items-start justify-between">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-1">
                  Результаты анализа
                </h2>
                <p className="text-sm text-gray-500">
                  Документ: <span className="font-medium text-gray-700">{results.documentTitle}</span>
                </p>
                {results.documentContent && (
                  <div className="mt-3">
                    <p className="text-sm text-gray-600 whitespace-pre-wrap">
                      {documentExpanded || results.documentContent.length <= DOCUMENT_PREVIEW_LIMIT
                        ? results.documentContent
                        : `${results.documentContent.slice(0, DOCUMENT_PREVIEW_LIMIT)}...`}
                    </p>
                    {results.documentContent.length > DOCUMENT_PREVIEW_LIMIT && (
                      <button
                        type="button"
                        onClick={() => setDocumentExpanded((prev) => !prev)}
                        className="mt-2 text-xs font-medium text-blue-600 hover:text-blue-700"
                      >
                        {documentExpanded ? 'Свернуть' : 'Показать больше'}
                      </button>
                    )}
                  </div>
                )}
              </div>
              <div className="text-right">
                <div className="text-2xl font-bold text-blue-600">{results.totalMatches}</div>
                <div className="text-xs text-gray-500">прецедентов найдено</div>
              </div>
            </div>

            {results.totalMatches === 0 && (
              <div className="mt-4 flex items-start gap-3 p-4 bg-yellow-50 rounded-lg">
                <AlertTriangle className="w-4 h-4 text-yellow-600 mt-0.5" />
                <div className="text-sm text-yellow-800">
                  <p className="font-medium">Похожие прецеденты не найдены</p>
                  <p className="mt-0.5 text-yellow-700">
                    Документ уникален или не содержит юридической тематики из нашей базы данных.
                  </p>
                </div>
              </div>
            )}
          </div>

          {results.matches.length > 0 && (
            <div className="space-y-4 mb-6">
              {results.matches.map((match) => (
                <PrecedentCard
                  key={match.precedentId}
                  match={match}
                />
              ))}
            </div>
          )}

          <div className="flex gap-3">
            <button onClick={handleReset} className="btn-primary">
              <Scale className="w-4 h-4 mr-2" />
              Анализировать новый документ
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
