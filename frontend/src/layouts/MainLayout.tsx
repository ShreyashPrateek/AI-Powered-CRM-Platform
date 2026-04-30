import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useSelector } from 'react-redux';
import { RootState } from '../store/store';

const NAV = [
  { to: '/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/leads', label: 'Leads', icon: '👥' },
  { to: '/deals', label: 'Pipeline', icon: '💼' },
  { to: '/email/campaigns', label: 'Campaigns', icon: '📧' },
  { to: '/email/ai-reply', label: 'AI Reply', icon: '🤖' },
  { to: '/analytics', label: 'Analytics', icon: '📈' },
  { to: '/ai', label: 'AI Assistant', icon: '✨' },
  { to: '/notifications', label: 'Notifications', icon: '🔔' },
];

export default function MainLayout() {
  const { email, logout } = useAuth();
  const { pathname } = useLocation();
  const unread = useSelector((s: RootState) => s.notifications.unreadCount);

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="w-56 bg-gray-900 text-white flex flex-col">
        <div className="px-4 py-5 border-b border-gray-700">
          <h1 className="text-lg font-bold">AI CRM</h1>
          <p className="text-xs text-gray-400 truncate">{email}</p>
        </div>
        <nav className="flex-1 py-4 space-y-1 px-2">
          {NAV.map(({ to, label, icon }) => (
            <Link
              key={to}
              to={to}
              className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition ${
                pathname.startsWith(to)
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-300 hover:bg-gray-700'
              }`}
            >
              <span>{icon}</span>
              <span>{label}</span>
              {to === '/notifications' && unread > 0 && (
                <span className="ml-auto bg-red-500 text-white text-xs rounded-full px-1.5">{unread}</span>
              )}
            </Link>
          ))}
        </nav>
        <button
          onClick={logout}
          className="m-3 px-3 py-2 text-sm text-gray-400 hover:text-white hover:bg-gray-700 rounded-lg text-left"
        >
          🚪 Logout
        </button>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto p-6">
        <Outlet />
      </main>
    </div>
  );
}
