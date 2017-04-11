/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.servicebus.AuthorizationKeys;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRule;
import com.microsoft.azure.management.servicebus.NamespaceSku;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.Subscription;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.rest.LogLevel;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;

import java.io.File;

/**
 * Azure Service Bus basic scenario sample.
 * - Create namespace with a queue and a topic
 * - Create 2 subscriptions for topic using different methods.
 * - Create send authorization rule for queue.
 * - Create send and listener authorization rule for Topic.
 * - Get the keys from authorization rule to connect to queue.
 * - Send a "Hello" message to queue using Data plan sdk for Service Bus.
 * - Send a "Hello" message to topic using Data plan sdk for Service Bus.
 * - Delete namespace
 */
public final class ServiceBusWithClaimBasedAuthorization {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String rgName = SdkContext.randomResourceName("rgSB03_", 24);
        final String namespaceName = SdkContext.randomResourceName("namespace", 20);
        final String queueName = SdkContext.randomResourceName("queue1_", 24);
        final String topicName = SdkContext.randomResourceName("topic_", 24);
        final String subscription1Name = SdkContext.randomResourceName("sub1_", 24);
        final String subscription2Name = SdkContext.randomResourceName("sub2_", 24);

        try {
            //============================================================
            // Create a namespace.

            System.out.println("Creating name space " + namespaceName + " along with a queue " + queueName + " and a topic " +  topicName + " in resource group " + rgName + "...");

            ServiceBusNamespace serviceBusNamespace = azure.serviceBusNamespaces()
                    .define(namespaceName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withSku(NamespaceSku.PREMIUM_CAPACITY1)
                    .withNewQueue(queueName, 1024)
                    .withNewTopic(topicName, 1024)
                    .create();

            System.out.println("Created service bus " + serviceBusNamespace.name() + " (with queue and topic)");
            Utils.print(serviceBusNamespace);

            Queue queue = serviceBusNamespace.queues().getByName(queueName);
            Utils.print(queue);

            Topic topic = serviceBusNamespace.topics().getByName(topicName);
            Utils.print(topic);

            //============================================================
            // Create 2 subscriptions in topic using different methods.
            System.out.println("Creating a subscription in the topic using update on topic");
            topic = topic.update().withNewSubscription(subscription1Name).apply();

            Subscription subscription1 = topic.subscriptions().getByName(subscription1Name);

            System.out.println("Creating another subscription in the topic using direct create method for subscription");
            Subscription subscription2 = topic.subscriptions().define(subscription2Name).create();

            Utils.print(subscription1);
            Utils.print(subscription2);

            //=============================================================
            // Create new authorization rule for queue to send message.
            System.out.println("Create authorization rule for queue ...");
            NamespaceAuthorizationRule sendQueueAuthorizationRule = serviceBusNamespace.authorizationRules().define("SendRule").withSendingEnabled().create();
            Utils.print(sendQueueAuthorizationRule);

            System.out.println("Getting keys for authorization rule ...");
            AuthorizationKeys keys = sendQueueAuthorizationRule.getKeys();
            Utils.print(keys);

            //=============================================================
            // Send a message to queue.
            try {
                Configuration config = Configuration.load();
                config.setProperty(ServiceBusConfiguration.CONNECTION_STRING, keys.primaryConnectionString());
                ServiceBusContract queueService = ServiceBusService.create(config);
                queueService.sendMessage(queueName, new BrokeredMessage("Hello"));
            }
            catch (Exception ex) {
            }


            //=============================================================
            // Send a message to topic.
            try {
                Configuration config2 = Configuration.load();
                config2.setProperty(ServiceBusConfiguration.CONNECTION_STRING, keys.primaryConnectionString());
                ServiceBusContract topicService = ServiceBusService.create(config2);
                topicService.sendMessage(topicName, new BrokeredMessage("Hello"));
            }
            catch (Exception ex) {
            }


            //=============================================================
            // Delete a namespace
            System.out.println("Deleting namespace " + namespaceName + " [topic, queues and subscription will delete along with that]...");
            // This will delete the namespace and queue within it.
            try {
                azure.serviceBusNamespaces().deleteById(serviceBusNamespace.id());
            }
            catch (Exception ex) {
            }
            System.out.println("Deleted namespace " + namespaceName + "...");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            runSample(azure);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
