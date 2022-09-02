/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation;

import io.opentelemetry.api.common.AttributeKey;

import java.util.List;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

public final class AiSemanticAttributes {

    // replaced by ai.preview.connection_string
    @Deprecated
    public static final AttributeKey<String> INSTRUMENTATION_KEY =
        AttributeKey.stringKey("ai.preview.instrumentation_key");

    public static final AttributeKey<String> CONNECTION_STRING =
        AttributeKey.stringKey("ai.preview.connection_string");

    public static final AttributeKey<String> ROLE_NAME =
        AttributeKey.stringKey("ai.preview.service_name");

    public static final AttributeKey<String> ROLE_INSTANCE_ID =
        AttributeKey.stringKey("ai.preview.service_instance_id");

    public static final AttributeKey<String> APPLICATION_VERSION =
        AttributeKey.stringKey("ai.preview.service_version");

    public static final AttributeKey<String> OPERATION_NAME =
        stringKey("applicationinsights.internal.operation_name");

    public static final AttributeKey<Long> ITEM_COUNT =
        longKey("applicationinsights.internal.item_count");

    public static final AttributeKey<Boolean> IS_SYNTHETIC =
        booleanKey("applicationinsights.internal.is_synthetic");

    // this is only used by the 2.x web interop bridge
    // for ThreadContext.getRequestTelemetryContext().getRequestTelemetry().setSource()
    public static final AttributeKey<String> SPAN_SOURCE =
        AttributeKey.stringKey("applicationinsights.internal.source");

    public static final AttributeKey<String> SESSION_ID =
        AttributeKey.stringKey("applicationinsights.internal.session_id");

    public static final AttributeKey<String> DEVICE_OS =
        AttributeKey.stringKey("applicationinsights.internal.operating_system");

    public static final AttributeKey<String> DEVICE_OS_VERSION =
        AttributeKey.stringKey("applicationinsights.internal.operating_system_version");

    // TODO (trask) remove once this makes it into SemanticAttributes
    public static final AttributeKey<String> NET_SOCK_PEER_ADDR =
        AttributeKey.stringKey("net.sock.peer.addr");

    // TODO (trask) this can go away once new indexer is rolled out to gov clouds
    public static final AttributeKey<List<String>> REQUEST_CONTEXT =
        AttributeKey.stringArrayKey("http.response.header.request_context");

    public static final AttributeKey<String> LEGACY_PARENT_ID =
        AttributeKey.stringKey("applicationinsights.internal.legacy_parent_id");
    public static final AttributeKey<String> LEGACY_ROOT_ID =
        AttributeKey.stringKey("applicationinsights.internal.legacy_root_id");

    public static final AttributeKey<String> AZURE_SDK_NAMESPACE =
        AttributeKey.stringKey("az.namespace");
    public static final AttributeKey<String> AZURE_SDK_PEER_ADDRESS =
        AttributeKey.stringKey("peer.address");
    public static final AttributeKey<String> AZURE_SDK_MESSAGE_BUS_DESTINATION =
        AttributeKey.stringKey("message_bus.destination");
    public static final AttributeKey<Long> AZURE_SDK_ENQUEUED_TIME =
        AttributeKey.longKey("x-opt-enqueued-time");

    public static final AttributeKey<Long> KAFKA_RECORD_QUEUE_TIME_MS =
        AttributeKey.longKey("kafka.record.queue_time_ms");
    public static final AttributeKey<Long> KAFKA_OFFSET = AttributeKey.longKey("kafka.offset");

    public static final AttributeKey<Boolean> IS_PRE_AGGREGATED =
        AttributeKey.booleanKey("applicationinsights.internal.is_pre_aggregated");

    private AiSemanticAttributes() {
    }
}
