// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class FallbackAttributes implements InstrumentationAttributes {
    private final Map<String, Object> attributes;

    FallbackAttributes(Map<String, Object> attributes) {
        if (attributes == null) {
            this.attributes = Collections.emptyMap();
            return;
        }

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "attribute key cannot be null.");
            Objects.requireNonNull(entry.getValue(), "attribute value cannot be null.");
        }
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    @Override
    public InstrumentationAttributes put(String key, Object value) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        Objects.requireNonNull(value, "'value' cannot be null.");

        Map<String, Object> newAttributes = new HashMap<>((int) ((attributes.size() + 1) * 1.5));
        newAttributes.putAll(attributes);
        newAttributes.put(key, value);
        return new FallbackAttributes(newAttributes);
    }

    Map<String, Object> getAttributes() {
        return attributes;
    }
}
