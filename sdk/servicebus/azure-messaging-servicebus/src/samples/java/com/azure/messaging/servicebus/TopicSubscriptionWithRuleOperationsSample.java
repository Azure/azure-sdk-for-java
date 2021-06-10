// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.SqlRuleAction;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample demonstrates how to receive {@link ServiceBusReceivedMessage messages} by topic subscriptions from
 * an Azure Service Bus Topic.
 */
public class TopicSubscriptionWithRuleOperationsSample {

    static final String SERVICE_BUS_CONNECTION_STRING = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    static final String SERVICE_BUS_TOPIC_NAME = "{Your Service Bus Topic Name}";
    static final String ALL_MESSAGES_SUBSCRIPTION_NAME = "{Subscription 1 Name}";
    static final String SQL_FILTER_ONLY_SUBSCRIPTION_NAME = "{Subscription 2 Name}";
    static final String SQL_FILTER_WITH_ACTION_SUBSCRIPTION_NAME = "{Subscription 3 Name}";
    static final String CORRELATION_FILTER_SUBSCRIPTION_NAME = "{Subscription 4 Name}";
    static final String DEFAULT_SUBSCRIPTION_RULE_NAME = "$Default";
    static final String SQL_FILTER_ONLY_SUBSCRIPTION_RULE_NAME = "RedSqlRule";
    static final String SQL_FILTER_WITH_ACTION_SUBSCRIPTION_RULE_NAME = "BlueSqlRule";
    static final String CORRELATION_FILTER_SUBSCRIPTION_RULE_NAME = "ImportantCorrelationRule";

    /**
     * Main method to invoke this demo on how to receive {@link ServiceBusReceivedMessage messages}
     * by topic subscriptions from an Azure Service Bus Topic.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        TopicSubscriptionWithRuleOperationsSample app = new TopicSubscriptionWithRuleOperationsSample();
        app.run();
    }

    /**
     * This method to invoke this demo on how to receive {@link ServiceBusReceivedMessage messages}
     * by topic subscriptions from an Azure Service Bus Topic.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    void run() throws InterruptedException {

        ServiceBusAdministrationClient administrationClient =
            new ServiceBusAdministrationClientBuilder()
                .connectionString(SERVICE_BUS_CONNECTION_STRING)
                .buildClient();

        // First Subscription is already created with default rule. Leave as is.
        System.out.println(String.format("SubscriptionName: %s, Removing and re-adding Default Rule", ALL_MESSAGES_SUBSCRIPTION_NAME));
        administrationClient.deleteRule(SERVICE_BUS_TOPIC_NAME, ALL_MESSAGES_SUBSCRIPTION_NAME, DEFAULT_SUBSCRIPTION_RULE_NAME);
        administrationClient.createRule(SERVICE_BUS_TOPIC_NAME, ALL_MESSAGES_SUBSCRIPTION_NAME, DEFAULT_SUBSCRIPTION_RULE_NAME);
        administrationClient.updateRule(SERVICE_BUS_TOPIC_NAME, ALL_MESSAGES_SUBSCRIPTION_NAME,
            administrationClient.getRule(SERVICE_BUS_TOPIC_NAME, ALL_MESSAGES_SUBSCRIPTION_NAME, DEFAULT_SUBSCRIPTION_RULE_NAME)
                .setFilter(new TrueRuleFilter()));

        // 2nd Subscription: Add SqlFilter on Subscription 2
        // Delete Default Rule.
        // Add the required SqlFilter Rule
        // Note: Does not apply to this sample but if there are multiple rules configured for a
        // single subscription, then one message is delivered to the subscription when any of the
        // rule matches. If more than one rules match and if there is no `SqlRuleAction` set for the
        // rule, then only one message will be delivered to the subscription. If more than one rules
        // match and there is a `SqlRuleAction` specified for the rule, then one message per `SqlRuleAction`
        // is delivered to the subscription.
        System.out.println(String.format("SubscriptionName: %s, Removing Default Rule and Adding SqlFilter", SQL_FILTER_ONLY_SUBSCRIPTION_NAME));
        administrationClient.deleteRule(SERVICE_BUS_TOPIC_NAME, SQL_FILTER_ONLY_SUBSCRIPTION_NAME, DEFAULT_SUBSCRIPTION_RULE_NAME);
        administrationClient.createRule(SERVICE_BUS_TOPIC_NAME, SQL_FILTER_ONLY_SUBSCRIPTION_NAME, SQL_FILTER_ONLY_SUBSCRIPTION_RULE_NAME);
        administrationClient.updateRule(SERVICE_BUS_TOPIC_NAME, SQL_FILTER_ONLY_SUBSCRIPTION_NAME,
            administrationClient.getRule(SERVICE_BUS_TOPIC_NAME, SQL_FILTER_ONLY_SUBSCRIPTION_NAME, SQL_FILTER_ONLY_SUBSCRIPTION_RULE_NAME)
                .setFilter(new SqlRuleFilter("Color = 'Red'")));

        // 3rd Subscription: Add SqlFilter and SqlRuleAction on Subscription 3
        // Delete Default Rule
        // Add the required SqlFilter Rule and Action
        System.out.println(String.format("SubscriptionName: %s, Removing Default Rule and Adding CorrelationFilter", SQL_FILTER_WITH_ACTION_SUBSCRIPTION_NAME));
        administrationClient.deleteRule(SERVICE_BUS_TOPIC_NAME, SQL_FILTER_WITH_ACTION_SUBSCRIPTION_NAME, DEFAULT_SUBSCRIPTION_RULE_NAME);
        administrationClient.createRule(SERVICE_BUS_TOPIC_NAME, SQL_FILTER_WITH_ACTION_SUBSCRIPTION_NAME, SQL_FILTER_WITH_ACTION_SUBSCRIPTION_RULE_NAME);
        administrationClient.updateRule(SERVICE_BUS_TOPIC_NAME, SQL_FILTER_WITH_ACTION_SUBSCRIPTION_NAME,
            administrationClient.getRule(SERVICE_BUS_TOPIC_NAME, SQL_FILTER_WITH_ACTION_SUBSCRIPTION_NAME, SQL_FILTER_WITH_ACTION_SUBSCRIPTION_RULE_NAME)
                .setFilter(new SqlRuleFilter("Color = 'Blue'"))
                .setAction(new SqlRuleAction("SET Color = 'BlueProcessed'")));

        // 4th Subscription: Add Correlation Filter on Subscription 4
        System.out.println(String.format("SubscriptionName: %s, Removing Default Rule and Adding CorrelationFilter", CORRELATION_FILTER_SUBSCRIPTION_NAME));
        administrationClient.deleteRule(SERVICE_BUS_TOPIC_NAME, CORRELATION_FILTER_SUBSCRIPTION_NAME, DEFAULT_SUBSCRIPTION_RULE_NAME);
        administrationClient.createRule(SERVICE_BUS_TOPIC_NAME, CORRELATION_FILTER_SUBSCRIPTION_NAME, CORRELATION_FILTER_SUBSCRIPTION_RULE_NAME);
        administrationClient.updateRule(SERVICE_BUS_TOPIC_NAME, CORRELATION_FILTER_SUBSCRIPTION_NAME,
            administrationClient.getRule(SERVICE_BUS_TOPIC_NAME, CORRELATION_FILTER_SUBSCRIPTION_NAME, CORRELATION_FILTER_SUBSCRIPTION_RULE_NAME)
                .setFilter(new CorrelationRuleFilter().setCorrelationId("important").setLabel("Red")));

        // Get Rules on Subscription, called here only for one subscription as example
        administrationClient.listRules(SERVICE_BUS_TOPIC_NAME, CORRELATION_FILTER_SUBSCRIPTION_NAME)
            .forEach(ruleProperties -> {
                System.out.println(String.format("GetRules:: SubscriptionName: %s, CorrelationFilter Name: %s, Rule: %s", CORRELATION_FILTER_SUBSCRIPTION_NAME, ruleProperties.getName(), ruleProperties.getFilter()));
            });

        // Send messages to Topic
        sendMessagesAsync();

        // Receive messages from 'allMessagesSubscriptionName'. Should receive all 9 messages
        receiveMessagesAsync(ALL_MESSAGES_SUBSCRIPTION_NAME);

        // Receive messages from 'sqlFilterOnlySubscriptionName'. Should receive all messages with Color = 'Red' i.e 3 messages
        receiveMessagesAsync(SQL_FILTER_ONLY_SUBSCRIPTION_NAME);

        // Receive messages from 'sqlFilterWithActionSubscriptionName'. Should receive all messages with Color = 'Blue'
        // i.e 3 messages AND all messages should have color set to 'BlueProcessed'
        receiveMessagesAsync(SQL_FILTER_WITH_ACTION_SUBSCRIPTION_NAME);

        // Receive messages from 'correlationFilterSubscriptionName'. Should receive all messages  with Color = 'Red' and CorrelationId = "important"
        // i.e 1 message
        receiveMessagesAsync(CORRELATION_FILTER_SUBSCRIPTION_NAME);

    }

    /**
     * Receive {@link ServiceBusReceivedMessage messages} by topic subscriptions from an Azure Service Bus Topic.
     *
     * @param subscriptionName Subscription Name.
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    static void receiveMessagesAsync(String subscriptionName) throws InterruptedException {
        AtomicReference<ServiceBusReceiverAsyncClient> receiverAsyncClient = new AtomicReference<>();
        AtomicReference<CountDownLatch> countdownLatch = new AtomicReference<>();
        countdownLatch.set(new CountDownLatch(1));
        receiverAsyncClient.set(new ServiceBusClientBuilder()
            .connectionString(SERVICE_BUS_CONNECTION_STRING)
            .receiver()
            .topicName(SERVICE_BUS_TOPIC_NAME)
            .subscriptionName(subscriptionName)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .buildAsyncClient());
        System.out.println("==========================================================================");
        System.out.println(String.format("%s :: Receiving Messages From Subscription: %s", OffsetDateTime.now(), subscriptionName));
        AtomicLong receiveMessagesCount = new AtomicLong(0L);
        receiverAsyncClient.get().receiveMessages().parallel().subscribe(receivedMessage -> {
                receiveMessagesCount.incrementAndGet();
                System.out.println(String.format("Color Property = %s, CorrelationId = %s",
                    receivedMessage.getApplicationProperties().get("Color"),
                    receivedMessage.getCorrelationId() == null ? "" : receivedMessage.getCorrelationId()));
            }
        );
        countdownLatch.get().await(10, TimeUnit.SECONDS);
        System.out.println(String.format("%s :: Received '%s' Messages From Subscription: %s", OffsetDateTime.now(), receiveMessagesCount.get(), subscriptionName));
        System.out.println("==========================================================================");
        receiverAsyncClient.get().close();
    }

    /**
     * Send a {@link ServiceBusMessageBatch} to an Azure Service Bus Topic.
     */
    static void sendMessagesAsync() {
        ServiceBusSenderClient topicSenderClient = new ServiceBusClientBuilder()
            .connectionString(SERVICE_BUS_CONNECTION_STRING)
            .sender()
            .topicName(SERVICE_BUS_TOPIC_NAME)
            .buildClient();

        List<ServiceBusMessage> messageList = new ArrayList<ServiceBusMessage>() {{
            add(createServiceBusMessage("Red", null));
            add(createServiceBusMessage("Blue", null));
            add(createServiceBusMessage("Red", "important"));
            add(createServiceBusMessage("Blue", "important"));
            add(createServiceBusMessage("Red", "notimportant"));
            add(createServiceBusMessage("Blue", "notimportant"));
            add(createServiceBusMessage("Green", null));
            add(createServiceBusMessage("Green", "important"));
            add(createServiceBusMessage("Green", "notimportant"));
        }};
        System.out.println("==========================================================================");
        System.out.println("Sending Messages to Topic");
        ServiceBusMessageBatch messageBatch = topicSenderClient.createMessageBatch();
        messageList.forEach(message -> {
            System.out.println(String.format("Sent Message:: Label: %s, CorrelationId: %s",
                message.getBody().toString(), message.getCorrelationId() == null ? "" : message.getCorrelationId()));
            messageBatch.tryAddMessage(message);
        });
        topicSenderClient.sendMessages(messageBatch);
        topicSenderClient.close();
    }

    /**
     * Create a {@link ServiceBusMessage} for add to a {@link ServiceBusMessageBatch}.
     */
    static ServiceBusMessage createServiceBusMessage(String label, String correlationId) {
        ServiceBusMessage message = new ServiceBusMessage(label);
        message.getApplicationProperties().put("Color", label);
        if (correlationId != null) {
            message.setCorrelationId(correlationId);
        }
        return message;
    }

}
