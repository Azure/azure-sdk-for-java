// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.informaticadatamanagement.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.informaticadatamanagement.fluent.OrganizationsClient;
import com.azure.resourcemanager.informaticadatamanagement.fluent.models.InformaticaOrganizationResourceInner;
import com.azure.resourcemanager.informaticadatamanagement.fluent.models.InformaticaServerlessRuntimeResourceListInner;
import com.azure.resourcemanager.informaticadatamanagement.fluent.models.ServerlessMetadataResponseInner;
import com.azure.resourcemanager.informaticadatamanagement.models.InformaticaOrganizationResource;
import com.azure.resourcemanager.informaticadatamanagement.models.InformaticaServerlessRuntimeResourceList;
import com.azure.resourcemanager.informaticadatamanagement.models.Organizations;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessMetadataResponse;

public final class OrganizationsImpl implements Organizations {
    private static final ClientLogger LOGGER = new ClientLogger(OrganizationsImpl.class);

    private final OrganizationsClient innerClient;

    private final com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager serviceManager;

    public OrganizationsImpl(OrganizationsClient innerClient,
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public PagedIterable<InformaticaOrganizationResource> list() {
        PagedIterable<InformaticaOrganizationResourceInner> inner = this.serviceClient().list();
        return ResourceManagerUtils.mapPage(inner,
            inner1 -> new InformaticaOrganizationResourceImpl(inner1, this.manager()));
    }

    public PagedIterable<InformaticaOrganizationResource> list(Context context) {
        PagedIterable<InformaticaOrganizationResourceInner> inner = this.serviceClient().list(context);
        return ResourceManagerUtils.mapPage(inner,
            inner1 -> new InformaticaOrganizationResourceImpl(inner1, this.manager()));
    }

    public PagedIterable<InformaticaOrganizationResource> listByResourceGroup(String resourceGroupName) {
        PagedIterable<InformaticaOrganizationResourceInner> inner
            = this.serviceClient().listByResourceGroup(resourceGroupName);
        return ResourceManagerUtils.mapPage(inner,
            inner1 -> new InformaticaOrganizationResourceImpl(inner1, this.manager()));
    }

    public PagedIterable<InformaticaOrganizationResource> listByResourceGroup(String resourceGroupName,
        Context context) {
        PagedIterable<InformaticaOrganizationResourceInner> inner
            = this.serviceClient().listByResourceGroup(resourceGroupName, context);
        return ResourceManagerUtils.mapPage(inner,
            inner1 -> new InformaticaOrganizationResourceImpl(inner1, this.manager()));
    }

    public Response<InformaticaOrganizationResource> getByResourceGroupWithResponse(String resourceGroupName,
        String organizationName, Context context) {
        Response<InformaticaOrganizationResourceInner> inner
            = this.serviceClient().getByResourceGroupWithResponse(resourceGroupName, organizationName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new InformaticaOrganizationResourceImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public InformaticaOrganizationResource getByResourceGroup(String resourceGroupName, String organizationName) {
        InformaticaOrganizationResourceInner inner
            = this.serviceClient().getByResourceGroup(resourceGroupName, organizationName);
        if (inner != null) {
            return new InformaticaOrganizationResourceImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public void deleteByResourceGroup(String resourceGroupName, String organizationName) {
        this.serviceClient().delete(resourceGroupName, organizationName);
    }

    public void delete(String resourceGroupName, String organizationName, Context context) {
        this.serviceClient().delete(resourceGroupName, organizationName, context);
    }

    public Response<InformaticaServerlessRuntimeResourceList>
        getAllServerlessRuntimesWithResponse(String resourceGroupName, String organizationName, Context context) {
        Response<InformaticaServerlessRuntimeResourceListInner> inner
            = this.serviceClient().getAllServerlessRuntimesWithResponse(resourceGroupName, organizationName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new InformaticaServerlessRuntimeResourceListImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public InformaticaServerlessRuntimeResourceList getAllServerlessRuntimes(String resourceGroupName,
        String organizationName) {
        InformaticaServerlessRuntimeResourceListInner inner
            = this.serviceClient().getAllServerlessRuntimes(resourceGroupName, organizationName);
        if (inner != null) {
            return new InformaticaServerlessRuntimeResourceListImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public Response<ServerlessMetadataResponse> getServerlessMetadataWithResponse(String resourceGroupName,
        String organizationName, Context context) {
        Response<ServerlessMetadataResponseInner> inner
            = this.serviceClient().getServerlessMetadataWithResponse(resourceGroupName, organizationName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new ServerlessMetadataResponseImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public ServerlessMetadataResponse getServerlessMetadata(String resourceGroupName, String organizationName) {
        ServerlessMetadataResponseInner inner
            = this.serviceClient().getServerlessMetadata(resourceGroupName, organizationName);
        if (inner != null) {
            return new ServerlessMetadataResponseImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public InformaticaOrganizationResource getById(String id) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String organizationName = ResourceManagerUtils.getValueFromIdByName(id, "organizations");
        if (organizationName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'organizations'.", id)));
        }
        return this.getByResourceGroupWithResponse(resourceGroupName, organizationName, Context.NONE).getValue();
    }

    public Response<InformaticaOrganizationResource> getByIdWithResponse(String id, Context context) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String organizationName = ResourceManagerUtils.getValueFromIdByName(id, "organizations");
        if (organizationName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'organizations'.", id)));
        }
        return this.getByResourceGroupWithResponse(resourceGroupName, organizationName, context);
    }

    public void deleteById(String id) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String organizationName = ResourceManagerUtils.getValueFromIdByName(id, "organizations");
        if (organizationName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'organizations'.", id)));
        }
        this.delete(resourceGroupName, organizationName, Context.NONE);
    }

    public void deleteByIdWithResponse(String id, Context context) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String organizationName = ResourceManagerUtils.getValueFromIdByName(id, "organizations");
        if (organizationName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'organizations'.", id)));
        }
        this.delete(resourceGroupName, organizationName, context);
    }

    private OrganizationsClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager() {
        return this.serviceManager;
    }

    public InformaticaOrganizationResourceImpl define(String name) {
        return new InformaticaOrganizationResourceImpl(name, this.manager());
    }
}
