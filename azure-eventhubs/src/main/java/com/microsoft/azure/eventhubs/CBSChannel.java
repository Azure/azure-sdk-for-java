/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.Map;
import java.util.HashMap;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;

import com.microsoft.azure.eventhubs.amqp.IAmqpConnection;
import com.microsoft.azure.eventhubs.amqp.IOperationResult;
import com.microsoft.azure.eventhubs.amqp.ISessionProvider;
import com.microsoft.azure.eventhubs.amqp.ReactorDispatcher;
import com.microsoft.azure.eventhubs.amqp.RequestResponseChannel;
import com.microsoft.azure.eventhubs.amqp.RequestResponseCloser;
import com.microsoft.azure.eventhubs.amqp.RequestResponseOpener;
import com.microsoft.azure.eventhubs.amqp.AmqpResponseCode;

public class CBSChannel {

    final FaultTolerantObject<RequestResponseChannel> innerChannel;
    final ISessionProvider sessionProvider;
    final IAmqpConnection connectionEventDispatcher;

    public CBSChannel(
            final ISessionProvider sessionProvider,
            final IAmqpConnection connection,
            final String linkName) {

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
            final IOperationResult<Void, Exception> sendTokenCallback) {

        final Message request = Proton.message();
        final Map<String, Object> properties = new HashMap<>();
        properties.put(ClientConstants.PUT_TOKEN_OPERATION, ClientConstants.PUT_TOKEN_OPERATION_VALUE);
        properties.put(ClientConstants.PUT_TOKEN_TYPE, ClientConstants.SAS_TOKEN_TYPE);
        properties.put(ClientConstants.PUT_TOKEN_AUDIENCE, tokenAudience);
        final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
        request.setApplicationProperties(applicationProperties);
        request.setBody(new AmqpValue(token));

        this.innerChannel.runOnOpenedObject(dispatcher,
                new IOperationResult<RequestResponseChannel, Exception>() {
                    @Override
                    public void onComplete(final RequestResponseChannel result) {
                        result.request(dispatcher, request,
                                new IOperationResult<Message, Exception>() {
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
            final IOperationResult<Void, Exception> closeCallback) {

        this.innerChannel.close(reactorDispatcher, closeCallback);
    }
}
