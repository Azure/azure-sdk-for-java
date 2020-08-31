// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.resourcemanager.monitor.models.DiagnosticSetting;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.time.Duration;

/**
 * Stream Azure Service Logs and Metrics for consumption through Event Hub.
 *   - Create a DocumentDB instance
 *   - Creates a Event Hub namespace and an Event Hub in it
 *   - Retrieve the root namespace authorization rule
 *   - Enable diagnostics on a existing cosmosDB to stream events to event hub
 */
public class ManageEventHubEvents {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_EAST;
        final String rgName = azure.sdkContext().randomResourceName("rgEvHb", 24);
        final String namespaceName = azure.sdkContext().randomResourceName("ns", 24);
        final String eventHubName = "FirstEventHub";
        String diagnosticSettingId = null;

        try {
            //=============================================================
            // Creates a Cosmos DB.
            //
            CosmosDBAccount docDb = azure.cosmosDBAccounts()
                .define(namespaceName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withKind(DatabaseAccountKind.MONGO_DB)
                .withEventualConsistency()
                .withWriteReplication(Region.US_WEST)
                .withReadReplication(Region.US_CENTRAL)
                .create();

            System.out.println("Created a DocumentDb instance.");
            Utils.print(docDb);
            //=============================================================
            // Creates a Event Hub namespace and an Event Hub in it.
            //

            System.out.println("Creating event hub namespace and event hub");

            EventHubNamespace namespace = azure.eventHubNamespaces()
                .define(namespaceName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewEventHub(eventHubName)
                .create();

            System.out.println(String.format("Created event hub namespace %s and event hub %s ", namespace.name(), eventHubName));
            System.out.println();
            Utils.print(namespace);

            //=============================================================
            // Retrieve the root namespace authorization rule.
            //

            System.out.println("Retrieving the namespace authorization rule");

            EventHubNamespaceAuthorizationRule eventHubAuthRule = azure.eventHubNamespaces().authorizationRules()
                .getByName(namespace.resourceGroupName(), namespace.name(), "RootManageSharedAccessKey");

            System.out.println("Namespace authorization rule Retrieved");

            //=============================================================
            // Enable diagnostics on a cosmosDB to stream events to event hub
            //

            System.out.println("Enabling diagnostics events of a cosmosdb to stream to event hub");

            // Store Id of created Diagnostic settings only for clean-up
            DiagnosticSetting ds  = azure.diagnosticSettings()
                .define("DiaEventHub")
                .withResource(docDb.id())
                .withEventHub(eventHubAuthRule.id(), eventHubName)
                .withLog("DataPlaneRequests", 0)
                .withLog("MongoRequests", 0)
                .withMetric("AllMetrics", Duration.ofMinutes(5), 0)
                .create();

            Utils.print(ds);
            diagnosticSettingId = ds.id();

            System.out.println("Streaming of diagnostics events to event hub is enabled");

            //=============================================================
            // Listen for events from event hub using Event Hub dataplane APIs.

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                if (diagnosticSettingId != null) {
                    System.out.println("Deleting Diagnostic Setting: " + diagnosticSettingId);
                    azure.diagnosticSettings().deleteById(diagnosticSettingId);
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
