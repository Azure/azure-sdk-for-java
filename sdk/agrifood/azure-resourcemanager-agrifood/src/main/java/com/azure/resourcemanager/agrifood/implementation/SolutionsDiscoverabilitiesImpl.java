// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.agrifood.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.agrifood.fluent.SolutionsDiscoverabilitiesClient;
import com.azure.resourcemanager.agrifood.fluent.models.FarmBeatsSolutionInner;
import com.azure.resourcemanager.agrifood.models.FarmBeatsSolution;
import com.azure.resourcemanager.agrifood.models.SolutionsDiscoverabilities;
import java.util.List;

public final class SolutionsDiscoverabilitiesImpl implements SolutionsDiscoverabilities {
    private static final ClientLogger LOGGER = new ClientLogger(SolutionsDiscoverabilitiesImpl.class);

    private final SolutionsDiscoverabilitiesClient innerClient;

    private final com.azure.resourcemanager.agrifood.AgriFoodManager serviceManager;

    public SolutionsDiscoverabilitiesImpl(SolutionsDiscoverabilitiesClient innerClient,
        com.azure.resourcemanager.agrifood.AgriFoodManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public PagedIterable<FarmBeatsSolution> list() {
        PagedIterable<FarmBeatsSolutionInner> inner = this.serviceClient().list();
        return ResourceManagerUtils.mapPage(inner, inner1 -> new FarmBeatsSolutionImpl(inner1, this.manager()));
    }

    public PagedIterable<FarmBeatsSolution> list(List<String> farmBeatsSolutionIds, List<String> farmBeatsSolutionNames,
        Integer maxPageSize, Context context) {
        PagedIterable<FarmBeatsSolutionInner> inner
            = this.serviceClient().list(farmBeatsSolutionIds, farmBeatsSolutionNames, maxPageSize, context);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new FarmBeatsSolutionImpl(inner1, this.manager()));
    }

    public Response<FarmBeatsSolution> getWithResponse(String farmBeatsSolutionId, Context context) {
        Response<FarmBeatsSolutionInner> inner = this.serviceClient().getWithResponse(farmBeatsSolutionId, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new FarmBeatsSolutionImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public FarmBeatsSolution get(String farmBeatsSolutionId) {
        FarmBeatsSolutionInner inner = this.serviceClient().get(farmBeatsSolutionId);
        if (inner != null) {
            return new FarmBeatsSolutionImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    private SolutionsDiscoverabilitiesClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.agrifood.AgriFoodManager manager() {
        return this.serviceManager;
    }
}
