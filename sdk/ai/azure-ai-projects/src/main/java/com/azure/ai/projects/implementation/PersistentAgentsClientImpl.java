// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.ai.agents.persistent.FilesAsyncClient;
import com.azure.ai.agents.persistent.FilesClient;
import com.azure.ai.agents.persistent.MessagesAsyncClient;
import com.azure.ai.agents.persistent.MessagesClient;
import com.azure.ai.agents.persistent.PersistentAgentsAdministrationAsyncClient;
import com.azure.ai.agents.persistent.PersistentAgentsAdministrationClient;
import com.azure.ai.agents.persistent.PersistentAgentsClientBuilder;
import com.azure.ai.agents.persistent.RunsAsyncClient;
import com.azure.ai.agents.persistent.RunsClient;
import com.azure.ai.agents.persistent.ThreadsAsyncClient;
import com.azure.ai.agents.persistent.ThreadsClient;
import com.azure.ai.agents.persistent.VectorStoresAsyncClient;
import com.azure.ai.agents.persistent.VectorStoresClient;

/**
 * Initializes a new instance of the PersistentAgentsClient type.
 */
public final class PersistentAgentsClientImpl {

    private final PersistentAgentsClientBuilder clientBuilder;

    /**
     * Initializes an instance of PersistentAgentsClient client.
     * 
     * @param clientBuilder the client builder containing configurations for the client.
     */
    public PersistentAgentsClientImpl(PersistentAgentsClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    /**
     * Gets an instance of PersistentAgentsAdministration class.
     *
     * @return an instance of PersistentAgentsAdministration class.
     */
    public PersistentAgentsAdministrationClient getPersistentAgentsAdministration() {
        return clientBuilder.buildPersistentAgentsAdministrationClient();
    }

    /**
     * Gets an instance of PersistentAgentsAdministrationAsyncClient class.
     *
     * @return an instance of PersistentAgentsAdministrationAsyncClient class.
     */
    public PersistentAgentsAdministrationAsyncClient getPersistentAgentsAdministrationAsync() {
        return clientBuilder.buildPersistentAgentsAdministrationAsyncClient();
    }

    /**
     * Gets an instance of FilesClient class.
     *
     * @return an instance of FilesClient class.
     */
    public FilesClient getFiles() {
        return clientBuilder.buildFilesClient();
    }

    /**
     * Gets an instance of FilesAsyncClient class.
     *
     * @return an instance of FilesAsyncClient class.
     */
    public FilesAsyncClient getFilesAsync() {
        return clientBuilder.buildFilesAsyncClient();
    }

    /**
     * Gets an instance of MessagesClient class.
     *
     * @return an instance of MessagesClient class.
     */
    public MessagesClient getMessages() {
        return clientBuilder.buildMessagesClient();
    }

    /**
     * Gets an instance of MessagesAsyncClient class.
     *
     * @return an instance of MessagesAsyncClient class.
     */
    public MessagesAsyncClient getMessagesAsync() {
        return clientBuilder.buildMessagesAsyncClient();
    }

    /**
     * Gets an instance of RunsClient class.
     *
     * @return an instance of RunsClient class.
     */
    public RunsClient getRuns() {
        return clientBuilder.buildRunsClient();
    }

    /**
     * Gets an instance of RunsAsyncClient class.
     *
     * @return an instance of RunsAsyncClient class.
     */
    public RunsAsyncClient getRunsAsync() {
        return clientBuilder.buildRunsAsyncClient();
    }

    /**
     * Gets an instance of ThreadsClient class.
     *
     * @return an instance of ThreadsClient class.
     */
    public ThreadsClient getThreads() {
        return clientBuilder.buildThreadsClient();
    }

    /**
     * Gets an instance of ThreadsAsyncClient class.
     *
     * @return an instance of ThreadsAsyncClient class.
     */
    public ThreadsAsyncClient getThreadsAsync() {
        return clientBuilder.buildThreadsAsyncClient();
    }

    /**
     * Gets an instance of VectorStoresClient class.
     *
     * @return an instance of VectorStoresClient class.
     */
    public VectorStoresClient getVectorStores() {
        return clientBuilder.buildVectorStoresClient();
    }

    /**
     * Gets an instance of VectorStoresAsyncClient class.
     *
     * @return an instance of VectorStoresAsyncClient class.
     */
    public VectorStoresAsyncClient getVectorStoresAsync() {
        return clientBuilder.buildVectorStoresAsyncClient();
    }
}
