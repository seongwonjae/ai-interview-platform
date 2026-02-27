import { useMemo } from "react";
import { Link, useParams } from "react-router-dom";
import { useSubmissionStatus } from "../hooks/useSubmissionStatus";

function ScoreBadge({ score }: { score: number | null }) {
  if (score == null) {
    return (
      <span className="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-700">
        -
      </span>
    );
  }

  const tone =
    score >= 80
      ? "bg-emerald-100 text-emerald-800 ring-emerald-200"
      : score >= 60
      ? "bg-amber-100 text-amber-800 ring-amber-200"
      : "bg-rose-100 text-rose-800 ring-rose-200";

  return (
    <span className={`rounded-full px-3 py-1 text-sm font-semibold ring-1 ${tone}`}>
      {score} / 100
    </span>
  );
}

function StatusPill({ status }: { status: string }) {
  const s = (status || "").toLowerCase();
  const tone =
    s === "DONE"
      ? "bg-emerald-50 text-emerald-800 ring-emerald-200"
      : s === "FAILED"
      ? "bg-rose-50 text-rose-800 ring-rose-200"
      : "bg-slate-50 text-slate-700 ring-slate-200";

  return (
    <span className={`rounded-full px-3 py-1 text-xs font-semibold ring-1 ${tone}`}>
      {status}
    </span>
  );
}

function Spinner() {
  return (
    <div className="inline-flex items-center gap-2 text-sm text-slate-600">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-transparent" />
      평가 중…
    </div>
  );
}

export default function SubmissionResultPage() {
  const { submissionId } = useParams<{ submissionId: string }>();
  const sid = useMemo(() => (submissionId ? Number(submissionId) : null), [submissionId]);

  const { data, loading, err } = useSubmissionStatus(sid);

  if (!sid) {
    return (
      <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        submissionId가 없습니다.
      </div>
    );
  }

  const status = data?.status ?? (loading ? "PROCESSING" : "-");
  const isProcessing = status === "PROCESSING" || status === "PENDING";

  return (
    <div className="space-y-4">
      <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <div className="text-xs font-medium text-slate-500">제출 결과</div>
            <div className="mt-1 flex items-center gap-2">
              <h1 className="text-xl font-bold tracking-tight">Submission #{sid}</h1>
              <StatusPill status={status} />
            </div>
            <div className="mt-2 text-sm text-slate-600">
              {isProcessing ? <Spinner /> : "평가가 완료되었습니다."}
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Link
              to="/sessions"
              className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-medium hover:bg-slate-50"
            >
              세션 목록
            </Link>
            <button
              onClick={() => window.location.reload()}
              className="rounded-lg bg-black px-3 py-2 text-sm font-semibold text-white hover:opacity-90"
              title="새로고침(강제 재조회)"
            >
              새로고침
            </button>
          </div>
        </div>

        {err && (
          <div className="mt-4 rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800">
            {err}
          </div>
        )}
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm font-semibold">점수</div>
              <div className="mt-1 text-xs text-slate-500">overall_score</div>
            </div>
            <ScoreBadge score={data?.overall_score ?? null} />
          </div>

          <div className="mt-4 rounded-xl bg-slate-50 p-4 text-sm text-slate-700">
            {isProcessing
              ? "평가가 진행 중입니다. 잠시만 기다려주세요."
              : data?.status === "FAILED"
              ? `실패: ${data?.error_message ?? "원인 미상"}`
              : "피드백을 기반으로 답변을 개선해보세요."}
          </div>

          <div className="mt-4 flex flex-wrap gap-2">
            <Link
              to="/sessions"
              className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-medium hover:bg-slate-50"
            >
              다른 세션 보기
            </Link>
            <Link
              to="/sessions"
              className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-medium hover:bg-slate-50"
              title="세션 상세로 이동해 다시 답변 제출"
            >
              다시 도전하기
            </Link>
          </div>
        </div>

        <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <div className="text-sm font-semibold">피드백</div>
          <div className="mt-1 text-xs text-slate-500">feedback</div>

          <div className="mt-4 rounded-xl border border-slate-200 bg-white p-4">
            {isProcessing ? (
              <div className="text-sm text-slate-600">피드백 생성 중…</div>
            ) : data?.feedback ? (
              <pre className="whitespace-pre-wrap text-sm leading-6 text-slate-800">
                {data.feedback}
              </pre>
            ) : (
              <div className="text-sm text-slate-600">피드백이 없습니다.</div>
            )}
          </div>

          {data?.status === "FAILED" && data?.error_message && (
            <div className="mt-3 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
              {data.error_message}
            </div>
          )}
        </div>
      </div>

      <div className="text-xs text-slate-500">
        * status가 <b>done/failed</b>가 되면 폴링이 자동으로 중지됩니다.
      </div>
    </div>
  );
}