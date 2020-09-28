// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;

/**
 * Azure Event Hub sample for managing event hub -
 *   - Create an event hub namespace
 *   - Create an event hub in the namespace with data capture enabled along with a consumer group and rule
 *   - List consumer groups in the event hub
 *   - Create a second event hub in the namespace
 *   - Create a consumer group in the second event hub
 *   - List consumer groups in the second event hub
 *   - Create an event hub namespace along with event hub.
 */
public class ManageEventHub {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEMV_", 24);
        final String namespaceName1 = Utils.randomResourceName(azureResourceManager, "ns", 14);
        final String namespaceName2 = Utils.randomResourceName(azureResourceManager, "ns", 14);
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "stg", 14);
        final String eventHubName1 = Utils.randomResourceName(azureResourceManager, "eh", 14);
        final String eventHubName2 = Utils.randomResourceName(azureResourceManager, "eh", 14);
        try {

            //============================================================
            // Create an event hub namespace
            //
            System.out.println("Creating a namespace");

            EventHubNamespace namespace1 = azureResourceManager.eventHubNamespaces()
                .define(namespaceName1)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .create();

            Utils.print(namespace1);
            System.out.println("Created a namespace");
            //============================================================
            // Create an event hub in the namespace with data capture enabled, with consumer group and auth rule
            //

            Creatable<StorageAccount> storageAccountCreatable = azureResourceManager.storageAccounts()
                .define(storageAccountName)
                .withRegion(Region.US_EAST2)
                .withExistingResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_LRS);

            System.out.println("Creating an event hub with data capture enabled with a consumer group and rule in it");

            EventHub eventHub1 = azureResourceManager.eventHubs()
                .define(eventHubName1)
                .withExistingNamespace(namespace1)
                // Optional - configure data capture
                .withNewStorageAccountForCapturedData(storageAccountCreatable, "datacpt")
                .withDataCaptureEnabled()
                // Optional - create one consumer group in event hub
                .withNewConsumerGroup("cg1", "sometadata")
                // Optional - create an authorization rule for event hub
                .withNewListenRule("listenrule1")
                .create();

            System.out.println("Created an event hub with data capture enabled with a consumer group and rule in it");
            Utils.print(eventHub1);

            //============================================================
            // Retrieve consumer groups in the event hub
            //
            System.out.println("Retrieving consumer groups");

            PagedIterable<EventHubConsumerGroup> consumerGroups = eventHub1.listConsumerGroups();

            System.out.println("Retrieved consumer groups");
            for (EventHubConsumerGroup group : consumerGroups) {
                Utils.print(group);
            }

            //============================================================
            // Create another event hub in the namespace using event hub accessor in namespace accessor
            //

            System.out.println("Creating another event hub in the namespace");

            EventHub eventHub2 = azureResourceManager.eventHubNamespaces()
                .eventHubs()
                .define(eventHubName2)
                .withExistingNamespace(namespace1)
                .create();

            System.out.println("Created second event hub");
            Utils.print(eventHub2);

            //============================================================
            // Create a consumer group in the event hub using consumer group accessor in event hub accessor
            //

            System.out.println("Creating a consumer group in the second event hub");

            EventHubConsumerGroup consumerGroup2 = azureResourceManager.eventHubNamespaces()
                .eventHubs()
                .consumerGroups()
                .define("cg2")
                .withExistingEventHub(eventHub2)
                // Optional
                .withUserMetadata("sometadata")
                .create();

            System.out.println("Created a consumer group in the second event hub");
            Utils.print(consumerGroup2);

            //============================================================
            // Retrieve consumer groups in the event hub
            //
            System.out.println("Retrieving consumer groups in the second event hub");

            consumerGroups = eventHub2.listConsumerGroups();

            System.out.println("Retrieved consumer groups in the seoond event hub");
            for (EventHubConsumerGroup group : consumerGroups) {
                Utils.print(group);
            }

            //============================================================
            // Create an event hub namespace with event hub
            //

            System.out.println("Creating an event hub namespace along with event hub");

            EventHubNamespace namespace2 = azureResourceManager.eventHubNamespaces()
                .define(namespaceName2)
                .withRegion(Region.US_EAST2)
                .withExistingResourceGroup(rgName)
                .withNewEventHub(eventHubName2)
                .create();

            System.out.println("Created an event hub namespace along with event hub");
            Utils.print(namespace2);
            for (EventHub eh : namespace2.listEventHubs()) {
                Utils.print(eh);
            }
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().deleteByName(rgName);
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
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

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
