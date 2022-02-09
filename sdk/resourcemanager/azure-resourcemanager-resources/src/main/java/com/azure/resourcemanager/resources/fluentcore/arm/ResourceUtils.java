// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.models.ProviderResourceType;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for Azure resource IDs.
 */
public final class ResourceUtils {
    private ResourceUtils() {
    }

    /**
     * Extract resource group from a resource ID string.
     *
     * @param id the resource ID string
     * @return the resource group name
     */
    public static String groupFromResourceId(String id) {
        return (id != null) ? ResourceId.fromString(id).resourceGroupName() : null;
    }

    /**
     * Extract the subscription ID from a resource ID string.
     *
     * @param id the resource ID string
     * @return the subscription ID
     */
    public static String subscriptionFromResourceId(String id) {
        return (id != null) ? ResourceId.fromString(id).subscriptionId() : null;
    }

    /**
     * Extract resource provider from a resource ID string.
     *
     * @param id the resource ID string
     * @return the resource group name
     */
    public static String resourceProviderFromResourceId(String id) {
        return (id != null) ? ResourceId.fromString(id).providerNamespace() : null;
    }

    /**
     * Extract resource type from a resource ID string.
     *
     * @param id the resource ID string
     * @return the resource type
     */
    public static String resourceTypeFromResourceId(String id) {
        return (id != null) ? ResourceId.fromString(id).resourceType() : null;
    }

    /**
     * Extract parent resource ID from a resource ID string.
     * E.g. subscriptions/s/resourcegroups/r/foos/foo/bars/bar will return
     * subscriptions/s/resourcegroups/r/foos/foo.
     *
     * @param id the resource ID string
     * @return the parent resource ID
     */
    public static String parentResourceIdFromResourceId(String id) {
        if (id == null) {
            return null;
        }
        ResourceId resourceId = ResourceId.fromString(id);
        if (resourceId.parent() != null) {
            return ResourceId.fromString(id).parent().id();
        }

        return null;
    }

    /**
     * Extract parent resource path from a resource ID string.
     * E.g. subscriptions/s/resourcegroups/r/foos/foo/bars/bar will return foos/foo.
     *
     * @param id the resource ID string
     * @return the parent resource ID
     */
    public static String parentRelativePathFromResourceId(String id) {
        if (id == null) {
            return null;
        }

        ResourceId parent = ResourceId.fromString(id).parent();
        if (parent != null) {
            return parent.resourceType() + "/" + parent.name();
        }

        return "";
    }

    /**
     * Extract the relative path to the current resource provider.
     * E.g. subscriptions/sub1/resourceGroups/rg1/providers/Microsoft.Foo/foos/foo1 will return foos/foo1.
     *
     * @param id the id of the resource
     * @return the relative path
     */
    public static String relativePathFromResourceId(String id) {
        if (id == null) {
            return null;
        }
        String[] paths = id.split("/providers/" + resourceProviderFromResourceId(id) + "/", 2);
        if (paths.length == 1) {
            return "";
        } else {
            return paths[1];
        }
    }

    /**
     * Extract information from a resource ID string with the resource type
     * as the identifier.
     *
     * @param id the resource ID
     * @param identifier the identifier to match, e.g. "resourceGroups", "storageAccounts"
     * @return the information extracted from the identifier
     */
    public static String extractFromResourceId(String id, String identifier) {
        if (id == null || identifier == null) {
            return id;
        }
        Pattern pattern = Pattern.compile(identifier + "/[-\\w._]+");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find()) {
            return matcher.group().split("/")[1];
        } else {
            return null;
        }
    }

    /**
     * Extract name of the resource from a resource ID.
     *
     * @param id the resource ID
     * @return the name of the resource
     */
    public static String nameFromResourceId(String id) {
        return (id != null) ? ResourceId.fromString(id).name() : null;
    }

    /**
     * Find out the default api version to make a REST request with from
     * the resource provider.
     *
     * @param id the resource ID
     * @param provider the resource provider
     * @return the default api version to use
     */
    public static String defaultApiVersion(String id, Provider provider) {
        String resourceType = resourceTypeFromResourceId(id).toLowerCase(Locale.ROOT);
        // Exact match
        for (ProviderResourceType prt : provider.resourceTypes()) {
            if (prt.resourceType().equalsIgnoreCase(resourceType)) {
                return prt.defaultApiVersion() == null ? prt.apiVersions().get(0) : prt.defaultApiVersion();
            }
        }
        // child resource, e.g. sites/config
        for (ProviderResourceType prt : provider.resourceTypes()) {
            if (prt.resourceType().toLowerCase(Locale.ROOT).contains("/" + resourceType)) {
                return prt.defaultApiVersion() == null ? prt.apiVersions().get(0) : prt.defaultApiVersion();
            }
        }
        // look for parent
        String parentId = parentResourceIdFromResourceId(id);
        if (parentId != null) {
            return defaultApiVersion(parentId, provider);
        } else {
            // Fallback: use a random one, not guaranteed to work
            return provider.resourceTypes().get(0).apiVersions().get(0);
        }
    }

    /**
     * Creates a resource ID from information of a generic resource.
     *
     * @param subscriptionId the subscription UUID
     * @param resourceGroupName the resource group name
     * @param resourceProviderNamespace the resource provider namespace
     * @param resourceType the type of the resource or nested resource
     * @param resourceName name of the resource or nested resource
     * @param parentResourcePath parent resource's relative path to the provider,
     *                                  if the resource is a generic resource
     * @return the resource ID string
     */
    public static String constructResourceId(
            final String subscriptionId,
            final String resourceGroupName,
            final String resourceProviderNamespace,
            final String resourceType,
            final String resourceName,
            final String parentResourcePath) {
        String prefixedParentPath = parentResourcePath;
        if (parentResourcePath != null && !parentResourcePath.isEmpty()) {
            prefixedParentPath = "/" + parentResourcePath;
        }
        return String.format(
                "/subscriptions/%s/resourcegroups/%s/providers/%s%s/%s/%s",
                subscriptionId,
                resourceGroupName,
                resourceProviderNamespace,
                prefixedParentPath,
                resourceType,
                resourceName);
    }
}
