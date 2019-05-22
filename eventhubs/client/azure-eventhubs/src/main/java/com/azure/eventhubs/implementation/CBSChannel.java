// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.ExceptionUtil;
import com.azure.core.implementation.logging.ServiceLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class CBSChannel implements CBSNode {
    private static final String SESSION_NAME = "cbs-session";
    private static final String LINK_NAME = "cbs";
    private static final String CBS_ADDRESS = "$cbs";
    private static final String PUT_TOKEN_OPERATION = "operation";
    private static final String PUT_TOKEN_OPERATION_VALUE = "put-token";
    private static final String PUT_TOKEN_TYPE = "type";
    private static final String SAS_TOKEN_TYPE = "servicebus.windows.net:sastoken";
    private static final String PUT_TOKEN_AUDIENCE = "name";
    private static final String PUT_TOKEN_STATUS_CODE = "status-code";
    private static final String PUT_TOKEN_STATUS_DESCRIPTION = "status-description";

    private final ServiceLogger logger = new ServiceLogger(CBSChannel.class);
    private final ReactorConnection connection;
    private final TokenProvider tokenProvider;
    private final Mono<RequestResponseChannel> cbsChannelMono;
    private final ReactorDispatcher dispatcher;

    CBSChannel(ReactorConnection connection, TokenProvider tokenProvider, ReactorDispatcher dispatcher) {
        this.connection = connection;
        this.tokenProvider = tokenProvider;

        this.cbsChannelMono = connection.createSession(SESSION_NAME)
            .cast(ReactorSession.class)
            .map(session -> new RequestResponseChannel(connection.getProperties(), LINK_NAME, CBS_ADDRESS, session.session()));
        this.dispatcher = dispatcher;
    }

    @Override
    public Mono<Void> authorize(final String tokenAudience, final Duration duration) {
        final Message request = Proton.message();
        final Map<String, Object> properties = new HashMap<>();
        properties.put(PUT_TOKEN_OPERATION, PUT_TOKEN_OPERATION_VALUE);
        properties.put(PUT_TOKEN_TYPE, SAS_TOKEN_TYPE);
        properties.put(PUT_TOKEN_AUDIENCE, tokenAudience);
        final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
        request.setApplicationProperties(applicationProperties);

        final String token;
        try {
            token = tokenProvider.getToken(tokenAudience, duration);
        } catch (UnsupportedEncodingException ex) {
            return Mono.error(ex);
        }

        request.setBody(new AmqpValue(token));

        return cbsChannelMono.flatMap(x -> x.sendWithAck(request, dispatcher)).flatMap(response -> {
            final int statusCode = (int) response.getApplicationProperties().getValue()
                .get(PUT_TOKEN_STATUS_CODE);
            final String statusDescription = (String) response.getApplicationProperties().getValue()
                .get(PUT_TOKEN_STATUS_DESCRIPTION);

            if (statusCode != AmqpResponseCode.ACCEPTED.getValue() && statusCode != AmqpResponseCode.OK.getValue()) {
                return Mono.error(ExceptionUtil.amqpResponseCodeToException(statusCode, statusDescription));
            } else {
                return Mono.empty();
            }
        });
    }

    @Override
    public void close() {
        final RequestResponseChannel channel = cbsChannelMono.block(Duration.ofSeconds(60));
        if (channel != null) {
            channel.close();
        }

        if (!connection.removeSession(SESSION_NAME)) {
            logger.asInformational().log("Unable to remove CBSChannel {} from connection", SESSION_NAME);
        }
    }
}
