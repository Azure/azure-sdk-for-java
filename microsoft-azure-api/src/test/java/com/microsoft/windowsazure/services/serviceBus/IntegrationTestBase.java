package com.microsoft.windowsazure.services.serviceBus;

import static com.microsoft.windowsazure.services.serviceBus.Util.*;

import org.junit.Before;
import org.junit.BeforeClass;

import com.microsoft.windowsazure.configuration.Configuration;
import com.microsoft.windowsazure.services.serviceBus.Queue;
import com.microsoft.windowsazure.services.serviceBus.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.Topic;

public abstract class IntegrationTestBase {
    protected Configuration createConfiguration() {
        Configuration config = new Configuration();
        ServiceBusConfiguration.configure(config, "lodejard", "owner", "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");

        // when mock running
        //config.setProperty("serviceBus.uri", "http://localhost:8086");
        //config.setProperty("wrapClient.uri", "http://localhost:8081/WRAPv0.9");

        return config;
    }

    @BeforeClass
    public static void initializeSystem() {
        System.out.println("initialize");
        System.setProperty("http.proxyHost", "itgproxy");
        System.setProperty("http.proxyPort", "80");
        System.setProperty("http.keepAlive", "false");
    }

    @Before
    public void initialize() throws Exception {
        System.out.println("initialize");
        System.setProperty("http.proxyHost", "itgproxy");
        System.setProperty("http.proxyPort", "80");
        System.setProperty("http.keepAlive", "false");

        boolean testAlphaExists = false;
        ServiceBusContract service = createConfiguration().create(ServiceBusContract.class);
        for (Queue queue : iterateQueues(service)) {
            String queueName = queue.getName();
            if (queueName.startsWith("Test") || queueName.startsWith("test")) {
                if (queueName.equalsIgnoreCase("TestAlpha")) {
                    testAlphaExists = true;
                    long count = queue.getMessageCount();
                    for (long i = 0; i != count; ++i) {
                        service.receiveQueueMessage(queueName, new ReceiveMessageOptions().setTimeout(20));
                    }
                }
                else {
                    service.deleteQueue(queueName);
                }
            }
        }
        for (Topic topic : iterateTopics(service)) {
            String topicName = topic.getName();
            if (topicName.startsWith("Test") || topicName.startsWith("test")) {
                service.deleteQueue(topicName);
            }
        }
        if (!testAlphaExists) {
            service.createQueue(new Queue("TestAlpha"));
        }
    }
}
