import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { authApi } from '../../api/auth';
import {
  Scale,
  FileText,
  History,
  Heart,
  LogOut,
  User,
  Search,
  BookOpen,
} from 'lucide-react';

interface LayoutProps {
  children: React.ReactNode;
}

const navItems = [
  { path: '/dashboard', label: 'Анализ', icon: Search },
  { path: '/documents', label: 'Документы', icon: FileText },
  { path: '/precedents', label: 'Прецеденты', icon: BookOpen },
  { path: '/history', label: 'История', icon: History },
  { path: '/favorites', label: 'Избранное', icon: Heart },
];

export default function Layout({ children }: LayoutProps) {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } finally {
      logout();
      navigate('/login');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <div className="w-64 bg-white border-r border-gray-200 flex flex-col fixed h-full z-10">
        {/* Logo */}
        <div className="p-6 border-b border-gray-100">
          <Link to="/dashboard" className="flex items-center gap-3">
            <div className="w-9 h-9 bg-blue-600 rounded-lg flex items-center justify-center">
              <Scale className="w-5 h-5 text-white" />
            </div>
            <div>
              <p className="text-sm font-bold text-gray-900 leading-tight">ЮрАнализ</p>
              <p className="text-xs text-gray-500">Поиск прецедентов</p>
            </div>
          </Link>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-1">
          {navItems.map(({ path, label, icon: Icon }) => {
            const isActive = location.pathname === path;
            return (
              <Link
                key={path}
                to={path}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-50 text-blue-700'
                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-blue-600' : 'text-gray-400'}`} />
                {label}
              </Link>
            );
          })}
        </nav>

        {/* User section */}
        <div className="p-4 border-t border-gray-100">
          <div className="flex items-center gap-3 px-3 py-2 rounded-lg">
            <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
              {user?.avatarUrl ? (
                <img src={user.avatarUrl} alt="" className="w-8 h-8 rounded-full" />
              ) : (
                <User className="w-4 h-4 text-blue-600" />
              )}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">
                {user?.fullName || user?.email}
              </p>
              <p className="text-xs text-gray-500 capitalize">{user?.role?.toLowerCase()}</p>
            </div>
            <button
              onClick={handleLogout}
              className="p-1.5 text-gray-400 hover:text-red-600 rounded transition-colors"
              title="Выйти"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="flex-1 ml-64">
        <main className="p-8">{children}</main>
      </div>
    </div>
  );
}
