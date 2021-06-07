// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.amqp.models.DeliveryOutcome;
import com.azure.core.amqp.models.DeliveryState;
import com.azure.core.amqp.models.ModifiedDeliveryOutcome;
import com.azure.core.amqp.models.ReceivedDeliveryOutcome;
import com.azure.core.amqp.models.RejectedDeliveryOutcome;
import com.azure.core.amqp.models.TransactionalDeliveryOutcome;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Footer;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Received;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Converts {@link AmqpAnnotatedMessage messages} to and from proton-j messages.
 */
final class MessageUtils {
    private static final ClientLogger LOGGER = new ClientLogger(MessageUtils.class);
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Converts an {@link AmqpAnnotatedMessage} to a proton-j message.
     *
     * @param message The message to convert.
     *
     * @return The corresponding proton-j message.
     *
     * @throws NullPointerException if {@code message} is null.
     */
    static Message toProtonJMessage(AmqpAnnotatedMessage message) {
        Objects.requireNonNull(message, "'message' to serialize cannot be null.");

        final Message response = Proton.message();

        //TODO (conniey): support AMQP sequence and AMQP value.
        final AmqpMessageBody body = message.getBody();
        switch (body.getBodyType()) {
            case DATA:
                response.setBody(new Data(new Binary(body.getFirstData())));
                break;
            case VALUE:
            case SEQUENCE:
            default:
                throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
                    "bodyType [" + body.getBodyType() + "] is not supported yet."));
        }

        // Setting message properties.
        final AmqpMessageProperties properties = message.getProperties();
        response.setMessageId(properties.getMessageId());
        response.setContentType(properties.getContentType());
        response.setCorrelationId(properties.getCorrelationId());
        response.setSubject(properties.getSubject());

        final AmqpAddress replyTo = properties.getReplyTo();
        response.setReplyTo(replyTo != null ? replyTo.toString() : null);

        response.setReplyToGroupId(properties.getReplyToGroupId());
        response.setGroupId(properties.getGroupId());
        response.setContentEncoding(properties.getContentEncoding());

        if (properties.getGroupSequence() != null) {
            response.setGroupSequence(properties.getGroupSequence());
        }

        final AmqpAddress messageTo = properties.getTo();
        if (response.getProperties() == null) {
            response.setProperties(new Properties());
        }

        response.getProperties().setTo(messageTo != null ? messageTo.toString() : null);

        response.getProperties().setUserId(new Binary(properties.getUserId()));

        if (properties.getAbsoluteExpiryTime() != null) {
            response.getProperties().setAbsoluteExpiryTime(
                Date.from(properties.getAbsoluteExpiryTime().toInstant()));
        }

        if (properties.getCreationTime() != null) {
            response.getProperties().setCreationTime(Date.from(properties.getCreationTime().toInstant()));
        }

        // Set header
        final AmqpMessageHeader header = message.getHeader();
        if (header.getTimeToLive() != null) {
            response.setTtl(header.getTimeToLive().toMillis());
        }
        if (header.getDeliveryCount() != null) {
            response.setDeliveryCount(header.getDeliveryCount());
        }
        if (header.getPriority() != null) {
            response.setPriority(header.getPriority());
        }
        if (header.isDurable() != null) {
            response.setDurable(header.isDurable());
        }
        if (header.isFirstAcquirer() != null) {
            response.setFirstAcquirer(header.isFirstAcquirer());
        }
        if (header.getTimeToLive() != null) {
            response.setTtl(header.getTimeToLive().toMillis());
        }

        // Set footer
        response.setFooter(new Footer(message.getFooter()));

        // Set message annotations.
        final Map<Symbol, Object> messageAnnotations = convert(message.getMessageAnnotations());
        response.setMessageAnnotations(new MessageAnnotations(messageAnnotations));

        // Set Delivery Annotations.
        final Map<Symbol, Object> deliveryAnnotations = convert(message.getDeliveryAnnotations());
        response.setDeliveryAnnotations(new DeliveryAnnotations(deliveryAnnotations));

        // Set application properties
        response.setApplicationProperties(new ApplicationProperties(message.getApplicationProperties()));

        return response;
    }

    /**
     * Converts a proton-j message to {@link AmqpAnnotatedMessage}.
     *
     * @param message The message to convert.
     *
     * @return The corresponding {@link AmqpAnnotatedMessage message}.
     *
     * @throws NullPointerException if {@code message} is null.
     */
    static AmqpAnnotatedMessage toAmqpAnnotatedMessage(Message message) {
        Objects.requireNonNull(message, "'message' cannot be null");

        final byte[] bytes;
        final Section body = message.getBody();
        if (body != null) {
            //TODO (conniey): Support other AMQP types like AmqpValue and AmqpSequence.
            if (body instanceof Data) {
                final Binary messageData = ((Data) body).getValue();
                bytes = messageData.getArray();
            } else {
                LOGGER.warning("Message not of type Data. Actual: {}",
                    body.getType());
                bytes = EMPTY_BYTE_ARRAY;
            }
        } else {
            LOGGER.warning("Message does not have a body.");
            bytes = EMPTY_BYTE_ARRAY;
        }

        final AmqpAnnotatedMessage response = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(bytes));

        // Application properties
        final ApplicationProperties applicationProperties = message.getApplicationProperties();
        if (applicationProperties != null) {
            final Map<String, Object> propertiesValue = applicationProperties.getValue();
            response.getApplicationProperties().putAll(propertiesValue);
        }

        // Header
        final AmqpMessageHeader responseHeader = response.getHeader();
        responseHeader.setTimeToLive(Duration.ofMillis(message.getTtl()));
        responseHeader.setDeliveryCount(message.getDeliveryCount());
        responseHeader.setPriority(message.getPriority());

        if (message.getHeader() != null) {
            responseHeader.setDurable(message.getHeader().getDurable());
            responseHeader.setFirstAcquirer(message.getHeader().getFirstAcquirer());
        }

        // Footer
        final Footer footer = message.getFooter();
        if (footer != null && footer.getValue() != null) {
            @SuppressWarnings("unchecked") final Map<Symbol, Object> footerValue = footer.getValue();

            setValues(footerValue, response.getFooter());
        }

        // Properties
        final AmqpMessageProperties responseProperties = response.getProperties();
        responseProperties.setReplyToGroupId(message.getReplyToGroupId());
        final String replyTo = message.getReplyTo();
        if (replyTo != null) {
            responseProperties.setReplyTo(new AmqpAddress(message.getReplyTo()));
        }
        final Object messageId = message.getMessageId();
        if (messageId != null) {
            responseProperties.setMessageId(new AmqpMessageId(messageId.toString()));
        }

        responseProperties.setContentType(message.getContentType());
        final Object correlationId = message.getCorrelationId();
        if (correlationId != null) {
            responseProperties.setCorrelationId(new AmqpMessageId(correlationId.toString()));
        }

        final Properties amqpProperties = message.getProperties();
        if (amqpProperties != null) {
            final String to = amqpProperties.getTo();
            if (to != null) {
                responseProperties.setTo(new AmqpAddress(amqpProperties.getTo()));
            }

            if (amqpProperties.getAbsoluteExpiryTime() != null) {
                responseProperties.setAbsoluteExpiryTime(amqpProperties.getAbsoluteExpiryTime().toInstant()
                    .atOffset(ZoneOffset.UTC));
            }
            if (amqpProperties.getCreationTime() != null) {
                responseProperties.setCreationTime(amqpProperties.getCreationTime().toInstant()
                    .atOffset(ZoneOffset.UTC));
            }
        }

        responseProperties.setSubject(message.getSubject());
        responseProperties.setGroupId(message.getGroupId());
        responseProperties.setContentEncoding(message.getContentEncoding());
        responseProperties.setGroupSequence(message.getGroupSequence());
        responseProperties.setUserId(message.getUserId());

        // DeliveryAnnotations
        final DeliveryAnnotations deliveryAnnotations = message.getDeliveryAnnotations();
        if (deliveryAnnotations != null) {
            setValues(deliveryAnnotations.getValue(), response.getDeliveryAnnotations());
        }

        // Message Annotations
        final MessageAnnotations messageAnnotations = message.getMessageAnnotations();
        if (messageAnnotations != null) {
            setValues(messageAnnotations.getValue(), response.getMessageAnnotations());
        }

        return response;
    }

    /**
     * Converts a proton-j delivery state to one supported by azure-core-amqp.
     *
     * @param deliveryState Delivery state to convert.
     *
     * @return The corresponding delivery outcome or null if parameter was null.
     *
     * @throws IllegalArgumentException if {@code deliveryState} type but there is no transactional state associated
     *     or transaction id. If {@code deliveryState} is declared but there is no transaction id or the type is not
     *     {@link Declared}.
     * @throws UnsupportedOperationException If the {@link DeliveryStateType} is unknown.
     */
    static DeliveryOutcome toDeliveryOutcome(org.apache.qpid.proton.amqp.transport.DeliveryState deliveryState) {
        if (deliveryState == null) {
            return null;
        }

        switch (deliveryState.getType()) {
            case Accepted:
                return new DeliveryOutcome(DeliveryState.ACCEPTED);
            case Modified:
                if (!(deliveryState instanceof Modified)) {
                    return new ModifiedDeliveryOutcome();
                }

                return toDeliveryOutcome((Modified) deliveryState);
            case Received:
                if (!(deliveryState instanceof Received)) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Received delivery state should have a Received state."));
                }

                final Received received = (Received) deliveryState;
                if (received.getSectionNumber() == null || received.getSectionOffset() == null) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Received delivery state does not have any offset or section number. " + received));
                }

                return new ReceivedDeliveryOutcome(received.getSectionNumber().intValue(),
                    received.getSectionOffset().longValue());
            case Rejected:
                if (!(deliveryState instanceof Rejected)) {
                    return new DeliveryOutcome(DeliveryState.REJECTED);
                }

                return toDeliveryOutcome((Rejected) deliveryState);
            case Released:
                return new DeliveryOutcome(DeliveryState.RELEASED);
            case Declared:
                if (!(deliveryState instanceof Declared)) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Declared delivery type should have a declared outcome"));
                }
                return toDeliveryOutcome((Declared) deliveryState);
            case Transactional:
                if (!(deliveryState instanceof TransactionalState)) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Transactional delivery type should have a TransactionalState outcome."));
                }

                final TransactionalState transactionalState = (TransactionalState) deliveryState;
                if (transactionalState.getTxnId() == null) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Transactional delivery states should have an associated transaction id."));
                }

                final AmqpTransaction transaction = new AmqpTransaction(transactionalState.getTxnId().asByteBuffer());
                final DeliveryOutcome outcome = toDeliveryOutcome(transactionalState.getOutcome());
                return new TransactionalDeliveryOutcome(transaction).setOutcome(outcome);
            default:
                throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
                    "Delivery state not supported: " + deliveryState.getType()));
        }
    }

    /**
     * Converts from a proton-j outcome to its corresponding {@link DeliveryOutcome}.
     *
     * @param outcome Outcome to convert.
     *
     * @return Corresponding {@link DeliveryOutcome} or null if parameter was null.
     *
     * @throws UnsupportedOperationException If the type of {@link Outcome} is unknown.
     */
    static DeliveryOutcome toDeliveryOutcome(Outcome outcome) {
        if (outcome == null) {
            return null;
        }

        if (outcome instanceof Accepted) {
            return new DeliveryOutcome(DeliveryState.ACCEPTED);
        } else if (outcome instanceof Modified) {
            return toDeliveryOutcome((Modified) outcome);
        } else if (outcome instanceof Rejected) {
            return toDeliveryOutcome((Rejected) outcome);
        } else if (outcome instanceof Released) {
            return new DeliveryOutcome(DeliveryState.RELEASED);
        } else if (outcome instanceof Declared) {
            return toDeliveryOutcome((Declared) outcome);
        } else {
            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
                "Outcome is not known: " + outcome));
        }
    }

    /**
     * Converts from a delivery outcome to its corresponding proton-j delivery state.
     *
     * @param deliveryOutcome Outcome to convert. {@code null} if the outcome is null.
     *
     * @return Proton-j delivery state.
     *
     * @throws IllegalArgumentException if deliveryState is {@link DeliveryState#RECEIVED} but its {@code
     *     deliveryOutcome} is not {@link ReceivedDeliveryOutcome}. If {@code deliveryOutcome} is {@link
     *     TransactionalDeliveryOutcome} but there is no transaction id.
     * @throws UnsupportedOperationException if {@code deliveryState} is unsupported.
     */
    static org.apache.qpid.proton.amqp.transport.DeliveryState toProtonJDeliveryState(DeliveryOutcome deliveryOutcome) {
        if (deliveryOutcome == null) {
            return null;
        }

        if (DeliveryState.ACCEPTED.equals(deliveryOutcome.getDeliveryState())) {
            return Accepted.getInstance();
        } else if (DeliveryState.REJECTED.equals(deliveryOutcome.getDeliveryState())) {
            return toProtonJRejected(deliveryOutcome);
        } else if (DeliveryState.RELEASED.equals(deliveryOutcome.getDeliveryState())) {
            return Released.getInstance();
        } else if (DeliveryState.MODIFIED.equals(deliveryOutcome.getDeliveryState())) {
            return toProtonJModified(deliveryOutcome);
        } else if (DeliveryState.RECEIVED.equals(deliveryOutcome.getDeliveryState())) {
            if (!(deliveryOutcome instanceof ReceivedDeliveryOutcome)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Received delivery type should be "
                    + "ReceivedDeliveryOutcome. Actual: " + deliveryOutcome.getClass()));
            }

            final ReceivedDeliveryOutcome receivedDeliveryOutcome = (ReceivedDeliveryOutcome) deliveryOutcome;
            final Received received = new Received();

            received.setSectionNumber(UnsignedInteger.valueOf(receivedDeliveryOutcome.getSectionNumber()));
            received.setSectionOffset(UnsignedLong.valueOf(receivedDeliveryOutcome.getSectionOffset()));
            return received;
        } else if (deliveryOutcome instanceof TransactionalDeliveryOutcome) {
            final TransactionalDeliveryOutcome transaction = ((TransactionalDeliveryOutcome) deliveryOutcome);
            final TransactionalState state = new TransactionalState();
            if (transaction.getTransactionId() == null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Transactional deliveries require an id."));
            }

            final Binary binary = Objects.requireNonNull(Binary.create(transaction.getTransactionId()),
                "Transaction Ids are required for a transaction.");

            state.setOutcome(toProtonJOutcome(transaction.getOutcome()));
            state.setTxnId(binary);
            return state;
        } else {
            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
                "Outcome could not be translated to a proton-j delivery outcome:" + deliveryOutcome.getDeliveryState()));
        }
    }

    /**
     * Converts from delivery outcome to its corresponding proton-j outcome.
     *
     * @param deliveryOutcome Delivery outcome.
     *
     * @return Corresponding proton-j outcome.
     *
     * @throws UnsupportedOperationException when an unsupported delivery state is passed such as {@link
     *     DeliveryState#RECEIVED};
     */
    static Outcome toProtonJOutcome(DeliveryOutcome deliveryOutcome) {
        if (deliveryOutcome == null) {
            return null;
        }

        if (DeliveryState.ACCEPTED.equals(deliveryOutcome.getDeliveryState())) {
            return Accepted.getInstance();
        } else if (DeliveryState.REJECTED.equals(deliveryOutcome.getDeliveryState())) {
            return toProtonJRejected(deliveryOutcome);
        } else if (DeliveryState.RELEASED.equals(deliveryOutcome.getDeliveryState())) {
            return Released.getInstance();
        } else if (DeliveryState.MODIFIED.equals(deliveryOutcome.getDeliveryState())) {
            return toProtonJModified(deliveryOutcome);
        } else {
            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
                "DeliveryOutcome cannot be converted to proton-j outcome: " + deliveryOutcome.getDeliveryState()));
        }
    }

    private static Modified toProtonJModified(DeliveryOutcome outcome) {
        final Modified modified = new Modified();

        if (!(outcome instanceof ModifiedDeliveryOutcome)) {
            return modified;
        }

        final ModifiedDeliveryOutcome modifiedDeliveryOutcome = (ModifiedDeliveryOutcome) outcome;
        final Map<Symbol, Object> annotations = convert(modifiedDeliveryOutcome.getMessageAnnotations());

        modified.setMessageAnnotations(annotations);
        modified.setUndeliverableHere(modifiedDeliveryOutcome.isUndeliverableHere());
        modified.setDeliveryFailed(modifiedDeliveryOutcome.isDeliveryFailed());

        return modified;
    }

    private static Rejected toProtonJRejected(DeliveryOutcome outcome) {
        if (!(outcome instanceof RejectedDeliveryOutcome)) {
            return new Rejected();
        }
        final Rejected rejected = new Rejected();

        final RejectedDeliveryOutcome rejectedDeliveryOutcome = (RejectedDeliveryOutcome) outcome;
        final AmqpErrorCondition errorCondition = rejectedDeliveryOutcome.getErrorCondition();
        if (errorCondition == null) {
            return rejected;
        }


        final ErrorCondition condition = new ErrorCondition(
            Symbol.getSymbol(errorCondition.getErrorCondition()), errorCondition.toString());

        condition.setInfo(convert(rejectedDeliveryOutcome.getErrorInfo()));

        rejected.setError(condition);
        return rejected;
    }

    private static DeliveryOutcome toDeliveryOutcome(Modified modified) {
        final ModifiedDeliveryOutcome modifiedOutcome = new ModifiedDeliveryOutcome();

        if (modified.getDeliveryFailed() != null) {
            modifiedOutcome.setDeliveryFailed(modified.getDeliveryFailed());
        }

        if (modified.getUndeliverableHere() != null) {
            modifiedOutcome.setUndeliverableHere(modified.getUndeliverableHere());
        }

        return modifiedOutcome.setMessageAnnotations(convertMap(modified.getMessageAnnotations()));
    }

    private static DeliveryOutcome toDeliveryOutcome(Rejected rejected) {
        final ErrorCondition rejectedError = rejected.getError();

        if (rejectedError == null || rejectedError.getCondition() == null) {
            return new DeliveryOutcome(DeliveryState.REJECTED);
        }

        AmqpErrorCondition errorCondition =
            AmqpErrorCondition.fromString(rejectedError.getCondition().toString());
        if (errorCondition == null) {
            LOGGER.warning("Error condition is unknown: {}", rejected.getError());
            errorCondition = AmqpErrorCondition.INTERNAL_ERROR;
        }

        return new RejectedDeliveryOutcome(errorCondition)
            .setErrorInfo(convertMap(rejectedError.getInfo()));
    }

    private static DeliveryOutcome toDeliveryOutcome(Declared declared) {
        if (declared.getTxnId() == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Declared delivery states should have an associated transaction id."));
        }

        return new TransactionalDeliveryOutcome(new AmqpTransaction(declared.getTxnId().asByteBuffer()));
    }

    /**
     * Converts from the "raw" map type exposed by proton-j (which is backed by a Symbol, Object to a generic map.
     *
     * @param map the map to use.
     *
     * @return A corresponding map.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<String, Object> convertMap(Map map) {
        // proton-j only exposes "Map" even though the underlying data structure is this.
        final Map<String, Object> outcomeMessageAnnotations = new HashMap<>();
        setValues(map, outcomeMessageAnnotations);

        return outcomeMessageAnnotations;
    }

    private static void setValues(Map<Symbol, Object> sourceMap, Map<String, Object> targetMap) {
        if (sourceMap == null) {
            return;
        }

        for (Map.Entry<Symbol, Object> entry : sourceMap.entrySet()) {
            targetMap.put(entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * Converts a map from it's string keys to use {@link Symbol}.
     *
     * @param sourceMap Source map.
     *
     * @return A map with corresponding keys as symbols.
     */
    private static Map<Symbol, Object> convert(Map<String, Object> sourceMap) {
        if (sourceMap == null) {
            return null;
        }

        return sourceMap.entrySet().stream()
            .collect(HashMap::new,
                (existing, entry) -> existing.put(Symbol.valueOf(entry.getKey()), entry.getValue()),
                (HashMap::putAll));
    }

    /**
     * Private constructor.
     */
    private MessageUtils() {
    }
}
