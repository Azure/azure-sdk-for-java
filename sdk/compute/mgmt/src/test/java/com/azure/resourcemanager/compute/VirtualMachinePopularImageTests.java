// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class VirtualMachinePopularImageTests extends ComputeManagementTest {
    private final String rgName = generateRandomResourceName("rg", 10);

    public VirtualMachinePopularImageTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void canCreateAllPopularImageVM() {
        List<Flux<Indexable>> vmFluxes = new ArrayList<>();
        for (KnownWindowsVirtualMachineImage image : KnownWindowsVirtualMachineImage.values()) {
            Flux<Indexable> flux = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm", 10))
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/24")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(image)
                .withAdminUsername("testUser")
                .withAdminPassword(password())
                .withSize(VirtualMachineSizeTypes.STANDARD_B1S)
                .createAsync();
            vmFluxes.add(flux);
        }

        for (KnownLinuxVirtualMachineImage image : KnownLinuxVirtualMachineImage.values()) {
            Flux<Indexable> flux = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm", 10))
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/24")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(image)
                .withRootUsername("testUser")
                .withRootPassword(password())
                .withSize(VirtualMachineSizeTypes.STANDARD_B1S)
                .createAsync();
            vmFluxes.add(flux);
        }

        Flux.merge(vmFluxes).blockLast();
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }
}
