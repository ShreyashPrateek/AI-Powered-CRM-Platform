import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface Notification {
  id: number;
  type: 'EMAIL' | 'SMS' | 'IN_APP';
  title: string;
  message: string;
  status: 'UNREAD' | 'READ';
  createdAt: string;
}

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
}

const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
};

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    setNotifications(state, action: PayloadAction<Notification[]>) {
      state.notifications = action.payload;
      state.unreadCount = action.payload.filter(n => n.status === 'UNREAD').length;
    },
    addNotification(state, action: PayloadAction<Notification>) {
      state.notifications.unshift(action.payload);
      if (action.payload.status === 'UNREAD') state.unreadCount += 1;
    },
    markRead(state, action: PayloadAction<number>) {
      const n = state.notifications.find(n => n.id === action.payload);
      if (n && n.status === 'UNREAD') {
        n.status = 'READ';
        state.unreadCount = Math.max(0, state.unreadCount - 1);
      }
    },
    markAllRead(state) {
      state.notifications.forEach(n => { n.status = 'READ'; });
      state.unreadCount = 0;
    },
  },
});

export const { setNotifications, addNotification, markRead, markAllRead } = notificationSlice.actions;
export default notificationSlice.reducer;
