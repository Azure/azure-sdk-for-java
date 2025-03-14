// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.cognitiveservices.fluent.ModelCapacitiesClient;
import com.azure.resourcemanager.cognitiveservices.fluent.models.ModelCapacityListResultValueItemInner;
import com.azure.resourcemanager.cognitiveservices.models.ModelCapacities;
import com.azure.resourcemanager.cognitiveservices.models.ModelCapacityListResultValueItem;

public final class ModelCapacitiesImpl implements ModelCapacities {
    private static final ClientLogger LOGGER = new ClientLogger(ModelCapacitiesImpl.class);

    private final ModelCapacitiesClient innerClient;

    private final com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager serviceManager;

    public ModelCapacitiesImpl(ModelCapacitiesClient innerClient,
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public PagedIterable<ModelCapacityListResultValueItem> list(String modelFormat, String modelName,
        String modelVersion) {
        PagedIterable<ModelCapacityListResultValueItemInner> inner
            = this.serviceClient().list(modelFormat, modelName, modelVersion);
        return ResourceManagerUtils.mapPage(inner,
            inner1 -> new ModelCapacityListResultValueItemImpl(inner1, this.manager()));
    }

    public PagedIterable<ModelCapacityListResultValueItem> list(String modelFormat, String modelName,
        String modelVersion, Context context) {
        PagedIterable<ModelCapacityListResultValueItemInner> inner
            = this.serviceClient().list(modelFormat, modelName, modelVersion, context);
        return ResourceManagerUtils.mapPage(inner,
            inner1 -> new ModelCapacityListResultValueItemImpl(inner1, this.manager()));
    }

    private ModelCapacitiesClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager() {
        return this.serviceManager;
    }
}
