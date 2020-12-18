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
        ServiceBusAdministrationClient administrationClient = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        QueueProperties queueProperties = administrationClient.getQueue("<<queue-name>>");

        System.out.printf("Before update queue properties status :[%s], Max Delivery count :[%d]. %n" ,
            queueProperties.getStatus(), queueProperties.getMaxDeliveryCount());

        // You can update 'QueueProperties' object with properties you want to change.
        queueProperties.setStatus(EntityStatus.DISABLED);
        queueProperties.setMaxDeliveryCount(9);

        QueueProperties updatedQueueProperties = administrationClient.updateQueue(queueProperties);

        System.out.printf("After update queue properties status :[%s], Max Delivery count :[%d]. %n" ,
            updatedQueueProperties.getStatus(), updatedQueueProperties.getMaxDeliveryCount());
    }

}
