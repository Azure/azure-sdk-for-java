// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;

import java.util.HashMap;
import java.util.Map;

final class CBSChannel {

    final FaultTolerantObject<RequestResponseChannel> innerChannel;

    CBSChannel(
            final SessionProvider sessionProvider,
            final AmqpConnection connection,
            final String clientId) {

        RequestResponseCloser closer = new RequestResponseCloser();
        this.innerChannel = new FaultTolerantObject<>(
                new RequestResponseOpener(sessionProvider, clientId, "cbs-session", "cbs", ClientConstants.CBS_ADDRESS, connection),
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

        final MessageOperationResult messageOperation = new MessageOperationResult(response -> sendTokenCallback.onComplete(null), sendTokenCallback::onError);
        final OperationResultBase<RequestResponseChannel, Exception> operation = new OperationResultBase<>(
            result -> result.request(request, messageOperation),
            sendTokenCallback::onError);

        this.innerChannel.runOnOpenedObject(dispatcher, operation);
    }

    public void close(
            final ReactorDispatcher reactorDispatcher,
            final OperationResult<Void, Exception> closeCallback) {

        this.innerChannel.close(reactorDispatcher, closeCallback);
    }
}
