package com.portfolio.interview.api;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    // strengths_json 같은 ["a","b"] 형태만 파싱하는 초간단 버전 (MVP용)
    public static List<String> parseStringArray(String json) {
        List<String> out = new ArrayList<>();
        if (json == null) return out;
        String s = json.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) return out;
        s = s.substring(1, s.length()-1).trim();
        if (s.isEmpty()) return out;

        // 매우 단순 split (문장에 콤마 많아지면 jackson으로 교체 추천)
        String[] parts = s.split("\",\"");
        for (String p : parts) {
            String v = p.replace("\"", "").trim();
            if (!v.isEmpty()) out.add(v);
        }
        return out;
    }
}
