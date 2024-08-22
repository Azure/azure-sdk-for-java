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
    private Region region = Region.US_WEST2;

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canCreateAllPopularImageVM() {
        if (skipInPlayback()) {
            return;
        }

        rgName = generateRandomResourceName("rg", 10);
        List<Mono<VirtualMachine>> vmMonos = new ArrayList<>();
        for (KnownWindowsVirtualMachineImage image : Arrays.stream(KnownWindowsVirtualMachineImage.values())
            .filter(image -> image != KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2019_DATACENTER_WITH_CONTAINERS_GEN2
                && image != KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2019_DATACENTER_WITH_CONTAINERS
                && image != KnownWindowsVirtualMachineImage.WINDOWS_DESKTOP_10_20H1_PRO)
            .collect(Collectors.toList())) {
            Mono<VirtualMachine> mono = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm", 10))
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/24")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(image)
                .withAdminUsername("testUser")
                .withAdminPassword(password())
                .withSize(VirtualMachineSizeTypes.STANDARD_D2S_V3)
                .createAsync();
            vmMonos.add(mono);
        }

        for (KnownLinuxVirtualMachineImage image : Arrays.stream(KnownLinuxVirtualMachineImage.values())
            .filter(image -> image != KnownLinuxVirtualMachineImage.OPENSUSE_LEAP_15_1
                && image != KnownLinuxVirtualMachineImage.SLES_15_SP1
                && image != KnownLinuxVirtualMachineImage.ORACLE_LINUX_8_1)
            .collect(Collectors.toList())) {

            Mono<VirtualMachine> mono = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm", 10))
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/24")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(image)
                .withRootUsername("testUser")
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.STANDARD_D2S_V3)
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
