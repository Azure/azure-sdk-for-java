// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

class CBSChannel extends EndpointStateNotifierBase implements CBSNode {
    static final String SESSION_NAME = "cbs-session";
    static final String CBS_ADDRESS = "$cbs";

    private static final String LINK_NAME = "cbs";
    private static final String PUT_TOKEN_OPERATION = "operation";
    private static final String PUT_TOKEN_OPERATION_VALUE = "put-token";
    private static final String PUT_TOKEN_TYPE = "type";
    private static final String PUT_TOKEN_TYPE_VALUE_FORMAT = "servicebus.windows.net:%s";
    private static final String PUT_TOKEN_AUDIENCE = "name";

    private final AmqpConnection connection;
    private final TokenCredential credential;
    private final Mono<RequestResponseChannel> cbsChannelMono;
    private final ReactorProvider provider;
    private final CBSAuthorizationType authorizationType;
    private final RetryOptions retryOptions;

    CBSChannel(AmqpConnection connection, TokenCredential tokenCredential, CBSAuthorizationType authorizationType,
               ReactorProvider provider, ReactorHandlerProvider handlerProvider, RetryOptions retryOptions) {
        super(new ClientLogger(CBSChannel.class));

        Objects.requireNonNull(connection);
        Objects.requireNonNull(tokenCredential);
        Objects.requireNonNull(authorizationType);
        Objects.requireNonNull(provider);
        Objects.requireNonNull(handlerProvider);
        Objects.requireNonNull(retryOptions);

        this.authorizationType = authorizationType;
        this.retryOptions = retryOptions;
        this.connection = connection;
        this.credential = tokenCredential;
        this.provider = provider;
        this.cbsChannelMono = connection.createSession(SESSION_NAME)
            .cast(ReactorSession.class)
            .map(session -> new RequestResponseChannel(connection.getIdentifier(), connection.getHost(), LINK_NAME,
                CBS_ADDRESS, session.session(), this.retryOptions, handlerProvider))
            .cache();
    }

    @Override
    public Mono<OffsetDateTime> authorize(final String tokenAudience) {
        final Message request = Proton.message();
        final Map<String, Object> properties = new HashMap<>();
        properties.put(PUT_TOKEN_OPERATION, PUT_TOKEN_OPERATION_VALUE);
        properties.put(PUT_TOKEN_TYPE, String.format(Locale.ROOT, PUT_TOKEN_TYPE_VALUE_FORMAT, authorizationType.getTokenType()));
        properties.put(PUT_TOKEN_AUDIENCE, tokenAudience);
        final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
        request.setApplicationProperties(applicationProperties);

        return credential.getToken(tokenAudience).flatMap(accessToken -> {
            request.setBody(new AmqpValue(accessToken.token()));

            return cbsChannelMono.flatMap(x -> x.sendWithAck(request, provider.getReactorDispatcher()))
                .then(Mono.fromCallable(() -> accessToken.expiresOn()));
        });
    }

    @Override
    public void close() {
        final RequestResponseChannel channel = cbsChannelMono.block(retryOptions.tryTimeout());
        if (channel != null) {
            channel.close();
        }

        if (!connection.removeSession(SESSION_NAME)) {
            logger.info("Unable to remove CBSChannel {} from connection", SESSION_NAME);
        }
    }
}
