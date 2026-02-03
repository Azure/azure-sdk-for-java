// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

// this is a copy of io.opentelemetry.semconv.ExceptionAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/java/SemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class ExceptionAttributes {
    /** The exception message. */
    public static final AttributeKey<String> EXCEPTION_MESSAGE = stringKey("exception.message");

    /**
     * A stacktrace as a string in the natural representation for the language runtime. The
     * representation is to be determined and documented by each language SIG.
     */
    public static final AttributeKey<String> EXCEPTION_STACKTRACE = stringKey("exception.stacktrace");

    /**
     * The type of the exception (its fully-qualified class name, if applicable). The dynamic type of
     * the exception should be preferred over the static type in languages that support it.
     */
    public static final AttributeKey<String> EXCEPTION_TYPE = stringKey("exception.type");

    private ExceptionAttributes() {
    }
}
