package com.azure.messaging.servicebus;

import java.util.Date;

public class SendSyncTest {
    static String connectionString = "Endpoint=sb://sbtrack2-hemanttest-prototype.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=7uJdC9utZi6pxJ2trk4MmiiEyuHltIz1Oyejp1jZRgM=";

    // Create a Queue in that Service Bus namespace.
    static String queueName = "queue-0";//"session1-0";

    public static void main(String[] args) throws InterruptedException {
        String keyName = "topic1";

        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

        ServiceBusSenderClient sender = builder
                .sender()
                .topicName("topic-1")
                .buildClient();
            System.out.println("Start sending message.");
            int index = 0;
            while(true) {
                ServiceBusMessage message = new ServiceBusMessage("Hello-"+ (index++));
                sender.sendMessage(message);
                System.out.println(new Date() +  " " + index + ". Message sent data  and calling close");
                sender.close();
                break;
                // TimeUnit.SECONDS.sleep(10);
            }


        System.out.println(new Date() + " !!! Exit -------------------");
        java.util.Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();

        for (java.util.Iterator itr = map.keySet().iterator(); itr.hasNext(); ) {

            Thread thread = (Thread)itr.next();
            if (!thread.isDaemon()) {
                System.out.println(new Date() + " !!! Thread -------------------");
                System.out.println(thread.getName() + " --> " + thread.isDaemon());

                StackTraceElement[] stackTraceElements = map.get(thread);
                for (int i = 0; i < stackTraceElements.length; i++) {
                    System.out.println(stackTraceElements[i]);
                }
            }
        }

    }
}
