// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairing;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.ProvisioningStateDR;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.samples.Utils;

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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = azure.sdkContext().randomResourceName("rgNEMV_", 24);
        final String primaryNamespaceName = azure.sdkContext().randomResourceName("ns", 14);
        final String secondaryNamespaceName = azure.sdkContext().randomResourceName("ns", 14);
        final String geoDRName = azure.sdkContext().randomResourceName("geodr", 14);
        final String eventHubName = azure.sdkContext().randomResourceName("eh", 14);
        boolean isFailOverSucceeded = false;
        EventHubDisasterRecoveryPairing pairing = null;

        try {

            //============================================================
            // Create resource group for the namespaces and recovery pairings
            //
            ResourceGroup resourceGroup = azure.resourceGroups().define(rgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .create();

            System.out.println("Creating primary event hub namespace " + primaryNamespaceName);

            EventHubNamespace primaryNamespace = azure.eventHubNamespaces()
                .define(primaryNamespaceName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withExistingResourceGroup(resourceGroup)
                .create();

            System.out.println("Primary event hub namespace created");
            Utils.print(primaryNamespace);

            System.out.println("Creating secondary event hub namespace " + primaryNamespaceName);

            EventHubNamespace secondaryNamespace = azure.eventHubNamespaces()
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

            pairing = azure.eventHubDisasterRecoveryPairings()
                .define(geoDRName)
                .withExistingPrimaryNamespace(primaryNamespace)
                .withExistingSecondaryNamespace(secondaryNamespace)
                .create();

            while (pairing.provisioningState() != ProvisioningStateDR.SUCCEEDED) {
                pairing = pairing.refresh();
                SdkContext.sleep(15 * 1000);
                if (pairing.provisioningState() == ProvisioningStateDR.FAILED) {
                    throw new Exception("Provisioning state of the pairing is FAILED");
                }
            }

            System.out.println("Created geo-disaster recovery pairing " + geoDRName);
            Utils.print(pairing);

            //============================================================
            // Create an event hub and consumer group in primary namespace
            //

            System.out.println("Creating an event hub and consumer group in primary namespace");

            EventHub eventHubInPrimaryNamespace = azure.eventHubs()
                .define(eventHubName)
                .withExistingNamespace(primaryNamespace)
                .withNewConsumerGroup("consumerGrp1")
                .create();

            System.out.println("Created event hub and consumer group in primary namespace");
            Utils.print(eventHubInPrimaryNamespace);

            System.out.println("Waiting for 60 seconds to allow metadata to sync across primary and secondary");
            SdkContext.sleep(60 * 1000);    // Wait for syncing to finish

            System.out.println("Retrieving the event hubs in secondary namespace");

            EventHub eventHubInSecondaryNamespace = azure.eventHubs().getByName(rgName, secondaryNamespaceName, eventHubName);

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
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
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
