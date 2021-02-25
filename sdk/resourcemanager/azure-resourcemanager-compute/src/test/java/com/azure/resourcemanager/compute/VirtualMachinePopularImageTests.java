// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VirtualMachinePopularImageTests extends ComputeManagementTest {
    private String rgName = "";

    @Test
    @DoNotRecord
    public void canCreateAllPopularImageVM() {
        if (skipInPlayback()) {
            return;
        }

        rgName = generateRandomResourceName("rg", 10);
        List<Mono<VirtualMachine>> vmMonos = new ArrayList<>();
        for (KnownWindowsVirtualMachineImage image : KnownWindowsVirtualMachineImage.values()) {
            Mono<VirtualMachine> mono = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm", 10))
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/24")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(image)
                .withAdminUsername("testUser")
                .withAdminPassword(password())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .createAsync();
            vmMonos.add(mono);
        }

        for (KnownLinuxVirtualMachineImage image : Arrays.stream(KnownLinuxVirtualMachineImage.values())
            .filter(image -> image != KnownLinuxVirtualMachineImage.OPENSUSE_LEAP_15_1 && image != KnownLinuxVirtualMachineImage.SLES_15_SP1)
            .collect(Collectors.toList())) {

            Mono<VirtualMachine> mono = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm", 10))
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/24")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(image)
                .withRootUsername("testUser")
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .createAsync();
            vmMonos.add(mono);
        }

        Flux.merge(vmMonos).blockLast();
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }
}
