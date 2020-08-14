// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.test.ResourceManagerTestBase;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class AppPlatformTest extends ResourceManagerTestBase {
    protected AppPlatformManager appPlatformManager;
    protected AppServiceManager appServiceManager;
    protected DnsZoneManager dnsZoneManager;
    protected KeyVaultManager keyVaultManager;
    protected String rgName = "";

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential, profile, null, new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS),
            null, new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("rg", 20);
        appPlatformManager = AppPlatformManager.authenticate(httpPipeline, profile);
        appServiceManager = AppServiceManager.authenticate(httpPipeline, profile);
        dnsZoneManager = DnsZoneManager.authenticate(httpPipeline, profile);
        keyVaultManager = KeyVaultManager.authenticate(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        try {
            appPlatformManager.resourceManager().resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) { }
    }
}
