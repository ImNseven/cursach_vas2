import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { Scale, Eye, EyeOff, AlertCircle } from 'lucide-react';

const GITHUB_CLIENT_ID = import.meta.env.VITE_GITHUB_CLIENT_ID || '';
const GITHUB_SCOPE = 'read:user user:email';

const schema = z.object({
  email: z.string().email('Введите корректный email'),
  password: z.string().min(1, 'Введите пароль'),
});

type FormData = z.infer<typeof schema>;

export default function LoginPage() {
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();
  const [showPassword, setShowPassword] = useState(false);
  const [apiError, setApiError] = useState('');

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const handleGitHubLogin = () => {
    if (!GITHUB_CLIENT_ID) {
      setApiError('GitHub OAuth не настроен. Обратитесь к администратору.');
      return;
    }
    const redirectUri = `${window.location.origin}/auth/github/callback`;
    const state = crypto.randomUUID();
    window.sessionStorage.setItem('github_oauth_state', state);
    const params = new URLSearchParams({
      client_id: GITHUB_CLIENT_ID,
      redirect_uri: redirectUri,
      scope: GITHUB_SCOPE,
      state,
    });
    window.location.href = `https://github.com/login/oauth/authorize?${params.toString()}`;
  };

  const onSubmit = async (data: FormData) => {
    setApiError('');
    try {
      const response = await authApi.login(data);
      setAuth(response.user, response.accessToken, response.refreshToken);
      navigate('/dashboard');
    } catch (err: any) {
      setApiError(err.response?.data?.message || 'Неверный email или пароль');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
            <Scale className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">ЮрАнализ</h1>
          <p className="text-gray-500 mt-1">Система анализа юридических документов</p>
        </div>

        <div className="card shadow-lg">
          <h2 className="text-xl font-semibold text-gray-900 mb-6">Вход в систему</h2>

          {apiError && (
            <div className="flex items-center gap-2 p-3 bg-red-50 text-red-700 rounded-lg mb-4 text-sm">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              {apiError}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
              <input
                {...register('email')}
                type="email"
                className="input"
                placeholder="your@email.com"
                autoComplete="email"
              />
              {errors.email && (
                <p className="text-xs text-red-600 mt-1">{errors.email.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Пароль</label>
              <div className="relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  className="input pr-10"
                  placeholder="Введите пароль"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.password && (
                <p className="text-xs text-red-600 mt-1">{errors.password.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full btn-primary py-2.5 justify-center text-base"
            >
              {isSubmitting ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                'Войти'
              )}
            </button>

            <div className="relative my-4">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="bg-white px-2 text-gray-500">или</span>
              </div>
            </div>
            <button
              type="button"
              onClick={handleGitHubLogin}
              className="w-full flex items-center justify-center gap-2 py-2.5 px-4 border border-gray-300 rounded-lg bg-white hover:bg-gray-50 text-gray-700 font-medium transition-colors"
            >
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
              </svg>
              Войти через GitHub
            </button>
          </form>

          <p className="text-sm text-center text-gray-500 mt-5">
            Нет аккаунта?{' '}
            <Link to="/register" className="text-blue-600 hover:text-blue-700 font-medium">
              Зарегистрироваться
            </Link>
          </p>

          <div className="mt-4 p-3 bg-gray-50 rounded-lg text-xs text-gray-500">
            <p className="font-medium mb-1">Тестовый доступ (admin):</p>
            <p>Email: admin@legal-analysis.com</p>
            <p>Пароль: password</p>
          </div>
        </div>
      </div>
    </div>
  );
}
