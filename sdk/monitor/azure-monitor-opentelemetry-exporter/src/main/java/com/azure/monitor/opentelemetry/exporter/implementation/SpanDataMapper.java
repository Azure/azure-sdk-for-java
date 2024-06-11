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
import io.opentelemetry.api.common.AttributeKey;
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

import static com.azure.monitor.opentelemetry.exporter.implementation.MappingsBuilder.MappingType.SPAN;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

// TODO (trask) can probably align dropping compatibility with old HTTP semconv with the release of
//  Application Insights Java 4.0
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

    // this is needed until Azure SDK moves to latest OTel semantic conventions
    private static final String COSMOS = "Cosmos";

    private static final Mappings MAPPINGS;

    // TODO (trask) add to generated ContextTagKeys class
    private static final ContextTagKeys AI_DEVICE_OS = ContextTagKeys.fromString("ai.device.os");

    static {
        MappingsBuilder mappingsBuilder =
            new MappingsBuilder(SPAN)
                // these are from azure SDK (AZURE_SDK_PEER_ADDRESS gets filtered out automatically
                // since it uses the otel "peer." prefix)
                .ignoreExact(AiSemanticAttributes.AZURE_SDK_NAMESPACE.getKey())
                .ignoreExact(AiSemanticAttributes.AZURE_SDK_MESSAGE_BUS_DESTINATION.getKey())
                .ignoreExact(AiSemanticAttributes.AZURE_SDK_ENQUEUED_TIME.getKey())
                .ignoreExact(AiSemanticAttributes.KAFKA_RECORD_QUEUE_TIME_MS.getKey())
                .ignoreExact(AiSemanticAttributes.KAFKA_OFFSET.getKey())
                .exact(
                    SemanticAttributes.USER_AGENT_ORIGINAL.getKey(),
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
    private final BiPredicate<SpanData, EventData> shouldSuppress;

    public SpanDataMapper(
        boolean captureHttpServer4xxAsError,
        BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer,
        BiPredicate<EventData, String> eventSuppressor,
        BiPredicate<SpanData, EventData> shouldSuppress) {
        this.captureHttpServer4xxAsError = captureHttpServer4xxAsError;
        this.telemetryInitializer = telemetryInitializer;
        this.eventSuppressor = eventSuppressor;
        this.shouldSuppress = shouldSuppress;
    }

    public TelemetryItem map(SpanData span) {
        Double sampleRate = getSampleRate(span);
        return map(span, sampleRate);
    }

    public void map(SpanData span, Consumer<TelemetryItem> consumer) {
        Double sampleRate = getSampleRate(span);
        TelemetryItem telemetryItem = map(span, sampleRate);
        consumer.accept(telemetryItem);
        exportEvents(
            span,
            telemetryItem.getTags().get(ContextTagKeys.AI_OPERATION_NAME.toString()),
            sampleRate,
            consumer);
    }

    // TODO looks like this method can be private
    public TelemetryItem map(SpanData span, @Nullable Double sampleRate) {
        if (RequestChecker.isRequest(span)) {
            return exportRequest(span, sampleRate);
        } else {
            return exportRemoteDependency(span, span.getKind() == SpanKind.INTERNAL, sampleRate);
        }
    }

    private static boolean checkIsPreAggregatedStandardMetric(SpanData span) {
        Boolean isPreAggregatedStandardMetric =
            span.getAttributes().get(AiSemanticAttributes.IS_PRE_AGGREGATED);
        return isPreAggregatedStandardMetric != null && isPreAggregatedStandardMetric;
    }

    private TelemetryItem exportRemoteDependency(SpanData span, boolean inProc, @Nullable Double sampleRate) {
        RemoteDependencyTelemetryBuilder telemetryBuilder = RemoteDependencyTelemetryBuilder.create();
        telemetryInitializer.accept(telemetryBuilder, span.getResource());

        // set standard properties
        setOperationTags(telemetryBuilder, span);
        setTime(telemetryBuilder, span.getStartEpochNanos());
        setSampleRate(telemetryBuilder, sampleRate);

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
            // TODO (trask) need to handle Cosmos INTERNAL spans
            // see https://github.com/microsoft/ApplicationInsights-Java/pull/2906/files#r1104981386
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
            asList("OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "CONNECT", "PATCH"));

    // the backend product prefers more detailed (but possibly infinite cardinality) name for http
    // dependencies
    private static String getDependencyName(SpanData span) {
        String name = span.getName();

        String method = getStableOrOldAttribute(span.getAttributes(), SemanticAttributes.HTTP_REQUEST_METHOD, SemanticAttributes.HTTP_METHOD);
        if (method == null) {
            return name;
        }

        if (!DEFAULT_HTTP_SPAN_NAMES.contains(name)) {
            return name;
        }

        String url = getStableOrOldAttribute(span.getAttributes(), SemanticAttributes.URL_FULL, SemanticAttributes.HTTP_URL);
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
        String httpMethod = getStableOrOldAttribute(attributes, SemanticAttributes.HTTP_REQUEST_METHOD, SemanticAttributes.HTTP_METHOD);
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
        if (dbSystem == null) {
            // special case needed until Azure SDK moves to latest OTel semantic conventions
            dbSystem = attributes.get(AiSemanticAttributes.AZURE_SDK_DB_TYPE);
        }
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
        String target = getTargetOrDefault(attributes, Integer.MAX_VALUE, null);
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

        String httpUrl = getStableOrOldAttribute(attributes, SemanticAttributes.URL_FULL, SemanticAttributes.HTTP_URL);
        int defaultPort = getDefaultPortForHttpUrl(httpUrl);
        String target = getTargetOrDefault(attributes, defaultPort, "Http");

        telemetryBuilder.setType("Http");
        telemetryBuilder.setTarget(target);

        Long httpStatusCode = getStableOrOldAttribute(attributes, SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, SemanticAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode != null) {
            telemetryBuilder.setResultCode(Long.toString(httpStatusCode));
        } else {
            // https://1dsdocs.azurewebsites.net/schema/Mappings/AzureMonitor-AI.html#remotedependencyresultcode
            telemetryBuilder.setResultCode("0");
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
        String target = getTargetOrNullStableSemconv(attributes, defaultPort);
        if (target != null) {
            return target;
        }
        target = getTargetOrNullOldSemconv(attributes, defaultPort);
        if (target != null) {
            return target;
        }
        return defaultTarget;
    }

    @Nullable
    private static String getTargetOrNullStableSemconv(Attributes attributes, int defaultPort) {
        String peerService = attributes.get(SemanticAttributes.PEER_SERVICE); // this isn't part of stable semconv, but still has priority for now
        if (peerService != null) {
            return peerService;
        }
        String host = attributes.get(SemanticAttributes.SERVER_ADDRESS);
        if (host != null) {
            Long port = attributes.get(SemanticAttributes.SERVER_PORT);
            return getTarget(host, port, defaultPort);
        }
        return null;
    }

    @Nullable
    private static String getTargetOrNullOldSemconv(Attributes attributes, int defaultPort) {
        String peerService = attributes.get(SemanticAttributes.PEER_SERVICE);
        if (peerService != null) {
            return peerService;
        }
        String host = attributes.get(SemanticAttributes.NET_PEER_NAME);
        if (host != null) {
            Long port = attributes.get(SemanticAttributes.NET_PEER_PORT);
            return getTarget(host, port, defaultPort);
        }
        host = attributes.get(SemanticAttributes.NET_SOCK_PEER_NAME);
        if (host == null) {
            host = attributes.get(SemanticAttributes.NET_SOCK_PEER_ADDR);
        }
        if (host != null) {
            Long port = attributes.get(SemanticAttributes.NET_SOCK_PEER_PORT);
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
        } else if (dbSystem.equals(COSMOS)) {
            // this has special icon in portal (documentdb was the old name for cosmos)
            type = "Microsoft.DocumentDb";
        } else {
            type = dbSystem;
        }
        telemetryBuilder.setType(type);
        telemetryBuilder.setData(dbStatement);

        String target;
        String dbName;
        if (dbSystem.equals(COSMOS)) {
            // special case needed until Azure SDK moves to latest OTel semantic conventions
            String dbUrl = attributes.get(AiSemanticAttributes.AZURE_SDK_DB_URL);
            if (dbUrl != null) {
                target = UrlParser.getTarget(dbUrl);
            } else {
                target = null;
            }
            dbName = attributes.get(AiSemanticAttributes.AZURE_SDK_DB_INSTANCE);
        } else {
            target = getTargetOrDefault(attributes, getDefaultPortForDbSystem(dbSystem), dbSystem);
            dbName = attributes.get(SemanticAttributes.DB_NAME);
        }
        target = nullAwareConcat(target, dbName, " | ");
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

    private TelemetryItem exportRequest(SpanData span, @Nullable Double sampleRate) {
        RequestTelemetryBuilder telemetryBuilder = RequestTelemetryBuilder.create();
        telemetryInitializer.accept(telemetryBuilder, span.getResource());

        Attributes attributes = span.getAttributes();
        long startEpochNanos = span.getStartEpochNanos();

        // set standard properties
        telemetryBuilder.setId(span.getSpanId());
        setTime(telemetryBuilder, startEpochNanos);
        setSampleRate(telemetryBuilder, sampleRate);

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

        Long httpStatusCode = getStableOrOldAttribute(attributes, SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, SemanticAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode == null) {
            httpStatusCode = attributes.get(SemanticAttributes.RPC_GRPC_STATUS_CODE);
        }
        if (httpStatusCode != null) {
            telemetryBuilder.setResponseCode(Long.toString(httpStatusCode));
        } else {
            telemetryBuilder.setResponseCode("0");
        }

        String locationIp = getStableOrOldAttribute(attributes, SemanticAttributes.CLIENT_ADDRESS, SemanticAttributes.HTTP_CLIENT_IP);
        if (locationIp == null) {
            // only use net.peer.ip if http.client_ip is not available
            locationIp = attributes.get(SemanticAttributes.NET_SOCK_PEER_ADDR);
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
                    Long statusCode = getStableOrOldAttribute(span.getAttributes(), SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, SemanticAttributes.HTTP_STATUS_CODE);
                    return statusCode == null || statusCode < 400;
                }
                return true;
        }
        return true;
    }

    @Nullable
    public static String getHttpUrlFromServerSpan(Attributes attributes) {
        String httpUrl = getHttpUrlFromServerSpanStableSemconv(attributes);
        if (httpUrl != null) {
            return httpUrl;
        }
        return getHttpUrlFromServerSpanOldSemconv(attributes);
    }

    @Nullable
    private static String getHttpUrlFromServerSpanStableSemconv(Attributes attributes) {
        String scheme = attributes.get(SemanticAttributes.URL_SCHEME);
        if (scheme == null) {
            return null;
        }
        String host = attributes.get(SemanticAttributes.SERVER_ADDRESS);
        if (host == null) {
            return null;
        }
        Long port = attributes.get(SemanticAttributes.SERVER_PORT);
        String path = attributes.get(SemanticAttributes.URL_PATH);
        if (path == null) {
            return null;
        }
        String query = attributes.get(SemanticAttributes.URL_QUERY);

        int len = scheme.length() + host.length() + path.length();
        if (port != null) {
            len += 6; // max 5 digits for port plus the port separator ":"
        }
        if (query != null) {
            len += query.length() + 1; // including the query separator "?"
        }

        StringBuilder sb = new StringBuilder(len);
        sb.append(scheme);
        sb.append("://");
        sb.append(host);
        if (port != null && port > 0 && !isDefaultPortForScheme(port, scheme)) {
            sb.append(':');
            sb.append(port);
        }
        sb.append(path);
        if (query != null) {
            sb.append('?');
            sb.append(query);
        }
        return sb.toString();
    }

    private static boolean isDefaultPortForScheme(Long port, String scheme) {
        return (port == 80 && scheme.equals("http")) || (port == 443 && scheme.equals("https"));
    }

    @Nullable
    private static String getHttpUrlFromServerSpanOldSemconv(Attributes attributes) {
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
                getTargetOrNullOldSemconv(attributes, Integer.MAX_VALUE),
                attributes.get(SemanticAttributes.MESSAGING_DESTINATION_NAME),
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
        return span.getName();
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
        @Nullable Double sampleRate,
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
                    if (stacktrace != null && !shouldSuppress.test(span, event)) {
                        String exceptionLogged = span.getAttributes().get(AiSemanticAttributes.LOGGED_EXCEPTION);
                        if (!stacktrace.equals(exceptionLogged)) {
                            consumer.accept(createExceptionTelemetryItem(event.getAttributes().get(SemanticAttributes.EXCEPTION_STACKTRACE), span, operationName, sampleRate));
                        }
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
            setSampleRate(telemetryBuilder, sampleRate);

            // update tags
            MAPPINGS.map(event.getAttributes(), telemetryBuilder);

            // set message-specific properties
            telemetryBuilder.setMessage(event.getName());

            consumer.accept(telemetryBuilder.build());
        }
    }

    private TelemetryItem createExceptionTelemetryItem(
        String errorStack, SpanData span, @Nullable String operationName, @Nullable Double sampleRate) {

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
        setSampleRate(telemetryBuilder, sampleRate);

        MAPPINGS.map(span.getAttributes(), telemetryBuilder);

        // set exception-specific properties
        telemetryBuilder.setExceptions(Exceptions.minimalParse(errorStack));

        return telemetryBuilder.build();
    }


    public static <T> T getStableOrOldAttribute(Attributes attributes, AttributeKey<T> stable, AttributeKey<T> old) {
        T value = attributes.get(stable);
        if (value != null) {
            return value;
        }
        return attributes.get(old);
    }

    private static void setTime(AbstractTelemetryBuilder telemetryBuilder, long epochNanos) {
        telemetryBuilder.setTime(FormattedTime.offSetDateTimeFromEpochNanos(epochNanos));
    }

    private static void setSampleRate(AbstractTelemetryBuilder telemetryBuilder, @Nullable Double sampleRate) {
        if (sampleRate != null) {
            telemetryBuilder.setSampleRate(sampleRate.floatValue());
        }
    }

    @Nullable
    private static Double getSampleRate(SpanData span) {
        return span.getAttributes().get(AiSemanticAttributes.SAMPLE_RATE);
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

    @SuppressWarnings("deprecation") // used to emit warning to users
    private static final WarningLogger connectionStringAttributeNoLongerSupported =
        new WarningLogger(
            SpanDataMapper.class,
            AiSemanticAttributes.DEPRECATED_CONNECTION_STRING.getKey()
                + " is no longer supported because it"
                + " is incompatible with pre-aggregated standard metrics. Please use"
                + " \"connectionStringOverrides\" configuration, or reach out to"
                + " https://github.com/microsoft/ApplicationInsights-Java/issues if you have a"
                + " different use case.");

    @SuppressWarnings("deprecation") // used to emit warning to users
    private static final WarningLogger roleNameAttributeNoLongerSupported =
        new WarningLogger(
            SpanDataMapper.class,
            AiSemanticAttributes.DEPRECATED_ROLE_NAME.getKey()
                + " is no longer supported because it"
                + " is incompatible with pre-aggregated standard metrics. Please use"
                + " \"roleNameOverrides\" configuration, or reach out to"
                + " https://github.com/microsoft/ApplicationInsights-Java/issues if you have a"
                + " different use case.");

    @SuppressWarnings("deprecation") // used to emit warning to users
    private static final WarningLogger roleInstanceAttributeNoLongerSupported =
        new WarningLogger(
            SpanDataMapper.class,
            AiSemanticAttributes.DEPRECATED_ROLE_INSTANCE.getKey()
                + " is no longer supported because it"
                + " is incompatible with pre-aggregated standard metrics. Please reach out to"
                + " https://github.com/microsoft/ApplicationInsights-Java/issues if you have a use"
                + " case for this.");

    @SuppressWarnings("deprecation") // used to emit warning to users
    private static final WarningLogger instrumentationKeyAttributeNoLongerSupported =
        new WarningLogger(
            SpanDataMapper.class,
            AiSemanticAttributes.DEPRECATED_INSTRUMENTATION_KEY.getKey()
                + " is no longer supported because it"
                + " is incompatible with pre-aggregated standard metrics. Please use"
                + " \"connectionStringOverrides\" configuration, or reach out to"
                + " https://github.com/microsoft/ApplicationInsights-Java/issues if you have a"
                + " different use case.");

    @SuppressWarnings("deprecation") // used to emit warning to users
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
