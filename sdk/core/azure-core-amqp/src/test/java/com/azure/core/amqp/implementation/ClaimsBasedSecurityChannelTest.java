// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.amqp.implementation.ClaimsBasedSecurityChannel.PUT_TOKEN_AUDIENCE;
import static com.azure.core.amqp.implementation.ClaimsBasedSecurityChannel.PUT_TOKEN_EXPIRY;
import static com.azure.core.amqp.implementation.ClaimsBasedSecurityChannel.PUT_TOKEN_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ClaimsBasedSecurityChannel}.
 */
class ClaimsBasedSecurityChannelTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(10);

    private final AmqpRetryOptions options
        = new AmqpRetryOptions().setMode(AmqpRetryMode.FIXED).setTryTimeout(Duration.ofSeconds(45)).setMaxRetries(4);
    private final String tokenAudience = "path.foo.bar";
    private final String scopes = "scopes.cbs.foo";
    private final OffsetDateTime validUntil = OffsetDateTime.of(2019, 11, 10, 15, 2, 5, 0, ZoneOffset.UTC);
    private final AccessToken accessToken = new AccessToken("an-access-token?", validUntil);

    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;

    @Mock
    private RequestResponseChannel requestResponseChannel;

    @Mock
    private TokenCredential tokenCredential;
    private Message acceptedResponse;
    private Message unauthorizedResponse;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        acceptedResponse = Proton.message();
        final Map<String, Object> responseProperties = new HashMap<>();
        responseProperties.put("status-code", AmqpResponseCode.ACCEPTED.getValue());
        acceptedResponse.setApplicationProperties(new ApplicationProperties(responseProperties));

        unauthorizedResponse = Proton.message();
        final Map<String, Object> properties = new HashMap<>();
        properties.put("status-code", AmqpResponseCode.UNAUTHORIZED.getValue());
        properties.put("statusDescription", "This is unauthorized. :)");
        unauthorizedResponse.setApplicationProperties(new ApplicationProperties(properties));
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
        requestResponseChannel = null;
        tokenCredential = null;

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Closing a manager must not interrupt CBS dispatch initiated by an asynchronously emitted credential token.
     */
    @Test
    void managerCloseDuringAsynchronousCredentialDispatchPreservesPipe() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        AtomicInteger tokens = new AtomicInteger();
        AtomicInteger requests = new AtomicInteger();
        AtomicBoolean interrupted = new AtomicBoolean();
        AtomicReference<Throwable> dispatchError = new AtomicReference<>();
        AtomicReference<Throwable> shutdown = new AtomicReference<>();
        Scheduler credentialScheduler = Schedulers.newBoundedElastic(1, 10, "test-credential");
        Reactor reactor = Proton.reactor();
        Pipe pipe = Pipe.open();
        ReactorDispatcher dispatcher = new ReactorDispatcher("async-credential", reactor, pipe);
        Disposable shutdownSubscription = dispatcher.getShutdownSignal().subscribe(signal -> {
        }, shutdown::set);
        when(tokenCredential.getToken(any())).thenAnswer(invocation -> Mono.fromCallable(() -> {
            Duration lifetime = tokens.incrementAndGet() == 1 ? Duration.ofSeconds(3) : Duration.ofHours(1);
            return new AccessToken("test-token", OffsetDateTime.now(ZoneOffset.UTC).plus(lifetime));
        }).subscribeOn(credentialScheduler));
        when(requestResponseChannel.sendWithAck(any())).thenAnswer(invocation -> {
            if (requests.incrementAndGet() == 1) {
                return Mono.just(acceptedResponse);
            }
            return Mono.create(sink -> {
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
                    Thread.interrupted();
                    finished.countDown();
                }
            });
        });
        ClaimsBasedSecurityChannel channel = new ClaimsBasedSecurityChannel(Mono.just(requestResponseChannel),
            tokenCredential, CbsAuthorizationType.JSON_WEB_TOKEN, options);
        ActiveClientTokenManager manager = new ActiveClientTokenManager(Mono.just(channel), tokenAudience, scopes);
        try {
            manager.authorize().block(VERIFY_TIMEOUT);
            assertTrue(entered.await(VERIFY_TIMEOUT.getSeconds(), TimeUnit.SECONDS));
            manager.close();
            release.countDown();
            assertTrue(finished.await(VERIFY_TIMEOUT.getSeconds(), TimeUnit.SECONDS));
            assertFalse(interrupted.get());
            assertNull(dispatchError.get());
            assertNull(shutdown.get());
            assertTrue(pipe.sink().isOpen());
            dispatcher.invoke(() -> {
            });
            assertEquals(2, pipe.source().read(ByteBuffer.allocate(2)));
            assertEquals(2, tokens.get());
            assertEquals(2, requests.get());
        } finally {
            release.countDown();
            manager.close();
            finished.await(VERIFY_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            shutdownSubscription.dispose();
            credentialScheduler.dispose();
            pipe.sink().close();
            pipe.source().close();
            reactor.free();
        }
    }

    /**
     * Tests that the proper token type is used for SAS token.
     */
    @Test
    public void authorizesSasToken() {
        // Arrange
        final Date expectedDate = Date.from(validUntil.toInstant());
        final ClaimsBasedSecurityChannel cbsChannel = new ClaimsBasedSecurityChannel(Mono.just(requestResponseChannel),
            tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, options);

        when(requestResponseChannel.sendWithAck(any())).thenReturn(Mono.just(acceptedResponse));
        when(tokenCredential.getToken(argThat(arg -> arg.getScopes().contains(scopes))))
            .thenReturn(Mono.just(accessToken));

        // Act
        StepVerifier.create(cbsChannel.authorize(tokenAudience, scopes))
            .expectNext(accessToken.getExpiresAt())
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // Assert
        verify(requestResponseChannel).sendWithAck(messageArgumentCaptor.capture());
        final Message message = messageArgumentCaptor.getValue();

        final Map<String, Object> properties = message.getApplicationProperties().getValue();
        assertTrue(properties.containsKey(PUT_TOKEN_AUDIENCE), "'PUT_TOKEN_AUDIENCE' should be there.");
        Assertions.assertEquals(tokenAudience, properties.get(PUT_TOKEN_AUDIENCE));
        Assertions.assertEquals("servicebus.windows.net:sastoken", properties.get(PUT_TOKEN_TYPE));

        assertTrue(properties.get(PUT_TOKEN_EXPIRY) instanceof Date);
        Assertions.assertEquals(expectedDate, properties.get(PUT_TOKEN_EXPIRY));
    }

    /**
     * Tests that the proper token type is used for AAD.
     */
    @Test
    public void authorizesJwt() {
        // Arrange
        final ClaimsBasedSecurityChannel cbsChannel = new ClaimsBasedSecurityChannel(Mono.just(requestResponseChannel),
            tokenCredential, CbsAuthorizationType.JSON_WEB_TOKEN, options);

        when(requestResponseChannel.sendWithAck(any())).thenReturn(Mono.just(acceptedResponse));
        when(tokenCredential.getToken(argThat(arg -> arg.getScopes().contains(scopes))))
            .thenReturn(Mono.just(accessToken));

        // Act
        StepVerifier.create(cbsChannel.authorize(tokenAudience, scopes))
            .expectNext(accessToken.getExpiresAt())
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // Assert
        verify(requestResponseChannel).sendWithAck(messageArgumentCaptor.capture());
        final Message message = messageArgumentCaptor.getValue();

        final Map<String, Object> properties = message.getApplicationProperties().getValue();
        assertTrue(properties.containsKey(PUT_TOKEN_AUDIENCE), "'PUT_TOKEN_AUDIENCE' should be there.");
        Assertions.assertEquals(tokenAudience, properties.get(PUT_TOKEN_AUDIENCE));
        Assertions.assertEquals("jwt", properties.get(PUT_TOKEN_TYPE));
    }

    /**
     * Tests that it errors when an unsuccessful response is returned.
     */
    @Test
    void invalidReturn() {
        // Arrange
        final ClaimsBasedSecurityChannel cbsChannel = new ClaimsBasedSecurityChannel(Mono.just(requestResponseChannel),
            tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, options);

        when(tokenCredential.getToken(argThat(arg -> arg.getScopes().contains(scopes))))
            .thenReturn(Mono.just(accessToken));

        when(requestResponseChannel.sendWithAck(any())).thenReturn(Mono.just(unauthorizedResponse));

        // Act
        StepVerifier.create(cbsChannel.authorize(tokenAudience, scopes)).expectErrorSatisfies(error -> {
            assertTrue(error instanceof AmqpException);
            assertEquals(AmqpErrorCondition.UNAUTHORIZED_ACCESS, ((AmqpException) error).getErrorCondition());
        }).verify(VERIFY_TIMEOUT);
    }

    /**
     * Verifies that it errors when no response is received from CBS node.
     */
    @Test
    void errorsWhenNoResponse() {
        // Arrange
        final AccessToken accessToken = new AccessToken("an-access-token?", validUntil);
        final ClaimsBasedSecurityChannel cbsChannel = new ClaimsBasedSecurityChannel(Mono.just(requestResponseChannel),
            tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, options);

        when(tokenCredential.getToken(argThat(arg -> arg.getScopes().contains(scopes))))
            .thenReturn(Mono.just(accessToken));

        when(requestResponseChannel.sendWithAck(any())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(cbsChannel.authorize(tokenAudience, scopes)).expectErrorSatisfies(error -> {
            assertTrue(error instanceof AmqpException);
            assertTrue(((AmqpException) error).isTransient());
        }).verify(VERIFY_TIMEOUT);
    }

    /**
     * Verifies that it closes the CBS node asynchronously.
     */
    @Test
    void closesAsync() {
        // Arrange
        final ClaimsBasedSecurityChannel cbsChannel
            = new ClaimsBasedSecurityChannel(Mono.defer(() -> Mono.just(requestResponseChannel)), tokenCredential,
                CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, options);

        when(requestResponseChannel.closeAsync()).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(cbsChannel.closeAsync()).expectComplete().verify(VERIFY_TIMEOUT);

        verify(requestResponseChannel).closeAsync();
    }

    /**
     * Verifies that it closes the cbs node synchronously.
     */
    @Test
    void closes() {
        // Arrange
        final ClaimsBasedSecurityChannel cbsChannel
            = new ClaimsBasedSecurityChannel(Mono.defer(() -> Mono.just(requestResponseChannel)), tokenCredential,
                CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, options);

        when(requestResponseChannel.closeAsync()).thenReturn(Mono.empty());

        // Act & Assert
        cbsChannel.close();

        verify(requestResponseChannel).closeAsync();
    }
}
