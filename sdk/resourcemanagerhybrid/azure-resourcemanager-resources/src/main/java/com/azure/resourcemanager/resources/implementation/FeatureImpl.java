// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.models.Feature;
import com.azure.resourcemanager.resources.fluent.models.FeatureResultInner;

import java.util.regex.Pattern;

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
        return innerModel().name();
    }

    @Override
    public String type() {
        return innerModel().type();
    }

    @Override
    public String state() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().state();
    }

    @Override
    public String resourceProviderName() {
        if (this.name() == null) {
            return null;
        }

        String[] segments = this.name().split(Pattern.quote("/"));
        return segments.length > 0 ? segments[0] : null;
    }

    @Override
    public String featureName() {
        if (this.name() == null) {
            return null;
        }

        String[] segments = this.name().split(Pattern.quote("/"));
        return segments.length > 1 ? segments[1] : null;
    }

    @Override
    public String id() {
        return innerModel().id();
    }
}
