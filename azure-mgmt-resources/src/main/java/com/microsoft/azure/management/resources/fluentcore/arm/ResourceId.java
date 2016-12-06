/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.microsoft.azure.management.apigeneration.LangDefinition;

import java.security.InvalidParameterException;

/**
 * Instantiate itself from a resource id, and give easy access to resource information like subscription, resourceGroup,
 * resource name.
 */
@LangDefinition
public final class ResourceId {

    private String subscriptionId;
    private String resourceGroupName;
    private String name;
    private ResourceId parent;
    private String providerNamespace;
    private String resourceType;
    private String id;

    /**
     * Returns parsed ResourceId object for a given resource id.
     * @param id of the resource
     * @return ResourceId object.
     */
    public static ResourceId parseResourceId(String id) {
        // Example of id is id=/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/ans/providers/Microsoft.Network/applicationGateways/something
        // Remove the first '/' and then split using '/'
        String[] splits = id.substring(1).split("/");

        if (splits.length % 2 == 1) {
            throw new InvalidParameterException();
        }
        ResourceId resourceId = new ResourceId();

        resourceId.id = id;
        resourceId.subscriptionId = splits[1];
        resourceId.resourceGroupName = splits[3];

        // In case of a resource group Id is passed, then name is resource group name.
        if (splits.length == 4) {
            resourceId.name = resourceId.resourceGroupName;
            return resourceId;
        }

        resourceId.providerNamespace = splits[5];

        resourceId.name = splits[splits.length - 1];
        resourceId.resourceType = splits[splits.length - 2];

        int numberOfParents = splits.length / 2 - 4;
        if (numberOfParents == 0) {
            return resourceId;
        }

        String resourceType = splits[splits.length - 2];

        resourceId.parent = ResourceId.parseResourceId(id.substring(0, id.length() - ("/" + resourceType + "/" + resourceId.name()).length()));

        return resourceId;
    }

    /**
     * @return subscriptionId of the resource.
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * @return resourceGroupName of the resource.
     */
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    /**
     * @return name of the resource.
     */
    public String name() {
        return this.name;
    }

    /**
     * @return parent resource id of the resource if any, otherwise null.
     */
    public ResourceId parent() {
        return this.parent;
    }

    /**
     * @return name of the provider.
     */
    public String providerNamespace() {
        return this.providerNamespace;
    }

    /**
     * @return type of the resource.
     */
    public String resourceType() {
        return this.resourceType;
    }

    /**
     * @return full type of the resource.
     */
    public String fullResourceType() {
        if (this.parent == null) {
            return this.providerNamespace + "/" + this.resourceType;
        }
        return this.parent.fullResourceType() + "/" + this.resourceType;
    }

    /**
     * @return the id of the resource.
     */
    public String id() {
        return id;
    }
}
