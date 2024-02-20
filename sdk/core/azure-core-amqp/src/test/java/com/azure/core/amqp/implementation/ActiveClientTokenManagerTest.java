// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.exception.AzureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActiveClientTokenManagerTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(30);
    private static final String AUDIENCE = "an-audience-test";
    private static final String SCOPES = "scopes-test";
    private static final Duration DEFAULT_DURATION = Duration.ofSeconds(5);

    /**
     * Verify that we can get successes and errors from CBS node.
     */
    @Test
    void getAuthorizationResults() {
        // Arrange
        ClaimsBasedSecurityNode cbsNode = new MockClaimsBasedSecurityNode(() -> getNextExpiration(DEFAULT_DURATION));
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);

        // Act & Assert
        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
        StepVerifier.create(tokenManager.authorize().thenMany(tokenManager.getAuthorizationResults()))
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenAwait(DEFAULT_DURATION)
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenCancel()
            .verify(VERIFY_TIMEOUT);
    }

    /**
     * Verify that we can get successes and errors from CBS node. This un-retriable error will not allow it to be
     * rescheduled.
     */
    @Test
    void getAuthorizationResultsSuccessFailure() {
        // Arrange
        final AtomicBoolean returnError = new AtomicBoolean(false);
        ClaimsBasedSecurityNode cbsNode = new MockClaimsBasedSecurityNode(() -> {
            if (returnError.get()) {
                return Mono.error(new IllegalArgumentException("Some error"));
            } else {
                return Mono.just(OffsetDateTime.now(ZoneOffset.UTC).plus(DEFAULT_DURATION));
            }
        });
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);

        // Act & Assert
        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
        StepVerifier.create(tokenManager.authorize().thenMany(tokenManager.getAuthorizationResults()))
            .expectNext(AmqpResponseCode.ACCEPTED)
            .assertNext(code -> {
                assertEquals(AmqpResponseCode.ACCEPTED, code);
                returnError.set(true);
            })
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    /**
     * Verify that we cannot authorize with CBS node when it has already been disposed of.
     */
    @Test
    void cannotAuthorizeDisposedInstance() {
        // Arrange
        ClaimsBasedSecurityNode cbsNode = new MockClaimsBasedSecurityNode(() -> getNextExpiration(DEFAULT_DURATION));
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);

        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
        tokenManager.authorize().then(Mono.fromRunnable(tokenManager::close)).block();

        // Act & Assert
        StepVerifier.create(tokenManager.authorize()).expectError(AzureException.class).verify(VERIFY_TIMEOUT);
    }

    /**
     * Verify that the ActiveClientTokenManager reschedules the authorization task.
     */
    @Test
    void getAuthorizationResultsRetriableError() {
        // Arrange
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.ARGUMENT_ERROR,
            "Retryable argument error", new AmqpErrorContext("Test-context-namespace"));

        AtomicInteger authorizationCalls = new AtomicInteger();
        ClaimsBasedSecurityNode cbsNode = new MockClaimsBasedSecurityNode(() -> {
            switch (authorizationCalls.getAndIncrement()) {
                case 0:
                    return getNextExpiration(DEFAULT_DURATION);

                case 1:
                    return Mono.error(error);

                case 2:
                    return getNextExpiration(DEFAULT_DURATION);

                default:
                    return Mono.error(new IllegalStateException("Too many authorization requests"));
            }
        });
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);

        // Act & Assert
        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
        StepVerifier.create(tokenManager.authorize().thenMany(tokenManager.getAuthorizationResults()))
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenAwait(DEFAULT_DURATION)

            // This is where the exception occurs and we await another retry.
            .thenAwait(DEFAULT_DURATION)
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenCancel()
            .verify(VERIFY_TIMEOUT.multipliedBy(2));
    }

    /**
     * Verify that the ActiveClientTokenManager does not get more authorization tasks.
     */
    @Test
    void getAuthorizationResultsNonRetriableError() {
        // Arrange
        final AmqpException error = new AmqpException(false, AmqpErrorCondition.TIMEOUT_ERROR, "Test CBS node error.",
            new AmqpErrorContext("Test-context-namespace"));

        AtomicInteger authorizationCalls = new AtomicInteger();
        ClaimsBasedSecurityNode cbsNode = new MockClaimsBasedSecurityNode(() -> {
            switch (authorizationCalls.getAndIncrement()) {
                case 0:
                    return getNextExpiration(DEFAULT_DURATION);

                case 1:
                    return Mono.error(error);

                case 2:
                    return getNextExpiration(DEFAULT_DURATION);

                default:
                    return Mono.error(new IllegalStateException("Too many authorization requests"));
            }
        });
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = Mono.fromCallable(() -> cbsNode);

        // Act & Assert
        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeMono, AUDIENCE, SCOPES);
        StepVerifier.create(tokenManager.authorize().thenMany(tokenManager.getAuthorizationResults()))
            .expectNext(AmqpResponseCode.ACCEPTED)
            .thenAwait(DEFAULT_DURATION)
            .expectErrorSatisfies(throwable -> {
                Assertions.assertTrue(throwable instanceof AmqpException);
                Assertions.assertFalse(((AmqpException) throwable).isTransient());
            })
            .verify(VERIFY_TIMEOUT);
    }

    private Mono<OffsetDateTime> getNextExpiration(Duration duration) {
        return Mono.fromCallable(() -> OffsetDateTime.now(ZoneOffset.UTC).plus(duration));
    }

    private static final class MockClaimsBasedSecurityNode implements ClaimsBasedSecurityNode {
        private final Supplier<Mono<OffsetDateTime>> authorizationSupplier;

        MockClaimsBasedSecurityNode(Supplier<Mono<OffsetDateTime>> authorizationSupplier) {
            this.authorizationSupplier = authorizationSupplier;
        }

        @Override
        public Mono<OffsetDateTime> authorize(String audience, String scopes) {
            return authorizationSupplier.get();
        }

        @Override
        public void close() {
            // no-op
        }
    }
}
