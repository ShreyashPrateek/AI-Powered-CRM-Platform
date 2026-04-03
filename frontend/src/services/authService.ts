import api from "./api";

export const login = (form: { email: string; password: string }) =>
  api.post("/api/auth/login", form).then((res) => res.data);

export const register = (form: { email: string; password: string }) =>
  api.post("/api/auth/register", form).then((res) => res.data);

export const refresh = (refreshToken: string) =>
  api.post("/api/auth/refresh", { refreshToken }).then((res) => res.data);

export const logout = (refreshToken: string) =>
  api.post("/api/auth/logout", { refreshToken });
