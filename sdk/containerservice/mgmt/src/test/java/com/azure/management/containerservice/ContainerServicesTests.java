/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerservice;

import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerServicesTests extends ContainerServiceManagementTest {
    private static final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.Com";

    @Test
    public void canCRUDContainerServices() throws Exception {
        String containerServiceName = sdkContext.randomResourceName("acs", 15);
        String agentPoolName = sdkContext.randomResourceName("ap0", 15);
        String dnsPrefix = sdkContext.randomResourceName("dns", 15);

        // create
        ContainerService containerService = containerServiceManager
            .containerServices()
            .define(containerServiceName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(RG_NAME)
            .withDcosOrchestration()
            .withLinux()
            .withRootUsername("testacs")
            .withSshKey(sshKey)
            .withMasterNodeCount(ContainerServiceMasterProfileCount.MIN)
            .defineAgentPool(agentPoolName)
                .withVirtualMachineCount(1)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A1)
                .withDnsPrefix("ap0" + dnsPrefix)
                .attach()
            .withMasterDnsPrefix("mp0" + dnsPrefix)
            .withDiagnostics()
            .withTag("tag1", "vaule1")
            .create();
    
        Assertions.assertNotNull(containerService.id());
        Assertions.assertEquals(Region.US_EAST, containerService.region());
        Assertions.assertEquals(ContainerServiceMasterProfileCount.MIN.count(), containerService.masterNodeCount());
        Assertions.assertEquals("testacs", containerService.linuxRootUsername());
        Assertions.assertEquals(1, containerService.agentPools().size());
        Assertions.assertNotNull(containerService.agentPools().get(agentPoolName));
        Assertions.assertEquals(1, containerService.agentPools().get(agentPoolName).count());
        Assertions.assertEquals("ap0" + dnsPrefix, containerService.agentPools().get(agentPoolName).dnsPrefix());
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_A1, containerService.agentPools().get(agentPoolName).vmSize());
        Assertions.assertEquals(ContainerServiceOrchestratorTypes.DCOS, containerService.orchestratorType());
        Assertions.assertTrue(containerService.isDiagnosticsEnabled());
        Assertions.assertNotNull(containerService.tags().get("tag1"));

        // update
        containerService = containerService.update()
            .withAgentVirtualMachineCount(5)
            .withTag("tag2", "value2")
            .withTag("tag3", "value3")
            .withoutTag("tag1")
            .apply();

        Assertions.assertEquals(1, containerService.agentPools().size());
        Assertions.assertEquals(5, containerService.agentPools().get(agentPoolName).count());
        Assertions.assertNotNull(containerService.tags().get("tag2"));
        Assertions.assertTrue(!containerService.tags().containsKey("tag1"));
    }
}