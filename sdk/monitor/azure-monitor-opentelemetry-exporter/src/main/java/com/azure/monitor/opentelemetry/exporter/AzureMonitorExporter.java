// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryEventData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryExceptionDetails;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RequestData;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import reactor.util.context.Context;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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

/**
 * This class is an implementation of OpenTelemetry {@link SpanExporter} that allows different tracing services to
 * export recorded data for sampled spans in their own format.
 */
public final class AzureMonitorExporter implements SpanExporter {
    private static final Pattern COMPONENT_PATTERN = Pattern
        .compile("io\\.opentelemetry\\.auto\\.([^0-9]*)(-[0-9.]*)?");

    private static final Set<String> SQL_DB_SYSTEMS;

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
    }

    private final MonitorExporterAsyncClient client;
    private final ClientLogger logger = new ClientLogger(AzureMonitorExporter.class);
    private final String instrumentationKey;
    private final String telemetryItemNamePrefix;

    /**
     * Creates an instance of exporter that is configured with given exporter client that sends telemetry events to
     * Application Insights resource identified by the instrumentation key.
     *
     * @param client The client used to send data to Azure Monitor.
     * @param instrumentationKey The instrumentation key of Application Insights resource.
     */
    AzureMonitorExporter(MonitorExporterAsyncClient client, String instrumentationKey) {
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

        try {
            List<TelemetryItem> telemetryItems = new ArrayList<>();
            for (SpanData span : spans) {
                logger.verbose("exporting span: {}", span);
                export(span, telemetryItems);
            }
            client.export(telemetryItems)
                .subscriberContext(Context.of(Tracer.DISABLE_TRACING_KEY, true))
                .subscribe();
            return CompletableResultCode.ofSuccess();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return CompletableResultCode.ofFailure();
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
        Span.Kind kind = span.getKind();
        String instrumentationName = span.getInstrumentationLibraryInfo().getName();
        Matcher matcher = COMPONENT_PATTERN.matcher(instrumentationName);
        String stdComponent = matcher.matches() ? matcher.group(1) : null;
        if ("jms".equals(stdComponent) && !span.getParentSpanContext().isValid() && kind == Span.Kind.CLIENT) {
            // no need to capture these, at least is consistent with prior behavior
            // these tend to be frameworks pulling messages which are then pushed to consumers
            // where we capture them
            return;
        }
        if (kind == Span.Kind.INTERNAL) {
            if (!span.getParentSpanContext().isValid()) {
                // TODO (srnagar): revisit this decision
                // maybe user-generated telemetry?
                // otherwise this top-level span won't show up in Performance blade
                exportRequest(stdComponent, span, telemetryItems);
            } else if (span.getName().equals("EventHubs.message")) {
                // TODO (srnagar): eventhubs should use PRODUCER instead of INTERNAL
                exportRemoteDependency(stdComponent, span, false, telemetryItems);
            } else {
                exportRemoteDependency(stdComponent, span, true, telemetryItems);
            }
        } else if (kind == Span.Kind.CLIENT || kind == Span.Kind.PRODUCER) {
            exportRemoteDependency(stdComponent, span, false, telemetryItems);
        } else if (kind == Span.Kind.SERVER || kind == Span.Kind.CONSUMER) {
            exportRequest(stdComponent, span, telemetryItems);
        } else {
            throw logger.logExceptionAsError(new UnsupportedOperationException(kind.name()));
        }
    }


    private static List<TelemetryExceptionDetails> minimalParse(String errorStack) {
        TelemetryExceptionDetails details = new TelemetryExceptionDetails();
        String line = errorStack.split("\n")[0];
        int index = line.indexOf(": ");

        if (index != -1) {
            details.setTypeName(line.substring(0, index));
            details.setMessage(line.substring(index + 2));
        } else {
            details.setTypeName(line);
        }
        details.setStack(errorStack);
        return Arrays.asList(details);
    }

    private void exportRemoteDependency(String stdComponent, SpanData span, boolean inProc,
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

        span.getInstrumentationLibraryInfo().getName();

        Attributes attributes = span.getAttributes();

        if (inProc) {
            remoteDependencyData.setType("InProc");
        } else {
            if (attributes.get(SemanticAttributes.HTTP_METHOD) != null) {
                applyHttpRequestSpan(attributes, remoteDependencyData);
            } else if (attributes.get(SemanticAttributes.DB_SYSTEM) != null) {
                applyDatabaseQuerySpan(attributes, remoteDependencyData, stdComponent);
            } else if (span.getName().equals("EventHubs.send")) {
                // TODO (srnagar): eventhubs should use CLIENT instead of PRODUCER
                // TODO (srnagar): eventhubs should add links to messages?
                remoteDependencyData.setType("Microsoft.EventHub");
                String peerAddress = removeAttributeString(attributes, SemanticAttributes.PEER_SERVICE.getKey());
                String destination = removeAttributeString(attributes,
                    SemanticAttributes.MESSAGING_DESTINATION.getKey());
                // TODO: (savaity) should we rename this to MESSAGING_DESTINATION
                remoteDependencyData.setTarget(peerAddress + "/" + destination);
            } else if (span.getName().equals("EventHubs.message")) {
                // TODO (srnagar): eventhubs should populate peer.address and message_bus.destination
                String peerAddress = removeAttributeString(attributes, SemanticAttributes.PEER_SERVICE.getKey());
                String destination = removeAttributeString(attributes,
                    SemanticAttributes.MESSAGING_DESTINATION.getKey());
                if (peerAddress != null) {
                    remoteDependencyData.setTarget(peerAddress + "/" + destination);
                }
                remoteDependencyData.setType("Microsoft.EventHub");
            } else if ("kafka-clients".equals(stdComponent)) {
                remoteDependencyData.setType("Kafka");
                remoteDependencyData.setTarget(span.getName()); // destination queue name
            } else if ("jms".equals(stdComponent)) {
                remoteDependencyData.setType("JMS");
                remoteDependencyData.setTarget(span.getName()); // destination queue name
            }
        }

        remoteDependencyData.setId(span.getSpanId());
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), span.getTraceId());

        String parentSpanId = span.getParentSpanId();
        if (span.getParentSpanContext().isValid()) {
            telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), parentSpanId);
        }

        telemetryItem.setTime(getFormattedTime(span.getStartEpochNanos()));
        remoteDependencyData
            .setDuration(getFormattedDuration(Duration.ofNanos(span.getEndEpochNanos() - span.getStartEpochNanos())));

        remoteDependencyData.setSuccess(span.getStatus().isOk());
        String description = span.getStatus().getDescription();
        if (description != null) {
            remoteDependencyData.getProperties().put("statusDescription", description);
        }

        // TODO: sampling will not be supported in this exporter
        // Sampling cleanup will be done in a separate PR
        Double samplingPercentage = 100.0;

        // for now, only add extra attributes for custom telemetry
        if (stdComponent == null) {
            addExtraAttributes(remoteDependencyData.getProperties(), attributes);
        }
        telemetryItem.setSampleRate(samplingPercentage.floatValue());
        telemetryItems.add(telemetryItem);
        exportEvents(span, samplingPercentage, telemetryItems);
    }

    private void applyDatabaseQuerySpan(Attributes attributes, RemoteDependencyData rd,
                                        String component) {
        String type = attributes.get(SemanticAttributes.DB_SYSTEM);

        if (SQL_DB_SYSTEMS.contains(type)) {
            type = "SQL";
        }
        rd.setType(type);
        rd.setData(attributes.get(SemanticAttributes.DB_STATEMENT));

        String dbUrl = attributes.get(SemanticAttributes.DB_CONNECTION_STRING);
        if (dbUrl == null) {
            // this is needed until all database instrumentation captures the required db.url
            rd.setTarget(type);
        } else {
            String dbInstance = attributes.get(SemanticAttributes.DB_NAME);
            if (dbInstance != null) {
                dbUrl += " | " + dbInstance;
            }
            if ("jdbc".equals(component)) {
                // TODO (srnagar): this is special case to match 2.x behavior
                //      because U/X strips off the beginning in E2E tx view
                rd.setTarget("jdbc:" + dbUrl);
            } else {
                rd.setTarget(dbUrl);
            }
        }
        // TODO (srnagar): put db.instance somewhere
    }

    private void applyHttpRequestSpan(Attributes attributes,
                                      RemoteDependencyData remoteDependencyData) {

        remoteDependencyData.setType("Http (tracked component)");

        String method = attributes.get(SemanticAttributes.HTTP_METHOD);
        String url = attributes.get(SemanticAttributes.HTTP_URL);

        Long httpStatusCode = attributes.get(SemanticAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode != null) {
            remoteDependencyData.setResultCode(Long.toString(httpStatusCode));
        }

        if (url != null) {
            try {
                URI uriObject = new URI(url);
                String target = createTarget(uriObject);
                remoteDependencyData.setTarget(target);
                // TODO (srnagar): is this right, overwriting name to include the full path?
                String path = uriObject.getPath();
                if (CoreUtils.isNullOrEmpty(path)) {
                    remoteDependencyData.setName(method + " /");
                } else {
                    remoteDependencyData.setName(method + " " + path);
                }
            } catch (URISyntaxException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void exportRequest(String stdComponent, SpanData span, List<TelemetryItem> telemetryItems) {
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

        if ("kafka-clients".equals(stdComponent)) {
            requestData.setSource(span.getName()); // destination queue name
        } else if ("jms".equals(stdComponent)) {
            requestData.setSource(span.getName()); // destination queue name
        }
        addLinks(requestData.getProperties(), span.getLinks());
        Long httpStatusCode = attributes.get(SemanticAttributes.HTTP_STATUS_CODE);

        requestData.setResponseCode("200");
        if (httpStatusCode != null) {
            requestData.setResponseCode(Long.toString(httpStatusCode));
        }

        String httpUrl = removeAttributeString(attributes, SemanticAttributes.HTTP_URL.getKey());
        if (httpUrl != null) {
            requestData.setUrl(httpUrl);
        }

        String httpMethod = removeAttributeString(attributes, SemanticAttributes.HTTP_METHOD.getKey());
        String name = span.getName();
        if (httpMethod != null && name.startsWith("/")) {
            name = httpMethod + " " + name;
        }
        requestData.setName(name);
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_NAME.toString(), name);

        if (span.getName().equals("EventHubs.process")) {
            // TODO (srnagar): eventhubs should use CONSUMER instead of SERVER
            // (https://gist.github.com/lmolkova/e4215c0f44a49ef824983382762e6b92#opentelemetry-example-1)
            String peerAddress = removeAttributeString(attributes, SemanticAttributes.PEER_SERVICE.getKey());
            String destination = removeAttributeString(attributes, SemanticAttributes.MESSAGING_DESTINATION.getKey());
            requestData.setSource(peerAddress + "/" + destination);
        }
        requestData.setId(span.getSpanId());
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), span.getTraceId());

        String aiLegacyParentId = span.getTraceState().get("ai-legacy-parent-id");
        if (aiLegacyParentId != null) {
            // see behavior specified at https://github.com/microsoft/ApplicationInsights-Java/issues/1174
            telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), aiLegacyParentId);

            String aiLegacyOperationId = span.getTraceState().get("ai-legacy-operation-id");
            if (aiLegacyOperationId != null) {
                telemetryItem.getTags().putIfAbsent("ai_legacyRootID", aiLegacyOperationId);
            }
        } else {
            String parentSpanId = span.getParentSpanId();
            if (span.getParentSpanContext().isValid()) {
                telemetryItem.getTags()
                    .put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), parentSpanId);
            }
        }

        long startEpochNanos = span.getStartEpochNanos();
        telemetryItem.setTime(getFormattedTime(startEpochNanos));

        Duration duration = Duration.ofNanos(span.getEndEpochNanos() - startEpochNanos);
        requestData.setDuration(getFormattedDuration(duration));

        requestData.setSuccess(span.getStatus().isOk());
        String description = span.getStatus().getDescription();
        if (description != null) {
            requestData.getProperties().put("statusDescription", description);
        }

        Double samplingPercentage = 100.0;

        // for now, only add extra attributes for custom telemetry
        if (stdComponent == null) {
            addExtraAttributes(requestData.getProperties(), attributes);
        }

        telemetryItem.setSampleRate(samplingPercentage.floatValue());
        telemetryItems.add(telemetryItem);
        exportEvents(span, samplingPercentage, telemetryItems);
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
                .put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), span.getParentSpanId());
            telemetryItem.setTime(getFormattedTime(event.getEpochNanos()));
            addExtraAttributes(eventData.getProperties(), event.getAttributes());

            if (event.getAttributes().get(SemanticAttributes.EXCEPTION_TYPE) != null
                || event.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE) != null) {
                // TODO (srnagar): Remove this boolean after we can confirm that the exception duplicate
                //  is a bug from the opentelmetry-java-instrumentation
                if (!foundException) {
                    // TODO (srnagar): map OpenTelemetry exception to Application Insights exception better
                    Object stacktrace = event.getAttributes()
                        .get(SemanticAttributes.EXCEPTION_STACKTRACE);
                    if (stacktrace != null) {
                        trackException(stacktrace.toString(), span, operationId,
                            span.getSpanId(), samplingPercentage, telemetryItems);
                    }
                }
                foundException = true;
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
            sb.append(link.getSpanContext().getTraceIdAsHexString());
            sb.append("\",\"id\":\"");
            sb.append(link.getSpanContext().getSpanIdAsHexString());
            sb.append("\"}");
            first = false;
        }
        sb.append("]");
        properties.put("_MS.links", sb.toString());
    }

    private static String removeAttributeString(Attributes attributes, String attributeName) {
        Object attributeValue = attributes.get(AttributeKey.stringKey(attributeName));
        if (attributeValue == null) {
            return null;
        } else if (attributeValue instanceof String) {
            return attributeValue.toString();
        } else {
            // TODO (srnagar): log debug warning
            return null;
        }
    }

    private static String createTarget(URI uriObject) {
        String target = uriObject.getHost();
        if (uriObject.getPort() != 80 && uriObject.getPort() != 443 && uriObject.getPort() != -1) {
            target += ":" + uriObject.getPort();
        }
        return target;
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

    private static void addExtraAttributes(final Map<String, String> properties, Attributes attributes) {
        attributes.forEach((key, value) ->  {
            String val = getStringValue(key, value);
            if (val != null) {
                properties.put(key.toString(), val);
            }
        });
    }
}
