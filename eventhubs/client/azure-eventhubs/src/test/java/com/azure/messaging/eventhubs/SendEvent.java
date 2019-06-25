// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.ProxyConfiguration.SYSTEM_DEFAULTS;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SendEvent {

    @Test
    public void nullProxyConfiguration() {
        Assert.assertNull(SYSTEM_DEFAULTS.authentication());
        Assert.assertNull(SYSTEM_DEFAULTS.credential());
        Assert.assertNull(SYSTEM_DEFAULTS.proxyAddress());
    }

    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
    private static final int NUMBER_OF_EVENTS = 10;


    EventData thirdEvent;


    @Test
    public void receiveEvent() throws InterruptedException, IOException {
        CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint=sb://event-hubs-1.servicebus.windows.net/;SharedAccessKeyName=root;SharedAccessKey=gDdTyGFLiDNbwPZPKJtRzTz4c59GRBBPoT5hzaxT1Eg=;EntityPath=conniey-test";
//            "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";

        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.latest() tells the
        // service we only want events that are sent to the partition after we begin listening.
        EventHubConsumer consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            firstPartition, EventPosition.latest());

        // We start receiving any events that come from `firstPartition`, print out the contents, and decrement the
        // countDownLatch.
        Disposable subscription = consumer.receive().subscribe(event -> {
            String contents = UTF_8.decode(event.body()).toString();
            System.out.println(String.format("[%s] Sequence Number: %s. Contents: %s", countDownLatch.getCount(),
                event.offset(), contents));

            countDownLatch.countDown();
        });

        // Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
        // This creates a producer that only sends events to `firstPartition`.
        EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(firstPartition);
        EventHubProducer producer = client.createProducer(producerOptions);

        // We create 10 events to send to the service and block until the send has completed.
        Flux.range(0, NUMBER_OF_EVENTS).flatMap(number -> {
            String body = String.format("Hello world! Number: %s", number);
            return producer.send(new EventData(body.getBytes(UTF_8)));
        }).blockLast(OPERATION_TIMEOUT);

        // We wait for all the events to be received before continuing.
        countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

        // Dispose and close of all the resources we've created.
        subscription.dispose();
        producer.close();
        consumer.close();
        client.close();
    }

    @Test
    public void HelloWorld() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint=sb://event-hubs-1.servicebus.windows.net/;SharedAccessKeyName=root;SharedAccessKey=gDdTyGFLiDNbwPZPKJtRzTz4c59GRBBPoT5hzaxT1Eg=;EntityPath=conniey-test";

        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // Acquiring the semaphore so that this sample does not end before all the partition properties are fetched.
        semaphore.acquire();

        // Querying the partition identifiers for the Event Hub. Then calling client.getPartitionProperties with the
        // identifier to get information about each partition.
        client.getPartitionIds().flatMap(partitionId -> client.getPartitionProperties(partitionId)).subscribe(properties -> {
            System.out.println("The Event Hub has the following properties:");
            System.out.println(String.format(
                "Event Hub Name: %s; Partition Id: %s; Is partition empty? %s; First Sequence Number: %s; "
                + "Last Enqueued Time: %s; Last Enqueued Sequence Number: %s; Last Enqueued Offset: %s",
                properties.eventHubPath(), properties.id(), properties.isEmpty(),
                properties.beginningSequenceNumber(),
                properties.lastEnqueuedTime(),
                properties.lastEnqueuedSequenceNumber(),
                properties.lastEnqueuedOffset()));
        }, error -> {
            System.err.println("Error occurred while fetching partition properties: " + error.toString());
        }, () -> {
            // Releasing the semaphore now that we've finished querying for partition properties.
            semaphore.release();

            // Close the client resource
            client.close();
        });

        System.out.println("Waiting for partition properties to complete...");
        semaphore.acquire();
        System.out.println("Finished.");
    }

    @Test
    public void sentEventLIstWithProducerOptions() {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint=sb://event-hubs-1.servicebus.windows.net/;SharedAccessKeyName=root;SharedAccessKey=gDdTyGFLiDNbwPZPKJtRzTz4c59GRBBPoT5hzaxT1Eg=;EntityPath=conniey-test";

        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // When an Event Hub producer is associated with any specific partition, it can publish events only to that partition.
        // The producer has no ability to ask for the service to route events, including by using a partition key.
        //
        // If you attempt to use a partition key with an Event Hub producer that is associated with a partition, an exception
        // will occur. Otherwise, publishing to a specific partition is exactly the same as other publishing scenarios.
        EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(firstPartition);

        // Create a producer. Consequently, events sent from this producer will deliver to the specific partition ID Event Hub instance.
        EventHubProducer producer = client.createProducer(producerOptions);

        // Create an event list to send.
        List<EventData> dataList = new ArrayList<>();
        dataList.add(new EventData("EventData Sample 1".getBytes(UTF_8)));
        dataList.add(new EventData("EventData Sample 2 ".getBytes(UTF_8)));
        dataList.add(new EventData("EventData Sample 3".getBytes(UTF_8)));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(dataList).subscribe(
            (ignored) -> System.out.println("Event sent.")
            , error -> {
                System.err.println("There was an error sending the event: " + error.toString());

                if (error instanceof AmqpException) {
                    AmqpException amqpException = (AmqpException) error;

                    System.err.println(String.format("Is send operation retriable? %s. Error condition: %s",
                        amqpException.isTransient(), amqpException.getErrorCondition()));
                }
            }, () -> {
                // Disposing of our producer and client.
                try {
                    producer.close();
                } catch (IOException e) {
                    System.err.println("Error encountered while closing producer: " + e.toString());
                }

                client.close();
            });
    }

    @Test
    public void sentSmallEventBatchWithSendOptions() {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint=sb://event-hubs-1.servicebus.windows.net/;SharedAccessKeyName=root;SharedAccessKey=gDdTyGFLiDNbwPZPKJtRzTz4c59GRBBPoT5hzaxT1Eg=;EntityPath=conniey-test";

        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // Create a producer. This overload of `createProducer` does not accept any arguments
        EventHubProducer producer = client.createProducer();

        // We will publish a small batch of events based on simple sentences.
        List<EventData> dataList = new ArrayList<>();
        dataList.add(new EventData("EventData Sample 1".getBytes(UTF_8)));
        dataList.add(new EventData("EventData Sample 2 ".getBytes(UTF_8)));
        dataList.add(new EventData("EventData Sample 3".getBytes(UTF_8)));

        // Replace it with the partition ID you known. By list all partition IDs, you can use
        // String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT)
        // to find the first partition ID information belong to the hub
        final String partitionID = "0";

        // When an Event Hub producer is not associated with any specific partition, it may be desirable to request that
        // the Event Hubs service keep different events or batches of events together on the same partition. This can be
        // accomplished by setting a partition key when publishing the events.
        //
        // The partition key is NOT the identifier of a specific partition. Rather, it is an arbitrary piece of string data
        // that Event Hubs uses as the basis to compute a hash value. Event Hubs will associate the hash value with a specific
        // partition, ensuring that any events published with the same partition key are rerouted to the same partition.
        //
        // Note that there is no means of accurately predicting which partition will be associated with a given partition key;
        // we can only be assured that it will be a consistent choice of partition. If you have a need to understand which
        // exact partition an event is published to, you will need to use an Event Hub producer associated with that partition.
        SendOptions sendOptions = new SendOptions().partitionKey(partitionID);

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(dataList, sendOptions).subscribe(
            (ignored) -> System.out.println("A list of event sent to specific partition, partition ID = " + partitionID),
            error -> {
                System.err.println("There was an error sending the event batch: " + error.toString());

                if (error instanceof AmqpException) {
                    AmqpException amqpException = (AmqpException) error;

                    System.err.println(String.format("Is send operation retriable? %s. Error condition: %s",
                        amqpException.isTransient(), amqpException.getErrorCondition()));
                }
            }, () -> {
                // Disposing of our producer and client.
                try {
                    producer.close();
                } catch (IOException e) {
                    System.err.println("Error encountered while closing producer: " + e.toString());
                }

                client.close();
            });
    }


    @Test
    public void sendCustomEventDataList() {

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint=sb://event-hubs-1.servicebus.windows.net/;SharedAccessKeyName=root;SharedAccessKey=gDdTyGFLiDNbwPZPKJtRzTz4c59GRBBPoT5hzaxT1Eg=;EntityPath=conniey-test";


        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // Create a producer. This overload of `createProducer` does not accept any arguments
        EventHubProducer producer = client.createProducer();

        // Because an event consists mainly of an opaque set of bytes, it may be difficult for consumers of those events to
        // make informed decisions about how to process them.
        //
        // In order to allow event publishers to offer better context for consumers, event data may also contain custom metadata,
        // in the form of a set of key/value pairs.  This metadata is not used by, or in any way meaningful to, the Event Hubs
        // service; it exists only for coordination between event publishers and consumers.
        //
        // One common scenario for the inclusion of metadata is to provide a hint about the type of data contained by an event,
        // so that consumers understand its format and can deserialize it appropriately.
        //
        // We will publish a small batch of events based on simple sentences, but will attach some custom metadata with
        // pretend type names and other hints.  Note that the set of metadata is unique to an event; there is no need for every

        // event in a batch to have the same metadata properties available nor the same data type for those properties.
        List<EventData> dataList = new ArrayList<>();
        EventData firstEvent = new EventData("EventData Sample 1".getBytes(UTF_8));
        firstEvent.properties().put("EventType", "com.microsoft.samples.hello-event");
        firstEvent.properties().put("priority", 1);
        firstEvent.properties().put("score", 9.0);

        EventData secEvent = new EventData("EventData Sample 2 ".getBytes(UTF_8));
        secEvent.properties().put("EventType", "com.microsoft.samples.goodbye-event");
        secEvent.properties().put("priority", "17");
        secEvent.properties().put("blob", true);

        dataList.add(firstEvent);
        dataList.add(secEvent);

        // Replace it with the partition ID you known. By list all partition IDs, you can use
        // String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT)
        // to find the first partition ID information belong to the hub
        final String partitionID = "0";

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(dataList).subscribe(
            (ignored) -> System.out.println("Event sent to specific partition, ID = " + partitionID),
            error -> {
                System.err.println("There was an error sending the event batch: " + error.toString());

                if (error instanceof AmqpException) {
                    AmqpException amqpException = (AmqpException) error;

                    System.err.println(String.format("Is send operation retriable? %s. Error condition: %s",
                        amqpException.isTransient(), amqpException.getErrorCondition()));
                }
            }, () -> {
                // Disposing of our producer and client.
                try {
                    producer.close();
                } catch (IOException e) {
                    System.err.println("Error encountered while closing producer: " + e.toString());
                }

                client.close();
            });
    }

    @Test
    public void consumeEventsByBatch() throws InterruptedException, IOException {
        CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
//        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";
        String connectionString = "Endpoint=sb://event-hubs-1.servicebus.windows.net/;SharedAccessKeyName=root;SharedAccessKey=gDdTyGFLiDNbwPZPKJtRzTz4c59GRBBPoT5hzaxT1Eg=;EntityPath=conniey-test";

        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.latest() tells the
        // service we only want events that are sent to the partition after we begin listening.
        EventHubConsumer consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            firstPartition, EventPosition.latest());

        // We start receiving any events that come from `firstPartition`, print out the contents, and decrement the
        // countDownLatch.
        Disposable subscription = consumer.receive().subscribe(event -> {
            String contents = UTF_8.decode(event.body()).toString();
            System.out.println(String.format("[%s] Sequence Number: %s. Contents: %s", countDownLatch.getCount(),
                event.sequenceNumber(), contents));

            countDownLatch.countDown();
        });

        // Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
        // This creates a producer that only sends events to `firstPartition`.
        EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(firstPartition);
        EventHubProducer producer = client.createProducer(producerOptions);

        // Crate 10 events
        final int eventBatchSize = 10;
        ArrayList<EventData> events = new ArrayList<>(eventBatchSize);
        for (int i = 0; i < eventBatchSize; i++) {
            events.add(new EventData(UTF_8.encode("I am Event " + i)));
        }

        // We create 10 events to send to the service and block until the send has completed.
        producer.send(events).block(OPERATION_TIMEOUT);
        // We wait for all the events to be received before continuing.
        countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

        // Dispose and close of all the resources we've created.
        subscription.dispose();
        producer.close();
        consumer.close();
        client.close();
    }

    @Test
    public void consumeEventsByBatchFromVaryPosition() throws InterruptedException, IOException {
        int eventBatchSize = 10;
        CountDownLatch countDownLatch = new CountDownLatch(eventBatchSize);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
//        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";
        String connectionString = "Endpoint=sb://event-hubs-1.servicebus.windows.net/;SharedAccessKeyName=root;SharedAccessKey=gDdTyGFLiDNbwPZPKJtRzTz4c59GRBBPoT5hzaxT1Eg=;EntityPath=conniey-test";

        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);


        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.latest() tells the
        // service we only want events that are sent to the partition after we begin listening.
        EventHubConsumer consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            firstPartition, EventPosition.latest());

        // We start receiving any events that come from `firstPartition`, print out the contents, and decrement the
        // countDownLatch.
        Disposable subscription = consumer.receive().subscribe(event -> {
            String contents = UTF_8.decode(event.body()).toString();
            if (countDownLatch.getCount() == 3) {
                thirdEvent = event;
            }
            System.out.println(String.format("[%s] Sequence Number: %s. Contents: %s", countDownLatch.getCount(),
                event.sequenceNumber(), contents));

            countDownLatch.countDown();
        });

        // Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
        // This creates a producer that only sends events to `firstPartition`.
        EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(firstPartition);
        EventHubProducer producer = client.createProducer(producerOptions);

        ArrayList<EventData> events = new ArrayList<>(eventBatchSize);
        for (int i = 0; i < eventBatchSize; i++) {
            events.add(new EventData(UTF_8.encode("I am Event " + i)));
        }

        // We create 100 events to send to the service and block until the send has completed.
        producer.send(events).block(OPERATION_TIMEOUT);

        // We wait for all the events to be received before continuing.
        countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

        System.out.println("Third Event's sequence number is " + thirdEvent.sequenceNumber());

        Semaphore semaphore = new Semaphore(1);
        // Acquiring the semaphore so that this sample does not end before all the partition properties are fetched.
        semaphore.acquire();

        // Customs the sequence number event position.
        // If Inclusive is true, the event with the sequenceNumber is included; otherwise, the next event will be received.
        EventPosition exclusiveSequenceNumberPosition = EventPosition.fromSequenceNumber(thirdEvent.sequenceNumber(), false);

        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.fromSequenceNumber() tells the
        // service we only want events that are after the sequence number to the partition after we begin listening.
        EventHubConsumer newConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, firstPartition,
            exclusiveSequenceNumberPosition);

        Disposable newSubscription = newConsumer.receive().subscribe(event -> {
            String contents = UTF_8.decode(event.body()).toString();
            System.out.println(String.format("Sequence Number: %s. Contents: %s", event.sequenceNumber(), contents));
            // Releasing the semaphore now that we've finished querying for partition properties.
            semaphore.release();
        });

        semaphore.acquire();

        // Dispose and close of all the resources we've created.
        subscription.dispose();
        newSubscription.dispose();
        consumer.close();
        newConsumer.close();
        producer.close();
        client.close();
    }
}
