// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.agents.persistent.FilesClient;
import com.azure.ai.agents.persistent.MessagesClient;
import com.azure.ai.agents.persistent.PersistentAgentsAdministrationClient;
import com.azure.ai.agents.persistent.RunsClient;
import com.azure.ai.agents.persistent.ThreadsClient;
import com.azure.ai.agents.persistent.VectorStoresClient;
import com.azure.ai.projects.implementation.PersistentAgentsClientImpl;

/**
 * Initializes a new instance of the synchronous PersistentAgentsClient type.
 */
public final class PersistentAgentsClient {

    private final PersistentAgentsClientImpl serviceClient;

    /**
     * Initializes an instance of PersistentAgentsClient class.
     *
     * @param serviceClient the service client implementation.
     */
    PersistentAgentsClient(PersistentAgentsClientImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Gets an instance of PersistentAgentsAdministrationClient class.
     *
     * @return an instance of PersistentAgentsAdministrationClient class.
     */
    public PersistentAgentsAdministrationClient getPersistentAgentsAdministration() {
        return serviceClient.getPersistentAgentsAdministration();
    }

    /**
     * Gets an instance of FilesClient class.
     *
     * @return an instance of FilesClient class.
     */
    public FilesClient getFiles() {
        return serviceClient.getFiles();
    }

    /**
     * Gets an instance of MessagesClient class.
     *
     * @return an instance of MessagesClient class.
     */
    public MessagesClient getMessages() {
        return serviceClient.getMessages();
    }

    /**
     * Gets an instance of RunsClient class.
     *
     * @return an instance of RunsClient class.
     */
    public RunsClient getRuns() {
        return serviceClient.getRuns();
    }

    /**
     * Gets an instance of ThreadsClient class.
     *
     * @return an instance of ThreadsClient class.
     */
    public ThreadsClient getThreads() {
        return serviceClient.getThreads();
    }

    /**
     * Gets an instance of VectorStoresClient class.
     *
     * @return an instance of VectorStoresClient class.
     */
    public VectorStoresClient getVectorStores() {
        return serviceClient.getVectorStores();
    }
}
