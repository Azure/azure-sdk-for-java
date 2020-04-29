// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.implementation.ManagementConstants.MANAGEMENT_OPERATION_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.OPERATION_GET_SESSION_STATE;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.OPERATION_RENEW_SESSION_LOCK;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.OPERATION_SET_SESSION_STATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ManagementChannel}.
 */
class ManagementChannelTests {
    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";

    private final ClientLogger logger = new ClientLogger(ManagementChannelTests.class);
    private final Message responseMessage = Proton.message();

    private ManagementChannel managementChannel;

    @Mock
    private TokenManager tokenManager;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private RequestResponseChannel requestResponseChannel;
    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        logger.info("[{}] Setting up.", testInfo.getDisplayName());

        MockitoAnnotations.initMocks(this);

        Flux<AmqpResponseCode> results = Flux.create(sink -> {
            sink.onRequest(requested -> {
                logger.info("Requested {} authorization results.", requested);
                sink.next(AmqpResponseCode.OK);
            });
        });

        final Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("status-code", AmqpResponseCode.OK.getValue());
        responseMessage.setApplicationProperties(new ApplicationProperties(applicationProperties));

        when(tokenManager.authorize()).thenReturn(Mono.just(1000L));
        when(tokenManager.getAuthorizationResults()).thenReturn(results);

        when(requestResponseChannel.sendWithAck(any(Message.class))).thenReturn(Mono.just(responseMessage));
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        logger.info("[{}] Tearing down.", testInfo.getDisplayName());
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that we can set the session state.
     */
    @MethodSource("sessionStates")
    @ParameterizedTest
    void setsSessionState(byte[] state) {
        // Arrange
        final String sessionId = "A session-id";
        managementChannel = new ManagementChannel(Mono.just(requestResponseChannel), NAMESPACE, ENTITY_PATH, sessionId,
            tokenManager, messageSerializer, Duration.ofSeconds(10));

        // Act
        StepVerifier.create(managementChannel.setSessionState(state))
            .expectComplete()
            .verify();

        // Assert
        verify(requestResponseChannel).sendWithAck(messageCaptor.capture());

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
     * Verifies that we can get the session state.
     */
    @Test
    void getSessionState() {
        // Arrange
        final byte[] sessionState = new byte[]{10, 11, 8, 88, 15};
        final String sessionId = "A session-id";
        managementChannel = new ManagementChannel(Mono.just(requestResponseChannel), NAMESPACE, ENTITY_PATH, sessionId,
            tokenManager, messageSerializer, Duration.ofSeconds(10));

        final Map<String, Object> responseBody = new HashMap<>();
        final Binary sessionStateBinary = new Binary(sessionState);
        responseBody.put(ManagementConstants.SESSION_STATE, sessionStateBinary);
        responseMessage.setBody(new AmqpValue(responseBody));

        // Act & Assert
        StepVerifier.create(managementChannel.getSessionState())
            .expectNext(sessionState)
            .verifyComplete();

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture());

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
     * Verifies that a null session state completes with an empty mono. Null is not allowed as an "onNext" value.
     */
    @Test
    void getSessionStateNull() {
        // Arrange
        final String sessionId = "A session-id";
        managementChannel = new ManagementChannel(Mono.just(requestResponseChannel), NAMESPACE, ENTITY_PATH, sessionId,
            tokenManager, messageSerializer, Duration.ofSeconds(10));

        final Map<String, Object> responseBody = new HashMap<>();
        responseBody.put(ManagementConstants.SESSION_STATE, null);
        responseMessage.setBody(new AmqpValue(responseBody));

        // Act & Assert
        StepVerifier.create(managementChannel.getSessionState())
            .verifyComplete();

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture());

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
        managementChannel = new ManagementChannel(Mono.just(requestResponseChannel), NAMESPACE, ENTITY_PATH, sessionId,
            tokenManager, messageSerializer, Duration.ofSeconds(10));

        final Map<String, Object> responseBody = new HashMap<>();
        responseBody.put(ManagementConstants.EXPIRATION, expirationDate);
        responseMessage.setBody(new AmqpValue(responseBody));

        // Act & Assert
        StepVerifier.create(managementChannel.renewSessionLock())
            .assertNext(expiration -> assertEquals(instant, expiration))
            .verifyComplete();

        verify(requestResponseChannel).sendWithAck(messageCaptor.capture());

        final Message sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getBody() instanceof AmqpValue);

        final AmqpValue amqpValue = (AmqpValue) sentMessage.getBody();

        @SuppressWarnings("unchecked") final Map<String, Object> hashMap = (Map<String, Object>) amqpValue.getValue();
        assertEquals(sessionId, hashMap.get(ManagementConstants.SESSION_ID));

        // Assert application properties
        final Map<String, Object> applicationProperties = sentMessage.getApplicationProperties().getValue();
        assertEquals(OPERATION_RENEW_SESSION_LOCK, applicationProperties.get(MANAGEMENT_OPERATION_KEY));
    }

    private static Stream<Arguments> sessionStates() {
        // Got a warning about this being confusing because it was passed to varargs. So we cast to Object.
        final Object contents = new byte[]{10, 11, 8, 88};
        return Stream.of(
            Arguments.of(contents),
            Arguments.of((Object) null));
    }
}
