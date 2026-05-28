package com.example.demo.services;

public record SubmissionResult(
        String verdict,
        int testsPassed,
        int totalTests,
        String rawStatusDescription,
        int failedTestNumber,
        String failedStdout,
        String failedExpectedStdout) {

    public boolean isWin() {
        return testsPassed == totalTests && totalTests > 0;
    }

    public static SubmissionResult win(int total) {
        return new SubmissionResult("ACCEPTED", total, total, null, 0, null, null);
    }

    public static SubmissionResult failure(String verdict, int testsPassed, int total,
            String rawStatus, int failedNum, String stdout, String expected) {
        return new SubmissionResult(verdict, testsPassed, total, rawStatus, failedNum, stdout, expected);
    }
}
