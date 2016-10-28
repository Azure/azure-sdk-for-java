package com.microsoft.azure.management;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestVirtualMachineCustomData extends TestTemplate<VirtualMachine, VirtualMachines> {
    final PublicIpAddresses pips;

    public TestVirtualMachineCustomData(PublicIpAddresses pips) {
        this.pips = pips;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        final String publicIpDnsLabel = ResourceNamer.randomResourceName("abc", 16);

        // Prepare the custom data
        //
        String cloudInitFilePath = getClass().getClassLoader().getResource("cloud-init").getPath();
        cloudInitFilePath = cloudInitFilePath.replaceFirst("^/(.:/)", "$1"); // In Windows remove leading slash
        byte[] cloudInitAsBytes = Files.readAllBytes(Paths.get(cloudInitFilePath));
        byte[] cloudInitEncoded = Base64.encodeBase64(cloudInitAsBytes);
        String cloudInitEncodedString = new String(cloudInitEncoded);

        PublicIpAddress pip = pips.define(publicIpDnsLabel)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withLeafDomainLabel(publicIpDnsLabel)
                .create();

        VirtualMachine vm = virtualMachines.define(vmName)
                .withRegion(pip.regionName())
                .withExistingResourceGroup(pip.resourceGroupName())
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withExistingPrimaryPublicIpAddress(pip)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword("12NewPA$$w0rd!")
                .withCustomData(cloudInitEncodedString)
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();

        pip.refresh();
        Assert.assertTrue(pip.hasAssignedNetworkInterface());

        JSch jsch= new JSch();
        Session session = null;
        ChannelExec channel = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session=jsch.getSession("testuser",  publicIpDnsLabel + "." + "eastus.cloudapp.azure.com", 22);
            session.setPassword("12NewPA$$w0rd!");
            session.setConfig(config);
            session.connect();

            // Try running the package installed via init script
            //
            channel = (ChannelExec) session.openChannel("exec");
            BufferedReader in=new BufferedReader(new InputStreamReader(channel.getInputStream()));
            channel.setCommand("pwgen;");
            channel.connect();

            String msg;
            while((msg = in.readLine()) != null){
                Assert.assertFalse(msg.startsWith("The program 'pwgen' is currently not installed"));
            }
        } catch (Exception e) {
            Assert.fail("SSH connection failed" + e.getMessage());
        }finally {
            if (channel != null) {
                channel.disconnect();
            }

            if(session != null) {
                session.disconnect();
            }
        }
        return vm;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        return virtualMachine;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
