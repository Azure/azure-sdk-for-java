// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * Sample example showing how to update properties of Service Bus Queue.
 */
public class AdministrationClientUpdateQueueSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    /**
     * Main method to show how to update properties of Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        AdministrationClientUpdateQueueSample sample = new AdministrationClientUpdateQueueSample();
        sample.run();
    }

    /**
     * run method to invoke this demo on how to peek at a message within a Service Bus Queue.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    @Test
    public void run() {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        // The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "queueName" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.

        // Create a administrator client using connection string.
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        QueueProperties properties = client.getQueue(queueName);

        System.out.printf("Before queue properties LockDuration: [%d seconds], Max Delivery count: [%d].%n",
            properties.getLockDuration().getSeconds(), properties.getMaxDeliveryCount());

        // You can update 'QueueProperties' object with properties you want to change.
        properties.setMaxDeliveryCount(10).setLockDuration(Duration.ofSeconds(60));

        QueueProperties updatedProperties = client.updateQueue(properties);

        System.out.printf("After queue properties LockDuration: [%d seconds], Max Delivery count: [%d].%n",
            updatedProperties.getLockDuration().getSeconds(), updatedProperties.getMaxDeliveryCount());

        // This assertion is to ensure that samples are working. Users should remove this.
        Assertions.assertEquals(10, updatedProperties.getMaxDeliveryCount());
    }

}
