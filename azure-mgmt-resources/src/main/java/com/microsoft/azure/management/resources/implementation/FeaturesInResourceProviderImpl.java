/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Features;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.FeatureClientImpl;
import com.microsoft.azure.management.resources.implementation.api.FeaturesInner;
import com.microsoft.azure.management.resources.Feature;
import com.microsoft.azure.management.resources.implementation.api.FeatureResultInner;

import java.io.IOException;
import java.util.List;

public final class FeaturesInResourceProviderImpl
        implements Features.InResourceProvider {
    private final FeaturesInner client;
    private String resourceProviderNamespace;

    public FeaturesInResourceProviderImpl(FeaturesInner client, String resourceProviderNamespace) {
        this.client = client;
        this.resourceProviderNamespace = resourceProviderNamespace;
    }

    @Override
    public PagedList<Feature> list() throws CloudException, IOException {
        PagedListConverter<FeatureResultInner, Feature> converter = new PagedListConverter<FeatureResultInner, Feature>() {
            @Override
            public Feature typeConvert(FeatureResultInner tenantInner) {
                return new FeatureImpl(tenantInner);
            }
        };
        return converter.convert(client.list(resourceProviderNamespace).getBody());
    }

    @Override
    public Feature register(String featureName) throws IOException, CloudException {
        return new FeatureImpl(client.register(resourceProviderNamespace, featureName).getBody());
    }

    @Override
    public Feature get(String name) throws CloudException, IOException {
        return new FeatureImpl(client.get(resourceProviderNamespace, name).getBody());
    }
}
