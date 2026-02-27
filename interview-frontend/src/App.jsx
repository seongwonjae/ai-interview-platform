import { Routes, Route, Navigate, NavLink, useNavigate } from "react-router-dom";

import { getToken, clearToken } from "./lib/api";
import LoginPage from "./pages/LoginPage";
import SettingsPage from "./pages/SettingsPage";
import SessionsPage from "./pages/SessionsPage";
import SessionDetailPage from "./pages/SessionDetailPage";
import SubmissionResultPage from "./pages/SubmissionResultPage";


function Layout({ children }) {
  const nav = useNavigate();
  const token = getToken();

  const linkClass = ({ isActive }) =>
    [
      "rounded-lg px-3 py-1.5 text-sm font-medium transition",
      isActive
        ? "bg-white border border-[#E2DDD3] text-[#1a1a1a] shadow-[0_1px_2px_rgba(0,0,0,0.04)]"
        : "text-[#8C8578] hover:text-[#1a1a1a] hover:bg-[#EDE8DE]",
    ].join(" ");

  return (
    <div className="min-h-screen bg-[#F6F1E8] text-[#1a1a1a]">
      <header className="sticky top-0 z-40 border-b border-[#E2DDD3] bg-[#F6F1E8]/85 backdrop-blur">
        <div className="mx-auto max-w-4xl px-4 py-3 flex items-center justify-between">
          <h1 className="font-semibold text-[15px] tracking-tight text-[#1a1a1a]">
            AI Interview
          </h1>

          <div className="flex items-center gap-2">
            <NavLink className={linkClass} to="/sessions">
              Sessions
            </NavLink>

            <NavLink className={linkClass} to="/settings">
              Settings
            </NavLink>

            {token && (
              <button
                className="ml-1 rounded-lg border border-[#E2DDD3] bg-white px-3 py-1.5 text-sm font-semibold text-[#1a1a1a] shadow-[0_1px_2px_rgba(0,0,0,0.04)] transition hover:bg-[#EDE8DE] active:scale-[0.98]"
                onClick={() => {
                  clearToken();
                  nav("/login");
                }}
              >
                Logout
              </button>
            )}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-4xl px-4 py-6">{children}</main>
    </div>
  );
}

function RequireAuth({ children }) {
  const token = getToken();
  if (!token) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/sessions" replace />} />
        <Route path="/login" element={<LoginPage />} />

        <Route path="/settings" element={<RequireAuth><SettingsPage /></RequireAuth>} />
        <Route path="/sessions" element={<RequireAuth><SessionsPage /></RequireAuth>} />
        <Route path="/sessions/:sessionId" element={<RequireAuth><SessionDetailPage /></RequireAuth>} />
        <Route
          path="/submissions/:submissionId"
          element={
            <RequireAuth>
              <SubmissionResultPage />
            </RequireAuth>
          }
        />

        <Route path="*" element={<div>Not found</div>} />
      </Routes>
    </Layout>
  );
}
