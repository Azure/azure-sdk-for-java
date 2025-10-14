// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.HashMap;
import java.util.Map;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.agents.persistent.TestUtils.size;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunsClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private PersistentAgentsAdministrationClient administrationClient;
    private ThreadsClient threadsClient;
    private RunsClient runsClient;
    private PersistentAgent agent;
    private PersistentAgentThread thread;

    private PersistentAgent createAgent(String agentName) {
        // Mimics agent creation as in other tests.
        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName(agentName).setInstructions("You are a helpful agent");
        PersistentAgent createdAgent = administrationClient.createAgent(options);
        assertNotNull(createdAgent, "Persistent agent should not be null");
        return createdAgent;
    }

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        threadsClient = agentsClient.getThreadsClient();
        runsClient = agentsClient.getRunsClient();
        agent = createAgent("TestAgent");
        thread = threadsClient.createThread();
    }

    private ThreadRun createRun() {
        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId());
        ThreadRun createdRun = runsClient.createRun(createRunOptions);
        assertNotNull(createdRun, "Created run should not be null");
        return createdRun;
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateAndRetrieveRun(HttpClient httpClient) {
        setup(httpClient);
        ThreadRun run = createRun();
        assertNotNull(run.getId(), "Run ID should not be null");

        ThreadRun retrievedRun = runsClient.getRun(thread.getId(), run.getId());
        assertNotNull(retrievedRun, "Retrieved run should not be null");
        assertEquals(run.getId(), retrievedRun.getId(), "Run IDs should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateRunWithMetadata(HttpClient httpClient) {
        setup(httpClient);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("priority", "high");

        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId()).setMetadata(metadata);
        ThreadRun run = runsClient.createRun(createRunOptions);
        assertNotNull(run, "Run with metadata should not be null");
        assertNotNull(run.getMetadata(), "Run metadata should not be null");
        assertEquals("high", run.getMetadata().get("priority"), "Metadata priority should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUpdateRun(HttpClient httpClient) {
        setup(httpClient);
        ThreadRun run = createRun();
        // ensure completed before updating
        waitForRunCompletion(run, runsClient);

        // Prepare metadata updates
        Map<String, String> updatedMetadata = new HashMap<>();
        updatedMetadata.put("updated", "true");
        updatedMetadata.put("timestamp", String.valueOf(System.currentTimeMillis()));

        ThreadRun updatedRun = runsClient.updateRun(thread.getId(), run.getId(), updatedMetadata);

        assertNotNull(updatedRun, "Updated run should not be null");
        assertNotNull(updatedRun.getMetadata(), "Updated metadata should not be null");
        assertEquals("true", updatedRun.getMetadata().get("updated"), "Updated flag should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListRunsDefault(HttpClient httpClient) {
        setup(httpClient);
        // Create multiple runs
        for (int i = 0; i < 2; i++) {
            CreateRunOptions runOptions = new CreateRunOptions(thread.getId(), agent.getId());
            ThreadRun run = runsClient.createRun(runOptions);
            waitForRunCompletion(run, runsClient);
        }

        PagedIterable<ThreadRun> runsList = runsClient.listRuns(thread.getId());
        assertNotNull(runsList, "Runs list should not be null");
        assertTrue(runsList.stream().count() >= 2, "There should be at least 2 runs");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListRunsWithParameters(HttpClient httpClient) {
        setup(httpClient);
        // Create several runs
        for (int i = 0; i < 5; i++) {
            CreateRunOptions runOptions = new CreateRunOptions(thread.getId(), agent.getId());
            ThreadRun run = runsClient.createRun(runOptions);
            waitForRunCompletion(run, runsClient);
        }

        PagedIterable<ThreadRun> runs = runsClient.listRuns(thread.getId(), 10,    // limit
            null,  // order
            null,  // after
            null   // before
        );

        assertNotNull(runs, "Filtered runs should not be null");
        assertTrue(size(runs) <= 5, "Run list should have at most 5 runs");
    }

    @AfterEach
    public void cleanup() {
        try {
            if (thread != null) {
                threadsClient.deleteThread(thread.getId());
            }
            if (agent != null) {
                administrationClient.deleteAgent(agent.getId());
            }
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }
}
