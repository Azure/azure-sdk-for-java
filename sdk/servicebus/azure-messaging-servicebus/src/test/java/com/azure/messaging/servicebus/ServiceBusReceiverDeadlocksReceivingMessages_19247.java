package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceBusReceiverDeadlocksReceivingMessages_19247 {
    public static void main(String[] args) throws InterruptedException {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint=sb://sbtrack2-hemanttest-prototype.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=7uJdC9utZi6pxJ2trk4MmiiEyuHltIz1Oyejp1jZRgM=";
        // connectionString = "Endpoint=sb://company.servicebus.windows.net/;SharedAccessKeyName=SharedAccessKeyName=MySharedAccessKey;SharedAccessKey=1234";
        String topicName= "topic-2";
        String subscriptionName = "subs";
        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .disableAutoComplete()
            //.receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            //.prefetchCount(2)
            .maxAutoLockRenewDuration(Duration.ZERO) //disable auto lock renew
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

       /* ServiceBusSessionReceiverAsyncClient sessreceiver = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sessionReceiver()
                .queueName("session1-0")
                .buildAsyncClient();
        */

        //System.out.println("!!!! will get next new session ");
        //ServiceBusReceiverAsyncClient receiver =  sessreceiver.acceptSession("87608041-dce8-4d2d-bfd3-9514c086b8a0").block();
        //System.out.println("!!!! Session accepted" );
        AtomicInteger count =  new AtomicInteger();
        Disposable subscription = receiver.receiveMessages()
            .flatMap(message -> {
                count.incrementAndGet();
                System.out.printf(new Date()+   " " + count.get() +" !!!! Received Sequence #: %s. Contents: %s %n", message.getSequenceNumber(), message.getBody().toString());
                /*try {
                    Thread.sleep(1 * 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                //System.out.println(" !!!! After wait, complete the message.. ");
               return Mono.just(message);
                /*return receiver
                    .complete(message)
                    .onErrorResume(e -> {
                        System.out.println("!!!! Error in completing -> onErrorResume..");
                        e.printStackTrace();
                        return Mono.empty();
                    })
                    .then(Mono.just(true));*/
                            /*.onErrorResume(e -> {
                                System.out.println("!!!! Error in completing ..");
                                e.printStackTrace();
                                return Mono.empty();
                            });*/
            })
            .doOnNext(value ->  {
               // System.out.println(" !!!! Receiving  on Next " + value);
            })
            .doOnError(e -> System.out.println(" !!!! ERROR in receiving : " + e.getMessage()))
            .doOnComplete(() -> System.out.println(" !!!! Receiving  COMPLETE"))
            .doOnCancel(() -> {
                System.out.println(" !!!! Receiving doOnCancel ");
            })
            .subscribe(aBoolean -> {
                   // System.out.println(" !!!! subscriber value " + aBoolean);
                }, throwable -> {
                    System.out.println(" !!!! subscriber Error consumer " + throwable);
                },
                () -> {
                    System.out.println(" !!!! subscriber complete consumer ");
                });

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.MINUTES.sleep(5);
        System.out.println("!!!! finished ");
        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();
    }
}
