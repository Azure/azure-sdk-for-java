// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;

import java.io.IOException;

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
}
