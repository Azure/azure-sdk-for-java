// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryEventData;
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
    private final ClientLogger logger = new ClientLogger(AzureMonitorTraceExporter.class);
    private final String instrumentationKey;
    private final String telemetryItemNamePrefix;

    /**
     * Creates an instance of exporter that is configured with given exporter client that sends telemetry events to
     * Application Insights resource identified by the instrumentation key.
     * @param client The client used to send data to Azure Monitor.
     * @param instrumentationKey The instrumentation key of Application Insights resource.
     */
    AzureMonitorTraceExporter(MonitorExporterAsyncClient client, String instrumentationKey) {
        this.client = client;
        this.instrumentationKey = instrumentationKey;
        String formattedInstrumentationKey = instrumentationKey.replaceAll("-", "");
        this.telemetryItemNamePrefix = "Microsoft.ApplicationInsights." + formattedInstrumentationKey + ".";
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
                logger.verbose("exporting span: {}", span);
                export(span, telemetryItems);
            }
            client.export(telemetryItems)
                .subscriberContext(Context.of(Tracer.DISABLE_TRACING_KEY, true))
                .subscribe(ignored -> { }, error -> completableResultCode.fail(), completableResultCode::succeed);
            return completableResultCode;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
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
            throw logger.logExceptionAsError(new UnsupportedOperationException(kind.name()));
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
        TelemetryItem telemetryItem = new TelemetryItem();
        RemoteDependencyData remoteDependencyData = new RemoteDependencyData();
        MonitorBase monitorBase = new MonitorBase();

        telemetryItem.setTags(new HashMap<>());
        telemetryItem.setName(telemetryItemNamePrefix + "RemoteDependency");
        telemetryItem.setVersion(1);
        telemetryItem.setInstrumentationKey(instrumentationKey);
        telemetryItem.setData(monitorBase);

        remoteDependencyData.setProperties(new HashMap<>());
        remoteDependencyData.setVersion(2);
        monitorBase.setBaseType("RemoteDependencyData");
        monitorBase.setBaseData(remoteDependencyData);

        addLinks(remoteDependencyData.getProperties(), span.getLinks());
        remoteDependencyData.setName(span.getName());

        if (inProc) {
            remoteDependencyData.setType("InProc");
        } else {
            applySemanticConventions(span, remoteDependencyData);
        }

        remoteDependencyData.setId(span.getSpanId());
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), span.getTraceId());
        String parentSpanId = span.getParentSpanId();
        if (SpanId.isValid(parentSpanId)) {
            telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), parentSpanId);
        }

        telemetryItem.setTime(getFormattedTime(span.getStartEpochNanos()));
        remoteDependencyData
            .setDuration(getFormattedDuration(Duration.ofNanos(span.getEndEpochNanos() - span.getStartEpochNanos())));

        remoteDependencyData.setSuccess(span.getStatus().getStatusCode() != StatusCode.ERROR);

        setExtraAttributes(telemetryItem, remoteDependencyData.getProperties(), span.getAttributes());

        // sampling will not be supported in this exporter
        Double samplingPercentage = 100.0;
        telemetryItem.setSampleRate(samplingPercentage.floatValue());
        telemetryItems.add(telemetryItem);
        exportEvents(span, samplingPercentage, telemetryItems);
    }

    private void applySemanticConventions(SpanData span, RemoteDependencyData remoteDependencyData) {
        Attributes attributes = span.getAttributes();
        String httpMethod = attributes.get(AttributeKey.stringKey("http.method"));
        if (httpMethod != null) {
            applyHttpClientSpan(attributes, remoteDependencyData);
            return;
        }
        String rpcSystem = attributes.get(AttributeKey.stringKey("rpc.system"));
        if (rpcSystem != null) {
            applyRpcClientSpan(attributes, remoteDependencyData, rpcSystem);
            return;
        }
        String dbSystem = attributes.get(AttributeKey.stringKey("db.system"));
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
        String messagingSystem = attributes.get(AttributeKey.stringKey("messaging.system"));
        if (messagingSystem != null) {
            applyMessagingClientSpan(attributes, remoteDependencyData, messagingSystem, span.getKind());
            return;
        }
    }

    private void applyHttpClientSpan(Attributes attributes, RemoteDependencyData telemetry) {

        // from the spec, at least one of the following sets of attributes is required:
        // * http.url
        // * http.scheme, http.host, http.target
        // * http.scheme, net.peer.name, net.peer.port, http.target
        // * http.scheme, net.peer.ip, net.peer.port, http.target
        String scheme = attributes.get(AttributeKey.stringKey("http.scheme"));
        int defaultPort;
        if ("http".equals(scheme)) {
            defaultPort = 80;
        } else if ("https".equals(scheme)) {
            defaultPort = 443;
        } else {
            defaultPort = 0;
        }
        String target = getTargetFromPeerAttributes(attributes, defaultPort);
        if (target == null) {
            target = attributes.get(AttributeKey.stringKey("http.host"));
        }
        String url = attributes.get(AttributeKey.stringKey("http.url"));
        if (target == null && url != null) {
            try {
                URI uri = new URI(url);
                target = uri.getHost();
                if (uri.getPort() != 80 && uri.getPort() != 443 && uri.getPort() != -1) {
                    target += ":" + uri.getPort();
                }
            } catch (URISyntaxException e) {
                // TODO (trask) "log once"
                logger.error(e.getMessage());
                logger.verbose(e.getMessage(), e);
            }
        }
        if (target == null) {
            // this should not happen, just a failsafe
            target = "Http";
        }

        telemetry.setType("Http");
        telemetry.setTarget(target);

        Long httpStatusCode = attributes.get(AttributeKey.longKey("http.status_code"));
        if (httpStatusCode != null) {
            telemetry.setResultCode(Long.toString(httpStatusCode));
        }

        telemetry.setData(url);
    }

    private static String getTargetFromPeerAttributes(Attributes attributes, int defaultPort) {
        String target = attributes.get(AttributeKey.stringKey("peer.service"));
        if (target != null) {
            // do not append port if peer.service is provided
            return target;
        }
        target = attributes.get(AttributeKey.stringKey("net.peer.name"));
        if (target == null) {
            target = attributes.get(AttributeKey.stringKey("net.peer.ip"));
        }
        if (target == null) {
            return null;
        }
        // append net.peer.port to target
        Long port = attributes.get(AttributeKey.longKey("net.peer.port"));
        if (port != null && port != defaultPort) {
            return target + ":" + port;
        }
        return target;
    }

    private static void applyRpcClientSpan(Attributes attributes, RemoteDependencyData telemetry, String rpcSystem) {
        telemetry.setType(rpcSystem);
        String target = getTargetFromPeerAttributes(attributes, 0);
        // not appending /rpc.service for now since that seems too fine-grained
        if (target == null) {
            target = rpcSystem;
        }
        telemetry.setTarget(target);
    }

    private static void applyDatabaseClientSpan(Attributes attributes, RemoteDependencyData telemetry, String dbSystem) {
        String dbStatement = attributes.get(AttributeKey.stringKey("db.statement"));
        String type;
        if (SQL_DB_SYSTEMS.contains(dbSystem)) {
            type = "SQL";
            // keeping existing behavior that was release in 3.0.0 for now
            // not going with new jdbc instrumentation span name of "<db.operation> <db.name>.<db.sql.table>" for now
            // just in case this behavior is reversed due to spec:
            // "It is not recommended to attempt any client-side parsing of `db.statement` just to get these properties,
            // they should only be used if the library being instrumented already provides them."
            // also need to discuss with other AI language exporters
            //
            // if we go to shorter span name now, and it gets reverted, no way for customers to get the shorter name back
            // whereas if we go to shorter span name in future, and they still prefer more cardinality, they can get that
            // back using telemetry processor to copy db.statement into span name
            telemetry.setName(dbStatement);
        } else {
            type = dbSystem;
        }
        telemetry.setType(type);
        telemetry.setData(dbStatement);
        String target = nullAwareConcat(getTargetFromPeerAttributes(attributes, getDefaultPortForDbSystem(dbSystem)),
            attributes.get(AttributeKey.stringKey("db.name")), "/");
        if (target == null) {
            target = dbSystem;
        }
        telemetry.setTarget(target);
    }

    private void applyMessagingClientSpan(Attributes attributes, RemoteDependencyData telemetry, String messagingSystem, SpanKind spanKind) {
        if (spanKind == SpanKind.PRODUCER) {
            telemetry.setType("Queue Message | " + messagingSystem);
        } else {
            // e.g. CONSUMER kind (without remote parent) and CLIENT kind
            telemetry.setType(messagingSystem);
        }
        String destination = attributes.get(AttributeKey.stringKey("messaging.destination"));
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
        switch (dbSystem) {
            // TODO (trask) add these default ports to the OpenTelemetry database semantic conventions spec
            // TODO (trask) need to add more default ports once jdbc instrumentation reports net.peer.*
            case "mongodb":
                return 27017;
            case "cassandra":
                return 9042;
            case "redis":
                return 6379;
            default:
                return 0;
        }
    }

    private void exportRequest(SpanData span, List<TelemetryItem> telemetryItems) {
        TelemetryItem telemetryItem = new TelemetryItem();
        RequestData requestData = new RequestData();
        MonitorBase monitorBase = new MonitorBase();

        telemetryItem.setTags(new HashMap<>());
        telemetryItem.setName(telemetryItemNamePrefix + "Request");
        telemetryItem.setVersion(1);
        telemetryItem.setInstrumentationKey(instrumentationKey);
        telemetryItem.setData(monitorBase);

        requestData.setProperties(new HashMap<>());
        requestData.setVersion(2);
        monitorBase.setBaseType("RequestData");
        monitorBase.setBaseData(requestData);

        Attributes attributes = span.getAttributes();

        requestData.setSource(getSource(attributes));

        if (isAzureQueue(attributes)) {
            // TODO(trask): for batch consumer, enqueuedTime should be the average of this attribute
            //  across all links
            Long enqueuedTime = attributes.get(AZURE_SDK_ENQUEUED_TIME);
            if (enqueuedTime != null) {
                long timeSinceEnqueued =
                    NANOSECONDS.toMillis(span.getStartEpochNanos()) - SECONDS.toMillis(enqueuedTime);
                if (timeSinceEnqueued < 0) {
                    timeSinceEnqueued = 0;
                }
                if (requestData.getMeasurements() == null) {
                    requestData.setMeasurements(new HashMap<>());
                }
                requestData.getMeasurements().put("timeSinceEnqueued", (double) timeSinceEnqueued);
            }
        }

        addLinks(requestData.getProperties(), span.getLinks());
        Long httpStatusCode = attributes.get(AttributeKey.longKey("http.status_code"));

        requestData.setResponseCode("200");
        if (httpStatusCode != null) {
            requestData.setResponseCode(Long.toString(httpStatusCode));
        }

        String httpUrl = attributes.get(AttributeKey.stringKey("http.url"));
        if (httpUrl != null) {
            requestData.setUrl(httpUrl);
        }

        String name = span.getName();
        requestData.setName(name);
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_NAME.toString(), name);
        requestData.setId(span.getSpanId());
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), span.getTraceId());

        String aiLegacyParentId = span.getSpanContext().getTraceState().get("ai-legacy-parent-id");
        if (aiLegacyParentId != null) {
            // see behavior specified at https://github.com/microsoft/ApplicationInsights-Java/issues/1174
            telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), aiLegacyParentId);
            String aiLegacyOperationId = span.getSpanContext().getTraceState().get("ai-legacy-operation-id");
            if (aiLegacyOperationId != null) {
                telemetryItem.getTags().putIfAbsent("ai_legacyRootID", aiLegacyOperationId);
            }
        } else {
            String parentSpanId = span.getParentSpanId();
            if (SpanId.isValid(parentSpanId)) {
                telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), parentSpanId);
            }
        }

        long startEpochNanos = span.getStartEpochNanos();
        telemetryItem.setTime(getFormattedTime(startEpochNanos));

        Duration duration = Duration.ofNanos(span.getEndEpochNanos() - startEpochNanos);
        requestData.setDuration(getFormattedDuration(duration));

        requestData.setSuccess(span.getStatus().getStatusCode() != StatusCode.ERROR);

        String description = span.getStatus().getDescription();
        if (description != null) {
            requestData.getProperties().put("statusDescription", description);
        }

        Double samplingPercentage = 100.0;

        setExtraAttributes(telemetryItem, requestData.getProperties(), attributes);

        telemetryItem.setSampleRate(samplingPercentage.floatValue());
        telemetryItems.add(telemetryItem);
        exportEvents(span, samplingPercentage, telemetryItems);
    }

    private static String getSource(Attributes attributes) {
        if (isAzureQueue(attributes)) {
            return getAzureSdkTargetSource(attributes);
        }
        String messagingSystem = attributes.get(AttributeKey.stringKey("messaging.system"));
        if (messagingSystem != null) {
            // TODO (trask) AI mapping: should this pass default port for messaging.system?
            String source =
                nullAwareConcat(
                    getTargetFromPeerAttributes(attributes, 0),
                    attributes.get(AttributeKey.stringKey("messaging.destination")),
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
        return azureNamespace.equals("Microsoft.EventHub") || azureNamespace.equals("Microsoft.ServiceBus");
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

    private void exportEvents(SpanData span, Double samplingPercentage, List<TelemetryItem> telemetryItems) {
        boolean foundException = false;
        for (EventData event : span.getEvents()) {

            TelemetryItem telemetryItem = new TelemetryItem();
            TelemetryEventData eventData = new TelemetryEventData();
            MonitorBase monitorBase = new MonitorBase();

            telemetryItem.setTags(new HashMap<>());
            telemetryItem.setName(telemetryItemNamePrefix + "Event");
            telemetryItem.setVersion(1);
            telemetryItem.setInstrumentationKey(instrumentationKey);
            telemetryItem.setData(monitorBase);

            eventData.setProperties(new HashMap<>());
            eventData.setVersion(2);
            monitorBase.setBaseType("EventData");
            monitorBase.setBaseData(eventData);
            eventData.setName(event.getName());

            String operationId = span.getTraceId();
            telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), operationId);
            telemetryItem.getTags()
                .put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), span.getSpanId());
            telemetryItem.setTime(getFormattedTime(event.getEpochNanos()));
            setExtraAttributes(telemetryItem, eventData.getProperties(), event.getAttributes());

            if (event.getAttributes().get(AttributeKey.stringKey("exception.type")) != null
                || event.getAttributes().get(AttributeKey.stringKey("exception.message")) != null) {
                String stacktrace = event.getAttributes().get(AttributeKey.stringKey("exception.stacktrace"));
                if (stacktrace != null) {
                    trackException(stacktrace, span, operationId, span.getSpanId(), samplingPercentage, telemetryItems);
                }
            } else {
                telemetryItem.setSampleRate(samplingPercentage.floatValue());
                telemetryItems.add(telemetryItem);
            }
        }
    }

    private void trackException(String errorStack, SpanData span, String operationId,
                                String id, Double samplingPercentage, List<TelemetryItem> telemetryItems) {
        TelemetryItem telemetryItem = new TelemetryItem();
        TelemetryExceptionData exceptionData = new TelemetryExceptionData();
        MonitorBase monitorBase = new MonitorBase();

        telemetryItem.setTags(new HashMap<>());
        telemetryItem.setName(telemetryItemNamePrefix + "Exception");
        telemetryItem.setVersion(1);
        telemetryItem.setInstrumentationKey(instrumentationKey);
        telemetryItem.setData(monitorBase);

        exceptionData.setProperties(new HashMap<>());
        exceptionData.setVersion(2);
        monitorBase.setBaseType("ExceptionData");
        monitorBase.setBaseData(exceptionData);

        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), operationId);
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), id);
        telemetryItem.setTime(getFormattedTime(span.getEndEpochNanos()));
        telemetryItem.setSampleRate(samplingPercentage.floatValue());
        exceptionData.setExceptions(minimalParse(errorStack));
        telemetryItems.add(telemetryItem);
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

    private String getStringValue(AttributeKey<?> attributeKey, Object value) {
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
                logger.warning("unexpected attribute type: {}", attributeKey.getType());
                return null;
        }
    }


    private static <T> String join(List<T> values) {
        StringBuilder sb = new StringBuilder();
        if (CoreUtils.isNullOrEmpty(values)) {
            return sb.toString();
        }

        for (int i = 0; i < values.size() - 1; i++) {
            sb.append(values.get(i));
            sb.append(", ");
        }
        sb.append(values.get(values.size() - 1));
        return sb.toString();
    }

    private void setExtraAttributes(TelemetryItem telemetry, Map<String, String> properties,
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
}
