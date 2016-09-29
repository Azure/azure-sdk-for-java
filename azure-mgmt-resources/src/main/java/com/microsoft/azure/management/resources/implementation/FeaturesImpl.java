/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Feature;
import com.microsoft.azure.management.resources.Features;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

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
    public PagedList<Feature> list() {
        return wrapList(client.listAll());
    }

    @Override
    public Feature register(String resourceProviderName, String featureName) {
        return wrapModel(client.register(resourceProviderName, featureName));
    }

    @Override
    protected FeatureImpl wrapModel(FeatureResultInner inner) {
        return new FeatureImpl(inner);
    }
}
