// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datadog.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.datadog.fluent.MarketplaceAgreementsClient;
import com.azure.resourcemanager.datadog.fluent.models.DatadogAgreementResourceInner;
import com.azure.resourcemanager.datadog.models.DatadogAgreementResource;
import com.azure.resourcemanager.datadog.models.MarketplaceAgreements;

public final class MarketplaceAgreementsImpl implements MarketplaceAgreements {
    private static final ClientLogger LOGGER = new ClientLogger(MarketplaceAgreementsImpl.class);

    private final MarketplaceAgreementsClient innerClient;

    private final com.azure.resourcemanager.datadog.MicrosoftDatadogManager serviceManager;

    public MarketplaceAgreementsImpl(MarketplaceAgreementsClient innerClient,
        com.azure.resourcemanager.datadog.MicrosoftDatadogManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public PagedIterable<DatadogAgreementResource> list() {
        PagedIterable<DatadogAgreementResourceInner> inner = this.serviceClient().list();
        return ResourceManagerUtils.mapPage(inner, inner1 -> new DatadogAgreementResourceImpl(inner1, this.manager()));
    }

    public PagedIterable<DatadogAgreementResource> list(Context context) {
        PagedIterable<DatadogAgreementResourceInner> inner = this.serviceClient().list(context);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new DatadogAgreementResourceImpl(inner1, this.manager()));
    }

    public Response<DatadogAgreementResource> createOrUpdateWithResponse(DatadogAgreementResourceInner body,
        Context context) {
        Response<DatadogAgreementResourceInner> inner = this.serviceClient().createOrUpdateWithResponse(body, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new DatadogAgreementResourceImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public DatadogAgreementResource createOrUpdate() {
        DatadogAgreementResourceInner inner = this.serviceClient().createOrUpdate();
        if (inner != null) {
            return new DatadogAgreementResourceImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    private MarketplaceAgreementsClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager() {
        return this.serviceManager;
    }
}
