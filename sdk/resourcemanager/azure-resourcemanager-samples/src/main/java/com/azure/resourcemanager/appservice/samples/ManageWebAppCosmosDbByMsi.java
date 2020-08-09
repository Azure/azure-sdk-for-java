// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;
import com.azure.core.http.policy.HttpLogDetailLevel;

/**
 * Azure App Service basic sample for managing web apps.
 *  - Create a Cosmos DB with credentials stored in a Key Vault
 *  - Create a web app which interacts with the Cosmos DB by first
 *      reading the secrets from the Key Vault.
 *
 *      The source code of the web app is located at
 *      https://github.com/Microsoft/todo-app-java-on-azure/tree/keyvault-secrets
 */
public final class ManageWebAppCosmosDbByMsi {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @param credential the credential to use
     * @param clientId the client ID
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure, TokenCredential credential, String clientId) {
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

            Vault vault = azure.vaults()
                    .define(vaultName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .defineAccessPolicy()
                        .forServicePrincipal(clientId)
                        .allowSecretAllPermissions()
                        .attach()
                    .create();

            SdkContext.sleep(10000);

            SecretClient client = new SecretClientBuilder()
                    .vaultUrl(vault.vaultUri())
                    .credential(credential)
                    .httpClient(vault.manager().httpPipeline().getHttpClient())
                    .buildClient();

            //============================================================
            // Store Cosmos DB credentials in Key Vault

            client.setSecret(new KeyVaultSecret("azure-documentdb-uri", cosmosDBAccount.documentEndpoint()));
            client.setSecret(new KeyVaultSecret("azure-documentdb-key", cosmosDBAccount.listKeys().primaryMasterKey()));
            client.setSecret(new KeyVaultSecret("azure-documentdb-database", "tododb"));

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

            Utils.uploadFileViaFtp(app.getPublishingProfile(), "ROOT.jar", ManageWebAppCosmosDbByMsi.class.getResourceAsStream("/todo-app-java-on-azure-1.0-SNAPSHOT.jar"));
            Utils.uploadFileViaFtp(app.getPublishingProfile(), "web.config", ManageWebAppCosmosDbByMsi.class.getResourceAsStream("/web.config"));

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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();
            final Configuration configuration = Configuration.getGlobalConfiguration();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            runSample(azure, credential, configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
