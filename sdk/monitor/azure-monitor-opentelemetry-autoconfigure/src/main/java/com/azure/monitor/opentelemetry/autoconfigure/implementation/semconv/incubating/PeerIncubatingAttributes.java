// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

// this is a copy of io.opentelemetry.semconv.incubating.PeerIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class PeerIncubatingAttributes {
    /**
     * The name of the remote service being connected to.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>May be a logical name or a domain name. Examples: "service-a", "10.0.0.1:5555",
     *       "httpbin.org".
     * </ul>
     */
    public static final AttributeKey<String> PEER_SERVICE = stringKey("peer.service");

    private PeerIncubatingAttributes() {
    }
}
