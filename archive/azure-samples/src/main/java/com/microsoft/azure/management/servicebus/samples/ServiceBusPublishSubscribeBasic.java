/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.samples;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.servicebus.AuthorizationKeys;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRule;
import com.microsoft.azure.management.servicebus.NamespaceSku;
import com.microsoft.azure.management.servicebus.Policykey;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String rgName = SdkContext.randomResourceName("rgSB02_", 24);
        final String namespaceName = SdkContext.randomResourceName("namespace", 20);
        final String topicName = SdkContext.randomResourceName("topic_", 24);
        final String subscription1Name = SdkContext.randomResourceName("sub1_", 24);
        final String subscription2Name = SdkContext.randomResourceName("sub2_", 24);

        try {
            //============================================================
            // Create a namespace.

            System.out.println("Creating name space " + namespaceName + " in resource group " + rgName + "...");

            ServiceBusNamespace serviceBusNamespace = azure.serviceBusNamespaces()
                    .define(namespaceName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withSku(NamespaceSku.PREMIUM_CAPACITY1)
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

            PagedList<Topic> topics = serviceBusNamespace.topics().list();
            System.out.println("Number of topics in namespace :" + topics.size());

            for (Topic topicInNamespace : topics) {
                Utils.print(topicInNamespace);
            }

            //=============================================================
            // List all subscriptions for topic in namespaces

            PagedList<ServiceBusSubscription> subscriptions = topic.subscriptions().list();
            System.out.println("Number of subscriptions to topic: " + subscriptions.size());

            for (ServiceBusSubscription subscription : subscriptions) {
                Utils.print(subscription);
            }

            //=============================================================
            // Get connection string for default authorization rule of namespace

            PagedList<NamespaceAuthorizationRule> namespaceAuthorizationRules = serviceBusNamespace.authorizationRules().list();
            System.out.println("Number of authorization rule for namespace :" + namespaceAuthorizationRules.size());


            for (NamespaceAuthorizationRule namespaceAuthorizationRule: namespaceAuthorizationRules) {
                Utils.print(namespaceAuthorizationRule);
            }

            System.out.println("Getting keys for authorization rule ...");

            AuthorizationKeys keys = namespaceAuthorizationRules.get(0).getKeys();
            Utils.print(keys);
            System.out.println("Regenerating secondary key for authorization rule ...");
            keys = namespaceAuthorizationRules.get(0).regenerateKey(Policykey.SECONDARY_KEY);
            Utils.print(keys);

            //=============================================================
            // Send a message to topic.
            try {
                Configuration config = Configuration.load();
                config.setProperty(ServiceBusConfiguration.CONNECTION_STRING, keys.primaryConnectionString());
                ServiceBusContract service = ServiceBusService.create(config);
                service.sendTopicMessage(topicName, new BrokeredMessage("Hello World"));
            }
            catch (Exception ex) {
            }
            //=============================================================
            // Delete a queue and namespace
            System.out.println("Deleting subscription " + subscription1Name + " in topic " + topicName + " via update flow...");
            topic = topic.update().withoutSubscription(subscription1Name).apply();
            System.out.println("Deleted subscription " + subscription1Name + "...");

            System.out.println("Number of subscriptions in the topic after deleting first subscription: " + topic.subscriptionCount());

            System.out.println("Deleting namespace " + namespaceName + "...");
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
