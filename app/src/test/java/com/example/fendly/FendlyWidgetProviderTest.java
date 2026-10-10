package com.example.fendly;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.junit.Test;

public class FendlyWidgetProviderTest {
    @Test
    public void countReportTypes_countsOnlyLostAndFoundReportsCaseInsensitively() throws Exception {
        int[] counts = FendlyWidgetProvider.countReportTypes(
                "[{\"type\":\"LOST\"},{\"type\":\"found\"},{\"type\":\"other\"},{\"title\":\"missing type\"}]");

        assertArrayEquals(new int[]{1, 1}, counts);
    }

    @Test
    public void readUnreadCount_returnsUnreadNotificationCount() throws Exception {
        assertEquals(3, FendlyWidgetProvider.readUnreadCount("{\"unread_count\":3}"));
    }

    @Test(expected = JSONException.class)
    public void readUnreadCount_rejectsMissingCount() throws Exception {
        FendlyWidgetProvider.readUnreadCount("{\"notifications\":[]}");
    }
}
