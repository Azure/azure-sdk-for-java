// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.FalseRuleFilter;
import com.azure.messaging.servicebus.administration.models.SqlRuleAction;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Sample demonstrates how to manage (add/remove/get) rules on Subscription. We will also explore different forms of
 * subscription filters.
 *
 * <p>
 * Topics are similar to Queues for the send side of the application. However unlike Queues, Topic can have zero or more
 * subscriptions, from which messages can be retrieved and each of subscription act like independent queues. Whether a
 * message is selected into the subscription is determined by the Filter condition for the subscription. Filters can be
 * one of the following:
 * </p>
 *
 * <ul>
 * <li>{@link TrueRuleFilter TrueFilter} - Selects all messages to subscription.</li>
 * <li>{@link FalseRuleFilter FalseFilter} - Selects none of the messages to subscription.</li>
 * <li>{@link SqlRuleFilter SqlFilter} - Holds a SQL-like condition expression that is evaluated in the ServiceBus
 * service against the arriving messages' user-defined properties and system properties and if matched the message is
 * selected for subscription.</li>
 * <li>{@link CorrelationRuleFilter CorrelationFilter} - Holds a set of conditions that is evaluated in the ServiceBus
 * service against the arriving messages' user-defined properties and system properties. A match exists when an arriving
 * message's value for a property is equal to the value specified in the correlation filter.</li>
 * </ul>
 *
 * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/topic-filters">Topic Filters</a>
 */
public class TopicSubscriptionWithRuleOperationsSample {
    // Connection String for the namespace can be obtained from the Azure portal under the
    // `Shared Access policies` section.
    private static final String SERVICEBUS_NAMESPACE_CONNECTION_STRING =
        System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    private static final String TOPIC_NAME = System.getenv("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");

    /**
     * Main method to invoke this demo on how to receive {@link ServiceBusReceivedMessage messages} by topic
     * subscriptions from an Azure Service Bus Topic.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TopicSubscriptionWithRuleOperationsSample app = new TopicSubscriptionWithRuleOperationsSample();
        app.run();
    }

    /**
     * This method to invoke this demo on how to receive {@link ServiceBusReceivedMessage messages} by topic
     * subscriptions from an Azure Service Bus Topic.
     */
    void run() {
        // Create 4 default subscriptions (no rules specified explicitly) and provide subscription names.
        // The Rule addition will be done as part of the sample depending on the subscription behavior expected.
        final String allMessagesSubscription = "{Subscription 1 Name}";
        final String sqlFilterOnlySubscription = "{Subscription 2 Name}";
        final String sqlFilterWithActionSubscription = "{Subscription 3 Name}";
        final String correlationFilterSubscription = "{Subscription 4 Name}";

        // When a subscription is created. A default rule is created for it. This is the name of that default rule.
        // The rule associated with the default rule is a TrueRuleFilter. It will receive all messages.
        final String defaultRuleName = "$Default";

        // Names of the subscription rules we will create.
        final String sqlFilterOnlyRuleName = "RedSqlRule";
        final String sqlFilterWithActionRuleName = "BlueSqlRule";
        final String correlationFilterSubscriptionRule = "ImportantCorrelationRule";

        final ServiceBusAdministrationClient administrationClient = new ServiceBusAdministrationClientBuilder()
            .connectionString(SERVICEBUS_NAMESPACE_CONNECTION_STRING)
            .buildClient();

        // In this scenario, we're deleting the default rule and adding the equivalent rule back.
        System.out.printf("SubscriptionName: %s, Removing and re-adding Default Rule%n", allMessagesSubscription);
        administrationClient.deleteRule(TOPIC_NAME, allMessagesSubscription, defaultRuleName);

        // Creating the equivalent of the $Default rule.
        final CreateRuleOptions defaultRuleOptions = new CreateRuleOptions()
            .setFilter(new TrueRuleFilter());
        administrationClient.createRule(TOPIC_NAME, allMessagesSubscription, defaultRuleName, defaultRuleOptions);

        // 2nd Subscription: Add SqlFilter on Subscription 2
        // Delete Default Rule.
        // Add the required SqlFilter Rule
        // Note: Does not apply to this sample but if there are multiple rules configured for a
        // single subscription, then one message is delivered to the subscription when any of the
        // rule matches. If more than one rules match and if there is no `SqlRuleAction` set for the
        // rule, then only one message will be delivered to the subscription. If more than one rules
        // match and there is a `SqlRuleAction` specified for the rule, then one message per `SqlRuleAction`
        // is delivered to the subscription.
        System.out.printf("SubscriptionName: %s, Removing Default Rule and Adding SqlFilter%n",
            sqlFilterOnlySubscription);
        administrationClient.deleteRule(TOPIC_NAME, sqlFilterOnlySubscription, defaultRuleName);

        final CreateRuleOptions sqlFilterRuleOptions = new CreateRuleOptions()
            .setFilter(new SqlRuleFilter("Color = 'Red'"));
        administrationClient.createRule(TOPIC_NAME, sqlFilterOnlySubscription, sqlFilterOnlyRuleName,
            sqlFilterRuleOptions);

        // 3rd Subscription: Add SqlFilter and SqlRuleAction on Subscription 3
        // Delete Default Rule
        // Add the required SqlFilter Rule and Action
        System.out.printf("SubscriptionName: %s, Removing Default Rule and Adding CorrelationFilter%n",
            sqlFilterWithActionSubscription);
        administrationClient.deleteRule(TOPIC_NAME, sqlFilterWithActionSubscription, defaultRuleName);


        final CreateRuleOptions sqlRuleWithActionOptions = new CreateRuleOptions()
            .setFilter(new SqlRuleFilter("Color = 'Blue'"))
            .setAction(new SqlRuleAction("SET Color = 'BlueProcessed'"));
        administrationClient.createRule(TOPIC_NAME, sqlFilterWithActionSubscription, sqlFilterWithActionRuleName,
            sqlRuleWithActionOptions);

        // 4th Subscription: Add Correlation Filter on Subscription 4
        System.out.printf("SubscriptionName: %s, Removing Default Rule and Adding CorrelationFilter%n",
            correlationFilterSubscription);
        administrationClient.deleteRule(TOPIC_NAME, correlationFilterSubscription, defaultRuleName);

        final CreateRuleOptions correlationFilterRuleOptions = new CreateRuleOptions()
            .setFilter(new CorrelationRuleFilter().setCorrelationId("important").setLabel("Red"));
        administrationClient.createRule(TOPIC_NAME, correlationFilterSubscription, correlationFilterSubscriptionRule,
            correlationFilterRuleOptions);

        // Get Rules on Subscription, called here only for one subscription as example
        administrationClient.listRules(TOPIC_NAME, correlationFilterSubscription)
            .forEach(ruleProperties ->
                System.out.printf("GetRules:: SubscriptionName: %s, CorrelationFilter Name: %s, Rule: %s%n",
                    correlationFilterSubscription, ruleProperties.getName(), ruleProperties.getFilter()));

        // Send messages to Topic
        sendMessages();

        // Receive messages from 'allMessagesSubscriptionName'. Should receive all 9 messages
        // The function is asynchronous, but we block here because we want to see the behaviour in sequence.
        receiveMessages(allMessagesSubscription).block();

        // Receive messages from 'sqlFilterOnlySubscriptionName'. Should receive all messages with Color = 'Red' i.e 3 messages
        receiveMessages(sqlFilterOnlySubscription).block();

        // Receive messages from 'sqlFilterWithActionSubscriptionName'. Should receive all messages with Color = 'Blue'
        // i.e 3 messages AND all messages should have color set to 'BlueProcessed'
        receiveMessages(sqlFilterWithActionSubscription).block();

        // Receive messages from 'correlationFilterSubscriptionName'. Should receive all messages  with Color = 'Red' and CorrelationId = "important"
        // i.e 1 message
        receiveMessages(correlationFilterSubscription).block();
    }

    /**
     * Send a {@link ServiceBusMessageBatch} to an Azure Service Bus Topic.
     */
    private static void sendMessages() {
        List<ServiceBusMessage> messageList = Arrays.asList(
            createServiceBusMessage("Red", null),
            createServiceBusMessage("Blue", null),
            createServiceBusMessage("Red", "important"),
            createServiceBusMessage("Blue", "important"),
            createServiceBusMessage("Red", "not_important"),
            createServiceBusMessage("Blue", "not_important"),
            createServiceBusMessage("Green", null),
            createServiceBusMessage("Green", "important"),
            createServiceBusMessage("Green", "not_important")
        );

        ServiceBusSenderClient topicSenderClient = new ServiceBusClientBuilder()
            .connectionString(SERVICEBUS_NAMESPACE_CONNECTION_STRING)
            .sender()
            .topicName(TOPIC_NAME)
            .buildClient();

        try {
            System.out.println("==========================================================================");
            System.out.println("Sending Messages to Topic");

            topicSenderClient.sendMessages(messageList);
        } finally {
            topicSenderClient.close();
        }
    }

    /**
     * Receive {@link ServiceBusReceivedMessage messages} by topic subscriptions from an Azure Service Bus Topic.
     *
     * @param subscriptionName Subscription Name.
     *
     * @return A Mono that completes when all messages have been received from the subscription. That is, when there is
     *     a period of 5 seconds where no message has been received.
     */
    private static Mono<Void> receiveMessages(String subscriptionName) {
        System.out.println("==========================================================================");
        System.out.printf("%s: Receiving Messages From Subscription: %s%n", OffsetDateTime.now(), subscriptionName);

        return Mono.using(() -> {
            // Creating a receiver in RECEIVE_AND_DELETE mode, which means that the service automatically removes the
            // message when it is received.
            return new ServiceBusClientBuilder()
                .connectionString(SERVICEBUS_NAMESPACE_CONNECTION_STRING)
                .receiver()
                .topicName(TOPIC_NAME)
                .subscriptionName(subscriptionName)
                .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                .buildAsyncClient();
        }, receiver -> {
            return receiver.receiveMessages()
                .timeout(Duration.ofSeconds(5))
                .map(message -> {
                    System.out.printf("Color Property = %s, CorrelationId = %s%n",
                        message.getApplicationProperties().get("Color"),
                        message.getCorrelationId() == null ? "" : message.getCorrelationId());
                    return message;
                })
                .onErrorResume(TimeoutException.class, error -> {
                    System.out.println("There were no more messages to receive. Error: " + error);
                    return Mono.empty();
                }).then();
        }, receiver -> {
            // Disposing of receiver.
            receiver.close();
        });
    }

    /**
     * Create a {@link ServiceBusMessage} for add to a {@link ServiceBusMessageBatch}.
     */
    private static ServiceBusMessage createServiceBusMessage(String label, String correlationId) {
        ServiceBusMessage message = new ServiceBusMessage(label);
        message.getApplicationProperties().put("Color", label);
        if (correlationId != null) {
            message.setCorrelationId(correlationId);
        }

        return message;
    }
}
