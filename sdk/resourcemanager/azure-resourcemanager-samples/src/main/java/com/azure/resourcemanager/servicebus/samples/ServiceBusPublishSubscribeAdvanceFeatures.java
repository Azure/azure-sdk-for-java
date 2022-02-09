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
import com.azure.resourcemanager.servicebus.models.NamespaceAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.NamespaceSku;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.servicebus.models.TopicAuthorizationRule;

import java.time.Duration;

/**
 * Azure Service Bus basic scenario sample.
 * - Create namespace.
 * - Create a service bus subscription in the topic with session and dead-letter enabled.
 * - Create another subscription in the topic with auto deletion of idle entities.
 * - Create second topic with new Send Authorization rule, partitioning enabled and a new Service bus Subscription.
 * - Update second topic to change time for AutoDeleteOnIdle time, without Send rule and with a new manage authorization rule.
 * - Get the keys from default authorization rule to connect to topic.
 * - Send a "Hello" message to topic using Data plan sdk for Service Bus.
 * - Delete a topic
 * - Delete namespace
 */
public final class ServiceBusPublishSubscribeAdvanceFeatures {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        // New resources
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSB04_", 24);
        final String namespaceName = Utils.randomResourceName(azureResourceManager, "namespace", 20);
        final String topic1Name = Utils.randomResourceName(azureResourceManager, "topic1_", 24);
        final String topic2Name = Utils.randomResourceName(azureResourceManager, "topic2_", 24);
        final String subscription1Name = Utils.randomResourceName(azureResourceManager, "subs_", 24);
        final String subscription2Name = Utils.randomResourceName(azureResourceManager, "subs_", 24);
        final String subscription3Name = Utils.randomResourceName(azureResourceManager, "subs_", 24);
        final String sendRuleName = "SendRule";
        final String manageRuleName = "ManageRule";

        try {
            //============================================================
            // Create a namespace.

            System.out.println("Creating name space " + namespaceName + " in resource group " + rgName + "...");

            ServiceBusNamespace serviceBusNamespace = azureResourceManager.serviceBusNamespaces()
                .define(namespaceName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withSku(NamespaceSku.STANDARD)
                .withNewTopic(topic1Name, 1024)
                .create();

            System.out.println("Created service bus " + serviceBusNamespace.name());
            Utils.print(serviceBusNamespace);

            System.out.println("Created topic following topic along with namespace " + namespaceName);

            Topic firstTopic = serviceBusNamespace.topics().getByName(topic1Name);
            Utils.print(firstTopic);

            //============================================================
            // Create a service bus subscription in the topic with session and dead-letter enabled.

            System.out.println("Creating subscription " + subscription1Name + " in topic " + topic1Name + "...");
            ServiceBusSubscription firstSubscription = firstTopic.subscriptions().define(subscription1Name)
                .withSession()
                .withDefaultMessageTTL(Duration.ofMinutes(20))
                .withMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount(20)
                .withExpiredMessageMovedToDeadLetterSubscription()
                .withMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException()
                .create();
            System.out.println("Created subscription " + subscription1Name + " in topic " + topic1Name + "...");

            Utils.print(firstSubscription);

            //============================================================
            // Create another subscription in the topic with auto deletion of idle entities.
            System.out.println("Creating another subscription " + subscription2Name + " in topic " + topic1Name + "...");

            ServiceBusSubscription secondSubscription = firstTopic.subscriptions().define(subscription2Name)
                .withSession()
                .withDeleteOnIdleDurationInMinutes(20)
                .create();
            System.out.println("Created subscription " + subscription2Name + " in topic " + topic1Name + "...");

            Utils.print(secondSubscription);

            //============================================================
            // Create second topic with new Send Authorization rule, partitioning enabled and a new Service bus Subscription.

            System.out.println("Creating second topic " + topic2Name + ", with De-duplication and AutoDeleteOnIdle features...");

            Topic secondTopic = serviceBusNamespace.topics().define(topic2Name)
                .withNewSendRule(sendRuleName)
                .withPartitioning()
                .withNewSubscription(subscription3Name)
                .create();

            System.out.println("Created second topic in namespace");

            Utils.print(secondTopic);

            System.out.println("Creating following authorization rules in second topic ");

            PagedIterable<TopicAuthorizationRule> authorizationRules = secondTopic.authorizationRules().list();
            for (TopicAuthorizationRule authorizationRule: authorizationRules) {
                Utils.print(authorizationRule);
            }

            //============================================================
            // Update second topic to change time for AutoDeleteOnIdle time, without Send rule and with a new manage authorization rule.
            System.out.println("Updating second topic " + topic2Name + "...");

            secondTopic = secondTopic.update()
                .withDeleteOnIdleDurationInMinutes(5)
                .withoutAuthorizationRule(sendRuleName)
                .withNewManageRule(manageRuleName)
                .apply();

            System.out.println("Updated second topic to change its auto deletion time");

            Utils.print(secondTopic);
            System.out.println("Updated  following authorization rules in second topic, new list of authorization rules are ");

            authorizationRules = secondTopic.authorizationRules().list();
            for (TopicAuthorizationRule authorizationRule: authorizationRules) {
                Utils.print(authorizationRule);
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

            //=============================================================
            // Send a message to topic.
            ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(keys.primaryConnectionString())
                .sender()
                .topicName(topic1Name)
                .buildClient();
            sender.sendMessage(new ServiceBusMessage("Hello World").setMessageId("1"));
            sender.close();

            //=============================================================
            // Delete a topic and namespace
            System.out.println("Deleting topic " + topic1Name + "in namespace " + namespaceName + "...");
            serviceBusNamespace.topics().deleteByName(topic1Name);
            System.out.println("Deleted topic " + topic1Name + "...");

            System.out.println("Deleting namespace " + namespaceName + "...");
            // This will delete the namespace and topic within it.
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
