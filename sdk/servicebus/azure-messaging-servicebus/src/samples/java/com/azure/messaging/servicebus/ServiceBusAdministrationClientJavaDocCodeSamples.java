// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import org.junit.jupiter.api.Test;

/**
 * Class contains sample code snippets that will be used in javadocs.
 *
 * @see ServiceBusAdministrationAsyncClient
 * @see ServiceBusAdministrationClient
 */
public class ServiceBusAdministrationClientJavaDocCodeSamples {
    private static String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");

    /**
     * Creates {@link ServiceBusAdministrationClient} with a connection string.
     */
    @Test
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation
        // Retrieve 'connectionString' from your configuration.

        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS);

        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(logOptions)
            .buildClient();
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation

        client.createQueue("my-new-queue");
    }

    /**
     * Creates {@link ServiceBusAdministrationAsyncClient} using Azure Identity.
     */
    public void instantiateAsync() {
        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.instantiation
        // DefaultAzureCredential creates a credential based on the environment it is executed in.
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString("<< Service Bus NAMESPACE connection string>>")
            .credential("<< my-sb-namespace.servicebus.windows.net >>", credential)
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.instantiation

    }

    /**
     * Creates a queue synchronously.
     */
    @Test
    public void createQueue() {
        // Retrieve 'connectionString' from your configuration.
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string
        QueueProperties queue = client.createQueue("my-new-queue");
        System.out.printf("Queue created. Name: %s. Lock Duration: %s.%n",
            queue.getName(), queue.getLockDuration());
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string
    }

    /**
     * Creates a queue asynchronously.
     */
    @Test
    public void createQueueAsync() {
        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.createqueue#string
        // `.subscribe()` is a non-blocking call. It'll move onto the next
        // instruction after setting up the `consumer` and `errorConsumer` callbacks.
        client.createQueue("my-new-queue").subscribe(queue -> {
            System.out.printf("Queue created. Name: %s. Lock Duration: %s.%n",
                queue.getName(), queue.getLockDuration());
        }, error -> {
                System.err.println("Error creating queue: " + error);
            });
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.createqueue#string
    }

    /**
     * Updates a subscription.
     */
    @Test
    public void updateSubscription() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatesubscription#subscriptionproperties
        // To update the subscription we have to:
        // 1. Get the subscription info from the service.
        // 2. Update the SubscriptionProperties we want to change.
        // 3. Call the updateSubscription() with the updated object.
        SubscriptionProperties subscription = client.getSubscription("my-topic", "my-subscription");

        System.out.println("Original delivery count: " + subscription.getMaxDeliveryCount());

        // Updating it to a new value.
        subscription.setMaxDeliveryCount(5);

        // Persisting the updates to the subscription object.
        SubscriptionProperties updated = client.updateSubscription(subscription);

        System.out.printf("Subscription updated. Name: %s. Delivery count: %s.%n",
            updated.getSubscriptionName(), updated.getMaxDeliveryCount());
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatesubscription#subscriptionproperties
    }

    /**
     * Edits a subscription asynchronously.
     */
    @Test
    public void updateSubscriptionAsync() {
        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.updatesubscription#subscriptionproperties
        // To update the subscription we have to:
        // 1. Get the subscription info from the service.
        // 2. Update the SubscriptionProperties we want to change.
        // 3. Call the updateSubscription() with the updated object.

        // `.subscribe()` is a non-blocking call. It'll move onto the next
        // instruction after setting up the `consumer` and `errorConsumer` callbacks.
        client.getSubscription("my-topic", "my-subscription")
            .flatMap(subscription -> {
                System.out.println("Original delivery count: " + subscription.getMaxDeliveryCount());

                // Updating it to a new value.
                subscription.setMaxDeliveryCount(5);

                // Persisting the updates to the subscription object.
                return client.updateSubscription(subscription);
            })
            .subscribe(subscription -> {
                System.out.printf("Subscription updated. Name: %s. Delivery count: %s.%n",
                    subscription.getSubscriptionName(), subscription.getMaxDeliveryCount());
            }, error -> {
                    System.err.println("Error updating subscription: " + error);
                });
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.updatesubscription#subscriptionproperties
    }

    /**
     * Lists queues.
     */
    @Test
    public void listQueues() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.listQueues
        client.listQueues().forEach(queue -> {
            System.out.printf("Queue [%s]. Lock Duration: %s.%n",
                queue.getName(), queue.getLockDuration());
        });
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.listQueues
    }

    /**
     * Lists queues asynchronously.
     */
    @Test
    public void listQueuesAsync() {
        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.listQueues
        // `.subscribe()` is a non-blocking call. It'll move onto the next
        // instruction after setting up the `consumer` and `errorConsumer` callbacks.
        client.listQueues().subscribe(queue -> {
            System.out.printf("Queue [%s]. Lock Duration: %s.%n",
                queue.getName(), queue.getLockDuration());
        }, error -> {
                System.err.println("Error fetching queues: " + error);
            });
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.listQueues
    }
}
