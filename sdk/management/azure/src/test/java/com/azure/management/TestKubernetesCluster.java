/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management;

import com.azure.management.containerservice.ContainerServiceVMSizeTypes;
import com.azure.management.containerservice.KubernetesCluster;
import com.azure.management.containerservice.KubernetesClusters;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;

public class TestKubernetesCluster extends TestTemplate<KubernetesCluster, KubernetesClusters> {
    @Override
    public KubernetesCluster createResource(KubernetesClusters kubernetesClusters) throws Exception {
        final String sshKeyData =  this.getSshKey();

        final String newName = "aks" + kubernetesClusters.manager().getSdkContext().randomResourceName("", 8);
        final String dnsPrefix = "dns" + newName;
        final String agentPoolName = "ap" + newName;
        final String clientId = "clientId";
        final String secret = "secret";

        KubernetesCluster resource = kubernetesClusters.define(newName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup()
            .withLatestVersion()
            .withRootUsername("aksadmin")
            .withSshKey(sshKeyData)
            .withServicePrincipalClientId(clientId)
            .withServicePrincipalSecret(secret)
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .attach()
            .withDnsPrefix(dnsPrefix)
            .withTag("tag1", "value1")
            .create();
        Assertions.assertNotNull("Container service not found.", resource.id());
        Assertions.assertEquals(Region.US_EAST, resource.region());
        Assertions.assertEquals("aksadmin", resource.linuxRootUsername());
        Assertions.assertEquals(1, resource.agentPools().size());
        Assertions.assertNotNull(resource.agentPools().get(agentPoolName));
        Assertions.assertEquals(1, resource.agentPools().get(agentPoolName).count());
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_D2_V2, resource.agentPools().get(agentPoolName).vmSize());
        Assertions.assertTrue(resource.tags().containsKey("tag1"));

        resource = kubernetesClusters.getByResourceGroup(resource.resourceGroupName(), newName);

        byte[] kubeConfigAdmin = resource.adminKubeConfigContent();
        Assertions.assertTrue(kubeConfigAdmin != null && kubeConfigAdmin.length > 0);
        byte[] kubeConfigUser = resource.userKubeConfigContent();
        Assertions.assertTrue(kubeConfigUser != null && kubeConfigUser.length > 0);

        return resource;
    }

    @Override
    public KubernetesCluster updateResource(KubernetesCluster resource) throws Exception {
        String agentPoolName = new ArrayList<>(resource.agentPools().keySet()).get(0);
        // Modify existing container service
        resource =  resource.update()
            .withAgentPoolVirtualMachineCount(agentPoolName, 5)
            .withTag("tag2", "value2")
            .withTag("tag3", "value3")
            .withoutTag("tag1")
            .apply();

        Assertions.assertEquals(1, resource.agentPools().size());
        Assertions.assertTrue( resource.agentPools().get(agentPoolName).count() == 5, "Agent pool count was not updated.");
        Assertions.assertTrue(resource.tags().containsKey("tag2"));
        Assertions.assertTrue(!resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public void print(KubernetesCluster resource) {
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
