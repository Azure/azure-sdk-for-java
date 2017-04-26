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
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.Subscription;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.servicebus.TopicAuthorizationRule;
import com.microsoft.rest.LogLevel;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import org.joda.time.Period;

import java.io.File;

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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String rgName = SdkContext.randomResourceName("rgSB04_", 24);
        final String namespaceName = SdkContext.randomResourceName("namespace", 20);
        final String topic1Name = SdkContext.randomResourceName("topic1_", 24);
        final String topic2Name = SdkContext.randomResourceName("topic2_", 24);
        final String subscription1Name = SdkContext.randomResourceName("subs_", 24);
        final String subscription2Name = SdkContext.randomResourceName("subs_", 24);
        final String subscription3Name = SdkContext.randomResourceName("subs_", 24);
        final String sendRuleName = "SendRule";
        final String manageRuleName = "ManageRule";

        try {
            //============================================================
            // Create a namespace.

            System.out.println("Creating name space " + namespaceName + " in resource group " + rgName + "...");

            ServiceBusNamespace serviceBusNamespace = azure.serviceBusNamespaces()
                    .define(namespaceName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withSku(NamespaceSku.PREMIUM_CAPACITY1)
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
            Subscription firstSubscription = firstTopic.subscriptions().define(subscription1Name)
                    .withSession()
                    .withDefaultMessageTTL(new Period().withMinutes(20))
                    .withMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount(20)
                    .withExpiredMessageMovedToDeadLetterSubscription()
                    .withMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException()
                    .create();
            System.out.println("Created subscription " + subscription1Name + " in topic " + topic1Name + "...");

            Utils.print(firstSubscription);

            //============================================================
            // Create another subscription in the topic with auto deletion of idle entities.
            System.out.println("Creating another subscription " + subscription2Name + " in topic " + topic1Name + "...");

            Subscription secondSubscription = firstTopic.subscriptions().define(subscription2Name)
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

            PagedList<TopicAuthorizationRule> authorizationRules = secondTopic.authorizationRules().list();
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

            PagedList<NamespaceAuthorizationRule> namespaceAuthorizationRules = serviceBusNamespace.authorizationRules().list();
            System.out.println("Number of authorization rule for namespace :" + namespaceAuthorizationRules.size());


            for (NamespaceAuthorizationRule namespaceAuthorizationRule: namespaceAuthorizationRules) {
                Utils.print(namespaceAuthorizationRule);
            }

            System.out.println("Getting keys for authorization rule ...");

            AuthorizationKeys keys = namespaceAuthorizationRules.get(0).getKeys();
            Utils.print(keys);

            //=============================================================
            // Send a message to topic.
            try {
                Configuration config = Configuration.load();
                config.setProperty(ServiceBusConfiguration.CONNECTION_STRING, keys.primaryConnectionString());
                ServiceBusContract service = ServiceBusService.create(config);
                service.sendTopicMessage(topic1Name, new BrokeredMessage("Hello World"));
            }
            catch (Exception ex) {
            }
            //=============================================================
            // Delete a topic and namespace
            System.out.println("Deleting topic " + topic1Name + "in namespace " + namespaceName + "...");
            serviceBusNamespace.topics().deleteByName(topic1Name);
            System.out.println("Deleted topic " + topic1Name + "...");

            System.out.println("Deleting namespace " + namespaceName + "...");
            // This will delete the namespace and topic within it.
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
