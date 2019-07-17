// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.AuthorizationFailedException;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.IllegalEntityException;
import com.microsoft.azure.eventhubs.PartitionRuntimeInformation;
import com.microsoft.azure.eventhubs.TimeoutException;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.impl.AmqpException;
import com.microsoft.azure.eventhubs.impl.AmqpResponseCode;
import com.microsoft.azure.eventhubs.impl.ClientConstants;
import com.microsoft.azure.eventhubs.impl.EventHubClientImpl;
import com.microsoft.azure.eventhubs.impl.FaultTolerantObject;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.impl.Operation;
import com.microsoft.azure.eventhubs.impl.OperationResult;
import com.microsoft.azure.eventhubs.impl.ReactorDispatcher;
import com.microsoft.azure.eventhubs.impl.RequestResponseChannel;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import junit.framework.AssertionFailedError;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RequestResponseTest extends ApiTestBase {

    static MessagingFactory factory;
    static ConnectionStringBuilder connectionString;

    @BeforeClass
    public static void initializeEventHub() throws Exception {
        connectionString = TestContext.getConnectionString();
        factory = MessagingFactory.createFromConnectionString(connectionString.toString(), TestContext.EXECUTOR_SERVICE).get();
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {
        if (factory != null) {
            factory.closeSync();
        }
    }

    @Test()
    public void testRequestResponse() throws Exception {

        final ReactorDispatcher dispatcher = factory.getReactorDispatcher();
        final RequestResponseChannel requestResponseChannel = new RequestResponseChannel(
                "reqresp",
                ClientConstants.MANAGEMENT_ADDRESS,
                factory.getSession("path", null, null));
        final FaultTolerantObject<RequestResponseChannel> fchannel = new FaultTolerantObject<>(
                new Operation<RequestResponseChannel>() {
                    @Override
                    public void run(OperationResult<RequestResponseChannel, Exception> operationCallback) {

                        requestResponseChannel.open(
                                new OperationResult<Void, Exception>() {
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
                                new OperationResult<Void, Exception>() {
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
                new Operation<Void>() {
                    @Override
                    public void run(OperationResult<Void, Exception> operationCallback) {
                        requestResponseChannel.close(new OperationResult<Void, Exception>() {
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
        while (true) {
            final CompletableFuture<Void> task = new CompletableFuture<>();

            final Message request = Proton.message();
            final Map<String, Object> properties = new HashMap<>();
            properties.put(ClientConstants.MANAGEMENT_ENTITY_TYPE_KEY, ClientConstants.MANAGEMENT_EVENTHUB_ENTITY_TYPE);
            properties.put(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY, connectionString.getEventHubName());
            properties.put(ClientConstants.MANAGEMENT_OPERATION_KEY, ClientConstants.READ_OPERATION_VALUE);
            final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
            request.setApplicationProperties(applicationProperties);

            fchannel.runOnOpenedObject(dispatcher,
                    new OperationResult<RequestResponseChannel, Exception>() {
                        @Override
                        public void onComplete(RequestResponseChannel result) {
                            result.request(request,
                                    new OperationResult<Message, Exception>() {
                                        @Override
                                        public void onComplete(Message response) {
                                            Map<String, Object> resultMap = null;

                                            final int statusCode = (int) response.getApplicationProperties().getValue().get(ClientConstants.MANAGEMENT_STATUS_CODE_KEY);
                                            final String statusDescription = (String) response.getApplicationProperties().getValue().get(ClientConstants.MANAGEMENT_STATUS_DESCRIPTION_KEY);

                                            if (statusCode == AmqpResponseCode.ACCEPTED.getValue() || statusCode == AmqpResponseCode.OK.getValue()) {

                                                if (response.getBody() == null) {
                                                    resultMap = null;
                                                } else {
                                                    resultMap = (Map<String, Object>) ((AmqpValue) response.getBody()).getValue();
                                                }
                                            } else {

                                                final Symbol condition = (Symbol) response.getApplicationProperties().getValue().get(ClientConstants.MANAGEMENT_RESPONSE_ERROR_CONDITION);
                                                final ErrorCondition error = new ErrorCondition(condition, statusDescription);
                                                this.onError(new AmqpException(error));
                                            }

                                            if (connectionString.getEventHubName().equalsIgnoreCase((String) resultMap.get(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY))) {
                                                task.complete(null);
                                            } else {
                                                task.completeExceptionally(new AssertionFailedError("response doesn't have correct eventhub name"));
                                            }
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
            if (i % parallelization == 0) {
                CompletableFuture.allOf(tasks).get();
                if (i >= (parallelization * 5)) {
                    break;
                }
            }
        }

        final CompletableFuture<Void> closeFuture = new CompletableFuture<>();
        fchannel.close(dispatcher, new OperationResult<Void, Exception>() {
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
        testGetRuntimeInfos(TestContext.getConnectionString());
    }

    public void testGetRuntimeInfos(ConnectionStringBuilder connectionString) throws Exception {
        EventHubClient ehc = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        EventHubRuntimeInformation ehInfo = ehc.getRuntimeInformation().get();

        Assert.assertNotNull(ehInfo);
        Assert.assertTrue(connectionString.getEventHubName().equalsIgnoreCase(ehInfo.getPath()));
        Assert.assertNotNull(ehInfo.getCreatedAt()); // creation time could be almost anything, can't really check value
        Assert.assertTrue(ehInfo.getPartitionCount() >= 1); // max legal partition count is variable but 2 is hard minimum
        Assert.assertEquals(ehInfo.getPartitionIds().length, ehInfo.getPartitionCount());

        for (int i = 0; i < ehInfo.getPartitionCount(); i++) {
            String id = ehInfo.getPartitionIds()[i];
            Assert.assertNotNull(id);
            Assert.assertFalse(id.isEmpty());
            //System.out.println("Partition id[" + i + "]: " + ehInfo.getPartitionIds()[i]);
        }

        for (String id : ehInfo.getPartitionIds()) {
            PartitionRuntimeInformation partInfo = ehc.getPartitionRuntimeInformation(id).get();

            Assert.assertNotNull(partInfo);
            Assert.assertTrue(connectionString.getEventHubName().equalsIgnoreCase(partInfo.getEventHubPath()));
            Assert.assertTrue(id.equalsIgnoreCase(partInfo.getPartitionId()));
            Assert.assertTrue(partInfo.getBeginSequenceNumber() >= -1);
            Assert.assertTrue(partInfo.getLastEnqueuedSequenceNumber() >= -1);
            Assert.assertTrue(partInfo.getLastEnqueuedSequenceNumber() >= partInfo.getBeginSequenceNumber());
            Assert.assertNotNull(partInfo.getLastEnqueuedOffset());
            Assert.assertFalse(partInfo.getLastEnqueuedOffset().isEmpty());
            Assert.assertNotNull(partInfo.getLastEnqueuedTimeUtc());  // last enqueued time could be almost anything, can't really check value
        }

        ehc.closeSync();
    }

    @Test
    public void testGetRuntimesWebSockets() throws Exception {
        ConnectionStringBuilder connectionStringBuilder = TestContext.getConnectionString();
        connectionStringBuilder.setTransportType(TransportType.AMQP_WEB_SOCKETS);
        testGetRuntimeInfos(connectionStringBuilder);
    }

    @Test
    public void testGetRuntimeInfoCallTimesout() throws Exception {
        final EventHubClientImpl eventHubClient = (EventHubClientImpl) EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);

        // set operation timeout to 5ms - so that the actual operation doesn't event start
        final Field factoryField = EventHubClientImpl.class.getDeclaredField("underlyingFactory");
        factoryField.setAccessible(true);
        final MessagingFactory factory = (MessagingFactory) factoryField.get(eventHubClient);

        final Field timeoutField = MessagingFactory.class.getDeclaredField("operationTimeout");
        timeoutField.setAccessible(true);
        final Duration originalTimeout = factory.getOperationTimeout();
        timeoutField.set(factory, Duration.ofMillis(ClientConstants.MGMT_CHANNEL_MIN_RETRY_IN_MILLIS));

        try {
            eventHubClient.getPartitionRuntimeInformation("0").get();
            Assert.assertTrue(false); // exception should be thrown
        } catch (ExecutionException exception) {
            Assert.assertTrue(exception.getCause() instanceof TimeoutException);
        } finally {
            timeoutField.set(factory, originalTimeout);
            eventHubClient.closeSync();
        }
    }

    @Test
    public void testGetRuntimesBadHub() throws EventHubException, IOException {
        ConnectionStringBuilder bogusConnectionString = new ConnectionStringBuilder()
                .setEndpoint(connectionString.getEndpoint())
                .setEventHubName("NOHUBZZZZZ")
                .setSasKeyName(connectionString.getSasKeyName())
                .setSasKey(connectionString.getSasKey());
        EventHubClient ehc = EventHubClient.createSync(bogusConnectionString.toString(), TestContext.EXECUTOR_SERVICE);

        try {
            ehc.getRuntimeInformation().get();
            Assert.fail("Expected exception, got success");
        } catch (ExecutionException e) {
            if (e.getCause() == null) {
                Assert.fail("Got ExecutionException but no inner exception");
            } else if (e.getCause() instanceof IllegalEntityException) {
                Assert.assertTrue(e.getCause().getMessage().contains("could not be found"));
            } else {
                Assert.fail("Got unexpected inner exception " + e.getCause().toString());
            }
        } catch (Exception e) {
            Assert.fail("Unexpected exception " + e.toString());
        }

        try {
            ehc.getPartitionRuntimeInformation("0").get();
            Assert.fail("Expected exception, got success");
        } catch (ExecutionException e) {
            if (e.getCause() == null) {
                Assert.fail("Got ExecutionException but no inner exception");
            } else if (e.getCause() instanceof IllegalEntityException) {
                Assert.assertTrue(e.getCause().getMessage().contains("could not be found"));
            } else {
                Assert.fail("Got unexpected inner exception " + e.getCause().toString());
            }
        } catch (Exception e) {
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
        EventHubClient ehc = EventHubClient.createSync(bogusConnectionString.toString(), TestContext.EXECUTOR_SERVICE);

        try {
            ehc.getRuntimeInformation().get();
            Assert.fail("Expected exception, got success");
        } catch (ExecutionException e) {
            Assert.assertNotNull(e.getCause());
            Assert.assertTrue(e.getCause() instanceof AuthorizationFailedException);
        } catch (Exception e) {
            Assert.fail("Unexpected exception " + e.toString());
        }

        try {
            ehc.getPartitionRuntimeInformation("0").get();
            Assert.fail("Expected exception, got success");
        } catch (ExecutionException e) {
            Assert.assertNotNull(e.getCause());
            Assert.assertTrue(e.getCause() instanceof AuthorizationFailedException);
        } catch (Exception e) {
            Assert.fail("Unexpected exception " + e.toString());
        }

        ehc.closeSync();
    }

    @Test
    public void testGetRuntimesClosedClient() throws EventHubException, IOException, InterruptedException, ExecutionException {
        EventHubClient ehc = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehc.closeSync();

        try {
            ehc.getRuntimeInformation().get();
            Assert.fail("getRuntimeInformation did not throw as expected");
        } catch (IllegalStateException e) {
            // Success
        } catch (Exception e) {
            Assert.fail("Unexpected exception from getRuntimeInformation " + e.toString());
        }

        try {
            ehc.getPartitionRuntimeInformation("0").get();
            Assert.fail("getPartitionRuntimeInformation did not throw as expected");
        } catch (IllegalStateException e) {
            // Success
        } catch (Exception e) {
            Assert.fail("Unexpected exception from getPartitionRuntimeInformation " + e.toString());
        }
    }
}
