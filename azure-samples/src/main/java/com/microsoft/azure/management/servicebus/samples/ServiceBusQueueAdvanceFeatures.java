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
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
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
 * - Add a queue in namespace with features session and dead-lettering.
 * - Create another queue with auto-forwarding to first queue. [Remove]
 * - Create another queue with dead-letter auto-forwarding to first queue. [Remove]
 * - Create second queue with Deduplication and AutoDeleteOnIdle feature
 * - Update second queue to change time for AutoDeleteOnIdle.
 * - Update first queue to disable dead-letter forwarding and with new Send authorization rule
 * - Update queue to remove the Send Authorization rule.
 * - Get default authorization rule.
 * - Get the keys from authorization rule to connect to queue.
 * - Send a "Hello" message to queue using Data plan sdk for Service Bus.
 * - Delete queue
 * - Delete namespace
 */
public final class ServiceBusQueueAdvanceFeatures {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String rgName = SdkContext.randomResourceName("rgSB04_", 24);
        final String namespaceName = SdkContext.randomResourceName("namespace", 20);
        final String queue1Name = SdkContext.randomResourceName("queue1_", 24);
        final String queue2Name = SdkContext.randomResourceName("queue2_", 24);
        final String sendRuleName = "SendRule";

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
            // Add a queue in namespace with features session and dead-lettering.
            System.out.println("Creating first queue " + queue1Name + ", with session, time to live and move to dead-letter queue features...");

            Queue firstQueue = serviceBusNamespace.queues().define(queue1Name)
                    .withSession()
                    .withDefaultMessageTTL(new Period().withMinutes(10))
                    .withExpiredMessageMovedToDeadLetterQueue()
                    .withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(40)
                    .create();
            Utils.print(firstQueue);

            //============================================================
            // Create second queue with Deduplication and AutoDeleteOnIdle feature

            System.out.println("Creating second queue " + queue2Name + ", with De-duplication and AutoDeleteOnIdle features...");

            Queue secondQueue = serviceBusNamespace.queues().define(queue2Name)
                    .withSizeInMB(2048)
                    .withDuplicateMessageDetection(new Period().withMinutes(10))
                    .withDeleteOnIdleDurationInMinutes(10)
                    .create();

            System.out.println("Created second queue in namespace");

            Utils.print(secondQueue);

            //============================================================
            // Update second queue to change time for AutoDeleteOnIdle.

            secondQueue = secondQueue.update()
                    .withDeleteOnIdleDurationInMinutes(5)
                    .apply();

            System.out.println("Updated second queue to change its auto deletion time");

            Utils.print(secondQueue);

            //=============================================================
            // Update first queue to disable dead-letter forwarding and with new Send authorization rule
            secondQueue = firstQueue.update()
                    .withoutExpiredMessageMovedToDeadLetterQueue()
                    .withNewSendRule(sendRuleName)
                    .apply();

            System.out.println("Updated first queue to change dead-letter forwarding");

            Utils.print(secondQueue);

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
            // Update first queue to remove Send Authorization rule.
            firstQueue.update().withoutAuthorizationRule(sendRuleName).apply();

            //=============================================================
            // Send a message to queue.

            try {
                Configuration config = Configuration.load();
                config.setProperty(ServiceBusConfiguration.CONNECTION_STRING, keys.primaryConnectionString());
                ServiceBusContract service = ServiceBusService.create(config);
                BrokeredMessage message = new BrokeredMessage("Hello");
                message.setSessionId("23424");
                service.sendQueueMessage(queue1Name, message);
            }
            catch (Exception ex) {
            }

            //=============================================================
            // Delete a queue and namespace
            System.out.println("Deleting queue " + queue1Name + "in namespace " + namespaceName + "...");
            serviceBusNamespace.queues().deleteByName(queue1Name);
            System.out.println("Deleted queue " + queue1Name + "...");

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
