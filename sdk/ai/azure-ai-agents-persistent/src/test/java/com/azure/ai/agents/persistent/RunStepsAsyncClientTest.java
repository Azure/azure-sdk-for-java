// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunStepsAsyncClientTest extends ClientTestBase {

    private PersistentAgentsAdministrationClientBuilder clientBuilder;
    private PersistentAgentsAdministrationAsyncClient agentsAsyncClient;
    private ThreadsAsyncClient threadsAsyncClient;
    private RunsAsyncClient runsAsyncClient;
    private RunStepsAsyncClient runStepsAsyncClient;
    private PersistentAgent agent;
    private PersistentAgentThread thread;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        agentsAsyncClient = clientBuilder.buildAsyncClient();
        threadsAsyncClient = clientBuilder.buildThreadsAsyncClient();
        runsAsyncClient = clientBuilder.buildRunsAsyncClient();
        runStepsAsyncClient = clientBuilder.buildRunStepsAsyncClient();
    }

    private void createTestAgent() {
        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName("TestAgent").setInstructions("You are a helpful agent");

        StepVerifier.create(agentsAsyncClient.createAgent(options)).assertNext(createdAgent -> {
            assertNotNull(createdAgent, "Persistent agent should not be null");
            agent = createdAgent;
            assertAgent(createdAgent);
        }).verifyComplete();
    }

    private void createTestThread() {
        StepVerifier.create(threadsAsyncClient.createThread()).assertNext(createdThread -> {
            assertNotNull(createdThread, "Created thread should not be null");
            thread = createdThread;
        }).verifyComplete();
    }

    private ThreadRun createAndWaitForRun() {
        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId());

        ThreadRun[] run = new ThreadRun[1];

        StepVerifier.create(runsAsyncClient.createRun(createRunOptions)).assertNext(createdRun -> {
            assertNotNull(createdRun, "Created run should not be null");
            run[0] = createdRun;
        }).verifyComplete();

        // Wait for run completion
        waitForRunCompletionAsync(run[0], runsAsyncClient);

        return run[0];
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListRunSteps(HttpClient httpClient) {
        setup(httpClient);
        createTestAgent();
        createTestThread();

        ThreadRun run = createAndWaitForRun();

        StepVerifier.create(runStepsAsyncClient.listRunSteps(run.getThreadId(), run.getId()).take(10).collectList())
            .assertNext(runSteps -> {
                assertNotNull(runSteps, "Run steps list should not be null");
                assertTrue(runSteps.size() > 0, "Run steps list should contain at least one step");
            })
            .verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetRunStep(HttpClient httpClient) {
        setup(httpClient);
        createTestAgent();
        createTestThread();

        ThreadRun run = createAndWaitForRun();

        // First get a list of run steps
        StepVerifier.create(runStepsAsyncClient.listRunSteps(run.getThreadId(), run.getId()).take(1).single())
            .assertNext(firstRunStep -> {
                assertNotNull(firstRunStep, "First run step should not be null");

                // Then retrieve a specific run step by ID
                StepVerifier
                    .create(runStepsAsyncClient.getRunStep(run.getThreadId(), run.getId(), firstRunStep.getId()))
                    .assertNext(retrievedRunStep -> {
                        assertNotNull(retrievedRunStep, "Retrieved run step should not be null");
                        assertEquals(firstRunStep.getId(), retrievedRunStep.getId(), "Run step IDs should match");
                    })
                    .verifyComplete();
            })
            .verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        if (thread != null) {
            StepVerifier.create(threadsAsyncClient.deleteThread(thread.getId())).assertNext(deletionStatus -> {
                assertNotNull(deletionStatus, "Thread deletion status should not be null");
                assertTrue(deletionStatus.isDeleted(), "Thread should be deleted");
            }).verifyComplete();
        }

        if (agent != null) {
            agentsAsyncClient.deleteAgent(agent.getId()).block();
        }
    }
}
