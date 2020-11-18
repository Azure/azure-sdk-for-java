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
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;

import java.time.Duration;

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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        // New resources
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSB04_", 24);
        final String namespaceName = Utils.randomResourceName(azureResourceManager, "namespace", 20);
        final String queue1Name = Utils.randomResourceName(azureResourceManager, "queue1_", 24);
        final String queue2Name = Utils.randomResourceName(azureResourceManager, "queue2_", 24);
        final String sendRuleName = "SendRule";

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
            // Add a queue in namespace with features session and dead-lettering.
            System.out.println("Creating first queue " + queue1Name + ", with session, time to live and move to dead-letter queue features...");

            Queue firstQueue = serviceBusNamespace.queues().define(queue1Name)
                .withSession()
                .withDefaultMessageTTL(Duration.ofMinutes(10))
                .withExpiredMessageMovedToDeadLetterQueue()
                .withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(40)
                .create();
            Utils.print(firstQueue);

            //============================================================
            // Create second queue with Deduplication and AutoDeleteOnIdle feature

            System.out.println("Creating second queue " + queue2Name + ", with De-duplication and AutoDeleteOnIdle features...");

            Queue secondQueue = serviceBusNamespace.queues().define(queue2Name)
                .withSizeInMB(2048)
                .withDuplicateMessageDetection(Duration.ofMinutes(10))
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

            PagedIterable<NamespaceAuthorizationRule> namespaceAuthorizationRules = serviceBusNamespace.authorizationRules().list();
            System.out.println("Number of authorization rule for namespace :" + Utils.getSize(namespaceAuthorizationRules));


            for (NamespaceAuthorizationRule namespaceAuthorizationRule: namespaceAuthorizationRules) {
                Utils.print(namespaceAuthorizationRule);
            }

            System.out.println("Getting keys for authorization rule ...");

            AuthorizationKeys keys = namespaceAuthorizationRules.iterator().next().getKeys();
            Utils.print(keys);

            //=============================================================
            // Update first queue to remove Send Authorization rule.
            firstQueue.update().withoutAuthorizationRule(sendRuleName).apply();

            //=============================================================
            // Send a message to queue.
            ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(keys.primaryConnectionString())
                .sender()
                .queueName(queue1Name)
                .buildClient();
            sender.sendMessage(new ServiceBusMessage("Hello").setSessionId("23424"));
            sender.close();

            //=============================================================
            // Delete a queue and namespace
            System.out.println("Deleting queue " + queue1Name + "in namespace " + namespaceName + "...");
            serviceBusNamespace.queues().deleteByName(queue1Name);
            System.out.println("Deleted queue " + queue1Name + "...");

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
