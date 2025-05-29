// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.RunStep;
import com.azure.ai.agents.persistent.models.ThreadDeletionStatus;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.agents.persistent.TestUtils.first;
import static com.azure.ai.agents.persistent.TestUtils.size;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunStepsClientTest extends ClientTestBase {

    private PersistentAgentsAdministrationClientBuilder clientBuilder;
    private PersistentAgentsAdministrationClient agentsClient;
    private ThreadsClient threadsClient;
    private RunsClient runsClient;
    private RunStepsClient runStepsClient;
    private PersistentAgent agent;
    private PersistentAgentThread thread;

    private PersistentAgent createAgent(String agentName) {
        // Mimics agent creation as in other tests.
        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName(agentName).setInstructions("You are a helpful agent");
        PersistentAgent createdAgent = agentsClient.createAgent(options);
        assertNotNull(createdAgent, "Persistent agent should not be null");
        return createdAgent;
    }

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        agentsClient = clientBuilder.buildClient();
        threadsClient = clientBuilder.buildThreadsClient();
        runsClient = clientBuilder.buildRunsClient();
        runStepsClient = clientBuilder.buildRunStepsClient();
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
    public void testListRunSteps(HttpClient httpClient) {
        setup(httpClient);

        ThreadRun run = createRun();
        waitForRunCompletion(run, runsClient);

        PagedIterable<RunStep> runSteps = runStepsClient.listRunSteps(run.getThreadId(), run.getId());
        assertNotNull(runSteps, "Run steps list should not be null");
        assertTrue(size(runSteps) > 0, "Run steps list should contain at least one step");
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetRunSteps(HttpClient httpClient) {
        setup(httpClient);

        ThreadRun run = createRun();
        waitForRunCompletion(run, runsClient);

        PagedIterable<RunStep> runSteps = runStepsClient.listRunSteps(run.getThreadId(), run.getId());
        assertNotNull(runSteps, "Run steps response should not be null");
        assertTrue(size(runSteps) > 0, "Run steps list should contain at least one step");

        RunStep firstRunStep = first(runSteps);
        RunStep retrievedRunStep = runStepsClient.getRunStep(run.getThreadId(), run.getId(), firstRunStep.getId());
        assertNotNull(retrievedRunStep, "Retrieved run step should not be null");
        assertEquals(firstRunStep.getId(), retrievedRunStep.getId(), "Run step IDs should match");
    }

    @AfterEach
    public void cleanup() {
        if (thread != null) {
            ThreadDeletionStatus deletionStatus = threadsClient.deleteThread(thread.getId());
            assertNotNull(deletionStatus, "Thread deletion status should not be null");
            assertTrue(deletionStatus.isDeleted(), "Thread should be deleted");
        }
        if (agent != null) {
            agentsClient.deleteAgent(agent.getId());
        }
    }
}
