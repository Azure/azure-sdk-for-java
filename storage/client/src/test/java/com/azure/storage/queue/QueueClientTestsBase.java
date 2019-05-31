// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public abstract class QueueClientTestsBase extends TestBase {
    private static final String AZURE_STORAGE_CONNECTION_STRING = "AZURE_STORAGE_CONNECTION_STRING";
    private static final String ACCOUNT_NAME = "AccountName".toLowerCase();
    private static final String PROTOCOL = "DefaultEndpointsProtocol".toLowerCase();
    private static final String ENDPOINT_SUFFIX = "EndpointSuffix".toLowerCase();
    private static final String ENDPOINT_FORMAT = "%s://%s.queues.%s/%s";
    private static String testQueueName;
    private static String connectionString;


    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    <T> T clientSetup(BiFunction<String, String, T> clientBuilder) {
        if (ImplUtils.isNullOrEmpty(connectionString)) {
            connectionString = interceptorManager.isPlaybackMode()
                ? "DefaultEndpointsProtocol=http;AccountName=playbackAccount;AccountKey=EJadf4qp7wsKngrEU8/chQiIJBAbXb/ouwuDt+m9Ksie+KHnFEM/5ROEK/OeAvxYuZ6OsOGag/bSltOiZJc2Mg==;EndpointSuffix=core.windows.net"
                : ConfigurationManager.getConfiguration().get(AZURE_STORAGE_CONNECTION_STRING);
        }

        Objects.requireNonNull(connectionString, "AZURE_STORAGE_CONNECTION_STRING expected to be set.");

        return Objects.requireNonNull(clientBuilder.apply(connectionString, getQueueEndpoint(connectionString)));
    }

    private String getQueueEndpoint(String connectionString) {
        Map<String, String> connectionKVPs = new HashMap<>();
        for (String s : connectionString.split(";")) {
            String[] kvp = s.split("=", 1);
            connectionKVPs.put(kvp[0].toLowerCase(), kvp[1]);
        }

        String protocol = connectionKVPs.get(PROTOCOL);
        String accountName = connectionKVPs.get(ACCOUNT_NAME);
        String endpointSuffix = connectionKVPs.get(ENDPOINT_SUFFIX);

        return String.format(ENDPOINT_FORMAT, protocol, accountName, endpointSuffix, testQueueName);
    }

    @Override
    protected void beforeTest() {
        if (ImplUtils.isNullOrEmpty(testQueueName)) {
            testQueueName = testResourceNamer.randomName("queue", 16);
        }
    }

    @Override
    protected void afterTest() {
    }

    /**
     * Tests that creating a queue with a unique name returns a CREATED response.
     */
    @Test
    public abstract void createQueue();

    /**
     * Tests that creating a queue with the same name of one that already exists using the same metadata returns a
     * NO_CONTENT response.
     */
    @Test
    public abstract void createQueueAlreadyExistsSameMetadata();

    /**
     * Tests that creating a queue with the same name of one that already exists using different metadata returns a
     * CONFLICT error response.
     */
    @Test
    public abstract void createQueueAlreadyExistsDifferentMetadata();

    /**
     * Tests that deleting a queue that exists returns a NO_CONTENT response.
     */
    @Test
    public abstract void deleteQueue();

    /**
     * Tests that deleting a queue that doesn't exist returns a NOT_FOUND response.
     */
    @Test
    public abstract void deleteQueueDoesNotExist();

    /**
     * Tests that getting a queue's metadata returns an OK response and the response contains the metadata.
     */
    @Test
    public abstract void getProperties();

    /**
     * Tests that setting a queue's metadata with empty values returns an NO_CONTENT response and getting the metadata afterwards
     * returns no values.
     */
    @Test
    public abstract void setEmptyMetadata();

    /**
     * Tests that setting a queue's metadata with values returns an NO_CONTENT response and getting the metadata afterwards
     * returned the values that were set.
     */
    @Test
    public abstract void setFilledMetadata();

    /**
     * Tests that getting a queue's access policy returns an OK response and the response contains the permissions.
     */
    @Test
    public abstract void getAccessPolicy();

    /**
     * Tests that setting a queue's access policy with no policies returns a NO_CONTENT response and getting the
     * permissions afterwards returns no values.
     */
    @Test
    public abstract void setEmptyAccessPolicy();

    /**
     * Tests that setting a queue's access policy with policies returns a NO_CONTENT response and getting the
     * permissions afterwards returns the values that were set.
     */
    @Test
    public abstract void setFilledAccessPolicy();

    /**
     * Tests that setting a queue's access policy with a policy that exceeds the ID size (64 characters) returns
     * an error (what type though?)
     */
    @Test
    public abstract void setAccessPolicyIdTooLong();
}
