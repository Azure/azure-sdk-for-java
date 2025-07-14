// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ToolResources;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;
import java.util.HashMap;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadsAsyncClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private PersistentAgentsAdministrationAsyncClient administrationAsyncClient;
    private ThreadsAsyncClient threadsAsyncClient;
    private PersistentAgent agent;
    private PersistentAgentThread thread;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        createAgent();
    }

    private void createAgent() {
        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName("TestAgent").setInstructions("You are a helpful agent");

        agent = administrationAsyncClient.createAgent(options).block();
        assertNotNull(agent, "Persistent agent should not be null");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateThread(HttpClient httpClient) {
        setup(httpClient);

        StepVerifier.create(threadsAsyncClient.createThread()).assertNext(createdThread -> {
            assertNotNull(createdThread, "Thread should not be null");
            assertNotNull(createdThread.getId(), "Thread ID should not be null");
            thread = createdThread;
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListThreads(HttpClient httpClient) {
        setup(httpClient);

        // Create a thread first
        thread = threadsAsyncClient.createThread().block();
        assertNotNull(thread, "Thread should not be null");

        StepVerifier.create(threadsAsyncClient.listThreads().take(10).collectList()).assertNext(threads -> {
            assertNotNull(threads, "Thread list should not be null");
            assertTrue(!threads.isEmpty(), "Thread list should not be empty");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetThread(HttpClient httpClient) {
        setup(httpClient);

        // Create a thread first
        thread = threadsAsyncClient.createThread().block();
        assertNotNull(thread, "Thread should not be null");

        StepVerifier.create(threadsAsyncClient.getThread(thread.getId())).assertNext(retrievedThread -> {
            assertNotNull(retrievedThread, "Retrieved thread should not be null");
            assertTrue(thread.getId().equals(retrievedThread.getId()), "Thread ID should match");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUpdateThread(HttpClient httpClient) {
        setup(httpClient);

        // Create a thread first
        thread = threadsAsyncClient.createThread().block();
        assertNotNull(thread, "Thread should not be null");

        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");

        StepVerifier.create(threadsAsyncClient.updateThread(thread.getId(), new ToolResources(), metadata))
            .assertNext(updatedThread -> {
                assertNotNull(updatedThread, "Updated thread should not be null");
                assertTrue("testValue".equals(updatedThread.getMetadata().get("testKey")),
                    "Updated metadata should match");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteThread(HttpClient httpClient) {
        setup(httpClient);

        // Create a thread first
        thread = threadsAsyncClient.createThread().block();
        assertNotNull(thread, "Thread should not be null");

        StepVerifier.create(threadsAsyncClient.deleteThread(thread.getId())).verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        if (thread != null) {
            try {
                // Attempt to delete the thread
                threadsAsyncClient.deleteThread(thread.getId()).block();
            } catch (Exception e) {
                System.out.println("Failed to cleanup thread: " + thread.getId());
                System.out.println(e.getMessage());
            }
        }
        if (agent != null) {
            administrationAsyncClient.deleteAgent(agent.getId()).block();
        }
    }
}
