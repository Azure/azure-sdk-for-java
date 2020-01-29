package com.azure.messaging.servicebus;


import com.azure.core.amqp.exception.AmqpException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AsyncSenderTest {

    @Rule
    public TestName testName = new TestName();

    @Captor
    ArgumentCaptor<com.azure.core.amqp.EventData> singleMessageCaptor;

    private static String TEST_CONTENTS = "Hello World Azure Servicebus !!";
    protected String testName() {
        return testName.getMethodName();
    }

    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub.
     */
    @Test
    public void testPublishSingleMessage() throws Exception{
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        connectionString = "Endpoint=sb://sbtrack2-hemanttest-prototype.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=7uJdC9utZi6pxJ2trk4MmiiEyuHltIz1Oyejp1jZRgM=;EntityPath=hemant-test1";


        // Queue : https://azure-sdk-java-development-local.servicebus.windows.net/unit-test-temporary
        // Instantiate a client that will be used to call the service.
        SenderAsyncClient asyncSender = new QueueClientBuilder()
            .connectionString(connectionString)
            .fullyQualifiedNamespace("sbtrack2-hemanttest-prototype")
            .entityPath("hemant-test1")
            .buildAsyncClient();

        // Create an event to send.
        EventData data = new EventData("Hello world!".getBytes(UTF_8));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.

        asyncSender.send(data)
            .doOnError((error) -> {
                System.out.println("doOnError = "+error);
                error.printStackTrace();;
            })
            .subscribe(
                (response) -> System.out.println("Message sent. "+response),
                error -> {

                    System.out.println("There was an error sending the event: " + error.toString());
                    error.printStackTrace();
                    if (error instanceof AmqpException) {
                        AmqpException amqpException = (AmqpException) error;

                        System.err.println(String.format("Is send operation retriable? %s. Error condition: %s",
                            amqpException.isTransient(), amqpException.getErrorCondition()));
                    }
                }, () -> {
                    // Disposing of our producer and client.
                    System.out.println("Disposing of our producer and client. ");
                    try {
                        asyncSender.close();
                    } catch (Exception e) {
                        System.err.println("Error encountered while closing producer: " + e.toString());
                    }

                    asyncSender.close();
                });
        Thread.sleep(1000 *10);
        // Arrange
        //final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));

        //when(asyncSender.send(any(EventData.class))).thenReturn(Mono.empty());

        //final SendOptions options = new SendOptions();
        //final EventHubProducerOptions producerOptions = new EventHubProducerOptions()
        //    .retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        //final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());
        //final EventHubAsyncProducer producer = new EventHubAsyncProducer(Mono.just(sendLink), producerOptions, tracerProvider);

        // Act
        //StepVerifier.create(asyncSender.send(testData)).verifyComplete();

        // Assert
        //verify(asyncSender, times(1)).send(any(EventData.class));
        //verify(asyncSender).send(singleMessageCaptor.capture());

        //final EventData event = singleMessageCaptor.getValue();



    }
}
