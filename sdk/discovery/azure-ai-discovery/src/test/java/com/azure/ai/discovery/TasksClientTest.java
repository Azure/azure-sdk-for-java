// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.ByType;
import com.azure.ai.discovery.models.ExecutionHistoryEntry;
import com.azure.ai.discovery.models.Task;
import com.azure.ai.discovery.models.TaskAssignee;
import com.azure.ai.discovery.models.TaskComment;
import com.azure.ai.discovery.models.TaskPriority;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TasksClient}, covering create, get, list (with and without filter), update, start,
 * addComment, addExecutionHistory, and delete.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class TasksClientTest extends DiscoveryClientTestBase {

    private Task createTask(TasksClient client, String title) {
        return client.create(projectName, investigationName,
            new Task().setTitle(title)
                .setDescription("Created by SDK tests")
                .setPriority(TaskPriority.MEDIUM)
                .setAssignedTo(new TaskAssignee().setId(agentName).setType(ByType.APPLICATION)));
    }

    @Test
    @Order(2)
    public void testCreate() {
        TasksClient client = getTasksClient();
        Task task = createTask(client, "Test task");
        try {
            assertNotNull(task);
            assertNotNull(task.getName());
            assertEquals("Test task", task.getTitle());
        } finally {
            client.delete(projectName, investigationName, task.getName());
        }
    }

    @Test
    @Order(3)
    public void testGet() {
        TasksClient client = getTasksClient();
        Task created = createTask(client, "Task for get");
        try {
            Task task = client.get(projectName, investigationName, created.getName());
            assertNotNull(task);
            assertEquals(created.getName(), task.getName());
        } finally {
            client.delete(projectName, investigationName, created.getName());
        }
    }

    @Test
    @Order(1)
    public void testList() {
        TasksClient client = getTasksClient();
        Task created = createTask(client, "Task for list");
        try {
            PagedIterable<Task> tasks = client.list(projectName, investigationName);
            assertTrue(tasks.stream().anyMatch(task -> created.getName().equals(task.getName())));
        } finally {
            client.delete(projectName, investigationName, created.getName());
        }
    }

    @Test
    @Order(4)
    public void testStableUpdate() {
        TasksClient client = getTasksClient();
        Task created = createTask(client, "Task before update");
        try {
            Task updated = client.update(projectName, investigationName, created.getName(),
                new Task().setDescription("Updated by SDK tests"));
            assertNotNull(updated);
            assertEquals(created.getName(), updated.getName());
        } finally {
            client.delete(projectName, investigationName, created.getName());
        }
    }

    @Test
    @Order(6)
    public void testStart() {
        TasksClient client = getTasksClient();
        Task created = createTask(client, "Task to start");
        try {
            Task started = client.start(projectName, investigationName, created.getName());
            assertNotNull(started);
        } finally {
            client.delete(projectName, investigationName, created.getName());
        }
    }

    @Test
    @Order(7)
    public void testAddComment() {
        TasksClient client = getTasksClient();
        Task created = createTask(client, "Task for comment");
        try {
            Task task = client.addComment(projectName, investigationName, created.getName(),
                new TaskComment().setText("A comment from the SDK tests"));
            assertNotNull(task);
        } finally {
            client.delete(projectName, investigationName, created.getName());
        }
    }

    @Test
    @Order(8)
    public void testAddExecutionHistory() {
        TasksClient client = getTasksClient();
        Task created = createTask(client, "Task for execution history");
        try {
            Task task = client.addExecutionHistory(projectName, investigationName, created.getName(),
                new ExecutionHistoryEntry().setCreatedAt(OffsetDateTime.of(2026, 4, 8, 21, 0, 0, 0, ZoneOffset.UTC))
                    .setAction("completed")
                    .setCreatedBy(agentName)
                    .setCreatedByType(ByType.APPLICATION)
                    .setSummary("Task execution completed"));
            assertNotNull(task);
        } finally {
            client.delete(projectName, investigationName, created.getName());
        }
    }

    @Test
    @Order(5)
    public void testDelete() {
        TasksClient client = getTasksClient();
        Task created = createTask(client, "Task to delete");
        client.delete(projectName, investigationName, created.getName());

        PagedIterable<Task> tasks = client.list(projectName, investigationName);
        assertTrue(tasks.stream().noneMatch(task -> created.getName().equals(task.getName())));
    }
}
