// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.core.util.BinaryData;

/**
 * Wrapper class for supplying {@link BinaryData} to parameterized tests. Spock calls toString() on parameters, and
 * for some {@code BinaryDataContent} implementations, toString() consumes a stream that cannot be re-consumed.
 * Wrapping the parameter with a safe toString prevents this (and also provides a more helpful string for Spock
 * outputs).
 */
public final class BinaryDataParamWrapper {
    private final BinaryData data;
    private final String description;

    public BinaryDataParamWrapper(BinaryData data, String description) {
        this.data = data;
        this.description = description;
    }

    public BinaryData getData() {
        return data;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
