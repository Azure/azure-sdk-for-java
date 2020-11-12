// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import org.junit.jupiter.api.Test;

import java.util.List;

public abstract class KeyVaultAccessControlClientTestBase extends KeyVaultAdministrationClientTestBase {
    protected static final String ROLE_NAME = "Managed HSM Crypto User";
    String clientId = "49acc88b-8f9e-4619-9856-16691db66767";

    protected KeyVaultAccessControlClientBuilder getClientBuilder(HttpClient httpClient, boolean forCleanup) {
        List<HttpPipelinePolicy> policies = getPolicies();

        if (getTestMode() == TestMode.RECORD && !forCleanup) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return new KeyVaultAccessControlClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline);
    }

    @Test
    public abstract void listRoleDefinitions(HttpClient httpClient);

    @Test
    public abstract void listRoleAssignments(HttpClient httpClient);

    @Test
    public abstract void createRoleAssignment(HttpClient httpClient);

    @Test
    public abstract void getRoleAssignment(HttpClient httpClient);

    @Test
    public abstract void deleteRoleAssignment(HttpClient httpClient);
}
