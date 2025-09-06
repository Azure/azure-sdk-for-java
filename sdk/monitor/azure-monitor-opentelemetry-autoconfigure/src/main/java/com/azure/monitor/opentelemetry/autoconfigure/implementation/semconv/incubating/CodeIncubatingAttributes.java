// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

// this is a copy of io.opentelemetry.semconv.incubating.CodeIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class CodeIncubatingAttributes {
    /**
     * The column number in {@code code.filepath} best representing the operation. It SHOULD point
     * within the code unit named in {@code code.function}.
     */
    public static final AttributeKey<Long> CODE_COLUMN = longKey("code.column");

    /**
     * The source code file name that identifies the code unit as uniquely as possible (preferably an
     * absolute file path).
     */
    public static final AttributeKey<String> CODE_FILEPATH = stringKey("code.filepath");

    /**
     * The method or function name, or equivalent (usually rightmost part of the code unit's name).
     */
    public static final AttributeKey<String> CODE_FUNCTION = stringKey("code.function");

    /**
     * The line number in {@code code.filepath} best representing the operation. It SHOULD point
     * within the code unit named in {@code code.function}.
     */
    public static final AttributeKey<Long> CODE_LINENO = longKey("code.lineno");

    /**
     * The "namespace" within which {@code code.function} is defined. Usually the qualified class or
     * module name, such that {@code code.namespace} + some separator + {@code code.function} form a
     * unique identifier for the code unit.
     */
    public static final AttributeKey<String> CODE_NAMESPACE = stringKey("code.namespace");

    /**
     * A stacktrace as a string in the natural representation for the language runtime. The
     * representation is to be determined and documented by each language SIG.
     */
    public static final AttributeKey<String> CODE_STACKTRACE = stringKey("code.stacktrace");

    private CodeIncubatingAttributes() {
    }
}
