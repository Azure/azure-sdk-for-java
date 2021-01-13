// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.QueueProperties;

import java.time.Duration;

/**
 * Sample example showing how to update properties of Service Bus Queue.
 */
public class AdministrationClientUpdateQueueSample {
    /**
     * Main method to show how to update properties of Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.

        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";

        // Create a administrator client using connection string.
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        QueueProperties properties = client.getQueue("<<queue-name>>");

        System.out.printf("Before queue properties LockDuration: [%d seconds], Max Delivery count: [%d].%n",
            properties.getLockDuration().getSeconds(), properties.getMaxDeliveryCount());

        // You can update 'QueueProperties' object with properties you want to change.
        properties.setMaxDeliveryCount(10).setLockDuration(Duration.ofSeconds(60));

        QueueProperties updatedProperties = client.updateQueue(properties);

        System.out.printf("After queue properties LockDuration: [%d seconds], Max Delivery count: [%d].%n",
            updatedProperties.getLockDuration().getSeconds(), updatedProperties.getMaxDeliveryCount());
    }

}
