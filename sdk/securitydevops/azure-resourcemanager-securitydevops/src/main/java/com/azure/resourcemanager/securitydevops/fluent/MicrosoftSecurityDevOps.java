// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.securitydevops.fluent;

import com.azure.core.http.HttpPipeline;
import java.time.Duration;

/** The interface for MicrosoftSecurityDevOps class. */
public interface MicrosoftSecurityDevOps {
    /**
     * Gets The ID of the target subscription.
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
     * Gets the AzureDevOpsConnectorsClient object to access its operations.
     *
     * @return the AzureDevOpsConnectorsClient object.
     */
    AzureDevOpsConnectorsClient getAzureDevOpsConnectors();

    /**
     * Gets the AzureDevOpsRepoesClient object to access its operations.
     *
     * @return the AzureDevOpsRepoesClient object.
     */
    AzureDevOpsRepoesClient getAzureDevOpsRepoes();

    /**
     * Gets the AzureDevOpsConnectorStatsOperationsClient object to access its operations.
     *
     * @return the AzureDevOpsConnectorStatsOperationsClient object.
     */
    AzureDevOpsConnectorStatsOperationsClient getAzureDevOpsConnectorStatsOperations();

    /**
     * Gets the AzureDevOpsOrgsClient object to access its operations.
     *
     * @return the AzureDevOpsOrgsClient object.
     */
    AzureDevOpsOrgsClient getAzureDevOpsOrgs();

    /**
     * Gets the AzureDevOpsProjectsClient object to access its operations.
     *
     * @return the AzureDevOpsProjectsClient object.
     */
    AzureDevOpsProjectsClient getAzureDevOpsProjects();

    /**
     * Gets the GitHubConnectorsClient object to access its operations.
     *
     * @return the GitHubConnectorsClient object.
     */
    GitHubConnectorsClient getGitHubConnectors();

    /**
     * Gets the GitHubRepoesClient object to access its operations.
     *
     * @return the GitHubRepoesClient object.
     */
    GitHubRepoesClient getGitHubRepoes();

    /**
     * Gets the GitHubConnectorStatsOperationsClient object to access its operations.
     *
     * @return the GitHubConnectorStatsOperationsClient object.
     */
    GitHubConnectorStatsOperationsClient getGitHubConnectorStatsOperations();

    /**
     * Gets the GitHubOwnersClient object to access its operations.
     *
     * @return the GitHubOwnersClient object.
     */
    GitHubOwnersClient getGitHubOwners();

    /**
     * Gets the OperationsClient object to access its operations.
     *
     * @return the OperationsClient object.
     */
    OperationsClient getOperations();
}
