// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.agents.persistent.FilesAsyncClient;
import com.azure.ai.agents.persistent.MessagesAsyncClient;
import com.azure.ai.agents.persistent.PersistentAgentsAdministrationAsyncClient;
import com.azure.ai.agents.persistent.RunsAsyncClient;
import com.azure.ai.agents.persistent.ThreadsAsyncClient;
import com.azure.ai.agents.persistent.VectorStoresAsyncClient;
import com.azure.ai.projects.implementation.PersistentAgentsClientImpl;

/**
 * Initializes a new instance of the asynchronous PersistentAgentsAsyncClient type.
 */
public final class PersistentAgentsAsyncClient {

    private final PersistentAgentsClientImpl serviceClient;

    /**
     * Initializes an instance of PersistentAgentsAsyncClient class.
     *
     * @param serviceClient the service client implementation.
     */
    PersistentAgentsAsyncClient(PersistentAgentsClientImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Gets an instance of PersistentAgentsAdministrationAsyncClient class.
     *
     * @return an instance of PersistentAgentsAdministrationAsyncClient class.
     */
    public PersistentAgentsAdministrationAsyncClient getPersistentAgentsAdministration() {
        return serviceClient.getPersistentAgentsAdministrationAsync();
    }

    /**
     * Gets an instance of FilesAsyncClient class.
     *
     * @return an instance of FilesAsyncClient class.
     */
    public FilesAsyncClient getFiles() {
        return serviceClient.getFilesAsync();
    }

    /**
     * Gets an instance of MessagesAsyncClient class.
     *
     * @return an instance of MessagesAsyncClient class.
     */
    public MessagesAsyncClient getMessages() {
        return serviceClient.getMessagesAsync();
    }

    /**
     * Gets an instance of RunsAsyncClient class.
     *
     * @return an instance of RunsAsyncClient class.
     */
    public RunsAsyncClient getRuns() {
        return serviceClient.getRunsAsync();
    }

    /**
     * Gets an instance of ThreadsAsyncClient class.
     *
     * @return an instance of ThreadsAsyncClient class.
     */
    public ThreadsAsyncClient getThreads() {
        return serviceClient.getThreadsAsync();
    }

    /**
     * Gets an instance of VectorStoresAsyncClient class.
     *
     * @return an instance of VectorStoresAsyncClient class.
     */
    public VectorStoresAsyncClient getVectorStores() {
        return serviceClient.getVectorStoresAsync();
    }
}
