// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import io.opentelemetry.api.common.AttributeKey;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

public final class AiSemanticAttributes {

    // replaced by ai.preview.connection_string
    // not supported anymore, because if you stamp it on non-recording span then standard metrics
    // will not see it and then be incorrect
    @Deprecated
    public static final AttributeKey<String> DEPRECATED_INSTRUMENTATION_KEY =
        AttributeKey.stringKey("ai.preview.instrumentation_key");

    // not supported anymore, because if you stamp it on non-recording span then standard metrics
    // will not see it and then be incorrect
    @Deprecated
    public static final AttributeKey<String> DEPRECATED_CONNECTION_STRING =
        AttributeKey.stringKey("ai.preview.connection_string");

    // not supported anymore, because if you stamp it on non-recording span then standard metrics
    // will not see it and then be incorrect
    @Deprecated
    public static final AttributeKey<String> DEPRECATED_ROLE_NAME =
        AttributeKey.stringKey("ai.preview.service_name");

    // not supported anymore, because if you stamp it on non-recording span then standard metrics
    // will not see it and then be incorrect
    @Deprecated
    public static final AttributeKey<String> DEPRECATED_ROLE_INSTANCE =
        AttributeKey.stringKey("ai.preview.service_instance_id");

    public static final AttributeKey<String> PREVIEW_APPLICATION_VERSION =
        AttributeKey.stringKey("ai.preview.service_version");

    public static final AttributeKey<String> INTERNAL_CONNECTION_STRING =
        AttributeKey.stringKey("applicationinsights.internal.connection_string");

    public static final AttributeKey<String> INTERNAL_ROLE_NAME =
        AttributeKey.stringKey("applicationinsights.internal.service_name");

    public static final AttributeKey<String> OPERATION_NAME =
        stringKey("applicationinsights.internal.operation_name");

    public static final AttributeKey<Double> SAMPLE_RATE =
        doubleKey("applicationinsights.internal.sample_rate");

    // marks whether a request is coming from a "real" user, or a "synthetic" user (e.g. a bot or
    // health check)
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
    public static final AttributeKey<String> AZURE_SDK_DB_TYPE = AttributeKey.stringKey("db.type");
    public static final AttributeKey<String> AZURE_SDK_DB_INSTANCE =
        AttributeKey.stringKey("db.instance");
    public static final AttributeKey<String> AZURE_SDK_DB_URL = AttributeKey.stringKey("db.url");

    public static final AttributeKey<Long> KAFKA_RECORD_QUEUE_TIME_MS =
        AttributeKey.longKey("kafka.record.queue_time_ms");
    public static final AttributeKey<Long> KAFKA_OFFSET = AttributeKey.longKey("kafka.offset");

    public static final AttributeKey<String> JOB_SYSTEM = AttributeKey.stringKey("job.system");

    public static final AttributeKey<Boolean> IS_PRE_AGGREGATED =
        AttributeKey.booleanKey("applicationinsights.internal.is_pre_aggregated");

    public static final AttributeKey<String> LOGGED_EXCEPTION =
        AttributeKey.stringKey("applicationinsights.internal.logged_exception");

    // These attributes are specific for Azure Function and are added to Application Insights traces'
    // custom dimensions. When Azure Function host starts suppressing the same logs to Application
    // Insights, these new attributes will provide backward compatibility for keeping the same
    // behaviour.
    public static final AttributeKey<String> AZ_FN_INVOCATION_ID =
        AttributeKey.stringKey("applicationinsights.internal.invocationId");
    public static final AttributeKey<String> AZ_FN_PROCESS_ID =
        AttributeKey.stringKey("applicationinsights.internal.processId");
    public static final AttributeKey<String> AZ_FN_LOG_LEVEL =
        AttributeKey.stringKey("applicationinsights.internal.logLevel");
    public static final AttributeKey<String> AZ_FN_CATEGORY =
        AttributeKey.stringKey("applicationinsights.internal.category");
    public static final AttributeKey<String> AZ_FN_HOST_INSTANCE_ID =
        AttributeKey.stringKey("applicationinsights.internal.hostInstanceId");
    public static final AttributeKey<String> AZ_FN_LIVE_LOGS_SESSION_ID =
        AttributeKey.stringKey("applicationinsights.internal.azFuncLiveLogsSessionId");

    private AiSemanticAttributes() {
    }
}
