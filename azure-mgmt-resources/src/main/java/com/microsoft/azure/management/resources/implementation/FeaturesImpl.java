/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Features;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.Feature;
import java.io.IOException;

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
    public PagedList<Feature> list() throws CloudException, IOException {
        return wrapList(client.listAll());
    }

    @Override
    public InResourceProvider resourceProvider(String resourceProviderName) {
        return null;
    }

    @Override
    protected FeatureImpl wrapModel(FeatureResultInner inner) {
        return new FeatureImpl(inner);
    }
}
