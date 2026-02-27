import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, setToken, getToken } from "../lib/api";

export default function LoginPage() {
  const nav = useNavigate();

  const [mode, setMode] = useState("login"); // "login" | "register"
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState("");

  // ✅ 이미 토큰 있으면 세션으로 (렌더 중 nav 방지)
  useEffect(() => {
    if (getToken()) {
      nav("/sessions", { replace: true });
    }
  }, [nav]);

  const submit = async (e) => {
    e.preventDefault();
    setMsg("");
    setBusy(true);

    try {
      if (mode === "register") {
        await api("/api/auth/register", { method: "POST", body: { email, password } });
        setMsg("회원가입 완료! 이제 로그인 해줘.");
        setMode("login");
      } else {
        const res = await api("/api/auth/login", { method: "POST", body: { email, password } });
        setToken(res.access_token);
        nav("/settings");
      }
    } catch (err) {
      setMsg(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="mx-auto w-full max-w-md">
      <div className="rounded-2xl border border-[#E2DDD3] bg-white p-6 shadow-[0_1px_3px_0_rgba(0,0,0,0.04)]">
        {/* Header */}
        <div className="flex items-start justify-between gap-3">
          <div>
            <h1 className="text-lg font-semibold tracking-tight text-[#1a1a1a]">
              {mode === "login" ? "로그인" : "회원가입"}
            </h1>
            <p className="mt-1 text-sm text-[#8C8578]">
              {mode === "login" ? "토큰 발급 후 API 호출" : "계정 생성"}
            </p>
          </div>

          {/* mode toggle pill */}
          <div className="inline-flex rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] p-1">
            <button
              type="button"
              onClick={() => setMode("login")}
              className={[
                "rounded-lg px-3 py-1.5 text-xs font-semibold transition",
                mode === "login"
                  ? "bg-white text-[#1a1a1a] shadow-[0_1px_2px_rgba(0,0,0,0.04)]"
                  : "text-[#8C8578] hover:text-[#1a1a1a]",
              ].join(" ")}
            >
              Login
            </button>
            <button
              type="button"
              onClick={() => setMode("register")}
              className={[
                "rounded-lg px-3 py-1.5 text-xs font-semibold transition",
                mode === "register"
                  ? "bg-white text-[#1a1a1a] shadow-[0_1px_2px_rgba(0,0,0,0.04)]"
                  : "text-[#8C8578] hover:text-[#1a1a1a]",
              ].join(" ")}
            >
              Register
            </button>
          </div>
        </div>

        <form onSubmit={submit} className="mt-6 space-y-4">
          <div>
            <label className="text-xs font-medium text-[#8C8578]">Email</label>
            <input
              className="mt-2 w-full rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] px-3 py-2 text-sm text-[#1a1a1a] outline-none placeholder:text-[#8C8578] focus:border-[#B5AE9E] focus:ring-2 focus:ring-[#B5AE9E]/40"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="test1@test.com"
              autoComplete="email"
              required
              disabled={busy}
            />
          </div>

          <div>
            <label className="text-xs font-medium text-[#8C8578]">Password</label>
            <input
              className="mt-2 w-full rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] px-3 py-2 text-sm text-[#1a1a1a] outline-none placeholder:text-[#8C8578] focus:border-[#B5AE9E] focus:ring-2 focus:ring-[#B5AE9E]/40"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="1234"
              autoComplete={mode === "login" ? "current-password" : "new-password"}
              required
              disabled={busy}
            />
          </div>

          {msg && (
            <div
              className={[
                "rounded-lg border px-3 py-2 text-sm",
                mode === "register"
                  ? "border-[#2f7a4a]/25 bg-[#2f7a4a]/10 text-[#235b37]"
                  : "border-[#c44a3f]/25 bg-[#c44a3f]/10 text-[#8a2d25]",
              ].join(" ")}
            >
              {msg}
            </div>
          )}

          <button
            disabled={busy}
            className="w-full rounded-xl bg-[#2c2c2c] px-4 py-2 text-sm font-semibold text-[#f5f0e8] shadow-[0_1px_2px_rgba(0,0,0,0.08)] transition hover:bg-[#1f1f1f] active:scale-[0.98] disabled:opacity-50"
          >
            {busy ? "처리 중..." : mode === "login" ? "Login" : "Register"}
          </button>

          <div className="text-xs text-[#8C8578]">
            {mode === "login"
              ? "처음이면 Register로 계정 생성 후 로그인 해주세요."
              : "계정 생성 후 Login으로 돌아가서 로그인 해주세요."}
          </div>
        </form>
      </div>
    </div>
  );
}
