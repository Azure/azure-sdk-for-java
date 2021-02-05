// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.models.ContextTagKeys;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.models.MonitorBase;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.models.RemoteDependencyData;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.models.RequestData;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.models.TelemetryEventData;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.models.TelemetryExceptionData;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.models.TelemetryExceptionDetails;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.models.TelemetryItem;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.common.ReadableKeyValuePairs;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.attributes.SemanticAttributes;

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

    private final MonitorExporterClient client;
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
    AzureMonitorExporter(MonitorExporterClient client, String instrumentationKey) {
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
            client.export(telemetryItems);
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
        if ("jms".equals(stdComponent) && !span.getParentSpanId().isValid() && kind == Span.Kind.CLIENT) {
            // no need to capture these, at least is consistent with prior behavior
            // these tend to be frameworks pulling messages which are then pushed to consumers
            // where we capture them
            return;
        }
        if (kind == Span.Kind.INTERNAL) {
            if (!span.getParentSpanId().isValid()) {
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

        Map<String, AttributeValue> attributes = getAttributesCopy(span.getAttributes());

        if (inProc) {
            remoteDependencyData.setType("InProc");
        } else {
            if (attributes.containsKey("http.method")) {
                applyHttpRequestSpan(attributes, remoteDependencyData);
            } else if (attributes.containsKey(SemanticAttributes.DB_SYSTEM.key())) {
                applyDatabaseQuerySpan(attributes, remoteDependencyData, stdComponent);
            } else if (span.getName().equals("EventHubs.send")) {
                // TODO (srnagar): eventhubs should use CLIENT instead of PRODUCER
                // TODO (srnagar): eventhubs should add links to messages?
                remoteDependencyData.setType("Microsoft.EventHub");
                String peerAddress = removeAttributeString(attributes, "peer.address");
                String destination = removeAttributeString(attributes, "message_bus.destination");
                remoteDependencyData.setTarget(peerAddress + "/" + destination);
            } else if (span.getName().equals("EventHubs.message")) {
                // TODO (srnagar): eventhubs should populate peer.address and message_bus.destination
                String peerAddress = removeAttributeString(attributes, "peer.address");
                String destination = removeAttributeString(attributes, "message_bus.destination");
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

        remoteDependencyData.setId(span.getSpanId().toLowerBase16());
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), span.getTraceId().toLowerBase16());

        SpanId parentSpanId = span.getParentSpanId();
        if (parentSpanId.isValid()) {
            telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), parentSpanId.toLowerBase16());
        }

        telemetryItem.setTime(getFormattedTime(span.getStartEpochNanos()));
        remoteDependencyData
            .setDuration(getFormattedDuration(Duration.ofNanos(span.getEndEpochNanos() - span.getStartEpochNanos())));

        remoteDependencyData.setSuccess(span.getStatus().isOk());
        String description = span.getStatus().getDescription();
        if (description != null) {
            remoteDependencyData.getProperties().put("statusDescription", description);
        }

        Double samplingPercentage = removeAiSamplingPercentage(attributes);

        // for now, only add extra attributes for custom telemetry
        if (stdComponent == null) {
            addExtraAttributes(remoteDependencyData.getProperties(), attributes);
        }
        telemetryItem.setSampleRate(samplingPercentage.floatValue());
        telemetryItems.add(telemetryItem);
        exportEvents(span, samplingPercentage, telemetryItems);
    }

    private void applyDatabaseQuerySpan(Map<String, AttributeValue> attributes, RemoteDependencyData rd,
                                        String component) {
        String type = removeAttributeString(attributes, SemanticAttributes.DB_SYSTEM.key());

        if (SQL_DB_SYSTEMS.contains(type)) {
            type = "SQL";
        }
        rd.setType(type);
        rd.setData(removeAttributeString(attributes, SemanticAttributes.DB_STATEMENT.key()));

        String dbUrl = removeAttributeString(attributes, SemanticAttributes.DB_CONNECTION_STRING.key());
        if (dbUrl == null) {
            // this is needed until all database instrumentation captures the required db.url
            rd.setTarget(type);
        } else {
            String dbInstance = removeAttributeString(attributes, SemanticAttributes.DB_NAME.key());
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

    private void applyHttpRequestSpan(Map<String, AttributeValue> attributes,
                                      RemoteDependencyData remoteDependencyData) {

        remoteDependencyData.setType("Http (tracked component)");

        String method = removeAttributeString(attributes, SemanticAttributes.HTTP_METHOD.key());
        String url = removeAttributeString(attributes, SemanticAttributes.HTTP_URL.key());

        AttributeValue httpStatusCode = attributes.remove(SemanticAttributes.HTTP_STATUS_CODE.key());
        if (httpStatusCode != null && httpStatusCode.getType() == AttributeValue.Type.LONG) {
            long statusCode = httpStatusCode.getLongValue();
            remoteDependencyData.setResultCode(Long.toString(statusCode));
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

        Map<String, AttributeValue> attributes = getAttributesCopy(span.getAttributes());

        if ("kafka-clients".equals(stdComponent)) {
            requestData.setSource(span.getName()); // destination queue name
        } else if ("jms".equals(stdComponent)) {
            requestData.setSource(span.getName()); // destination queue name
        }
        addLinks(requestData.getProperties(), span.getLinks());
        AttributeValue httpStatusCode = attributes.remove(SemanticAttributes.HTTP_STATUS_CODE.key());

        if (isNonNullLong(httpStatusCode)) {
            requestData.setResponseCode(Long.toString(httpStatusCode.getLongValue()));
        }

        String httpUrl = removeAttributeString(attributes, SemanticAttributes.HTTP_URL.key());
        if (httpUrl != null) {
            requestData.setUrl(httpUrl);
        }

        String httpMethod = removeAttributeString(attributes, SemanticAttributes.HTTP_METHOD.key());
        String name = span.getName();
        if (httpMethod != null && name.startsWith("/")) {
            name = httpMethod + " " + name;
        }
        requestData.setName(name);
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_NAME.toString(), name);

        if (span.getName().equals("EventHubs.process")) {
            // TODO (srnagar): eventhubs should use CONSUMER instead of SERVER
            // (https://gist.github.com/lmolkova/e4215c0f44a49ef824983382762e6b92#opentelemetry-example-1)
            String peerAddress = removeAttributeString(attributes, "peer.address");
            String destination = removeAttributeString(attributes, "message_bus.destination");
            requestData.setSource(peerAddress + "/" + destination);
        }
        requestData.setId(span.getSpanId().toLowerBase16());
        telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), span.getTraceId().toLowerBase16());

        String aiLegacyParentId = span.getTraceState().get("ai-legacy-parent-id");
        if (aiLegacyParentId != null) {
            // see behavior specified at https://github.com/microsoft/ApplicationInsights-Java/issues/1174
            telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), aiLegacyParentId);

            String aiLegacyOperationId = span.getTraceState().get("ai-legacy-operation-id");
            if (aiLegacyOperationId != null) {
                telemetryItem.getTags().putIfAbsent("ai_legacyRootID", aiLegacyOperationId);
            }
        } else {
            SpanId parentSpanId = span.getParentSpanId();
            if (parentSpanId.isValid()) {
                telemetryItem.getTags()
                    .put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), parentSpanId.toLowerBase16());
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

        Double samplingPercentage = removeAiSamplingPercentage(attributes);

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
        for (SpanData.Event event : span.getEvents()) {

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

            String operationId = span.getTraceId().toLowerBase16();
            telemetryItem.getTags().put(ContextTagKeys.AI_OPERATION_ID.toString(), operationId);
            telemetryItem.getTags()
                .put(ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), span.getParentSpanId().toLowerBase16());
            telemetryItem.setTime(getFormattedTime(event.getEpochNanos()));
            addExtraAttributes(eventData.getProperties(), event.getAttributes());

            if (event.getAttributes().get(SemanticAttributes.EXCEPTION_TYPE.key()) != null
                || event.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE.key()) != null) {
                // TODO (srnagar): Remove this boolean after we can confirm that the exception duplicate
                //  is a bug from the opentelmetry-java-instrumentation
                if (!foundException) {
                    // TODO (srnagar): map OpenTelemetry exception to Application Insights exception better
                    AttributeValue stacktrace = event.getAttributes()
                        .get(SemanticAttributes.EXCEPTION_STACKTRACE.key());
                    if (stacktrace != null) {
                        trackException(stacktrace.getStringValue(), span, operationId,
                            span.getSpanId().toLowerBase16(), samplingPercentage, telemetryItems);
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

    private boolean isNonNullLong(AttributeValue attributeValue) {
        return attributeValue != null && attributeValue.getType() == AttributeValue.Type.LONG;
    }

    private Map<String, AttributeValue> getAttributesCopy(ReadableAttributes attributes) {
        final Map<String, AttributeValue> copy = new HashMap<>();
        attributes.forEach(copy::put);
        return copy;
    }

    private static void addLinks(Map<String, String> properties, List<SpanData.Link> links) {
        if (links.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (SpanData.Link link : links) {
            if (!first) {
                sb.append(",");
            }
            sb.append("{\"operation_Id\":\"");
            sb.append(link.getContext().getTraceId().toLowerBase16());
            sb.append("\",\"id\":\"");
            sb.append(link.getContext().getSpanId().toLowerBase16());
            sb.append("\"}");
            first = false;
        }
        sb.append("]");
        properties.put("_MS.links", sb.toString());
    }

    private static String removeAttributeString(Map<String, AttributeValue> attributes, String attributeName) {
        AttributeValue attributeValue = attributes.remove(attributeName);
        if (attributeValue == null) {
            return null;
        } else if (attributeValue.getType() == AttributeValue.Type.STRING) {
            return attributeValue.getStringValue();
        } else {
            // TODO (srnagar): log debug warning
            return null;
        }
    }

    private static Double removeAttributeDouble(Map<String, AttributeValue> attributes, String attributeName) {
        AttributeValue attributeValue = attributes.remove(attributeName);
        if (attributeValue == null) {
            return null;
        } else if (attributeValue.getType() == AttributeValue.Type.DOUBLE) {
            return attributeValue.getDoubleValue();
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

    private static String getStringValue(AttributeValue value) {
        switch (value.getType()) {
            case STRING:
                return value.getStringValue();
            case BOOLEAN:
                return Boolean.toString(value.getBooleanValue());
            case LONG:
                return Long.toString(value.getLongValue());
            case DOUBLE:
                return Double.toString(value.getDoubleValue());
            case STRING_ARRAY:
                return join(value.getStringArrayValue());
            case BOOLEAN_ARRAY:
                return join(value.getBooleanArrayValue());
            case LONG_ARRAY:
                return join(value.getLongArrayValue());
            case DOUBLE_ARRAY:
                return join(value.getDoubleArrayValue());
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

    private static Double removeAiSamplingPercentage(Map<String, AttributeValue> attributes) {
        return removeAttributeDouble(attributes, "ai.sampling.percentage");
    }

    private static void addExtraAttributes(Map<String, String> properties, Map<String, AttributeValue> attributes) {
        for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
            String value = getStringValue(entry.getValue());
            if (value != null) {
                properties.put(entry.getKey(), value);
            }
        }
    }

    private static void addExtraAttributes(final Map<String, String> properties, Attributes attributes) {
        attributes.forEach(new ReadableKeyValuePairs.KeyValueConsumer<AttributeValue>() {
            @Override
            public void consume(String key, AttributeValue value) {
                String val = getStringValue(value);
                if (val != null) {
                    properties.put(key, val);
                }
            }
        });
    }
}
