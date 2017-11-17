/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestVirtualMachineSsh extends TestTemplate<VirtualMachine, VirtualMachines> {
    final PublicIPAddresses pips;
    public TestVirtualMachineSsh(PublicIPAddresses pips) {
        this.pips = pips;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";        
        final String publicIpDnsLabel = vmName;
        PublicIPAddress pip = pips.define(publicIpDnsLabel)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup()
            .withLeafDomainLabel(publicIpDnsLabel)
            .create();

        VirtualMachine vm = virtualMachines.define(vmName)
                .withRegion(pip.regionName())
                .withExistingResourceGroup(pip.resourceGroupName())
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(pip)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword("12NewPA$$w0rd!")
                .withSsh(sshKey)
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();

        pip.refresh();
        Assert.assertTrue(pip.hasAssignedNetworkInterface());

        JSch jsch= new JSch();
        Session session = null;
        if (TestBase.isRecordMode()) {
            try {
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                // jsch.addIdentity(sshFile, filePassword);
                session = jsch.getSession("testuser", publicIpDnsLabel + "." + "eastus.cloudapp.azure.com", 22);
                session.setPassword("12NewPA$$w0rd!");
                session.setConfig(config);
                session.connect();
            } catch (Exception e) {
                Assert.fail("SSH connection failed" + e.getMessage());
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }

            Assert.assertNotNull(vm.inner().osProfile().linuxConfiguration().ssh());
            Assert.assertTrue(vm.inner().osProfile().linuxConfiguration().ssh().publicKeys().size() > 0);
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
