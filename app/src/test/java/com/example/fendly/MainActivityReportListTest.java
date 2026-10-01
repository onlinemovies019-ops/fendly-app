package com.example.fendly;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

public class MainActivityReportListTest {
    @Test
    public void parseReportsFromBackendJson_sortsNewestFirst() throws Exception {
        String jsonPayload = "["
                + "{\"id\":\"2\",\"type\":\"FOUND\",\"title\":\"Found earphones\",\"description\":\"Near station\",\"report_location\":\"Station\",\"report_date\":\"2026-09-11\",\"image_url\":\"https://example.com/found.jpg\",\"created_at\":\"2026-09-11T08:00:00Z\",\"edit_count\":0},"
                + "{\"id\":\"1\",\"type\":\"LOST\",\"title\":\"Lost keys\",\"description\":\"Lost near bus stop\",\"report_location\":\"Bus stop\",\"report_date\":\"2026-09-10\",\"image_url\":\"https://example.com/lost.jpg\",\"created_at\":\"2026-09-10T08:15:00Z\",\"edit_count\":1}"
                + "]";

        List<JSONObject> reports = ReportParser.parseReportsFromBackendJson(jsonPayload);

        assertEquals(2, reports.size());
        assertEquals("FOUND", reports.get(0).optString("type"));
        assertEquals("Found earphones", reports.get(0).optString("title"));
        assertEquals("LOST", reports.get(1).optString("type"));
    }
}
