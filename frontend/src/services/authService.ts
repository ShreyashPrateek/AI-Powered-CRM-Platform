import api from './api';
import { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth.types';

export const login = (data: LoginRequest): Promise<AuthResponse> =>
  api.post('/api/auth/login', data).then(r => r.data);

export const register = (data: RegisterRequest): Promise<AuthResponse> =>
  api.post('/api/auth/register', data).then(r => r.data);

export const logout = (refreshToken: string): Promise<void> =>
  api.post('/api/auth/logout', { refreshToken }).then(() => undefined);
