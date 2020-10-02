// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.core.management.Region;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.servicebus.models.AuthorizationKeys;
import com.azure.resourcemanager.servicebus.models.NamespaceAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.NamespaceSku;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.Topic;

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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        // New resources
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSB03_", 24);
        final String namespaceName = Utils.randomResourceName(azureResourceManager, "namespace", 20);
        final String queueName = Utils.randomResourceName(azureResourceManager, "queue1_", 24);
        final String topicName = Utils.randomResourceName(azureResourceManager, "topic_", 24);
        final String subscription1Name = Utils.randomResourceName(azureResourceManager, "sub1_", 24);
        final String subscription2Name = Utils.randomResourceName(azureResourceManager, "sub2_", 24);

        try {
            //============================================================
            // Create a namespace.

            System.out.println("Creating name space " + namespaceName + " along with a queue " + queueName + " and a topic " +  topicName + " in resource group " + rgName + "...");

            ServiceBusNamespace serviceBusNamespace = azureResourceManager.serviceBusNamespaces()
                .define(namespaceName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withSku(NamespaceSku.STANDARD)
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

            ServiceBusSubscription subscription1 = topic.subscriptions().getByName(subscription1Name);

            System.out.println("Creating another subscription in the topic using direct create method for subscription");
            ServiceBusSubscription subscription2 = topic.subscriptions().define(subscription2Name).create();

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
            ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(keys.primaryConnectionString())
                .sender()
                .queueName(queueName)
                .buildClient();
            sender.sendMessage(new ServiceBusMessage("Hello").setMessageId("1"));
            sender.close();


            //=============================================================
            // Send a message to topic.
            sender = new ServiceBusClientBuilder()
                .connectionString(keys.primaryConnectionString())
                .sender()
                .topicName(topicName)
                .buildClient();
            sender.sendMessage(new ServiceBusMessage("Hello").setMessageId("1"));
            sender.close();

            //=============================================================
            // Delete a namespace
            System.out.println("Deleting namespace " + namespaceName + " [topic, queues and subscription will delete along with that]...");
            // This will delete the namespace and queue within it.
            azureResourceManager.serviceBusNamespaces().deleteById(serviceBusNamespace.id());
            System.out.println("Deleted namespace " + namespaceName + "...");

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
