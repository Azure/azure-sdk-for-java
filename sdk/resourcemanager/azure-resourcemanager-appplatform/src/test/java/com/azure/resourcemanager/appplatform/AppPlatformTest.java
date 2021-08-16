// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AppPlatformTest extends ResourceManagerTestBase {
    protected AppPlatformManager appPlatformManager;
    protected AppServiceManager appServiceManager;
    protected DnsZoneManager dnsZoneManager;
    protected KeyVaultManager keyVaultManager;
    protected String rgName = "";

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
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        rgName = generateRandomResourceName("rg", 20);
        appPlatformManager = buildManager(AppPlatformManager.class, httpPipeline, profile);
        appServiceManager = buildManager(AppServiceManager.class, httpPipeline, profile);
        dnsZoneManager = buildManager(DnsZoneManager.class, httpPipeline, profile);
        keyVaultManager = buildManager(KeyVaultManager.class, httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        try {
            appPlatformManager.resourceManager().resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) { }
    }

    protected boolean checkRedirect(String url) throws IOException {
        for (int i = 0; i < 60; ++i) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            try {
                connection.connect();
                if (200 <= connection.getResponseCode() && connection.getResponseCode() < 400) {
                    connection.getInputStream().close();
                    if (connection.getResponseCode() / 100 == 3) {
                        return true;
                    }
                    System.out.printf("Do request to %s with response code %d%n", url, connection.getResponseCode());
                }
            } catch (Exception e) {
                System.err.printf("Do request to %s with error %s%n", url, e.getMessage());
            } finally {
                connection.disconnect();
            }
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
        }
        return false;
    }

    protected boolean requestSuccess(String url) throws IOException {
        for (int i = 0; i < 60; ++i) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            try {
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    System.out.printf("Request to %s succeeded%n", url);
                    connection.getInputStream().close();
                    return true;
                }
                System.out.printf("Do request to %s with response code %d%n", url, connection.getResponseCode());
            } catch (Exception e) {
                System.err.printf("Do request to %s with error %s%n", url, e.getMessage());
            } finally {
                connection.disconnect();
            }
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
        }
        return false;
    }
}
