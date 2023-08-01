// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.amqp.models.DeliveryOutcome;
import com.azure.core.amqp.models.DeliveryState;
import com.azure.core.amqp.models.ModifiedDeliveryOutcome;
import com.azure.core.amqp.models.ReceivedDeliveryOutcome;
import com.azure.core.amqp.models.RejectedDeliveryOutcome;
import com.azure.core.amqp.models.TransactionalDeliveryOutcome;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedByte;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Footer;
import org.apache.qpid.proton.amqp.messaging.Header;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Received;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests utility methods in {@link MessageUtilsTest}.
 */
public class MessageUtilsTest {

    /**
     * Parameters to pass into {@link #toDeliveryOutcomeFromOutcome(Outcome, DeliveryOutcome)} and {@link
     * #toDeliveryOutcomeFromDeliveryState(org.apache.qpid.proton.amqp.transport.DeliveryState, DeliveryOutcome)}.
     * Proton-j classes inherit from two interfaces, so can be used as inputs to both tests.
     *
     * @return Stream of arguments.
     */
    public static Stream<Arguments> getProtonJOutcomesAndDeliveryStates() {
        return Stream.of(
            Arguments.of(Accepted.getInstance(), new DeliveryOutcome(DeliveryState.ACCEPTED)),
            Arguments.of(Released.getInstance(), new DeliveryOutcome(DeliveryState.RELEASED)));
    }

    /**
     * Simple arguments where the proton-j delivery state is also its outcome.
     *
     * @return A stream of arguments.
     */
    public static Stream<Arguments> getDeliveryStatesToTest() {
        return Stream.of(
            Arguments.arguments(DeliveryState.ACCEPTED, Accepted.getInstance(),
                DeliveryStateType.Accepted),
            Arguments.arguments(DeliveryState.RELEASED, Released.getInstance(),
                DeliveryStateType.Released),
            Arguments.arguments(DeliveryState.MODIFIED, new Modified(),
                DeliveryStateType.Modified),
            Arguments.arguments(DeliveryState.REJECTED, new Rejected(),
                DeliveryStateType.Rejected));
    }

    /**
     * Unsupported message bodies.
     *
     * @return Unsupported messaged bodies.
     */
    public static Stream<AmqpMessageBodyType> getUnsupportedMessageBody() {
        return Stream.of(AmqpMessageBodyType.VALUE, AmqpMessageBodyType.SEQUENCE);
    }

    /**
     * Converts from a proton-j message to an AMQP annotated message.
     */
    @Test
    public void toAmqpAnnotatedMessage() {
        final byte[] contents = "foo-bar".getBytes(StandardCharsets.UTF_8);
        final Data body = new Data(Binary.create(ByteBuffer.wrap(contents)));

        final Header header = new Header();
        header.setDurable(true);
        header.setDeliveryCount(new UnsignedInteger(17));
        header.setPriority(new UnsignedByte((byte) 2));
        header.setFirstAcquirer(false);
        header.setTtl(new UnsignedInteger(10));
        final String messageId = "Test-message-id";
        final String correlationId = "correlation-id-test";
        final byte[] userId = "baz".getBytes(StandardCharsets.UTF_8);
        final Properties properties = new Properties();

        final OffsetDateTime absoluteDate = OffsetDateTime.parse("2021-02-04T10:15:30+00:00");
        properties.setAbsoluteExpiryTime(Date.from(absoluteDate.toInstant()));
        properties.setContentEncoding(Symbol.valueOf("content-encoding-test"));
        properties.setContentType(Symbol.valueOf("content-type-test"));
        properties.setCorrelationId(correlationId);

        final OffsetDateTime creationTime = OffsetDateTime.parse("2021-02-03T10:15:30+00:00");
        properties.setCreationTime(Date.from(creationTime.toInstant()));
        properties.setGroupId("group-id-test");
        properties.setGroupSequence(new UnsignedInteger(16));
        properties.setMessageId(messageId);
        properties.setReplyToGroupId("reply-to-group-id-test");
        properties.setReplyTo("foo");
        properties.setTo("bar");
        properties.setSubject("subject-item");
        properties.setUserId(Binary.create(ByteBuffer.wrap(userId)));

        final Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("1", "one");
        applicationProperties.put("two", 2);

        final Map<Symbol, Object> deliveryAnnotations = new HashMap<>();
        deliveryAnnotations.put(Symbol.valueOf("delivery1"), 1);
        deliveryAnnotations.put(Symbol.valueOf("delivery2"), 2);

        final Map<Symbol, Object> messageAnnotations = new HashMap<>();
        messageAnnotations.put(Symbol.valueOf("something"), "else");

        final Map<Symbol, Object> footer = new HashMap<>();
        footer.put(Symbol.valueOf("1"), false);

        final Message message = Proton.message();
        message.setBody(body);
        message.setHeader(header);
        message.setProperties(properties);
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));
        message.setMessageAnnotations(new MessageAnnotations(messageAnnotations));
        message.setDeliveryAnnotations(new DeliveryAnnotations(deliveryAnnotations));
        message.setFooter(new Footer(footer));

        // Act
        final AmqpAnnotatedMessage actual = MessageUtils.toAmqpAnnotatedMessage(message);

        // Assert
        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertArrayEquals(contents, actual.getBody().getFirstData());

        assertHeader(actual.getHeader(), header);
        assertProperties(actual.getProperties(), properties);

        assertNotNull(actual.getApplicationProperties());
        assertEquals(applicationProperties.size(), actual.getApplicationProperties().size());
        applicationProperties.forEach((key, value) -> assertEquals(value, actual.getApplicationProperties().get(key)));

        assertSymbolMap(deliveryAnnotations, actual.getDeliveryAnnotations());
        assertSymbolMap(messageAnnotations, actual.getMessageAnnotations());
        assertSymbolMap(footer, actual.getFooter());
    }

    /**
     * Tests a conversion from {@link AmqpAnnotatedMessage} to proton-j Message.
     */
    @Test
    public void toProtonJMessage() {
        // Arrange
        final byte[] contents = "foo-bar".getBytes(StandardCharsets.UTF_8);
        final AmqpMessageBody body = AmqpMessageBody.fromData(contents);
        final AmqpAnnotatedMessage expected = new AmqpAnnotatedMessage(body);
        final AmqpMessageHeader header = expected.getHeader().setDurable(true)
            .setDeliveryCount(17L)
            .setPriority((short) 2)
            .setFirstAcquirer(false)
            .setTimeToLive(Duration.ofSeconds(10));
        final String messageId = "Test-message-id";
        final AmqpMessageId amqpMessageId = new AmqpMessageId(messageId);
        final AmqpMessageId correlationId = new AmqpMessageId("correlation-id-test");
        final AmqpAddress replyTo = new AmqpAddress("foo");
        final AmqpAddress to = new AmqpAddress("bar");
        final byte[] userId = "baz".getBytes(StandardCharsets.UTF_8);
        final AmqpMessageProperties properties = expected.getProperties()
            .setAbsoluteExpiryTime(OffsetDateTime.parse("2021-02-04T10:15:30+00:00"))
            .setContentEncoding("content-encoding-test")
            .setContentType("content-type-test")
            .setCorrelationId(correlationId)
            .setCreationTime(OffsetDateTime.parse("2021-02-03T10:15:30+00:00"))
            .setGroupId("group-id-test")
            .setGroupSequence(22L)
            .setMessageId(amqpMessageId)
            .setReplyToGroupId("reply-to-group-id-test")
            .setReplyTo(replyTo)
            .setTo(to)
            .setSubject("subject-item")
            .setUserId(userId);

        final Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("1", "one");
        applicationProperties.put("two", 2);

        applicationProperties.forEach((key, value) ->
            expected.getApplicationProperties().put(key, value));

        final Map<String, Object> deliveryAnnotations = new HashMap<>();
        deliveryAnnotations.put("delivery1", 1);
        deliveryAnnotations.put("delivery2", 2);

        deliveryAnnotations.forEach((key, value) -> expected.getDeliveryAnnotations().put(key, value));

        final Map<String, Object> messageAnnotations = new HashMap<>();
        messageAnnotations.put("something", "else");

        messageAnnotations.forEach((key, value) -> expected.getMessageAnnotations().put(key, value));

        final Map<String, Object> footer = new HashMap<>();
        footer.put("1", false);

        footer.forEach((key, value) -> expected.getFooter().put(key, value));

        // Act
        final Message actual = MessageUtils.toProtonJMessage(expected);

        // Assert
        assertNotNull(actual);

        assertTrue(actual.getBody() instanceof Data);

        final Data dataBody = (Data) actual.getBody();
        assertArrayEquals(body.getFirstData(), dataBody.getValue().getArray());

        assertHeader(header, actual.getHeader());
        assertProperties(properties, actual.getProperties());
    }

    /**
     * Tests the unsupported message bodies. AMQP sequence and value.
     */
    @MethodSource("getUnsupportedMessageBody")
    @ParameterizedTest
    public void toProtonJMessageUnsupportedMessageBody(AmqpMessageBodyType bodyType) {
        final AmqpMessageBody messageBody = mock(AmqpMessageBody.class);
        when(messageBody.getBodyType()).thenReturn(bodyType);

        final AmqpAnnotatedMessage message = new AmqpAnnotatedMessage(messageBody);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> MessageUtils.toProtonJMessage(message));
    }

    /**
     * Converts from proton-j DeliveryState to delivery outcome.
     */
    @MethodSource("getProtonJOutcomesAndDeliveryStates")
    @ParameterizedTest
    public void toDeliveryOutcomeFromDeliveryState(org.apache.qpid.proton.amqp.transport.DeliveryState deliveryState,
        DeliveryOutcome expected) {

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(deliveryState);

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getDeliveryState(), actual.getDeliveryState());
    }

    /**
     * Tests that we can convert from a Modified delivery state to the appropriate delivery outcome.
     */
    @Test
    public void toDeliveryOutcomeFromModifiedDeliveryState() {
        // Arrange
        final Map<Symbol, Object> messageAnnotations = new HashMap<>();
        messageAnnotations.put(Symbol.getSymbol("bar"), "foo");
        messageAnnotations.put(Symbol.getSymbol("baz"), 10);

        final Modified modified = new Modified();
        modified.setDeliveryFailed(true);
        modified.setMessageAnnotations(messageAnnotations);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(
            (org.apache.qpid.proton.amqp.transport.DeliveryState) modified);

        // Assert
        assertTrue(actual instanceof ModifiedDeliveryOutcome);
        assertModified((ModifiedDeliveryOutcome) actual, modified);
    }

    /**
     * Tests that we can convert from Modified delivery state type to the appropriate delivery outcome. The difference
     * is that this does not use the {@link Modified} class.
     */
    @Test
    public void toDeliveryOutcomeFromModifiedDeliveryStateNotSameClass() {
        // Arrange
        final org.apache.qpid.proton.amqp.transport.DeliveryState state =
            mock(org.apache.qpid.proton.amqp.transport.DeliveryState.class);

        when(state.getType()).thenReturn(DeliveryStateType.Modified);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(state);

        // Assert
        assertTrue(actual instanceof ModifiedDeliveryOutcome);
        assertEquals(DeliveryState.MODIFIED, actual.getDeliveryState());
    }

    /**
     * Tests that we can convert from a Rejected delivery state to the appropriate delivery outcome.
     */
    @Test
    public void toDeliveryOutcomeFromRejectedDeliveryState() {
        // Arrange
        final Map<Symbol, Object> errorInfo = new HashMap<>();
        errorInfo.put(Symbol.getSymbol("bar"), "foo");
        errorInfo.put(Symbol.getSymbol("baz"), 10);

        final AmqpErrorCondition error = AmqpErrorCondition.INTERNAL_ERROR;
        final String errorDescription = "test: " + error.getErrorCondition();

        final ErrorCondition errorCondition = new ErrorCondition(Symbol.getSymbol(error.getErrorCondition()),
            errorDescription);
        errorCondition.setInfo(errorInfo);

        final Rejected rejected = new Rejected();
        rejected.setError(errorCondition);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(
            (org.apache.qpid.proton.amqp.transport.DeliveryState) rejected);

        // Assert
        assertTrue(actual instanceof RejectedDeliveryOutcome);
        assertRejected((RejectedDeliveryOutcome) actual, rejected);
    }

    /**
     * Tests that we can convert from Rejected delivery state type to the appropriate delivery outcome. The difference
     * is that this does not use the {@link Rejected} class.
     */
    @Test
    public void toDeliveryOutcomeFromRejectedDeliveryStateNotSameClass() {
        // Arrange
        final org.apache.qpid.proton.amqp.transport.DeliveryState state =
            mock(org.apache.qpid.proton.amqp.transport.DeliveryState.class);

        when(state.getType()).thenReturn(DeliveryStateType.Rejected);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(state);

        // Assert
        assertEquals(DeliveryState.REJECTED, actual.getDeliveryState());
    }

    /**
     * Tests that we can convert from a Declared delivery state to the appropriate delivery outcome.
     */
    @Test
    public void toDeliveryOutcomeFromDeclaredDeliveryState() {
        // Arrange
        final ByteBuffer transactionId = ByteBuffer.wrap("foo".getBytes(StandardCharsets.UTF_8));
        final Binary binary = Binary.create(transactionId);
        final Declared declared = new Declared();
        declared.setTxnId(binary);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(
            (org.apache.qpid.proton.amqp.transport.DeliveryState) declared);

        // Assert
        assertTrue(actual instanceof TransactionalDeliveryOutcome);

        final TransactionalDeliveryOutcome actualOutcome = (TransactionalDeliveryOutcome) actual;
        assertEquals(DeliveryState.TRANSACTIONAL, actualOutcome.getDeliveryState());
        assertNull(actualOutcome.getOutcome());

        assertEquals(transactionId, actualOutcome.getTransactionId());
    }

    /**
     * Tests that Declared delivery state with no transaction id has an exception thrown.
     */
    @Test
    public void toDeliveryOutcomeFromDeclaredDeliveryStateNoTransactionId() {
        // Arrange
        final Declared declared = new Declared();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> MessageUtils.toDeliveryOutcome(
            (org.apache.qpid.proton.amqp.transport.DeliveryState) declared));
    }

    /**
     * Tests that an Declared delivery state type that is not also {@link Declared} throws.
     */
    @Test
    public void toDeliveryOutcomeDeclaredDeliveryStateNotSameClass() {
        // Arrange
        final org.apache.qpid.proton.amqp.transport.DeliveryState deliveryState = mock(
            org.apache.qpid.proton.amqp.transport.DeliveryState.class);

        when(deliveryState.getType()).thenReturn(DeliveryStateType.Declared);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> MessageUtils.toDeliveryOutcome(deliveryState));
    }

    /**
     * Tests that we can convert from a Transactional delivery state to the appropriate delivery outcome. The
     * transaction does not have an outcome associated with it.
     */
    @Test
    public void toDeliveryOutcomeFromTransactionalDeliveryStateNoOutcome() {
        // Arrange
        final ByteBuffer transactionId = ByteBuffer.wrap("foo".getBytes(StandardCharsets.UTF_8));
        final Binary binary = Binary.create(transactionId);
        final TransactionalState transactionalState = new TransactionalState();
        transactionalState.setTxnId(binary);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(transactionalState);

        // Assert
        assertTrue(actual instanceof TransactionalDeliveryOutcome);

        final TransactionalDeliveryOutcome actualOutcome = (TransactionalDeliveryOutcome) actual;
        assertEquals(DeliveryState.TRANSACTIONAL, actualOutcome.getDeliveryState());
        assertEquals(transactionId, actualOutcome.getTransactionId());

        assertNull(actualOutcome.getOutcome());
    }

    /**
     * Tests that we can convert from a Transactional delivery state to the appropriate delivery outcome.
     */
    @Test
    public void toDeliveryOutcomeFromTransactionalDeliveryState() {
        // Arrange
        final Map<Symbol, Object> messageAnnotations = new HashMap<>();
        messageAnnotations.put(Symbol.getSymbol("bar"), "foo");
        messageAnnotations.put(Symbol.getSymbol("baz"), 10);

        final Modified modifiedOutcome = new Modified();
        modifiedOutcome.setDeliveryFailed(false);
        modifiedOutcome.setUndeliverableHere(false);
        modifiedOutcome.setMessageAnnotations(messageAnnotations);

        final ByteBuffer transactionId = ByteBuffer.wrap("foo".getBytes(StandardCharsets.UTF_8));
        final Binary binary = Binary.create(transactionId);
        final TransactionalState transactionalState = new TransactionalState();
        transactionalState.setTxnId(binary);
        transactionalState.setOutcome(modifiedOutcome);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(transactionalState);

        // Assert
        assertTrue(actual instanceof TransactionalDeliveryOutcome);

        final TransactionalDeliveryOutcome actualOutcome = (TransactionalDeliveryOutcome) actual;
        assertEquals(DeliveryState.TRANSACTIONAL, actualOutcome.getDeliveryState());
        assertEquals(transactionId, actualOutcome.getTransactionId());

        assertNotNull(actualOutcome.getOutcome());
        assertTrue(actualOutcome.getOutcome() instanceof ModifiedDeliveryOutcome);
        assertModified((ModifiedDeliveryOutcome) actualOutcome.getOutcome(), modifiedOutcome);
    }

    /**
     * Tests that Transactional delivery state with no transaction id has an exception thrown.
     */
    @Test
    public void toDeliveryOutcomeFromTransactionalDeliveryStateNoTransactionId() {
        // Arrange
        final TransactionalState transactionalState = new TransactionalState();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> MessageUtils.toDeliveryOutcome(transactionalState));
    }

    /**
     * Tests that an Transactional delivery state type that is not also {@link TransactionalState} throws.
     */
    @Test
    public void toDeliveryOutcomeTransactionDeliveryStateNotSameClass() {
        // Arrange
        final org.apache.qpid.proton.amqp.transport.DeliveryState deliveryState = mock(
            org.apache.qpid.proton.amqp.transport.DeliveryState.class);

        when(deliveryState.getType()).thenReturn(DeliveryStateType.Transactional);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> MessageUtils.toDeliveryOutcome(deliveryState));
    }

    /**
     * Converts from proton-j outcome to delivery outcome.
     */
    @MethodSource("getProtonJOutcomesAndDeliveryStates")
    @ParameterizedTest
    public void toDeliveryOutcomeFromOutcome(Outcome outcome, DeliveryOutcome expected) {
        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome(outcome);

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getDeliveryState(), actual.getDeliveryState());
    }

    /**
     * Tests that we can convert from a Modified outcome to the appropriate delivery outcome.
     */
    @Test
    public void toDeliveryOutcomeFromModifiedOutcome() {
        // Arrange
        final Map<Symbol, Object> messageAnnotations = new HashMap<>();
        messageAnnotations.put(Symbol.getSymbol("bar"), "foo");
        messageAnnotations.put(Symbol.getSymbol("baz"), 10);

        final Modified modified = new Modified();
        modified.setDeliveryFailed(true);
        modified.setMessageAnnotations(messageAnnotations);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome((Outcome) modified);

        // Assert
        assertTrue(actual instanceof ModifiedDeliveryOutcome);

        final ModifiedDeliveryOutcome actualOutcome = (ModifiedDeliveryOutcome) actual;
        assertEquals(DeliveryState.MODIFIED, actualOutcome.getDeliveryState());
        assertEquals(modified.getUndeliverableHere(), actualOutcome.isUndeliverableHere());
        assertEquals(modified.getDeliveryFailed(), actualOutcome.isDeliveryFailed());

        assertSymbolMap(messageAnnotations, actualOutcome.getMessageAnnotations());
    }

    /**
     * Tests that we can convert from a Rejected outcome to the appropriate delivery outcome.
     */
    @Test
    public void toDeliveryOutcomeFromRejectedOutcome() {
        // Arrange
        final Map<Symbol, Object> errorInfo = new HashMap<>();
        errorInfo.put(Symbol.getSymbol("bar"), "foo");
        errorInfo.put(Symbol.getSymbol("baz"), 10);

        final AmqpErrorCondition error = AmqpErrorCondition.INTERNAL_ERROR;
        final String errorDescription = "test: " + error.getErrorCondition();

        final ErrorCondition errorCondition = new ErrorCondition(Symbol.getSymbol(error.getErrorCondition()),
            errorDescription);
        errorCondition.setInfo(errorInfo);

        final Rejected rejected = new Rejected();
        rejected.setError(errorCondition);

        // Act
        final DeliveryOutcome actual = MessageUtils.toDeliveryOutcome((Outcome) rejected);

        // Assert
        assertTrue(actual instanceof RejectedDeliveryOutcome);

        final RejectedDeliveryOutcome actualOutcome = (RejectedDeliveryOutcome) actual;
        assertEquals(DeliveryState.REJECTED, actualOutcome.getDeliveryState());
        assertEquals(error, actualOutcome.getErrorCondition());
        assertEquals(actualOutcome.getErrorCondition().getErrorCondition(),
            actualOutcome.getErrorDescription());
        assertSymbolMap(errorInfo, actualOutcome.getErrorInfo());
    }

    /**
     * Tests that an unsupported outcome will throw an exception.
     */
    @Test
    public void toDeliveryOutcomeUnsupportedOutcome() {
        // Arrange
        final Outcome outcome = mock(Outcome.class);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> MessageUtils.toDeliveryOutcome(outcome));
    }

    /**
     * Tests simple conversions where the delivery states are just their statuses.
     *
     * @param deliveryState Delivery state.
     * @param expected Expected outcome.
     * @param expectedType Expected type.
     */
    @MethodSource("getDeliveryStatesToTest")
    @ParameterizedTest
    public void toProtonJDeliveryState(DeliveryState deliveryState,
        org.apache.qpid.proton.amqp.transport.DeliveryState expected,
        DeliveryStateType expectedType) {

        // Arrange
        final DeliveryOutcome outcome = new DeliveryOutcome(deliveryState);

        // Act
        final org.apache.qpid.proton.amqp.transport.DeliveryState actual = MessageUtils.toProtonJDeliveryState(outcome);

        // Assert
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.getType(), actual.getType());

        assertEquals(expectedType, actual.getType());
    }

    /**
     * Tests the received outcome is mapped to its delivery state.
     */
    @Test
    public void toProtonJDeliveryStateReceived() {
        // Arrange
        final ReceivedDeliveryOutcome expected = new ReceivedDeliveryOutcome(10, 1053L);

        // Act
        org.apache.qpid.proton.amqp.transport.DeliveryState actual = MessageUtils.toProtonJDeliveryState(expected);

        // Assert
        assertTrue(actual instanceof Received);

        final Received received = (Received) actual;
        assertNotNull(received.getSectionNumber());
        assertNotNull(received.getSectionOffset());

        assertEquals(expected.getSectionNumber(), received.getSectionNumber().intValue());
        assertEquals(expected.getSectionOffset(), received.getSectionOffset().longValue());
    }

    /**
     * Tests that the rejected delivery state is mapped correctly.
     */
    @Test
    public void toProtonJDeliveryStateRejected() {
        // Arrange
        final AmqpErrorCondition condition = AmqpErrorCondition.ILLEGAL_STATE;
        final Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("foo", 10);
        errorInfo.put("bar", "baz");
        final RejectedDeliveryOutcome expected = new RejectedDeliveryOutcome(condition)
            .setErrorInfo(errorInfo);

        // Act
        org.apache.qpid.proton.amqp.transport.DeliveryState actual = MessageUtils.toProtonJDeliveryState(expected);

        // Assert
        assertTrue(actual instanceof Rejected);
        assertRejected(expected, (Rejected) actual);
    }

    /**
     * Tests that the modified delivery state is mapped correctly.
     */
    @Test
    public void toProtonJDeliveryStateModified() {
        // Arrange
        final Map<String, Object> annotations = new HashMap<>();
        annotations.put("foo", 10);
        annotations.put("bar", "baz");
        final ModifiedDeliveryOutcome expected = new ModifiedDeliveryOutcome()
            .setDeliveryFailed(true).setUndeliverableHere(true)
            .setMessageAnnotations(annotations);

        // Act
        final org.apache.qpid.proton.amqp.transport.DeliveryState actual = MessageUtils.toProtonJDeliveryState(expected);

        // Assert
        assertTrue(actual instanceof Modified);
        assertModified(expected, (Modified) actual);
    }

    /**
     * Tests simple conversions where the outcomes are just their statuses.
     *
     * @param deliveryState Delivery state.
     * @param expectedType Expected type.
     * @param expected Expected outcome.
     */
    @MethodSource("getDeliveryStatesToTest")
    @ParameterizedTest
    public void toProtonJOutcome(DeliveryState deliveryState, Outcome expected,
        DeliveryStateType expectedType) {
        // Arrange
        final DeliveryOutcome outcome = new DeliveryOutcome(deliveryState);

        // Act
        final Outcome actual = MessageUtils.toProtonJOutcome(outcome);

        // Assert
        assertEquals(expected.getClass(), actual.getClass());

        if (actual instanceof org.apache.qpid.proton.amqp.transport.DeliveryState) {
            assertEquals(expectedType, ((org.apache.qpid.proton.amqp.transport.DeliveryState) actual).getType());
        }
    }

    /**
     * Tests that an exception is thrown when an unsupported state is passed.
     */
    @Test
    public void toProtonJOutcomeUnsupported() {
        // Arrange
        // Received is not an outcome because it represents a partial message.
        final DeliveryOutcome outcome = new DeliveryOutcome(DeliveryState.RECEIVED);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> MessageUtils.toProtonJOutcome(outcome));
    }

    /**
     * Tests that the modified outcome is mapped correctly.
     */
    @Test
    public void toProtonJOutcomeModified() {
        // Arrange
        final Map<String, Object> annotations = new HashMap<>();
        annotations.put("foo", 10);
        annotations.put("bar", "baz");
        final ModifiedDeliveryOutcome expected = new ModifiedDeliveryOutcome()
            .setDeliveryFailed(true).setUndeliverableHere(true)
            .setMessageAnnotations(annotations);

        // Act
        final Outcome actual = MessageUtils.toProtonJOutcome(expected);

        // Assert
        assertTrue(actual instanceof Modified);
        assertModified(expected, (Modified) actual);
    }

    /**
     * Tests that the rejected outcome is mapped correctly.
     */
    @Test
    public void toProtonJOutcomeRejected() {
        // Arrange
        final AmqpErrorCondition condition = AmqpErrorCondition.RESOURCE_LIMIT_EXCEEDED;
        final Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("foo", 10);
        errorInfo.put("bar", "baz");
        final RejectedDeliveryOutcome expected = new RejectedDeliveryOutcome(condition)
            .setErrorInfo(errorInfo);

        // Act
        final Outcome actual = MessageUtils.toProtonJOutcome(expected);

        // Assert
        assertTrue(actual instanceof Rejected);
        assertRejected(expected, (Rejected) actual);
    }

    /**
     * When input is null, returns null.
     */
    @Test
    public void nullInputs() {

        assertThrows(NullPointerException.class, () -> MessageUtils.toProtonJMessage(null));
        assertThrows(NullPointerException.class, () -> MessageUtils.toAmqpAnnotatedMessage(null));

        assertNull(MessageUtils.toProtonJOutcome(null));
        assertNull(MessageUtils.toProtonJDeliveryState(null));

        assertNull(MessageUtils.toDeliveryOutcome((Outcome) null));
        assertNull(MessageUtils.toDeliveryOutcome((org.apache.qpid.proton.amqp.transport.DeliveryState) null));
    }

    private static void assertRejected(RejectedDeliveryOutcome rejected, Rejected protonJRejected) {
        if (rejected == null) {
            assertNull(protonJRejected);
            return;
        }

        assertNotNull(protonJRejected);
        final AmqpErrorCondition expectedCondition = rejected.getErrorCondition();

        assertNotNull(protonJRejected.getError());
        assertEquals(expectedCondition.getErrorCondition(), protonJRejected.getError().getCondition().toString());

        @SuppressWarnings("unchecked") final Map<Symbol, Object> actualMap = protonJRejected.getError().getInfo();
        assertSymbolMap(actualMap, rejected.getErrorInfo());
    }

    private static void assertModified(ModifiedDeliveryOutcome modified, Modified protonJModified) {
        if (modified == null) {
            assertNull(protonJModified);
            return;
        }

        assertNotNull(protonJModified);
        assertEquals(modified.isDeliveryFailed(), protonJModified.getDeliveryFailed());
        assertEquals(modified.isUndeliverableHere(), protonJModified.getUndeliverableHere());

        @SuppressWarnings("unchecked") final Map<Symbol, Object> actualMap = protonJModified.getMessageAnnotations();
        assertSymbolMap(actualMap, modified.getMessageAnnotations());
    }

    private static void assertSymbolMap(Map<Symbol, Object> symbolMap, Map<String, Object> stringMap) {
        if (symbolMap == null) {
            assertNull(stringMap);
            return;
        }

        assertNotNull(stringMap);
        assertEquals(symbolMap.size(), stringMap.size());

        symbolMap.forEach((key, value) -> {
            assertTrue(stringMap.containsKey(key.toString()));
            assertEquals(value, stringMap.get(key.toString()));
        });
    }

    private static void assertHeader(AmqpMessageHeader header, Header protonJHeader) {
        if (header == null) {
            assertNull(protonJHeader);
            return;
        }

        assertNotNull(protonJHeader);
        if (header.getDeliveryCount() == null) {
            assertNull(protonJHeader.getDeliveryCount());
        } else {
            assertNotNull(protonJHeader.getDeliveryCount());
            assertEquals(header.getDeliveryCount(), protonJHeader.getDeliveryCount().longValue());
        }

        assertEquals(header.isDurable(), protonJHeader.getDurable());
        assertEquals(header.isFirstAcquirer(), protonJHeader.getFirstAcquirer());

        if (header.getPriority() == null) {
            assertNull(protonJHeader.getPriority());
        } else {
            assertNotNull(protonJHeader.getPriority());
            assertEquals(header.getPriority(), protonJHeader.getPriority().byteValue());
        }

        if (header.getTimeToLive() == null) {
            assertNotNull(protonJHeader.getTtl());
        } else {
            assertEquals(header.getTimeToLive().toMillis(), protonJHeader.getTtl().longValue());
        }
    }

    private static void assertProperties(AmqpMessageProperties properties, Properties protonJProperties) {
        assertDate(properties.getAbsoluteExpiryTime(), protonJProperties.getAbsoluteExpiryTime());
        assertSymbol(properties.getContentEncoding(), protonJProperties.getContentEncoding());
        assertSymbol(properties.getContentType(), protonJProperties.getContentType());

        assertMessageId(properties.getCorrelationId(), protonJProperties.getCorrelationId());
        assertMessageId(properties.getMessageId(), protonJProperties.getMessageId());

        assertDate(properties.getCreationTime(), protonJProperties.getCreationTime());
        assertEquals(properties.getGroupId(), protonJProperties.getGroupId());

        assertAddress(properties.getReplyTo(), protonJProperties.getReplyTo());
        assertEquals(properties.getReplyToGroupId(), protonJProperties.getReplyToGroupId());

        assertAddress(properties.getTo(), protonJProperties.getTo());
        assertEquals(properties.getSubject(), protonJProperties.getSubject());

        if (properties.getUserId() != null) {
            assertNotNull(protonJProperties.getUserId());
            assertArrayEquals(properties.getUserId(), protonJProperties.getUserId().getArray());
        } else {
            assertNull(protonJProperties.getUserId());
        }
    }

    private static void assertMessageId(AmqpMessageId amqpMessageId, Object id) {
        if (amqpMessageId == null) {
            assertNull(id);
            return;
        }

        assertNotNull(id);
        assertEquals(amqpMessageId.toString(), id.toString());
    }

    private static void assertDate(OffsetDateTime offsetDateTime, Date date) {
        if (offsetDateTime == null) {
            assertNull(date);
        } else {
            assertNotNull(date);
            assertEquals(offsetDateTime.toInstant(), date.toInstant());
        }
    }

    private static void assertSymbol(String content, Symbol symbol) {
        if (content == null) {
            assertNull(symbol);
        } else {
            assertNotNull(symbol);
            assertEquals(content, symbol.toString());
        }
    }

    private static void assertAddress(AmqpAddress amqpAddress, String address) {
        if (amqpAddress == null) {
            assertNull(address);
        } else {
            assertNotNull(address);
            assertEquals(amqpAddress.toString(), address);
        }
    }
}
