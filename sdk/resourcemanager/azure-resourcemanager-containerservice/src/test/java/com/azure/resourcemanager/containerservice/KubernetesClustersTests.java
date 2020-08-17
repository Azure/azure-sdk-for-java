// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KubernetesClustersTests extends ContainerServiceManagementTest {
    private static final String SSH_KEY =
        "ssh-rsa"
            + " AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD"
            + " azjava@javalib.Com";

    @Test
    public void canCRUDKubernetesCluster() throws Exception {
        String aksName = sdkContext.randomResourceName("aks", 15);
        String dnsPrefix = sdkContext.randomResourceName("dns", 10);
        String agentPoolName = sdkContext.randomResourceName("ap0", 10);
        String servicePrincipalClientId = "spId";
        String servicePrincipalSecret = "spSecret";

        // aks can use another azure auth rather than original client auth to access azure service.
        // Thus, set it to AZURE_AUTH_LOCATION_2 when you want.
        String envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION_2");
        if (envSecondaryServicePrincipal == null
            || envSecondaryServicePrincipal.isEmpty()
            || !(new File(envSecondaryServicePrincipal).exists())) {
            envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION");
        }

        if (!isPlaybackMode()) {
            HashMap<String, String> credentialsMap = parseAuthFile(envSecondaryServicePrincipal);
            servicePrincipalClientId = credentialsMap.get("clientId");
            servicePrincipalSecret = credentialsMap.get("clientSecret");
        }

        // create
        KubernetesCluster kubernetesCluster =
            containerServiceManager
                .kubernetesClusters()
                .define(aksName)
                .withRegion(Region.US_CENTRAL)
                .withExistingResourceGroup(rgName)
                .withLatestVersion()
                .withRootUsername("testaks")
                .withSshKey(SSH_KEY)
                .withServicePrincipalClientId(servicePrincipalClientId)
                .withServicePrincipalSecret(servicePrincipalSecret)
                .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .attach()
                .withDnsPrefix("mp1" + dnsPrefix)
                .withTag("tag1", "value1")
                .create();

        Assertions.assertNotNull(kubernetesCluster.id());
        Assertions.assertEquals(Region.US_CENTRAL, kubernetesCluster.region());
        Assertions.assertEquals("testaks", kubernetesCluster.linuxRootUsername());
        Assertions.assertEquals(1, kubernetesCluster.agentPools().size());
        Assertions.assertNotNull(kubernetesCluster.agentPools().get(agentPoolName));
        Assertions.assertEquals(1, kubernetesCluster.agentPools().get(agentPoolName).count());
        Assertions
            .assertEquals(
                ContainerServiceVMSizeTypes.STANDARD_D2_V2, kubernetesCluster.agentPools().get(agentPoolName).vmSize());
        Assertions
            .assertEquals(
                AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS, kubernetesCluster.agentPools().get(agentPoolName).type());
        Assertions.assertNotNull(kubernetesCluster.tags().get("tag1"));

        // update
        kubernetesCluster =
            kubernetesCluster
                .update()
                .withAgentPoolVirtualMachineCount(agentPoolName, 5)
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();

        Assertions.assertEquals(1, kubernetesCluster.agentPools().size());
        Assertions.assertEquals(5, kubernetesCluster.agentPools().get(agentPoolName).count());
        Assertions.assertNotNull(kubernetesCluster.tags().get("tag2"));
        Assertions.assertTrue(!kubernetesCluster.tags().containsKey("tag1"));
    }

    /**
     * Parse azure auth to hashmap
     *
     * @param authFilename the azure auth location
     * @return all fields in azure auth json
     * @throws Exception exception
     */
    private static HashMap<String, String> parseAuthFile(String authFilename) throws Exception {
        String content = new String(Files.readAllBytes(new File(authFilename).toPath()), StandardCharsets.UTF_8).trim();
        HashMap<String, String> auth = new HashMap<>();
        if (isJsonBased(content)) {
            auth = new JacksonAdapter().deserialize(content, auth.getClass(), SerializerEncoding.JSON);
        } else {
            Properties authSettings = new Properties();
            FileInputStream credentialsFileStream = new FileInputStream(new File(authFilename));
            authSettings.load(credentialsFileStream);
            credentialsFileStream.close();

            for (final String authName : authSettings.stringPropertyNames()) {
                auth.put(authName, authSettings.getProperty(authName));
            }
        }
        return auth;
    }

    private static boolean isJsonBased(String content) {
        return content.startsWith("{");
    }
}
