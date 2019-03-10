/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;

import java.util.HashMap;
import java.util.Map;

final class CBSChannel {

    final FaultTolerantObject<RequestResponseChannel> innerChannel;
    final SessionProvider sessionProvider;
    final AmqpConnection connectionEventDispatcher;

    public CBSChannel(
            final SessionProvider sessionProvider,
            final AmqpConnection connection) {

        this.sessionProvider = sessionProvider;
        this.connectionEventDispatcher = connection;

        RequestResponseCloser closer = new RequestResponseCloser();
        this.innerChannel = new FaultTolerantObject<>(
                new RequestResponseOpener(sessionProvider, "cbs-session", "cbs", ClientConstants.CBS_ADDRESS, connection),
                closer);
        closer.setInnerChannel(this.innerChannel);
    }

    public void sendToken(
            final ReactorDispatcher dispatcher,
            final String token,
            final String tokenAudience,
            final OperationResult<Void, Exception> sendTokenCallback) {

        final Message request = Proton.message();
        final Map<String, Object> properties = new HashMap<>();
        properties.put(ClientConstants.PUT_TOKEN_OPERATION, ClientConstants.PUT_TOKEN_OPERATION_VALUE);
        properties.put(ClientConstants.PUT_TOKEN_TYPE, ClientConstants.SAS_TOKEN_TYPE);
        properties.put(ClientConstants.PUT_TOKEN_AUDIENCE, tokenAudience);
        final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
        request.setApplicationProperties(applicationProperties);
        request.setBody(new AmqpValue(token));

        this.innerChannel.runOnOpenedObject(dispatcher,
                new OperationResult<RequestResponseChannel, Exception>() {
                    @Override
                    public void onComplete(final RequestResponseChannel result) {
                        result.request(request,
                                new OperationResult<Message, Exception>() {
                                    @Override
                                    public void onComplete(final Message response) {

                                        final int statusCode = (int) response.getApplicationProperties().getValue().get(ClientConstants.PUT_TOKEN_STATUS_CODE);
                                        final String statusDescription = (String) response.getApplicationProperties().getValue().get(ClientConstants.PUT_TOKEN_STATUS_DESCRIPTION);

                                        if (statusCode == AmqpResponseCode.ACCEPTED.getValue() || statusCode == AmqpResponseCode.OK.getValue()) {
                                            sendTokenCallback.onComplete(null);
                                        } else {
                                            this.onError(ExceptionUtil.amqpResponseCodeToException(statusCode, statusDescription));
                                        }
                                    }

                                    @Override
                                    public void onError(final Exception error) {
                                        sendTokenCallback.onError(error);
                                    }
                                });
                    }

                    @Override
                    public void onError(Exception error) {
                        sendTokenCallback.onError(error);
                    }
                });
    }

    public void close(
            final ReactorDispatcher reactorDispatcher,
            final OperationResult<Void, Exception> closeCallback) {

        this.innerChannel.close(reactorDispatcher, closeCallback);
    }
}
