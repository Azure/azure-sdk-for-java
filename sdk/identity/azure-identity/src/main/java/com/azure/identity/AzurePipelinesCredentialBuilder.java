// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.Configuration;

import java.util.Objects;

/**
 * The {@link AzurePipelinesCredentialBuilder} provides a fluent builder for {@link AzurePipelinesCredential}.
 */
public class AzurePipelinesCredentialBuilder extends AadCredentialBuilderBase<AzurePipelinesCredentialBuilder> {
    private String serviceConnectionId;
    private final static String OIDC_API_VERSION = "7.1-preview.1";
    /**
     * Sets the service connection id for the Azure Devops Pipeline service connection. The service connection id is
     * retrieved from the Serivce Connection in the portal.
     *
     * @param serviceConnectionId the service connection id for the Azure Devops Pipeline service connection.
     * @return the updated instance of the builder.
     */
    public AzurePipelinesCredentialBuilder serviceConnectionId(String serviceConnectionId) {
        this.serviceConnectionId = serviceConnectionId;
        return this;
    }

    /**
     * Builds an instance of the {@link AzurePipelinesCredential} with the current configurations.
     * @return an instance of the {@link AzurePipelinesCredential}.
     */
    public AzurePipelinesCredential build() {
        Objects.requireNonNull(serviceConnectionId);
        Objects.requireNonNull(this.clientId);
        Objects.requireNonNull(this.tenantId);

        String teamFoundationCollectionUri = Configuration.getGlobalConfiguration().get("SYSTEM_TEAMFOUNDATIONCOLLECTIONURI");
        String teamProjectId = Configuration.getGlobalConfiguration().get("SYSTEM_TEAMPROJECTID");
        String planId = Configuration.getGlobalConfiguration().get("SYSTEM_PLANID");
        String jobId = Configuration.getGlobalConfiguration().get("SYSTEM_JOBID");
        String systemAccessToken = Configuration.getGlobalConfiguration().get("SYSTEM_ACCESSTOKEN");

        Objects.requireNonNull(teamFoundationCollectionUri);
        Objects.requireNonNull(teamProjectId);
        Objects.requireNonNull(planId);
        Objects.requireNonNull(jobId);
        Objects.requireNonNull(systemAccessToken);

        String requestUrl = String.format("%s%s/_apis/distributedtask/hubs/build/plans/%s/jobs/%s/oidctoken?api-version=%s&serviceConnectionId=%s",
            teamFoundationCollectionUri, teamProjectId, planId, jobId, OIDC_API_VERSION, serviceConnectionId);
        return new AzurePipelinesCredential(this.clientId, this.tenantId, requestUrl, systemAccessToken, identityClientOptions.clone());
    }
}
