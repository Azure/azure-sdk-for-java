/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.Feature;
import com.azure.management.resources.Features;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.management.resources.models.FeatureResultInner;
import com.azure.management.resources.models.FeaturesInner;
import reactor.core.publisher.Mono;

/**
 * The implementation of {@link Features}.
 */
final class FeaturesImpl
        extends ReadableWrappersImpl<Feature, FeatureImpl, FeatureResultInner>
        implements Features {
    private final FeaturesInner client;

    FeaturesImpl(final FeaturesInner client) {
        this.client = client;
    }

    @Override
    public PagedIterable<Feature> list() {
        return wrapList(client.list());
    }

    @Override
    public Feature register(String resourceProviderName, String featureName) {
        return this.registerAsync(resourceProviderName, featureName).block();
    }

    @Override
    public Mono<Feature> registerAsync(String resourceProviderName, String featureName) {
        return client.registerAsync(resourceProviderName, featureName).map(featureResultInner -> wrapModel(featureResultInner));
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
        return wrapPageAsync(client.listAsync());
    }
}
