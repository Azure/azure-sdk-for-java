// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArmMetadataTests {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testGetAzureEnvironmentFromArmEndpoint() {
        AzureEnvironment azureEnvironment = AzureResourceManager.getAzureEnvironmentFromArmEndpoint("https://management.azure.com/", HttpClient.createDefault());
        Assertions.assertEquals("https://management.azure.com/", azureEnvironment.getResourceManagerEndpoint());
        Assertions.assertEquals("https://management.core.windows.net/", azureEnvironment.getManagementEndpoint()); // actually we should use azureEnvironment.getActiveDirectoryResourceId(), but keep it as many libs uses this.
        Assertions.assertEquals("https://management.core.windows.net/", azureEnvironment.getActiveDirectoryResourceId());
        Assertions.assertEquals("https://graph.windows.net/", azureEnvironment.getGraphEndpoint());
        Assertions.assertEquals(".azure.com/", azureEnvironment.getStorageEndpointSuffix());
        Assertions.assertEquals(".vault.azure.com/", azureEnvironment.getKeyVaultDnsSuffix());
    }
}
