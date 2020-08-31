// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppPlatformTest extends TestBase {
    protected AppPlatformManager appPlatformManager;
    protected AppServiceManager appServiceManager;
    protected DnsZoneManager dnsZoneManager;
    protected KeyVaultManager keyVaultManager;
    protected String rgName = "";

    public AppPlatformTest() {
    }

    AppPlatformTest(RunCondition runCondition) {
        super(runCondition);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) throws IOException {
        rgName = generateRandomResourceName("rg", 20);
        appPlatformManager = AppPlatformManager.authenticate(httpPipeline, profile, sdkContext);
        appServiceManager = AppServiceManager.authenticate(httpPipeline, profile, sdkContext);
        dnsZoneManager = DnsZoneManager.authenticate(httpPipeline, profile, sdkContext);
        keyVaultManager = KeyVaultManager.authenticate(httpPipeline, profile, sdkContext);
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
            SdkContext.sleep(5000);
        }
        return false;
    }

    protected boolean requestSuccess(String url) throws IOException {
        for (int i = 0; i < 60; ++i) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            try {
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    connection.getInputStream().close();
                    return true;
                }
                System.out.printf("Do request to %s with response code %d%n", url, connection.getResponseCode());
            } catch (Exception e) {
                System.err.printf("Do request to %s with error %s%n", url, e.getMessage());
            } finally {
                connection.disconnect();
            }
            SdkContext.sleep(5000);
        }
        return false;
    }
}
