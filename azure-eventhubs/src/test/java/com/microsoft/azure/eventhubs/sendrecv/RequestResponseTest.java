/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.amqp.AmqpException;
import com.microsoft.azure.eventhubs.amqp.AmqpResponseCode;
import com.microsoft.azure.eventhubs.amqp.IOperation;
import com.microsoft.azure.eventhubs.amqp.IOperationResult;
import com.microsoft.azure.eventhubs.amqp.ReactorDispatcher;
import com.microsoft.azure.eventhubs.amqp.RequestResponseChannel;

import org.junit.Assert;
import junit.framework.AssertionFailedError;

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
        factory = MessagingFactory.createFromConnectionString(connectionString.toString(), TestContext.EXECUTOR_SERVICE).get();
    }
    
    @Test()
    public void testRequestResponse() throws Exception {
        
        final ReactorDispatcher dispatcher = factory.getReactorScheduler();
        final RequestResponseChannel requestResponseChannel = new RequestResponseChannel(
                                "reqresp", 
                                ClientConstants.MANAGEMENT_ADDRESS,
                                factory.getSession("path", null, null));
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
            properties.put(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY, connectionString.getEventHubName());
            properties.put(ClientConstants.MANAGEMENT_OPERATION_KEY, ClientConstants.READ_OPERATION_VALUE);
            final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
            request.setApplicationProperties(applicationProperties);

            fchannel.runOnOpenedObject(dispatcher, 
                new IOperationResult<RequestResponseChannel, Exception>() {
                    @Override
                    public void onComplete(RequestResponseChannel result) {
                        result.request(request,
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

                                    if (connectionString.getEventHubName().equalsIgnoreCase((String) resultMap.get(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY)))
                                        task.complete(null);
                                    else
                                        task.completeExceptionally(new AssertionFailedError("response doesn't have correct eventhub name"));
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
    
    @Test
    public void testGetRuntimes() throws Exception {
    	EventHubClient ehc = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
    	EventHubRuntimeInformation ehInfo = ehc.getRuntimeInformation().get();

    	Assert.assertNotNull(ehInfo);
    	Assert.assertTrue(connectionString.getEventHubName().equalsIgnoreCase(ehInfo.getPath()));
    	Assert.assertNotNull(ehInfo.getCreatedAt()); // creation time could be almost anything, can't really check value
    	Assert.assertTrue(ehInfo.getPartitionCount() >= 2); // max legal partition count is variable but 2 is hard minimum
    	Assert.assertEquals(ehInfo.getPartitionIds().length, ehInfo.getPartitionCount());
    	/*
    	System.out.println("Event hub name: " + ehInfo.getPath());
    	System.out.println("Created at: " + ehInfo.getCreatedAt().toString());
    	System.out.println("Partition count: " + ehInfo.getPartitionCount());
    	*/
    	for (int i = 0; i < ehInfo.getPartitionCount(); i++) {
    		String id = ehInfo.getPartitionIds()[i];
    		Assert.assertNotNull(id);
    		Assert.assertFalse(id.isEmpty());
    		//System.out.println("Partition id[" + i + "]: " + ehInfo.getPartitionIds()[i]);
    	}
    	
    	for (String id : ehInfo.getPartitionIds()) {
	    	EventHubPartitionRuntimeInformation partInfo = ehc.getPartitionRuntimeInformation(id).get();
	    	
	    	Assert.assertNotNull(partInfo);
	    	Assert.assertTrue(connectionString.getEventHubName().equalsIgnoreCase(partInfo.getEventHubPath()));
	    	Assert.assertTrue(id.equalsIgnoreCase(partInfo.getPartitionId()));
	    	Assert.assertTrue(partInfo.getBeginSequenceNumber() >= -1);
	    	Assert.assertTrue(partInfo.getLastEnqueuedSequenceNumber() >= -1);
	    	Assert.assertTrue(partInfo.getLastEnqueuedSequenceNumber() >= partInfo.getBeginSequenceNumber());
	    	Assert.assertNotNull(partInfo.getLastEnqueuedOffset());
	    	Assert.assertFalse(partInfo.getLastEnqueuedOffset().isEmpty());
	    	Assert.assertNotNull(partInfo.getLastEnqueuedTimeUtc());  // last enqueued time could be almost anything, can't really check value
	    	/*
	    	System.out.println("Event hub name: " + partInfo.getEventHubPath());
	    	System.out.println("Partition id: " + partInfo.getPartitionId());
	    	System.out.println("Begin seq: " + partInfo.getBeginSequenceNumber());
	    	System.out.println("Last seq: " + partInfo.getLastEnqueuedSequenceNumber());
	    	System.out.println("Last offset: " + partInfo.getLastEnqueuedOffset());
	    	System.out.println("Last time: " + partInfo.getLastEnqueuedTimeUtc().toString());
	    	*/
    	}
    	
    	ehc.closeSync();
    }
    
    @Test
    public void testGetRuntimesBadHub() throws EventHubException, IOException {
    	ConnectionStringBuilder bogusConnectionString = new ConnectionStringBuilder()
                .setEndpoint(connectionString.getEndpoint())
                .setEventHubName("NOHUBZZZZZ")
                .setSasKeyName(connectionString.getSasKeyName())
                .setSasKey(connectionString.getSasKey());
    	EventHubClient ehc = EventHubClient.createFromConnectionStringSync(bogusConnectionString.toString(), TestContext.EXECUTOR_SERVICE);
    	
    	try {
    		ehc.getRuntimeInformation().get();
    		Assert.fail("Expected exception, got success");
    	}
    	catch (ExecutionException e) {
    		if (e.getCause() == null) {
    			Assert.fail("Got ExecutionException but no inner exception");
    		}
    		else if (e.getCause() instanceof AmqpException) {
    			// TODO we should really be returning a MessagingEntityNotFound exception
    			// but that can be an enhancement for later, right now it's an AmqpException
    			Assert.assertTrue(e.getCause().getMessage().contains("could not be found"));
    		}
    		else {
    			Assert.fail("Got unexpected inner exception " + e.getCause().toString());
    		}
    	}
    	catch (Exception e) {
    		Assert.fail("Unexpected exception " + e.toString());
    	}
    	
    	try {
    		ehc.getPartitionRuntimeInformation("0").get();
    		Assert.fail("Expected exception, got success");
    	}
    	catch (ExecutionException e) {
    		if (e.getCause() == null) {
    			Assert.fail("Got ExecutionException but no inner exception");
    		}
    		else if (e.getCause() instanceof AmqpException) {
    			// TODO we should really be returning a MessagingEntityNotFound exception
    			// but that can be an enhancement for later, right now it's an AmqpException
    			Assert.assertTrue(e.getCause().getMessage().contains("could not be found"));
    		}
    		else {
    			Assert.fail("Got unexpected inner exception " + e.getCause().toString());
    		}
    	}
    	catch (Exception e) {
    		Assert.fail("Unexpected exception " + e.toString());
    	}
    	
    	ehc.closeSync();
    }
    
    @Test
    public void testGetRuntimesBadKeyname() throws EventHubException, IOException {
    	ConnectionStringBuilder bogusConnectionString = new ConnectionStringBuilder()
                .setEndpoint(connectionString.getEndpoint())
                .setEventHubName(connectionString.getEventHubName())
                .setSasKeyName("xxxnokeyxxx")
                .setSasKey(connectionString.getSasKey());
    	EventHubClient ehc = EventHubClient.createFromConnectionStringSync(bogusConnectionString.toString(), TestContext.EXECUTOR_SERVICE);
    	
    	try {
    		ehc.getRuntimeInformation().get();
    		Assert.fail("Expected exception, got success");
    	}
    	catch (ExecutionException e) {
    		if (e.getCause() == null) {
    			Assert.fail("Got ExecutionException but no inner exception");
    		}
    		else if (e.getCause() instanceof AuthorizationFailedException) {
    			// Success
    		}
    		else {
    			Assert.fail("Got unexpected inner exception " + e.getCause().toString());
    		}
    	}
    	catch (Exception e) {
    		Assert.fail("Unexpected exception " + e.toString());
    	}
    	
    	try {
    		ehc.getPartitionRuntimeInformation("0").get();
    		Assert.fail("Expected exception, got success");
    	}
    	catch (ExecutionException e) {
    		if (e.getCause() == null) {
    			Assert.fail("Got ExecutionException but no inner exception");
    		}
    		else if (e.getCause() instanceof AuthorizationFailedException) {
    			// Success
    		}
    		else {
    			Assert.fail("Got unexpected inner exception " + e.getCause().toString());
    		}
    	}
    	catch (Exception e) {
    		Assert.fail("Unexpected exception " + e.toString());
    	}
    	
    	ehc.closeSync();
    }
    
    @Test
    public void testGetRuntimesClosedClient() throws EventHubException, IOException, InterruptedException, ExecutionException {
    	EventHubClient ehc = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
    	ehc.closeSync();

    	try {
    		ehc.getRuntimeInformation().get();
    		Assert.fail("getRuntimeInformation did not throw as expected");
    	}
    	catch (IllegalStateException e) {
    		// Success
    	}
    	catch (Exception e) {
    		Assert.fail("Unexpected exception from getRuntimeInformation " + e.toString());
    	}

    	try {
    		ehc.getPartitionRuntimeInformation("0").get();
    		Assert.fail("getPartitionRuntimeInformation did not throw as expected");
    	}
    	catch (IllegalStateException e) {
    		// Success
    	}
    	catch (Exception e) {
    		Assert.fail("Unexpected exception from getPartitionRuntimeInformation " + e.toString());
    	}
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {

        if (factory != null)
            factory.closeSync();
    }
}
