// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeBuilder;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * {@inheritDoc}
 */
class MicrometerTags implements AzureAttributeBuilder {
    private List<Tag> tags;

    MicrometerTags() {
        tags = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeBuilder add(String key, String value) {
        tags.add(new ImmutableTag(key, value));

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeBuilder add(String key, long value) {
        tags.add(new ImmutableTag(key, String.valueOf(value)));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeBuilder add(String key, double value) {
        tags.add(new ImmutableTag(key, String.valueOf(value)));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeBuilder add(String key, boolean value) {
        tags.add(new ImmutableTag(key, String.valueOf(value)));
        return this;
    }

    Iterable<Tag> get() {
        return tags;
    }
}
