import { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, FileText, X, AlertCircle } from 'lucide-react';

interface FileUploadProps {
  onUpload: (file: File) => void;
  isLoading?: boolean;
  error?: string | null;
}

export default function FileUpload({ onUpload, isLoading, error }: FileUploadProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewContent, setPreviewContent] = useState<string | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [isPreviewLoading, setIsPreviewLoading] = useState(false);
  const [isPreviewExpanded, setIsPreviewExpanded] = useState(false);
  const PREVIEW_LIMIT = 700;

  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles.length > 0) {
      const file = acceptedFiles[0];
      setSelectedFile(file);
      setPreviewContent(null);
      setPreviewError(null);
      setIsPreviewExpanded(false);

      const isTxtFile = file.type === 'text/plain' || file.name.toLowerCase().endsWith('.txt');
      if (!isTxtFile) {
        return;
      }

      setIsPreviewLoading(true);
      file.text()
        .then((text) => {
          setPreviewContent(text);
        })
        .catch(() => {
          setPreviewError('Не удалось прочитать содержимое файла.');
        })
        .finally(() => {
          setIsPreviewLoading(false);
        });
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/plain': ['.txt'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
    },
    maxFiles: 1,
    maxSize: 10 * 1024 * 1024,
    disabled: isLoading,
  });

  const handleSubmit = () => {
    if (selectedFile) {
      onUpload(selectedFile);
    }
  };

  const clearFile = () => {
    setSelectedFile(null);
    setPreviewContent(null);
    setPreviewError(null);
    setIsPreviewLoading(false);
    setIsPreviewExpanded(false);
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  return (
    <div className="w-full max-w-2xl mx-auto">
      {!selectedFile ? (
        <div
          {...getRootProps()}
          className={`border-2 border-dashed rounded-xl p-12 text-center cursor-pointer transition-colors ${
            isDragActive
              ? 'border-blue-400 bg-blue-50'
              : 'border-gray-300 bg-white hover:border-blue-400 hover:bg-blue-50/30'
          } ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
        >
          <input {...getInputProps()} />
          <Upload className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <p className="text-lg font-medium text-gray-700 mb-2">
            {isDragActive ? 'Отпустите файл здесь' : 'Загрузите юридический документ'}
          </p>
          <p className="text-sm text-gray-500 mb-4">
            Перетащите файл или нажмите для выбора
          </p>
          <div className="flex items-center justify-center gap-4 text-xs text-gray-400">
            <span className="px-2 py-1 bg-gray-100 rounded">.TXT</span>
            <span className="px-2 py-1 bg-gray-100 rounded">.DOCX</span>
            <span>Максимум 10 MB</span>
          </div>
        </div>
      ) : (
        <div className="border-2 border-green-200 bg-green-50 rounded-xl p-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <FileText className="w-5 h-5 text-green-600" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-900">{selectedFile.name}</p>
                <p className="text-xs text-gray-500">{formatFileSize(selectedFile.size)}</p>
              </div>
            </div>
            <button
              onClick={clearFile}
              className="p-1.5 text-gray-400 hover:text-gray-600 rounded transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <div className="mt-4 rounded-lg border border-green-100 bg-white p-3">
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-500 mb-2">
              Содержимое файла
            </p>
            {isPreviewLoading && (
              <p className="text-sm text-gray-500">Читаем текст файла...</p>
            )}
            {previewError && (
              <p className="text-sm text-red-600">{previewError}</p>
            )}
            {!isPreviewLoading && !previewError && previewContent !== null && (
              <>
                <p className="text-sm text-gray-700 whitespace-pre-wrap">
                  {isPreviewExpanded || previewContent.length <= PREVIEW_LIMIT
                    ? previewContent || 'Файл пустой.'
                    : `${previewContent.slice(0, PREVIEW_LIMIT)}...`}
                </p>
                {previewContent.length > PREVIEW_LIMIT && (
                  <button
                    type="button"
                    onClick={() => setIsPreviewExpanded((prev) => !prev)}
                    className="mt-2 text-xs font-medium text-blue-600 hover:text-blue-700"
                  >
                    {isPreviewExpanded ? 'Свернуть' : 'Показать больше'}
                  </button>
                )}
              </>
            )}
            {!isPreviewLoading && !previewError && previewContent === null && (
              <p className="text-sm text-gray-500">
                Предпросмотр доступен для файлов формата .txt.
              </p>
            )}
          </div>
        </div>
      )}

      {error && (
        <div className="mt-3 flex items-start gap-2 text-sm text-red-600">
          <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <button
        onClick={handleSubmit}
        disabled={!selectedFile || isLoading}
        className="mt-4 w-full btn-primary justify-center py-3 text-base"
      >
        {isLoading ? (
          <>
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2" />
            Анализируем документ...
          </>
        ) : (
          <>
            <Upload className="w-5 h-5 mr-2" />
            Загрузить и найти прецеденты
          </>
        )}
      </button>
    </div>
  );
}
