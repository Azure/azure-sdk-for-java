// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.*;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.SeverityLevel;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedTime;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.monitor.opentelemetry.exporter.implementation.MappingsBuilder.MappingType.LOG;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

public class LogDataMapper {

    private static final ClientLogger logger = new ClientLogger(LogDataMapper.class);

    private static final String LOG4J_MDC_PREFIX = "log4j.mdc."; // log4j 1.2
    private static final String LOG4J_CONTEXT_DATA_PREFIX = "log4j.context_data."; // log4j 2.x
    private static final String LOGBACK_MDC_PREFIX = "logback.mdc.";
    private static final String JBOSS_LOGGING_MDC_PREFIX = "jboss-logmanager.mdc.";

    private static final String LOG4J_MAP_MESSAGE_PREFIX = "log4j.map_message."; // log4j 2.x

    private static final AttributeKey<String> LOG4J_MARKER = stringKey("log4j.marker");
    private static final AttributeKey<List<String>> LOGBACK_MARKER = stringArrayKey("logback.marker");

    private static final Mappings MAPPINGS;

    static {
        MappingsBuilder mappingsBuilder =
            new MappingsBuilder(LOG)
                .prefix(
                    LOG4J_MDC_PREFIX,
                    (telemetryBuilder, key, value) -> {
                        telemetryBuilder.addProperty(
                            key.substring(LOG4J_MDC_PREFIX.length()), String.valueOf(value));
                    })
                .prefix(
                    LOG4J_CONTEXT_DATA_PREFIX,
                    (telemetryBuilder, key, value) -> {
                        telemetryBuilder.addProperty(
                            key.substring(LOG4J_CONTEXT_DATA_PREFIX.length()), String.valueOf(value));
                    })
                .prefix(
                    LOGBACK_MDC_PREFIX,
                    (telemetryBuilder, key, value) -> {
                        telemetryBuilder.addProperty(
                            key.substring(LOGBACK_MDC_PREFIX.length()), String.valueOf(value));
                    })
                .prefix(
                    JBOSS_LOGGING_MDC_PREFIX,
                    (telemetryBuilder, key, value) -> {
                        telemetryBuilder.addProperty(
                            key.substring(JBOSS_LOGGING_MDC_PREFIX.length()), String.valueOf(value));
                    })
                .prefix(
                    LOG4J_MAP_MESSAGE_PREFIX,
                    (telemetryBuilder, key, value) -> {
                        telemetryBuilder.addProperty(
                            key.substring(LOG4J_MAP_MESSAGE_PREFIX.length()), String.valueOf(value));
                    })
                .exactString(SemanticAttributes.CODE_FILEPATH, "FileName")
                .exactString(SemanticAttributes.CODE_NAMESPACE, "ClassName")
                .exactString(SemanticAttributes.CODE_FUNCTION, "MethodName")
                .exactLong(SemanticAttributes.CODE_LINENO, "LineNumber")
                .exactString(LOG4J_MARKER, "Marker")
                .exactStringArray(LOGBACK_MARKER, "Marker");

        SpanDataMapper.applyCommonTags(mappingsBuilder);

        MAPPINGS = mappingsBuilder.build();
    }

    private final boolean captureLoggingLevelAsCustomDimension;
    private final boolean captureAzureFunctionsAttributes;
    private final BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer;

    public LogDataMapper(
        boolean captureLoggingLevelAsCustomDimension,
        boolean captureAzureFunctionsAttributes,
        BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer) {

        this.captureLoggingLevelAsCustomDimension = captureLoggingLevelAsCustomDimension;
        this.captureAzureFunctionsAttributes = captureAzureFunctionsAttributes;
        this.telemetryInitializer = telemetryInitializer;
    }

    public TelemetryItem map(LogRecordData log, @Nullable String stack, @Nullable Double sampleRate) {
        if (sampleRate == null) {
            sampleRate = getSampleRate(log);
        }
        if (stack == null) {
            return createMessageTelemetryItem(log, sampleRate);
        } else {
            return createExceptionTelemetryItem(log, stack, sampleRate);
        }
    }

    private TelemetryItem createMessageTelemetryItem(LogRecordData log, @Nullable Double sampleRate) {
        MessageTelemetryBuilder telemetryBuilder = MessageTelemetryBuilder.create();
        telemetryInitializer.accept(telemetryBuilder, log.getResource());

        // set standard properties
        setOperationTags(telemetryBuilder, log);
        setTime(telemetryBuilder, log);
        setSampleRate(telemetryBuilder, sampleRate);

        // update tags
        Attributes attributes = log.getAttributes();
        if (captureAzureFunctionsAttributes) {
            setFunctionExtraTraceAttributes(telemetryBuilder, attributes);
        }
        MAPPINGS.map(attributes, telemetryBuilder);

        telemetryBuilder.setSeverityLevel(toSeverityLevel(log.getSeverity()));
        telemetryBuilder.setMessage(log.getBody().asString());

        // set message-specific properties
        setLoggerProperties(
            telemetryBuilder,
            log.getInstrumentationScopeInfo().getName(),
            attributes.get(SemanticAttributes.THREAD_NAME),
            log.getSeverity());

        return telemetryBuilder.build();
    }

    private TelemetryItem createExceptionTelemetryItem(
        LogRecordData log, String stack, @Nullable Double sampleRate) {
        ExceptionTelemetryBuilder telemetryBuilder = ExceptionTelemetryBuilder.create();
        telemetryInitializer.accept(telemetryBuilder, log.getResource());

        // set standard properties
        setOperationTags(telemetryBuilder, log);
        setTime(telemetryBuilder, log);
        setSampleRate(telemetryBuilder, sampleRate);

        // update tags
        Attributes attributes = log.getAttributes();
        MAPPINGS.map(attributes, telemetryBuilder);

        List<ExceptionDetailBuilder> builders = Exceptions.minimalParse(stack);
        ExceptionDetailBuilder exceptionDetailBuilder = builders.get(0);
        String type = log.getAttributes().get(SemanticAttributes.EXCEPTION_TYPE);
        if (type != null && !type.isEmpty()) {
            exceptionDetailBuilder.setTypeName(type);
        }
        String message = log.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE);
        if (message != null && !message.isEmpty()) {
            exceptionDetailBuilder.setMessage(message);
        }
        telemetryBuilder.setExceptions(builders);
        telemetryBuilder.setSeverityLevel(toSeverityLevel(log.getSeverity()));

        // set exception-specific properties
        setLoggerProperties(
            telemetryBuilder,
            log.getInstrumentationScopeInfo().getName(),
            attributes.get(SemanticAttributes.THREAD_NAME),
            log.getSeverity());

        if (log.getBody() != null) {
            telemetryBuilder.addProperty("Logger Message", log.getBody().asString());
        }

        return telemetryBuilder.build();
    }

    private static void setOperationTags(
        AbstractTelemetryBuilder telemetryBuilder, LogRecordData log) {
        SpanContext spanContext = log.getSpanContext();
        if (spanContext.isValid()) {
            telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_ID.toString(), spanContext.getTraceId());
            telemetryBuilder.addTag(
                ContextTagKeys.AI_OPERATION_PARENT_ID.toString(), spanContext.getSpanId());
        }
        setOperationName(telemetryBuilder, log.getAttributes());
    }

    private static void setOperationName(
        AbstractTelemetryBuilder telemetryBuilder, Attributes attributes) {
        String operationName = attributes.get(AiSemanticAttributes.OPERATION_NAME);
        if (operationName != null) {
            telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_NAME.toString(), operationName);
        }
    }

    private static void setTime(AbstractTelemetryBuilder telemetryBuilder, LogRecordData log) {
        telemetryBuilder.setTime(FormattedTime.offSetDateTimeFromEpochNanos(getTimestampEpochNanosWithFallback(log)));
    }

    private static long getTimestampEpochNanosWithFallback(LogRecordData log) {
        long timestamp = log.getTimestampEpochNanos();
        if (timestamp != 0) {
            return timestamp;
        }
        return log.getObservedTimestampEpochNanos();
    }

    private static void setSampleRate(AbstractTelemetryBuilder telemetryBuilder, @Nullable Double sampleRate) {
        if (sampleRate != null) {
            telemetryBuilder.setSampleRate(sampleRate.floatValue());
        }
    }

    @Nullable
    private static Double getSampleRate(LogRecordData log) {
        return log.getAttributes().get(AiSemanticAttributes.SAMPLE_RATE);
    }

    private static void setFunctionExtraTraceAttributes(
        AbstractTelemetryBuilder telemetryBuilder, Attributes attributes) {
        String invocationId = attributes.get(AiSemanticAttributes.AZ_FN_INVOCATION_ID);
        if (invocationId != null) {
            telemetryBuilder.addProperty("InvocationId", invocationId);
        }
        String processId = attributes.get(AiSemanticAttributes.AZ_FN_PROCESS_ID);
        if (processId != null) {
            telemetryBuilder.addProperty("ProcessId", processId);
        }
        String logLevel = attributes.get(AiSemanticAttributes.AZ_FN_LOG_LEVEL);
        if (logLevel != null) {
            telemetryBuilder.addProperty("LogLevel", logLevel);
        }
        String category = attributes.get(AiSemanticAttributes.AZ_FN_CATEGORY);
        if (category != null) {
            telemetryBuilder.addProperty("Category", category);
        }
        String hostInstanceId = attributes.get(AiSemanticAttributes.AZ_FN_HOST_INSTANCE_ID);
        if (hostInstanceId != null) {
            telemetryBuilder.addProperty("HostInstanceId", hostInstanceId);
        }
        String liveLogsSessionId = attributes.get(AiSemanticAttributes.AZ_FN_LIVE_LOGS_SESSION_ID);
        if (liveLogsSessionId != null) {
            telemetryBuilder.addProperty("#AzFuncLiveLogsSessionId", liveLogsSessionId);
        }
    }

    private void setLoggerProperties(
        AbstractTelemetryBuilder telemetryBuilder,
        @Nullable String loggerName,
        @Nullable String threadName,
        Severity severity) {

        telemetryBuilder.addProperty("SourceType", "Logger");

        if (captureLoggingLevelAsCustomDimension) {
            String loggingLevel = mapSeverityToLoggingLevel(severity);
            if (loggingLevel != null) {
                telemetryBuilder.addProperty("LoggingLevel", loggingLevel);
            }
        }

        if (loggerName != null) {
            telemetryBuilder.addProperty("LoggerName", loggerName);
        }
        if (threadName != null) {
            telemetryBuilder.addProperty("ThreadName", threadName);
        }
    }

    @Nullable
    private static SeverityLevel toSeverityLevel(Severity severity) {
        switch (severity) {
            case UNDEFINED_SEVERITY_NUMBER:
                // TODO (trask) AI mapping: is this a good fallback?
            case TRACE:
            case TRACE2:
            case TRACE3:
            case TRACE4:
            case DEBUG:
            case DEBUG2:
            case DEBUG3:
            case DEBUG4:
                return SeverityLevel.VERBOSE;
            case INFO:
            case INFO2:
            case INFO3:
            case INFO4:
                return SeverityLevel.INFORMATION;
            case WARN:
            case WARN2:
            case WARN3:
            case WARN4:
                return SeverityLevel.WARNING;
            case ERROR:
            case ERROR2:
            case ERROR3:
            case ERROR4:
                return SeverityLevel.ERROR;
            case FATAL:
            case FATAL2:
            case FATAL3:
            case FATAL4:
                return SeverityLevel.CRITICAL;
        }
        // TODO (trask) AI mapping: is this a good fallback?
        return SeverityLevel.VERBOSE;
    }

    // TODO need to retrieve logging frameworks' name (Log4j, Logback, Java Util Logging) so that we
    // can correctly map Severity to logging level
    @Nullable
    private static String mapSeverityToLoggingLevel(Severity severity) {
        switch (severity) {
            case UNDEFINED_SEVERITY_NUMBER:
                return null;
            case FATAL:
            case FATAL2:
            case FATAL3:
            case FATAL4:
                return "FATAL";
            case ERROR:
            case ERROR2:
            case ERROR3:
            case ERROR4:
                return "ERROR";
            case WARN:
            case WARN2:
            case WARN3:
            case WARN4:
                return "WARN";
            case INFO:
            case INFO2:
            case INFO3:
            case INFO4:
                return "INFO";
            case DEBUG:
            case DEBUG2:
            case DEBUG3:
            case DEBUG4:
                return "DEBUG";
            case TRACE:
            case TRACE2:
            case TRACE3:
            case TRACE4:
                return "TRACE";
            default:
                logger.error("Unexpected severity {}", severity);
                return null;
        }
    }
}
