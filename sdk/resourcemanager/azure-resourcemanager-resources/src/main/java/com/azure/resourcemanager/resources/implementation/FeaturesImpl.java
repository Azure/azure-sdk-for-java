// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.models.Feature;
import com.azure.resourcemanager.resources.models.Features;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.resourcemanager.resources.fluent.models.FeatureResultInner;
import com.azure.resourcemanager.resources.fluent.FeaturesClient;
import reactor.core.publisher.Mono;

/**
 * The implementation of {@link Features}.
 */
public final class FeaturesImpl
        extends ReadableWrappersImpl<Feature, FeatureImpl, FeatureResultInner>
        implements Features {
    private final FeaturesClient client;

    public FeaturesImpl(final FeaturesClient client) {
        this.client = client;
    }

    @Override
    public PagedIterable<Feature> list() {
        return wrapList(client.listAll());
    }

    @Override
    public Feature register(String resourceProviderName, String featureName) {
        return this.registerAsync(resourceProviderName, featureName).block();
    }

    @Override
    public Mono<Feature> registerAsync(String resourceProviderName, String featureName) {
        return client.registerAsync(resourceProviderName, featureName)
            .map(featureResultInner -> wrapModel(featureResultInner));
    }

    @Override
    protected FeatureImpl wrapModel(FeatureResultInner inner) {
        if (inner == null) {
            return null;
        }
        return new FeatureImpl(inner);
    }

    @Override
    public PagedFlux<Feature> listAsync() {
        return wrapPageAsync(client.listAllAsync());
    }
}
