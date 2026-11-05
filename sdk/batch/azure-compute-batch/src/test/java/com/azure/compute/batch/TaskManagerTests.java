// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.implementation.task.TaskManager;
import com.azure.compute.batch.implementation.task.TaskSubmitter;
import com.azure.compute.batch.models.BatchTaskBulkCreateOptions;
import com.azure.compute.batch.models.BatchTaskCreateParameters;
import com.azure.compute.batch.models.CreateTasksErrorException;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class TaskManagerTests {

    private static List<BatchTaskCreateParameters> makeTasks(String prefix, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> new BatchTaskCreateParameters(prefix + i, "cmd /c echo " + i))
            .collect(Collectors.toList());
    }

    private static void runCreateTasks(TaskSubmitter submitter, List<BatchTaskCreateParameters> tasks) {
        TaskManager.createTasks(submitter, "job-1", tasks, new BatchTaskBulkCreateOptions().setMaxConcurrency(4));
    }

    @Test
    public void serverErrorsAreRetriedUntilSuccessNoCap() {
        // A fails 10 times, then succeeds; B succeeds immediately.
        Map<String, Integer> serverErrors = new HashMap<>();
        serverErrors.put("A", 10);

        FakeTaskSubmitter fake = new FakeTaskSubmitter(Collections.emptySet(), serverErrors, 0);
        List<BatchTaskCreateParameters> tasks
            = Arrays.asList(new BatchTaskCreateParameters("A", "echo A"), new BatchTaskCreateParameters("B", "echo B"));

        assertDoesNotThrow(() -> runCreateTasks(fake, tasks));

        assertEquals(11, fake.getSubmissionCount("A"), "A should succeed after ten retries");
        assertEquals(1, fake.getSubmissionCount("B"), "B should submit once");
    }

    @RepeatedTest(1000)
    public void successAfterOneServerErrorDidRetry() {
        Map<String, Integer> serverErrors = new HashMap<>();
        serverErrors.put("X", 1); // one failure then success

        FakeTaskSubmitter fake = new FakeTaskSubmitter(Collections.emptySet(), serverErrors, 0);
        List<BatchTaskCreateParameters> tasks = Collections.singletonList(new BatchTaskCreateParameters("X", "echo X"));

        assertDoesNotThrow(() -> runCreateTasks(fake, tasks));
        assertEquals(2, fake.getSubmissionCount("X"), "X should succeed after one retry");
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 4 })
    public void retriesAcrossMultipleChunks(int maxConcurrency) {
        List<BatchTaskCreateParameters> tasks = makeTasks("T", 201);
        Map<String, Integer> serverErrors = new HashMap<>();
        tasks.forEach(task -> serverErrors.put(task.getId(), 1));
        FakeTaskSubmitter fake = new FakeTaskSubmitter(Collections.emptySet(), serverErrors, 0);

        assertDoesNotThrow(() -> TaskManager.createTasks(fake, "job-1", tasks,
            new BatchTaskBulkCreateOptions().setMaxConcurrency(maxConcurrency)));

        tasks.forEach(task -> assertEquals(2, fake.getSubmissionCount(task.getId()),
            task.getId() + " should succeed after one retry"));
    }

    @RepeatedTest(100)
    public void submissionExceptionIsPropagated() {
        RuntimeException expected = new IllegalStateException("Submission failed");
        TaskSubmitter submitter = (jobId, taskCollection) -> {
            throw expected;
        };

        assertSame(expected, assertThrows(IllegalStateException.class,
            () -> runCreateTasks(submitter, Collections.singletonList(new BatchTaskCreateParameters("X", "echo X")))));
    }

    @Test
    public void checkedSubmissionExceptionIsPropagated() {
        IOException expected = new IOException("Submission failed");
        TaskSubmitter submitter = (jobId, taskCollection) -> {
            throw expected;
        };

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> runCreateTasks(submitter, Collections.singletonList(new BatchTaskCreateParameters("X", "echo X"))));
        assertSame(expected, actual.getCause());
    }

    @Test
    public void splitOn413AndShrinkChunkSize() {
        FakeTaskSubmitter fake = new FakeTaskSubmitter(Collections.emptySet(), Collections.emptyMap(), 50);
        List<BatchTaskCreateParameters> tasks = makeTasks("T", 110);

        assertDoesNotThrow(() -> runCreateTasks(fake, tasks));
        assertTrue(fake.getMaxObservedSuccessfulGroupSize() <= 50,
            "Successful group size should be <= 50 after split logic");
    }

    @Test
    public void mixedClientAndServerErrorsBehaveAsSpecified() {
        Set<String> clientErrors = new HashSet<>(Collections.singletonList("C"));
        Map<String, Integer> serverErrors = new HashMap<>();
        serverErrors.put("S", 1); // one retry then success

        FakeTaskSubmitter fake = new FakeTaskSubmitter(clientErrors, serverErrors, 0);
        List<BatchTaskCreateParameters> tasks = Arrays.asList(new BatchTaskCreateParameters("C", "echo C"),
            new BatchTaskCreateParameters("S", "echo S"), new BatchTaskCreateParameters("OK", "echo OK"));

        CreateTasksErrorException ex = assertThrows(CreateTasksErrorException.class, () -> runCreateTasks(fake, tasks));

        // Client error task must be in failures.
        assertTrue(ex.getFailureTaskList().stream().anyMatch(r -> "C".equals(r.getTaskId())),
            "Client-error task C should be reported as failure");

        assertTrue(fake.getSubmissionCount("S") >= 1, "S should have been retried at least once");
        assertFalse(ex.getFailureTaskList().stream().anyMatch(r -> "S".equals(r.getTaskId())),
            "S should have eventually succeeded");

        // OK submitted once, succeeded.
        assertEquals(1, fake.getSubmissionCount("OK"));
    }
}
