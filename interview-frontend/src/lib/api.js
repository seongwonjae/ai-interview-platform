// src/lib/api.js (또는 너 프로젝트의 해당 경로)

// ✅ 배포(prod) 기본값: 같은 origin(nginx) 기준 상대경로
// - dev에서만 VITE_API_BASE=http://localhost:8080 같은 값으로 덮어쓰면 됨
const BASE = (import.meta.env.VITE_API_BASE ?? "").replace(/\/+$/, ""); // 뒤 슬래시 제거

export function getToken() {
  return localStorage.getItem("token") || "";
}

export function setToken(token) {
  localStorage.setItem("token", token);
}

export function clearToken() {
  localStorage.removeItem("token");
}

async function parseBody(res) {
  const txt = await res.text();
  try {
    return txt ? JSON.parse(txt) : null;
  } catch {
    return txt;
  }
}

// path 정규화:
// - "api/auth/login" => "/api/auth/login"
// - "/api/auth/login" => "/api/auth/login"
function normalizePath(path) {
  const p = (path ?? "").toString().trim();
  if (!p) return "/";
  return p.startsWith("/") ? p : `/${p}`;
}

export async function api(path, { method = "GET", body, headers = {} } = {}) {
  const token = getToken();
  const normalizedPath = normalizePath(path);

  const url = `${BASE}${normalizedPath}`; // BASE가 ""면 "/api/..." 형태로 요청됨

  const res = await fetch(url, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      ...headers,
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  const data = await parseBody(res);

  if (!res.ok) {
    if (res.status === 401) clearToken();
    const err = new Error(
      data?.message || data?.error || data?.detail || `HTTP ${res.status}`
    );
    err.status = res.status;
    err.data = data;
    throw err;
  }

  return data;
}