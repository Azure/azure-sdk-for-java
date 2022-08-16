// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.FalseRuleFilter;
import com.azure.messaging.servicebus.administration.models.RuleAction;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SqlRuleAction;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.DescribedType;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import reactor.core.publisher.Signal;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_SERVICE_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.EPOCH_TICKS;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.TICK_PER_SECOND;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.TIME_LENGTH_DELTA;


/**
 * Contains helper methods for message conversions, reading status codes, and getting delivery state.
 */
public final class MessageUtils {
    static final UUID ZERO_LOCK_TOKEN = new UUID(0L, 0L);
    static final int LOCK_TOKEN_SIZE = 16;

    private static final Symbol DEAD_LETTER_OPERATION = Symbol.getSymbol(AmqpConstants.VENDOR + ":dead-letter");
    private static final String DEAD_LETTER_REASON = "DeadLetterReason";
    private static final String DEAD_LETTER_ERROR_DESCRIPTION = "DeadLetterErrorDescription";
    private static final long EPOCH_IN_DOT_NET_TICKS = 621355968000000000L;
    private static final int GUID_SIZE = 16;

    private MessageUtils() {
    }

    // Pass little less than client timeout to the server so client doesn't time out before server times out
    public static Duration adjustServerTimeout(Duration clientTimeout) {
        return clientTimeout.minusMillis(1000);
    }

    /**
     * Calculate the total time from the retry options assuming all retries are exhausted.
     */
    public static Duration getTotalTimeout(AmqpRetryOptions retryOptions) {
        long tryTimeout = retryOptions.getTryTimeout().toNanos();
        long maxDelay = retryOptions.getMaxDelay().toNanos();
        long totalTimeout = tryTimeout;  // The original attempt not counted as a retry
        if (retryOptions.getMode() == AmqpRetryMode.FIXED) {
            totalTimeout += (retryOptions.getDelay().toNanos() + tryTimeout) * retryOptions.getMaxRetries();
        } else {
            int multiplier = 1;
            for (int i = 0; i < retryOptions.getMaxRetries(); i++) {
                long retryDelay = retryOptions.getDelay().toNanos() * multiplier;
                if (retryDelay >= maxDelay) {
                    retryDelay = maxDelay;
                    totalTimeout += (tryTimeout + retryDelay) * (retryOptions.getMaxRetries() - i);
                    break;
                }
                multiplier *= 2;
                totalTimeout += tryTimeout + retryDelay;
            }
        }
        return Duration.ofNanos(totalTimeout);
    }
    /**
     * Converts a .NET GUID to its Java UUID representation.
     *
     * @param dotNetBytes .NET GUID to convert.
     *
     * @return the equivalent UUID.
     */
    static UUID convertDotNetBytesToUUID(byte[] dotNetBytes) {
        // First 4 bytes are in reverse order, 5th and 6th bytes are in reverse order,
        // 7th and 8th bytes are also in reverse order
        if (dotNetBytes == null || dotNetBytes.length != GUID_SIZE) {
            return ZERO_LOCK_TOKEN;
        }

        final byte[] reOrderedBytes = reorderBytes(dotNetBytes);

        final ByteBuffer buffer = ByteBuffer.wrap(reOrderedBytes);
        final long mostSignificantBits = buffer.getLong();
        final long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    /**
     * Converts a Java UUID to its byte[] representation.
     *
     * @param uuid UUID to convert to .NET bytes.
     *
     * @return The .NET byte representation.
     */
    static byte[] convertUUIDToDotNetBytes(UUID uuid) {
        if (uuid == null || uuid.equals(ZERO_LOCK_TOKEN)) {
            return new byte[GUID_SIZE];
        }

        ByteBuffer buffer = ByteBuffer.allocate(GUID_SIZE);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        byte[] javaBytes = buffer.array();

        return reorderBytes(javaBytes);
    }

    /**
     * Gets the {@link OffsetDateTime} representation of .NET epoch ticks. .NET ticks are measured from 0001/01/01.
     * Java {@link OffsetDateTime} is measured from 1970/01/01.
     *
     * @param dotNetTicks long measured from 01/01/0001
     *
     * @return The instant represented by the ticks.
     */
    static OffsetDateTime convertDotNetTicksToOffsetDateTime(long dotNetTicks) {
        long ticksFromEpoch = dotNetTicks - EPOCH_IN_DOT_NET_TICKS;
        long millisecondsFromEpoch = Double.valueOf(ticksFromEpoch * 0.0001).longValue();
        long fractionTicks = ticksFromEpoch % 10000;

        return Instant.ofEpochMilli(millisecondsFromEpoch).plusNanos(fractionTicks * 100).atOffset(ZoneOffset.UTC);
    }

    /**
     * Given the disposition state, returns its associated delivery state.
     *
     * @return The corresponding DeliveryState, or null if the disposition status is unknown.
     */
    public static DeliveryState getDeliveryState(DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {

        boolean hasTransaction = transactionContext != null && transactionContext.getTransactionId() != null;

        final DeliveryState state;
        switch (dispositionStatus) {
            case COMPLETED:
                if (hasTransaction) {
                    state = getTransactionState(transactionContext.getTransactionId(), Accepted.getInstance());
                } else {
                    state = Accepted.getInstance();
                }
                break;
            case SUSPENDED:
                final Rejected rejected = new Rejected();
                final ErrorCondition error = new ErrorCondition(DEAD_LETTER_OPERATION, null);
                final Map<String, Object> errorInfo = new HashMap<>();
                if (!CoreUtils.isNullOrEmpty(deadLetterReason)) {
                    errorInfo.put(DEAD_LETTER_REASON, deadLetterReason);
                }
                if (!CoreUtils.isNullOrEmpty(deadLetterErrorDescription)) {
                    errorInfo.put(DEAD_LETTER_ERROR_DESCRIPTION, deadLetterErrorDescription);
                }
                if (propertiesToModify != null) {
                    errorInfo.putAll(propertiesToModify);
                }
                error.setInfo(errorInfo);
                rejected.setError(error);

                if (hasTransaction) {
                    state = getTransactionState(transactionContext.getTransactionId(), rejected);
                } else {
                    state = rejected;
                }
                break;
            case ABANDONED:
                final Modified outcome = new Modified();
                if (propertiesToModify != null) {
                    outcome.setMessageAnnotations(propertiesToModify);
                }

                if (hasTransaction) {
                    state = getTransactionState(transactionContext.getTransactionId(), outcome);
                } else {
                    state = outcome;
                }
                break;
            case DEFERRED:
                final Modified deferredOutcome = new Modified();
                deferredOutcome.setUndeliverableHere(true);
                if (propertiesToModify != null) {
                    deferredOutcome.setMessageAnnotations(propertiesToModify);
                }

                if (hasTransaction) {
                    state = getTransactionState(transactionContext.getTransactionId(), deferredOutcome);
                } else {
                    state = deferredOutcome;
                }
                break;
            case RELEASED:
                state = Released.getInstance();
                break;
            default:
                state = null;
        }

        return state;
    }

    /**
     * Gets the primitive value or {@code false} if there is no value.
     *
     * @param value The value.
     * @return It's primitive type.
     */
    public static boolean toPrimitive(Boolean value) {
        return value != null ? value : false;
    }

    /**
     * Gets the primitive value or 0 if there is no value.
     *
     * @param value The value.
     * @return It's primitive type.
     */
    public static int toPrimitive(Integer value) {
        return value != null ? value : 0;
    }

    /**
     * Gets the primitive value or {@code 0L} if there is no value.
     *
     * @param value The value.
     * @return It's primitive type.
     */
    public static long toPrimitive(Long value) {
        return value != null ? value : 0L;
    }

    private static byte[] reorderBytes(byte[] javaBytes) {
        byte[] reorderedBytes = new byte[GUID_SIZE];
        for (int i = 0; i < GUID_SIZE; i++) {
            int indexInReorderedBytes;
            switch (i) {
                case 0:
                    indexInReorderedBytes = 3;
                    break;
                case 1:
                    indexInReorderedBytes = 2;
                    break;
                case 2:
                    indexInReorderedBytes = 1;
                    break;
                case 3:
                    indexInReorderedBytes = 0;
                    break;
                case 4:
                    indexInReorderedBytes = 5;
                    break;
                case 5:
                    indexInReorderedBytes = 4;
                    break;
                case 6:
                    indexInReorderedBytes = 7;
                    break;
                case 7:
                    indexInReorderedBytes = 6;
                    break;
                default:
                    indexInReorderedBytes = i;
            }

            reorderedBytes[indexInReorderedBytes] = javaBytes[i];
        }

        return reorderedBytes;
    }

    private static TransactionalState getTransactionState(ByteBuffer transactionId, Outcome outcome) {
        TransactionalState transactionalState = new TransactionalState();

        // Use Binary.create(ByteBuffer) as it'll handle differences between HeapByteBuffer and DirectByteBuffer, as
        // well as handling when the ByteBuffer is read-only.
        transactionalState.setTxnId(Binary.create(transactionId));
        transactionalState.setOutcome(outcome);
        return transactionalState;
    }

    /**
     * Used in ServiceBusMessageBatch.tryAddMessage() to start tracing for to-be-sent out messages.
     */
    public static ServiceBusMessage traceMessageSpan(ServiceBusMessage serviceBusMessage,
        Context messageContext, String hostname, String entityPath, TracerProvider tracerProvider) {
        Optional<Object> eventContextData = messageContext.getData(SPAN_CONTEXT_KEY);
        if (eventContextData.isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return serviceBusMessage;
        } else {
            // Starting the span makes the sampling decision (nothing is logged at this time)
            Context newMessageContext = messageContext
                .addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE)
                .addData(ENTITY_PATH_KEY, entityPath)
                .addData(HOST_NAME_KEY, hostname);
            Context eventSpanContext = tracerProvider.startSpan(AZ_TRACING_SERVICE_NAME, newMessageContext,
                ProcessKind.MESSAGE);
            Optional<Object> eventDiagnosticIdOptional = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);
            if (eventDiagnosticIdOptional.isPresent()) {
                serviceBusMessage.getApplicationProperties().put(DIAGNOSTIC_ID_KEY, eventDiagnosticIdOptional.get()
                    .toString());
                tracerProvider.endSpan(eventSpanContext, Signal.complete());
                serviceBusMessage.addContext(SPAN_CONTEXT_KEY, eventSpanContext);
            }
        }
        return serviceBusMessage;
    }


    /**
     * Convert DescribedType to origin type based on the descriptor.
     * @param describedType Service bus defined DescribedType.
     * @param <T> Including URI, OffsetDateTime and Duration
     * @return Original type value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T describedToOrigin(DescribedType describedType) {
        Object descriptor = describedType.getDescriptor();
        Object described = describedType.getDescribed();
        Objects.requireNonNull(descriptor, "descriptor of described type cannot be null.");
        Objects.requireNonNull(described, "described of described type cannot be null.");

        if (ServiceBusConstants.URI_SYMBOL.equals(descriptor)) {
            try {
                return (T) URI.create((String) described);
            } catch (IllegalArgumentException ex) {
                return (T) described;
            }
        } else if (ServiceBusConstants.OFFSETDATETIME_SYMBOL.equals(descriptor)) {
            // Convert tick value to OffsetDateTime
            long tickTime = (long) described - EPOCH_TICKS;
            int nano = (int) ((tickTime % TICK_PER_SECOND) * TIME_LENGTH_DELTA);
            long seconds = tickTime / TICK_PER_SECOND;
            return (T) OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nano), ZoneId.systemDefault());
        } else if (ServiceBusConstants.DURATION_SYMBOL.equals(descriptor)) {
            return (T) Duration.ofNanos(((long) described) * TIME_LENGTH_DELTA);
        }
        return (T) described;
    }

    /**
     * Create a map and put {@link SqlRuleFilter} or {@link CorrelationRuleFilter} info into map for management request.
     *
     * @param name name of rule.
     * @param options The options for the rule to add.
     * @return A map with {@link SqlRuleFilter} or {@link CorrelationRuleFilter} info to put into management message body.
     */
    public static Map<String, Object> encodeRuleOptionToMap(String name, CreateRuleOptions options) {
        HashMap<String, Object> descriptionMap = new HashMap<>();
        if (options.getFilter() instanceof SqlRuleFilter) {
            HashMap<String, Object> filterMap = new HashMap<>();
            filterMap.put(ManagementConstants.EXPRESSION, ((SqlRuleFilter) options.getFilter()).getSqlExpression());
            descriptionMap.put(ManagementConstants.SQL_RULE_FILTER, filterMap);
        } else if (options.getFilter() instanceof CorrelationRuleFilter) {
            CorrelationRuleFilter correlationFilter = (CorrelationRuleFilter) options.getFilter();
            HashMap<String, Object> filterMap = new HashMap<>();
            filterMap.put(ManagementConstants.CORRELATION_ID, correlationFilter.getCorrelationId());
            filterMap.put(ManagementConstants.MESSAGE_ID, correlationFilter.getMessageId());
            filterMap.put(ManagementConstants.TO, correlationFilter.getTo());
            filterMap.put(ManagementConstants.REPLY_TO, correlationFilter.getReplyTo());
            filterMap.put(ManagementConstants.LABEL, correlationFilter.getLabel());
            filterMap.put(ManagementConstants.SESSION_ID, correlationFilter.getSessionId());
            filterMap.put(ManagementConstants.REPLY_TO_SESSION_ID, correlationFilter.getReplyToSessionId());
            filterMap.put(ManagementConstants.CONTENT_TYPE, correlationFilter.getContentType());
            filterMap.put(ManagementConstants.CORRELATION_FILTER_PROPERTIES, correlationFilter.getProperties());

            descriptionMap.put(ManagementConstants.CORRELATION_FILTER, filterMap);
        } else {
            throw new IllegalArgumentException("This API supports the addition of only SQLFilters and CorrelationFilters.");
        }

        RuleAction action = options.getAction();
        if (action == null) {
            descriptionMap.put(ManagementConstants.SQL_RULE_ACTION, null);
        } else if (action instanceof SqlRuleAction) {
            HashMap<String, Object> sqlActionMap = new HashMap<>();
            sqlActionMap.put(ManagementConstants.EXPRESSION, ((SqlRuleAction) action).getSqlExpression());
            descriptionMap.put(ManagementConstants.SQL_RULE_ACTION, sqlActionMap);
        } else {
            throw new IllegalArgumentException("This API supports the addition of only filters with SqlRuleActions.");
        }

        descriptionMap.put(ManagementConstants.RULE_NAME, name);

        return descriptionMap;
    }

    /**
     * Get message status from application properties.
     *
     * @param properties The application properties from message.
     * @return Status code.
     */
    public static int getMessageStatus(ApplicationProperties properties) {
        int statusCode = ManagementConstants.UNDEFINED_STATUS_CODE;
        if (properties.getValue() == null) {
            return statusCode;
        }
        Object codeObject = properties.getValue().get(ManagementConstants.STATUS_CODE);
        if (codeObject == null) {
            codeObject = properties.getValue().get(ManagementConstants.LEGACY_STATUS_CODE);
        }
        if (codeObject != null) {
            statusCode = (int) codeObject;
        }
        return statusCode;
    }

    /**
     * Get {@link RuleProperties} from {@link DescribedType}.
     *
     * @param ruleDescribedType A {@link DescribedType} with rule information.
     * @return A {@link RuleProperties} contains name, {@link RuleAction} and {@link RuleFilter}.
     */
    @SuppressWarnings("unchecked")
    public static RuleProperties decodeRuleDescribedType(DescribedType ruleDescribedType) {
        if (ruleDescribedType == null) {
            return null;
        }
        if (!(ruleDescribedType.getDescriptor()).equals(ServiceBusConstants.RULE_DESCRIPTION_NAME)) {
            return null;
        }
        RuleProperties ruleProperties = new RuleProperties();
        if (ruleDescribedType.getDescribed() instanceof ArrayList) {
            ArrayList<Object> describedRule = (ArrayList<Object>) ruleDescribedType.getDescribed();
            int count = describedRule.size();
            if (count-- > 0) {
                ruleProperties.setFilter(decodeFilter((DescribedType) describedRule.get(0)));
            }

            if (count-- > 0) {
                ruleProperties.setAction(decodeRuleAction((DescribedType) describedRule.get(1)));
            }

            if (count > 0) {
                ruleProperties.setName((String) describedRule.get(2));
            }
        }

        return ruleProperties;
    }

    /**
     * Get {@link RuleFilter} from a {@link DescribedType}.
     *
     * @param describedFilter A {@link DescribedType} with rule filter information.
     * @return A {@link RuleFilter}.
     */
    @SuppressWarnings("unchecked")
    private static RuleFilter decodeFilter(DescribedType describedFilter) {
        if (describedFilter.getDescriptor().equals(ServiceBusConstants.SQL_FILTER_NAME)) {
            ArrayList<Object> describedSqlFilter = (ArrayList<Object>) describedFilter.getDescribed();
            if (describedSqlFilter.size() > 0) {
                return new SqlRuleFilter((String) describedSqlFilter.get(0));
            }
        } else if (describedFilter.getDescriptor().equals(ServiceBusConstants.CORRELATION_FILTER_NAME)) {
            CorrelationRuleFilter correlationFilter = new CorrelationRuleFilter();
            ArrayList<Object> describedCorrelationFilter = (ArrayList<Object>) describedFilter.getDescribed();
            int countCorrelationFilter = describedCorrelationFilter.size();
            if (countCorrelationFilter-- > 0) {
                correlationFilter.setCorrelationId((String) (describedCorrelationFilter.get(0)));
            }
            if (countCorrelationFilter-- > 0) {
                correlationFilter.setMessageId((String) (describedCorrelationFilter.get(1)));
            }
            if (countCorrelationFilter-- > 0) {
                correlationFilter.setTo((String) (describedCorrelationFilter.get(2)));
            }
            if (countCorrelationFilter-- > 0) {
                correlationFilter.setReplyTo((String) (describedCorrelationFilter.get(3)));
            }
            if (countCorrelationFilter-- > 0) {
                correlationFilter.setLabel((String) (describedCorrelationFilter.get(4)));
            }
            if (countCorrelationFilter-- > 0) {
                correlationFilter.setSessionId((String) (describedCorrelationFilter.get(5)));
            }
            if (countCorrelationFilter-- > 0) {
                correlationFilter.setReplyToSessionId((String) (describedCorrelationFilter.get(6)));
            }
            if (countCorrelationFilter-- > 0) {
                correlationFilter.setContentType((String) (describedCorrelationFilter.get(7)));
            }
            if (countCorrelationFilter > 0) {
                Object properties = describedCorrelationFilter.get(8);
                if (properties instanceof Map) {
                    correlationFilter.getProperties().putAll((Map<String, ?>) properties);
                }
            }

            return correlationFilter;
        } else if (describedFilter.getDescriptor().equals(ServiceBusConstants.TRUE_FILTER_NAME)) {
            return new TrueRuleFilter();
        } else if (describedFilter.getDescriptor().equals(ServiceBusConstants.FALSE_FILTER_NAME)) {
            return new FalseRuleFilter();
        } else {
            throw new UnsupportedOperationException("This client doesn't support filter with descriptor: " + describedFilter.getDescriptor());
        }
        return null;
    }

    /**
     * Get {@link RuleAction} from a {@link DescribedType}.
     *
     * @param describedAction A {@link DescribedType} with rule action information.
     * @return A {@link RuleAction}.
     */
    @SuppressWarnings("unchecked")
    private static RuleAction decodeRuleAction(DescribedType describedAction) {
        if (describedAction.getDescriptor().equals(ServiceBusConstants.EMPTY_RULE_ACTION_NAME)) {
            return null;
        } else if (describedAction.getDescriptor().equals(ServiceBusConstants.SQL_RULE_ACTION_NAME)) {
            ArrayList<Object> describedSqlAction = (ArrayList<Object>) describedAction.getDescribed();
            if (describedSqlAction.size() > 0) {
                return new SqlRuleAction((String) describedSqlAction.get(0));
            }
        }

        return null;
    }

}
