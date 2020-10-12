// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.containerinstance.models.Container;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupRestartPolicy;
import com.azure.resourcemanager.containerinstance.models.ContainerGroups;
import com.azure.resourcemanager.containerinstance.models.ContainerPort;
import com.azure.resourcemanager.containerinstance.models.EnvironmentVariable;
import com.azure.resourcemanager.containerinstance.models.Operation;
import com.azure.resourcemanager.containerinstance.models.ResourceIdentityType;
import com.azure.resourcemanager.containerinstance.models.Volume;
import com.azure.resourcemanager.containerinstance.models.VolumeMount;
import com.azure.core.management.Region;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;

public class TestContainerInstanceWithPrivateIpAddress extends TestTemplate<ContainerGroup, ContainerGroups> {

    @Override
    public ContainerGroup createResource(ContainerGroups containerGroups) throws Exception {
        final String cgName = containerGroups.manager().resourceManager().internalContext().randomResourceName("aci", 10);
        final String rgName = containerGroups.manager().resourceManager().internalContext().randomResourceName("rgaci", 10);

        final String logAnalyticsWorkspaceId = "REPLACE WITH YOUR LOG ANALYTICS WORKSPACE ID";
        final String logAnalyticsWorkspaceKey = "REPLACE WITH YOUR LOG ANALYTICS WORKSPACE KEY";
        final String networkProfileSubscriptionId = "REPLACE WITH YOUR NETWORK PROFILE SUBSCRIPTION ID";
        final String networkProfileResourceGroupName = "REPLACE WITH YOUR NETWORK PROFILE RESOURCE GROUP NAME";
        final String networkProfileName = "REPLEACE WITH YOUR NETWORK PROFILE NAME";
        final List<String> dnsServerNames = new ArrayList<String>();
        dnsServerNames.add("dnsServer1");

        List<String> dnsServers = new ArrayList<String>();
        dnsServers.add("dnsServer1");
        ContainerGroup containerGroup =
            containerGroups
                .define(cgName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withEmptyDirectoryVolume("emptydir1")
                .defineContainerInstance("tomcat")
                .withImage("tomcat")
                .withExternalTcpPort(8080)
                .withCpuCoreCount(1)
                .withEnvironmentVariable("ENV1", "value1")
                .attach()
                .defineContainerInstance("nginx")
                .withImage("nginx")
                .withExternalTcpPort(80)
                .withEnvironmentVariableWithSecuredValue("ENV2", "securedValue1")
                .attach()
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .withRestartPolicy(ContainerGroupRestartPolicy.NEVER)
                .withLogAnalytics(logAnalyticsWorkspaceId, logAnalyticsWorkspaceKey)
                .withExistingNetworkProfile(networkProfileSubscriptionId, networkProfileResourceGroupName, networkProfileName)
                .withDnsConfiguration(dnsServerNames, "dnsSearchDomains", "dnsOptions")
                .withTag("tag1", "value1")
                .create();

        Assertions.assertEquals(cgName, containerGroup.name());
        Assertions.assertEquals("Linux", containerGroup.osType().toString());
        Assertions.assertEquals(0, containerGroup.imageRegistryServers().size());
        Assertions.assertEquals(1, containerGroup.volumes().size());
        Assertions.assertNotNull(containerGroup.volumes().get("emptydir1"));
        Assertions.assertNotNull(containerGroup.ipAddress());
        Assertions.assertTrue(containerGroup.isIPAddressPrivate());
        Assertions.assertEquals(2, containerGroup.externalTcpPorts().length);
        Assertions.assertEquals(2, containerGroup.externalPorts().size());
        Assertions.assertEquals(2, containerGroup.externalTcpPorts().length);
        Assertions.assertEquals(8080, containerGroup.externalTcpPorts()[0]);
        Assertions.assertEquals(80, containerGroup.externalTcpPorts()[1]);
        Assertions.assertEquals(2, containerGroup.containers().size());
        Container tomcatContainer = containerGroup.containers().get("tomcat");
        Assertions.assertNotNull(tomcatContainer);
        Container nginxContainer = containerGroup.containers().get("nginx");
        Assertions.assertNotNull(nginxContainer);
        Assertions.assertEquals("tomcat", tomcatContainer.name());
        Assertions.assertEquals("tomcat", tomcatContainer.image());
        Assertions.assertEquals(1.0, tomcatContainer.resources().requests().cpu(), .1);
        Assertions.assertEquals(1.5, tomcatContainer.resources().requests().memoryInGB(), .1);
        Assertions.assertEquals(1, tomcatContainer.ports().size());
        Assertions.assertEquals(8080, tomcatContainer.ports().get(0).port());
        Assertions.assertNull(tomcatContainer.volumeMounts());
        Assertions.assertNull(tomcatContainer.command());
        Assertions.assertNotNull(tomcatContainer.environmentVariables());
        Assertions.assertEquals(1, tomcatContainer.environmentVariables().size());
        Assertions.assertEquals("nginx", nginxContainer.name());
        Assertions.assertEquals("nginx", nginxContainer.image());
        Assertions.assertEquals(1.0, nginxContainer.resources().requests().cpu(), .1);
        Assertions.assertEquals(1.5, nginxContainer.resources().requests().memoryInGB(), .1);
        Assertions.assertEquals(1, nginxContainer.ports().size());
        Assertions.assertEquals(80, nginxContainer.ports().get(0).port());
        Assertions.assertNull(nginxContainer.volumeMounts());
        Assertions.assertNull(nginxContainer.command());
        Assertions.assertNotNull(nginxContainer.environmentVariables());
        Assertions.assertEquals(1, nginxContainer.environmentVariables().size());
        Assertions.assertTrue(containerGroup.tags().containsKey("tag1"));
        Assertions.assertEquals(ContainerGroupRestartPolicy.NEVER, containerGroup.restartPolicy());
        Assertions.assertTrue(containerGroup.isManagedServiceIdentityEnabled());
        Assertions.assertEquals(ResourceIdentityType.SYSTEM_ASSIGNED, containerGroup.managedServiceIdentityType());
        Assertions.assertEquals(logAnalyticsWorkspaceId, containerGroup.logAnalytics().workspaceId());
        Assertions
            .assertEquals(
                "/subscriptions/"
                    + networkProfileSubscriptionId
                    + "/resourceGroups/"
                    + networkProfileResourceGroupName
                    + "/providers/Microsoft.Network/networkProfiles/"
                    + networkProfileName,
                containerGroup.networkProfileId());
        Assertions.assertEquals("dnsServer1", containerGroup.dnsConfig().nameServers().get(0));
        Assertions.assertEquals("dnsSearchDomains", containerGroup.dnsConfig().searchDomains());
        Assertions.assertEquals("dnsOptions", containerGroup.dnsConfig().options());

        ContainerGroup containerGroup2 = containerGroups.getByResourceGroup(rgName, cgName);

        List<ContainerGroup> containerGroupList =
            containerGroups.listByResourceGroup(rgName).stream().collect(Collectors.toList());
        Assertions.assertTrue(containerGroupList.size() > 0);

        containerGroup.refresh();

        Set<Operation> containerGroupOperations = containerGroups.listOperations().stream().collect(Collectors.toSet());
        // Number of supported operation can change hence don't assert with a predefined number.
        Assertions.assertTrue(containerGroupOperations.size() > 0);

        return containerGroup;
    }

    @Override
    public ContainerGroup updateResource(ContainerGroup containerGroup) throws Exception {
        containerGroup.update().withoutTag("tag1").withTag("tag2", "value2").apply();
        Assertions.assertFalse(containerGroup.tags().containsKey("tag"));
        Assertions.assertTrue(containerGroup.tags().containsKey("tag2"));

        containerGroup.restart();
        containerGroup.stop();

        return containerGroup;
    }

    @Override
    public void print(ContainerGroup resource) {
        StringBuilder info =
            new StringBuilder()
                .append("Container Group: ")
                .append(resource.id())
                .append("Name: ")
                .append(resource.name())
                .append("\n\tResource group: ")
                .append(resource.resourceGroupName())
                .append("\n\tRegion: ")
                .append(resource.region())
                .append("\n\tTags: ")
                .append(resource.tags())
                .append("\n\tOS type: ")
                .append(resource.osType());

        if (resource.ipAddress() != null) {
            info.append("\n\tPublic IP address: ").append(resource.ipAddress());
        }
        if (resource.externalTcpPorts() != null) {
            info.append("\n\tExternal TCP ports:");
            for (int port : resource.externalTcpPorts()) {
                info.append(" ").append(port);
            }
        }
        if (resource.externalUdpPorts() != null) {
            info.append("\n\tExternal UDP ports:");
            for (int port : resource.externalUdpPorts()) {
                info.append(" ").append(port);
            }
        }
        if (resource.imageRegistryServers() != null) {
            info.append("\n\tPrivate Docker image registries:");
            for (String server : resource.imageRegistryServers()) {
                info.append(" ").append(server);
            }
        }
        if (resource.volumes() != null) {
            info.append("\n\tVolume mapping: ");
            for (Map.Entry<String, Volume> entry : resource.volumes().entrySet()) {
                info
                    .append("\n\t\tName: ")
                    .append(entry.getKey())
                    .append(" -> ")
                    .append(
                        entry.getValue().azureFile() != null
                            ? entry.getValue().azureFile().shareName()
                            : "empty direcory volume");
            }
        }
        if (resource.containers() != null) {
            info.append("\n\tContainer instances: ");
            for (Map.Entry<String, Container> entry : resource.containers().entrySet()) {
                Container container = entry.getValue();
                info.append("\n\t\tName: ").append(entry.getKey()).append(" -> ").append(container.image());
                info.append("\n\t\t\tResources: ");
                info.append(container.resources().requests().cpu()).append("CPUs ");
                info.append(container.resources().requests().memoryInGB()).append("GB");
                info.append("\n\t\t\tPorts:");
                for (ContainerPort port : container.ports()) {
                    info.append(" ").append(port.port());
                }
                if (container.volumeMounts() != null) {
                    info.append("\n\t\t\tVolume mounts:");
                    for (VolumeMount volumeMount : container.volumeMounts()) {
                        info.append(" ").append(volumeMount.name()).append("->").append(volumeMount.mountPath());
                    }
                }
                if (container.command() != null) {
                    info.append("\n\t\t\tStart commands:");
                    for (String command : container.command()) {
                        info.append("\n\t\t\t\t").append(command);
                    }
                }
                if (container.environmentVariables() != null) {
                    info.append("\n\t\t\tENV vars:");
                    for (EnvironmentVariable envVar : container.environmentVariables()) {
                        info.append("\n\t\t\t\t").append(envVar.name()).append("=").append(envVar.value());
                    }
                }
            }
        }

        System.out.println(info.toString());
    }
}
