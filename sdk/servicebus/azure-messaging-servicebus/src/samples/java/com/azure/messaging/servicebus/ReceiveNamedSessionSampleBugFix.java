package com.azure.messaging.servicebus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiveNamedSessionSampleBugFix{

    /**
     * Main method to invoke this demo on how to receive messages from a session with id "greetings" in an Azure Service
     * Bus Queue.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint=sb://sbtrack2-hemanttest-prototype.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=7uJdC9utZi6pxJ2trk4MmiiEyuHltIz1Oyejp1jZRgM=";
        String queueName = "session-0";
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

        // Instantiate a client that will be used to send messages.
        ServiceBusSenderAsyncClient sender = builder
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        // session 1
        sender.createMessageBatch().flatMap(batch -> {
            for(int i =0 ; i < 5;++i) {
                ServiceBusMessage message = new ServiceBusMessage("dummy message ");
                batch.tryAddMessage(message.setMessageId((i+1) + "").setSessionId("1"));
            }
            System.out.println(new Date() + " Sender : sending total message : " + batch.getCount());

            return sender.sendMessages(batch);
        })
            .subscribeOn(Schedulers.parallel())
            .subscribe(unused -> {
                },
                error -> System.err.println("Error occurred while sending batch:" + error),
                () -> System.out.println("Send complete."));

       int delayInSeconds = 100;
        Flux.interval(Duration.ofSeconds(delayInSeconds))
            .take(1)
            .map(aLong -> {
                int ID = 6;
                System.out.println(new Date() + " Sender after "+delayInSeconds+" second: session id = 1  creating message with ID : "+ ID);
                ServiceBusMessage message = new ServiceBusMessage("dummy message " +ID);
                return sender.sendMessage(message.setMessageId(ID+"").setSessionId("1")).block(Duration.ofSeconds(3));
            }).subscribe(voidMono -> {
            System.out.println(new Date() + " Sender : "+delayInSeconds+" second Message sent : "+ voidMono);
        });
        /*
        Flux.interval(Duration.ofSeconds(1))
            .take(1)
            .map(aLong -> {
                int ID = 6;
                System.out.println(new Date() + " Sender after "+delayInSeconds+" second: session id = 2 creating message with ID : "+ ID);
                ServiceBusMessage message = new ServiceBusMessage("dummy message " +ID);
                return sender.sendMessage(message.setMessageId(ID+"").setSessionId("2")).block(Duration.ofSeconds(3));
            }).subscribe(voidMono -> {
            System.out.println(new Date() + " Sender : "+delayInSeconds+" second Message sent : "+ voidMono);
        });*/
        
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            //.retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(70)))
            .sessionProcessor()
            .queueName(queueName)
            .maxConcurrentSessions(1)
            .processMessage(ReceiveNamedSessionSampleBugFix::processMessage)
            .processError(ReceiveNamedSessionSampleBugFix::processError)
            .buildProcessorClient();

        System.out.println("Starting the processor");
        processorClient.start();

        TimeUnit.SECONDS.sleep(60 * 15);
        //System.out.println("Stopping the processor");
        //processorClient.stop();


        System.out.println("!!!! finished ");
        // Disposing of the subscription will cancel the receive() operation.
        processorClient.close();

        // Close the receiver.
    }

    private static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        System.out.printf(new Date() + " Processing message. Session: %s, Sequence #: %s, Message Id #: %s. Contents: %s :",  message.getSessionId() ,  message.getSequenceNumber() ,  message.getMessageId() ,  message.getBody());

        // When this message function completes, the message is automatically completed. If an exception is
        // thrown in here, the message is abandoned.
        // To disable this behaviour, toggle ServiceBusSessionProcessorClientBuilder.disableAutoComplete()
        // when building the session receiver.
    }

    /**
     * Processes an exception that occurred in the Service Bus Processor.
     *
     * @param context Context around the exception that occurred.
     */
    private static void processError(ServiceBusErrorContext context) {
        System.out.printf(new Date() +  " Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
            context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        System.out.printf("ServiceBusException source: %s. Reason: %s. Is transient? %s%n", context.getErrorSource(),
            exception.getReason(), exception.isTransient());
    }


    private static boolean processMessage(ServiceBusReceivedMessage message) {
        System.out.println("Processing message: " + message.getMessageId());
        return true;
    }
}
