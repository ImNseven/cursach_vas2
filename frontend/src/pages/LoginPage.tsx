import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { Scale, Eye, EyeOff, AlertCircle } from 'lucide-react';

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
