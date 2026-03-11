import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { Scale, Eye, EyeOff, AlertCircle } from 'lucide-react';

const schema = z.object({
  fullName: z.string().min(2, 'Имя должно быть не менее 2 символов'),
  email: z.string().email('Введите корректный email'),
  password: z.string().min(6, 'Пароль должен быть не менее 6 символов'),
});

type FormData = z.infer<typeof schema>;

export default function RegisterPage() {
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
      const response = await authApi.register(data);
      setAuth(response.user, response.accessToken, response.refreshToken);
      navigate('/dashboard');
    } catch (err: any) {
      setApiError(err.response?.data?.message || 'Ошибка регистрации. Попробуйте снова.');
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
          <p className="text-gray-500 mt-1">Создайте аккаунт для начала работы</p>
        </div>

        <div className="card shadow-lg">
          <h2 className="text-xl font-semibold text-gray-900 mb-6">Регистрация</h2>

          {apiError && (
            <div className="flex items-center gap-2 p-3 bg-red-50 text-red-700 rounded-lg mb-4 text-sm">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              {apiError}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Полное имя</label>
              <input
                {...register('fullName')}
                type="text"
                className="input"
                placeholder="Иванов Иван Иванович"
              />
              {errors.fullName && (
                <p className="text-xs text-red-600 mt-1">{errors.fullName.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
              <input
                {...register('email')}
                type="email"
                className="input"
                placeholder="your@email.com"
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
                  placeholder="Минимум 6 символов"
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
                'Зарегистрироваться'
              )}
            </button>
          </form>

          <p className="text-sm text-center text-gray-500 mt-5">
            Уже есть аккаунт?{' '}
            <Link to="/login" className="text-blue-600 hover:text-blue-700 font-medium">
              Войти
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
