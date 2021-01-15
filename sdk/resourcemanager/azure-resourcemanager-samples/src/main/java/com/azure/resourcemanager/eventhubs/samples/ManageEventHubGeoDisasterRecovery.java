// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairing;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.ProvisioningStateDR;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.samples.Utils;

import java.time.Duration;

/**
 * Azure Event Hub sample for managing geo disaster recovery pairing -
 *   - Create two event hub namespaces
 *   - Create a pairing between two namespaces
 *   - Create an event hub in the primary namespace and retrieve it from the secondary namespace
 *   - Retrieve the pairing connection string
 *   - Fail over so that secondary namespace become primary.
 */
public class ManageEventHubGeoDisasterRecovery {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEMV_", 24);
        final String primaryNamespaceName = Utils.randomResourceName(azureResourceManager, "ns", 14);
        final String secondaryNamespaceName = Utils.randomResourceName(azureResourceManager, "ns", 14);
        final String geoDRName = Utils.randomResourceName(azureResourceManager, "geodr", 14);
        final String eventHubName = Utils.randomResourceName(azureResourceManager, "eh", 14);
        boolean isFailOverSucceeded = false;
        EventHubDisasterRecoveryPairing pairing = null;

        try {

            //============================================================
            // Create resource group for the namespaces and recovery pairings
            //
            ResourceGroup resourceGroup = azureResourceManager.resourceGroups().define(rgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .create();

            System.out.println("Creating primary event hub namespace " + primaryNamespaceName);

            EventHubNamespace primaryNamespace = azureResourceManager.eventHubNamespaces()
                .define(primaryNamespaceName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withExistingResourceGroup(resourceGroup)
                .create();

            System.out.println("Primary event hub namespace created");
            Utils.print(primaryNamespace);

            System.out.println("Creating secondary event hub namespace " + primaryNamespaceName);

            EventHubNamespace secondaryNamespace = azureResourceManager.eventHubNamespaces()
                .define(secondaryNamespaceName)
                .withRegion(Region.US_NORTH_CENTRAL)
                .withExistingResourceGroup(resourceGroup)
                .create();

            System.out.println("Secondary event hub namespace created");
            Utils.print(secondaryNamespace);

            //============================================================
            // Create primary and secondary namespaces and recovery pairing
            //
            System.out.println("Creating geo-disaster recovery pairing " + geoDRName);

            pairing = azureResourceManager.eventHubDisasterRecoveryPairings()
                .define(geoDRName)
                .withExistingPrimaryNamespace(primaryNamespace)
                .withExistingSecondaryNamespace(secondaryNamespace)
                .create();

            while (pairing.provisioningState() != ProvisioningStateDR.SUCCEEDED) {
                pairing = pairing.refresh();
                ResourceManagerUtils.sleep(Duration.ofSeconds(15));
                if (pairing.provisioningState() == ProvisioningStateDR.FAILED) {
                    throw new IllegalStateException("Provisioning state of the pairing is FAILED");
                }
            }

            System.out.println("Created geo-disaster recovery pairing " + geoDRName);
            Utils.print(pairing);

            //============================================================
            // Create an event hub and consumer group in primary namespace
            //

            System.out.println("Creating an event hub and consumer group in primary namespace");

            EventHub eventHubInPrimaryNamespace = azureResourceManager.eventHubs()
                .define(eventHubName)
                .withExistingNamespace(primaryNamespace)
                .withNewConsumerGroup("consumerGrp1")
                .create();

            System.out.println("Created event hub and consumer group in primary namespace");
            Utils.print(eventHubInPrimaryNamespace);

            System.out.println("Waiting for 60 seconds to allow metadata to sync across primary and secondary");
            ResourceManagerUtils.sleep(Duration.ofMinutes(1));    // Wait for syncing to finish

            System.out.println("Retrieving the event hubs in secondary namespace");

            EventHub eventHubInSecondaryNamespace = azureResourceManager.eventHubs().getByName(rgName, secondaryNamespaceName, eventHubName);

            System.out.println("Retrieved the event hubs in secondary namespace");
            Utils.print(eventHubInSecondaryNamespace);

            //============================================================
            // Retrieving the connection string
            //
            PagedIterable<DisasterRecoveryPairingAuthorizationRule> rules = pairing.listAuthorizationRules();
            for (DisasterRecoveryPairingAuthorizationRule rule : rules) {
                DisasterRecoveryPairingAuthorizationKey key = rule.getKeys();
                Utils.print(key);
            }

            System.out.println("Initiating fail over");

            pairing.failOver();
            isFailOverSucceeded = true;

            System.out.println("Fail over initiated");
            return true;
        } finally {
            try {
                try {
                    // It is necessary to break pairing before deleting resource group
                    //
                    if (pairing != null && !isFailOverSucceeded) {
                        pairing.breakPairing();
                    }
                } catch (Exception ex) {
                    System.out.println("Pairing breaking failed:" + ex.getMessage());
                }

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
