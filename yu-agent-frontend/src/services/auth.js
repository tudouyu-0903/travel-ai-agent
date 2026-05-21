import { http } from './api';

const USER_KEY = 'yu_travel_user';

function unwrapResponse(response) {
  const body = response.data;
  if (body && typeof body === 'object' && 'code' in body) {
    if (body.code !== 0) {
      throw new Error(body.message || '请求失败');
    }
    return body.data;
  }
  return body;
}

function sanitizeUser(user) {
  if (!user) {
    return null;
  }

  const { passwordHash, ...safeUser } = user;
  return safeUser;
}

function saveAuthSession(user) {
  const safeUser = sanitizeUser(user);
  if (safeUser) {
    localStorage.setItem(USER_KEY, JSON.stringify(safeUser));
  }
  window.dispatchEvent(new Event('auth-changed'));
  return safeUser;
}

export function getCurrentUser() {
  const rawUser = localStorage.getItem(USER_KEY);
  if (!rawUser) {
    return null;
  }
  try {
    return JSON.parse(rawUser);
  } catch (error) {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

export function isAuthenticated() {
  return Boolean(getCurrentUser());
}

export async function login(payload) {
  const user = unwrapResponse(await http.post('/user/login', {
    userAccount: payload.userAccount,
    password: payload.password
  }));
  return saveAuthSession(user);
}

export async function register(payload) {
  unwrapResponse(await http.post('/user/register', {
    userAccount: payload.userAccount,
    username: payload.username,
    password: payload.password,
    phone: payload.phone || undefined,
    email: payload.email || undefined
  }));

  return login({
    userAccount: payload.userAccount,
    password: payload.password
  });
}

export async function logout() {
  localStorage.removeItem(USER_KEY);
  window.dispatchEvent(new Event('auth-changed'));
}
