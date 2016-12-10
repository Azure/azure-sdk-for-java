/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import com.microsoft.azure.servicebus.amqp.AmqpException;
import com.microsoft.azure.servicebus.amqp.IAmqpConnection;
import com.microsoft.azure.servicebus.amqp.IOperation;
import com.microsoft.azure.servicebus.amqp.IOperationResult;
import com.microsoft.azure.servicebus.amqp.ReactorDispatcher;
import com.microsoft.azure.servicebus.amqp.RequestResponseChannel;
import com.microsoft.azure.servicebus.amqp.AmqpResponseCode;

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

        this.innerChannel = new FaultTolerantObject<>(
                                new OpenRequestResponseChannel(),
                                new CloseRequestResponseChannel());
    }

    public void sendToken(
            final ReactorDispatcher dispatcher,
            final String token,
            final String tokenAudience,
            final IOperationResult<Void, Exception> sendTokenCallback) {
        
        final Message request= Proton.message();
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
                                    }
                                    else {
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
    
    private class OpenRequestResponseChannel implements IOperation<RequestResponseChannel> {
        @Override
        public void run(IOperationResult<RequestResponseChannel, Exception> operationCallback) {

            final RequestResponseChannel requestResponseChannel = new RequestResponseChannel(
                "cbs", 
                ClientConstants.CBS_ADDRESS,
                CBSChannel.this.sessionProvider.getSession(
                    "cbs-session",
                    null,
                    new Consumer<ErrorCondition>() {
                        @Override
                        public void accept(ErrorCondition error) {
                            operationCallback.onError(new AmqpException(error));
                        }
                    }));

            requestResponseChannel.open(
                new IOperationResult<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        connectionEventDispatcher.registerForConnectionError(requestResponseChannel.getSendLink());
                        connectionEventDispatcher.registerForConnectionError(requestResponseChannel.getReceiveLink());

                        operationCallback.onComplete(requestResponseChannel);
                    }
                    @Override
                    public void onError(Exception error) {
                        operationCallback.onError(error);
                    }
                },
                new IOperationResult<Void, Exception>() {
                @Override
                public void onComplete(Void result) {
                    connectionEventDispatcher.deregisterForConnectionError(requestResponseChannel.getSendLink());
                    connectionEventDispatcher.deregisterForConnectionError(requestResponseChannel.getReceiveLink());
                }
                @Override
                public void onError(Exception error) {
                    connectionEventDispatcher.deregisterForConnectionError(requestResponseChannel.getSendLink());
                    connectionEventDispatcher.deregisterForConnectionError(requestResponseChannel.getReceiveLink());
                }
            });
        }
    }

    private class CloseRequestResponseChannel implements IOperation<Void> {

        @Override
        public void run(IOperationResult<Void, Exception> closeOperationCallback) {
            
            final RequestResponseChannel channelToBeClosed = innerChannel.unsafeGetIfOpened();
            if (channelToBeClosed == null) {
             
                closeOperationCallback.onComplete(null);
            }
            else {
                
                channelToBeClosed.close(new IOperationResult<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        closeOperationCallback.onComplete(result);
                    }

                    @Override
                    public void onError(Exception error) {
                        closeOperationCallback.onError(error);
                    }
                });
            }            
        }        
    }
}
