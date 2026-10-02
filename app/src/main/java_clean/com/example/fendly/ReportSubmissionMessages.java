package com.example.fendly;

public final class ReportSubmissionMessages {
    private ReportSubmissionMessages() {
    }

    public static String buildSubmissionSuccessMessage(boolean paidLostReport, String errorMessage, String matchError) {
        if (paidLostReport) {
            return "Payment Successful. Report submitted.";
        }
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            return "Report saved without image: " + errorMessage;
        }
        return "Report saved securely";
    }
}
