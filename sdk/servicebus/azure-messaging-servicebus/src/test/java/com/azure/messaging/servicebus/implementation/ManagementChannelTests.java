// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.ChannelCacheWrapper;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusErrorSource;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusExceptionTestHelper;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.implementation.ManagementConstants.ASSOCIATED_LINK_NAME_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.DEADLETTER_DESCRIPTION_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.DEADLETTER_REASON_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.DISPOSITION_STATUS_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.LOCK_TOKENS_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.MANAGEMENT_OPERATION_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.OPERATION_GET_SESSION_STATE;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.OPERATION_RENEW_SESSION_LOCK;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.OPERATION_SET_SESSION_STATE;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.OPERATION_UPDATE_DISPOSITION;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.PROPERTIES_TO_MODIFY_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ManagementChannel}.
 */
class ManagementChannelTests {
    private static final String STATUS_CODE_KEY = "status-code";
    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";
    private static final String LINK_NAME = "a-link-name";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final ClientLogger LOGGER = new ClientLogger(ManagementChannelTests.class);

    // Mocked response values from the RequestResponseChannel.
    private final Message responseMessage = Proton.message();
    private final Map<String, Object> applicationProperties = new HashMap<>();
    private AmqpResponseCode authorizationResponseCode;

    private ManagementChannel managementChannel;

    // Get rules message with a default rule and two customized rules, one is correlation rule, the another one is sql
    // rule.
    private static final byte[] THREE_RULE_MESSAGE = new byte[] {
        0, 83, 115, -64, 15, 13, 64, 64, 64, 64, 64, 83, 1, 64, 64, 64, 64, 64, 64, 64, 0, 83, 116, -63, 83, 8, -95, 10,
        115, 116, 97, 116, 117, 115, 67, 111, 100, 101, 113, 0, 0, 0, -56, -95, 14, 101, 114, 114, 111, 114, 67, 111,
        110, 100, 105, 116, 105, 111, 110, 64, -95, 17, 115, 116, 97, 116, 117, 115, 68, 101, 115, 99, 114, 105, 112,
        116, 105, 111, 110, 64, -95, 25, 99, 111, 109, 46, 109, 105, 99, 114, 111, 115, 111, 102, 116, 58, 116, 114, 97,
        99, 107, 105, 110, 103, 45, 105, 100, 64, 0, 83, 119, -47, 0, 0, 1, 48, 0, 0, 0, 2, -95, 5, 114, 117, 108, 101,
        115, -48, 0, 0, 1, 32, 0, 0, 0, 3, -63, 73, 2, -95, 16, 114, 117, 108, 101, 45, 100, 101, 115, 99, 114, 105,
        112, 116, 105, 111, 110, 0, -128, 0, 0, 1, 55, 0, 0, 0, 4, -64, 42, 4, 0, -128, 0, 0, 0, 19, 112, 0, 0, 7, 69,
        0, -128, 0, 0, 1, 55, 0, 0, 0, 5, 69, -95, 8, 36, 68, 101, 102, 97, 117, 108, 116, -125, 0, 0, 1, -126, -112,
        -66, -80, 119, -63, 117, 2, -95, 16, 114, 117, 108, 101, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111,
        110, 0, -128, 0, 0, 1, 55, 0, 0, 0, 4, -64, 86, 4, 0, -128, 0, 0, 0, 19, 112, 0, 0, 9, -64, 29, 9, 64, 64, 64,
        -95, 3, 102, 111, 111, 64, 64, 64, 64, -63, 14, 2, -95, 3, 98, 97, 114, -95, 6, 114, 97, 110, 100, 111, 109, 0,
        -128, 0, 0, 1, 55, 0, 0, 0, 5, 69, -95, 22, 110, 101, 119, 45, 99, 111, 114, 114, 101, 108, 97, 116, 105, 111,
        110, 45, 102, 105, 108, 116, 101, 114, -125, 0, 0, 1, -126, -111, 22, 53, -99, -63, 88, 2, -95, 16, 114, 117,
        108, 101, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, 0, -128, 0, 0, 1, 55, 0, 0, 0, 4, -64, 57,
        4, 0, -128, 0, 0, 0, 19, 112, 0, 0, 6, -64, 8, 2, -95, 3, 49, 61, 49, 84, 20, 0, -128, 0, 0, 1, 55, 0, 0, 0, 5,
        69, -95, 14, 110, 101, 119, 45, 115, 113, 108, 45, 102, 105, 108, 116, 101, 114, -125, 0, 0, 1, -126, -111, 23,
        29, 49
    };

    @Mock
    private TokenManager tokenManager;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private RequestResponseChannel requestResponseChannel;
    @Captor
    private ArgumentCaptor<Message> messageCaptor;
    @Captor
    private ArgumentCaptor<DeliveryState> amqpDeliveryStateCaptor;

    @BeforeEach
    void setup(TestInfo testInfo) {
        LOGGER.info("[{}] Setting up.", testInfo.getDisplayName());

        MockitoAnnotations.initMocks(this);

        authorizationResponseCode = AmqpResponseCode.OK;

        Flux<AmqpResponseCode> results = Flux.create(sink -> sink.onRequest(requested -> {
            LOGGER.info("Requested {} authorization results.", requested);
            sink.next(authorizationResponseCode);
        }));

        applicationProperties.put(STATUS_CODE_KEY, AmqpResponseCode.OK.getValue());
        responseMessage.setApplicationProperties(new ApplicationProperties(applicationProperties));

        when(tokenManager.authorize()).thenReturn(Mono.just(1000L));
        when(tokenManager.getAuthorizationResults()).thenReturn(results);

        when(requestResponseChannel.sendWithAck(any(Message.class))).thenReturn(Mono.just(responseMessage));
        when(requestResponseChannel.sendWithAck(any(Message.class), isNull())).thenReturn(Mono.just(responseMessage));

        ChannelCacheWrapper channelCache = new ChannelCacheWrapper(Mono.just(requestResponseChannel));
        managementChannel = new ManagementChannel(channelCache, NAMESPACE, ENTITY_PATH,
            tokenManager, messageSerializer, TIMEOUT);
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        LOGGER.info("[{}] Tearing down.", testInfo.getDisplayName());
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that we can set the session state.
     */
    @MethodSource("sessionStates")
    @ParameterizedTest
    void setsSessionState(byte[] state) {
        // Arrange
        final String sessionId = "A session-id";

        // Act
        StepVerifier.create(managementChannel.setSessionState(sessionId, state, LINK_NAME))
            .expectComplete()
            .verify(TIMEOUT);

        // Assert
        verify(requestResponseChannel).sendWithAck(messageCaptor.capture(), isNull());

        // Assert message body
        final Message sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getBody() instanceof AmqpValue);

        final AmqpValue amqpValue = (AmqpValue) sentMessage.getBody();

        @SuppressWarnings("unchecked") final Map<String, Object> hashMap = (Map<String, Object>) amqpValue.getValue();
        assertEquals(sessionId, hashMap.get(ManagementConstants.SESSION_ID));

        final Object addedState = hashMap.get(ManagementConstants.SESSION_STATE);
        if (state == null) {
            assertNull(addedState);
        } else {
            assertTrue(addedState instanceof Binary);
            assertEquals(state, ((Binary) addedState).getArray());
        }

        // Assert application properties
        final Map<String, Object> applicationProperties = sentMessage.getApplicationProperties().getValue();
        assertEquals(OPERATION_SET_SESSION_STATE, applicationProperties.get(MANAGEMENT_OPERATION_KEY));
    }

    /**
     * Verifies that it errors when invalid session ids are passed in.
     */
    @Test
    void setSessionStateNoSessionId() {
        // Arrange
        final byte[] sessionState = new byte[]{10, 11, 8, 88, 15};

        // Act & Assert
        StepVerifier.create(managementChannel.setSessionState(null, sessionState, LINK_NAME))
            .expectError(NullPointerException.class)
            .verify(TIMEOUT);

        StepVerifier.create(managementChannel.setSessionState("", sessionState, LINK_NAME))
            .expectError(IllegalArgumentException.class)
            .verify(TIMEOUT);

        verifyNoInteractions(requestResponseChannel);
    }

    /**
     * Verifies that we can get the session state.
     */
    @Test
    void getSessionState() {
        // Arrange
        final byte[] sessionState = new byte[]{10, 11, 8, 88, 15};
        final String sessionId = "A session-id";

        final Map<String, Object> responseBody = new HashMap<>();
        final Binary sessionStateBinary = new Binary(sessionState);
        responseBody.put(ManagementConstants.SESSION_STATE, sessionStateBinary);
        responseMessage.setBody(new AmqpValue(responseBody));

        // Act & Assert
        StepVerifier.create(managementChannel.getSessionState(sessionId, LINK_NAME))
            .expectNext(sessionState)
            .expectComplete()
            .verify(TIMEOUT);

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture(), isNull());

        final Message sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getBody() instanceof AmqpValue);

        final AmqpValue amqpValue = (AmqpValue) sentMessage.getBody();

        @SuppressWarnings("unchecked") final Map<String, Object> hashMap = (Map<String, Object>) amqpValue.getValue();
        assertEquals(sessionId, hashMap.get(ManagementConstants.SESSION_ID));

        // Assert application properties
        final Map<String, Object> applicationProperties = sentMessage.getApplicationProperties().getValue();
        assertEquals(OPERATION_GET_SESSION_STATE, applicationProperties.get(MANAGEMENT_OPERATION_KEY));
    }

    /**
     * Verifies that it errors when invalid sessionId's are passed in.
     */
    @Test
    void getSessionStateNoSessionId() {
        // Act & Assert
        StepVerifier.create(managementChannel.getSessionState(null, LINK_NAME))
            .expectError(NullPointerException.class)
            .verify(TIMEOUT);

        StepVerifier.create(managementChannel.getSessionState("", LINK_NAME))
            .expectError(IllegalArgumentException.class)
            .verify(TIMEOUT);

        verifyNoInteractions(requestResponseChannel);
    }

    /**
     * Verifies that a null session state completes with an empty mono. Null is not allowed as an "onNext" value.
     */
    @Test
    void getSessionStateNull() {
        // Arrange
        final String sessionId = "A session-id";

        final Map<String, Object> responseBody = new HashMap<>();
        responseBody.put(ManagementConstants.SESSION_STATE, null);
        responseMessage.setBody(new AmqpValue(responseBody));

        // Act & Assert
        StepVerifier.create(managementChannel.getSessionState(sessionId, LINK_NAME))
            .expectComplete()
            .verify(TIMEOUT);

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture(), isNull());

        final Message sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getBody() instanceof AmqpValue);

        final AmqpValue amqpValue = (AmqpValue) sentMessage.getBody();

        @SuppressWarnings("unchecked") final Map<String, Object> hashMap = (Map<String, Object>) amqpValue.getValue();
        assertEquals(sessionId, hashMap.get(ManagementConstants.SESSION_ID));

        // Assert application properties
        final Map<String, Object> applicationProperties = sentMessage.getApplicationProperties().getValue();
        assertEquals(OPERATION_GET_SESSION_STATE, applicationProperties.get(MANAGEMENT_OPERATION_KEY));
    }

    /**
     * Verifies that we can renew the session lock.
     */
    @Test
    void renewSessionLock() {
        // Arrange
        final Instant instant = Instant.ofEpochSecond(1587997482L);
        final Date expirationDate = Date.from(instant);
        final String sessionId = "A session-id";

        final Map<String, Object> responseBody = new HashMap<>();
        responseBody.put(ManagementConstants.EXPIRATION, expirationDate);
        responseMessage.setBody(new AmqpValue(responseBody));

        // Act & Assert
        StepVerifier.create(managementChannel.renewSessionLock(sessionId, LINK_NAME))
            .assertNext(expiration -> assertEquals(instant.atOffset(ZoneOffset.UTC), expiration))
            .expectComplete()
            .verify(TIMEOUT);

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture(), isNull());

        final Message sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getBody() instanceof AmqpValue);

        final AmqpValue amqpValue = (AmqpValue) sentMessage.getBody();

        @SuppressWarnings("unchecked") final Map<String, Object> hashMap = (Map<String, Object>) amqpValue.getValue();
        assertEquals(sessionId, hashMap.get(ManagementConstants.SESSION_ID));

        // Assert application properties
        final Map<String, Object> applicationProperties = sentMessage.getApplicationProperties().getValue();
        assertEquals(OPERATION_RENEW_SESSION_LOCK, applicationProperties.get(MANAGEMENT_OPERATION_KEY));
    }

    /**
     * Verifies that it errors when invalid session ids are passed in.
     */
    @Test
    void renewSessionLockNoSessionId() {
        // Act & Assert
        StepVerifier.create(managementChannel.renewSessionLock(null, LINK_NAME))
            .expectError(NullPointerException.class)
            .verify(TIMEOUT);

        StepVerifier.create(managementChannel.renewSessionLock("", LINK_NAME))
            .expectError(IllegalArgumentException.class)
            .verify(TIMEOUT);

        verifyNoInteractions(requestResponseChannel);
    }

    /**
     * Verifies that the correct properties are sent with the request and response is processed correctly.
     */
    @MethodSource
    @ParameterizedTest
    void updateDisposition(String sessionId, String associatedLinkName) {
        // Arrange
        final Map<String, Object> propertiesToModify = new HashMap<>();
        propertiesToModify.put("test-key", "test-value");
        final DeadLetterOptions options = new DeadLetterOptions()
            .setDeadLetterErrorDescription("dlq-description")
            .setDeadLetterReason("dlq-reason")
            .setPropertiesToModify(propertiesToModify);

        final boolean isSessioned = !CoreUtils.isNullOrEmpty(sessionId);
        final boolean hasName = !CoreUtils.isNullOrEmpty(associatedLinkName);
        final UUID lockToken = UUID.randomUUID();

        // Act & Assert
        StepVerifier.create(managementChannel.updateDisposition(lockToken.toString(), DispositionStatus.SUSPENDED,
            options.getDeadLetterReason(), options.getDeadLetterErrorDescription(), options.getPropertiesToModify(),
            sessionId, associatedLinkName, null))
            .expectComplete()
            .verify(TIMEOUT);

        // Verify the contents of our request to make sure the correct properties were given.
        verify(requestResponseChannel).sendWithAck(messageCaptor.capture(), isNull());

        final Message sentMessage = messageCaptor.getValue();

        // Assert AMQP body
        assertTrue(sentMessage.getBody() instanceof AmqpValue);

        final AmqpValue amqpValue = (AmqpValue) sentMessage.getBody();
        @SuppressWarnings("unchecked") final Map<String, Object> body = (Map<String, Object>) amqpValue.getValue();

        assertEquals(DispositionStatus.SUSPENDED.getValue(), body.get(DISPOSITION_STATUS_KEY));

        final UUID[] requestLockTokens = (UUID[]) body.get(LOCK_TOKENS_KEY);
        assertNotNull(requestLockTokens);
        assertEquals(1, requestLockTokens.length);
        assertEquals(lockToken, requestLockTokens[0]);

        assertEquals(options.getDeadLetterReason(), body.get(DEADLETTER_REASON_KEY));
        assertEquals(options.getDeadLetterErrorDescription(), body.get(DEADLETTER_DESCRIPTION_KEY));
        assertEquals(options.getPropertiesToModify(), body.get(PROPERTIES_TO_MODIFY_KEY));

        if (isSessioned) {
            assertEquals(sessionId, body.get(ManagementConstants.SESSION_ID));
        } else {
            assertFalse(body.containsKey(ManagementConstants.SESSION_ID));
        }

        // Assert application properties
        final Map<String, Object> applicationProperties = sentMessage.getApplicationProperties().getValue();
        assertEquals(OPERATION_UPDATE_DISPOSITION, applicationProperties.get(MANAGEMENT_OPERATION_KEY));

        if (hasName) {
            assertEquals(associatedLinkName, applicationProperties.get(ASSOCIATED_LINK_NAME_KEY));
        } else {
            assertFalse(applicationProperties.containsKey(ASSOCIATED_LINK_NAME_KEY));
        }
    }

    /**
     * Verifies that transaction-id is set properly.
     */
    @Test
    void updateDispositionWithTransaction() {
        // Arrange
        final String associatedLinkName = "associatedLinkName";
        final String txnIdString = "Transaction-ID";
        final DeadLetterOptions options = new DeadLetterOptions()
            .setDeadLetterErrorDescription("dlq-description")
            .setDeadLetterReason("dlq-reason");

        final UUID lockToken = UUID.randomUUID();
        final ServiceBusTransactionContext mockTransaction = mock(ServiceBusTransactionContext.class);
        when(mockTransaction.getTransactionId()).thenReturn(ByteBuffer.wrap(txnIdString.getBytes()));
        when(requestResponseChannel.sendWithAck(any(Message.class), any(DeliveryState.class)))
            .thenReturn(Mono.just(responseMessage));

        // Act & Assert
        StepVerifier.create(managementChannel.updateDisposition(lockToken.toString(), DispositionStatus.SUSPENDED,
            options.getDeadLetterReason(), options.getDeadLetterErrorDescription(), options.getPropertiesToModify(),
            null, associatedLinkName, mockTransaction))
            .expectComplete()
            .verify(TIMEOUT);

        // Verify the contents of our request to make sure the correct properties were given.
        verify(requestResponseChannel).sendWithAck(any(Message.class), amqpDeliveryStateCaptor.capture());

        final DeliveryState delivery = amqpDeliveryStateCaptor.getValue();
        Assertions.assertNotNull(delivery);
        Assertions.assertTrue(delivery instanceof TransactionalState);
        Assertions.assertEquals(txnIdString, ((TransactionalState) delivery).getTxnId().toString());
    }

    /**
     * Verifies that an error is emitted when user is unauthorized.
     */
    @Test
    void unauthorized() {
        // Arrange
        final String sessionId = "A session-id";
        authorizationResponseCode = AmqpResponseCode.UNAUTHORIZED;

        // Act & Assert
        StepVerifier.create(managementChannel.getSessionState(sessionId, LINK_NAME))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof ServiceBusException);
                assertEquals(ServiceBusErrorSource.MANAGEMENT, ServiceBusExceptionTestHelper.getInternalErrorSource((ServiceBusException) error));
                assertEquals(ServiceBusFailureReason.UNAUTHORIZED, ((ServiceBusException) error).getReason());
                assertFalse(((ServiceBusException) error).isTransient());
            })
            .verify(TIMEOUT);

        StepVerifier.create(managementChannel.renewMessageLock(sessionId, LINK_NAME))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ServiceBusException);
                    assertEquals(ServiceBusErrorSource.MANAGEMENT, ServiceBusExceptionTestHelper.getInternalErrorSource((ServiceBusException) error));
                    assertEquals(ServiceBusFailureReason.UNAUTHORIZED, ((ServiceBusException) error).getReason());
                    assertFalse(((ServiceBusException) error).isTransient());
                })
                .verify(TIMEOUT);

        StepVerifier.create(managementChannel.renewMessageLock(sessionId, LINK_NAME))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ServiceBusException);
                    assertEquals(ServiceBusErrorSource.MANAGEMENT, ServiceBusExceptionTestHelper.getInternalErrorSource((ServiceBusException) error));
                    assertEquals(ServiceBusFailureReason.UNAUTHORIZED, ((ServiceBusException) error).getReason());
                    assertFalse(((ServiceBusException) error).isTransient());
                })
                .verify(TIMEOUT);

        StepVerifier.create(managementChannel.renewSessionLock(sessionId, LINK_NAME))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ServiceBusException);
                    assertEquals(ServiceBusErrorSource.MANAGEMENT, ServiceBusExceptionTestHelper.getInternalErrorSource((ServiceBusException) error));
                    assertEquals(ServiceBusFailureReason.UNAUTHORIZED, ((ServiceBusException) error).getReason());
                    assertFalse(((ServiceBusException) error).isTransient());
                })
                .verify(TIMEOUT);

        StepVerifier.create(managementChannel.setSessionState(sessionId, new byte[0], LINK_NAME))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ServiceBusException);
                    assertEquals(ServiceBusErrorSource.MANAGEMENT, ServiceBusExceptionTestHelper.getInternalErrorSource((ServiceBusException) error));
                    assertEquals(ServiceBusFailureReason.UNAUTHORIZED, ((ServiceBusException) error).getReason());
                    assertFalse(((ServiceBusException) error).isTransient());
                })
                .verify(TIMEOUT);

        StepVerifier.create(managementChannel.schedule(new ArrayList<>(), OffsetDateTime.now(), 1, LINK_NAME, null))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ServiceBusException);
                    assertEquals(ServiceBusErrorSource.MANAGEMENT, ServiceBusExceptionTestHelper.getInternalErrorSource((ServiceBusException) error));
                    assertEquals(ServiceBusFailureReason.UNAUTHORIZED, ((ServiceBusException) error).getReason());
                    assertFalse(((ServiceBusException) error).isTransient());
                })
                .verify(TIMEOUT);

        StepVerifier.create(managementChannel.updateDisposition(UUID.randomUUID().toString(),
                DispositionStatus.ABANDONED, "", "",
                null, sessionId, LINK_NAME, null))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ServiceBusException);
                    assertEquals(ServiceBusErrorSource.MANAGEMENT, ServiceBusExceptionTestHelper.getInternalErrorSource((ServiceBusException) error));
                    assertEquals(ServiceBusFailureReason.UNAUTHORIZED, ((ServiceBusException) error).getReason());
                    assertFalse(((ServiceBusException) error).isTransient());
                })
                .verify(TIMEOUT);
    }

    @Test
    void getDeferredMessagesWithEmptyArrayReturnsAnEmptyFlux() {
        // Arrange, act, assert
        StepVerifier.create(managementChannel.receiveDeferredMessages(ServiceBusReceiveMode.PEEK_LOCK, null, null, new ArrayList<>()))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    void getDeferredMessagesWithNullThrows() {
        // Arrange, act, assert
        StepVerifier.create(managementChannel.receiveDeferredMessages(ServiceBusReceiveMode.PEEK_LOCK, null, null, null))
            .expectError(NullPointerException.class)
            .verify(TIMEOUT);
    }

    @Test
    void cancelScheduledMessagesWithEmptyIterable() {
        // Arrange, act, assert
        StepVerifier.create(managementChannel.cancelScheduledMessages(new ArrayList<>(), null))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    void createRule() {
        // Arrange
        final String ruleName = "foo-bar";
        CreateRuleOptions options = new CreateRuleOptions();

        // Act & Assert
        StepVerifier.create(managementChannel.createRule(ruleName, options))
            .expectComplete()
            .verify(TIMEOUT);

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture(), isNull());

        // Assert message body
        final Message message = messageCaptor.getValue();
        assertTrue(message.getBody() instanceof AmqpValue);
        final AmqpValue amqpValue = (AmqpValue) message.getBody();

        // Assert request body value.
        @SuppressWarnings("unchecked") final Map<String, Object> hashMap = (Map<String, Object>) amqpValue.getValue();
        assertEquals(ruleName, hashMap.get(ManagementConstants.RULE_NAME));

        @SuppressWarnings("unchecked") final Map<String, Object> ruleHashMap = (Map<String, Object>) hashMap.get(ManagementConstants.RULE_DESCRIPTION);
        assertEquals(ruleName, ruleHashMap.get(ManagementConstants.RULE_NAME));

        @SuppressWarnings("unchecked") final Map<String, Object> ruleFilterMap = (Map<String, Object>) ruleHashMap.get(ManagementConstants.SQL_RULE_FILTER);
        assertEquals(ruleFilterMap.get(ManagementConstants.EXPRESSION), ((SqlRuleFilter) options.getFilter()).getSqlExpression());
    }

    @Test
    void getRules() {
        // Arrange, act, assert
        final  Message message = Proton.message();
        message.decode(THREE_RULE_MESSAGE, 0, THREE_RULE_MESSAGE.length);
        responseMessage.setBody(message.getBody());

        // Assert response message content.
        StepVerifier.create(managementChannel.listRules())
            .assertNext(ruleProperties -> {
                assertEquals("$Default", ruleProperties.getName());
                assertEquals(ruleProperties.getFilter(), new TrueRuleFilter());
            })
            .assertNext(ruleProperties -> {
                assertEquals("new-correlation-filter", ruleProperties.getName());
                assertTrue(ruleProperties.getFilter() instanceof CorrelationRuleFilter);
                CorrelationRuleFilter filter = (CorrelationRuleFilter) ruleProperties.getFilter();
                assertEquals(filter.getReplyTo(), "foo");
                assertEquals(filter.getProperties().get("bar"), "random");
            })
            .assertNext(ruleProperties -> {
                assertEquals("new-sql-filter", ruleProperties.getName());
                assertEquals(ruleProperties.getFilter(), new TrueRuleFilter());
            })
            .expectComplete()
            .verify(TIMEOUT);

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture(), isNull());

        // Assert message body
        final Message sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getBody() instanceof AmqpValue);
        final AmqpValue amqpValue = (AmqpValue) sentMessage.getBody();

        // Assert request body value.
        @SuppressWarnings("unchecked") final Map<String, Object> hashMap = (Map<String, Object>) amqpValue.getValue();
        assertEquals(hashMap.get(ManagementConstants.SKIP), 0);
        assertEquals(hashMap.get(ManagementConstants.TOP), Integer.MAX_VALUE);
    }

    @Test
    void deleteRule() {
        // Arrange
        String ruleName = "exist-rule";

        // Act & Assert
        StepVerifier.create(managementChannel.deleteRule(ruleName))
            .expectComplete()
            .verify(TIMEOUT);

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture(), isNull());

        // Assert message body
        final Message message = messageCaptor.getValue();
        assertTrue(message.getBody() instanceof AmqpValue);
        final AmqpValue amqpValue = (AmqpValue) message.getBody();

        // Assert request body value.
        @SuppressWarnings("unchecked") final Map<String, Object> hashMap = (Map<String, Object>) amqpValue.getValue();
        assertEquals(ruleName, hashMap.get(ManagementConstants.RULE_NAME));

    }

    private static Stream<Arguments> updateDisposition() {
        return Stream.of(Arguments.of(
            "", "test-link-name",
            "", null,
            "test-session", "",
            null, "test-link-name"
        ));
    }

    private static Stream<Arguments> sessionStates() {
        // Got a warning about this being confusing because it was passed to varargs. So we cast to Object.
        final Object contents = new byte[]{10, 11, 8, 88};
        return Stream.of(
            Arguments.of(contents),
            Arguments.of((Object) null));
    }
}
