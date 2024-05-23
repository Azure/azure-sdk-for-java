// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.Arrays;

/**
 * The {@link AzurePipelinesCredentialBuilder} provides a fluent builder for {@link AzurePipelinesCredential}.
 *
 * <!-- src_embed com.azure.identity.credential.azurepipelinescredential.construct -->
 * <pre>
 * &#47;&#47; serviceConnectionId is retrieved from the portal.
 * &#47;&#47; systemAccessToken is retrieved from the pipeline environment as shown.
 * &#47;&#47; You may choose another name for this variable.
 *
 * String systemAccessToken = System.getenv&#40;&quot;SYSTEM_ACCESSTOKEN&quot;&#41;;
 * AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .serviceConnectionId&#40;serviceConnectionId&#41;
 *     .systemAccessToken&#40;systemAccessToken&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azurepipelinescredential.construct -->
 */
public class AzurePipelinesCredentialBuilder extends AadCredentialBuilderBase<AzurePipelinesCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(AzurePipelinesCredentialBuilder.class);
    private static final String OIDC_API_VERSION = "7.1-preview.1";
    private String serviceConnectionId;
    private String systemAccessToken;

    /**
     * Sets the service connection id for the Azure Pipelines service connection. The service connection ID is
     * retrieved from the Service Connection in the portal.
     *
     * @param serviceConnectionId The service connection ID, as found in the query string's resourceId key.
     * @return the updated instance of the builder.
     */
    public AzurePipelinesCredentialBuilder serviceConnectionId(String serviceConnectionId) {
        this.serviceConnectionId = serviceConnectionId;
        return this;
    }

    /**
     * Sets the System Access Token for the Azure Pipelines service connection. The system access token is
     * retrieved from the pipeline variables by assigning it to an environment variable and reading it.
     * See {@link AzurePipelinesCredential} for more information.
     *
     * @param systemAccessToken the system access token for the Azure Pipelines service connection.
     * @return The updated instance of the builder.
     */
    public AzurePipelinesCredentialBuilder systemAccessToken(String systemAccessToken) {
        this.systemAccessToken = systemAccessToken;
        return this;
    }

    /**
     * Builds an instance of the {@link AzurePipelinesCredential} with the current configurations.
     * @return an instance of the {@link AzurePipelinesCredential}.
     */
    public AzurePipelinesCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(),
            LOGGER,
            Arrays.asList("clientId", "tenantId", "serviceConnectionId", "systemAccessToken"),
            Arrays.asList(this.clientId, this.tenantId, this.serviceConnectionId, this.systemAccessToken));

        Configuration configuration = identityClientOptions.getConfiguration();
        if (configuration == null) {
            configuration = Configuration.getGlobalConfiguration();
        }
        String teamFoundationCollectionUri = configuration.get("SYSTEM_TEAMFOUNDATIONCOLLECTIONURI");
        String teamProjectId = configuration.get("SYSTEM_TEAMPROJECTID");
        String planId = configuration.get("SYSTEM_PLANID");
        String jobId = configuration.get("SYSTEM_JOBID");

        ValidationUtil.validate(getClass().getSimpleName(), LOGGER,
            Arrays.asList("SYSTEM_TEAMFOUNDATIONCOLLECTIONURI", "SYSTEM_TEAMPROJECTID", "SYSTEM_PLANID", "SYSTEM_JOBID"),
            Arrays.asList(teamFoundationCollectionUri, teamProjectId, planId, jobId));

        String requestUrl = String.format("%s%s/_apis/distributedtask/hubs/build/plans/%s/jobs/%s/oidctoken?api-version=%s&serviceConnectionId=%s",
            teamFoundationCollectionUri, teamProjectId, planId, jobId, OIDC_API_VERSION, serviceConnectionId);
        return new AzurePipelinesCredential(this.clientId, this.tenantId, requestUrl, systemAccessToken, identityClientOptions.clone());
    }
}
