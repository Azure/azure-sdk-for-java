// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.implementation.ExceptionUtil.amqpResponseCodeToException;
import static com.azure.core.amqp.implementation.RequestResponseUtils.getStatusCode;
import static com.azure.core.amqp.implementation.RequestResponseUtils.getStatusDescription;

public class ClaimsBasedSecurityChannel implements ClaimsBasedSecurityNode {
    static final String PUT_TOKEN_TYPE = "type";
    static final String PUT_TOKEN_AUDIENCE = "name";
    static final String PUT_TOKEN_EXPIRY = "expiration";
    private static final String PUT_TOKEN_OPERATION = "operation";
    private static final String PUT_TOKEN_OPERATION_VALUE = "put-token";

    private final TokenCredential credential;
    private final Mono<RequestResponseChannel> cbsChannelMono;
    private final CbsAuthorizationType authorizationType;
    private final AmqpRetryOptions retryOptions;

    public ClaimsBasedSecurityChannel(Mono<RequestResponseChannel> responseChannelMono, TokenCredential tokenCredential,
        CbsAuthorizationType authorizationType, AmqpRetryOptions retryOptions) {

        this.authorizationType = Objects.requireNonNull(authorizationType, "'authorizationType' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.credential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        this.cbsChannelMono = Objects.requireNonNull(responseChannelMono, "'responseChannelMono' cannot be null.");
    }

    @Override
    public Mono<OffsetDateTime> authorize(String tokenAudience, String scopes) {
        return cbsChannelMono.flatMap(channel ->
            credential.getToken(new TokenRequestContext().addScopes(scopes))
                .flatMap(accessToken -> {
                    final Message request = Proton.message();
                    final Map<String, Object> properties = new HashMap<>();
                    properties.put(PUT_TOKEN_OPERATION, PUT_TOKEN_OPERATION_VALUE);
                    properties.put(PUT_TOKEN_EXPIRY, Date.from(accessToken.getExpiresAt().toInstant()));
                    properties.put(PUT_TOKEN_TYPE, authorizationType.toString());
                    properties.put(PUT_TOKEN_AUDIENCE, tokenAudience);

                    final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
                    request.setApplicationProperties(applicationProperties);
                    request.setBody(new AmqpValue(accessToken.getToken()));

                    return channel.sendWithAck(request)
                        .handle((Message message, SynchronousSink<OffsetDateTime> sink) -> {
                            if (RequestResponseUtils.isSuccessful(message)) {
                                sink.next(accessToken.getExpiresAt());
                            } else {
                                final String description = getStatusDescription(message);
                                final AmqpResponseCode statusCode = getStatusCode(message);
                                final Exception error = amqpResponseCodeToException(
                                    statusCode.getValue(), description, channel.getErrorContext());

                                sink.error(error);
                            }
                        })
                        .switchIfEmpty(Mono.error(new AmqpException(true, String.format(
                            "No response received from CBS node. tokenAudience: '%s'. scopes: '%s'",
                            tokenAudience, scopes), channel.getErrorContext())));
                }));
    }

    @Override
    public void close() {
        closeAsync().block(retryOptions.getTryTimeout());
    }

    @Override
    public Mono<Void> closeAsync() {
        return cbsChannelMono.flatMap(channel -> channel.closeAsync());
    }
}
