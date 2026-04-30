import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import leadReducer from './leadSlice';
import dealReducer from './dealSlice';
import analyticsReducer from './analyticsSlice';
import notificationReducer from './notificationSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    leads: leadReducer,
    deals: dealReducer,
    analytics: analyticsReducer,
    notifications: notificationReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
