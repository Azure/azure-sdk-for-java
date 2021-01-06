// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionRuntimeProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Class contains sample code snippets that will be used in javadocs.
 *
 * @see ServiceBusAdministrationAsyncClient
 * @see ServiceBusAdministrationClient
 */
public class ServiceBusAdministrationClientJavaDocCodeSamples {

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

        client.createQueue("my-new-queue").block(Duration.ofMinutes(1));
    }

    /**
     * Creates a queue synchronously.
     */
    public void createQueue() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString("<< Service Bus NAMESPACE connection string>>")
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
    public void createQueueAsync() {
        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString("<< Service Bus NAMESPACE connection string>>")
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

    @Test
    public void updateSubscription() throws InterruptedException {
        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS);

        String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_LISTENONLY_CONNECTION_STRING");
        //String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        System.out.println("Listen only connectionString: " + connectionString);

        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(logOptions)
            .buildAsyncClient();

        /*client.createSubscription("hemant-test-topic2", "subs1-0")
            .doOnError(throwable -> {
                System.out.println("!!! Error : " + throwable.getMessage() + "\n");
                throwable.printStackTrace();
            })
            .subscribe(subscriptionRuntimeProperties -> {
                System.out.println("SubscriptionName: " + subscriptionRuntimeProperties.getSubscriptionName());
            });
        */

       client.getSubscriptionRuntimeProperties("hemant-test-topic2", "subs1-0")
           .doOnError(throwable -> {
               System.out.println("!!! Error : " + throwable.getMessage() + "\n");
               throwable.printStackTrace();
           })
           .subscribe(subscriptionRuntimeProperties -> {
           System.out.println("SubscriptionName: " + subscriptionRuntimeProperties.getSubscriptionName());
        });

        TimeUnit.SECONDS.sleep(10);

    }
}
