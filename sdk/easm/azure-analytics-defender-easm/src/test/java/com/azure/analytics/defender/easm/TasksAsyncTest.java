// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.Task;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksAsyncTest extends EasmClientTestBase {
    String existingTaskId = "efad1fac-52d5-4ea9-b601-d5bf54a83780";
    String cancelTaskId = "efad1fac-52d5-4ea9-b601-d5bf54a83780";

    @Test
    public void testTasksListAsync() {
        PagedFlux<Task> taskPagedFlux = easmAsyncClient.listTask();
        StepVerifier.create(taskPagedFlux).assertNext(task -> {
            task.getId().matches(uuidRegex);
        });
    }

    @Test
    public void testTasksGetAsync() {
        Mono<Task> taskMono = easmAsyncClient.getTask(existingTaskId);
        StepVerifier.create(taskMono).assertNext(task -> {
            assertTrue(task.getId().equals(existingTaskId));
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testTasksCancelAsync() {
        Mono<Task> taskMono = easmAsyncClient.cancelTask(cancelTaskId);
        StepVerifier.create(taskMono).assertNext(task -> {
            assertTrue(task.getId().equals(existingTaskId));
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }
}
