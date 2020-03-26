/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.samples;

import com.azure.management.ApplicationTokenCredential;
import com.azure.management.Azure;
import com.azure.management.appservice.JavaVersion;
import com.azure.management.appservice.PricingTier;
import com.azure.management.appservice.WebApp;
import com.azure.management.appservice.WebContainer;
import com.azure.management.cosmosdb.CosmosDBAccount;
import com.azure.management.cosmosdb.DatabaseAccountKind;
import com.azure.management.keyvault.Vault;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.samples.Utils;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;

import java.io.File;


/**
 * Azure App Service basic sample for managing web apps.
 *  - Create a Cosmos DB with credentials stored in a Key Vault
 *  - Create a web app which interacts with the Cosmos DB by first
 *      reading the secrets from the Key Vault.
 *
 *      The source code of the web app is located at
 *      https://github.com/Microsoft/todo-app-java-on-azure/tree/keyvault-secrets
 */
public final class ManageWebAppCosmosDbThroughKeyVault {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final Region region         = Region.US_WEST;
        final String appName        = azure.sdkContext().randomResourceName("webapp1-", 20);
        final String rgName         = azure.sdkContext().randomResourceName("rg1NEMV_", 24);
        final String vaultName      = azure.sdkContext().randomResourceName("vault", 20);
        final String cosmosName     = azure.sdkContext().randomResourceName("cosmosdb", 20);
        final String appUrl         = appName + ".azurewebsites.net";

        try {
            //============================================================
            // Create a CosmosDB

            System.out.println("Creating a CosmosDB...");
            CosmosDBAccount cosmosDBAccount = azure.cosmosDBAccounts().define(cosmosName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                    .withEventualConsistency()
                    .withWriteReplication(Region.US_EAST)
                    .withReadReplication(Region.US_CENTRAL)
                    .create();

            System.out.println("Created CosmosDB");
            Utils.print(cosmosDBAccount);

            //============================================================
            // Create a key vault

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            final ApplicationTokenCredential credentials = ApplicationTokenCredential.fromFile(credFile);

            Vault vault = azure.vaults()
                    .define(vaultName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .defineAccessPolicy()
                        .forServicePrincipal(credentials.getClientId())
                        .allowSecretAllPermissions()
                        .attach()
                    .create();

            SdkContext.sleep(10000);

            //============================================================
            // Store Cosmos DB credentials in Key Vault

            vault.secrets()
                    .define("azure-documentdb-uri")
                    .withValue(cosmosDBAccount.documentEndpoint())
                    .create();
            vault.secrets()
                    .define("azure-documentdb-key")
                    .withValue(cosmosDBAccount.listKeys().primaryMasterKey())
                    .create();
            vault.secrets()
                    .define("azure-documentdb-database")
                    .withValue("tododb")
                    .create();

            //============================================================
            // Create a web app with a new app service plan

            System.out.println("Creating web app " + appName + " in resource group " + rgName + "...");

            WebApp app = azure.webApps()
                    .define(appName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withNewWindowsPlan(PricingTier.STANDARD_S1)
                    .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                    .withWebContainer(WebContainer.TOMCAT_8_5_NEWEST)
                    .withAppSetting("AZURE_KEYVAULT_URI", vault.vaultUri())
                    .withSystemAssignedManagedServiceIdentity()
                    .create();

            System.out.println("Created web app " + app.name());
            Utils.print(app);

            //============================================================
            // Update vault to allow the web app to access

            vault.update()
                    .defineAccessPolicy()
                        .forObjectId(app.systemAssignedManagedServiceIdentityPrincipalId())
                        .allowSecretAllPermissions()
                        .attach()
                    .apply();

            //============================================================
            // Deploy to app through FTP

            System.out.println("Deploying a spring boot app to " + appName + " through FTP...");

            Utils.uploadFileToWebAppWwwRoot(app.getPublishingProfile(), "ROOT.jar", ManageWebAppCosmosDbThroughKeyVault.class.getResourceAsStream("/todo-app-java-on-azure-1.0-SNAPSHOT.jar"));
            Utils.uploadFileToWebAppWwwRoot(app.getPublishingProfile(), "web.config", ManageWebAppCosmosDbThroughKeyVault.class.getResourceAsStream("/web.config"));

            System.out.println("Deployment to web app " + app.name() + " completed");
            Utils.print(app);

            // warm up
            System.out.println("Warming up " + appUrl + "...");
            Utils.curl("http://" + appUrl);
            SdkContext.sleep(10000);
            System.out.println("CURLing " + appUrl);
            System.out.println(Utils.curl("http://" + appUrl));


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
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
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