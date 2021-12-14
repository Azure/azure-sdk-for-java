// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class to test ResourceId class.
 */
public class ResourceIdTests {


    @Test
    public void resourceIdForTopLevelResourceWorksFine() {
        ResourceId resourceId = ResourceId.fromString("/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/resourceGroupName/providers/Microsoft.Network/applicationGateways/something");

        Assertions.assertEquals(resourceId.name(), "something");
        Assertions.assertEquals(resourceId.subscriptionId(), "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef");
        Assertions.assertEquals(resourceId.resourceGroupName(), "resourceGroupName");
        Assertions.assertEquals(resourceId.providerNamespace(), "Microsoft.Network");
        Assertions.assertEquals(resourceId.resourceType(), "applicationGateways");
        Assertions.assertEquals(resourceId.fullResourceType(), "Microsoft.Network/applicationGateways");
        Assertions.assertNull(resourceId.parent());
    }

    @Test
    public void resourceIdForChildLevelResourceWorksFine() {
        ResourceId resourceId = ResourceId.fromString("/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/resourceGroupName/providers/Microsoft.Network/applicationGateways/something/someChildType/childName");

        Assertions.assertEquals(resourceId.name(), "childName");
        Assertions.assertEquals(resourceId.subscriptionId(), "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef");
        Assertions.assertEquals(resourceId.resourceGroupName(), "resourceGroupName");
        Assertions.assertEquals(resourceId.providerNamespace(), "Microsoft.Network");
        Assertions.assertEquals(resourceId.resourceType(), "someChildType");
        Assertions.assertEquals(resourceId.fullResourceType(), "Microsoft.Network/applicationGateways/someChildType");
        Assertions.assertNotNull(resourceId.parent());
        Assertions.assertEquals(resourceId.parent().name(), "something");
        Assertions.assertEquals(resourceId.parent().subscriptionId(), "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef");
        Assertions.assertEquals(resourceId.parent().resourceGroupName(), "resourceGroupName");
        Assertions.assertEquals(resourceId.parent().name(), "something");
        Assertions.assertEquals(resourceId.parent().providerNamespace(), "Microsoft.Network");
        Assertions.assertEquals(resourceId.parent().resourceType(), "applicationGateways");
        Assertions.assertEquals(resourceId.parent().fullResourceType(), "Microsoft.Network/applicationGateways");
    }

    @Test
    public void resourceIdForGrandChildLevelResourceWorksFine() {
        ResourceId resourceId = ResourceId.fromString("/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/resourceGroupName/providers/Microsoft.Network/applicationGateways/something/someChildType/childName/grandChildType/grandChild");

        Assertions.assertEquals(resourceId.name(), "grandChild");
        Assertions.assertEquals(resourceId.subscriptionId(), "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef");
        Assertions.assertEquals(resourceId.resourceGroupName(), "resourceGroupName");
        Assertions.assertEquals(resourceId.providerNamespace(), "Microsoft.Network");
        Assertions.assertEquals(resourceId.resourceType(), "grandChildType");
        Assertions.assertEquals(resourceId.fullResourceType(), "Microsoft.Network/applicationGateways/someChildType/grandChildType");
        Assertions.assertNotNull(resourceId.parent());
        Assertions.assertEquals(resourceId.parent().name(), "childName");
        Assertions.assertEquals(resourceId.parent().subscriptionId(), "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef");
        Assertions.assertEquals(resourceId.parent().resourceGroupName(), "resourceGroupName");
        Assertions.assertEquals(resourceId.parent().providerNamespace(), "Microsoft.Network");
        Assertions.assertEquals(resourceId.parent().resourceType(), "someChildType");
        Assertions.assertEquals(resourceId.parent().fullResourceType(), "Microsoft.Network/applicationGateways/someChildType");
        Assertions.assertNotNull(resourceId.parent().parent());
        Assertions.assertEquals(resourceId.parent().parent().name(), "something");
        Assertions.assertEquals(resourceId.parent().parent().subscriptionId(), "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef");
        Assertions.assertEquals(resourceId.parent().parent().resourceGroupName(), "resourceGroupName");
        Assertions.assertEquals(resourceId.parent().parent().name(), "something");
        Assertions.assertEquals(resourceId.parent().parent().providerNamespace(), "Microsoft.Network");
        Assertions.assertEquals(resourceId.parent().parent().resourceType(), "applicationGateways");
        Assertions.assertEquals(resourceId.parent().parent().fullResourceType(), "Microsoft.Network/applicationGateways");
    }
}
