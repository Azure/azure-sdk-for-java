// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * Generic abstract attribute collection applicable to metrics, tracing and logging implementations.
 * Implementation is capable of handling different attribute types, caching and optimizing the internal representation.
 */
public interface AttributeBuilder<T> {
    /**
     * Adds {@link String} attribute.
     *
     * @param key name of the attribute.
     * @param value value of the attribute.
     * @return updated instance of {@link AttributeBuilder}.
     */
    AttributeBuilder addAttribute(String key, String value);

    /**
     * Adds {@link long} attribute.
     *
     * @param key name of the attribute.
     * @param value value of the attribute.
     * @return updated instance of {@link AttributeBuilder}.
     */
    AttributeBuilder addAttribute(String key, long value);

    /**
     * Adds {@link double} attribute.
     *
     * @param key name of the attribute.
     * @param value value of the attribute.
     * @return updated instance of {@link AttributeBuilder}.
     */
    AttributeBuilder addAttribute(String key, double value);

    /**
     * Adds {@link boolean} attribute.
     *
     * @param key name of the attribute.
     * @param value value of the attribute.
     * @return updated instance of {@link AttributeBuilder}.
     */
    AttributeBuilder addAttribute(String key, boolean value);

    /**
     * Returns collection of attributes.
     * @return
     */
    T getAttributes();
}
