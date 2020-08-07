// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance;

import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerGroupTest extends ContainerInstanceManagementTest {
    @Test
    public void testContainerGroupWithVirtualNetwork() {
        String containerGroupName = generateRandomResourceName("container", 20);
        Region region = Region.US_EAST;

        ContainerGroup containerGroup =
            containerInstanceManager
                .containerGroups()
                .define(containerGroupName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withoutVolume()
                .withContainerInstance("nginx", 80)
                .withNewVirtualNetwork("10.0.0.0/24")
                .create();

        Assertions.assertNotNull(containerGroup.networkProfileId());
    }
}
