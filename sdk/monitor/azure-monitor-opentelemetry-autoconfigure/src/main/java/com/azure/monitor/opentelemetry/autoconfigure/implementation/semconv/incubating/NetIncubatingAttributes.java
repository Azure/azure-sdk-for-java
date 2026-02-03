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

// this is a copy of io.opentelemetry.semconv.incubating.NetIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class NetIncubatingAttributes {
    /**
     * Deprecated, use {@code network.local.address}.
     *
     * @deprecated Replaced by {@code network.local.address}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_HOST_IP = stringKey("net.host.ip");

    /**
     * Deprecated, use {@code server.address}.
     *
     * @deprecated Replaced by {@code server.address}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_HOST_NAME = stringKey("net.host.name");

    /**
     * Deprecated, use {@code server.port}.
     *
     * @deprecated Replaced by {@code server.port}.
     */
    @Deprecated
    public static final AttributeKey<Long> NET_HOST_PORT = longKey("net.host.port");

    /**
     * Deprecated, use {@code network.peer.address}.
     *
     * @deprecated Replaced by {@code network.peer.address}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_PEER_IP = stringKey("net.peer.ip");

    /**
     * Deprecated, use {@code server.address} on client spans and {@code client.address} on server
     * spans.
     *
     * @deprecated Replaced by {@code server.address} on client spans and {@code client.address} on
     *     server spans.
     */
    @Deprecated
    public static final AttributeKey<String> NET_PEER_NAME = stringKey("net.peer.name");

    /**
     * Deprecated, use {@code server.port} on client spans and {@code client.port} on server spans.
     *
     * @deprecated Replaced by {@code server.port} on client spans and {@code client.port} on server
     *     spans.
     */
    @Deprecated
    public static final AttributeKey<Long> NET_PEER_PORT = longKey("net.peer.port");

    /**
     * Deprecated, use {@code network.protocol.name}.
     *
     * @deprecated Replaced by {@code network.protocol.name}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_PROTOCOL_NAME = stringKey("net.protocol.name");

    /**
     * Deprecated, use {@code network.protocol.version}.
     *
     * @deprecated Replaced by {@code network.protocol.version}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_PROTOCOL_VERSION = stringKey("net.protocol.version");

    /**
     * Deprecated, use {@code network.transport} and {@code network.type}.
     *
     * @deprecated Split to {@code network.transport} and {@code network.type}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_SOCK_FAMILY = stringKey("net.sock.family");

    /**
     * Deprecated, use {@code network.local.address}.
     *
     * @deprecated Replaced by {@code network.local.address}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_SOCK_HOST_ADDR = stringKey("net.sock.host.addr");

    /**
     * Deprecated, use {@code network.local.port}.
     *
     * @deprecated Replaced by {@code network.local.port}.
     */
    @Deprecated
    public static final AttributeKey<Long> NET_SOCK_HOST_PORT = longKey("net.sock.host.port");

    /**
     * Deprecated, use {@code network.peer.address}.
     *
     * @deprecated Replaced by {@code network.peer.address}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_SOCK_PEER_ADDR = stringKey("net.sock.peer.addr");

    /**
     * Deprecated, no replacement at this time.
     *
     * @deprecated Removed. No replacement at this time.
     */
    @Deprecated
    public static final AttributeKey<String> NET_SOCK_PEER_NAME = stringKey("net.sock.peer.name");

    /**
     * Deprecated, use {@code network.peer.port}.
     *
     * @deprecated Replaced by {@code network.peer.port}.
     */
    @Deprecated
    public static final AttributeKey<Long> NET_SOCK_PEER_PORT = longKey("net.sock.peer.port");

    /**
     * Deprecated, use {@code network.transport}.
     *
     * @deprecated Replaced by {@code network.transport}.
     */
    @Deprecated
    public static final AttributeKey<String> NET_TRANSPORT = stringKey("net.transport");

    // Enum definitions

    /**
     * Values for {@link #NET_SOCK_FAMILY}
     *
     * @deprecated Split to {@code network.transport} and {@code network.type}.
     */
    @Deprecated
    public static final class NetSockFamilyIncubatingValues {
        /** IPv4 address */
        public static final String INET = "inet";

        /** IPv6 address */
        public static final String INET6 = "inet6";

        /** Unix domain socket path */
        public static final String UNIX = "unix";

        private NetSockFamilyIncubatingValues() {
        }
    }

    /**
     * Values for {@link #NET_TRANSPORT}
     *
     * @deprecated Replaced by {@code network.transport}.
     */
    @Deprecated
    public static final class NetTransportIncubatingValues {
        /** ip_tcp. */
        public static final String IP_TCP = "ip_tcp";

        /** ip_udp. */
        public static final String IP_UDP = "ip_udp";

        /** Named or anonymous pipe. */
        public static final String PIPE = "pipe";

        /** In-process communication. */
        public static final String INPROC = "inproc";

        /** Something else (non IP-based). */
        public static final String OTHER = "other";

        private NetTransportIncubatingValues() {
        }
    }

    private NetIncubatingAttributes() {
    }
}
