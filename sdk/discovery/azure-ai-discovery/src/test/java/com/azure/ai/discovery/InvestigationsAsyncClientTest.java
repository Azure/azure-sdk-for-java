// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.DiscoveryEngine;
import com.azure.ai.discovery.models.DiscoveryEngineUpdate;
import com.azure.ai.discovery.models.Investigation;
import com.azure.ai.discovery.models.PagedInvestigation;
import com.azure.ai.discovery.models.PagedWorkingMemoryEntry;
import com.azure.ai.discovery.models.Task;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link InvestigationsAsyncClient}, covering the investigation lifecycle and discovery-engine operations.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class InvestigationsAsyncClientTest extends DiscoveryClientTestBase {

    @Test
    @Order(1)
    public void testCreateOrReplace() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        Investigation investigation = client
            .createOrReplace(projectName, investigationName,
                new Investigation().setDisplayName("Test investigation").setDescription("Created by SDK tests"))
            .block();

        assertNotNull(investigation);
        assertNotNull(investigation.getName());
    }

    @Test
    @Order(3)
    public void testGet() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        Investigation investigation = client.get(projectName, investigationName).block();

        assertNotNull(investigation);
        assertNotNull(investigation.getName());
    }

    @Test
    @Order(2)
    public void testList() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        PagedInvestigation page = client.list(projectName).block();

        assertNotNull(page);
        assertNotNull(page.getValue());
        assertFalse(page.getValue().isEmpty());
    }

    @Test
    @Order(9)
    public void testUpdate() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        Investigation investigation
            = client.update(projectName, investigationName, new Investigation().setDescription("Updated by SDK tests"))
                .block();

        assertNotNull(investigation);
    }

    @Test
    @Order(5)
    public void testGetDiscoveryEngine() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        DiscoveryEngine engine = client.getDiscoveryEngine(projectName, investigationName).block();

        assertNotNull(engine);
    }

    @Test
    @Order(4)
    public void testUpdateDiscoveryEngine() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        DiscoveryEngine engine
            = client
                .updateDiscoveryEngine(projectName, investigationName,
                    new DiscoveryEngineUpdate().setSystemPrompt("You are a helpful discovery assistant."))
                .block();

        assertNotNull(engine);
    }

    @Test
    @Order(6)
    public void testStartDiscoveryEngine() {
        // The discovery engine requires at least one task in the investigation before it can start.
        TasksAsyncClient tasksClient = getTasksAsyncClient();
        Task task
            = tasksClient
                .create(projectName, investigationName,
                    new Task().setTitle("test-task").setDescription("Task for engine start test"))
                .block();
        try {
            InvestigationsAsyncClient client = getInvestigationsAsyncClient();
            DiscoveryEngine engine = client.startDiscoveryEngine(projectName, investigationName).block();
            assertNotNull(engine);
        } finally {
            tasksClient.delete(projectName, investigationName, task.getName()).block();
        }
    }

    @Test
    @Order(7)
    public void testGetDiscoveryEngineMemory() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        PagedWorkingMemoryEntry memory = client.getDiscoveryEngineMemory(projectName, investigationName).block();

        assertNotNull(memory);
        assertNotNull(memory.getValue());
    }

    @Test
    @Order(8)
    public void testStopDiscoveryEngine() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        DiscoveryEngine engine = client.stopDiscoveryEngine(projectName, investigationName).block();

        assertNotNull(engine);
    }

    @Test
    @Order(10)
    public void testBeginDelete() {
        InvestigationsAsyncClient client = getInvestigationsAsyncClient();
        String throwawayName = testResourceNamer.randomName("sdk-del-invst", 24);
        client
            .createOrReplace(projectName, throwawayName, new Investigation().setDisplayName("Investigation to delete"))
            .block();

        client.beginDelete(projectName, throwawayName).blockLast();

        PagedInvestigation page = client.list(projectName).block();
        boolean stillPresent
            = page.getValue().stream().anyMatch(investigation -> throwawayName.equals(investigation.getName()));
        assertFalse(stillPresent);
    }
}
