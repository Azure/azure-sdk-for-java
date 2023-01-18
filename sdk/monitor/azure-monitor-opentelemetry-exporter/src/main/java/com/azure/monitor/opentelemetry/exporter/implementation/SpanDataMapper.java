// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.ExceptionTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.Exceptions;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MessageTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RemoteDependencyTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RequestTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedDuration;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedTime;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Trie;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.UrlParser;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class SpanDataMapper {

    // visible for testing
    public static final String MS_PROCESSED_BY_METRIC_EXTRACTORS = "_MS.ProcessedByMetricExtractors";

    private static final ClientLogger LOGGER = new ClientLogger(SpanDataMapper.class);

    private static final Set<String> SQL_DB_SYSTEMS;

    private static final Trie<Boolean> STANDARD_ATTRIBUTE_PREFIX_TRIE;

    // TODO (trask) add to generated ContextTagKeys class
    private static final ContextTagKeys AI_DEVICE_OS = ContextTagKeys.fromString("ai.device.os");

    static {
        Set<String> dbSystems = new HashSet<>();
        dbSystems.add(SemanticAttributes.DbSystemValues.DB2);
        dbSystems.add(SemanticAttributes.DbSystemValues.DERBY);
        dbSystems.add(SemanticAttributes.DbSystemValues.MARIADB);
        dbSystems.add(SemanticAttributes.DbSystemValues.MSSQL);
        dbSystems.add(SemanticAttributes.DbSystemValues.MYSQL);
        dbSystems.add(SemanticAttributes.DbSystemValues.ORACLE);
        dbSystems.add(SemanticAttributes.DbSystemValues.POSTGRESQL);
        dbSystems.add(SemanticAttributes.DbSystemValues.SQLITE);
        dbSystems.add(SemanticAttributes.DbSystemValues.OTHER_SQL);
        dbSystems.add(SemanticAttributes.DbSystemValues.HSQLDB);
        dbSystems.add(SemanticAttributes.DbSystemValues.H2);

        SQL_DB_SYSTEMS = Collections.unmodifiableSet(dbSystems);

        // TODO need to keep this list in sync as new semantic conventions are defined
        STANDARD_ATTRIBUTE_PREFIX_TRIE =
            Trie.<Boolean>newBuilder()
                .put("http.", true)
                .put("db.", true)
                .put("message.", true)
                .put("messaging.", true)
                .put("rpc.", true)
                .put("enduser.", true)
                .put("net.", true)
                .put("peer.", true)
                .put("exception.", true)
                .put("thread.", true)
                .put("faas.", true)
                .put("code.", true)
                .build();
    }

    private final boolean captureHttpServer4xxAsError;
    private final BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer;
    private final BiPredicate<EventData, String> eventSuppressor;
    private final Supplier<String> appIdSupplier;

    public SpanDataMapper(
        boolean captureHttpServer4xxAsError,
        BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer,
        BiPredicate<EventData, String> eventSuppressor,
        Supplier<String> appIdSupplier) {
        this.captureHttpServer4xxAsError = captureHttpServer4xxAsError;
        this.telemetryInitializer = telemetryInitializer;
        this.eventSuppressor = eventSuppressor;
        this.appIdSupplier = appIdSupplier;
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
        if (isRequest(span)) {
            return exportRequest(span, itemCount);
        } else {
            return exportRemoteDependency(span, span.getKind() == SpanKind.INTERNAL, itemCount);
        }
    }

    public static boolean isRequest(SpanData span) {
        return isRequest(
            span.getKind(),
            span.getParentSpanContext(),
            span.getInstrumentationScopeInfo(),
            span.getAttributes()::get);
    }

    public static boolean isRequest(ReadableSpan span) {
        return isRequest(
            span.getKind(),
            span.getParentSpanContext(),
            span.getInstrumentationScopeInfo(),
            span::getAttribute);
    }

    public static boolean isRequest(
        SpanKind kind,
        SpanContext parentSpanContext,
        @Nullable InstrumentationScopeInfo scopeInfo,
        Function<AttributeKey<String>, String> attrFn) {
        String instrumentationName = scopeInfo == null ? null : scopeInfo.getName();
        if (kind == SpanKind.INTERNAL) {
            // TODO (trask) AI mapping: need semantic convention for determining whether to map INTERNAL
            // to request or dependency (or need clarification to use SERVER for this)
            return !parentSpanContext.isValid()
                && instrumentationName != null
                && (instrumentationName.startsWith("io.opentelemetry.spring-scheduling-")
                || instrumentationName.startsWith("io.opentelemetry.quartz-")
                || instrumentationName.equals("io.opentelemetry.methods"));
        } else if (kind == SpanKind.CLIENT || kind == SpanKind.PRODUCER) {
            return false;
        } else if (kind == SpanKind.CONSUMER
            && "receive".equals(attrFn.apply(SemanticAttributes.MESSAGING_OPERATION))) {
            return false;
        } else if (kind == SpanKind.SERVER || kind == SpanKind.CONSUMER) {
            return true;
        } else {
            throw new UnsupportedOperationException(kind.name());
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
        setExtraAttributes(telemetryBuilder, span.getAttributes());

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
            Arrays.asList(
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

        String path = UrlParser.getPathFromUrl(url);
        if (path == null) {
            return name;
        }
        return path.isEmpty() ? method + " /" : method + " " + path;
    }

    private void applySemanticConventions(
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

    private void applyHttpClientSpan(
        RemoteDependencyTelemetryBuilder telemetryBuilder, Attributes attributes) {

        int defaultPort = getDefaultPortForHttpUrl(attributes.get(SemanticAttributes.HTTP_URL));
        String target = getTargetOrDefault(attributes, defaultPort, "Http");

        String targetAppId = getTargetAppId(attributes);

        if (targetAppId == null || targetAppId.equals(appIdSupplier.get())) {
            telemetryBuilder.setType("Http");
            telemetryBuilder.setTarget(target);
        } else {
            // using "Http (tracked component)" is important for dependencies that go cross-component
            // (have an appId in their target field)
            // if you use just HTTP, Breeze will remove appid from the target
            // TODO (trask) remove this once confirmed by zakima that it is no longer needed
            telemetryBuilder.setType("Http (tracked component)");
            telemetryBuilder.setTarget(target + " | " + targetAppId);
        }

        Long httpStatusCode = attributes.get(SemanticAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode != null) {
            telemetryBuilder.setResultCode(Long.toString(httpStatusCode));
        }

        String url = attributes.get(SemanticAttributes.HTTP_URL);
        telemetryBuilder.setData(url);
    }

    @Nullable
    private static String getTargetAppId(Attributes attributes) {
        List<String> requestContextList = attributes.get(AiSemanticAttributes.REQUEST_CONTEXT);
        if (requestContextList == null || requestContextList.isEmpty()) {
            return null;
        }
        String requestContext = requestContextList.get(0);
        int index = requestContext.indexOf('=');
        if (index == -1) {
            return null;
        }
        return requestContext.substring(index + 1);
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
        String netPeerName = attributes.get(SemanticAttributes.NET_PEER_NAME);
        if (netPeerName == null) {
            return null;
        }
        Long netPeerPort = attributes.get(SemanticAttributes.NET_PEER_PORT);
        if (netPeerPort != null && netPeerPort != defaultPort) {
            return netPeerName + ":" + netPeerPort;
        } else {
            return netPeerName;
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
        setExtraAttributes(telemetryBuilder, attributes);

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

        telemetryBuilder.setSource(getSource(attributes, span.getSpanContext()));

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
        String host = attributes.get(SemanticAttributes.HTTP_HOST);
        if (host == null) {
            return null;
        }
        String target = attributes.get(SemanticAttributes.HTTP_TARGET);
        if (target == null) {
            return null;
        }
        return scheme + "://" + host + target;
    }

    @Nullable
    private String getSource(Attributes attributes, @Nullable SpanContext spanContext) {
        // this is only used by the 2.x web interop bridge
        // for ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().setSource()
        String source = attributes.get(AiSemanticAttributes.SPAN_SOURCE);
        if (source != null) {
            return source;
        }
        if (spanContext != null) {
            source = spanContext.getTraceState().get("az");
        }
        if (source != null && !source.equals(appIdSupplier.get())) {
            return source;
        }
        return getMessagingTargetSource(attributes);
    }

    @Nullable
    private static String getMessagingTargetSource(Attributes attributes) {
        if (isAzureSdkMessaging(attributes.get(AiSemanticAttributes.AZURE_SDK_NAMESPACE))) {
            // special case needed until Azure SDK moves to OTel semantic conventions
            String peerAddress = attributes.get(AiSemanticAttributes.AZURE_SDK_PEER_ADDRESS);
            // TODO (limolkova) need to populate messaging.system in SB ans EH.
            // this make exporter backward-compatible with current EventHubs and ServiceBus instrumentation and new otel plugin
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
            setExtraAttributes(telemetryBuilder, event.getAttributes());

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
        setExtraAttributes(telemetryBuilder, span.getAttributes());

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

    private static void setExtraAttributes(
        AbstractTelemetryBuilder telemetryBuilder, Attributes attributes) {
        attributes.forEach(
            (attributeKey, value) -> {
                String key = attributeKey.getKey();
                if (key.startsWith("applicationinsights.internal.")) {
                    return;
                }
                if (key.equals(AiSemanticAttributes.AZURE_SDK_NAMESPACE.getKey())
                    || key.equals(AiSemanticAttributes.AZURE_SDK_MESSAGE_BUS_DESTINATION.getKey())
                    || key.equals(AiSemanticAttributes.AZURE_SDK_ENQUEUED_TIME.getKey())) {
                    // these are from azure SDK (AZURE_SDK_PEER_ADDRESS gets filtered out automatically
                    // since it uses the otel "peer." prefix)
                    return;
                }
                if (key.equals(AiSemanticAttributes.KAFKA_RECORD_QUEUE_TIME_MS.getKey())
                    || key.equals(AiSemanticAttributes.KAFKA_OFFSET.getKey())) {
                    return;
                }
                if (key.equals(AiSemanticAttributes.REQUEST_CONTEXT.getKey())) {
                    return;
                }
                if (key.equals(SemanticAttributes.HTTP_USER_AGENT.getKey()) && value instanceof String) {
                    telemetryBuilder.addTag("ai.user.userAgent", (String) value);
                    return;
                }
                if (applyCommonTags(telemetryBuilder, key, value)) {
                    return;
                }
                if (STANDARD_ATTRIBUTE_PREFIX_TRIE.getOrDefault(key, false)
                    && !key.startsWith("http.request.header.")
                    && !key.startsWith("http.response.header.")) {
                    return;
                }
                String val = convertToString(value, attributeKey.getType());
                if (value != null) {
                    telemetryBuilder.addProperty(attributeKey.getKey(), val);
                }
            });
    }

    static boolean applyCommonTags(
        AbstractTelemetryBuilder telemetryBuilder, String key, Object value) {

        if (key.equals(SemanticAttributes.ENDUSER_ID.getKey()) && value instanceof String) {
            telemetryBuilder.addTag(ContextTagKeys.AI_USER_ID.toString(), (String) value);
            return true;
        }
        if (applyConnectionStringAndRoleNameOverrides(telemetryBuilder, value, key)) {
            return true;
        }
        if (key.equals(AiSemanticAttributes.ROLE_INSTANCE_ID.getKey()) && value instanceof String) {
            telemetryBuilder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), (String) value);
            return true;
        }
        if (key.equals(AiSemanticAttributes.APPLICATION_VERSION.getKey()) && value instanceof String) {
            telemetryBuilder.addTag(ContextTagKeys.AI_APPLICATION_VER.toString(), (String) value);
            return true;
        }
        return false;
    }

    static boolean applyConnectionStringAndRoleNameOverrides(
        AbstractTelemetryBuilder telemetryBuilder, Object value, String key) {
        if (key.equals(AiSemanticAttributes.CONNECTION_STRING.getKey()) && value instanceof String) {
            // intentionally letting exceptions from parse bubble up
            telemetryBuilder.setConnectionString(ConnectionString.parse((String) value));
            return true;
        }
        if (key.equals(AiSemanticAttributes.INSTRUMENTATION_KEY.getKey()) && value instanceof String) {
            // intentionally letting exceptions from parse bubble up
            telemetryBuilder.setConnectionString(ConnectionString.parse("InstrumentationKey=" + value));
            return true;
        }
        if (key.equals(AiSemanticAttributes.ROLE_NAME.getKey()) && value instanceof String) {
            telemetryBuilder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), (String) value);
            return true;
        }
        return false;
    }

    @Nullable
    public static String convertToString(Object value, AttributeType type) {
        switch (type) {
            case STRING:
            case BOOLEAN:
            case LONG:
            case DOUBLE:
                return String.valueOf(value);
            case STRING_ARRAY:
            case BOOLEAN_ARRAY:
            case LONG_ARRAY:
            case DOUBLE_ARRAY:
                return join((List<?>) value);
        }
        LOGGER.warning("unexpected attribute type: {}", type);
        return null;
    }

    private static <T> String join(List<T> values) {
        StringBuilder sb = new StringBuilder();
        for (Object val : values) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(val);
        }
        return sb.toString();
    }
}
