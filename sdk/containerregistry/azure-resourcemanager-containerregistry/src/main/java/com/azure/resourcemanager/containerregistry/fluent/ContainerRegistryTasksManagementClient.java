// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.fluent;

import com.azure.core.http.HttpPipeline;
import java.time.Duration;

/**
 * An instance of this class provides access to all the operations defined in ContainerRegistryTasksManagementClient.
 * @deprecated Use azure-resourcemanager-containerregistry-tasks lib.
 */
@Deprecated
public interface ContainerRegistryTasksManagementClient {
    /**
     * Gets The Microsoft Azure subscription ID.
     * 
     * @return the subscriptionId value.
     */
    String getSubscriptionId();

    /**
     * Gets server parameter.
     * 
     * @return the endpoint value.
     */
    String getEndpoint();

    /**
     * Gets Api Version.
     * 
     * @return the apiVersion value.
     */
    String getApiVersion();

    /**
     * Gets The HTTP pipeline to send requests through.
     * 
     * @return the httpPipeline value.
     */
    HttpPipeline getHttpPipeline();

    /**
     * Gets The default poll interval for long-running operation.
     * 
     * @return the defaultPollInterval value.
     */
    Duration getDefaultPollInterval();

    /**
     * Gets the AgentPoolsClient object to access its operations.
     * 
     * @return the AgentPoolsClient object.
     */
    AgentPoolsClient getAgentPools();

    /**
     * Gets the RegistryTasksClient object to access its operations.
     * 
     * @return the RegistryTasksClient object.
     */
    RegistryTasksClient getRegistryTasks();

    /**
     * Gets the RunsClient object to access its operations.
     * 
     * @return the RunsClient object.
     */
    RunsClient getRuns();

    /**
     * Gets the TaskRunsClient object to access its operations.
     * 
     * @return the TaskRunsClient object.
     */
    TaskRunsClient getTaskRuns();

    /**
     * Gets the TasksClient object to access its operations.
     * 
     * @return the TasksClient object.
     */
    TasksClient getTasks();
}
