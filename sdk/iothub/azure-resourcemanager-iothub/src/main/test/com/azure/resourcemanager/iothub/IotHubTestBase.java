// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.iothub;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.iothub.fluent.models.IotHubDescriptionInner;
import com.azure.resourcemanager.iothub.models.IotHubDescription;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import java.util.UUID;

import static com.azure.resourcemanager.iothub.Constants.DEFAULT_INSTANCE_NAME;
import static com.azure.resourcemanager.iothub.Constants.DEFAULT_REGION;

public class IotHubTestBase extends TestBase {
    public ResourceManager createResourceManager() {
        return ResourceManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE))
            .withDefaultSubscription();
    }

    public ResourceGroup createResourceGroup(ResourceManager resourceManager) {
        String resourceGroupName = DEFAULT_INSTANCE_NAME + "-" + createRandomSuffix();
        return resourceManager.resourceGroups()
            .define(resourceGroupName)
            .withRegion(DEFAULT_REGION)
            .create();
    }

    public IotHubManager createIotHubManager() {
        return IotHubManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));
    }

    public IotHubDescriptionInner createIotHub(IotHubManager iotHubManager, ResourceGroup resourceGroup) {
        String serviceName = DEFAULT_INSTANCE_NAME + "-" + createRandomSuffix();

        IotHubDescription iotHubDescription = iotHubManager
            .iotHubResources()
            .define(serviceName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroup.name())
            .withSku(Constants.DefaultSku.INSTANCE)
            .create();

        IotHubDescriptionInner inner = iotHubManager
            .serviceClient()
            .getIotHubResources()
            .createOrUpdate(resourceGroup.name(), serviceName, iotHubDescription.innerModel());

        return inner;
    }

    public String createRandomSuffix() {
        // need to shorten the UUID since max service name is 50 characters
        return UUID.randomUUID().toString().substring(0, 18);
    }
}
