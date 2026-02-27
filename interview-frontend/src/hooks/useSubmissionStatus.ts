import { useEffect, useRef, useState } from "react";
import { api } from "../lib/api";

type StatusRes = {
  submission_id: number;
  status: "PENDING" | "PROCESSING" | "DONE" | "FAILED" | string;
  error_message: string | null;
  overall_score: number | null;
  feedback: string | null;
};

export function useSubmissionStatus(submissionId: number | null, intervalMs = 1200) {
  const [data, setData] = useState<StatusRes | null>(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const timer = useRef<number | null>(null);

  useEffect(() => {
    if (!submissionId) return;

    let cancelled = false;

    async function fetchOnce() {
      setLoading(true);
      setErr(null);

      try {
        const json = (await api(`/api/submissions/${submissionId}/status`)) as StatusRes;

        if (!cancelled) setData(json);

        const st = (json.status || "").toLowerCase();
        if (st === "DONE" || st === "FAILED") {
          if (timer.current) window.clearInterval(timer.current);
          timer.current = null;
        }
      } catch (e: any) {
        if (!cancelled) setErr(e?.message ?? "Unknown error");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    fetchOnce();
    timer.current = window.setInterval(fetchOnce, intervalMs);

    return () => {
      cancelled = true;
      if (timer.current) window.clearInterval(timer.current);
      timer.current = null;
    };
  }, [submissionId, intervalMs]);

  return { data, loading, err };
}