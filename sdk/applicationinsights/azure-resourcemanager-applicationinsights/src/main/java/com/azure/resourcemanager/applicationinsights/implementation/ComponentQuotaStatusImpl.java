// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.applicationinsights.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.applicationinsights.fluent.ComponentQuotaStatusClient;
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentQuotaStatusInner;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentQuotaStatus;
import com.azure.resourcemanager.applicationinsights.models.ComponentQuotaStatus;

public final class ComponentQuotaStatusImpl implements ComponentQuotaStatus {
    private static final ClientLogger LOGGER = new ClientLogger(ComponentQuotaStatusImpl.class);

    private final ComponentQuotaStatusClient innerClient;

    private final com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager serviceManager;

    public ComponentQuotaStatusImpl(ComponentQuotaStatusClient innerClient,
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public Response<ApplicationInsightsComponentQuotaStatus> getWithResponse(String resourceGroupName,
        String resourceName, Context context) {
        Response<ApplicationInsightsComponentQuotaStatusInner> inner
            = this.serviceClient().getWithResponse(resourceGroupName, resourceName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new ApplicationInsightsComponentQuotaStatusImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public ApplicationInsightsComponentQuotaStatus get(String resourceGroupName, String resourceName) {
        ApplicationInsightsComponentQuotaStatusInner inner = this.serviceClient().get(resourceGroupName, resourceName);
        if (inner != null) {
            return new ApplicationInsightsComponentQuotaStatusImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    private ComponentQuotaStatusClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager() {
        return this.serviceManager;
    }
}
