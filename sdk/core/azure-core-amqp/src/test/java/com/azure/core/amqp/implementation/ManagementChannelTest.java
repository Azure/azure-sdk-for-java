// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.amqp.models.DeliveryOutcome;
import com.azure.core.amqp.models.DeliveryState;
import com.azure.core.amqp.models.ModifiedDeliveryOutcome;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ManagementChannel}.
 */
public class ManagementChannelTest {
    private static final String STATUS_CODE_KEY = "status-code";
    private static final String STATUS_DESCRIPTION_KEY = "status-description";
    private static final String ERROR_CONDITION_KEY = "errorCondition";

    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";

    private final ClientLogger logger = new ClientLogger(ManagementChannelTest.class);

    // Mocked response values from the RequestResponseChannel.
    private final Map<String, Object> applicationProperties = new HashMap<>();
    private final Message responseMessage = Proton.message();
    private final TestPublisher<AmqpResponseCode> tokenProviderResults = TestPublisher.createCold();
    private final AmqpErrorContext errorContext = new AmqpErrorContext("Foo-bar");
    private final AmqpMessageBody messageBody = AmqpMessageBody.fromData("test-body".getBytes(StandardCharsets.UTF_8));
    private final AmqpAnnotatedMessage annotatedMessage = new AmqpAnnotatedMessage(messageBody);

    private ManagementChannel managementChannel;
    private AutoCloseable autoCloseable;

    @Mock
    private TokenManager tokenManager;
    @Mock
    private RequestResponseChannel requestResponseChannel;
    @Mock
    private AmqpRetryPolicy retryPolicy;

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    public void setup(TestInfo testInfo) {
        logger.info("[{}] Setting up.", testInfo.getDisplayName());

        autoCloseable = MockitoAnnotations.openMocks(this);

        final AmqpChannelProcessor<RequestResponseChannel> requestResponseMono =
            Mono.defer(() -> Mono.just(requestResponseChannel)).subscribeWith(new AmqpChannelProcessor<>(
                "foo", "bar", RequestResponseChannel::getEndpointStates,
                retryPolicy, logger));

        when(tokenManager.authorize()).thenReturn(Mono.just(1000L));
        when(tokenManager.getAuthorizationResults()).thenReturn(tokenProviderResults.flux());

        when(requestResponseChannel.getErrorContext()).thenReturn(errorContext);
        when(requestResponseChannel.getEndpointStates()).thenReturn(Flux.never());

        managementChannel = new ManagementChannel(requestResponseMono, NAMESPACE, ENTITY_PATH, tokenManager);
    }

    @AfterEach
    public void teardown(TestInfo testInfo) throws Exception {
        logger.info("[{}] Tearing down.", testInfo.getDisplayName());
        if (autoCloseable != null) {
            autoCloseable.close();
        }

        Mockito.framework().clearInlineMocks();
    }

    /**
     * When an empty response is returned, an error is returned.
     */
    @Test
    public void sendMessageEmptyResponseErrors() {
        // Arrange
        when(requestResponseChannel.getErrorContext()).thenReturn(errorContext);
        when(requestResponseChannel.sendWithAck(any(Message.class))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(managementChannel.send(annotatedMessage))
            .then(() -> tokenProviderResults.next(AmqpResponseCode.OK))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertEquals(errorContext, ((AmqpException) error).getContext());
                assertTrue(((AmqpException) error).isTransient());
            })
            .verify();
    }

    /**
     * Sends a message with success and asserts the response.
     */
    @MethodSource("successfulResponseCodes")
    @ParameterizedTest
    public void sendMessage(AmqpResponseCode responseCode) {
        // Arrange
        when(requestResponseChannel.getErrorContext()).thenReturn(errorContext);
        when(requestResponseChannel.sendWithAck(any(Message.class))).thenReturn(Mono.just(responseMessage));

        applicationProperties.put(STATUS_CODE_KEY, responseCode.getValue());
        responseMessage.setApplicationProperties(new ApplicationProperties(applicationProperties));

        final byte[] body = "foo".getBytes(StandardCharsets.UTF_8);
        final Data dataBody = new Data(Binary.create(ByteBuffer.wrap(body)));
        responseMessage.setBody(dataBody);

        // Act & Assert
        StepVerifier.create(managementChannel.send(annotatedMessage))
            .then(() -> tokenProviderResults.next(AmqpResponseCode.OK))
            .assertNext(actual -> {
                assertNotNull(actual.getApplicationProperties());
                assertEquals(responseCode.getValue(), actual.getApplicationProperties().get(STATUS_CODE_KEY));

                assertEquals(AmqpMessageBodyType.DATA, actual.getBody().getBodyType());
                assertEquals(body, actual.getBody().getFirstData());
            })
            .expectComplete()
            .verify();
    }

    /**
     * Sends a message and a delivery outcome with success and asserts the response.
     */
    @MethodSource("successfulResponseCodes")
    @ParameterizedTest
    public void sendMessageWithOutcome(AmqpResponseCode responseCode) {
        // Arrange
        final ModifiedDeliveryOutcome outcome = new ModifiedDeliveryOutcome();
        when(requestResponseChannel.getErrorContext()).thenReturn(errorContext);
        when(requestResponseChannel.sendWithAck(any(Message.class), argThat(p -> p instanceof Modified)))
            .thenReturn(Mono.just(responseMessage));

        applicationProperties.put(STATUS_CODE_KEY, responseCode.getValue());
        responseMessage.setApplicationProperties(new ApplicationProperties(applicationProperties));

        final byte[] body = "foo-bar".getBytes(StandardCharsets.UTF_8);
        final Data dataBody = new Data(Binary.create(ByteBuffer.wrap(body)));
        responseMessage.setBody(dataBody);

        // Act & Assert
        StepVerifier.create(managementChannel.send(annotatedMessage, outcome))
            .then(() -> tokenProviderResults.next(AmqpResponseCode.OK))
            .assertNext(actual -> {
                assertNotNull(actual.getApplicationProperties());
                assertEquals(responseCode.getValue(), actual.getApplicationProperties().get(STATUS_CODE_KEY));

                assertEquals(AmqpMessageBodyType.DATA, actual.getBody().getBodyType());
                assertEquals(body, actual.getBody().getFirstData());
            })
            .expectComplete()
            .verify();
    }

    /**
     * When an empty response is returned for sending a message with deliveryOutcome, an error is returned.
     */
    @Test
    public void sendMessageDeliveryOutcomeEmptyResponseErrors() {
        // Arrange
        final DeliveryOutcome outcome = new DeliveryOutcome(DeliveryState.ACCEPTED);

        when(requestResponseChannel.getErrorContext()).thenReturn(errorContext);
        when(requestResponseChannel.sendWithAck(any(Message.class), eq(Accepted.getInstance())))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(managementChannel.send(annotatedMessage, outcome))
            .then(() -> tokenProviderResults.next(AmqpResponseCode.OK))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertEquals(errorContext, ((AmqpException) error).getContext());
                assertTrue(((AmqpException) error).isTransient());
            })
            .verify();
    }

    /**
     * When an authorization returns no response, it errors.
     */
    @Test
    public void sendMessageDeliveryOutcomeNoAuthErrors() {
        // Arrange
        final DeliveryOutcome outcome = new DeliveryOutcome(DeliveryState.ACCEPTED);

        when(requestResponseChannel.getErrorContext()).thenReturn(errorContext);
        when(requestResponseChannel.sendWithAck(any(Message.class), eq(Accepted.getInstance())))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(managementChannel.send(annotatedMessage, outcome))
            .then(() -> tokenProviderResults.complete())
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertFalse(((AmqpException) error).isTransient());
            })
            .verify();

        verify(requestResponseChannel, never()).sendWithAck(any(), any());
    }

    /**
     * Sends a message with {@link AmqpResponseCode#NOT_FOUND} and asserts the response.
     */
    @MethodSource
    @ParameterizedTest
    public void sendMessageNotFound(AmqpErrorCondition errorCondition) {
        // Arrange
        when(requestResponseChannel.getErrorContext()).thenReturn(errorContext);
        when(requestResponseChannel.sendWithAck(any(Message.class))).thenReturn(Mono.just(responseMessage));

        final AmqpResponseCode responseCode = AmqpResponseCode.NOT_FOUND;
        applicationProperties.put(STATUS_CODE_KEY, responseCode.getValue());
        applicationProperties.put(ERROR_CONDITION_KEY, Symbol.getSymbol(errorCondition.getErrorCondition()));

        responseMessage.setApplicationProperties(new ApplicationProperties(applicationProperties));

        final byte[] body = "foo".getBytes(StandardCharsets.UTF_8);
        final Data dataBody = new Data(Binary.create(ByteBuffer.wrap(body)));
        responseMessage.setBody(dataBody);

        // Act & Assert
        StepVerifier.create(managementChannel.send(annotatedMessage))
            .then(() -> tokenProviderResults.next(AmqpResponseCode.OK))
            .assertNext(actual -> {
                assertNotNull(actual.getApplicationProperties());
                assertEquals(responseCode.getValue(), actual.getApplicationProperties().get(STATUS_CODE_KEY));

                assertEquals(AmqpMessageBodyType.DATA, actual.getBody().getBodyType());
                assertEquals(body, actual.getBody().getFirstData());
            })
            .expectComplete()
            .verify();
    }

    /**
     * Tests that we propagate any management errors.
     */
    @Test
    public void sendMessageUnsuccessful() {
        // Arrange
        when(requestResponseChannel.getErrorContext()).thenReturn(errorContext);
        when(requestResponseChannel.sendWithAck(any(Message.class))).thenReturn(Mono.just(responseMessage));

        final String statusDescription = "a status description";
        final AmqpResponseCode responseCode = AmqpResponseCode.FORBIDDEN;
        final AmqpErrorCondition errorCondition = AmqpErrorCondition.ILLEGAL_STATE;
        applicationProperties.put(STATUS_CODE_KEY, responseCode.getValue());
        applicationProperties.put(STATUS_DESCRIPTION_KEY, statusDescription);
        applicationProperties.put(ERROR_CONDITION_KEY, Symbol.getSymbol(errorCondition.getErrorCondition()));

        responseMessage.setApplicationProperties(new ApplicationProperties(applicationProperties));

        // Act & Assert
        StepVerifier.create(managementChannel.send(annotatedMessage))
            .then(() -> tokenProviderResults.next(AmqpResponseCode.OK))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertFalse(((AmqpException) error).isTransient());
                assertEquals(errorCondition, ((AmqpException) error).getErrorCondition());
                assertEquals(errorContext, ((AmqpException) error).getContext());
            })
            .verify();
    }

    public static Stream<AmqpErrorCondition> sendMessageNotFound() {
        return Stream.of(AmqpErrorCondition.MESSAGE_NOT_FOUND, AmqpErrorCondition.SESSION_NOT_FOUND);
    }

    public static Stream<AmqpResponseCode> successfulResponseCodes() {
        return Stream.of(AmqpResponseCode.ACCEPTED, AmqpResponseCode.OK, AmqpResponseCode.NO_CONTENT);
    }

    /**
     * Verifies that an error is emitted when user is unauthorized.
     */
    @Test
    void unauthorized() {
        // Arrange
        final AmqpResponseCode responseCode = AmqpResponseCode.UNAUTHORIZED;
        final AmqpErrorCondition expected = AmqpErrorCondition.UNAUTHORIZED_ACCESS;

        // Act & Assert
        StepVerifier.create(managementChannel.send(annotatedMessage))
            .then(() -> tokenProviderResults.next(responseCode))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertEquals(expected, ((AmqpException) error).getErrorCondition());
            })
            .verify();
    }
}
