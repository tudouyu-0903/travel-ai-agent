import { http } from './api';

const TOKEN_KEY = 'yu_travel_token';
const USER_KEY = 'yu_travel_user';

export function getAuthToken() {
  return localStorage.getItem(TOKEN_KEY);
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
  return Boolean(getAuthToken());
}

function saveAuthSession(data) {
  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(USER_KEY, JSON.stringify(data.user));
  window.dispatchEvent(new Event('auth-changed'));
  return data.user;
}

export async function login(payload) {
  const { data } = await http.post('/auth/login', payload);
  return saveAuthSession(data);
}

export async function register(payload) {
  const { data } = await http.post('/auth/register', payload);
  return saveAuthSession(data);
}

export async function logout() {
  try {
    await http.post('/auth/logout');
  } finally {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.dispatchEvent(new Event('auth-changed'));
  }
}
