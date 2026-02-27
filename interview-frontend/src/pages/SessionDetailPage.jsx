import { useEffect, useMemo, useState } from "react";
import { api } from "../lib/api";
import { useParams, useNavigate } from "react-router-dom";

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

export default function SessionDetailPage() {
  const { sessionId } = useParams();
  const nav = useNavigate();

  const [session, setSession] = useState(null);
  const [msg, setMsg] = useState("");

  const [question, setQuestion] = useState(null);
  const [answer, setAnswer] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const [submissionId, setSubmissionId] = useState(null);
  const [status, setStatus] = useState(null);
  const [evaluation, setEvaluation] = useState(null);

  const canSubmit = useMemo(() => !!question && answer.trim().length > 0, [question, answer]);

  const loadDetail = async () => {
    setMsg("");
    try {
      const data = await api(`/api/sessions/${sessionId}`);
      setSession(data);
    } catch (err) {
      setMsg(err.message);
    }
  };

  useEffect(() => {
    setQuestion(null);
    setAnswer("");
    setSubmissionId(null);
    setStatus(null);
    setEvaluation(null);
    loadDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionId]);

  const recommend = async () => {
    setMsg("");
    try {
      const q = await api("/api/questions/recommend");
      setQuestion(q);
      setAnswer("");
    } catch (err) {
      setMsg(err.message);
    }
  };

  const submit = async () => {
    if (!canSubmit) return;
    setSubmitting(true);
    setMsg("");
    setStatus(null);
    setEvaluation(null);

    try {
      const res = await api(`/api/sessions/${sessionId}/questions/${question.id}/submit`, {
        method: "POST",
        body: { answer_text: answer, prompt_version: "v1" },
      });

      const sid = res?.submission_id ?? res?.id ?? res?.submissionId;
      if (!sid) throw new Error("submission_id를 응답에서 찾지 못했어");

      setSubmissionId(sid);
      await loadDetail();
      nav(`/submissions/${sid}`);
    } catch (err) {
      setMsg(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const pollStatusAndEval = async (sid) => {
    if (!sid) return;

    // 1️⃣ 먼저 즉시 evaluation 한번 시도 (이미 DONE이면 바로 뜸)
    try {
      const ev = await api(`/api/submissions/${sid}/evaluation`);
      setEvaluation(ev);
      return;
    } catch (err) {
      if (err.status !== 404) {
        setMsg(err.message);
        return;
      }
    }

    // 2️⃣ status 폴링 (최대 15회, 1.5초 간격)
    for (let i = 0; i < 15; i++) {
      const s = await api(`/api/submissions/${sid}/status`);
      setStatus(s);

      const st = (s?.status || "").toLowerCase();

      if (st === "done" || st === "failed" || st === "error") {
        break;
      }

      await sleep(1500);
    }

    // 3️⃣ evaluation 폴링 (최대 10회)
    for (let i = 0; i < 10; i++) {
      try {
        const ev = await api(`/api/submissions/${sid}/evaluation`);
        setEvaluation(ev);
        return;
      } catch (err) {
        if (err.status === 404) {
          await sleep(1500);
          continue;
        }
        setMsg(err.message);
        return;
      }
    }
  };

  const pickSubmission = async (sid) => {
    setSubmissionId(sid);
    setStatus(null);
    setEvaluation(null);
    setMsg("");
    await pollStatusAndEval(sid);
  };

  const card = "rounded-2xl border border-[#E2DDD3] bg-white p-5 shadow-[0_1px_3px_0_rgba(0,0,0,0.04)]";
  const subtleBtn =
    "rounded-xl border border-[#E2DDD3] bg-white px-3 py-2 text-sm font-medium text-[#1a1a1a] shadow-[0_1px_2px_rgba(0,0,0,0.04)] transition hover:bg-[#EDE8DE] active:scale-[0.98]";
  const input =
    "w-full rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] px-3 py-2 text-sm text-[#1a1a1a] outline-none placeholder:text-[#8C8578] focus:border-[#B5AE9E] focus:ring-2 focus:ring-[#B5AE9E]/40";

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className={card}>
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <h1 className="text-lg font-semibold tracking-tight text-[#1a1a1a]">
              Session #{sessionId}
            </h1>
            <div className="mt-1 truncate text-sm text-[#8C8578]">{session?.title || ""}</div>
          </div>

          <div className="flex items-center gap-2">
            <button className={subtleBtn} onClick={() => nav("/sessions")}>
              Back
            </button>
            <button className={subtleBtn} onClick={loadDetail}>
              Refresh
            </button>
          </div>
        </div>

        {msg && (
          <div className="mt-4 rounded-lg border border-[#c44a3f]/25 bg-[#c44a3f]/10 px-3 py-2 text-sm text-[#8a2d25]">
            {msg}
          </div>
        )}
      </div>

      {/* Submissions */}
      <div className={card}>
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold text-[#1a1a1a]">Submissions</h2>
          <div className="text-xs text-[#8C8578]">{(session?.submissions || []).length} items</div>
        </div>

        <div className="mt-4 space-y-2">
          {(session?.submissions || []).map((s) => {
            const active = submissionId === s.id;
            return (
              <button
                key={s.id}
                onClick={() => pickSubmission(s.id)}
                className={[
                  "w-full rounded-xl border px-4 py-3 text-left transition",
                  active
                    ? "border-[#B5AE9E] bg-[#EDE8DE]/60"
                    : "border-[#E2DDD3] bg-white hover:bg-[#EDE8DE]/40",
                ].join(" ")}
              >
                <div className="flex items-center justify-between gap-3">
                  <div className="text-sm font-semibold text-[#1a1a1a]">
                    #{s.id} · {String(s.status || "").toUpperCase()}
                  </div>
                  <div className="text-xs text-[#8C8578]">
                    Score:{" "}
                    <span className="font-mono font-semibold text-[#1a1a1a]">
                      {s.overall_score ?? "-"}
                    </span>
                  </div>
                </div>

                <div className="mt-1 text-sm text-[#8C8578]">
                  Q: {s.question?.text}
                </div>
              </button>
            );
          })}

          {(session?.submissions || []).length === 0 && (
            <div className="rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] p-4 text-sm text-[#8C8578]">
              아직 제출이 없어. 아래에서 질문을 추천받고 답변을 제출해봐.
            </div>
          )}
        </div>
      </div>

      {/* Interview */}
      <div className={card}>
        <div className="flex items-center justify-between gap-3">
          <h2 className="text-sm font-semibold text-[#1a1a1a]">Interview</h2>
          <button className={subtleBtn} onClick={recommend}>
            질문 추천
          </button>
        </div>

        <div className="mt-4">
          {question ? (
            <div className="rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] p-4">
              <div className="text-xs text-[#8C8578]">Question #{question.id}</div>
              <div className="mt-1 text-sm font-semibold text-[#1a1a1a]">{question.text}</div>
            </div>
          ) : (
            <div className="rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] p-4 text-sm text-[#8C8578]">
              “질문 추천”을 눌러 시작해.
            </div>
          )}
        </div>

        <textarea
          className={"mt-4 min-h-[140px] " + input}
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          placeholder="답변을 입력하세요..."
        />

        <div className="mt-4 flex items-center justify-end">
          <button
            disabled={!canSubmit || submitting}
            className="rounded-xl bg-[#2c2c2c] px-4 py-2 text-sm font-semibold text-[#f5f0e8] shadow-[0_1px_2px_rgba(0,0,0,0.08)] transition hover:bg-[#1f1f1f] active:scale-[0.98] disabled:opacity-50"
            onClick={submit}
          >
            {submitting ? "제출 중..." : "답변 제출"}
          </button>
        </div>
      </div>

      {/* Status + Evaluation */}
      <div className="grid gap-4 md:grid-cols-2">
        <div className={card}>
          <h2 className="text-sm font-semibold text-[#1a1a1a]">Status</h2>

          {submissionId ? (
            <div className="mt-3 space-y-1 text-sm">
              <div className="text-[#8C8578]">
                submissionId: <span className="font-mono font-semibold text-[#1a1a1a]">{submissionId}</span>
              </div>
              <div className="text-[#8C8578]">
                status: <span className="font-semibold text-[#1a1a1a]">{status?.status ?? "-"}</span>
              </div>
              {status?.error_message && (
                <div className="mt-2 rounded-lg border border-[#c44a3f]/25 bg-[#c44a3f]/10 px-3 py-2 text-sm text-[#8a2d25]">
                  error: {status.error_message}
                </div>
              )}
            </div>
          ) : (
            <div className="mt-3 text-sm text-[#8C8578]">
              제출을 선택하거나 새로 제출하면 표시됨
            </div>
          )}
        </div>

        <div className={card}>
          <h2 className="text-sm font-semibold text-[#1a1a1a]">Evaluation</h2>

          {evaluation ? (
            <div className="mt-3 space-y-3 text-sm">
              <div className="text-base font-semibold text-[#1a1a1a]">
                Overall: <span className="font-mono">{evaluation.overall_score}</span>
              </div>

              {evaluation.scores && (
                <div className="grid grid-cols-3 gap-2">
                  <div className="rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] p-3">
                    <div className="text-xs text-[#8C8578]">구조</div>
                    <div className="mt-1 font-mono font-semibold text-[#1a1a1a]">
                      {evaluation.scores.structure}
                    </div>
                  </div>
                  <div className="rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] p-3">
                    <div className="text-xs text-[#8C8578]">명확</div>
                    <div className="mt-1 font-mono font-semibold text-[#1a1a1a]">
                      {evaluation.scores.clarity}
                    </div>
                  </div>
                  <div className="rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] p-3">
                    <div className="text-xs text-[#8C8578]">관련</div>
                    <div className="mt-1 font-mono font-semibold text-[#1a1a1a]">
                      {evaluation.scores.relevance}
                    </div>
                  </div>
                </div>
              )}

              {Array.isArray(evaluation.strengths) && (
                <div className="rounded-xl border border-[#E2DDD3] bg-white p-3">
                  <div className="text-xs font-semibold text-[#1a1a1a]">강점</div>
                  <ul className="mt-2 list-disc space-y-1 pl-5 text-[#8C8578]">
                    {evaluation.strengths.map((x, i) => (
                      <li key={i}>{x}</li>
                    ))}
                  </ul>
                </div>
              )}

              {Array.isArray(evaluation.improvements) && (
                <div className="rounded-xl border border-[#E2DDD3] bg-white p-3">
                  <div className="text-xs font-semibold text-[#1a1a1a]">개선점</div>
                  <ul className="mt-2 list-disc space-y-1 pl-5 text-[#8C8578]">
                    {evaluation.improvements.map((x, i) => (
                      <li key={i}>{x}</li>
                    ))}
                  </ul>
                </div>
              )}

              {evaluation.rewritten_answer && (
                <div className="rounded-xl border border-[#E2DDD3] bg-[#F6F1E8] p-3">
                  <div className="text-xs font-semibold text-[#1a1a1a]">모범 답안</div>
                  <div className="mt-2 whitespace-pre-wrap text-[#8C8578]">
                    {evaluation.rewritten_answer}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="mt-3 text-sm text-[#8C8578]">
              평가가 준비되면 표시됨 (제출 후 자동 폴링)
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
