/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class TestContainerService extends TestTemplate<ContainerService, ContainerServices> {

    @Override
    public ContainerService createResource(ContainerServices containerServices) throws Exception {
        final String newName = "as" + this.testId;
        final String dnsPrefix = "dns" + newName;
        ContainerService containerService = containerServices.define(newName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withMasterProfile(1, dnsPrefix)
                .withLinuxProfile("testUserName", "needSSHKey")
                .defineContainerServiceAgentPoolProfile("agentPool" + newName)
                .withCount(1)
                .withVmSize(ContainerServiceVMSizeTypes.STANDARD_A1)
                .withDnsPrefix(dnsPrefix)
                .attach()
                .create();
        return containerService;
    }

    @Override
    public ContainerService updateResource(ContainerService resource) throws Exception {
        // Modify existing availability set
        resource =  resource.update()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertTrue(!resource.tags().containsKey("tag1"));
        return null;
    }

    @Override
    public void print(ContainerService resource) {
        System.out.println(new StringBuilder().append("Availability Set: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .toString());
    }
}
