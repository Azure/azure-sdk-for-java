// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;

/**
 * Helper class responsible for efficient reporting metrics in AMQP core. It's efficient and safe to use when there is no
 * meter configured by client SDK when metrics are disabled.
 */
public class AmqpMetricsProvider {
    public static final String STATUS_CODE_KEY = "amqpStatusCode";
    public static final String MANAGEMENT_OPERATION_KEY = "amqpOperation";
    private static final ClientLogger LOGGER = new ClientLogger(AmqpMetricsProvider.class);
    private static final Symbol ENQUEUED_TIME_ANNOTATION = Symbol.valueOf(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());

    private static final String AZURE_CORE_AMQP_PROPERTIES_NAME = "azure-core.properties";
    private static final String AZURE_CORE_AMQP_PROPERTIES_VERSION_KEY = "version";

    private static final String AZURE_CORE_VERSION = CoreUtils
        .getProperties(AZURE_CORE_AMQP_PROPERTIES_NAME)
        .getOrDefault(AZURE_CORE_AMQP_PROPERTIES_VERSION_KEY, null);

    private static final Meter DEFAULT_METER = MeterProvider.getDefaultProvider().createMeter("azure-core-amqp", AZURE_CORE_VERSION, new MetricsOptions());
    private static final AmqpMetricsProvider NOOP = new AmqpMetricsProvider();
    private final boolean isEnabled;
    private final Meter meter;
    private Map<String, Object> commonAttributesMap;
    private DoubleHistogram sendDuration = null;
    private DoubleHistogram requestResponseDuration = null;
    private LongCounter closedConnections = null;
    private LongCounter sessionErrors = null;
    private LongCounter linkErrors = null;
    private LongCounter transportErrors = null;
    private DoubleHistogram receivedLag = null;
    private LongCounter addCredits = null;
    private AttributeCache sendAttributeCache = null;
    private AttributeCacheTwoDimensional requestResponseAttributeCache = null;
    private AttributeCache amqpErrorAttributeCache = null;
    private TelemetryAttributes commonAttributes = null;

    private AmqpMetricsProvider() {
        this.isEnabled = false;
        this.meter = DEFAULT_METER;
    }

    public enum ErrorSource {
        LINK,
        SESSION,
        TRANSPORT
    }

    public AmqpMetricsProvider(Meter meter, String namespace, String entityPath) {
        this.meter = meter != null ? meter : DEFAULT_METER;
        this.isEnabled = this.meter.isEnabled();

        if (isEnabled) {
            this.commonAttributesMap = new HashMap<>();
            commonAttributesMap.put(ClientConstants.HOSTNAME_KEY, namespace);

            if (entityPath != null) {
                int entityNameEnd = entityPath.indexOf('/');
                if (entityNameEnd > 0) {
                    commonAttributesMap.put(ClientConstants.ENTITY_NAME_KEY,  entityPath.substring(0, entityNameEnd));
                    commonAttributesMap.put(ClientConstants.ENTITY_PATH_KEY, entityPath);
                } else {
                    commonAttributesMap.put(ClientConstants.ENTITY_NAME_KEY,  entityPath);
                }
            }

            this.commonAttributes = this.meter.createAttributes(commonAttributesMap);
            this.requestResponseAttributeCache = new AttributeCacheTwoDimensional(STATUS_CODE_KEY, MANAGEMENT_OPERATION_KEY);
            this.sendAttributeCache = new AttributeCache(ClientConstants.DELIVERY_STATE_KEY);
            this.amqpErrorAttributeCache = new AttributeCache(ClientConstants.ERROR_CONDITION_KEY);
            this.sendDuration = this.meter.createDoubleHistogram("messaging.az.amqp.producer.send.duration", "Duration of AMQP-level send call.", "ms");
            this.requestResponseDuration = this.meter.createDoubleHistogram("messaging.az.amqp.management.request.duration", "Duration of AMQP request-response operation.", "ms");
            this.closedConnections = this.meter.createLongCounter("messaging.az.amqp.client.connections.closed", "Closed connections", "connections");
            this.sessionErrors = this.meter.createLongCounter("messaging.az.amqp.client.session.errors", "AMQP session errors", "errors");
            this.linkErrors = this.meter.createLongCounter("messaging.az.amqp.client.link.errors", "AMQP link errors", "errors");
            this.transportErrors = this.meter.createLongCounter("messaging.az.amqp.client.transport.errors", "AMQP session errors", "errors");
            this.addCredits = this.meter.createLongCounter("messaging.az.amqp.consumer.credits.requested", "Number of requested credits", "credits");
            this.receivedLag = this.meter.createDoubleHistogram("messaging.az.amqp.consumer.lag", "Approximate lag between time message was received and time it was enqueued on the broker.", "sec");
        }
    }

    public static AmqpMetricsProvider noop() {
        return NOOP;
    }

    /**
     * Checks if record delivers is enabled (for micro-optimizations).
     */
    public boolean isSendDeliveryEnabled() {
        return isEnabled && sendDuration.isEnabled();
    }

    /**
     * Records duration of AMQP send call.
     */
    public void recordSend(long start, DeliveryState.DeliveryStateType deliveryState) {
        if (isEnabled && sendDuration.isEnabled()) {
            TelemetryAttributes attributes = sendAttributeCache.getOrCreate(deliveryStateToLowerCaseString(deliveryState));
            sendDuration.record(Instant.now().toEpochMilli() - start, attributes, Context.NONE);
        }
    }

    /**
     * Records duration of AMQP management call.
     */
    public void recordRequestResponseDuration(long start, String operationName, AmqpResponseCode responseCode) {
        if (isEnabled && requestResponseDuration.isEnabled()) {
            TelemetryAttributes attributes = requestResponseAttributeCache.getOrCreate(deliveryStateToLowerCaseString(responseCode), operationName);
            requestResponseDuration.record(Instant.now().toEpochMilli() - start, attributes, Context.NONE);
        }
    }

    /**
     * Records connection close.
     */
    public void recordConnectionClosed(ErrorCondition condition) {
        if (isEnabled && closedConnections.isEnabled()) {
            Symbol conditionSymbol = condition != null ? condition.getCondition() : null;
            String conditionStr = conditionSymbol != null ? conditionSymbol.toString() : "ok";
            closedConnections.add(1, amqpErrorAttributeCache.getOrCreate(conditionStr), Context.NONE);
        }
    }

    /**
     * Records the message was received.
     */
    public void recordReceivedMessage(Message message) {
        if (!isEnabled || !receivedLag.isEnabled()
            || message == null
            || message.getMessageAnnotations() == null
            || message.getBody() == null) {
            return;
        }

        Map<Symbol, Object> properties = message.getMessageAnnotations().getValue();
        Object enqueuedTimeDate = properties != null ? properties.get(ENQUEUED_TIME_ANNOTATION) : null;
        if (enqueuedTimeDate instanceof Date) {
            Instant enqueuedTime = ((Date) enqueuedTimeDate).toInstant();
            long deltaMs = Instant.now().toEpochMilli() - enqueuedTime.toEpochMilli();
            if (deltaMs < 0) {
                deltaMs = 0;
            }
            receivedLag.record(deltaMs / 1000d, commonAttributes, Context.NONE);
        } else {
            LOGGER.verbose("Received message has unexpected `x-opt-enqueued-time` annotation value - `{}`. Ignoring it.", enqueuedTimeDate);
        }
    }

    /**
     * Records that credits were added to link
     */
    public void recordAddCredits(int credits) {
        if (isEnabled && addCredits.isEnabled()) {
            addCredits.add(credits, commonAttributes, Context.NONE);
        }
    }

    /**
     * Records link error. Noop if condition is null (no error).
     */
    public void recordHandlerError(ErrorSource source, ErrorCondition condition) {
        if (isEnabled && condition != null && condition.getCondition() != null) {
            TelemetryAttributes attributes = amqpErrorAttributeCache.getOrCreate(condition.getCondition().toString());
            switch (source) {
                case LINK:
                    if (linkErrors.isEnabled()) {
                        linkErrors.add(1, attributes, Context.NONE);
                    }
                    break;
                case SESSION:
                    if (sessionErrors.isEnabled()) {
                        sessionErrors.add(1, attributes, Context.NONE);
                    }
                    break;
                case TRANSPORT:
                    if (transportErrors.isEnabled()) {
                        transportErrors.add(1, attributes, Context.NONE);
                    }
                    break;
                default:
                    LOGGER.verbose("Unexpected error source: {}", source);
            }
        }
    }

    private static String deliveryStateToLowerCaseString(DeliveryState.DeliveryStateType state) {
        if (state == null) {
            return "error";
        }

        switch (state) {
            case Accepted:
                return "accepted";
            case Declared:
                return "declared";
            case Modified:
                return "modified";
            case Received:
                return "received";
            case Rejected:
                return "rejected";
            case Released:
                return "released";
            case Transactional:
                return "transactional";
            default:
                return "unknown";
        }
    }

    private static String deliveryStateToLowerCaseString(AmqpResponseCode response) {
        if (response == null) {
            return "error";
        }

        switch (response) {
            case OK:
                return "ok";
            case ACCEPTED:
                return "accepted";
            case BAD_REQUEST:
                return "bad_request";
            case NOT_FOUND:
                return "not_found";
            case FORBIDDEN:
                return "forbidden";
            case INTERNAL_SERVER_ERROR:
                return "internal_server_error";
            case UNAUTHORIZED:
                return "unauthorized";
            case CONTINUE:
                return "continue";
            case SWITCHING_PROTOCOLS:
                return "switching_protocols";
            case CREATED:
                return "created";
            case NON_AUTHORITATIVE_INFORMATION:
                return "not_authoritative_information";
            case NO_CONTENT:
                return "no_content";
            case RESET_CONTENT:
                return "reset_content";
            case PARTIAL_CONTENT:
                return "partial_content";
            case AMBIGUOUS:
                return "ambiguous";
            case MULTIPLE_CHOICES:
                return "multiple_choices";
            case MOVED:
                return "moved";
            case MOVED_PERMANENTLY:
                return "moved_permanently";
            case FOUND:
                return "found";
            case REDIRECT:
                return "redirect";
            case REDIRECT_METHOD:
                return "redirect_method";
            case SEE_OTHER:
                return "see_other";
            case NOT_MODIFIED:
                return "not_modified";
            case USE_PROXY:
                return "use_proxy";
            case UNUSED:
                return "unused";
            case REDIRECT_KEEP_VERB:
                return "redirect_keep_verb";
            case TEMPORARY_REDIRECT:
                return "temporary_redirect";
            case PAYMENT_REQUIRED:
                return "payment_required";
            case METHOD_NOT_ALLOWED:
                return "";

            case NOT_ACCEPTABLE:
                return "";

            case PROXY_AUTHENTICATION_REQUIRED:
                return "";

            case REQUEST_TIMEOUT:
                return "";

            case CONFLICT:
                return "";

            case GONE:
                return "";

            case LENGTH_REQUIRED:
                return "";

            case PRECONDITION_FAILED:
                return "";

            case REQUEST_ENTITY_TOO_LARGE:
                return "";

            case REQUEST_URI_TOO_LONG:
                return "";

            case UNSUPPORTED_MEDIA_TYPE:
                return "";

            case REQUESTED_RANGE_NOT_SATISFIABLE:
                return "";
            case EXPECTATION_FAILED:
                return "expectation_failed";
            case UPGRADE_REQUIRED:
                return "upgrade_required";
            case NOT_IMPLEMENTED:
                return "";
            case BAD_GATEWAY:
                return "bad_gateway";
            case SERVICE_UNAVAILABLE:
                return "service_unavailable";
            case GATEWAY_TIMEOUT:
                return "gateway_timeout";
            case HTTP_VERSION_NOT_SUPPORTED:
                return "http_version_not_supported";
            default:
                return "error";
        }
    }

    private class AttributeCache {
        private final Map<String, TelemetryAttributes> attr = new ConcurrentHashMap<>();
        private final String dimensionName;
        AttributeCache(String dimensionName) {
            this.dimensionName = dimensionName;
        }

        public TelemetryAttributes getOrCreate(String value) {
            return attr.computeIfAbsent(value, this::create);
        }

        private TelemetryAttributes create(String value) {
            Map<String, Object> attributes = new HashMap<>(commonAttributesMap);
            attributes.put(dimensionName, value);
            return meter.createAttributes(attributes);
        }
    }

    private class AttributeCacheTwoDimensional {
        private final Map<String, TelemetryAttributes> attr = new ConcurrentHashMap<>();
        private final String dimensionName1;
        private final String dimensionName2;
        AttributeCacheTwoDimensional(String dimension1, String dimension2) {
            this.dimensionName1 = dimension1;
            this.dimensionName2 = dimension2;
        }

        public TelemetryAttributes getOrCreate(String value1, String value2) {
            return attr.computeIfAbsent(value1 + ":" + value2, Ignored -> create(value1, value2));
        }

        private TelemetryAttributes create(String value1, String value2) {
            Map<String, Object> attributes = new HashMap<>(commonAttributesMap);
            attributes.put(dimensionName1, value1);
            attributes.put(dimensionName2, value2);
            return meter.createAttributes(attributes);
        }
    }
}
