// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.servicelinker.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.servicelinker.fluent.LinkersOperationsClient;
import com.azure.resourcemanager.servicelinker.fluent.models.ConfigurationResultInner;
import com.azure.resourcemanager.servicelinker.fluent.models.DaprConfigurationResourceInner;
import com.azure.resourcemanager.servicelinker.fluent.models.DryrunResourceInner;
import com.azure.resourcemanager.servicelinker.models.ConfigurationInfo;
import com.azure.resourcemanager.servicelinker.models.ConfigurationResult;
import com.azure.resourcemanager.servicelinker.models.DaprConfigurationResource;
import com.azure.resourcemanager.servicelinker.models.DryrunPatch;
import com.azure.resourcemanager.servicelinker.models.DryrunResource;
import com.azure.resourcemanager.servicelinker.models.LinkersOperations;

public final class LinkersOperationsImpl implements LinkersOperations {
    private static final ClientLogger LOGGER = new ClientLogger(LinkersOperationsImpl.class);

    private final LinkersOperationsClient innerClient;

    private final com.azure.resourcemanager.servicelinker.ServiceLinkerManager serviceManager;

    public LinkersOperationsImpl(LinkersOperationsClient innerClient,
        com.azure.resourcemanager.servicelinker.ServiceLinkerManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public PagedIterable<DryrunResource> listDryrun(String resourceUri) {
        PagedIterable<DryrunResourceInner> inner = this.serviceClient().listDryrun(resourceUri);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new DryrunResourceImpl(inner1, this.manager()));
    }

    public PagedIterable<DryrunResource> listDryrun(String resourceUri, Context context) {
        PagedIterable<DryrunResourceInner> inner = this.serviceClient().listDryrun(resourceUri, context);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new DryrunResourceImpl(inner1, this.manager()));
    }

    public Response<DryrunResource> getDryrunWithResponse(String resourceUri, String dryrunName, Context context) {
        Response<DryrunResourceInner> inner
            = this.serviceClient().getDryrunWithResponse(resourceUri, dryrunName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new DryrunResourceImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public DryrunResource getDryrun(String resourceUri, String dryrunName) {
        DryrunResourceInner inner = this.serviceClient().getDryrun(resourceUri, dryrunName);
        if (inner != null) {
            return new DryrunResourceImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public DryrunResource createDryrun(String resourceUri, String dryrunName, DryrunResourceInner parameters) {
        DryrunResourceInner inner = this.serviceClient().createDryrun(resourceUri, dryrunName, parameters);
        if (inner != null) {
            return new DryrunResourceImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public DryrunResource createDryrun(String resourceUri, String dryrunName, DryrunResourceInner parameters,
        Context context) {
        DryrunResourceInner inner = this.serviceClient().createDryrun(resourceUri, dryrunName, parameters, context);
        if (inner != null) {
            return new DryrunResourceImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public DryrunResource updateDryrun(String resourceUri, String dryrunName, DryrunPatch parameters) {
        DryrunResourceInner inner = this.serviceClient().updateDryrun(resourceUri, dryrunName, parameters);
        if (inner != null) {
            return new DryrunResourceImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public DryrunResource updateDryrun(String resourceUri, String dryrunName, DryrunPatch parameters, Context context) {
        DryrunResourceInner inner = this.serviceClient().updateDryrun(resourceUri, dryrunName, parameters, context);
        if (inner != null) {
            return new DryrunResourceImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public Response<Void> deleteDryrunWithResponse(String resourceUri, String dryrunName, Context context) {
        return this.serviceClient().deleteDryrunWithResponse(resourceUri, dryrunName, context);
    }

    public void deleteDryrun(String resourceUri, String dryrunName) {
        this.serviceClient().deleteDryrun(resourceUri, dryrunName);
    }

    public Response<ConfigurationResult> generateConfigurationsWithResponse(String resourceUri, String linkerName,
        ConfigurationInfo parameters, Context context) {
        Response<ConfigurationResultInner> inner
            = this.serviceClient().generateConfigurationsWithResponse(resourceUri, linkerName, parameters, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new ConfigurationResultImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public ConfigurationResult generateConfigurations(String resourceUri, String linkerName) {
        ConfigurationResultInner inner = this.serviceClient().generateConfigurations(resourceUri, linkerName);
        if (inner != null) {
            return new ConfigurationResultImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public PagedIterable<DaprConfigurationResource> listDaprConfigurations(String resourceUri) {
        PagedIterable<DaprConfigurationResourceInner> inner = this.serviceClient().listDaprConfigurations(resourceUri);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new DaprConfigurationResourceImpl(inner1, this.manager()));
    }

    public PagedIterable<DaprConfigurationResource> listDaprConfigurations(String resourceUri, Context context) {
        PagedIterable<DaprConfigurationResourceInner> inner
            = this.serviceClient().listDaprConfigurations(resourceUri, context);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new DaprConfigurationResourceImpl(inner1, this.manager()));
    }

    private LinkersOperationsClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager() {
        return this.serviceManager;
    }
}
