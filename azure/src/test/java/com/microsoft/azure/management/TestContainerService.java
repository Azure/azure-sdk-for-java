/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

public class TestContainerService extends TestTemplate<ContainerService, ContainerServices> {

    @Override
    public ContainerService createResource(ContainerServices containerServices) throws Exception {
        final String sshKeyData =  this.getSSHKey();

        final String newName = "as" + this.testId;
        final String dnsPrefix = "dns" + newName;
        ContainerService containerService = containerServices.define(newName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withMasterProfile(ContainerServiceMasterProfileCount.MIN, "mp1" + dnsPrefix)
                .withLinuxProfile()
                .withRootUsername("testUserName")
                .withSshKey(sshKeyData)
                .defineContainerServiceAgentPoolProfile("agentPool0" + newName)
                .withCount(1)
                .withVMSize(ContainerServiceVMSizeTypes.STANDARD_A1)
                .withDnsLabel("ap0" + dnsPrefix)
                .attach()
                .withDCOSOrchestration()
                .withDiagnostics()
                .withTag("tag1", "value1")
                .create();
        return containerService;
    }

    @Override
    public ContainerService updateResource(ContainerService resource) throws Exception {
        // Modify existing container service
        final String newName = "as" + this.testId;
        resource =  resource.update()
                .withAgentPoolCount(5)
                .withoutDiagnostics()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
        Assert.assertTrue("Agent pool count was not updated.", resource.agentPoolProfile().count() == 5);
        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertTrue(!resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public void print(ContainerService resource) {
        System.out.println(new StringBuilder().append("Container Service: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .toString());
    }

    private String getSSHKey() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair=keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey=(RSAPublicKey)keyPair.getPublic();
        ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteOs);
        dos.writeInt("ssh-rsa".getBytes().length);
        dos.write("ssh-rsa".getBytes());
        dos.writeInt(publicKey.getPublicExponent().toByteArray().length);
        dos.write(publicKey.getPublicExponent().toByteArray());
        dos.writeInt(publicKey.getModulus().toByteArray().length);
        dos.write(publicKey.getModulus().toByteArray());
        String publicKeyEncoded = new String(
                Base64.encodeBase64(byteOs.toByteArray()));
        return "ssh-rsa " + publicKeyEncoded + " ";
    }
}