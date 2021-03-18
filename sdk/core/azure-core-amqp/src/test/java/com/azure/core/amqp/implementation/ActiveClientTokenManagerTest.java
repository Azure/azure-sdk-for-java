// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.exception.AzureException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ActiveClientTokenManagerTest {
    private static final String AUDIENCE = "an-audience-test";
    private static final String SCOPES = "scopes-test";
    private static final Duration DEFAULT_DURATION = Duration.ofSeconds(20);

    @Mock
    private ClaimsBasedSecurityNode cbsNode;
    private AutoCloseable mocksCloseable;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMocks();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Verify that we can get successes and errors from CBS node.
     */
    @Test
    void getAuthorizationResults() {
        // Arrange
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        when(cbsNode.authorize(any(), any())).thenReturn(getNextExpiration(DEFAULT_DURATION));

        // Act & Assert
        StepVerifier.withVirtualTime(() -> {
            final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
            return tokenManager.authorize().thenMany(tokenManager.getAuthorizationResults());
        })
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenAwait(DEFAULT_DURATION)
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenCancel()
            .verify();
    }

    /**
     * Verify that we can get successes and errors from CBS node. This un-retriable error will not allow it to be
     * rescheduled.
     */
    @Test
    void getAuthorizationResultsSuccessFailure() {
        // Arrange
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        final IllegalArgumentException error = new IllegalArgumentException("Some error");
        final Duration expiryDuration = Duration.ofSeconds(20);

        final AtomicInteger invocations = new AtomicInteger();
        when(cbsNode.authorize(any(), any())).thenAnswer(invocationOnMock -> {
            if (invocations.incrementAndGet() < 3) {
                return Mono.just(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(20));
            } else {
                return Mono.error(error);
            }
        });

        // Act & Assert
        StepVerifier.withVirtualTime(() -> {
            final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
            return tokenManager.authorize().thenMany(tokenManager.getAuthorizationResults());
        })
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenAwait(expiryDuration)
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenAwait(expiryDuration)
            .expectError(IllegalArgumentException.class)
            .verifyThenAssertThat()
            .hasNotDroppedElements()
            .hasNotDroppedElements()
            .hasNotDroppedErrors();
    }

    /**
     * Verify that we cannot authorize with CBS node when it has already been disposed of.
     */
    @Test
    void cannotAuthorizeDisposedInstance() {
        // Arrange
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        when(cbsNode.authorize(any(), any())).thenReturn(getNextExpiration(DEFAULT_DURATION));

        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
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
    void getAuthorizationResultsRetriableError() {
        // Arrange
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.ARGUMENT_ERROR,
            "Retryable argument error", new AmqpErrorContext("Test-context-namespace"));

        when(cbsNode.authorize(any(), any())).thenReturn(getNextExpiration(DEFAULT_DURATION), Mono.error(error),
            getNextExpiration(DEFAULT_DURATION));

        // Act & Assert
        StepVerifier.withVirtualTime(() -> {
            final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
            return tokenManager.authorize().thenMany(tokenManager.getAuthorizationResults());
        })
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenAwait(DEFAULT_DURATION)

            // This is where the exception occurs and we await another retry.
            .thenAwait(DEFAULT_DURATION)
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenCancel()
            .verify();
    }

    /**
     * Verify that the ActiveClientTokenManager does not get more authorization tasks.
     */
    @SuppressWarnings("unchecked")
    @Test
    void getAuthorizationResultsNonRetriableError() {
        // Arrange
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);
        final AmqpException error = new AmqpException(false, AmqpErrorCondition.TIMEOUT_ERROR, "Test CBS node error.",
            new AmqpErrorContext("Test-context-namespace"));

        when(cbsNode.authorize(any(), any())).thenReturn(getNextExpiration(DEFAULT_DURATION), Mono.error(error),
            getNextExpiration(DEFAULT_DURATION));

        // Act & Assert
        StepVerifier.withVirtualTime(() -> {
            final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
            return tokenManager.authorize().thenMany(tokenManager.getAuthorizationResults());
        })
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenAwait(DEFAULT_DURATION)
            .expectErrorSatisfies(throwable -> {
                Assertions.assertTrue(throwable instanceof AmqpException);
                Assertions.assertFalse(((AmqpException) throwable).isTransient());
            })
            .verify();
    }

    private Mono<OffsetDateTime> getNextExpiration(Duration duration) {
        return Mono.fromCallable(() -> OffsetDateTime.now(ZoneOffset.UTC).plus(duration));
    }
}
