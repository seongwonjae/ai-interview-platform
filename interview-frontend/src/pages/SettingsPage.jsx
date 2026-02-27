import { useEffect, useState } from "react";
import { api } from "../lib/api";
import { useNavigate } from "react-router-dom";

export default function SettingsPage() {
  const nav = useNavigate();

  const [role, setRole] = useState("BACKEND");
  const [difficulty, setDifficulty] = useState("MEDIUM");
  const [language, setLanguage] = useState("JAVA");

  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    setMsg("");
  }, []);

  const save = async () => {
    setBusy(true);
    setMsg("");
    try {
      await api("/api/settings", { method: "POST", body: { role, difficulty, language } });
      setMsg("저장 완료");
      nav("/sessions");
    } catch (err) {
      setMsg(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg">
      <div className="rounded-2xl border border-[#E2DDD3] bg-white p-6 shadow-[0_1px_3px_0_rgba(0,0,0,0.04)]">
        <h1 className="text-lg font-semibold tracking-tight text-[#1a1a1a]">
          Settings
        </h1>
        <p className="mt-1 text-sm text-[#8C8578]">
          Configure your interview preferences.
        </p>

        <div className="mt-6 space-y-5">
          {/* Role */}
          <div>
            <label className="text-xs font-medium text-[#8C8578]">
              Role
            </label>
            <select
              value={role}
              onChange={(e) => setRole(e.target.value)}
              className="mt-2 w-full rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] px-3 py-2 text-sm text-[#1a1a1a] outline-none focus:border-[#B5AE9E] focus:ring-2 focus:ring-[#B5AE9E]/40"
            >
              <option value="BACKEND">BACKEND</option>
              <option value="FRONTEND">FRONTEND</option>
              <option value="DATA">DATA</option>
              <option value="PM">PM</option>
            </select>
          </div>

          {/* Difficulty */}
          <div>
            <label className="text-xs font-medium text-[#8C8578]">
              Difficulty
            </label>
            <select
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value)}
              className="mt-2 w-full rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] px-3 py-2 text-sm text-[#1a1a1a] outline-none focus:border-[#B5AE9E] focus:ring-2 focus:ring-[#B5AE9E]/40"
            >
              <option value="EASY">EASY</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HARD">HARD</option>
            </select>
          </div>

          {/* Language */}
          <div>
            <label className="text-xs font-medium text-[#8C8578]">
              Language
            </label>
            <select
              value={language}
              onChange={(e) => setLanguage(e.target.value)}
              className="mt-2 w-full rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] px-3 py-2 text-sm text-[#1a1a1a] outline-none focus:border-[#B5AE9E] focus:ring-2 focus:ring-[#B5AE9E]/40"
            >
              <option value="JAVA">JAVA</option>
              <option value="PYTHON">PYTHON</option>
              <option value="JAVASCRIPT">JAVASCRIPT</option>
              <option value="SQL">SQL</option>
            </select>
          </div>

          {/* Message */}
          {msg && (
            <div className="rounded-lg border border-[#E2DDD3] bg-[#F6F1E8] px-3 py-2 text-sm text-[#8C8578]">
              {msg}
            </div>
          )}

          {/* Save button */}
          <button
            disabled={busy}
            onClick={save}
            className="mt-2 w-full rounded-xl bg-[#2c2c2c] px-4 py-2 text-sm font-semibold text-[#f5f0e8] shadow-[0_1px_2px_rgba(0,0,0,0.08)] transition hover:bg-[#1f1f1f] active:scale-[0.98] disabled:opacity-50"
          >
            {busy ? "Saving..." : "Save and continue"}
          </button>
        </div>
      </div>
    </div>
  );
}
