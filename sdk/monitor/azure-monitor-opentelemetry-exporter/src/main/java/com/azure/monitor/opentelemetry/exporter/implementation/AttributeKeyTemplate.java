// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.exporter.implementation;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

// this is a copy of io.opentelemetry.semconv.AttributeKeyTemplate (1.23.1-alpha)
// because the module that contains that class is not stable,
// so don't want to take a dependency on it

/**
 * This class provides a handle for creating and caching dynamic / template-type attributes of the
 * form <b>&lt;prefix&gt;.&lt;key&gt;</b>. The &lt;prefix&gt; is fixed for a template instance while
 * {@link AttributeKey}s can be created and are cached for different values of the &lt;key&gt; part.
 *
 * <p>An example template-type attribute is the set of attributes for HTTP headers:
 * <b>http.request.header.&lt;key&gt;</b>
 *
 * @param <T> The type of the nested {@link AttributeKey}s.
 */
public final class AttributeKeyTemplate<T> {

    private final String prefix;
    private final Function<String, AttributeKey<T>> keyBuilder;
    private final ConcurrentMap<String, AttributeKey<T>> keysCache = new ConcurrentHashMap<>(1);

    private AttributeKeyTemplate(String prefix, Function<String, AttributeKey<T>> keyBuilder) {
        this.prefix = prefix;
        this.keyBuilder = keyBuilder;
    }

    /**
     * Create an {@link AttributeKeyTemplate} with type {@link AttributeType#STRING} and the given
     * {@code prefix}.
     */
    public static AttributeKeyTemplate<String> stringKeyTemplate(String prefix) {
        return new AttributeKeyTemplate<>(prefix, AttributeKey::stringKey);
    }

    /**
     * Create an {@link AttributeKeyTemplate} with type {@link AttributeType#STRING_ARRAY} and the
     * given {@code prefix}.
     */
    public static AttributeKeyTemplate<List<String>> stringArrayKeyTemplate(String prefix) {
        return new AttributeKeyTemplate<>(prefix, AttributeKey::stringArrayKey);
    }

    /**
     * Create an {@link AttributeKeyTemplate} with type {@link AttributeType#BOOLEAN} and the given
     * {@code prefix}.
     */
    public static AttributeKeyTemplate<Boolean> booleanKeyTemplate(String prefix) {
        return new AttributeKeyTemplate<>(prefix, AttributeKey::booleanKey);
    }

    /**
     * Create an {@link AttributeKeyTemplate} with type {@link AttributeType#BOOLEAN_ARRAY} and the
     * given {@code prefix}.
     */
    public static AttributeKeyTemplate<List<Boolean>> booleanArrayKeyTemplate(String prefix) {
        return new AttributeKeyTemplate<>(prefix, AttributeKey::booleanArrayKey);
    }

    /**
     * Create an {@link AttributeKeyTemplate} with type {@link AttributeType#LONG} and the given
     * {@code prefix}.
     */
    public static AttributeKeyTemplate<Long> longKeyTemplate(String prefix) {
        return new AttributeKeyTemplate<>(prefix, AttributeKey::longKey);
    }

    /**
     * Create an {@link AttributeKeyTemplate} with type {@link AttributeType#LONG_ARRAY} and the given
     * {@code prefix}.
     */
    public static AttributeKeyTemplate<List<Long>> longArrayKeyTemplate(String prefix) {
        return new AttributeKeyTemplate<>(prefix, AttributeKey::longArrayKey);
    }

    /**
     * Create an {@link AttributeKeyTemplate} with type {@link AttributeType#DOUBLE} and the given
     * {@code prefix}.
     */
    public static AttributeKeyTemplate<Double> doubleKeyTemplate(String prefix) {
        return new AttributeKeyTemplate<>(prefix, AttributeKey::doubleKey);
    }

    /**
     * Create an {@link AttributeKeyTemplate} with type {@link AttributeType#DOUBLE_ARRAY} and the
     * given {@code prefix}.
     */
    public static AttributeKeyTemplate<List<Double>> doubleArrayKeyTemplate(String prefix) {
        return new AttributeKeyTemplate<>(prefix, AttributeKey::doubleArrayKey);
    }

    private AttributeKey<T> createAttributeKey(String keyName) {
        String key = prefix + "." + keyName;
        return keyBuilder.apply(key);
    }

    /**
     * Returns an {@link AttributeKey} object for the given attribute key whereby the key is the
     * variable part of the full attribute name in a template-typed attribute, for example
     * <b>http.request.header.&lt;key&gt;</b>.
     *
     * <p>{@link AttributeKey} objets are being created and cached on the first invocation of this
     * method for a certain key. Subsequent invocations of this method with the same key return the
     * cached object.
     *
     * @param key The variable part of the template-typed attribute name.
     * @return An {@link AttributeKey} object for the given key.
     */
    public AttributeKey<T> getAttributeKey(String key) {
        return keysCache.computeIfAbsent(key, this::createAttributeKey);
    }
}

