package com.microsoft.azure.management;

import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class TestContainerInstance extends TestTemplate<ContainerGroup, ContainerGroups> {

    @Override
    public ContainerGroup createResource(ContainerGroups containerGroups) throws Exception {
        final String newName = "aci" + this.testId;
        ContainerGroup containerGroup = containerGroups.define(newName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup()
            .withLinux()
            .defineVolume("some volume")
                .withNewAzureFileShare("some share")
                .attach()
            .defineContainerInstance("some name")
                .withImage("some image")
                .withCPUCoreCount(1)
                .withMemorySizeInGB(1.5)
                .withExternalTcpPort(8080)
                .attach()
            .create();

        return containerGroup;
    }

    @Override
    public ContainerGroup updateResource(ContainerGroup containerGroup) throws Exception {
        return containerGroup;
    }

    @Override
    public void print(ContainerGroup resource) {
        System.out.println(new StringBuilder().append("Container Group: ").append(resource.id())
            .append("Name: ").append(resource.name())
            .append("\n\tResource group: ").append(resource.resourceGroupName())
            .append("\n\tRegion: ").append(resource.region())
            .append("\n\tTags: ").append(resource.tags())
            .toString());
    }
}