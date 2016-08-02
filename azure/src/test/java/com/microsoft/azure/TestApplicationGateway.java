/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import com.microsoft.azure.management.network.*;
import org.junit.Assert;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Test of application gateway management.
 */
public class TestApplicationGateway extends TestTemplate<ApplicationGateway, ApplicationGateways> {

    private final Networks networks;

    public TestApplicationGateway(Networks networks) {
        this.networks = networks;
    }

    @Override
    public ApplicationGateway createResource(ApplicationGateways applicationGateways) throws Exception {
        final String newName = "ag" + this.testId;
        Region region = Region.US_WEST;
        String networkName = "net" + this.testId;
        String groupName = "rg" + this.testId;

        Network vnet = networks.define(networkName)
                .withRegion(region)
                .withNewResourceGroup(groupName)
                .withAddressSpace("10.0.0.0/28")
                .create();

        // Create an application gateway
        return applicationGateways.define(newName)
                .withRegion(region)
                .withNewResourceGroup(groupName)
                .withSku(ApplicationGatewaySkuName.STANDARD_SMALL)
                .withCapacity(2)
                .withExistingNetwork(vnet)
                .withNewPublicIpAddress()
                .withFrontendPort(80)
                .withBackendAddressPool()
                .withBackendHttpSettings()
                .withHttpListener()
                .create();
    }

    @Override
    public ApplicationGateway updateResource(ApplicationGateway resource) throws Exception {
        resource =  resource.update()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));

        return resource;
    }

    @Override
    public void print(ApplicationGateway resource) {
        StringBuilder info = new StringBuilder();
        info.append("Application Gateway: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());

        System.out.println(info.toString());
    }
}