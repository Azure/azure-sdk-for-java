// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.samples.ManageServicePrincipalCredentials;
import com.azure.resourcemanager.authorization.samples.ManageUsersGroupsAndRoles;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GraphRbacTests extends ResourceManagerTestBase {
    private AzureResourceManager azureResourceManager;
    private AzureProfile profile;

    @Test
    @DoNotRecord
    public void testManageUsersGroupsAndRoles() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageUsersGroupsAndRoles.runSample(azureResourceManager, profile));
    }

//    @Test
//    public void testManageServicePrincipal() {
//        Assertions.assertTrue(ManageServicePrincipal.runSample(authenticated, defaultSubscription));
//    }

    @Test
    @DoNotRecord
    public void testManageServicePrincipalCredentials() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageServicePrincipalCredentials.runSample(azureResourceManager, profile));
    }

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        azureResourceManager = buildManager(AzureResourceManager.class, httpPipeline, profile);
        this.profile = profile;
    }

    @Override
    protected void cleanUpResources() {
    }
}

