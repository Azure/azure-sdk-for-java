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

// this is a copy of io.opentelemetry.semconv.ClientAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/java/SemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class ClientAttributes {
    /**
     * Client address - domain name if available without reverse DNS lookup; otherwise, IP address or
     * Unix domain socket name.
     *
     * <p>Notes:
     *
     * <p>When observed from the server side, and when communicating through an intermediary, {@code
     * client.address} SHOULD represent the client address behind any intermediaries, for example
     * proxies, if it's available.
     */
    public static final AttributeKey<String> CLIENT_ADDRESS = stringKey("client.address");

    /**
     * Client port number.
     *
     * <p>Notes:
     *
     * <p>When observed from the server side, and when communicating through an intermediary, {@code
     * client.port} SHOULD represent the client port behind any intermediaries, for example proxies,
     * if it's available.
     */
    public static final AttributeKey<Long> CLIENT_PORT = longKey("client.port");

    private ClientAttributes() {
    }
}
