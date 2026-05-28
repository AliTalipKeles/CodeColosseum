package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dtos.TestCaseDto;

@ExtendWith(MockitoExtension.class)
class JudgeServiceTest {

    @Spy
    JudgeService judgeService = new JudgeService();

    private TestCaseDto tc(String stdin, String expected) {
        TestCaseDto dto = new TestCaseDto();
        dto.setStdin(stdin);
        dto.setExpected_stdout(expected);
        return dto;
    }

    // ─── TC-SUB-01 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-SUB-01: All test cases accepted → ACCEPTED verdict, isWin()=true")
    void allAccepted_returnsWin() {
        doReturn("Accepted").when(judgeService).callJudge0(any(), anyInt(), any(), any()); //bypass judge0 call
        List<TestCaseDto> cases = List.of(tc("1 2", "3"), tc("3 4", "7"));

        SubmissionResult result = judgeService.evaluate("print(3)", "PYTHON", cases);

        assertEquals("ACCEPTED", result.verdict());
        assertEquals(2, result.testsPassed());
        assertTrue(result.isWin());
        verify(judgeService, times(2)).callJudge0(any(), anyInt(), any(), any());
    }

    // ─── TC-SUB-02 ───────────────────────────────────────────────────────────────
    // On the first failure evaluation stops immediately - Judge0 is not called again.
    @Test
    @DisplayName("TC-SUB-02: First test case fails with Wrong Answer → stops, 0 tests passed")
    void firstFails_stopsEarly() {
        doReturn("Wrong Answer").when(judgeService).callJudge0(any(), anyInt(), any(), any());
        List<TestCaseDto> cases = List.of(tc("1", "1"), tc("2", "2"));

        SubmissionResult result = judgeService.evaluate("print(0)", "PYTHON", cases);

        assertEquals("WRONG_ANSWER", result.verdict());
        assertEquals(0, result.testsPassed());
        assertFalse(result.isWin());
        verify(judgeService, times(1)).callJudge0(any(), anyInt(), any(), any());
    }

    // ─── TC-SUB-03 ───────────────────────────────────────────────────────────────
    // Verifies the bug-fix: "Time Limit Exceeded" must map to "TIME_LIMIT",
    // not "TIME_LIMIT_EXCEEDED" (the old code produced the latter, which fell
    // through to "RUNTIME_ERROR").
    @Test
    @DisplayName("TC-SUB-03: Time Limit Exceeded maps to TIME_LIMIT (not RUNTIME_ERROR)")
    void timeLimitExceeded_mapsCorrectly() {
        doReturn("Time Limit Exceeded").when(judgeService).callJudge0(any(), anyInt(), any(), any());
        List<TestCaseDto> cases = List.of(tc("big input", "0"));

        SubmissionResult result = judgeService.evaluate("while True: pass", "PYTHON", cases);

        assertEquals("TIME_LIMIT", result.verdict());
        assertFalse(result.isWin());
    }

    // ─── TC-SUB-04 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-SUB-04: Compilation Error maps to COMPILATION_ERROR")
    void compilationError_mapsCorrectly() {
        doReturn("Compilation Error").when(judgeService).callJudge0(any(), anyInt(), any(), any());
        List<TestCaseDto> cases = List.of(tc("1", "1"));

        SubmissionResult result = judgeService.evaluate("def broken(:", "PYTHON", cases);

        assertEquals("COMPILATION_ERROR", result.verdict());
        assertEquals(0, result.testsPassed());
    }

    // ─── TC-SUB-05 ───────────────────────────────────────────────────────────────
    // First test passes, second fails - testsPassed must reflect partial progress.
    @Test
    @DisplayName("TC-SUB-05: First test passes, second fails → 1 test passed, not a win")
    void partialPass_correctCount() {
        doReturn("Accepted").when(judgeService).callJudge0(any(), anyInt(), eq("1 2"), any());
        doReturn("Wrong Answer").when(judgeService).callJudge0(any(), anyInt(), eq("3 4"), any());
        List<TestCaseDto> cases = List.of(tc("1 2", "3"), tc("3 4", "7"));

        SubmissionResult result = judgeService.evaluate("print(int(input()))", "PYTHON", cases);

        assertEquals("WRONG_ANSWER", result.verdict());
        assertEquals(1, result.testsPassed());
        assertFalse(result.isWin());
        assertEquals(2, result.failedTestNumber());
    }
}
