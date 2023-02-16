// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.ExceptionTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.Exceptions;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MessageTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RemoteDependencyTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RequestTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.WarningLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedDuration;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedTime;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.UrlParser;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import reactor.util.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class SpanDataMapper {

    // visible for testing
    public static final String MS_PROCESSED_BY_METRIC_EXTRACTORS = "_MS.ProcessedByMetricExtractors";

    private static final Set<String> SQL_DB_SYSTEMS =
        new HashSet<>(
            asList(
                SemanticAttributes.DbSystemValues.DB2,
                SemanticAttributes.DbSystemValues.DERBY,
                SemanticAttributes.DbSystemValues.MARIADB,
                SemanticAttributes.DbSystemValues.MSSQL,
                SemanticAttributes.DbSystemValues.MYSQL,
                SemanticAttributes.DbSystemValues.ORACLE,
                SemanticAttributes.DbSystemValues.POSTGRESQL,
                SemanticAttributes.DbSystemValues.SQLITE,
                SemanticAttributes.DbSystemValues.OTHER_SQL,
                SemanticAttributes.DbSystemValues.HSQLDB,
                SemanticAttributes.DbSystemValues.H2));

    private static final Mappings MAPPINGS;

    // TODO (trask) add to generated ContextTagKeys class
    private static final ContextTagKeys AI_DEVICE_OS = ContextTagKeys.fromString("ai.device.os");

    static {
        MappingsBuilder mappingsBuilder =
            new MappingsBuilder()
                // these are from azure SDK (AZURE_SDK_PEER_ADDRESS gets filtered out automatically
                // since it uses the otel "peer." prefix)
                .ignoreExact(AiSemanticAttributes.AZURE_SDK_NAMESPACE.getKey())
                .ignoreExact(AiSemanticAttributes.AZURE_SDK_MESSAGE_BUS_DESTINATION.getKey())
                .ignoreExact(AiSemanticAttributes.AZURE_SDK_ENQUEUED_TIME.getKey())
                .ignoreExact(AiSemanticAttributes.KAFKA_RECORD_QUEUE_TIME_MS.getKey())
                .ignoreExact(AiSemanticAttributes.KAFKA_OFFSET.getKey())
                .exact(
                    SemanticAttributes.HTTP_USER_AGENT.getKey(),
                    (builder, value) -> {
                        if (value instanceof String) {
                            builder.addTag("ai.user.userAgent", (String) value);
                        }
                    })
                .ignorePrefix("applicationinsights.internal.")
                .prefix(
                    "http.request.header.",
                    (telemetryBuilder, key, value) -> {
                        if (value instanceof List) {
                            telemetryBuilder.addProperty(key, Mappings.join((List<?>) value));
                        }
                    })
                .prefix(
                    "http.response.header.",
                    (telemetryBuilder, key, value) -> {
                        if (value instanceof List) {
                            telemetryBuilder.addProperty(key, Mappings.join((List<?>) value));
                        }
                    });

        applyCommonTags(mappingsBuilder);

        MAPPINGS = mappingsBuilder.build();
    }

    private final boolean captureHttpServer4xxAsError;
    private final BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer;
    private final BiPredicate<EventData, String> eventSuppressor;

    public SpanDataMapper(
        boolean captureHttpServer4xxAsError,
        BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer,
        BiPredicate<EventData, String> eventSuppressor) {
        this.captureHttpServer4xxAsError = captureHttpServer4xxAsError;
        this.telemetryInitializer = telemetryInitializer;
        this.eventSuppressor = eventSuppressor;
    }

    public TelemetryItem map(SpanData span) {
        long itemCount = getItemCount(span);
        return map(span, itemCount);
    }

    public void map(SpanData span, Consumer<TelemetryItem> consumer) {
        long itemCount = getItemCount(span);
        TelemetryItem telemetryItem = map(span, itemCount);
        consumer.accept(telemetryItem);
        exportEvents(
            span,
            telemetryItem.getTags().get(ContextTagKeys.AI_OPERATION_NAME.toString()),
            itemCount,
            consumer);
    }

    public TelemetryItem map(SpanData span, long itemCount) {
        if (RequestChecker.isRequest(span)) {
            return exportRequest(span, itemCount);
        } else {
            return exportRemoteDependency(span, span.getKind() == SpanKind.INTERNAL, itemCount);
        }
    }

    private static boolean checkIsPreAggregatedStandardMetric(SpanData span) {
        Boolean isPreAggregatedStandardMetric =
            span.getAttributes().get(AiSemanticAttributes.IS_PRE_AGGREGATED);
        return isPreAggregatedStandardMetric != null && isPreAggregatedStandardMetric;
    }

    private TelemetryItem exportRemoteDependency(SpanData span, boolean inProc, long itemCount) {
        RemoteDependencyTelemetryBuilder telemetryBuilder = RemoteDependencyTelemetryBuilder.create();
        telemetryInitializer.accept(telemetryBuilder, span.getResource());

        // set standard properties
        setOperationTags(telemetryBuilder, span);
        setTime(telemetryBuilder, span.getStartEpochNanos());
        setItemCount(telemetryBuilder, itemCount);

        // update tags
        MAPPINGS.map(span.getAttributes(), telemetryBuilder);

        addLinks(telemetryBuilder, span.getLinks());

        // set dependency-specific properties
        telemetryBuilder.setId(span.getSpanId());
        telemetryBuilder.setName(getDependencyName(span));
        telemetryBuilder.setDuration(
            FormattedDuration.fromNanos(span.getEndEpochNanos() - span.getStartEpochNanos()));
        telemetryBuilder.setSuccess(getSuccess(span));

        if (inProc) {
            telemetryBuilder.setType("InProc");
        } else {
            applySemanticConventions(telemetryBuilder, span);
        }

        if (checkIsPreAggregatedStandardMetric(span)) {
            telemetryBuilder.addProperty(MS_PROCESSED_BY_METRIC_EXTRACTORS, "True");
        }

        return telemetryBuilder.build();
    }

    private static final Set<String> DEFAULT_HTTP_SPAN_NAMES =
        new HashSet<>(
            asList(
                "HTTP OPTIONS",
                "HTTP GET",
                "HTTP HEAD",
                "HTTP POST",
                "HTTP PUT",
                "HTTP DELETE",
                "HTTP TRACE",
                "HTTP CONNECT",
                "HTTP PATCH"));

    // the backend product prefers more detailed (but possibly infinite cardinality) name for http
    // dependencies
    private static String getDependencyName(SpanData span) {
        String name = span.getName();

        String method = span.getAttributes().get(SemanticAttributes.HTTP_METHOD);
        if (method == null) {
            return name;
        }

        if (!DEFAULT_HTTP_SPAN_NAMES.contains(name)) {
            return name;
        }

        String url = span.getAttributes().get(SemanticAttributes.HTTP_URL);
        if (url == null) {
            return name;
        }

        String path = UrlParser.getPath(url);
        if (path == null) {
            return name;
        }
        return path.isEmpty() ? method + " /" : method + " " + path;
    }

    private static void applySemanticConventions(
        RemoteDependencyTelemetryBuilder telemetryBuilder, SpanData span) {
        Attributes attributes = span.getAttributes();
        String httpMethod = attributes.get(SemanticAttributes.HTTP_METHOD);
        if (httpMethod != null) {
            applyHttpClientSpan(telemetryBuilder, attributes);
            return;
        }
        String rpcSystem = attributes.get(SemanticAttributes.RPC_SYSTEM);
        if (rpcSystem != null) {
            applyRpcClientSpan(telemetryBuilder, rpcSystem, attributes);
            return;
        }
        String dbSystem = attributes.get(SemanticAttributes.DB_SYSTEM);
        if (dbSystem != null) {
            applyDatabaseClientSpan(telemetryBuilder, dbSystem, attributes);
            return;
        }
        String messagingSystem = getMessagingSystem(attributes);
        if (messagingSystem != null) {
            applyMessagingClientSpan(telemetryBuilder, span.getKind(), messagingSystem, attributes);
            return;
        }

        // passing max value because we don't know what the default port would be in this case,
        // so we always want the port included
        String target = getTargetOrNull(attributes, Integer.MAX_VALUE);
        if (target != null) {
            telemetryBuilder.setTarget(target);
            return;
        }

        // with no target, the App Map falls back to creating a node based on the telemetry name,
        // which is very confusing, e.g. when multiple unrelated nodes all point to a single node
        // because they had dependencies with the same telemetry name
        //
        // so we mark these as InProc, even though they aren't INTERNAL spans,
        // in order to prevent App Map from considering them
        telemetryBuilder.setType("InProc");
    }

    @Nullable
    private static String getMessagingSystem(Attributes attributes) {
        String azureNamespace = attributes.get(AiSemanticAttributes.AZURE_SDK_NAMESPACE);
        if (isAzureSdkMessaging(azureNamespace)) {
            // special case needed until Azure SDK moves to OTel semantic conventions
            return azureNamespace;
        }
        return attributes.get(SemanticAttributes.MESSAGING_SYSTEM);
    }

    private static void setOperationTags(AbstractTelemetryBuilder telemetryBuilder, SpanData span) {
        setOperationId(telemetryBuilder, span.getTraceId());
        setOperationParentId(telemetryBuilder, span.getParentSpanContext().getSpanId());
        setOperationName(telemetryBuilder, span.getAttributes());
    }

    private static void setOperationId(AbstractTelemetryBuilder telemetryBuilder, String traceId) {
        telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_ID.toString(), traceId);
    }

    private static void setOperationParentId(
        AbstractTelemetryBuilder telemetryBuilder, String parentSpanId) {
        if (SpanId.isValid(parentSpanId)) {
            telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), parentSpanId);
        }
    }

    private static void setOperationName(
        AbstractTelemetryBuilder telemetryBuilder, Attributes attributes) {
        String operationName = attributes.get(AiSemanticAttributes.OPERATION_NAME);
        if (operationName != null) {
            setOperationName(telemetryBuilder, operationName);
        }
    }

    private static void setOperationName(
        AbstractTelemetryBuilder telemetryBuilder, String operationName) {
        telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_NAME.toString(), operationName);
    }

    private static void applyHttpClientSpan(
        RemoteDependencyTelemetryBuilder telemetryBuilder, Attributes attributes) {

        String httpUrl = attributes.get(SemanticAttributes.HTTP_URL);
        int defaultPort = getDefaultPortForHttpUrl(httpUrl);
        String target = getTargetOrDefault(attributes, defaultPort, "Http");

        telemetryBuilder.setType("Http");
        telemetryBuilder.setTarget(target);

        Long httpStatusCode = attributes.get(SemanticAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode != null) {
            telemetryBuilder.setResultCode(Long.toString(httpStatusCode));
        }

        telemetryBuilder.setData(httpUrl);
    }

    private static void applyRpcClientSpan(
        RemoteDependencyTelemetryBuilder telemetryBuilder, String rpcSystem, Attributes attributes) {
        telemetryBuilder.setType(rpcSystem);
        String target = getTargetOrDefault(attributes, Integer.MAX_VALUE, rpcSystem);
        // not appending /rpc.service for now since that seems too fine-grained
        telemetryBuilder.setTarget(target);
    }

    private static int getDefaultPortForHttpUrl(@Nullable String httpUrl) {
        if (httpUrl == null) {
            return Integer.MAX_VALUE;
        }
        if (httpUrl.startsWith("https://")) {
            return 443;
        }
        if (httpUrl.startsWith("http://")) {
            return 80;
        }
        return Integer.MAX_VALUE;
    }

    public static String getTargetOrDefault(
        Attributes attributes, int defaultPort, String defaultTarget) {
        String target = getTargetOrNull(attributes, defaultPort);
        return target != null ? target : defaultTarget;
    }

    @Nullable
    private static String getTargetOrNull(Attributes attributes, int defaultPort) {
        String peerService = attributes.get(SemanticAttributes.PEER_SERVICE);
        if (peerService != null) {
            return peerService;
        }
        String host = attributes.get(SemanticAttributes.NET_PEER_NAME);
        if (host != null) {
            Long port = attributes.get(SemanticAttributes.NET_PEER_PORT);
            return getTarget(host, port, defaultPort);
        }
        host = attributes.get(AiSemanticAttributes.NET_SOCK_PEER_NAME);
        if (host == null) {
            host = attributes.get(AiSemanticAttributes.NET_SOCK_PEER_ADDR);
        }
        if (host != null) {
            Long port = attributes.get(AiSemanticAttributes.NET_SOCK_PEER_PORT);
            return getTarget(host, port, defaultPort);
        }
        String httpUrl = attributes.get(SemanticAttributes.HTTP_URL);
        if (httpUrl != null) {
            // this is needed for instrumentations which don't yet follow the latest OpenTelemetry
            // semantic attributes (in particular Azure SDK instrumentation)
            return UrlParser.getTarget(httpUrl);
        }
        return null;
    }

    private static String getTarget(String host, @Nullable Long port, int defaultPort) {
        if (port != null && port != defaultPort) {
            return host + ":" + port;
        } else {
            return host;
        }
    }

    private static void applyDatabaseClientSpan(
        RemoteDependencyTelemetryBuilder telemetryBuilder, String dbSystem, Attributes attributes) {
        String dbStatement = attributes.get(SemanticAttributes.DB_STATEMENT);
        if (dbStatement == null) {
            dbStatement = attributes.get(SemanticAttributes.DB_OPERATION);
        }
        String type;
        if (SQL_DB_SYSTEMS.contains(dbSystem)) {
            if (dbSystem.equals(SemanticAttributes.DbSystemValues.MYSQL)) {
                type = "mysql"; // this has special icon in portal
            } else if (dbSystem.equals(SemanticAttributes.DbSystemValues.POSTGRESQL)) {
                type = "postgresql"; // this has special icon in portal
            } else {
                type = "SQL";
            }
        } else {
            type = dbSystem;
        }
        telemetryBuilder.setType(type);
        telemetryBuilder.setData(dbStatement);
        String target =
            nullAwareConcat(
                getTargetOrDefault(attributes, getDefaultPortForDbSystem(dbSystem), dbSystem),
                attributes.get(SemanticAttributes.DB_NAME),
                " | ");
        if (target == null) {
            target = dbSystem;
        }
        telemetryBuilder.setTarget(target);
    }

    private static void applyMessagingClientSpan(
        RemoteDependencyTelemetryBuilder telemetryBuilder,
        SpanKind spanKind,
        String messagingSystem,
        Attributes attributes) {
        if (spanKind == SpanKind.PRODUCER) {
            telemetryBuilder.setType("Queue Message | " + messagingSystem);
        } else {
            // e.g. CONSUMER kind (without remote parent) and CLIENT kind
            telemetryBuilder.setType(messagingSystem);
        }
        telemetryBuilder.setTarget(getMessagingTargetSource(attributes));
    }

    private static int getDefaultPortForDbSystem(String dbSystem) {
        // jdbc default ports are from
        // io.opentelemetry.javaagent.instrumentation.jdbc.JdbcConnectionUrlParser
        // TODO (trask) make the ports constants (at least in JdbcConnectionUrlParser) so they can be
        // used here
        switch (dbSystem) {
            case SemanticAttributes.DbSystemValues.MONGODB:
                return 27017;
            case SemanticAttributes.DbSystemValues.CASSANDRA:
                return 9042;
            case SemanticAttributes.DbSystemValues.REDIS:
                return 6379;
            case SemanticAttributes.DbSystemValues.MARIADB:
            case SemanticAttributes.DbSystemValues.MYSQL:
                return 3306;
            case SemanticAttributes.DbSystemValues.MSSQL:
                return 1433;
            case SemanticAttributes.DbSystemValues.DB2:
                return 50000;
            case SemanticAttributes.DbSystemValues.ORACLE:
                return 1521;
            case SemanticAttributes.DbSystemValues.H2:
                return 8082;
            case SemanticAttributes.DbSystemValues.DERBY:
                return 1527;
            case SemanticAttributes.DbSystemValues.POSTGRESQL:
                return 5432;
            default:
                return Integer.MAX_VALUE;
        }
    }

    private TelemetryItem exportRequest(SpanData span, long itemCount) {
        RequestTelemetryBuilder telemetryBuilder = RequestTelemetryBuilder.create();
        telemetryInitializer.accept(telemetryBuilder, span.getResource());

        Attributes attributes = span.getAttributes();
        long startEpochNanos = span.getStartEpochNanos();

        // set standard properties
        telemetryBuilder.setId(span.getSpanId());
        setTime(telemetryBuilder, startEpochNanos);
        setItemCount(telemetryBuilder, itemCount);

        // update tags
        MAPPINGS.map(attributes, telemetryBuilder);

        addLinks(telemetryBuilder, span.getLinks());

        String operationName = getOperationName(span);
        telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_NAME.toString(), operationName);
        telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_ID.toString(), span.getTraceId());

        // see behavior specified at https://github.com/microsoft/ApplicationInsights-Java/issues/1174
        String aiLegacyParentId = span.getAttributes().get(AiSemanticAttributes.LEGACY_PARENT_ID);
        if (aiLegacyParentId != null) {
            // this was the real (legacy) parent id, but it didn't fit span id format
            telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), aiLegacyParentId);
        } else if (span.getParentSpanContext().isValid()) {
            telemetryBuilder.addTag(
                ContextTagKeys.AI_OPERATION_PARENT_ID.toString(),
                span.getParentSpanContext().getSpanId());
        }
        String aiLegacyRootId = span.getAttributes().get(AiSemanticAttributes.LEGACY_ROOT_ID);
        if (aiLegacyRootId != null) {
            telemetryBuilder.addTag("ai_legacyRootID", aiLegacyRootId);
        }

        // set request-specific properties
        telemetryBuilder.setName(operationName);
        telemetryBuilder.setDuration(
            FormattedDuration.fromNanos(span.getEndEpochNanos() - startEpochNanos));
        telemetryBuilder.setSuccess(getSuccess(span));

        String httpUrl = getHttpUrlFromServerSpan(attributes);
        if (httpUrl != null) {
            telemetryBuilder.setUrl(httpUrl);
        }

        Long httpStatusCode = attributes.get(SemanticAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode == null) {
            httpStatusCode = attributes.get(SemanticAttributes.RPC_GRPC_STATUS_CODE);
        }
        if (httpStatusCode != null) {
            telemetryBuilder.setResponseCode(Long.toString(httpStatusCode));
        } else {
            telemetryBuilder.setResponseCode("0");
        }

        String locationIp = attributes.get(SemanticAttributes.HTTP_CLIENT_IP);
        if (locationIp == null) {
            // only use net.peer.ip if http.client_ip is not available
            locationIp = attributes.get(AiSemanticAttributes.NET_SOCK_PEER_ADDR);
        }
        if (locationIp != null) {
            telemetryBuilder.addTag(ContextTagKeys.AI_LOCATION_IP.toString(), locationIp);
        }

        telemetryBuilder.setSource(getSource(attributes));

        String sessionId = attributes.get(AiSemanticAttributes.SESSION_ID);
        if (sessionId != null) {
            // this is only used by the 2.x web interop bridge for
            // ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getContext().getSession().setId()
            telemetryBuilder.addTag(ContextTagKeys.AI_SESSION_ID.toString(), sessionId);
        }
        String deviceOs = attributes.get(AiSemanticAttributes.DEVICE_OS);
        if (deviceOs != null) {
            // this is only used by the 2.x web interop bridge for
            // ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getContext().getDevice().setOperatingSystem()
            telemetryBuilder.addTag(AI_DEVICE_OS.toString(), deviceOs);
        }
        String deviceOsVersion = attributes.get(AiSemanticAttributes.DEVICE_OS_VERSION);
        if (deviceOsVersion != null) {
            // this is only used by the 2.x web interop bridge for
            // ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getContext().getDevice().setOperatingSystemVersion()
            telemetryBuilder.addTag(ContextTagKeys.AI_DEVICE_OS_VERSION.toString(), deviceOsVersion);
        }

        if (checkIsPreAggregatedStandardMetric(span)) {
            telemetryBuilder.addProperty(MS_PROCESSED_BY_METRIC_EXTRACTORS, "True");
        }

        // TODO(trask)? for batch consumer, enqueuedTime should be the average of this attribute
        //  across all links
        Long enqueuedTime = attributes.get(AiSemanticAttributes.AZURE_SDK_ENQUEUED_TIME);
        if (enqueuedTime != null) {
            long timeSinceEnqueuedMillis =
                Math.max(
                    0L, NANOSECONDS.toMillis(span.getStartEpochNanos()) - SECONDS.toMillis(enqueuedTime));
            telemetryBuilder.addMeasurement("timeSinceEnqueued", (double) timeSinceEnqueuedMillis);
        }
        Long timeSinceEnqueuedMillis = attributes.get(AiSemanticAttributes.KAFKA_RECORD_QUEUE_TIME_MS);
        if (timeSinceEnqueuedMillis != null) {
            telemetryBuilder.addMeasurement("timeSinceEnqueued", (double) timeSinceEnqueuedMillis);
        }

        return telemetryBuilder.build();
    }

    private boolean getSuccess(SpanData span) {
        switch (span.getStatus().getStatusCode()) {
            case ERROR:
                return false;
            case OK:
                // instrumentation never sets OK, so this is explicit user override
                return true;
            case UNSET:
                if (captureHttpServer4xxAsError) {
                    Long statusCode = span.getAttributes().get(SemanticAttributes.HTTP_STATUS_CODE);
                    return statusCode == null || statusCode < 400;
                }
                return true;
        }
        return true;
    }

    @Nullable
    public static String getHttpUrlFromServerSpan(Attributes attributes) {
        String httpUrl = attributes.get(SemanticAttributes.HTTP_URL);
        if (httpUrl != null) {
            return httpUrl;
        }
        String scheme = attributes.get(SemanticAttributes.HTTP_SCHEME);
        if (scheme == null) {
            return null;
        }
        String target = attributes.get(SemanticAttributes.HTTP_TARGET);
        if (target == null) {
            return null;
        }
        String host = attributes.get(SemanticAttributes.NET_HOST_NAME);
        if (host == null) {
            // fall back to deprecated http.host if available
            host = attributes.get(SemanticAttributes.HTTP_HOST);
            if (host == null) {
                return null;
            }
            return scheme + "://" + host + target;
        }
        Long port = attributes.get(SemanticAttributes.NET_HOST_PORT);
        if (port != null && port > 0) {
            return scheme + "://" + host + ":" + port + target;
        }
        return scheme + "://" + host + target;
    }

    @Nullable
    private static String getSource(Attributes attributes) {
        // this is only used by the 2.x web interop bridge
        // for ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().setSource()
        String source = attributes.get(AiSemanticAttributes.SPAN_SOURCE);
        if (source != null) {
            return source;
        }
        return getMessagingTargetSource(attributes);
    }

    @Nullable
    private static String getMessagingTargetSource(Attributes attributes) {
        if (isAzureSdkMessaging(attributes.get(AiSemanticAttributes.AZURE_SDK_NAMESPACE))) {
            // special case needed until Azure SDK moves to OTel semantic conventions
            String peerAddress = attributes.get(AiSemanticAttributes.AZURE_SDK_PEER_ADDRESS);
            // TODO (limolkova) need to populate messaging.system in SB and EH.
            // this make exporter backward-compatible with current EventHubs and ServiceBus
            // instrumentation and new otel plugin
            if (peerAddress != null) {
                String destination = attributes.get(AiSemanticAttributes.AZURE_SDK_MESSAGE_BUS_DESTINATION);
                return peerAddress + "/" + destination;
            }
        }

        String messagingSystem = getMessagingSystem(attributes);
        if (messagingSystem == null) {
            return null;
        }
        // TODO (trask) AI mapping: should this pass default port for messaging.system?
        String source =
            nullAwareConcat(
                getTargetOrNull(attributes, 0),
                attributes.get(SemanticAttributes.MESSAGING_DESTINATION),
                "/");
        if (source != null) {
            return source;
        }
        // fallback
        return messagingSystem;
    }

    private static boolean isAzureSdkMessaging(String messagingSystem) {
        return "Microsoft.EventHub".equals(messagingSystem)
            || "Microsoft.ServiceBus".equals(messagingSystem);
    }

    private static String getOperationName(SpanData span) {
        String operationName = span.getAttributes().get(AiSemanticAttributes.OPERATION_NAME);
        if (operationName != null) {
            return operationName;
        }

        String spanName = span.getName();
        String httpMethod = span.getAttributes().get(SemanticAttributes.HTTP_METHOD);
        if (httpMethod != null && !httpMethod.isEmpty() && spanName.startsWith("/")) {
            return httpMethod + " " + spanName;
        }
        return spanName;
    }

    private static String nullAwareConcat(
        @Nullable String str1, @Nullable String str2, String separator) {
        if (str1 == null) {
            return str2;
        }
        if (str2 == null) {
            return str1;
        }
        return str1 + separator + str2;
    }

    private void exportEvents(
        SpanData span,
        @Nullable String operationName,
        long itemCount,
        Consumer<TelemetryItem> consumer) {
        for (EventData event : span.getEvents()) {
            String instrumentationScopeName = span.getInstrumentationScopeInfo().getName();
            if (eventSuppressor.test(event, instrumentationScopeName)) {
                continue;
            }

            if (event.getAttributes().get(SemanticAttributes.EXCEPTION_TYPE) != null
                || event.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE) != null) {
                SpanContext parentSpanContext = span.getParentSpanContext();
                // Application Insights expects exception records to be "top-level" exceptions
                // not just any exception that bubbles up
                if (!parentSpanContext.isValid() || parentSpanContext.isRemote()) {
                    // TODO (trask) map OpenTelemetry exception to Application Insights exception better
                    String stacktrace = event.getAttributes().get(SemanticAttributes.EXCEPTION_STACKTRACE);
                    if (stacktrace != null) {
                        consumer.accept(
                            createExceptionTelemetryItem(stacktrace, span, operationName, itemCount));
                    }
                }
                return;
            }

            MessageTelemetryBuilder telemetryBuilder = MessageTelemetryBuilder.create();
            telemetryInitializer.accept(telemetryBuilder, span.getResource());

            // set standard properties
            setOperationId(telemetryBuilder, span.getTraceId());
            setOperationParentId(telemetryBuilder, span.getSpanId());
            if (operationName != null) {
                setOperationName(telemetryBuilder, operationName);
            } else {
                setOperationName(telemetryBuilder, span.getAttributes());
            }
            setTime(telemetryBuilder, event.getEpochNanos());
            setItemCount(telemetryBuilder, itemCount);

            // update tags
            MAPPINGS.map(event.getAttributes(), telemetryBuilder);

            // set message-specific properties
            telemetryBuilder.setMessage(event.getName());

            consumer.accept(telemetryBuilder.build());
        }
    }

    private TelemetryItem createExceptionTelemetryItem(
        String errorStack, SpanData span, @Nullable String operationName, long itemCount) {

        ExceptionTelemetryBuilder telemetryBuilder = ExceptionTelemetryBuilder.create();
        telemetryInitializer.accept(telemetryBuilder, span.getResource());

        // set standard properties
        setOperationId(telemetryBuilder, span.getTraceId());
        setOperationParentId(telemetryBuilder, span.getSpanId());
        if (operationName != null) {
            setOperationName(telemetryBuilder, operationName);
        } else {
            setOperationName(telemetryBuilder, span.getAttributes());
        }
        setTime(telemetryBuilder, span.getEndEpochNanos());
        setItemCount(telemetryBuilder, itemCount);

        MAPPINGS.map(span.getAttributes(), telemetryBuilder);

        // set exception-specific properties
        telemetryBuilder.setExceptions(Exceptions.minimalParse(errorStack));

        return telemetryBuilder.build();
    }

    private static void setTime(AbstractTelemetryBuilder telemetryBuilder, long epochNanos) {
        telemetryBuilder.setTime(FormattedTime.offSetDateTimeFromEpochNanos(epochNanos));
    }

    private static void setItemCount(AbstractTelemetryBuilder telemetryBuilder, long itemCount) {
        if (itemCount != 1) {
            telemetryBuilder.setSampleRate(100.0f / itemCount);
        }
    }

    private static long getItemCount(SpanData span) {
        Long itemCount = span.getAttributes().get(AiSemanticAttributes.ITEM_COUNT);
        return itemCount == null ? 1 : itemCount;
    }

    private static void addLinks(AbstractTelemetryBuilder telemetryBuilder, List<LinkData> links) {
        if (links.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (LinkData link : links) {
            if (!first) {
                sb.append(",");
            }
            sb.append("{\"operation_Id\":\"");
            sb.append(link.getSpanContext().getTraceId());
            sb.append("\",\"id\":\"");
            sb.append(link.getSpanContext().getSpanId());
            sb.append("\"}");
            first = false;
        }
        sb.append("]");
        telemetryBuilder.addProperty("_MS.links", sb.toString());
    }

    static void applyCommonTags(MappingsBuilder mappingsBuilder) {
        mappingsBuilder
            .exact(
                SemanticAttributes.ENDUSER_ID.getKey(),
                (telemetryBuilder, value) -> {
                    if (value instanceof String) {
                        telemetryBuilder.addTag(ContextTagKeys.AI_USER_ID.toString(), (String) value);
                    }
                })
            .exact(
                AiSemanticAttributes.PREVIEW_APPLICATION_VERSION.getKey(),
                (telemetryBuilder, value) -> {
                    if (value instanceof String) {
                        telemetryBuilder.addTag(
                            ContextTagKeys.AI_APPLICATION_VER.toString(), (String) value);
                    }
                });

        applyConnectionStringAndRoleNameOverrides(mappingsBuilder);
    }

    private static final WarningLogger connectionStringAttributeNoLongerSupported =
        new WarningLogger(
            SpanDataMapper.class,
            AiSemanticAttributes.DEPRECATED_CONNECTION_STRING.getKey()
                + " is no longer supported because it"
                + " is incompatible with pre-aggregated standard metrics. Please use"
                + " \"connectionStringOverrides\" configuration, or reach out to"
                + " https://github.com/microsoft/ApplicationInsights-Java/issues if you have a"
                + " different use case.");
    private static final WarningLogger roleNameAttributeNoLongerSupported =
        new WarningLogger(
            SpanDataMapper.class,
            AiSemanticAttributes.DEPRECATED_ROLE_NAME.getKey()
                + " is no longer supported because it"
                + " is incompatible with pre-aggregated standard metrics. Please use"
                + " \"roleNameOverrides\" configuration, or reach out to"
                + " https://github.com/microsoft/ApplicationInsights-Java/issues if you have a"
                + " different use case.");
    private static final WarningLogger roleInstanceAttributeNoLongerSupported =
        new WarningLogger(
            SpanDataMapper.class,
            AiSemanticAttributes.DEPRECATED_ROLE_INSTANCE.getKey()
                + " is no longer supported because it"
                + " is incompatible with pre-aggregated standard metrics. Please reach out to"
                + " https://github.com/microsoft/ApplicationInsights-Java/issues if you have a use"
                + " case for this.");
    private static final WarningLogger instrumentationKeyAttributeNoLongerSupported =
        new WarningLogger(
            SpanDataMapper.class,
            AiSemanticAttributes.DEPRECATED_INSTRUMENTATION_KEY.getKey()
                + " is no longer supported because it"
                + " is incompatible with pre-aggregated standard metrics. Please use"
                + " \"connectionStringOverrides\" configuration, or reach out to"
                + " https://github.com/microsoft/ApplicationInsights-Java/issues if you have a"
                + " different use case.");

    static void applyConnectionStringAndRoleNameOverrides(MappingsBuilder mappingsBuilder) {
        mappingsBuilder
            .exact(
                AiSemanticAttributes.INTERNAL_CONNECTION_STRING.getKey(),
                (telemetryBuilder, value) -> {
                    // intentionally letting exceptions from parse bubble up
                    telemetryBuilder.setConnectionString(ConnectionString.parse((String) value));
                })
            .exact(
                AiSemanticAttributes.INTERNAL_ROLE_NAME.getKey(),
                (telemetryBuilder, value) -> {
                    if (value instanceof String) {
                        telemetryBuilder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), (String) value);
                    }
                })
            .exact(
                AiSemanticAttributes.DEPRECATED_CONNECTION_STRING.getKey(),
                (telemetryBuilder, value) -> {
                    connectionStringAttributeNoLongerSupported.recordWarning();
                })
            .exact(
                AiSemanticAttributes.DEPRECATED_ROLE_NAME.getKey(),
                (telemetryBuilder, value) -> {
                    roleNameAttributeNoLongerSupported.recordWarning();
                })
            .exact(
                AiSemanticAttributes.DEPRECATED_ROLE_INSTANCE.getKey(),
                (telemetryBuilder, value) -> {
                    roleInstanceAttributeNoLongerSupported.recordWarning();
                })
            .exact(
                AiSemanticAttributes.DEPRECATED_INSTRUMENTATION_KEY.getKey(),
                (telemetryBuilder, value) -> {
                    instrumentationKeyAttributeNoLongerSupported.recordWarning();
                });
    }
}
