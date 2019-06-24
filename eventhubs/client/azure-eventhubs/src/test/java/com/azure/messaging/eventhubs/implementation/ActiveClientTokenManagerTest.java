// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.exception.AzureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ActiveClientTokenManagerTest {
    private static final String AUDIENCE = "an-audience-test";
    private static final Duration TIMEOUT = Duration.ofSeconds(4);

    @Mock
    private CBSNode cbsNode;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        cbsNode = null;
    }

    /**
     * Verify that we can get successes and errors from CBS node.
     */
    @Test
    public void getAuthorizationResults() {
        // Arrange
        final Mono<CBSNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        when(cbsNode.authorize(any())).thenReturn(getNextExpiration(3));

        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE);

        // Act & Assert
        StepVerifier.create(tokenManager.getAuthorizationResults())
            .then(() -> tokenManager.authorize().block(TIMEOUT))
            .expectNext(AmqpResponseCode.ACCEPTED)
            .expectNext(AmqpResponseCode.ACCEPTED)
            .then(tokenManager::close)
            .verifyComplete();
    }

    /**
     * Verify that we can get successes and errors from CBS node. This un-retriable error will not allow it to be
     * rescheduled.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getAuthorizationResultsSuccessFailure() {
        // Arrange
        final Mono<CBSNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        final IllegalArgumentException error = new IllegalArgumentException("Some error");

        when(cbsNode.authorize(any())).thenReturn(getNextExpiration(2),
            getNextExpiration(2), Mono.error(error));

        // Act & Assert
        try (ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE)) {
            StepVerifier.create(tokenManager.getAuthorizationResults())
                .then(() -> tokenManager.authorize().block(TIMEOUT))
                .expectNext(AmqpResponseCode.ACCEPTED)
                .expectError(IllegalArgumentException.class)
                .verifyThenAssertThat()
                .hasNotDroppedElements()
                .hasNotDroppedElements()
                .hasNotDroppedErrors();
        }
    }

    /**
     * Verify that we cannot authorize with CBS node when it has already been disposed of.
     */
    @Test
    public void cannotAuthorizeDisposedInstance() {
        // Arrange
        final Mono<CBSNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        when(cbsNode.authorize(any())).thenReturn(getNextExpiration(2));

        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE);
        tokenManager.authorize().then(Mono.fromRunnable(tokenManager::close)).block();

        // Act & Assert
        StepVerifier.create(tokenManager.authorize())
            .expectError(AzureException.class)
            .verify();
    }

    /**
     * Verify that the ActiveClientTokenManager reschedules the authorization task.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getAuthorizationResultsRetriableError() {
        // Arrange
        final Mono<CBSNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        final AmqpException error = new AmqpException(true, ErrorCondition.TIMEOUT_ERROR, "Timed out",
            new ErrorContext("Test-context-namespace"));

        when(cbsNode.authorize(any())).thenReturn(getNextExpiration(3), Mono.error(error),
            getNextExpiration(5), getNextExpiration(10),
            getNextExpiration(45));

        // Act & Assert
        try (ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE)) {
            StepVerifier.create(tokenManager.getAuthorizationResults())
                .then(() -> tokenManager.authorize().block(TIMEOUT))
                .expectError(AmqpException.class)
                .verify();

            StepVerifier.create(tokenManager.getAuthorizationResults())
                .expectNext(AmqpResponseCode.ACCEPTED)
                .expectNext(AmqpResponseCode.ACCEPTED)
                .then(tokenManager::close)
                .verifyComplete();
        }
    }

    private Mono<OffsetDateTime> getNextExpiration(long secondsToWait) {
        return Mono.fromCallable(() -> OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(secondsToWait));
    }
}
