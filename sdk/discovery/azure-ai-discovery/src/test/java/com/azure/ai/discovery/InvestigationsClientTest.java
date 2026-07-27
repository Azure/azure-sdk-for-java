// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.DiscoveryEngine;
import com.azure.ai.discovery.models.DiscoveryEngineUpdate;
import com.azure.ai.discovery.models.Investigation;
import com.azure.ai.discovery.models.PagedInvestigation;
import com.azure.ai.discovery.models.PagedWorkingMemoryEntry;
import com.azure.ai.discovery.models.Task;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link InvestigationsClient}, covering the investigation lifecycle and the associated discovery-engine
 * operations.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class InvestigationsClientTest extends DiscoveryClientTestBase {

    @Test
    @Order(1)
    public void testCreateOrReplace() {
        InvestigationsClient client = getInvestigationsClient();
        Investigation investigation = client.createOrReplace(projectName, investigationName,
            new Investigation().setDisplayName("Test investigation").setDescription("Created by SDK tests"));

        assertNotNull(investigation);
        assertNotNull(investigation.getName());
    }

    @Test
    @Order(3)
    public void testGet() {
        InvestigationsClient client = getInvestigationsClient();
        Investigation investigation = client.get(projectName, investigationName);

        assertNotNull(investigation);
        assertNotNull(investigation.getName());
    }

    @Test
    @Order(2)
    public void testList() {
        InvestigationsClient client = getInvestigationsClient();
        PagedInvestigation page = client.list(projectName);

        assertNotNull(page.getValue());
        assertFalse(page.getValue().isEmpty());
    }

    @Test
    @Order(9)
    public void testUpdate() {
        InvestigationsClient client = getInvestigationsClient();
        Investigation investigation
            = client.update(projectName, investigationName, new Investigation().setDescription("Updated by SDK tests"));

        assertNotNull(investigation);
    }

    @Test
    @Order(5)
    public void testGetDiscoveryEngine() {
        InvestigationsClient client = getInvestigationsClient();
        DiscoveryEngine engine = client.getDiscoveryEngine(projectName, investigationName);

        assertNotNull(engine);
    }

    @Test
    @Order(4)
    public void testUpdateDiscoveryEngine() {
        InvestigationsClient client = getInvestigationsClient();
        DiscoveryEngine engine = client.updateDiscoveryEngine(projectName, investigationName,
            new DiscoveryEngineUpdate().setSystemPrompt("You are a helpful discovery assistant."));

        assertNotNull(engine);
    }

    @Test
    @Order(6)
    public void testStartDiscoveryEngine() {
        // The discovery engine requires at least one task in the investigation before it can start.
        TasksClient tasksClient = getTasksClient();
        Task task = tasksClient.create(projectName, investigationName,
            new Task().setTitle("test-task").setDescription("Task for engine start test"));
        try {
            InvestigationsClient client = getInvestigationsClient();
            DiscoveryEngine engine = client.startDiscoveryEngine(projectName, investigationName);
            assertNotNull(engine);
        } finally {
            tasksClient.delete(projectName, investigationName, task.getName());
        }
    }

    @Test
    @Order(7)
    public void testGetDiscoveryEngineMemory() {
        InvestigationsClient client = getInvestigationsClient();
        PagedWorkingMemoryEntry memory = client.getDiscoveryEngineMemory(projectName, investigationName);

        assertNotNull(memory);
        assertNotNull(memory.getValue());
    }

    @Test
    @Order(8)
    public void testStopDiscoveryEngine() {
        InvestigationsClient client = getInvestigationsClient();
        DiscoveryEngine engine = client.stopDiscoveryEngine(projectName, investigationName);

        assertNotNull(engine);
    }

    @Test
    @Order(10)
    public void testBeginDelete() {
        InvestigationsClient client = getInvestigationsClient();
        // Create a throwaway investigation so the shared fixture is not removed.
        String throwawayName = testResourceNamer.randomName("sdk-del-invst", 24);
        client.createOrReplace(projectName, throwawayName,
            new Investigation().setDisplayName("Investigation to delete"));

        SyncPoller<PollOperationDetails, Void> poller = client.beginDelete(projectName, throwawayName);
        poller.waitForCompletion();

        PagedInvestigation page = client.list(projectName);
        boolean stillPresent
            = page.getValue().stream().anyMatch(investigation -> throwawayName.equals(investigation.getName()));
        assertFalse(stillPresent);
    }
}
