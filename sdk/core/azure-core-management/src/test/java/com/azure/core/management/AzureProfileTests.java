package com.azure.core.management;

import com.azure.core.management.profile.AzureProfile;
import com.azure.core.AzureCloud;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class AzureProfileTests {
    @Test
    public void testFromAzureCloud() {
        // normal case
        AzureProfile azurePublicCloud = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
        Assertions.assertEquals(AzureEnvironment.AZURE, azurePublicCloud.getEnvironment());
        Assertions.assertEquals(AzureCloud.AZURE_PUBLIC_CLOUD, azurePublicCloud.getAzureCloud());

        AzureProfile azureChinaCloud = new AzureProfile(AzureCloud.AZURE_CHINA_CLOUD);
        Assertions.assertEquals(AzureEnvironment.AZURE_CHINA, azureChinaCloud.getEnvironment());
        Assertions.assertEquals(AzureCloud.AZURE_CHINA_CLOUD, azureChinaCloud.getAzureCloud());

        AzureProfile azureUSGovernment = new AzureProfile(AzureCloud.AZURE_US_GOVERNMENT);
        Assertions.assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT, azureUSGovernment.getEnvironment());
        Assertions.assertEquals(AzureCloud.AZURE_US_GOVERNMENT, azureUSGovernment.getAzureCloud());

        // exception case
        // exception when initializing using custom AzureCloud
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AzureProfile(AzureCloud.fromString("Custom")));

        AzureProfile customEnvironment = new AzureProfile(new AzureEnvironment(new HashMap<>()));
        // exception when getting AzureCloud from custom AzureEnvironment
        Assertions.assertThrows(IllegalArgumentException.class, customEnvironment::getAzureCloud);
    }
}
