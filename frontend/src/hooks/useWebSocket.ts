import { useEffect, useRef } from 'react';
import { useDispatch } from 'react-redux';
import { addNotification } from '../store/notificationSlice';

export function useWebSocket(userId: string | null) {
  const dispatch = useDispatch();
  const wsRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    if (!userId) return;

    const ws = new WebSocket(`ws://localhost:8086/ws/notifications?userId=${userId}`);
    wsRef.current = ws;

    ws.onmessage = (event) => {
      try {
        const notification = JSON.parse(event.data);
        dispatch(addNotification(notification));
      } catch {
        // ignore malformed messages
      }
    };

    ws.onerror = () => ws.close();

    return () => ws.close();
  }, [userId, dispatch]);

  return wsRef;
}
