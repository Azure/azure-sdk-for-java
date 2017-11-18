package com.microsoft.azure.management;

import com.microsoft.azure.management.containerinstance.Container;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroups;
import com.microsoft.azure.management.containerinstance.ContainerPort;
import com.microsoft.azure.management.containerinstance.EnvironmentVariable;
import com.microsoft.azure.management.containerinstance.Volume;
import com.microsoft.azure.management.containerinstance.VolumeMount;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

public class TestContainerInstance extends TestTemplate<ContainerGroup, ContainerGroups> {

    @Override
    public ContainerGroup createResource(ContainerGroups containerGroups) throws Exception {
        final String cgName = "aci" + this.testId;
        final String rgName = "rgaci" + this.testId;
        ContainerGroup containerGroup = containerGroups.define(cgName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName)
            .withLinux()
            .withPublicImageRegistryOnly()
            .withoutVolume()
            .defineContainerInstance("tomcat")
                .withImage("tomcat")
                .withExternalTcpPort(8080)
                .withCpuCoreCount(1)
            .attach()
            .defineContainerInstance("nginx")
                .withImage("nginx")
                .withExternalTcpPort(80)
                .attach()
                .withTag("tag1", "value1")
            .create();

        Assert.assertEquals(cgName, containerGroup.name());
        Assert.assertEquals("Linux", containerGroup.osType().toString());
        Assert.assertEquals(0, containerGroup.imageRegistryServers().size());
        Assert.assertEquals(0, containerGroup.volumes().size());
        Assert.assertNotNull(containerGroup.ipAddress());
        Assert.assertTrue(containerGroup.isIPAddressPublic());
        Assert.assertEquals(2, containerGroup.externalTcpPorts().length);
        Assert.assertEquals(2, containerGroup.externalPorts().size());
        Assert.assertEquals(2, containerGroup.externalTcpPorts().length);
        Assert.assertEquals(8080, containerGroup.externalTcpPorts()[0]);
        Assert.assertEquals(80, containerGroup.externalTcpPorts()[1]);
        Assert.assertEquals(2, containerGroup.containers().size());
        Container tomcatContainer = containerGroup.containers().get("tomcat");
        Assert.assertNotNull(tomcatContainer);
        Container nginxContainer = containerGroup.containers().get("nginx");
        Assert.assertNotNull(nginxContainer);
        Assert.assertEquals("tomcat", tomcatContainer.name());
        Assert.assertEquals("tomcat", tomcatContainer.image());
        Assert.assertEquals(1.0, tomcatContainer.resources().requests().cpu(), .1);
        Assert.assertEquals(1.5, tomcatContainer.resources().requests().memoryInGB(), .1);
        Assert.assertEquals(1, tomcatContainer.ports().size());
        Assert.assertEquals(8080, tomcatContainer.ports().get(0).port());
        Assert.assertNull(tomcatContainer.volumeMounts());
        Assert.assertNull(tomcatContainer.command());
        Assert.assertNotNull(tomcatContainer.environmentVariables());
        Assert.assertEquals(0, tomcatContainer.environmentVariables().size());
        Assert.assertEquals("nginx", nginxContainer.name());
        Assert.assertEquals("nginx", nginxContainer.image());
        Assert.assertEquals(1.0, nginxContainer.resources().requests().cpu(), .1);
        Assert.assertEquals(1.5, nginxContainer.resources().requests().memoryInGB(), .1);
        Assert.assertEquals(1, nginxContainer.ports().size());
        Assert.assertEquals(80, nginxContainer.ports().get(0).port());
        Assert.assertNull(nginxContainer.volumeMounts());
        Assert.assertNull(nginxContainer.command());
        Assert.assertNotNull(nginxContainer.environmentVariables());
        Assert.assertEquals(0, nginxContainer.environmentVariables().size());
        Assert.assertTrue(containerGroup.tags().containsKey("tag1"));

        ContainerGroup containerGroup2 = containerGroups.getByResourceGroup(rgName, cgName);

        List<ContainerGroup> containerGroupList = containerGroups.listByResourceGroup(rgName);

        containerGroup.refresh();

        return containerGroup;
    }

    @Override
    public ContainerGroup updateResource(ContainerGroup containerGroup) throws Exception {
        return containerGroup;
    }

    @Override
    public void print(ContainerGroup resource) {
        StringBuilder info = new StringBuilder().append("Container Group: ").append(resource.id())
            .append("Name: ").append(resource.name())
            .append("\n\tResource group: ").append(resource.resourceGroupName())
            .append("\n\tRegion: ").append(resource.region())
            .append("\n\tTags: ").append(resource.tags())
            .append("\n\tOS type: ").append(resource.osType());

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
            for (Map.Entry<String, Volume> entry: resource.volumes().entrySet()) {
                info.append("\n\t\tName: ").append(entry.getKey()).append(" -> ").append(entry.getValue().azureFile().shareName());
            }
        }
        if (resource.containers() != null) {
            info.append("\n\tContainer instances: ");
            for (Map.Entry<String, Container> entry: resource.containers().entrySet()) {
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