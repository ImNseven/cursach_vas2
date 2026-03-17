import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { AlertCircle, Loader2 } from 'lucide-react';

export default function GitHubCallbackPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setAuth } = useAuthStore();
  const [error, setError] = useState('');

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const savedState = sessionStorage.getItem('github_oauth_state');
    sessionStorage.removeItem('github_oauth_state');

    if (!code) {
      setError('Код авторизации не получен. Попробуйте войти снова.');
      return;
    }

    if (savedState && state !== savedState) {
      setError('Ошибка проверки безопасности. Попробуйте войти снова.');
      return;
    }

    authApi
      .loginWithGitHub(code)
      .then((response) => {
        setAuth(response.user, response.accessToken, response.refreshToken);
        navigate('/dashboard', { replace: true });
      })
      .catch((err: any) => {
        setError(err.response?.data?.message || 'Ошибка входа через GitHub. Попробуйте снова.');
      });
  }, [searchParams, setAuth, navigate]);

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
        <div className="card shadow-lg max-w-md text-center">
          <div className="flex justify-center text-red-500 mb-4">
            <AlertCircle className="w-12 h-12" />
          </div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Ошибка входа</h2>
          <p className="text-gray-600 mb-6">{error}</p>
          <button
            onClick={() => navigate('/login', { replace: true })}
            className="btn-primary w-full justify-center"
          >
            Вернуться к входу
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="card shadow-lg max-w-md text-center py-12">
        <Loader2 className="w-12 h-12 text-blue-600 animate-spin mx-auto mb-4" />
        <p className="text-gray-600">Выполняется вход через GitHub...</p>
      </div>
    </div>
  );
}
