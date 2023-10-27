// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.Task;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksTest extends EasmClientTestBase {

    String existingTaskId = "efad1fac-52d5-4ea9-b601-d5bf54a83780";
    String cancelTaskId = "efad1fac-52d5-4ea9-b601-d5bf54a83780";

    @Test
    public void testtasksListWithResponse() {
        PagedIterable<Task> taskPageResponse = easmClient.listTask();
        Task taskResponse = taskPageResponse.stream().iterator().next();
        assertTrue(taskResponse.getId().matches(uuidRegex));

    }

    @Test
    public void testtasksGetWithResponse() {
        Task taskResponse = easmClient.getTask(existingTaskId);
        assertTrue(taskResponse.getId().matches(uuidRegex));
    }

    @Test
    public void testtasksCancelWithResponse() {
        Task taskResponse = easmClient.cancelTask(cancelTaskId);
        assertTrue(taskResponse.getId().matches(uuidRegex));
    }

}
