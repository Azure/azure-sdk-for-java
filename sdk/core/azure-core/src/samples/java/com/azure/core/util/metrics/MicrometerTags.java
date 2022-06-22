// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeCollection;
import io.micrometer.core.instrument.Tags;

/**
 * {@inheritDoc}
 */
class MicrometerTags implements AzureAttributeCollection {
    private Tags tags;

    MicrometerTags() {
        tags = Tags.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection add(String key, String value) {
        tags.and(key, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection add(String key, long value) {
        tags.and(key, String.valueOf(value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection add(String key, double value) {
        tags.and(key, String.valueOf(value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection add(String key, boolean value) {
        tags.and(key, String.valueOf(value));
        return this;
    }

    Tags get() {
        return tags;
    }
}
