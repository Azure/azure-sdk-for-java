/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ClientConstants;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.FaultTolerantObject;
import com.microsoft.azure.servicebus.MessagingFactory;
import com.microsoft.azure.servicebus.ServiceBusException;
import com.microsoft.azure.servicebus.amqp.AmqpException;
import com.microsoft.azure.servicebus.amqp.AmqpResponseCode;
import com.microsoft.azure.servicebus.amqp.IOperation;
import com.microsoft.azure.servicebus.amqp.IOperationResult;
import com.microsoft.azure.servicebus.amqp.ReactorDispatcher;
import com.microsoft.azure.servicebus.amqp.RequestResponseChannel;
import java.util.concurrent.ExecutionException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RequestResponseTest  extends ApiTestBase {
    
    static MessagingFactory factory;
    static ConnectionStringBuilder connectionString;
    
    @BeforeClass
    public static void initializeEventHub()  throws Exception {

        connectionString = TestContext.getConnectionString();
        factory = MessagingFactory.createFromConnectionString(connectionString.toString()).get();
    }
    
    @Test()
    public void testRequestResponse() throws Exception {
        
        final ReactorDispatcher dispatcher = factory.getReactorScheduler();
        final RequestResponseChannel requestResponseChannel = new RequestResponseChannel(
                                "reqresp", 
                                ClientConstants.MANAGEMENT_ADDRESS,
                                factory.getSession("path", "sessionId", null, null));
        final FaultTolerantObject<RequestResponseChannel> fchannel = new FaultTolerantObject<>(
                new IOperation<RequestResponseChannel>() {
                    @Override
                    public void run(IOperationResult<RequestResponseChannel, Exception> operationCallback) {

                            requestResponseChannel.open(
                                new IOperationResult<Void, Exception>() {
                                    @Override
                                    public void onComplete(Void result) {
                                        factory.registerForConnectionError(requestResponseChannel.getSendLink());
                                        factory.registerForConnectionError(requestResponseChannel.getReceiveLink());

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
                                    factory.deregisterForConnectionError(requestResponseChannel.getSendLink());
                                    factory.deregisterForConnectionError(requestResponseChannel.getReceiveLink());
                                }
                                @Override
                                public void onError(Exception error) {
                                    factory.deregisterForConnectionError(requestResponseChannel.getSendLink());
                                    factory.deregisterForConnectionError(requestResponseChannel.getReceiveLink());
                                }
                            });
                    }
            },
            new IOperation<Void>() {
            @Override
            public void run(IOperationResult<Void, Exception> operationCallback) {
                requestResponseChannel.close(new IOperationResult<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        operationCallback.onComplete(result);
                    }

                    @Override
                    public void onError(Exception error) {
                        operationCallback.onError(error);
                    }
                });
            }
        });
        
        int parallelization = 10;
        final CompletableFuture<Void>[] tasks = new CompletableFuture[parallelization];

        int i = 0;
        while (true)
        {
            final CompletableFuture<Void> task = new CompletableFuture<>();

            final Message request= Proton.message();
            final Map<String, String> properties = new HashMap<>();
            properties.put(ClientConstants.MANAGEMENT_ENTITY_TYPE_KEY, ClientConstants.MANAGEMENT_EVENTHUB_ENTITY_TYPE);
            properties.put(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY, connectionString.getEntityPath());
            properties.put(ClientConstants.MANAGEMENT_OPERATION_KEY, ClientConstants.READ_OPERATION_VALUE);
            final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
            request.setApplicationProperties(applicationProperties);

            fchannel.runOnOpenedObject(dispatcher, 
                new IOperationResult<RequestResponseChannel, Exception>() {
                    @Override
                    public void onComplete(RequestResponseChannel result) {
                        result.request(dispatcher, request,
                            new IOperationResult<Message, Exception>() {
                                @Override
                                public void onComplete(Message response) {
                                    Map<String, Object> resultMap = null;

                                    final int statusCode = (int) response.getApplicationProperties().getValue().get(ClientConstants.MANAGEMENT_STATUS_CODE_KEY);
                                    final String statusDescription = (String) response.getApplicationProperties().getValue().get(ClientConstants.MANAGEMENT_STATUS_DESCRIPTION_KEY);

                                    if (statusCode == AmqpResponseCode.ACCEPTED.getValue() || statusCode == AmqpResponseCode.OK.getValue()) {

                                        if (response.getBody() == null)
                                            resultMap = null;
                                        else
                                            resultMap = (Map<String, Object>) ((AmqpValue) response.getBody()).getValue();
                                    }
                                    else {

                                        final Symbol condition = (Symbol) response.getApplicationProperties().getValue().get(ClientConstants.MANAGEMENT_RESPONSE_ERROR_CONDITION);
                                        final ErrorCondition error = new ErrorCondition(condition, statusDescription);
                                        this.onError(new AmqpException(error));
                                    }

                                    task.complete(null);
                                }

                                @Override
                                public void onError(Exception error) {
                                    task.completeExceptionally(error);
                                }
                            });
                    }

                    @Override
                    public void onError(Exception error) {
                        task.completeExceptionally(error);
                    }
            });

            tasks[i % parallelization] = task;
            i++;
            if (i % parallelization == 0)
            {
                CompletableFuture.allOf(tasks).get();
                if (i >= (parallelization * 5))
                    break;
            }
        }
        
        final CompletableFuture<Void> closeFuture = new CompletableFuture<>();
        fchannel.close(dispatcher, new IOperationResult<Void, Exception>() {
            @Override
            public void onComplete(Void result) {
                closeFuture.complete(null);
            }

            @Override
            public void onError(Exception error) {
                closeFuture.completeExceptionally(error);
            }
        });
        
        closeFuture.get();
    }
    
    @AfterClass()
    public static void cleanup() throws ServiceBusException {

        if (factory != null)
            factory.closeSync();
    }
}
