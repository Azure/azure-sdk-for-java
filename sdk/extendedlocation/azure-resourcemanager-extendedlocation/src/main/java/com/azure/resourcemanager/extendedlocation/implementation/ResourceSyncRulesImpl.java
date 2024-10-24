// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.extendedlocation.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.extendedlocation.fluent.ResourceSyncRulesClient;
import com.azure.resourcemanager.extendedlocation.fluent.models.ResourceSyncRuleInner;
import com.azure.resourcemanager.extendedlocation.models.ResourceSyncRule;
import com.azure.resourcemanager.extendedlocation.models.ResourceSyncRules;

public final class ResourceSyncRulesImpl implements ResourceSyncRules {
    private static final ClientLogger LOGGER = new ClientLogger(ResourceSyncRulesImpl.class);

    private final ResourceSyncRulesClient innerClient;

    private final com.azure.resourcemanager.extendedlocation.CustomLocationsManager serviceManager;

    public ResourceSyncRulesImpl(ResourceSyncRulesClient innerClient,
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public PagedIterable<ResourceSyncRule> listByCustomLocationId(String resourceGroupName, String resourceName) {
        PagedIterable<ResourceSyncRuleInner> inner
            = this.serviceClient().listByCustomLocationId(resourceGroupName, resourceName);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new ResourceSyncRuleImpl(inner1, this.manager()));
    }

    public PagedIterable<ResourceSyncRule> listByCustomLocationId(String resourceGroupName, String resourceName,
        Context context) {
        PagedIterable<ResourceSyncRuleInner> inner
            = this.serviceClient().listByCustomLocationId(resourceGroupName, resourceName, context);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new ResourceSyncRuleImpl(inner1, this.manager()));
    }

    public Response<ResourceSyncRule> getWithResponse(String resourceGroupName, String resourceName,
        String childResourceName, Context context) {
        Response<ResourceSyncRuleInner> inner
            = this.serviceClient().getWithResponse(resourceGroupName, resourceName, childResourceName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new ResourceSyncRuleImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public ResourceSyncRule get(String resourceGroupName, String resourceName, String childResourceName) {
        ResourceSyncRuleInner inner = this.serviceClient().get(resourceGroupName, resourceName, childResourceName);
        if (inner != null) {
            return new ResourceSyncRuleImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public Response<Void> deleteWithResponse(String resourceGroupName, String resourceName, String childResourceName,
        Context context) {
        return this.serviceClient().deleteWithResponse(resourceGroupName, resourceName, childResourceName, context);
    }

    public void delete(String resourceGroupName, String resourceName, String childResourceName) {
        this.serviceClient().delete(resourceGroupName, resourceName, childResourceName);
    }

    public ResourceSyncRule getById(String id) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String resourceName = ResourceManagerUtils.getValueFromIdByName(id, "customLocations");
        if (resourceName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'customLocations'.", id)));
        }
        String childResourceName = ResourceManagerUtils.getValueFromIdByName(id, "resourceSyncRules");
        if (childResourceName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceSyncRules'.", id)));
        }
        return this.getWithResponse(resourceGroupName, resourceName, childResourceName, Context.NONE).getValue();
    }

    public Response<ResourceSyncRule> getByIdWithResponse(String id, Context context) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String resourceName = ResourceManagerUtils.getValueFromIdByName(id, "customLocations");
        if (resourceName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'customLocations'.", id)));
        }
        String childResourceName = ResourceManagerUtils.getValueFromIdByName(id, "resourceSyncRules");
        if (childResourceName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceSyncRules'.", id)));
        }
        return this.getWithResponse(resourceGroupName, resourceName, childResourceName, context);
    }

    public void deleteById(String id) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String resourceName = ResourceManagerUtils.getValueFromIdByName(id, "customLocations");
        if (resourceName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'customLocations'.", id)));
        }
        String childResourceName = ResourceManagerUtils.getValueFromIdByName(id, "resourceSyncRules");
        if (childResourceName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceSyncRules'.", id)));
        }
        this.deleteWithResponse(resourceGroupName, resourceName, childResourceName, Context.NONE);
    }

    public Response<Void> deleteByIdWithResponse(String id, Context context) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String resourceName = ResourceManagerUtils.getValueFromIdByName(id, "customLocations");
        if (resourceName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'customLocations'.", id)));
        }
        String childResourceName = ResourceManagerUtils.getValueFromIdByName(id, "resourceSyncRules");
        if (childResourceName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceSyncRules'.", id)));
        }
        return this.deleteWithResponse(resourceGroupName, resourceName, childResourceName, context);
    }

    private ResourceSyncRulesClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager() {
        return this.serviceManager;
    }

    public ResourceSyncRuleImpl define(String name) {
        return new ResourceSyncRuleImpl(name, this.manager());
    }
}
