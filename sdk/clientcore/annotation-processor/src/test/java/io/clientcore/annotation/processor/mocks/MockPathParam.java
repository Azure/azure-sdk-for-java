// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.mocks;

import io.clientcore.core.http.annotations.PathParam;

import java.lang.annotation.Annotation;

/**
 * Mock implementation of {@link PathParam} for testing purposes.
 */
public class MockPathParam implements PathParam {
    private final boolean encoded;
    private final String value;

    /**
     * Creates an instance of {@link MockPathParam}.
     * @param value the path param value to be used for creating the mock.
     * @param encoded if the path param is encoded.
     */
    public MockPathParam(String value, boolean encoded) {
        this.value = value;
        this.encoded = encoded;
    }

    @Override
    public boolean encoded() {
        return encoded;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return PathParam.class;
    }
}
