// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class AzureProfileTests {
    @Test
    public void testFromAzureCloud() {
        // normal case
        AzureProfile azurePublicCloud = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
        Assertions.assertEquals(AzureEnvironment.AZURE, azurePublicCloud.getEnvironment());

        AzureProfile azureChinaCloud = new AzureProfile(AzureCloud.AZURE_CHINA_CLOUD);
        Assertions.assertEquals(AzureEnvironment.AZURE_CHINA, azureChinaCloud.getEnvironment());

        AzureProfile azureUSGovernment = new AzureProfile(AzureCloud.AZURE_US_GOVERNMENT_CLOUD);
        Assertions.assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT, azureUSGovernment.getEnvironment());

        // exception case
        // exception when initializing using custom AzureCloud
        AzureCloud azureCloud = AzureCloud.fromString("Custom");
        Assertions.assertNotNull(azureCloud);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AzureProfile(azureCloud));

        AzureProfile customEnvironment = new AzureProfile(new AzureEnvironment(new HashMap<>()));
    }
}
