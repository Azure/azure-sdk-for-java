// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.RuleAction;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SqlRuleAction;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * Class contains sample code snippets that will be used in javadocs.
 *
 * @see ServiceBusAdministrationAsyncClient
 * @see ServiceBusAdministrationClient
 */
public class ServiceBusAdministrationClientJavaDocCodeSamples {
    /**
     * Fully qualified namespace is the host name of the Service Bus resource.  It can be found by navigating to the
     * Service Bus namespace and looking in the "Essentials" panel.
     */
    private final String fullyQualifiedNamespace = System.getenv("AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME");

    /**
     * Creates {@link ServiceBusAdministrationClient} with a connection string.
     */
    @Test
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation
        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS);

        // DefaultAzureCredential creates a credential based on the environment it is executed in.
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, tokenCredential)
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

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.instantiation
    }

    /**
     * Creates {@link ServiceBusAdministrationAsyncClient} using Azure Identity and sets some log options.
     */
    @Test
    public void createAdministrationAsyncClientHttpLogOptions() {
        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.construct#retryoptions
        // DefaultAzureCredential creates a credential based on the environment it is executed in.
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        RetryOptions retryOptions = new RetryOptions(new FixedDelayOptions(4, Duration.ofSeconds(20)));

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .credential("<<fully-qualified-namespace>>", credential)
            .retryOptions(retryOptions)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.HEADERS))
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.construct#retryoptions

        client.createQueue("my-new-queue").block();
    }

    /**
     * Creates a queue synchronously.
     */
    @Test
    public void createQueue() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string
        QueueProperties queue = client.createQueue("my-new-queue");
        System.out.printf("Queue created. Name: %s. Lock Duration: %s.%n",
            queue.getName(), queue.getLockDuration());
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string
    }

    /**
     * Creates a queue synchronously.
     */
    @Test
    public void createQueueOptions() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string-createqueueoptions
        CreateQueueOptions queueOptions = new CreateQueueOptions()
            .setLockDuration(Duration.ofMinutes(2))
            .setMaxDeliveryCount(15);

        QueueProperties queue = client.createQueue("my-new-queue", queueOptions);
        System.out.printf("Queue created. Name: %s. Lock Duration: %s.%n",
            queue.getName(), queue.getLockDuration());
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string-createqueueoptions
    }

    /**
     * Creates a topic and a subscription with a rule.
     */
    @Test
    public void createTopicAndSubscription() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createsubscription#string-string-string
        String topicName = "my-new-topic";
        TopicProperties topic = client.createTopic(topicName);

        String subscriptionName = "high-importance-subscription";
        String ruleName = "important-emails-filter";
        CreateSubscriptionOptions subscriptionOptions = new CreateSubscriptionOptions()
            .setMaxDeliveryCount(15)
            .setLockDuration(Duration.ofMinutes(2));

        CorrelationRuleFilter ruleFilter = new CorrelationRuleFilter()
            .setCorrelationId("emails");
        ruleFilter.getProperties().put("importance", "high");

        CreateRuleOptions createRuleOptions = new CreateRuleOptions()
            .setFilter(ruleFilter);

        SubscriptionProperties subscription = client.createSubscription(topicName, subscriptionName, ruleName,
            subscriptionOptions, createRuleOptions);

        System.out.printf("Subscription created. Name: %s. Topic name: %s. Lock Duration: %s.%n",
            subscription.getSubscriptionName(), subscription.getTopicName(), subscription.getLockDuration());
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createsubscription#string-string-string
    }

    /**
     * Creates a queue asynchronously.
     */
    @Test
    public void createQueueAsync() {
        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
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
     * Creates a rule that subscribes to all messages.
     */
    @Test
    public void createTrueRuleFilter() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createRule
        String topicName = "my-existing-topic";
        String subscriptionName = "all-messages-subscription";
        String ruleName = "true-filter";

        RuleFilter alwaysTrueRule = new TrueRuleFilter();
        CreateRuleOptions createRuleOptions = new CreateRuleOptions()
            .setFilter(alwaysTrueRule);

        RuleProperties rule = client.createRule(topicName, ruleName, subscriptionName, createRuleOptions);

        System.out.printf("Rule '%s' created for topic %s, subscription %s. Filter: %s%n", rule.getName(), topicName,
            subscriptionName, rule.getFilter());
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createRule
    }

    /**
     * Creates a rule that matches messages with correlationId = "email", has a user-defined property
     * {@link ServiceBusMessage#getApplicationProperties()} called "sender" with the name 'joseph' and
     * "importance" = 'high'.  Then it sets the "importance" value to 'critical'.
     */
    @Test
    public void createSqlRuleFilterAndAction() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createRule#string-string-string-createRuleOptions
        String topicName = "emails";
        String subscriptionName = "important-emails";
        String ruleName = "emails-from-joseph";

        RuleFilter sqlRuleFilter = new SqlRuleFilter(
            "sys.CorrelationId = 'email' AND sender = 'joseph' AND (importance IS NULL OR importance = 'high')");
        RuleAction sqlRuleAction = new SqlRuleAction("SET importance = 'critical';");
        CreateRuleOptions createRuleOptions = new CreateRuleOptions()
            .setFilter(sqlRuleFilter)
            .setAction(sqlRuleAction);

        RuleProperties rule = client.createRule(topicName, ruleName, subscriptionName, createRuleOptions);

        System.out.printf("Rule '%s' created for topic %s, subscription %s. Filter: %s%n", rule.getName(), topicName,
            subscriptionName, rule.getFilter());
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.createRule#string-string-string-createRuleOptions
    }

    /**
     * Delete a queue.
     */
    @Test
    public void deleteQueue() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.deletequeue
        try {
            client.deleteQueue("my-existing-queue");
        } catch (AzureException exception) {
            System.err.println("Exception occurred deleting queue: " + exception);
        }
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.deletequeue
    }

    /**
     * Delete a queue asynchronously.
     */
    @Test
    public void deleteQueueAsync() {
        ServiceBusAdministrationAsyncClient asyncClient = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.deletequeue
        // `.subscribe()` is a non-blocking call. It'll move onto the next
        // instruction after setting up the `consumer`, `errorConsumer`, `completeConsumer` callbacks.
        asyncClient.deleteQueue("my-existing-queue").subscribe(unused -> {
        }, error -> {
            System.err.println("Error deleting queue: " + error);
        }, () -> {
            System.out.println("Deleted queue.");
        });
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.deletequeue
    }

    /**
     * Creates a queue synchronously.
     */
    @Test
    public void updateQueueOptions() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatequeue
        QueueProperties queue = client.getQueue("queue-that-exists");

        queue.setLockDuration(Duration.ofMinutes(3))
            .setMaxDeliveryCount(15)
            .setDeadLetteringOnMessageExpiration(true);

        QueueProperties updatedQueue = client.updateQueue(queue);

        System.out.printf("Queue updated. Name: %s. Lock duration: %s. Max delivery count: %s.%n",
            updatedQueue.getName(), updatedQueue.getLockDuration(), updatedQueue.getMaxDeliveryCount());
        // END: com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatequeue
    }

    /**
     * Updates a subscription.
     */
    @Test
    public void updateSubscription() {
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
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
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
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
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
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
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
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
