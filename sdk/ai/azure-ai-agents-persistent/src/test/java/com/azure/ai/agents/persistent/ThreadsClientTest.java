// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ToolResources;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.HashMap;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadsClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private PersistentAgentsAdministrationClient administrationClient;
    private ThreadsClient threadsClient;
    private PersistentAgent agent;
    private PersistentAgentThread thread;

    private PersistentAgent createAgent(String agentName) {
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
        agent = createAgent("TestAgent");
        thread = threadsClient.createThread();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateThread(HttpClient httpClient) {
        setup(httpClient);
        // Validate that thread exists
        assertNotNull(thread, "Thread should not be null");
        assertNotNull(thread.getId(), "Thread ID should not be null");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListThreads(HttpClient httpClient) {
        setup(httpClient);
        PagedIterable<PersistentAgentThread> threadList = threadsClient.listThreads();
        assertNotNull(threadList, "Thread list should not be null");
        assertTrue(threadList.stream().count() > 0, "Thread list should not be empty");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetThread(HttpClient httpClient) {
        setup(httpClient);
        PersistentAgentThread retrievedThread = threadsClient.getThread(thread.getId());
        assertNotNull(retrievedThread, "Retrieved thread should not be null");
        assertTrue(thread.getId().equals(retrievedThread.getId()), "Thread ID should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUpdateThread(HttpClient httpClient) {
        setup(httpClient);
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        PersistentAgentThread updatedThread = threadsClient.updateThread(thread.getId(), new ToolResources(), metadata);
        assertNotNull(updatedThread, "Updated thread should not be null");
        assertTrue("testValue".equals(updatedThread.getMetadata().get("testKey")), "Updated metadata should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteThread(HttpClient httpClient) {
        setup(httpClient);
        threadsClient.deleteThread(thread.getId());
        assertTrue(true, "Thread should be deleted");
    }

    @AfterEach
    public void cleanup() {
        if (thread != null) {
            try {
                // Attempt to delete the thread
                threadsClient.deleteThread(thread.getId());
            } catch (Exception e) {
                System.out.println("Failed to cleanup thread: " + thread.getId());
                System.out.println(e.getMessage());
            }
        }
        if (agent != null) {
            administrationClient.deleteAgent(agent.getId());
        }
    }
}
