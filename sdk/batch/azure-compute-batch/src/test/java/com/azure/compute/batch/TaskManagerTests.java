// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.implementation.task.TaskManager;
import com.azure.compute.batch.models.BatchTaskBulkCreateOptions;
import com.azure.compute.batch.models.BatchTaskCreateParameters;
import com.azure.compute.batch.models.CreateTasksErrorException;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTests {

    private static List<BatchTaskCreateParameters> makeTasks(String prefix, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> new BatchTaskCreateParameters(prefix + i, "cmd /c echo " + i))
            .collect(Collectors.toList());
    }

    private static void runCreateTasks(FakeTaskSubmitter submitter, List<BatchTaskCreateParameters> tasks) {
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

        assertTrue(fake.getSubmissionCount("A") > 1, "A should have been retried at least once");
        assertEquals(1, fake.getSubmissionCount("B"), "B should submit once");
    }

    @Test
    public void successAfterOneServerErrorDidRetry() {
        Map<String, Integer> serverErrors = new HashMap<>();
        serverErrors.put("X", 1); // one failure then success

        FakeTaskSubmitter fake = new FakeTaskSubmitter(Collections.emptySet(), serverErrors, 0);
        List<BatchTaskCreateParameters> tasks = Collections.singletonList(new BatchTaskCreateParameters("X", "echo X"));

        assertDoesNotThrow(() -> runCreateTasks(fake, tasks));
        assertTrue(fake.getSubmissionCount("X") >= 2, "X should have been retried at least once");
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
        assertTrue(ex.failureTaskList().stream().anyMatch(r -> "C".equals(r.getTaskId())),
            "Client-error task C should be reported as failure");

        assertTrue(fake.getSubmissionCount("S") >= 1, "S should have been retried at least once");
        assertFalse(ex.failureTaskList().stream().anyMatch(r -> "S".equals(r.getTaskId())),
            "S should have eventually succeeded");

        // OK submitted once, succeeded.
        assertEquals(1, fake.getSubmissionCount("OK"));
    }
}
