package com.example.fendly;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportParser {
    public static List<JSONObject> parseReportsFromBackendJson(String responseBody) throws Exception {
        List<JSONObject> reports = new ArrayList<>();
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return reports;
        }

        JSONArray response = new JSONArray(responseBody);
        int length = response.length();
        int i = 0;
        while (i < length) {
            JSONObject item = response.optJSONObject(i);
            if (item != null) {
                reports.add(item);
            }
            i++;
        }

        Collections.sort(reports, (a, b) -> Long.compare(parseReportCreatedAtMillis(b), parseReportCreatedAtMillis(a)));
        return reports;
    }

    public static long parseReportCreatedAtMillis(JSONObject report) {
        if (report == null) return 0L;
        Object createdAt = report.opt("created_at");
        if (createdAt instanceof Number number) {
            return number.longValue();
        }
        if (createdAt instanceof String strVal) {
            String value = strVal.trim();
            if (value.isEmpty()) return 0L;

            String[] patterns = {
                    "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                    "yyyy-MM-dd'T'HH:mm:ssX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd"
            };
            for (String pattern : patterns) {
                try {
                    Date parsedDate = new SimpleDateFormat(pattern, Locale.US).parse(value);
                    if (parsedDate != null) return parsedDate.getTime();
                } catch (Exception ignored) {
                }
            }
            try {
                return Long.parseLong(value);
            } catch (Exception ignored) {
            }
        }
        return 0L;
    }
}
