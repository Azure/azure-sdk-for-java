package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmClientTestBase;
import com.azure.analytics.defender.easm.models.Task;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksAsyncTest extends EasmClientTestBase {
    String existingTaskId = "efad1fac-52d5-4ea9-b601-d5bf54a83780";
    String cancelTaskId = "efad1fac-52d5-4ea9-b601-d5bf54a83780";

    @Test
    public void testTasksListAsync() {
        PagedFlux<Task> taskPagedFlux = easmAsyncClient.listTask();
        Task task = taskPagedFlux.blockFirst();
        assertTrue(task.getId().matches(uuidRegex));
    }

    @Test
    public void testTasksGetAsync() {
        Mono<Task> taskMono = easmAsyncClient.getTask(existingTaskId);
        taskMono.subscribe(
          task -> {
              assertTrue(task.getId().equals(existingTaskId));
          }
        );
    }

    @Test
    public void testTasksCancelAsync() {
        Mono<Task> taskMono = easmAsyncClient.cancelTask(cancelTaskId);
        taskMono.subscribe(
            task -> {
                assertTrue(task.getId().equals(existingTaskId));
            }
        );
    }
}
