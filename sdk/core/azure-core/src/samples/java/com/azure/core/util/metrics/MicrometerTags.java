// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AttributesBuilder;
import io.micrometer.core.instrument.Tags;

/**
 * {@inheritDoc}
 */
class MicrometerTags implements AttributesBuilder {
    private Tags tags;

    MicrometerTags() {
        tags = Tags.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder add(String key, String value) {
        this.tags = tags.and(key, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder add(String key, long value) {
        this.tags = tags.and(key, String.valueOf(value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder add(String key, double value) {
        this.tags = tags.and(key, String.valueOf(value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder add(String key, boolean value) {
        this.tags = tags.and(key, String.valueOf(value));
        return this;
    }

    Tags get() {
        return tags;
    }
}
