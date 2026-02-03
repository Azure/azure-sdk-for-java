// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

// this is a copy of io.opentelemetry.semconv.CodeAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/java/SemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class CodeAttributes {
    /**
     * The column number in {@code code.file.path} best representing the operation. It SHOULD point
     * within the code unit named in {@code code.function.name}. This attribute MUST NOT be used on
     * the Profile signal since the data is already captured in 'message Line'. This constraint is
     * imposed to prevent redundancy and maintain data integrity.
     */
    public static final AttributeKey<Long> CODE_COLUMN_NUMBER = longKey("code.column.number");

    /**
     * The source code file name that identifies the code unit as uniquely as possible (preferably an
     * absolute file path). This attribute MUST NOT be used on the Profile signal since the data is
     * already captured in 'message Function'. This constraint is imposed to prevent redundancy and
     * maintain data integrity.
     */
    public static final AttributeKey<String> CODE_FILE_PATH = stringKey("code.file.path");

    /**
     * The method or function fully-qualified name without arguments. The value should fit the natural
     * representation of the language runtime, which is also likely the same used within {@code
     * code.stacktrace} attribute value. This attribute MUST NOT be used on the Profile signal since
     * the data is already captured in 'message Function'. This constraint is imposed to prevent
     * redundancy and maintain data integrity.
     *
     * <p>Notes:
     *
     * <p>Values and format depends on each language runtime, thus it is impossible to provide an
     * exhaustive list of examples. The values are usually the same (or prefixes of) the ones found in
     * native stack trace representation stored in {@code code.stacktrace} without information on
     * arguments.
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>Java method: {@code com.example.MyHttpService.serveRequest}
     *   <li>Java anonymous class method: {@code com.mycompany.Main$1.myMethod}
     *   <li>Java lambda method: {@code com.mycompany.Main$$Lambda/0x0000748ae4149c00.myMethod}
     *   <li>PHP function: {@code GuzzleHttp\Client::transfer}
     *   <li>Go function: {@code github.com/my/repo/pkg.foo.func5}
     *   <li>Elixir: {@code OpenTelemetry.Ctx.new}
     *   <li>Erlang: {@code opentelemetry_ctx:new}
     *   <li>Rust: {@code playground::my_module::my_cool_func}
     *   <li>C function: {@code fopen}
     * </ul>
     */
    public static final AttributeKey<String> CODE_FUNCTION_NAME = stringKey("code.function.name");

    /**
     * The line number in {@code code.file.path} best representing the operation. It SHOULD point
     * within the code unit named in {@code code.function.name}. This attribute MUST NOT be used on
     * the Profile signal since the data is already captured in 'message Line'. This constraint is
     * imposed to prevent redundancy and maintain data integrity.
     */
    public static final AttributeKey<Long> CODE_LINE_NUMBER = longKey("code.line.number");

    /**
     * A stacktrace as a string in the natural representation for the language runtime. The
     * representation is identical to <a
     * href="/docs/exceptions/exceptions-spans.md#stacktrace-representation">{@code
     * exception.stacktrace}</a>. This attribute MUST NOT be used on the Profile signal since the data
     * is already captured in 'message Location'. This constraint is imposed to prevent redundancy and
     * maintain data integrity.
     */
    public static final AttributeKey<String> CODE_STACKTRACE = stringKey("code.stacktrace");

    private CodeAttributes() {
    }
}
