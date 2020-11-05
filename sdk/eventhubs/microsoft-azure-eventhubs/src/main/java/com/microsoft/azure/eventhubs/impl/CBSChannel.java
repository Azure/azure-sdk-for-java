// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.eventhubs.SecurityToken;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

final class CBSChannel {

    final ScheduledExecutorService executor;
    final FaultTolerantObject<RequestResponseChannel> innerChannel;

    CBSChannel(
            final SessionProvider sessionProvider,
            final AmqpConnection connection,
            final String clientId,
            final ScheduledExecutorService executor) {
        this.executor = executor;
        RequestResponseCloser closer = new RequestResponseCloser();
        final String sessionName = "cbs-session";
        this.innerChannel = new FaultTolerantObject<>(
                new RequestResponseOpener(sessionProvider, clientId, sessionName, "cbs", ClientConstants.CBS_ADDRESS, connection, this.executor),
                closer, clientId, sessionName);
        closer.setInnerChannel(this.innerChannel);
    }

    public void sendToken(
            final ReactorDispatcher dispatcher,
            final CompletableFuture<SecurityToken> tokenFuture,
            final String tokenAudience,
            final OperationResult<Void, Exception> sendTokenCallback,
            final Consumer<Exception> errorCallback) {
        tokenFuture.thenAcceptAsync((token) -> {
            innerSendToken(dispatcher, token, tokenAudience, sendTokenCallback);
        }, this.executor)
            .whenCompleteAsync((empty, exception) -> {
            // TODO: whenCompleteAsync presents a Throwable. But many of the error callbacks expect
            // an Exception. For now, do a cast here. Will we ever actually get an error that is
            // not an Exception?
                if ((exception != null) && (exception instanceof Exception)) {
                    errorCallback.accept((Exception) exception);
                }
            }, this.executor);
    }

    private void innerSendToken(
            final ReactorDispatcher dispatcher,
            final SecurityToken token,
            final String tokenAudience,
            final OperationResult<Void, Exception> sendTokenCallback) {
        final Message request = Proton.message();
        final Map<String, Object> properties = new HashMap<>();
        properties.put(ClientConstants.PUT_TOKEN_OPERATION, ClientConstants.PUT_TOKEN_OPERATION_VALUE);
        properties.put(ClientConstants.PUT_TOKEN_TYPE, token.getTokenType());
        properties.put(ClientConstants.PUT_TOKEN_EXPIRY, token.validTo());
        properties.put(ClientConstants.PUT_TOKEN_AUDIENCE, tokenAudience);

        final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
        request.setApplicationProperties(applicationProperties);
        request.setBody(new AmqpValue(token.getToken()));

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
