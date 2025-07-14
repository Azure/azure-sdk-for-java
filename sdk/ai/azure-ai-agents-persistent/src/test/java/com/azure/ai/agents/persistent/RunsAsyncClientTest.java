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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunsAsyncClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private PersistentAgentsAdministrationAsyncClient administrationAsyncClient;
    private ThreadsAsyncClient threadsAsyncClient;
    private RunsAsyncClient runsAsyncClient;
    private PersistentAgent agent;
    private PersistentAgentThread thread;

    private void createTestAgent(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName("TestAgent").setInstructions("You are a helpful agent");

        StepVerifier.create(administrationAsyncClient.createAgent(options)).assertNext(createdAgent -> {
            assertNotNull(createdAgent, "Persistent agent should not be null");
            agent = createdAgent;
        }).verifyComplete();

        StepVerifier.create(threadsAsyncClient.createThread()).assertNext(createdThread -> {
            assertNotNull(createdThread, "Thread should not be null");
            thread = createdThread;
        }).verifyComplete();
    }

    private Mono<ThreadRun> createRunAndWaitForCompletion() {
        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId());

        Mono<ThreadRun> completedRun = runsAsyncClient.createRun(createRunOptions)
            .flatMap(run -> waitForRunCompletionAsync(run, runsAsyncClient));

        return completedRun;
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateAndRetrieveRun(HttpClient httpClient) {
        createTestAgent(httpClient);

        StepVerifier.create(createRunAndWaitForCompletion()).assertNext(run -> {
            assertNotNull(run, "Created run should not be null");
            assertNotNull(run.getId(), "Run ID should not be null");

            // Verify by retrieving the run
            StepVerifier.create(runsAsyncClient.getRun(thread.getId(), run.getId())).assertNext(retrievedRun -> {
                assertNotNull(retrievedRun, "Retrieved run should not be null");
                assertEquals(run.getId(), retrievedRun.getId(), "Run IDs should match");
            }).verifyComplete();
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateRunWithMetadata(HttpClient httpClient) {
        createTestAgent(httpClient);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("priority", "high");

        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId()).setMetadata(metadata);

        StepVerifier.create(runsAsyncClient.createRun(createRunOptions)).assertNext(run -> {
            assertNotNull(run, "Run with metadata should not be null");
            assertNotNull(run.getMetadata(), "Run metadata should not be null");
            assertEquals("high", run.getMetadata().get("priority"), "Metadata priority should match");
        }).verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUpdateRun(HttpClient httpClient) {
        createTestAgent(httpClient);

        StepVerifier.create(createRunAndWaitForCompletion()).assertNext(run -> {
            // Prepare metadata updates
            Map<String, String> updatedMetadata = new HashMap<>();
            updatedMetadata.put("updated", "true");
            updatedMetadata.put("timestamp", String.valueOf(System.currentTimeMillis()));

            // Update and verify
            StepVerifier.create(runsAsyncClient.updateRun(thread.getId(), run.getId(), updatedMetadata))
                .assertNext(updatedRun -> {
                    assertNotNull(updatedRun, "Updated run should not be null");
                    assertNotNull(updatedRun.getMetadata(), "Updated metadata should not be null");
                    assertEquals("true", updatedRun.getMetadata().get("updated"), "Updated flag should match");
                })
                .verifyComplete();
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListRunsDefault(HttpClient httpClient) {
        createTestAgent(httpClient);

        // Create multiple runs
        StepVerifier.create(createRunAndWaitForCompletion())
            .assertNext(run -> assertNotNull(run, "First run should not be null"))
            .verifyComplete();

        StepVerifier.create(createRunAndWaitForCompletion())
            .assertNext(run -> assertNotNull(run, "Second run should not be null"))
            .verifyComplete();

        // List runs and verify
        StepVerifier.create(runsAsyncClient.listRuns(thread.getId()).collectList()).assertNext(runsList -> {
            assertNotNull(runsList, "Runs list should not be null");
            assertTrue(runsList.size() >= 2, "There should be at least 2 runs");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListRunsWithParameters(HttpClient httpClient) {
        createTestAgent(httpClient);

        // Create multiple runs (at least 3)
        for (int i = 0; i < 3; i++) {
            createRunAndWaitForCompletion().block(Duration.ofMinutes(2));
        }

        // List with parameters and verify
        StepVerifier.create(runsAsyncClient.listRuns(thread.getId(), 5, null, null, null).collectList())
            .assertNext(filteredRuns -> {
                assertNotNull(filteredRuns, "Filtered runs should not be null");
                assertTrue(filteredRuns.size() <= 5, "Run list should have at most 5 runs");
            })
            .verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCancelRun(HttpClient httpClient) {
        createTestAgent(httpClient);

        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId());

        StepVerifier.create(runsAsyncClient.createRun(createRunOptions)).assertNext(run -> {
            // Attempt to cancel the run (this may not always succeed if the run completes quickly)
            StepVerifier.create(runsAsyncClient.cancelRun(thread.getId(), run.getId())).assertNext(cancelledRun -> {
                assertNotNull(cancelledRun, "Cancelled run should not be null");
                // Note: The run might complete before we can cancel it, so we don't assert on status
            }).verifyComplete();
        }).verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        if (thread != null && threadsAsyncClient != null) {
            threadsAsyncClient.deleteThread(thread.getId()).block(Duration.ofSeconds(30));
        }
        if (agent != null && administrationAsyncClient != null) {
            administrationAsyncClient.deleteAgent(agent.getId()).block(Duration.ofSeconds(30));
        }
    }
}
