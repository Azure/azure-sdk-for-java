// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.exception.AzureException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void cannotSubscribeToAuthorizationAfterClose() {
        AtomicInteger calls = new AtomicInteger();
        ClaimsBasedSecurityNode node = new MockClaimsBasedSecurityNode(() -> {
            calls.incrementAndGet();
            return getNextExpiration(DEFAULT_DURATION);
        });
        ActiveClientTokenManager manager = new ActiveClientTokenManager(Mono.just(node), AUDIENCE, SCOPES);
        Mono<Long> authorization = manager.authorize();
        manager.close();

        StepVerifier.create(authorization).expectError(AzureException.class).verify(VERIFY_TIMEOUT);
        assertEquals(0, calls.get());
    }

    @Test
    void cbsNodeArrivingAfterCloseDoesNotStartAuthorization() {
        AtomicReference<MonoSink<ClaimsBasedSecurityNode>> response = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        ClaimsBasedSecurityNode node = new MockClaimsBasedSecurityNode(() -> {
            calls.incrementAndGet();
            return getNextExpiration(DEFAULT_DURATION);
        });
        ActiveClientTokenManager manager = new ActiveClientTokenManager(Mono.create(response::set), AUDIENCE, SCOPES);

        StepVerifier.create(manager.authorize()).then(() -> {
            manager.close();
            response.get().success(node);
        }).expectError(AzureException.class).verify(VERIFY_TIMEOUT);
        assertEquals(0, calls.get());
    }

    @Test
    void initialAuthorizationCompletingAfterCloseDoesNotScheduleRefresh() {
        AtomicReference<MonoSink<OffsetDateTime>> response = new AtomicReference<>();
        ClaimsBasedSecurityNode node = new MockClaimsBasedSecurityNode(() -> Mono.create(response::set));
        ActiveClientTokenManager manager = new ActiveClientTokenManager(Mono.just(node), AUDIENCE, SCOPES);

        StepVerifier.create(manager.authorize()).then(() -> {
            manager.close();
            response.get().success(OffsetDateTime.now(ZoneOffset.UTC).plusHours(1));
        }).expectNextCount(1).verifyComplete();
        StepVerifier.create(manager.getAuthorizationResults()).verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = { "close", "terminal", "success", "transient" })
    void refreshCancellationPreservesDispatcher(String trigger) throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        CountDownLatch terminated = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        AtomicBoolean interrupted = new AtomicBoolean();
        AtomicReference<MonoSink<OffsetDateTime>> response = new AtomicReference<>();
        AtomicReference<Throwable> dispatchError = new AtomicReference<>();
        AtomicReference<Throwable> shutdown = new AtomicReference<>();
        AtomicReference<Throwable> authorizationError = new AtomicReference<>();
        AmqpException error = new AmqpException("transient".equals(trigger), AmqpErrorCondition.NOT_ALLOWED,
            "Test CBS failure", new AmqpErrorContext("Test-context-namespace"));
        Reactor reactor = Proton.reactor();
        Pipe pipe = Pipe.open();
        ReactorDispatcher dispatcher = new ReactorDispatcher("refresh-cancellation", reactor, pipe);
        Disposable shutdownSubscription = dispatcher.getShutdownSignal().subscribe(signal -> {
        }, shutdown::set);
        ClaimsBasedSecurityNode node = new MockClaimsBasedSecurityNode(() -> {
            if (calls.incrementAndGet() == 1) {
                return getNextExpiration(Duration.ofSeconds(3));
            }
            return Mono.create(sink -> {
                response.set(sink);
                entered.countDown();
                boolean wasInterrupted = false;
                long deadline = System.nanoTime() + VERIFY_TIMEOUT.toNanos();
                try {
                    while (release.getCount() != 0 && System.nanoTime() < deadline) {
                        try {
                            release.await(1, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            wasInterrupted = true;
                        }
                    }
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt();
                    }
                    interrupted.set(Thread.currentThread().isInterrupted());
                    dispatcher.invoke(() -> {
                    });
                } catch (Exception e) {
                    dispatchError.set(e);
                } finally {
                    // Do not leak a failing test's interrupt to another task on the shared scheduler.
                    Thread.interrupted();
                    finished.countDown();
                }
            });
        });
        ActiveClientTokenManager manager = new ActiveClientTokenManager(Mono.just(node), AUDIENCE, SCOPES);
        Disposable results = manager.getAuthorizationResults().subscribe(code -> {
        }, failure -> {
            authorizationError.set(failure);
            terminated.countDown();
        });
        try {
            manager.authorize().block(VERIFY_TIMEOUT);
            assertTrue(entered.await(VERIFY_TIMEOUT.getSeconds(), TimeUnit.SECONDS));
            if ("close".equals(trigger)) {
                manager.close();
            } else if ("success".equals(trigger)) {
                response.get().success(OffsetDateTime.now(ZoneOffset.UTC).plusHours(1));
            } else {
                response.get().error(error);
            }
            if ("terminal".equals(trigger)) {
                assertTrue(terminated.await(VERIFY_TIMEOUT.getSeconds(), TimeUnit.SECONDS));
                assertSame(error, authorizationError.get());
            }
            release.countDown();
            assertTrue(finished.await(VERIFY_TIMEOUT.getSeconds(), TimeUnit.SECONDS));
            assertFalse(interrupted.get());
            assertNull(dispatchError.get());
            assertNull(shutdown.get());
            assertTrue(pipe.sink().isOpen());

            // Another link can still signal the same connection's dispatcher.
            dispatcher.invoke(() -> {
            });
            ByteBuffer signals = ByteBuffer.allocate(2);
            assertEquals(2, pipe.source().read(signals));
            if ("close".equals(trigger) || "terminal".equals(trigger)) {
                StepVerifier.create(manager.authorize()).expectError(AzureException.class).verify(VERIFY_TIMEOUT);
            }
            assertEquals(2, calls.get());
        } finally {
            release.countDown();
            manager.close();
            finished.await(VERIFY_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            results.dispose();
            shutdownSubscription.dispose();
            pipe.sink().close();
            pipe.source().close();
            reactor.free();
        }
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
