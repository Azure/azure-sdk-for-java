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

// this is a copy of io.opentelemetry.semconv.ServerAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/java/SemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class ServerAttributes {
    /**
     * Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix
     * domain socket name.
     *
     * <p>Notes:
     *
     * <p>When observed from the client side, and when communicating through an intermediary, {@code
     * server.address} SHOULD represent the server address behind any intermediaries, for example
     * proxies, if it's available.
     */
    public static final AttributeKey<String> SERVER_ADDRESS = stringKey("server.address");

    /**
     * Server port number.
     *
     * <p>Notes:
     *
     * <p>When observed from the client side, and when communicating through an intermediary, {@code
     * server.port} SHOULD represent the server port behind any intermediaries, for example proxies,
     * if it's available.
     */
    public static final AttributeKey<Long> SERVER_PORT = longKey("server.port");

    private ServerAttributes() {
    }
}
