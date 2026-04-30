import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../../store/store';
import { setNotifications, markRead, markAllRead } from '../../store/notificationSlice';
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '../../services/notificationService';
import { formatDate } from '../../utils/formatters';

const TYPE_ICONS: Record<string, string> = { EMAIL: '📧', SMS: '📱', IN_APP: '🔔' };

export default function Notifications() {
  const dispatch = useDispatch();
  const { notifications, unreadCount } = useSelector((s: RootState) => s.notifications);

  useEffect(() => {
    getNotifications()
      .then(data => dispatch(setNotifications(data.content ?? [])))
      .catch(() => {});
  }, [dispatch]);

  const handleMarkRead = async (id: number) => {
    await markNotificationRead(id).catch(() => {});
    dispatch(markRead(id));
  };

  const handleMarkAll = async () => {
    await markAllNotificationsRead().catch(() => {});
    dispatch(markAllRead());
  };

  return (
    <div className="max-w-2xl space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">
          Notifications {unreadCount > 0 && <span className="ml-2 text-sm bg-red-500 text-white px-2 py-0.5 rounded-full">{unreadCount}</span>}
        </h2>
        {unreadCount > 0 && (
          <button onClick={handleMarkAll} className="text-sm text-blue-600 hover:underline">
            Mark all read
          </button>
        )}
      </div>

      <div className="bg-white rounded-xl shadow divide-y divide-gray-100">
        {notifications.map(n => (
          <div
            key={n.id}
            onClick={() => n.status === 'UNREAD' && handleMarkRead(n.id)}
            className={`flex gap-3 px-4 py-3 cursor-pointer hover:bg-gray-50 transition ${n.status === 'UNREAD' ? 'bg-blue-50' : ''}`}
          >
            <span className="text-xl mt-0.5">{TYPE_ICONS[n.type] ?? '🔔'}</span>
            <div className="flex-1 min-w-0">
              <p className={`text-sm ${n.status === 'UNREAD' ? 'font-semibold text-gray-900' : 'text-gray-700'}`}>{n.title}</p>
              <p className="text-xs text-gray-500 mt-0.5 truncate">{n.message}</p>
              <p className="text-xs text-gray-400 mt-1">{formatDate(n.createdAt)}</p>
            </div>
            {n.status === 'UNREAD' && <div className="w-2 h-2 bg-blue-500 rounded-full mt-2 flex-shrink-0" />}
          </div>
        ))}
        {notifications.length === 0 && (
          <div className="text-center py-12 text-gray-400">No notifications</div>
        )}
      </div>
    </div>
  );
}
