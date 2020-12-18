// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.EntityStatus;
import com.azure.messaging.servicebus.administration.models.QueueProperties;

/**
 * Sample example showing how to update queue properties in Service Bus Queue.
 */
public class AdministrationClientUpdateQueueSample {
    /**
     * Main method to how update queue properties in Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    public static void main(String[] args) throws InterruptedException {
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

        System.out.printf("Before queue properties status: [%s], Max Delivery count: [%d].%n",
            properties.getStatus(), properties.getMaxDeliveryCount());

        // You can update 'QueueProperties' object with properties you want to change.
        properties.setStatus(EntityStatus.DISABLED).setMaxDeliveryCount(9);

        QueueProperties updatedProperties = client.updateQueue(properties);

        System.out.printf("After queue properties status: [%s], Max Delivery count: [%d].%n",
            updatedProperties.getStatus(), updatedProperties.getMaxDeliveryCount());
    }

}
