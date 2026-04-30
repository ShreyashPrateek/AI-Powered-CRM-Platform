import api from './api';
import { Notification } from '../store/notificationSlice';

export const getNotifications = (page = 0, size = 20): Promise<{ content: Notification[] }> =>
  api.get('/api/notifications', { params: { page, size } }).then(r => r.data);

export const markNotificationRead = (id: number): Promise<void> =>
  api.patch(`/api/notifications/${id}/read`).then(() => undefined);

export const markAllNotificationsRead = (): Promise<void> =>
  api.patch('/api/notifications/read-all').then(() => undefined);
