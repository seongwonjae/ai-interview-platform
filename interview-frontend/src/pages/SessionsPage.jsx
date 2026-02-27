import { useEffect, useState } from "react";
import { Plus, Sparkles } from "lucide-react";
import { api } from "../lib/api";
import { SessionCard } from "../components/SessionCard";
import { useNavigate } from "react-router-dom";

export default function SessionsPage() {
  const nav = useNavigate();

  const [sessions, setSessions] = useState([]);
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState("");

  // create modal
  const [createOpen, setCreateOpen] = useState(false);
  const [newTitle, setNewTitle] = useState("AI Interview Session");

  // delete modal
  const [deleteTarget, setDeleteTarget] = useState(null);

  const load = async () => {
    setMsg("");
    try {
      const data = await api("/api/sessions");
      const mapped = (Array.isArray(data) ? data : []).map((s) => ({
        id: String(s.id),
        title: s.title,
        createdAt: s.createdAt,
        status: s.latestStatus,
        overallScore: s.overallScore,
      }));
      setSessions(mapped);
    } catch (e) {
      setMsg(e.message);
    }
  };

  useEffect(() => { load(); }, []);

  const openCreateModal = () => {
    setMsg("");
    setNewTitle("AI Interview Session");
    setCreateOpen(true);
  };

  const createSession = async () => {
    const title = (newTitle || "").trim();
    if (!title) {
      setMsg("제목을 입력해주세요.");
      return;
    }

    setBusy(true);
    setMsg("");
    try {
      await api("/api/sessions", { method: "POST", body: { title } });
      setCreateOpen(false);
      await load();
    } catch (e) {
      setMsg(e.message);
    } finally {
      setBusy(false);
    }
  };

  const deleteSession = async () => {
    if (!deleteTarget) return;

    setBusy(true);
    setMsg("");
    try {
      await api(`/api/sessions/${deleteTarget}`, { method: "DELETE" });
      setDeleteTarget(null);
      await load();
    } catch (e) {
      setMsg(e.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-120px)] bg-[#F6F1E8] text-[#1a1a1a]">
      <div className="mx-auto w-full max-w-4xl px-4 py-10 sm:px-6 sm:py-14">
        {/* Header (v0 느낌으로) */}
        <div className="flex items-center justify-between gap-4">
          <div className="min-w-0">
            <div className="flex items-center gap-3">
              <h1 className="truncate text-xl font-semibold tracking-tight sm:text-2xl">
                My Interview Sessions
              </h1>
              <span className="inline-flex items-center justify-center rounded-md bg-[#EDE8DE] px-2 py-0.5 text-xs font-medium text-[#8C8578] tabular-nums">
                {sessions.length}
              </span>
            </div>
            <p className="mt-1 text-sm text-[#8C8578]">
              Manage your interview sessions and track evaluation progress.
            </p>

            {msg && (
              <div className="mt-3 rounded-lg border border-[#c44a3f]/25 bg-[#c44a3f]/10 px-3 py-2 text-sm text-[#8a2d25]">
                {msg}
              </div>
            )}
          </div>

          <button
            onClick={openCreateModal}
            disabled={busy}
            className="inline-flex items-center gap-1.5 rounded-lg border border-[#E2DDD3] bg-white px-3.5 py-2 text-sm font-medium text-[#1a1a1a] shadow-[0_1px_2px_0_rgba(0,0,0,0.04)] transition-all duration-150 hover:bg-[#EDE8DE] hover:shadow-[0_2px_6px_0_rgba(0,0,0,0.06)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#B5AE9E] active:scale-[0.98] disabled:opacity-60"
          >
            <Plus className="h-4 w-4" />
            <span className="hidden sm:inline">New Session</span>
            <span className="sm:hidden">New</span>
          </button>
        </div>

        {/* List / Empty */}
        <div className="mt-8">
          {sessions.length > 0 ? (
            <div className="flex flex-col gap-2">
              {sessions.map((session) => (
                <SessionCard
                  key={session.id}
                  session={session}
                  onClick={() => nav(`/sessions/${session.id}`)}
                  onDelete={() => setDeleteTarget(session.id)}
                  deleting={busy}
                />
              ))}
            </div>
          ) : (
            !msg && (
              <div className="mt-14 rounded-2xl border border-[#E2DDD3] bg-white p-10 shadow-[0_1px_3px_0_rgba(0,0,0,0.04)]">
                <div className="flex flex-col items-center justify-center gap-4 text-center">
                  <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-[#E2DDD3] bg-[#EDE8DE]">
                    <Sparkles className="h-5 w-5 text-[#8C8578]" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-[#1a1a1a]">No sessions yet</p>
                    <p className="mt-1 text-sm text-[#8C8578]">
                      Create your first AI interview session to get started.
                    </p>
                  </div>
                  <button
                    onClick={openCreateModal}
                    className="inline-flex items-center gap-1.5 rounded-lg border border-[#E2DDD3] bg-white px-3.5 py-2 text-sm font-medium text-[#1a1a1a] shadow-[0_1px_2px_0_rgba(0,0,0,0.04)] transition-all duration-150 hover:bg-[#EDE8DE] hover:shadow-[0_2px_6px_0_rgba(0,0,0,0.06)] active:scale-[0.98]"
                  >
                    <Plus className="h-4 w-4" />
                    New Session
                  </button>
                </div>
              </div>
            )
          )}
        </div>
      </div>

      {/* Create modal (베이지 톤) */}
      {createOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-[2px]"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget && !busy) setCreateOpen(false);
          }}
        >
          <div className="w-full max-w-sm rounded-2xl border border-[#E2DDD3] bg-white p-6 shadow-[0_20px_80px_-40px_rgba(0,0,0,0.25)]">
            <h2 className="text-base font-semibold text-[#1a1a1a]">New Session</h2>
            <p className="mt-1 text-sm text-[#8C8578]">
              제목을 적어주세요.
            </p>

            <div className="mt-4">
              <label className="text-xs text-[#8C8578]">Title</label>
              <input
                autoFocus
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") createSession();
                  if (e.key === "Escape" && !busy) setCreateOpen(false);
                }}
                disabled={busy}
                className="mt-2 w-full rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] px-3 py-2 text-sm text-[#1a1a1a] outline-none placeholder:text-[#8C8578] focus:border-[#B5AE9E] focus:ring-2 focus:ring-[#B5AE9E]/40 disabled:opacity-60"
                placeholder="e.g. 백엔드 면접 · 트랜잭션"
              />
              <div className="mt-2 text-[11px] text-[#8C8578]">Enter: 생성 · Esc: 닫기</div>
            </div>

            <div className="mt-6 flex justify-end gap-2">
              <button
                onClick={() => !busy && setCreateOpen(false)}
                disabled={busy}
                className="rounded-xl border border-[#E2DDD3] bg-white px-4 py-2 text-sm text-[#1a1a1a] hover:bg-[#EDE8DE] disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={createSession}
                disabled={busy}
                className="rounded-xl bg-[#2c2c2c] px-4 py-2 text-sm font-semibold text-[#f5f0e8] hover:bg-[#1f1f1f] disabled:opacity-50"
              >
                {busy ? "Creating..." : "Create"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete modal (베이지 톤) */}
      {deleteTarget && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-[2px]"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget && !busy) setDeleteTarget(null);
          }}
        >
          <div className="w-full max-w-sm rounded-2xl border border-[#E2DDD3] bg-white p-6 shadow-[0_20px_80px_-40px_rgba(0,0,0,0.25)]">
            <h2 className="text-base font-semibold text-[#1a1a1a]">Delete Session</h2>
            <p className="mt-1 text-sm text-[#8C8578]">
              제출 기록과 평가 결과도 함께 삭제됩니다. 복구할 수 없습니다.
            </p>

            <div className="mt-6 flex justify-end gap-2">
              <button
                onClick={() => setDeleteTarget(null)}
                disabled={busy}
                className="rounded-xl border border-[#E2DDD3] bg-white px-4 py-2 text-sm text-[#1a1a1a] hover:bg-[#EDE8DE] disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={deleteSession}
                disabled={busy}
                className="rounded-xl bg-[#c44a3f] px-4 py-2 text-sm font-semibold text-white hover:bg-[#b14238] disabled:opacity-50"
              >
                {busy ? "Deleting..." : "Delete"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
