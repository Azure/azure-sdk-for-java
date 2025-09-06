// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.deviceregistry.models.Asset;
import com.azure.resourcemanager.deviceregistry.models.AssetEndpointProfile;
import com.azure.resourcemanager.deviceregistry.models.BillingContainer;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class DeviceRegistryManagerTests extends TestProxyTestBase {
    private DeviceRegistryManager deviceRegistryManager = null;
    private ResourceManager resourceManager = null;

    @Override
    public void beforeTest() {
        final TokenCredential credential = TestUtilities.getTokenCredentialForTest(getTestMode());
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        deviceRegistryManager = DeviceRegistryManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);
    }

    @Test
    @LiveOnly
    public void testList() {
        // The AssetEndpointProfile and Assets service must be supported by the service Kubernetes cluster with Azure Arc
        // and Microsoft.ExtendedLocation, but Kubernetes cluster with Azure Arc can only be created by script.
        // so only add List test
        List<Asset> assets = deviceRegistryManager.assets().list().stream().collect(Collectors.toList());
        Assertions.assertTrue(assets.size() >= 0);

        List<AssetEndpointProfile> assetEndpointProfiles
            = deviceRegistryManager.assetEndpointProfiles().list().stream().collect(Collectors.toList());
        Assertions.assertTrue(assetEndpointProfiles.size() >= 0);

        // The BillingContainers only supported `get` and `list`.
        List<BillingContainer> billingContainers
            = deviceRegistryManager.billingContainers().list().stream().collect(Collectors.toList());
        Assertions.assertTrue(billingContainers.size() >= 0);
    }
}
