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
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

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
        return wrapList(client.list());
    }

    @Override
    public Feature register(String resourceProviderName, String featureName) {
        return this.registerAsync(resourceProviderName, featureName).toBlocking().last();
    }

    @Override
    public Observable<Feature> registerAsync(String resourceProviderName, String featureName) {
        return client.registerAsync(resourceProviderName, featureName).map(new Func1<FeatureResultInner, Feature>() {
            @Override
            public Feature call(FeatureResultInner featureResultInner) {
                return wrapModel(featureResultInner);
            }
        });
    }

    @Override
    public ServiceFuture<Feature> registerAsync(String resourceProviderName, String featureName, ServiceCallback<Feature> callback) {
        return ServiceFuture.fromBody(this.registerAsync(resourceProviderName, featureName), callback);
    }

    @Override
    protected FeatureImpl wrapModel(FeatureResultInner inner) {
        if (inner == null) {
            return null;
        }
        return new FeatureImpl(inner);
    }

    @Override
    public Observable<Feature> listAsync() {
        return wrapPageAsync(client.listAsync());
    }
}
