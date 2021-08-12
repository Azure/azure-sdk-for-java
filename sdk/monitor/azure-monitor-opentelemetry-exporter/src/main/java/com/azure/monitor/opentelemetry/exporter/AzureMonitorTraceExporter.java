// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryExceptionDetails;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import reactor.util.context.Context;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This class is an implementation of OpenTelemetry {@link SpanExporter} that allows different tracing services to
 * export recorded data for sampled spans in their own format.
 */
public final class AzureMonitorTraceExporter implements SpanExporter {
    private static final Pattern COMPONENT_PATTERN = Pattern
        .compile("io\\.opentelemetry\\.javaagent\\.([^0-9]*)(-[0-9.]*)?");

    private static final Set<String> SQL_DB_SYSTEMS;

    private static final Set<String> STANDARD_ATTRIBUTE_PREFIXES;

    private static final AttributeKey<String> AZURE_NAMESPACE =
        AttributeKey.stringKey("az.namespace");
    private static final AttributeKey<String> AZURE_SDK_PEER_ADDRESS =
        AttributeKey.stringKey("peer.address");
    private static final AttributeKey<String> AZURE_SDK_MESSAGE_BUS_DESTINATION =
        AttributeKey.stringKey("message_bus.destination");
    private static final AttributeKey<Long> AZURE_SDK_ENQUEUED_TIME =
        AttributeKey.longKey("x-opt-enqueued-time");

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorTraceExporter.class);

    static {
        Set<String> dbSystems = new HashSet<>();
        dbSystems.add("db2");
        dbSystems.add("derby");
        dbSystems.add("mariadb");
        dbSystems.add("mssql");
        dbSystems.add("mysql");
        dbSystems.add("oracle");
        dbSystems.add("postgresql");
        dbSystems.add("sqlite");
        dbSystems.add("other_sql");
        dbSystems.add("hsqldb");
        dbSystems.add("h2");

        SQL_DB_SYSTEMS = Collections.unmodifiableSet(dbSystems);

        Set<String> standardAttributesPrefix = new HashSet<>();
        standardAttributesPrefix.add("http");
        standardAttributesPrefix.add("db");
        standardAttributesPrefix.add("message");
        standardAttributesPrefix.add("messaging");
        standardAttributesPrefix.add("rpc");
        standardAttributesPrefix.add("enduser");
        standardAttributesPrefix.add("net");
        standardAttributesPrefix.add("peer");
        standardAttributesPrefix.add("exception");
        standardAttributesPrefix.add("thread");
        standardAttributesPrefix.add("faas");

        STANDARD_ATTRIBUTE_PREFIXES = Collections.unmodifiableSet(standardAttributesPrefix);
    }

    private final MonitorExporterAsyncClient client;
    private final String instrumentationKey;

    /**
     * Creates an instance of exporter that is configured with given exporter client that sends telemetry events to
     * Application Insights resource identified by the instrumentation key.
     * @param client The client used to send data to Azure Monitor.
     * @param instrumentationKey The instrumentation key of Application Insights resource.
     */
    AzureMonitorTraceExporter(MonitorExporterAsyncClient client, String instrumentationKey) {
        this.client = client;
        this.instrumentationKey = instrumentationKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        CompletableResultCode completableResultCode = new CompletableResultCode();
        try {
            List<TelemetryItem> telemetryItems = new ArrayList<>();
            for (SpanData span : spans) {
                LOGGER.verbose("exporting span: {}", span);
                export(span, telemetryItems);
            }
            client.export(telemetryItems)
                .subscriberContext(Context.of(Tracer.DISABLE_TRACING_KEY, true))
                .subscribe(ignored -> { }, error -> completableResultCode.fail(), completableResultCode::succeed);
            return completableResultCode;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            return completableResultCode.fail();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    private void export(SpanData span, List<TelemetryItem> telemetryItems) {
        SpanKind kind = span.getKind();
        String instrumentationName = span.getInstrumentationLibraryInfo().getName();
        Matcher matcher = COMPONENT_PATTERN.matcher(instrumentationName);
        String stdComponent = matcher.matches() ? matcher.group(1) : null;
        if (kind == SpanKind.INTERNAL) {
            if ("spring-scheduling".equals(stdComponent) && !span.getParentSpanContext().isValid()) {
                // if (!span.getParentSpanContext().isValid()) {
                // TODO (trask) need semantic convention for determining whether to map INTERNAL to request or
                //  dependency (or need clarification to use SERVER for this)
                exportRequest(span, telemetryItems);
            } else {
                exportRemoteDependency(span, true, telemetryItems);
            }
        } else if (kind == SpanKind.CLIENT || kind == SpanKind.PRODUCER) {
            exportRemoteDependency(span, false, telemetryItems);
        } else if (kind == SpanKind.CONSUMER && !span.getParentSpanContext().isRemote()) {
            exportRemoteDependency(span, false, telemetryItems);
        } else if (kind == SpanKind.SERVER || kind == SpanKind.CONSUMER) {
            exportRequest(span, telemetryItems);
        } else {
            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(kind.name()));
        }
    }

    private static List<TelemetryExceptionDetails> minimalParse(String errorStack) {
        TelemetryExceptionDetails details = new TelemetryExceptionDetails();
        String line = errorStack.split(System.lineSeparator())[0];
        int index = line.indexOf(": ");

        if (index != -1) {
            details.setTypeName(line.substring(0, index));
            details.setMessage(line.substring(index + 2));
        } else {
            details.setTypeName(line);
        }
        // TODO (trask): map OpenTelemetry exception to Application Insights exception better
        details.setStack(errorStack);
        return Collections.singletonList(details);
    }

    private void exportRemoteDependency(SpanData span, boolean inProc,
                                        List<TelemetryItem> telemetryItems) {
        TelemetryItem telemetry = new TelemetryItem();
        RemoteDependencyData data = new RemoteDependencyData();
        initTelemetry(telemetry, data, "RemoteDependency", "RemoteDependencyData");
        // TODO (trask): can properties be moved up to MonitorDomain and then this can be lazy init in setExtraAttributes
        data.setProperties(new HashMap<>());
        // sampling is not supported in this exporter yet
        float samplingPercentage = 100;

        // set standard properties
        setOperationTags(telemetry, span);
        setTime(telemetry, span.getStartEpochNanos());
        setExtraAttributes(telemetry, data.getProperties(), span.getAttributes());
        addLinks(data.getProperties(), span.getLinks());

        // set dependency-specific properties
        data.setId(span.getSpanId());
        data.setName(span.getName());
        data.setDuration(getFormattedDuration(Duration.ofNanos(span.getEndEpochNanos() - span.getStartEpochNanos())));
        data.setSuccess(span.getStatus().getStatusCode() != StatusCode.ERROR);

        if (inProc) {
            data.setType("InProc");
        } else {
            applySemanticConventions(span, data);
        }

        telemetryItems.add(telemetry);
        exportEvents(span, telemetryItems);
    }

    private static void applySemanticConventions(SpanData span, RemoteDependencyData remoteDependencyData) {
        Attributes attributes = span.getAttributes();
        String httpMethod = attributes.get(SemanticAttributes.HTTP_METHOD);
        if (httpMethod != null) {
            applyHttpClientSpan(attributes, remoteDependencyData);
            return;
        }
        String rpcSystem = attributes.get(SemanticAttributes.RPC_SYSTEM);
        if (rpcSystem != null) {
            applyRpcClientSpan(attributes, remoteDependencyData, rpcSystem);
            return;
        }
        String dbSystem = attributes.get(SemanticAttributes.DB_SYSTEM);
        if (dbSystem != null) {
            applyDatabaseClientSpan(attributes, remoteDependencyData, dbSystem);
            return;
        }
        String azureNamespace = attributes.get(AZURE_NAMESPACE);
        if (azureNamespace != null && azureNamespace.equals("Microsoft.EventHub")) {
            applyEventHubsSpan(attributes, remoteDependencyData);
            return;
        }
        if (azureNamespace != null && azureNamespace.equals("Microsoft.ServiceBus")) {
            applyServiceBusSpan(attributes, remoteDependencyData);
            return;
        }
        String messagingSystem = attributes.get(SemanticAttributes.MESSAGING_SYSTEM);
        if (messagingSystem != null) {
            applyMessagingClientSpan(attributes, remoteDependencyData, messagingSystem, span.getKind());
            return;
        }

        // passing max value because we don't know what the default port would be in this case,
        // so we always want the port included
        String target = getTargetFromPeerAttributes(attributes, Integer.MAX_VALUE);
        if (target != null) {
            remoteDependencyData.setTarget(target);
            return;
        }

        // with no target, the App Map falls back to creating a node based on the telemetry name,
        // which is very confusing, e.g. when multiple unrelated nodes all point to a single node
        // because they had dependencies with the same telemetry name
        //
        // so we mark these as InProc, even though they aren't INTERNAL spans,
        // in order to prevent App Map from considering them
        remoteDependencyData.setType("InProc");
    }

    private static void setOperationTags(TelemetryItem telemetry, SpanData span) {
        setOperationTags(telemetry, span.getTraceId(), span.getParentSpanContext().getSpanId());
    }

    private static void setOperationTags(TelemetryItem telemetry, String traceId, String parentSpanId) {
        telemetry.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), traceId);
        if (SpanId.isValid(parentSpanId)) {
            telemetry.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), parentSpanId);
        }
    }

    private static void applyHttpClientSpan(Attributes attributes, RemoteDependencyData telemetry) {

        String target = getTargetForHttpClientSpan(attributes);

        telemetry.setType("Http");
        telemetry.setTarget(target);

        Long httpStatusCode = attributes.get(SemanticAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode != null) {
            telemetry.setResultCode(Long.toString(httpStatusCode));
        }

        String url = attributes.get(SemanticAttributes.HTTP_URL);
        telemetry.setData(url);
    }

    private static String getTargetForHttpClientSpan(Attributes attributes) {
        // from the spec, at least one of the following sets of attributes is required:
        // * http.url
        // * http.scheme, http.host, http.target
        // * http.scheme, net.peer.name, net.peer.port, http.target
        // * http.scheme, net.peer.ip, net.peer.port, http.target
        String target = getTargetFromPeerService(attributes);
        if (target != null) {
            return target;
        }
        // note http.host includes the port (at least when non-default)
        target = attributes.get(SemanticAttributes.HTTP_HOST);
        if (target != null) {
            String scheme = attributes.get(SemanticAttributes.HTTP_SCHEME);
            if ("http".equals(scheme)) {
                if (target.endsWith(":80")) {
                    target = target.substring(0, target.length() - 3);
                }
            } else if ("https".equals(scheme)) {
                if (target.endsWith(":443")) {
                    target = target.substring(0, target.length() - 4);
                }
            }
            return target;
        }
        String url = attributes.get(SemanticAttributes.HTTP_URL);
        if (url != null) {
            URI uri;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                LOGGER.verbose(e.getMessage(), e);
                uri = null;
            }
            if (uri != null) {
                target = uri.getHost();
                if (uri.getPort() != 80 && uri.getPort() != 443 && uri.getPort() != -1) {
                    target += ":" + uri.getPort();
                }
                return target;
            }
        }
        String scheme = attributes.get(SemanticAttributes.HTTP_SCHEME);
        int defaultPort;
        if ("http".equals(scheme)) {
            defaultPort = 80;
        } else if ("https".equals(scheme)) {
            defaultPort = 443;
        } else {
            defaultPort = 0;
        }
        target = getTargetFromNetAttributes(attributes, defaultPort);
        if (target != null) {
            return target;
        }
        // this should not happen, just a failsafe
        return "Http";
    }

    private static String getTargetFromPeerAttributes(Attributes attributes, int defaultPort) {
        String target = getTargetFromPeerService(attributes);
        if (target != null) {
            return target;
        }
        return getTargetFromNetAttributes(attributes, defaultPort);
    }

    private static String getTargetFromPeerService(Attributes attributes) {
        // do not append port to peer.service
        return attributes.get(SemanticAttributes.PEER_SERVICE);
    }

    private static String getTargetFromNetAttributes(Attributes attributes, int defaultPort) {
        String target = getHostFromNetAttributes(attributes);
        if (target == null) {
            return null;
        }
        // append net.peer.port to target
        Long port = attributes.get(SemanticAttributes.NET_PEER_PORT);
        if (port != null && port != defaultPort) {
            return target + ":" + port;
        }
        return target;
    }

    private static String getHostFromNetAttributes(Attributes attributes) {
        String host = attributes.get(SemanticAttributes.NET_PEER_NAME);
        if (host != null) {
            return host;
        }
        return attributes.get(SemanticAttributes.NET_PEER_IP);
    }

    private static void applyRpcClientSpan(Attributes attributes, RemoteDependencyData telemetry,
                                           String rpcSystem) {
        telemetry.setType(rpcSystem);
        String target = getTargetFromPeerAttributes(attributes, 0);
        // not appending /rpc.service for now since that seems too fine-grained
        if (target == null) {
            target = rpcSystem;
        }
        telemetry.setTarget(target);
    }

    private static void applyDatabaseClientSpan(Attributes attributes, RemoteDependencyData telemetry,
                                                String dbSystem) {
        String dbStatement = attributes.get(SemanticAttributes.DB_STATEMENT);
        String type;
        if (SQL_DB_SYSTEMS.contains(dbSystem)) {
            type = "SQL";
            // keeping existing behavior that was release in 3.0.0 for now
            // not going with new jdbc instrumentation span name of
            // "<db.operation> <db.name>.<db.sql.table>" for now just in case this behavior is reversed
            // due to spec:
            // "It is not recommended to attempt any client-side parsing of `db.statement` just to get
            // these properties, they should only be used if the library being instrumented already
            // provides them."
            // also need to discuss with other AI language exporters
            //
            // if we go to shorter span name now, and it gets reverted, no way for customers to get the
            // shorter name back
            // whereas if we go to shorter span name in the future, and they still prefer more
            // cardinality, they can get that back using telemetry processor to copy db.statement into
            // span name
            telemetry.setName(dbStatement);
        } else {
            type = dbSystem;
        }
        telemetry.setType(type);
        telemetry.setData(dbStatement);
        String target =
            nullAwareConcat(
                getTargetFromPeerAttributes(attributes, getDefaultPortForDbSystem(dbSystem)),
                attributes.get(SemanticAttributes.DB_NAME),
                "/");
        if (target == null) {
            target = dbSystem;
        }
        telemetry.setTarget(target);
    }

    private static void applyMessagingClientSpan(Attributes attributes, RemoteDependencyData telemetry,
                                                 String messagingSystem, SpanKind spanKind) {
        if (spanKind == SpanKind.PRODUCER) {
            telemetry.setType("Queue Message | " + messagingSystem);
        } else {
            // e.g. CONSUMER kind (without remote parent) and CLIENT kind
            telemetry.setType(messagingSystem);
        }
        String destination = attributes.get(SemanticAttributes.MESSAGING_DESTINATION);
        if (destination != null) {
            telemetry.setTarget(destination);
        } else {
            telemetry.setTarget(messagingSystem);
        }
    }

    // special case needed until Azure SDK moves to OTel semantic conventions
    private static void applyEventHubsSpan(Attributes attributes, RemoteDependencyData telemetry) {
        telemetry.setType("Microsoft.EventHub");
        telemetry.setTarget(getAzureSdkTargetSource(attributes));
    }

    // special case needed until Azure SDK moves to OTel semantic conventions
    private static void applyServiceBusSpan(Attributes attributes, RemoteDependencyData telemetry) {
        telemetry.setType("AZURE SERVICE BUS");
        telemetry.setTarget(getAzureSdkTargetSource(attributes));
    }

    private static String getAzureSdkTargetSource(Attributes attributes) {
        String peerAddress = attributes.get(AZURE_SDK_PEER_ADDRESS);
        String destination = attributes.get(AZURE_SDK_MESSAGE_BUS_DESTINATION);
        return peerAddress + "/" + destination;
    }

    private static int getDefaultPortForDbSystem(String dbSystem) {
        // jdbc default ports are from
        // io.opentelemetry.javaagent.instrumentation.jdbc.JdbcConnectionUrlParser
        // TODO (trask) make the ports constants (at least in JdbcConnectionUrlParser) so they can be used here
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
                return 0;
        }
    }

    private void exportRequest(SpanData span, List<TelemetryItem> telemetryItems) {
        TelemetryItem telemetry = new TelemetryItem();
        RequestData data = new RequestData();
        initTelemetry(telemetry, data, "Request", "RequestData");
        // TODO (trask): can properties be moved up to MonitorDomain and then this can be lazy init in setExtraAttributes
        data.setProperties(new HashMap<>());

        Attributes attributes = span.getAttributes();
        long startEpochNanos = span.getStartEpochNanos();
        // sampling is not supported in this exporter yet
        float samplingPercentage = 100;

        // set standard properties
        data.setId(span.getSpanId());
        setTime(telemetry, startEpochNanos);
        setExtraAttributes(telemetry, data.getProperties(), attributes);
        addLinks(data.getProperties(), span.getLinks());

        String operationName = getOperationName(span);
        telemetry.getTags().put(ContextTagKeys.AI_OPERATION_NAME.toString(), operationName);
        telemetry.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), span.getTraceId());

        telemetry
            .getTags()
            .put(
                ContextTagKeys.AI_OPERATION_PARENT_ID.toString(),
                span.getParentSpanContext().getSpanId());

        // set request-specific properties
        data.setName(operationName);
        data.setDuration(getFormattedDuration(Duration.ofNanos(span.getEndEpochNanos() - startEpochNanos)));
        data.setSuccess(span.getStatus().getStatusCode() != StatusCode.ERROR);

        String httpUrl = attributes.get(SemanticAttributes.HTTP_URL);
        if (httpUrl != null) {
            data.setUrl(httpUrl);
        }

        Long httpStatusCode = attributes.get(SemanticAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode == null) {
            httpStatusCode = attributes.get(SemanticAttributes.RPC_GRPC_STATUS_CODE);
        }
        if (httpStatusCode != null) {
            data.setResponseCode(Long.toString(httpStatusCode));
        } else {
            data.setResponseCode("0");
        }

        String locationIp = attributes.get(SemanticAttributes.HTTP_CLIENT_IP);
        if (locationIp == null) {
            // only use net.peer.ip if http.client_ip is not available
            locationIp = attributes.get(SemanticAttributes.NET_PEER_IP);
        }
        if (locationIp != null) {
            telemetry.getTags().put(ContextTagKeys.AI_LOCATION_IP.toString(), locationIp);
        }

        data.setSource(getSource(attributes));

        if (isAzureQueue(attributes)) {
            // TODO (trask): for batch consumer, enqueuedTime should be the average of this attribute
            //  across all links
            Long enqueuedTime = attributes.get(AZURE_SDK_ENQUEUED_TIME);
            if (enqueuedTime != null) {
                long timeSinceEnqueued =
                    NANOSECONDS.toMillis(span.getStartEpochNanos()) - SECONDS.toMillis(enqueuedTime);
                if (timeSinceEnqueued < 0) {
                    timeSinceEnqueued = 0;
                }
                if (data.getMeasurements() == null) {
                    data.setMeasurements(new HashMap<>());
                }
                data.getMeasurements().put("timeSinceEnqueued", (double) timeSinceEnqueued);
            }
        }

        telemetryItems.add(telemetry);
        exportEvents(span, telemetryItems);
    }

    private static String getSource(Attributes attributes) {
        if (isAzureQueue(attributes)) {
            return getAzureSdkTargetSource(attributes);
        }
        String messagingSystem = attributes.get(SemanticAttributes.MESSAGING_SYSTEM);
        if (messagingSystem != null) {
            // TODO (trask) AI mapping: should this pass default port for messaging.system?
            String source =
                nullAwareConcat(
                    getTargetFromPeerAttributes(attributes, 0),
                    attributes.get(SemanticAttributes.MESSAGING_DESTINATION),
                    "/");
            if (source != null) {
                return source;
            }
            // fallback
            return messagingSystem;
        }
        return null;
    }

    private static boolean isAzureQueue(Attributes attributes) {
        String azureNamespace = attributes.get(AZURE_NAMESPACE);
        if (azureNamespace == null) {
            return false;
        }
        return azureNamespace.equals("Microsoft.EventHub")
            || azureNamespace.equals("Microsoft.ServiceBus");
    }

    private static String getOperationName(SpanData span) {
        String spanName = span.getName();
        String httpMethod = span.getAttributes().get(SemanticAttributes.HTTP_METHOD);
        if (httpMethod != null && spanName.startsWith("/")) {
            return httpMethod + " " + spanName;
        }
        return spanName;
    }

    private static String nullAwareConcat(String str1, String str2, String separator) {
        if (str1 == null) {
            return str2;
        }
        if (str2 == null) {
            return str1;
        }
        return str1 + separator + str2;
    }

    private void exportEvents(SpanData span, List<TelemetryItem> telemetryItems) {
        for (EventData event : span.getEvents()) {

            if (event.getAttributes().get(SemanticAttributes.EXCEPTION_TYPE) != null
                || event.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE) != null) {
                // TODO (trask): map OpenTelemetry exception to Application Insights exception better
                String stacktrace = event.getAttributes().get(SemanticAttributes.EXCEPTION_STACKTRACE);
                if (stacktrace != null) {
                    trackException(stacktrace, span, telemetryItems);
                }
                return;
            }

            TelemetryItem telemetry = new TelemetryItem();
            MessageData data = new MessageData();
            initTelemetry(telemetry, data, "Message", "MessageData");
            // TODO (trask): can properties be moved up to MonitorDomain and then this can be lazy init in setExtraAttributes
            data.setProperties(new HashMap<>());

            // set standard properties
            setOperationTags(telemetry, span.getTraceId(), span.getSpanId());
            setTime(telemetry, event.getEpochNanos());
            setExtraAttributes(telemetry, data.getProperties(), event.getAttributes());

            // set message-specific properties
            data.setMessage(event.getName());

            telemetryItems.add(telemetry);
        }
    }

    private void trackException(String errorStack, SpanData span, List<TelemetryItem> telemetryItems) {
        TelemetryItem telemetry = new TelemetryItem();
        TelemetryExceptionData data = new TelemetryExceptionData();
        initTelemetry(telemetry, data, "Exception", "ExceptionData");
        // TODO (trask): can properties be moved up to MonitorDomain and then this can be lazy init in setExtraAttributes
        data.setProperties(new HashMap<>());

        // set standard properties
        setOperationTags(telemetry, span.getTraceId(), span.getSpanId());
        setTime(telemetry, span.getEndEpochNanos());

        // set exception-specific properties
        data.setExceptions(minimalParse(errorStack));

        telemetryItems.add(telemetry);
    }

    private void initTelemetry(TelemetryItem telemetry, MonitorDomain data, String telemetryName,
                               String baseType) {
        telemetry.setVersion(1);
        telemetry.setName(telemetryName);
        telemetry.setInstrumentationKey(instrumentationKey);
        telemetry.setTags(new HashMap<>());

        data.setVersion(2);

        MonitorBase monitorBase = new MonitorBase();
        telemetry.setData(monitorBase);
        monitorBase.setBaseType(baseType);
        monitorBase.setBaseData(data);
    }

    private static void setTime(TelemetryItem telemetry, long epochNanos) {
        telemetry.setTime(getFormattedTime(epochNanos));
    }

    private static String getFormattedDuration(Duration duration) {
        return duration.toDays() + "." + duration.toHours() + ":" + duration.toMinutes() + ":" + duration.getSeconds()
            + "." + duration.toMillis();
    }

    private static String getFormattedTime(long epochNanos) {
        return Instant.ofEpochMilli(NANOSECONDS.toMillis(epochNanos))
            .atOffset(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private static void addLinks(Map<String, String> properties, List<LinkData> links) {
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
        properties.put("_MS.links", sb.toString());
    }

    private static void setExtraAttributes(TelemetryItem telemetry, Map<String, String> properties,
                                           Attributes attributes) {
        attributes.forEach((key, value) -> {
            String stringKey = key.getKey();
            if (stringKey.startsWith("applicationinsights.internal.")) {
                return;
            }
            // TODO (trask) use az.namespace for something?
            if (stringKey.equals(AZURE_SDK_MESSAGE_BUS_DESTINATION.getKey())
                || stringKey.equals("az.namespace")) {
                return;
            }
            // special case mappings
            if (key.getKey().equals("enduser.id") && value instanceof String) {
                telemetry.getTags().put(ContextTagKeys.AI_USER_ID.toString(), (String) value);
                return;
            }
            if (key.getKey().equals("http.user_agent") && value instanceof String) {
                telemetry.getTags().put("ai.user.userAgent", (String) value);
                return;
            }
            int index = stringKey.indexOf(".");
            String prefix = index == -1 ? stringKey : stringKey.substring(0, index);
            if (STANDARD_ATTRIBUTE_PREFIXES.contains(prefix)) {
                return;
            }
            String val = getStringValue(key, value);
            if (value != null) {
                properties.put(key.getKey(), val);
            }
        });
    }

    private static String getStringValue(AttributeKey<?> attributeKey, Object value) {
        switch (attributeKey.getType()) {
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
            default:
                LOGGER.warning("unexpected attribute type: {}", attributeKey.getType());
                return null;
        }
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
