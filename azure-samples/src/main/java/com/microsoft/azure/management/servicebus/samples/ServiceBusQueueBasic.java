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
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.rest.LogLevel;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;

import java.io.File;

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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String rgName = SdkContext.randomResourceName("rgSB01_", 24);
        final String namespaceName = SdkContext.randomResourceName("namespace", 20);
        final String queue1Name = SdkContext.randomResourceName("queue1_", 24);
        final String queue2Name = SdkContext.randomResourceName("queue2_", 24);

        try {
            //============================================================
            // Create a namespace.

            System.out.println("Creating name space " + namespaceName + " in resource group " + rgName + "...");

            ServiceBusNamespace serviceBusNamespace = azure.serviceBusNamespaces()
                    .define(namespaceName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withSku(NamespaceSku.PREMIUM_CAPACITY1)
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
                    .withSku(NamespaceSku.PREMIUM_CAPACITY2)
                    .apply();
            System.out.println("Updated sku of namespace " + serviceBusNamespace.name());

            //=============================================================
            // List namespaces

            System.out.println("List of namespaces in resource group " + rgName + "...");

            for (ServiceBusNamespace serviceBusNamespace1 : azure.serviceBusNamespaces().listByResourceGroup(rgName)) {
                Utils.print(serviceBusNamespace1);
            }

            //=============================================================
            // List queues in namespaces

            PagedList<Queue> queues = serviceBusNamespace.queues().list();
            System.out.println("Number of queues in namespace :" + queues.size());

            for (Queue queue : queues) {
                Utils.print(queue);
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
            // Send a message to queue.
            try {
                Configuration config = Configuration.load();
                config.setProperty(ServiceBusConfiguration.CONNECTION_STRING, keys.primaryConnectionString());
                ServiceBusContract service = ServiceBusService.create(config);
                service.sendQueueMessage(queue1Name, new BrokeredMessage("Hello World"));
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
