// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CBSChannel extends EndpointStateNotifierBase implements CBSNode {
    static final String PUT_TOKEN_OPERATION = "operation";
    static final String PUT_TOKEN_OPERATION_VALUE = "put-token";
    static final String PUT_TOKEN_TYPE = "type";
    static final String PUT_TOKEN_AUDIENCE = "name";

    private final TokenCredential credential;
    private final Mono<RequestResponseChannel> cbsChannelMono;
    private final CBSAuthorizationType authorizationType;
    private final AmqpRetryOptions retryOptions;

    public CBSChannel(Mono<RequestResponseChannel> responseChannelMono, TokenCredential tokenCredential,
               CBSAuthorizationType authorizationType, AmqpRetryOptions retryOptions) {
        super(new ClientLogger(CBSChannel.class));

        this.authorizationType = Objects.requireNonNull(authorizationType, "'authorizationType' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.credential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        this.cbsChannelMono = Objects.requireNonNull(responseChannelMono, "'responseChannelMono' cannot be null.")
            .cache();
    }

    @Override
    public Mono<OffsetDateTime> authorize(String tokenAudience, String scopes) {
        final Message request = Proton.message();
        final Map<String, Object> properties = new HashMap<>();
        properties.put(PUT_TOKEN_OPERATION, PUT_TOKEN_OPERATION_VALUE);
        properties.put(PUT_TOKEN_TYPE, authorizationType.getTokenType());
        properties.put(PUT_TOKEN_AUDIENCE, tokenAudience);

        final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
        request.setApplicationProperties(applicationProperties);

        return credential.getToken(new TokenRequestContext().addScopes(scopes)).flatMap(accessToken -> {
            request.setBody(new AmqpValue(accessToken.getToken()));

            return cbsChannelMono.flatMap(x -> x.sendWithAck(request))
                .then(Mono.fromCallable(() -> accessToken.getExpiresAt()));
        });
    }

    @Override
    public void close() {
        final RequestResponseChannel channel = cbsChannelMono.block(retryOptions.getTryTimeout());
        if (channel != null) {
            channel.close();
        }
    }
}
