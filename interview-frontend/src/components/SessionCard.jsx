import { ChevronRight, Trash2 } from "lucide-react";
import { cn } from "../lib/cn";

export const SESSION_STATUS = /** @type {const} */ (["PENDING", "PROCESSING", "DONE", "FAILED"]);

const statusConfig = {
  PENDING: { label: "Pending", className: "border-[#8C8578]/30 text-[#8C8578]" },
  PROCESSING: { label: "Processing", className: "border-[#c9a043]/40 text-[#c9a043]" },
  DONE: { label: "Done", className: "border-[#5a8a5e]/30 text-[#5a8a5e]" },
  FAILED: { label: "Failed", className: "border-[#c44a3f]/30 text-[#c44a3f]" },
};

function StatusPill({ status }) {
  const cfg = statusConfig[status] ?? statusConfig.PENDING;
  return (
    <span className={cn("inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium tracking-wide", cfg.className)}>
      {cfg.label}
    </span>
  );
}

function ScoreBadge({ score }) {
  return (
    <span className="inline-flex items-center rounded-lg bg-[#EDE8DE] px-2 py-0.5 font-mono text-xs font-semibold text-[#1a1a1a] tabular-nums">
      {score}
      <span className="text-[#8C8578]">/100</span>
    </span>
  );
}

function formatDate(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

export function SessionCard({ session, onClick, onDelete, deleting }) {
  const dateText = formatDate(session.createdAt);

  return (
    <button
      type="button"
      onClick={onClick}
      className="group flex w-full items-center gap-4 rounded-xl border border-[#E2DDD3] bg-white p-4 text-left shadow-[0_1px_3px_0_rgba(0,0,0,0.04)] transition-all duration-200 hover:-translate-y-0.5 hover:shadow-[0_4px_12px_0_rgba(0,0,0,0.06)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#B5AE9E] sm:p-5"
      aria-label={`View session: ${session.title}`}
    >
      <div className="flex min-w-0 flex-1 flex-col gap-1.5 sm:flex-row sm:items-center sm:gap-4">
        <div className="min-w-0 flex-1">
          <h3 className="truncate text-sm font-medium text-[#1a1a1a] sm:text-[15px]">
            {session.title}
          </h3>
          <div className="mt-1 flex items-center gap-2">
            <time className="text-xs text-[#8C8578]">{dateText}</time>
          </div>
        </div>

        <div className="flex items-center gap-2.5">
          <StatusPill status={session.status} />

          {/* DONE이면 점수 반드시 보이게 하고 싶다면:
              백엔드에서 DONE인데 overallScore null인 경우를 막거나
              여기서 "-"로 표시하는 방식도 가능 */}
          {session.overallScore != null && <ScoreBadge score={session.overallScore} />}

          {onDelete && (
            <span
              role="button"
              tabIndex={0}
              aria-label="Delete session"
              title="Delete"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                if (!deleting) onDelete();
              }}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  e.preventDefault();
                  e.stopPropagation();
                  if (!deleting) onDelete();
                }
              }}
              className={cn(
                "inline-flex items-center justify-center rounded-md p-2 text-[#8C8578] transition",
                "hover:bg-[#EDE8DE] hover:text-[#1a1a1a]",
                deleting && "pointer-events-none opacity-40"
              )}
            >
              <Trash2 className="h-4 w-4" />
            </span>
          )}
        </div>
      </div>

      <ChevronRight
        className="size-4 shrink-0 text-[#8C8578]/60 transition-transform duration-200 group-hover:translate-x-0.5 group-hover:text-[#8C8578]"
        aria-hidden="true"
      />
    </button>
  );
}
