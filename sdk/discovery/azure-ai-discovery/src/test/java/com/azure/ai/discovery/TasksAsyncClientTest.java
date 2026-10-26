// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.ByType;
import com.azure.ai.discovery.models.ExecutionHistoryEntry;
import com.azure.ai.discovery.models.Task;
import com.azure.ai.discovery.models.TaskAssignee;
import com.azure.ai.discovery.models.TaskComment;
import com.azure.ai.discovery.models.TaskPriority;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TasksAsyncClient}, covering create, get, list, update, start, addComment,
 * addExecutionHistory, and delete.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class TasksAsyncClientTest extends DiscoveryClientTestBase {

    private Task createTask(TasksAsyncClient client, String title) {
        return client
            .create(projectName, investigationName,
                new Task().setTitle(title)
                    .setDescription("Created by SDK tests")
                    .setPriority(TaskPriority.MEDIUM)
                    .setAssignedTo(new TaskAssignee().setId(agentName).setType(ByType.APPLICATION)))
            .block();
    }

    @Test
    @Order(2)
    public void testCreate() {
        TasksAsyncClient client = getTasksAsyncClient();
        Task task = createTask(client, "Test task");
        try {
            assertNotNull(task);
            assertNotNull(task.getName());
            assertEquals("Test task", task.getTitle());
        } finally {
            client.delete(projectName, investigationName, task.getName()).block();
        }
    }

    @Test
    @Order(3)
    public void testGet() {
        TasksAsyncClient client = getTasksAsyncClient();
        Task created = createTask(client, "Task for get");
        try {
            Task task = client.get(projectName, investigationName, created.getName()).block();
            assertNotNull(task);
            assertEquals(created.getName(), task.getName());
        } finally {
            client.delete(projectName, investigationName, created.getName()).block();
        }
    }

    @Test
    @Order(1)
    public void testList() {
        TasksAsyncClient client = getTasksAsyncClient();
        Task created = createTask(client, "Task for list");
        try {
            List<Task> tasks = client.list(projectName, investigationName).collectList().block();
            assertNotNull(tasks);
            assertTrue(tasks.stream().anyMatch(task -> created.getName().equals(task.getName())));
        } finally {
            client.delete(projectName, investigationName, created.getName()).block();
        }
    }

    @Test
    @Order(4)
    public void testStableUpdate() {
        TasksAsyncClient client = getTasksAsyncClient();
        Task created = createTask(client, "Task before update");
        try {
            Task updated
                = client
                    .update(projectName, investigationName, created.getName(),
                        new Task().setDescription("Updated by SDK tests"))
                    .block();
            assertNotNull(updated);
            assertEquals(created.getName(), updated.getName());
        } finally {
            client.delete(projectName, investigationName, created.getName()).block();
        }
    }

    @Test
    @Order(6)
    public void testStart() {
        TasksAsyncClient client = getTasksAsyncClient();
        Task created = createTask(client, "Task to start");
        try {
            Task started = client.start(projectName, investigationName, created.getName()).block();
            assertNotNull(started);
        } finally {
            client.delete(projectName, investigationName, created.getName()).block();
        }
    }

    @Test
    @Order(7)
    public void testAddComment() {
        TasksAsyncClient client = getTasksAsyncClient();
        Task created = createTask(client, "Task for comment");
        try {
            Task task
                = client
                    .addComment(projectName, investigationName, created.getName(),
                        new TaskComment().setText("A comment from the SDK tests"))
                    .block();
            assertNotNull(task);
        } finally {
            client.delete(projectName, investigationName, created.getName()).block();
        }
    }

    @Test
    @Order(8)
    public void testAddExecutionHistory() {
        TasksAsyncClient client = getTasksAsyncClient();
        Task created = createTask(client, "Task for execution history");
        try {
            Task task = client.addExecutionHistory(projectName, investigationName, created.getName(),
                new ExecutionHistoryEntry().setCreatedAt(OffsetDateTime.of(2026, 4, 8, 21, 0, 0, 0, ZoneOffset.UTC))
                    .setAction("completed")
                    .setCreatedBy(agentName)
                    .setCreatedByType(ByType.APPLICATION)
                    .setSummary("Task execution completed"))
                .block();
            assertNotNull(task);
        } finally {
            client.delete(projectName, investigationName, created.getName()).block();
        }
    }

    @Test
    @Order(5)
    public void testDelete() {
        TasksAsyncClient client = getTasksAsyncClient();
        Task created = createTask(client, "Task to delete");
        client.delete(projectName, investigationName, created.getName()).block();

        List<Task> tasks = client.list(projectName, investigationName).collectList().block();
        assertTrue(tasks.stream().noneMatch(task -> created.getName().equals(task.getName())));
    }
}
