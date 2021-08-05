// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIpAddresses;
import com.azure.core.management.Region;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Assertions;

public class TestVirtualMachineSsh extends TestTemplate<VirtualMachine, VirtualMachines> {
    final PublicIpAddresses pips;

    public TestVirtualMachineSsh(PublicIpAddresses pips) {
        this.pips = pips;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vm", 10);
        final String password = ResourceManagerTestBase.password();
        final String sshKey =
            "ssh-rsa"
                + " AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD"
                + " azjava@javalib.com";
        final String publicIpDnsLabel = vmName;
        PublicIpAddress pip =
            pips
                .define(publicIpDnsLabel)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withLeafDomainLabel(publicIpDnsLabel)
                .create();

        VirtualMachine vm =
            virtualMachines
                .define(vmName)
                .withRegion(pip.regionName())
                .withExistingResourceGroup(pip.resourceGroupName())
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(pip)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword(ResourceManagerTestBase.password())
                .withSsh(sshKey)
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

        pip.refresh();
        Assertions.assertTrue(pip.hasAssignedNetworkInterface());

        Assertions.assertNotNull(vm.innerModel().osProfile().linuxConfiguration().ssh());
        Assertions.assertTrue(vm.innerModel().osProfile().linuxConfiguration().ssh().publicKeys().size() > 0);

        JSch jsch = new JSch();
        Session session = null;
        if (TestUtils.isRecordMode()) {
            try {
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                // jsch.addIdentity(sshFile, filePassword);
                session = jsch.getSession("testuser", publicIpDnsLabel + "." + "eastus.cloudapp.azure.com", 22);
                session.setPassword(password);
                session.setConfig(config);
                session.connect();
            } catch (Exception e) {
                Assertions.fail("SSH connection failed: " + e.getMessage());
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
        }
        return vm;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        // Updating Ssh public keys are not supported in Azure
        //
        return virtualMachine;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
