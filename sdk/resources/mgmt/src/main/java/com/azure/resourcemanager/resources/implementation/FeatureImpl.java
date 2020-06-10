// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.models.Feature;
import com.azure.resourcemanager.resources.fluent.inner.FeatureResultInner;

/**
 * The implementation of {@link Feature}.
 */
final class FeatureImpl extends
        IndexableWrapperImpl<FeatureResultInner>
        implements
        Feature {

    FeatureImpl(FeatureResultInner innerModel) {
        super(innerModel);
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public String type() {
        return inner().type();
    }

    @Override
    public String state() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().state();
    }
}
