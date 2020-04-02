/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management;


import com.azure.management.containerservice.ContainerService;
import com.azure.management.containerservice.ContainerServiceMasterProfileCount;
import com.azure.management.containerservice.ContainerServiceOrchestratorTypes;
import com.azure.management.containerservice.ContainerServiceVMSizeTypes;
import com.azure.management.containerservice.ContainerServices;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;

public class TestContainerService extends TestTemplate<ContainerService, ContainerServices> {

    @Override
    public ContainerService createResource(ContainerServices containerServices) throws Exception {
        final String sshKeyData =  this.getSshKey();

        final String newName = "as" + containerServices.manager().getSdkContext().randomResourceName("", 8);
        final String dnsPrefix = "dns" + newName;
        ContainerService resource = containerServices.define(newName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withDcosOrchestration()
                .withLinux()
                .withRootUsername("testUserName")
                .withSshKey(sshKeyData)
                .withMasterNodeCount(ContainerServiceMasterProfileCount.MIN)
                .defineAgentPool("agentPool0" + newName)
                    .withVirtualMachineCount(1)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A1)
                    .withDnsPrefix("ap0" + dnsPrefix)
                    .attach()
                .withMasterDnsPrefix("mp1" + dnsPrefix)
                .withDiagnostics()
                .withTag("tag1", "value1")
                .create();
        Assertions.assertNotNull("Container service not found.", resource.id());
        Assertions.assertEquals(resource.region(), Region.US_EAST);
        Assertions.assertEquals(resource.masterNodeCount(), ContainerServiceMasterProfileCount.MIN.count());
        Assertions.assertEquals(resource.linuxRootUsername(), "testUserName");
        Assertions.assertEquals(resource.agentPools().size(), 1);
        Assertions.assertNotNull(resource.agentPools().get("agentPool0" + newName));
        Assertions.assertEquals(resource.agentPools().get("agentPool0" + newName).count(), 1);
        Assertions.assertEquals(resource.agentPools().get("agentPool0" + newName).dnsPrefix(), "ap0" + dnsPrefix);
        Assertions.assertEquals(resource.agentPools().get("agentPool0" + newName).vmSize(), ContainerServiceVMSizeTypes.STANDARD_A1);
        Assertions.assertEquals(resource.orchestratorType(), ContainerServiceOrchestratorTypes.DCOS);
        Assertions.assertTrue(resource.isDiagnosticsEnabled());
        Assertions.assertTrue(resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public ContainerService updateResource(ContainerService resource) throws Exception {
        // Modify existing container service
        resource =  resource.update()
                .withAgentVirtualMachineCount(5)
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();

        Assertions.assertEquals(resource.agentPools().size(), 1);
        String agentPoolName = new ArrayList<>(resource.agentPools().keySet()).get(0);
        Assertions.assertTrue(resource.agentPools().get(agentPoolName).count() == 5, "Agent pool count was not updated.");
        Assertions.assertTrue(resource.tags().containsKey("tag2"));
        Assertions.assertTrue(!resource.tags().containsKey("tag1"));
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

    private String getSshKey() throws Exception {
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
                Base64.getEncoder().encode(byteOs.toByteArray()));
        return "ssh-rsa " + publicKeyEncoded + " ";
    }
}