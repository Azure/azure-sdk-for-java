// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ArmMetadataTests {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testGetAzureEnvironmentFromArmEndpoint() {
        AzureEnvironment azureEnvironment = AzureResourceManager.getAzureEnvironmentFromArmEndpoint("https://management.azure.com/", HttpClient.createDefault());
        Assertions.assertEquals("https://management.azure.com/", azureEnvironment.getResourceManagerEndpoint());
        Assertions.assertEquals("https://management.core.windows.net/", azureEnvironment.getManagementEndpoint()); // actually we should use azureEnvironment.getActiveDirectoryResourceId(), but keep it as many libs uses this.
        Assertions.assertEquals("https://management.core.windows.net/", azureEnvironment.getActiveDirectoryResourceId());
        Assertions.assertEquals("https://login.microsoftonline.com/", azureEnvironment.getActiveDirectoryEndpoint());
        Assertions.assertEquals("https://graph.windows.net/", azureEnvironment.getGraphEndpoint());
        Assertions.assertEquals(".core.windows.net", azureEnvironment.getStorageEndpointSuffix());
        Assertions.assertEquals(".vault.azure.net", azureEnvironment.getKeyVaultDnsSuffix());
    }

    @Test
    @Disabled("LIVE only, requires configuration of credentials")
    public void authenticateAzureResourceManager() {
        // HTTP client
        final HttpClient httpClient = HttpClient.createDefault();

        // Azure environment and profile
        final AzureEnvironment azureEnvironment = AzureResourceManager.getAzureEnvironmentFromArmEndpoint(
            "https://management.azure.com/", httpClient);
        final AzureProfile profile = new AzureProfile(azureEnvironment);

        // credential
        final TokenCredential credential = new DefaultAzureCredentialBuilder()
            .httpClient(httpClient)
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();

        AzureResourceManager azureResourceManager = AzureResourceManager
            .configure()
            .withHttpClient(httpClient)
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .authenticate(credential, profile)
            .withDefaultSubscription();

        azureResourceManager.resourceGroups().list().stream().count();
    }
}
