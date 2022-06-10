// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

/**
 * Instantiate itself from a resource id, and give easy access to resource information like subscription, resourceGroup,
 * resource name.
 */
public final class ResourceId {

    private final String subscriptionId;
    private final String resourceGroupName;
    private final String name;
    private final String providerNamespace;
    private final String resourceType;
    private final String id;
    private final String parentId;

    private static String badIdErrorText(String id) {
        return String.format("The specified ID `%s` is not a valid Azure resource ID.", id);
    }

    private ResourceId(final String id) {
        if (id == null) {
            // Protect against NPEs from null IDs, preserving legacy behavior for null IDs
            this.subscriptionId = null;
            this.resourceGroupName = null;
            this.name = null;
            this.providerNamespace = null;
            this.resourceType = null;
            this.id = null;
            this.parentId = null;
            return;
        } else {
            // Skip the first '/' if any, and then split using '/'
            String[] splits = (id.startsWith("/")) ? id.substring(1).split("/") : id.split("/");
            if (splits.length % 2 == 1) {
                throw new IllegalArgumentException(badIdErrorText(id));
            }

            // Save the ID itself
            this.id = id;

            // Format of id:
            // /subscriptions/<subscriptionId>/resourceGroups/<resourceGroupName>/providers
            // /<providerNamespace>(/<parentResourceType>/<parentName>)*/<resourceType>/<name>

            // 0             1                2              3                   4
            // 5                                                        N-2            N-1

            // Extract resource type and name
            if (splits.length < 2) {
                throw new IllegalArgumentException(badIdErrorText(id));
            } else {
                this.name = splits[splits.length - 1];
                this.resourceType = splits[splits.length - 2];
            }

            // Extract parent ID
            if (splits.length < 10) {
                this.parentId = null;
            } else {
                String[] parentSplits = new String[splits.length - 2];
                System.arraycopy(splits, 0, parentSplits, 0, splits.length - 2);
                this.parentId = "/" + String.join("/", parentSplits);
            }

            // Ensure "subscriptions"
            if (!splits[0].equalsIgnoreCase("subscriptions")) {
                throw new IllegalArgumentException(badIdErrorText(id));
            }
            // Extract subscription ID
            this.subscriptionId = splits[1];
            // Ensure "resourceGroups"
            if (splits.length > 2 && !splits[2].equalsIgnoreCase("resourceGroups")) {
                throw new IllegalArgumentException(badIdErrorText(id));
            }
            // Extract resource group name
            this.resourceGroupName = splits.length > 3 ? splits[3] : null;
            // Ensure "providers"
            if (splits.length > 4 && !splits[4].equalsIgnoreCase("providers")) {
                throw new IllegalArgumentException(badIdErrorText(id));
            }
            // Extract provider namespace
            this.providerNamespace = splits.length > 5 ? splits[5] : null;
        }
    }

    /**
     * Returns parsed ResourceId object for a given resource id.
     *
     * @param id of the resource
     * @return ResourceId object
     */
    public static ResourceId fromString(String id) {
        return new ResourceId(id);
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
        if (this.id == null || this.parentId == null) {
            return null;
        } else {
            return fromString(this.parentId);
        }
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
        if (this.parentId == null) {
            return this.providerNamespace + "/" + this.resourceType;
        } else {
            return this.parent().fullResourceType() + "/" + this.resourceType;
        }
    }

    /**
     * @return the id of the resource.
     */
    public String id() {
        return id;
    }
}
