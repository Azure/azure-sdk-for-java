// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.containerinstance.models.Container;
import com.azure.resourcemanager.containerinstance.models.ContainerAttachResult;
import com.azure.resourcemanager.containerinstance.models.ContainerExec;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.core.management.Region;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupRestartPolicy;
import com.azure.resourcemanager.containerinstance.models.ContainerHttpGet;
import com.azure.resourcemanager.containerinstance.models.ContainerProbe;
import com.azure.resourcemanager.containerinstance.models.ContainerState;
import com.azure.resourcemanager.containerinstance.models.Scheme;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;

public class ContainerGroupTest extends ContainerInstanceManagementTest {
    @Test
    public void testContainerGroupWithVirtualNetwork() {
        String containerGroupName = generateRandomResourceName("container", 20);
        Region region = Region.US_EAST;

        ContainerGroup containerGroup =
            containerInstanceManager
                .containerGroups()
                .define(containerGroupName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withoutVolume()
                .withContainerInstance("nginx", 80)
                .withNewVirtualNetwork("10.0.0.0/24")
                .create();

        Assertions.assertEquals(1, containerGroup.subnetIds().size());

        containerInstanceManager.containerGroups().deleteById(containerGroup.id());

        final String subnetName = "default";
        final String containerGroupName1 = generateRandomResourceName("container", 20);

        Network vnet = containerInstanceManager.networkManager().networks().define("vnet1")
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.1.0.0/24")
            .defineSubnet(subnetName)
                .withAddressPrefix("10.1.0.0/24")
                .withDelegation("Microsoft.ContainerInstance/containerGroups")
                .attach()
            .create();

        ContainerGroup containerGroup1 = containerInstanceManager.containerGroups().define(containerGroupName1)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withLinux()
            .withPublicImageRegistryOnly()
            .withoutVolume()
            .withContainerInstance("nginx", 80)
            .withExistingSubnet(vnet.subnets().get(subnetName))
            .create();

        Assertions.assertEquals(1, containerGroup1.subnetIds().size());
        Assertions.assertEquals(subnetName, containerGroup1.subnetIds().iterator().next().name());
        Assertions.assertEquals(vnet.subnets().get(subnetName).id(), containerGroup1.subnetIds().iterator().next().id());
    }

    @Test
    @DoNotRecord(skipInPlayback = true) // response contains secret
    public void testContainerOperation() {
        String containerGroupName = generateRandomResourceName("container", 20);
        String dnsPrefix = generateRandomResourceName("aci-dns", 20);
        Region region = Region.US_EAST;

        ContainerGroup containerGroup =
            containerInstanceManager
                .containerGroups()
                .define(containerGroupName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withoutVolume()
                .withContainerInstance("mcr.microsoft.com/azuredocs/aci-helloworld", 80)
                .withDnsPrefix(dnsPrefix)
                .create();
        Assertions.assertTrue(containerGroup.fqdn().startsWith(dnsPrefix));

        Container container = containerGroup.containers().values().iterator().next();

        ContainerAttachResult attachResult = containerGroup.attachOutputStream(container);
        Assertions.assertNotNull(attachResult.webSocketUri());
        Assertions.assertNotNull(attachResult.password());
    }

    @Test
    public void testCreateWithLivenessAndReadiness() {
        String containerGroupName = generateRandomResourceName("container", 20);
        String dnsPrefix = generateRandomResourceName("aci-dns", 20);
        Region region = Region.US_EAST;

        String containerName1 = generateRandomResourceName("container", 20);
        String containerName2 = generateRandomResourceName("container", 20);

        int healthySeconds = 30;
        int failureThreshold = 3;
        int probePeriodSeconds = 10;

        ContainerGroup containerGroup =
            containerInstanceManager
                .containerGroups()
                .define(containerGroupName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withoutVolume()
                .defineContainerInstance(containerName1)
                    .withImage("mcr.microsoft.com/azuredocs/aci-helloworld")
                    .withExternalTcpPort(80)
                    // simulate the situation where, container starts healthy for a given period of time, and then becomes unhealthy
                    .withStartingCommandLine("/bin/sh", "-c", "touch /tmp/healthy; sleep " + healthySeconds + "; rm -rf /tmp/healthy; sleep 600;")
                    .withLivenessProbeExecutionCommand(Arrays.asList("cat", "/tmp/healthy"), probePeriodSeconds, failureThreshold)
                    .withReadinessProbeHttpGet("/mypath", 80, 30, 3)
                .attach()
                .defineContainerInstance(containerName2)
                    .withImage("mcr.microsoft.com/azuredocs/aci-helloworld")
                    .withExternalTcpPort(8080)
                    .withLivenessProbe(
                        new ContainerProbe()
                            .withExec(
                                new ContainerExec()
                                    .withCommand(Arrays.asList("/bin/bash", "myCustomScript2.sh")))
                            .withPeriodSeconds(30)
                            .withFailureThreshold(2))
                    .withReadinessProbe(
                        new ContainerProbe()
                            .withHttpGet(
                                new ContainerHttpGet()
                                    .withPath("/")
                                    .withPort(8080)
                                    .withScheme(Scheme.HTTP))
                            .withPeriodSeconds(30)
                            .withInitialDelaySeconds(0))
                    .attach()
                .withDnsPrefix(dnsPrefix)
                .withRestartPolicy(ContainerGroupRestartPolicy.ALWAYS)
                .create();

        Assertions.assertEquals(0, containerGroup.containers().get(containerName1).instanceView().restartCount());
        Assertions.assertNull(containerGroup.containers().get(containerName1).instanceView().previousState());

        Assertions.assertNotNull(containerGroup.containers().get(containerName1).livenessProbe());
        Assertions.assertNotNull(containerGroup.containers().get(containerName1).readinessProbe());

        Assertions.assertNotNull(containerGroup.containers().get(containerName2).livenessProbe());
        Assertions.assertNotNull(containerGroup.containers().get(containerName2).readinessProbe());

        // wait for probe exhaustion and container restart
        while (containerGroup.containers().get(containerName1).instanceView().restartCount() == 0) {
            containerGroup.refresh();
            ResourceManagerUtils.sleep(Duration.ofSeconds(probePeriodSeconds / 2));
        }

        Container container = containerGroup.containers().get(containerName1);
        ContainerState previousState = container.instanceView().previousState();
        Assertions.assertNotNull(previousState);

        Duration durationBeforeRestart = Duration.ofSeconds(previousState.finishTime().toEpochSecond() - previousState.startTime().toEpochSecond());

        // duration before restart should be in between healthy duration plus last probe and healthy duration plus latest probe
        Assertions.assertTrue(durationBeforeRestart.compareTo(Duration.ofSeconds(healthySeconds + probePeriodSeconds * (failureThreshold - 1))) > 0);
        Assertions.assertTrue(durationBeforeRestart.compareTo(Duration.ofSeconds(healthySeconds + probePeriodSeconds * failureThreshold)) <= 0);
    }
}
