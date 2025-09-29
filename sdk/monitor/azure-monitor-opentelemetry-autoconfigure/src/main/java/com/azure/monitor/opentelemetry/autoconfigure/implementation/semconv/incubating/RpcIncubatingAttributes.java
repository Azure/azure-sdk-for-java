// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.AttributeKeyTemplate.stringArrayKeyTemplate;

import io.opentelemetry.api.common.AttributeKey;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.AttributeKeyTemplate;
import java.util.List;

// this is a copy of io.opentelemetry.semconv.incubating.RpcIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class RpcIncubatingAttributes {
    /**
     * The <a href="https://connectrpc.com//docs/protocol/#error-codes">error codes</a> of the Connect
     * request. Error codes are always string values.
     */
    public static final AttributeKey<String> RPC_CONNECT_RPC_ERROR_CODE = stringKey("rpc.connect_rpc.error_code");

    /**
     * Connect request metadata, {@code <key>} being the normalized Connect Metadata key (lowercase),
     * the value being the metadata values.
     *
     * <p>Notes:
     *
     * <p>Instrumentations SHOULD require an explicit configuration of which metadata values are to be
     * captured. Including all request metadata values can be a security risk - explicit configuration
     * helps avoid leaking sensitive information.
     *
     * <p>For example, a property {@code my-custom-key} with value {@code ["1.2.3.4", "1.2.3.5"]}
     * SHOULD be recorded as the {@code rpc.connect_rpc.request.metadata.my-custom-key} attribute with
     * value {@code ["1.2.3.4", "1.2.3.5"]}
     */
    public static final AttributeKeyTemplate<List<String>> RPC_CONNECT_RPC_REQUEST_METADATA
        = stringArrayKeyTemplate("rpc.connect_rpc.request.metadata");

    /**
     * Connect response metadata, {@code <key>} being the normalized Connect Metadata key (lowercase),
     * the value being the metadata values.
     *
     * <p>Notes:
     *
     * <p>Instrumentations SHOULD require an explicit configuration of which metadata values are to be
     * captured. Including all response metadata values can be a security risk - explicit
     * configuration helps avoid leaking sensitive information.
     *
     * <p>For example, a property {@code my-custom-key} with value {@code "attribute_value"} SHOULD be
     * recorded as the {@code rpc.connect_rpc.response.metadata.my-custom-key} attribute with value
     * {@code ["attribute_value"]}
     */
    public static final AttributeKeyTemplate<List<String>> RPC_CONNECT_RPC_RESPONSE_METADATA
        = stringArrayKeyTemplate("rpc.connect_rpc.response.metadata");

    /**
     * gRPC request metadata, {@code <key>} being the normalized gRPC Metadata key (lowercase), the
     * value being the metadata values.
     *
     * <p>Notes:
     *
     * <p>Instrumentations SHOULD require an explicit configuration of which metadata values are to be
     * captured. Including all request metadata values can be a security risk - explicit configuration
     * helps avoid leaking sensitive information.
     *
     * <p>For example, a property {@code my-custom-key} with value {@code ["1.2.3.4", "1.2.3.5"]}
     * SHOULD be recorded as {@code rpc.grpc.request.metadata.my-custom-key} attribute with value
     * {@code ["1.2.3.4", "1.2.3.5"]}
     */
    public static final AttributeKeyTemplate<List<String>> RPC_GRPC_REQUEST_METADATA
        = stringArrayKeyTemplate("rpc.grpc.request.metadata");

    /**
     * gRPC response metadata, {@code <key>} being the normalized gRPC Metadata key (lowercase), the
     * value being the metadata values.
     *
     * <p>Notes:
     *
     * <p>Instrumentations SHOULD require an explicit configuration of which metadata values are to be
     * captured. Including all response metadata values can be a security risk - explicit
     * configuration helps avoid leaking sensitive information.
     *
     * <p>For example, a property {@code my-custom-key} with value {@code ["attribute_value"]} SHOULD
     * be recorded as the {@code rpc.grpc.response.metadata.my-custom-key} attribute with value {@code
     * ["attribute_value"]}
     */
    public static final AttributeKeyTemplate<List<String>> RPC_GRPC_RESPONSE_METADATA
        = stringArrayKeyTemplate("rpc.grpc.response.metadata");

    /**
     * The <a href="https://github.com/grpc/grpc/blob/v1.33.2/doc/statuscodes.md">numeric status
     * code</a> of the gRPC request.
     */
    public static final AttributeKey<Long> RPC_GRPC_STATUS_CODE = longKey("rpc.grpc.status_code");

    /** {@code error.code} property of response if it is an error response. */
    public static final AttributeKey<Long> RPC_JSONRPC_ERROR_CODE = longKey("rpc.jsonrpc.error_code");

    /** {@code error.message} property of response if it is an error response. */
    public static final AttributeKey<String> RPC_JSONRPC_ERROR_MESSAGE = stringKey("rpc.jsonrpc.error_message");

    /**
     * {@code id} property of request or response. Since protocol allows id to be int, string, {@code
     * null} or missing (for notifications), value is expected to be cast to string for simplicity.
     * Use empty string in case of {@code null} value. Omit entirely if this is a notification.
     */
    public static final AttributeKey<String> RPC_JSONRPC_REQUEST_ID = stringKey("rpc.jsonrpc.request_id");

    /**
     * Protocol version as in {@code jsonrpc} property of request/response. Since JSON-RPC 1.0 doesn't
     * specify this, the value can be omitted.
     */
    public static final AttributeKey<String> RPC_JSONRPC_VERSION = stringKey("rpc.jsonrpc.version");

    /** Compressed size of the message in bytes. */
    public static final AttributeKey<Long> RPC_MESSAGE_COMPRESSED_SIZE = longKey("rpc.message.compressed_size");

    /**
     * MUST be calculated as two different counters starting from {@code 1} one for sent messages and
     * one for received message.
     *
     * <p>Notes:
     *
     * <p>This way we guarantee that the values will be consistent between different implementations.
     */
    public static final AttributeKey<Long> RPC_MESSAGE_ID = longKey("rpc.message.id");

    /** Whether this is a received or sent message. */
    public static final AttributeKey<String> RPC_MESSAGE_TYPE = stringKey("rpc.message.type");

    /** Uncompressed size of the message in bytes. */
    public static final AttributeKey<Long> RPC_MESSAGE_UNCOMPRESSED_SIZE = longKey("rpc.message.uncompressed_size");

    /**
     * The name of the (logical) method being called, must be equal to the $method part in the span
     * name.
     *
     * <p>Notes:
     *
     * <p>This is the logical name of the method from the RPC interface perspective, which can be
     * different from the name of any implementing method/function. The {@code code.function.name}
     * attribute may be used to store the latter (e.g., method actually executing the call on the
     * server side, RPC client stub method on the client side).
     */
    public static final AttributeKey<String> RPC_METHOD = stringKey("rpc.method");

    /**
     * The full (logical) name of the service being called, including its package name, if applicable.
     *
     * <p>Notes:
     *
     * <p>This is the logical name of the service from the RPC interface perspective, which can be
     * different from the name of any implementing class. The {@code code.namespace} attribute may be
     * used to store the latter (despite the attribute name, it may include a class name; e.g., class
     * with method actually executing the call on the server side, RPC client stub class on the client
     * side).
     */
    public static final AttributeKey<String> RPC_SERVICE = stringKey("rpc.service");

    /** A string identifying the remoting system. See below for a list of well-known identifiers. */
    public static final AttributeKey<String> RPC_SYSTEM = stringKey("rpc.system");

    // Enum definitions

    /** Values for {@link #RPC_CONNECT_RPC_ERROR_CODE}. */
    public static final class RpcConnectRpcErrorCodeIncubatingValues {
        /** cancelled. */
        public static final String CANCELLED = "cancelled";

        /** unknown. */
        public static final String UNKNOWN = "unknown";

        /** invalid_argument. */
        public static final String INVALID_ARGUMENT = "invalid_argument";

        /** deadline_exceeded. */
        public static final String DEADLINE_EXCEEDED = "deadline_exceeded";

        /** not_found. */
        public static final String NOT_FOUND = "not_found";

        /** already_exists. */
        public static final String ALREADY_EXISTS = "already_exists";

        /** permission_denied. */
        public static final String PERMISSION_DENIED = "permission_denied";

        /** resource_exhausted. */
        public static final String RESOURCE_EXHAUSTED = "resource_exhausted";

        /** failed_precondition. */
        public static final String FAILED_PRECONDITION = "failed_precondition";

        /** aborted. */
        public static final String ABORTED = "aborted";

        /** out_of_range. */
        public static final String OUT_OF_RANGE = "out_of_range";

        /** unimplemented. */
        public static final String UNIMPLEMENTED = "unimplemented";

        /** internal. */
        public static final String INTERNAL = "internal";

        /** unavailable. */
        public static final String UNAVAILABLE = "unavailable";

        /** data_loss. */
        public static final String DATA_LOSS = "data_loss";

        /** unauthenticated. */
        public static final String UNAUTHENTICATED = "unauthenticated";

        private RpcConnectRpcErrorCodeIncubatingValues() {
        }
    }

    /** Values for {@link #RPC_GRPC_STATUS_CODE}. */
    public static final class RpcGrpcStatusCodeIncubatingValues {
        /** OK */
        public static final long OK = 0;

        /** CANCELLED */
        public static final long CANCELLED = 1;

        /** UNKNOWN */
        public static final long UNKNOWN = 2;

        /** INVALID_ARGUMENT */
        public static final long INVALID_ARGUMENT = 3;

        /** DEADLINE_EXCEEDED */
        public static final long DEADLINE_EXCEEDED = 4;

        /** NOT_FOUND */
        public static final long NOT_FOUND = 5;

        /** ALREADY_EXISTS */
        public static final long ALREADY_EXISTS = 6;

        /** PERMISSION_DENIED */
        public static final long PERMISSION_DENIED = 7;

        /** RESOURCE_EXHAUSTED */
        public static final long RESOURCE_EXHAUSTED = 8;

        /** FAILED_PRECONDITION */
        public static final long FAILED_PRECONDITION = 9;

        /** ABORTED */
        public static final long ABORTED = 10;

        /** OUT_OF_RANGE */
        public static final long OUT_OF_RANGE = 11;

        /** UNIMPLEMENTED */
        public static final long UNIMPLEMENTED = 12;

        /** INTERNAL */
        public static final long INTERNAL = 13;

        /** UNAVAILABLE */
        public static final long UNAVAILABLE = 14;

        /** DATA_LOSS */
        public static final long DATA_LOSS = 15;

        /** UNAUTHENTICATED */
        public static final long UNAUTHENTICATED = 16;

        private RpcGrpcStatusCodeIncubatingValues() {
        }
    }

    /** Values for {@link #RPC_MESSAGE_TYPE}. */
    public static final class RpcMessageTypeIncubatingValues {
        /** sent. */
        public static final String SENT = "SENT";

        /** received. */
        public static final String RECEIVED = "RECEIVED";

        private RpcMessageTypeIncubatingValues() {
        }
    }

    /** Values for {@link #RPC_SYSTEM}. */
    public static final class RpcSystemIncubatingValues {
        /** gRPC */
        public static final String GRPC = "grpc";

        /** Java RMI */
        public static final String JAVA_RMI = "java_rmi";

        /** .NET WCF */
        public static final String DOTNET_WCF = "dotnet_wcf";

        /** Apache Dubbo */
        public static final String APACHE_DUBBO = "apache_dubbo";

        /** Connect RPC */
        public static final String CONNECT_RPC = "connect_rpc";

        private RpcSystemIncubatingValues() {
        }
    }

    private RpcIncubatingAttributes() {
    }
}
