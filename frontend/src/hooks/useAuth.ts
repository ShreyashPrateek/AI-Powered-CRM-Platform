import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { RootState } from '../store/store';
import { clearAuth } from '../store/authSlice';
import { logout } from '../services/authService';

export function useAuth() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const auth = useSelector((s: RootState) => s.auth);

  const handleLogout = async () => {
    if (auth.refreshToken) {
      await logout(auth.refreshToken).catch(() => {});
    }
    dispatch(clearAuth());
    navigate('/login');
  };

  const hasRole = (role: string) => auth.roles.includes(role);

  return { ...auth, logout: handleLogout, hasRole };
}
