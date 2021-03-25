// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
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
import com.azure.resourcemanager.servicebus.models.KeyType;
import com.azure.resourcemanager.servicebus.models.NamespaceAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.NamespaceSku;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.Topic;

/**
 * Azure Service Bus basic scenario sample.
 * - Create namespace.
 * - Create a topic.
 * - Update topic with new size and a new ServiceBus subscription.
 * - Create another ServiceBus subscription in the topic.
 * - List topic
 * - List ServiceBus subscriptions
 * - Get default authorization rule.
 * - Regenerate the keys in the authorization rule.
 * - Send a message to topic using Data plan sdk for Service Bus.
 * - Delete one ServiceBus subscription as part of update of topic.
 * - Delete another ServiceBus subscription.
 * - Delete topic
 * - Delete namespace
 */
public final class ServiceBusPublishSubscribeBasic {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        // New resources
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSB02_", 24);
        final String namespaceName = Utils.randomResourceName(azureResourceManager, "namespace", 20);
        final String topicName = Utils.randomResourceName(azureResourceManager, "topic_", 24);
        final String subscription1Name = Utils.randomResourceName(azureResourceManager, "sub1_", 24);
        final String subscription2Name = Utils.randomResourceName(azureResourceManager, "sub2_", 24);

        try {
            //============================================================
            // Create a namespace.

            System.out.println("Creating name space " + namespaceName + " in resource group " + rgName + "...");

            ServiceBusNamespace serviceBusNamespace = azureResourceManager.serviceBusNamespaces()
                .define(namespaceName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withSku(NamespaceSku.STANDARD)
                .create();

            System.out.println("Created service bus " + serviceBusNamespace.name());
            Utils.print(serviceBusNamespace);

            //============================================================
            // Create a topic in namespace

            System.out.println("Creating topic " + topicName + " in namespace " + namespaceName + "...");

            Topic topic = serviceBusNamespace.topics().define(topicName)
                .withSizeInMB(2048)
                .create();

            System.out.println("Created second queue in namespace");

            Utils.print(topic);

            //============================================================
            // Get and update topic with new size and a subscription
            System.out.println("Updating topic " + topicName + " with new size and a subscription...");
            topic = serviceBusNamespace.topics().getByName(topicName);
            topic = topic.update()
                .withNewSubscription(subscription1Name)
                .withSizeInMB(3072)
                .apply();

            System.out.println("Updated topic to change its size in MB along with a subscription");

            Utils.print(topic);

            ServiceBusSubscription firstSubscription = topic.subscriptions().getByName(subscription1Name);
            Utils.print(firstSubscription);
            //============================================================
            // Create a subscription
            System.out.println("Adding second subscription" + subscription2Name + " to topic " + topicName + "...");
            ServiceBusSubscription secondSubscription = topic.subscriptions().define(subscription2Name).withDeleteOnIdleDurationInMinutes(10).create();
            System.out.println("Added second subscription" + subscription2Name + " to topic " + topicName + "...");

            Utils.print(secondSubscription);

            //=============================================================
            // List topics in namespaces

            PagedIterable<Topic> topics = serviceBusNamespace.topics().list();
            System.out.println("Number of topics in namespace :" + Utils.getSize(topics));

            for (Topic topicInNamespace : topics) {
                Utils.print(topicInNamespace);
            }

            //=============================================================
            // List all subscriptions for topic in namespaces

            PagedIterable<ServiceBusSubscription> subscriptions = topic.subscriptions().list();
            System.out.println("Number of subscriptions to topic: " + Utils.getSize(subscriptions));

            for (ServiceBusSubscription subscription : subscriptions) {
                Utils.print(subscription);
            }

            //=============================================================
            // Get connection string for default authorization rule of namespace

            PagedIterable<NamespaceAuthorizationRule> namespaceAuthorizationRules = serviceBusNamespace.authorizationRules().list();
            System.out.println("Number of authorization rule for namespace :" + Utils.getSize(namespaceAuthorizationRules));


            for (NamespaceAuthorizationRule namespaceAuthorizationRule: namespaceAuthorizationRules) {
                Utils.print(namespaceAuthorizationRule);
            }

            System.out.println("Getting keys for authorization rule ...");

            AuthorizationKeys keys = namespaceAuthorizationRules.iterator().next().getKeys();
            Utils.print(keys);
            System.out.println("Regenerating secondary key for authorization rule ...");
            keys = namespaceAuthorizationRules.iterator().next().regenerateKey(KeyType.SECONDARY_KEY);
            Utils.print(keys);

            //=============================================================
            // Send a message to topic.
            ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(keys.primaryConnectionString())
                .sender()
                .topicName(topicName)
                .buildClient();
            sender.sendMessage(new ServiceBusMessage("Hello World").setMessageId("1"));
            sender.close();

            //=============================================================
            // Delete a queue and namespace
            System.out.println("Deleting subscription " + subscription1Name + " in topic " + topicName + " via update flow...");
            topic = topic.update().withoutSubscription(subscription1Name).apply();
            System.out.println("Deleted subscription " + subscription1Name + "...");

            System.out.println("Number of subscriptions in the topic after deleting first subscription: " + topic.subscriptionCount());

            System.out.println("Deleting namespace " + namespaceName + "...");
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
