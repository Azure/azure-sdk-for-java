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
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;

/**
 * Azure Service Bus basic scenario sample.
 * - Create namespace with a queue.
 * - Add another queue in same namespace.
 * - Update Queue.
 * - Update namespace
 * - List namespaces
 * - List queues
 * - Get default authorization rule.
 * - Regenerate the keys in the authorization rule.
 * - Get the keys from authorization rule to connect to queue.
 * - Send a "Hello" message to queue using Data plan sdk for Service Bus.
 * - Delete queue
 * - Delete namespace
 */
public final class ServiceBusQueueBasic {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        // New resources
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSB01_", 24);
        final String namespaceName = Utils.randomResourceName(azureResourceManager, "namespace", 20);
        final String queue1Name = Utils.randomResourceName(azureResourceManager, "queue1_", 24);
        final String queue2Name = Utils.randomResourceName(azureResourceManager, "queue2_", 24);

        try {
            //============================================================
            // Create a namespace.

            System.out.println("Creating name space " + namespaceName + " in resource group " + rgName + "...");

            ServiceBusNamespace serviceBusNamespace = azureResourceManager.serviceBusNamespaces()
                .define(namespaceName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withSku(NamespaceSku.BASIC)
                .withNewQueue(queue1Name, 1024)
                .create();

            System.out.println("Created service bus " + serviceBusNamespace.name());
            Utils.print(serviceBusNamespace);

            Queue firstQueue = serviceBusNamespace.queues().getByName(queue1Name);
            Utils.print(firstQueue);

            //============================================================
            // Create a second queue in same namespace

            System.out.println("Creating second queue " + queue2Name + " in namespace " + namespaceName + "...");

            Queue secondQueue = serviceBusNamespace.queues().define(queue2Name)
                .withExpiredMessageMovedToDeadLetterQueue()
                .withSizeInMB(2048)
                .withMessageLockDurationInSeconds(20)
                .create();

            System.out.println("Created second queue in namespace");

            Utils.print(secondQueue);

            //============================================================
            // Get and update second queue.

            secondQueue = serviceBusNamespace.queues().getByName(queue2Name);
            secondQueue = secondQueue.update().withSizeInMB(3072).apply();

            System.out.println("Updated second queue to change its size in MB");

            Utils.print(secondQueue);

            //=============================================================
            // Update namespace
            System.out.println("Updating sku of namespace " + serviceBusNamespace.name() + "...");

            serviceBusNamespace = serviceBusNamespace
                .update()
                .withSku(NamespaceSku.STANDARD)
                .apply();
            System.out.println("Updated sku of namespace " + serviceBusNamespace.name());

            //=============================================================
            // List namespaces

            System.out.println("List of namespaces in resource group " + rgName + "...");

            for (ServiceBusNamespace serviceBusNamespace1 : azureResourceManager.serviceBusNamespaces().listByResourceGroup(rgName)) {
                Utils.print(serviceBusNamespace1);
            }

            //=============================================================
            // List queues in namespaces

            PagedIterable<Queue> queues = serviceBusNamespace.queues().list();
            System.out.println("Number of queues in namespace :" + Utils.getSize(queues));

            for (Queue queue : queues) {
                Utils.print(queue);
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
            // Send a message to queue.
            ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(keys.primaryConnectionString())
                .sender()
                .queueName(queue1Name)
                .buildClient();
            sender.sendMessage(new ServiceBusMessage("Hello World").setSessionId("23424"));
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
