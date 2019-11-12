// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.RetryMode;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static com.azure.core.amqp.implementation.CBSChannel.PUT_TOKEN_AUDIENCE;
import static com.azure.core.amqp.implementation.CBSChannel.PUT_TOKEN_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CBSChannelTest {
    private final RetryOptions options = new RetryOptions()
        .setRetryMode(RetryMode.FIXED)
        .setTryTimeout(Duration.ofSeconds(45))
        .setMaxRetries(4);

    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;

    @Mock
    private RequestResponseChannel requestResponseChannel;

    @Mock
    private TokenCredential tokenCredential;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        requestResponseChannel = null;
        tokenCredential = null;
    }

    @Test
    public void authorizesSasToken() {
        // Arrange
        final String tokenAudience = "path.foo.bar";
        final String scopes = "scopes.cbs.foo";
        final AccessToken accessToken = new AccessToken("an-access-token?", OffsetDateTime.of(2019, 11, 10, 15, 2, 5, 0, ZoneOffset.UTC));
        final CBSChannel cbsChannel = new CBSChannel(Mono.just(requestResponseChannel), tokenCredential,
            CBSAuthorizationType.SHARED_ACCESS_SIGNATURE, options);

        when(tokenCredential.getToken(argThat(arg -> arg.getScopes().contains(scopes))))
            .thenReturn(Mono.just(accessToken));

        when(requestResponseChannel.sendWithAck(any())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(cbsChannel.authorize(tokenAudience, scopes))
            .expectNext(accessToken.getExpiresAt())
            .expectComplete()
            .verify(Duration.ofSeconds(10));

        // Assert
        verify(requestResponseChannel, times(1)).sendWithAck(any());
        verify(requestResponseChannel).sendWithAck(messageArgumentCaptor.capture());
        final Message message = messageArgumentCaptor.getValue();

        final Map<String, Object> properties = message.getApplicationProperties().getValue();
        Assertions.assertTrue(properties.containsKey(PUT_TOKEN_AUDIENCE), "'PUT_TOKEN_AUDIENCE' should be there.");
        Assertions.assertEquals(tokenAudience, properties.get(PUT_TOKEN_AUDIENCE));
        Assertions.assertEquals("servicebus.windows.net:sastoken", properties.get(PUT_TOKEN_TYPE));
    }


    @Test
    public void authorizesJwt() {
        // Arrange
        final String tokenAudience = "path.foo.bar";
        final String scopes = "scopes.cbs.foo";
        final AccessToken accessToken = new AccessToken("an-access-token?", OffsetDateTime.of(2019, 11, 10, 15, 2, 5, 0, ZoneOffset.UTC));
        final CBSChannel cbsChannel = new CBSChannel(Mono.just(requestResponseChannel), tokenCredential,
            CBSAuthorizationType.JSON_WEB_TOKEN, options);

        when(tokenCredential.getToken(argThat(arg -> arg.getScopes().contains(scopes))))
            .thenReturn(Mono.just(accessToken));

        when(requestResponseChannel.sendWithAck(any())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(cbsChannel.authorize(tokenAudience, scopes))
            .expectNext(accessToken.getExpiresAt())
            .expectComplete()
            .verify(Duration.ofSeconds(10));

        // Assert
        verify(requestResponseChannel, times(1)).sendWithAck(any());
        verify(requestResponseChannel).sendWithAck(messageArgumentCaptor.capture());
        final Message message = messageArgumentCaptor.getValue();

        final Map<String, Object> properties = message.getApplicationProperties().getValue();
        Assertions.assertTrue(properties.containsKey(PUT_TOKEN_AUDIENCE), "'PUT_TOKEN_AUDIENCE' should be there.");
        Assertions.assertEquals(tokenAudience, properties.get(PUT_TOKEN_AUDIENCE));
        Assertions.assertEquals("jwt", properties.get(PUT_TOKEN_TYPE));
    }
}
