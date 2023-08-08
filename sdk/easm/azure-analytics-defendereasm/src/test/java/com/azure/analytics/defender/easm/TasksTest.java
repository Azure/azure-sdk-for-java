package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmDefenderClientTestBase;
import com.azure.analytics.defender.easm.models.Task;
import com.azure.analytics.defender.easm.models.TaskPageResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksTest extends EasmDefenderClientTestBase {

    String existingTaskId = "62ccdc21-a3d8-434e-8f3d-fc08c7e45796";
    String cancelTaskId = "62ccdc21-a3d8-434e-8f3d-fc08c7e45796";

    @Test
    public void testtasksListWithResponse(){
        TaskPageResponse taskPageResponse = tasksClient.list();
        Task taskResponse = taskPageResponse.getValue().get(0);
        assertTrue(taskResponse.getId().matches(UUID_REGEX));
    }

    @Test
    public void testtasksGetWithResponse(){
        Task taskResponse = tasksClient.get(existingTaskId);
        assertTrue(taskResponse.getId().matches(UUID_REGEX));
    }

    @Test
    public void testtasksCancelWithResponse(){
        Task taskResponse = tasksClient.cancel(cancelTaskId);
        assertTrue(taskResponse.getId().matches(UUID_REGEX));
    }

}
