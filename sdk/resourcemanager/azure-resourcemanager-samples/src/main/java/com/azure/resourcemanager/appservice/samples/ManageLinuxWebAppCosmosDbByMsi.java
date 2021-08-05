// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.containerregistry.models.AccessKeyType;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.RegistryCredentials;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.keyvault.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.samples.DockerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.core.command.PushImageResultCallback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Azure App Service basic sample for managing web apps.
 *  - Create a Cosmos DB with credentials stored in a Key Vault
 *  - Create a web app which interacts with the Cosmos DB by first
 *      reading the secrets from the Key Vault.
 *
 *      The source code of the web app is located at
 *      https://github.com/Microsoft/todo-app-java-on-azure/tree/keyvault-secrets
 */
public final class ManageLinuxWebAppCosmosDbByMsi {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @param clientId the client ID
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azure, String clientId) throws IOException, InterruptedException {
        // New resources
        final Region region         = Region.US_WEST;
        final String acrName        = Utils.randomResourceName(azure, "acr", 20);
        final String appName        = Utils.randomResourceName(azure, "webapp1-", 20);
        final String password       = Utils.password();
        final String rgName         = Utils.randomResourceName(azure, "rg1NEMV_", 24);
        final String vaultName      = Utils.randomResourceName(azure, "vault", 20);
        final String cosmosName     = Utils.randomResourceName(azure, "cosmosdb", 20);

        String servicePrincipalClientId = clientId; // replace with a real service principal client id

        try {
            //============================================================
            // Create a CosmosDB

            System.out.println("Creating a CosmosDB...");
            CosmosDBAccount cosmosDBAccount = azure.cosmosDBAccounts().define(cosmosName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withDataModelSql()
                    .withStrongConsistency()
                    .create();

            System.out.println("Created CosmosDB");
            Utils.print(cosmosDBAccount);

            //============================================================
            // Create a service principal

            ServicePrincipal servicePrincipal = azure.accessManagement().servicePrincipals()
                    .define(appName)
                    .withNewApplication()
                    .definePasswordCredential("password")
                        .attach()
                    .create();

            //=============================================================
            // If service principal client id and secret are not set via the local variables, attempt to read the service
            // principal client id and secret from a secondary ".azureauth" file set through an environment variable.
            //
            // If the environment variable was not set then reuse the main service principal set for running this sample.

            if (servicePrincipalClientId == null || servicePrincipalClientId.isEmpty()) {
                servicePrincipalClientId = System.getenv("AZURE_CLIENT_ID");
                if (servicePrincipalClientId == null || servicePrincipalClientId.isEmpty()) {
                    String envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION_2");

                    if (envSecondaryServicePrincipal == null || !envSecondaryServicePrincipal.isEmpty() || !Files.exists(Paths.get(envSecondaryServicePrincipal))) {
                        envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION");
                    }

                    servicePrincipalClientId = Utils.getSecondaryServicePrincipalClientID(envSecondaryServicePrincipal);
                }
            }

            //============================================================
            // Create a key vault

            Vault vault = azure.vaults()
                    .define(vaultName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .defineAccessPolicy()
                        .forServicePrincipal(servicePrincipalClientId)
                        .allowSecretAllPermissions()
                        .attach()
                    .defineAccessPolicy()
                        .forServicePrincipal(servicePrincipal)
                        .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.LIST)
                        .attach()
                    .create();

//            SdkContext.sleep(10000);

            //============================================================
            // Store Cosmos DB credentials in Key Vault

            vault.secrets().define("azure-documentdb-uri")
                .withValue(cosmosDBAccount.documentEndpoint())
                .create();
            vault.secrets().define("azure-documentdb-key")
                .withValue(cosmosDBAccount.listKeys().primaryMasterKey())
                .create();
            vault.secrets().define("azure-documentdb-database")
                .withValue("tododb")
                .create();

            //=============================================================
            // Create an Azure Container Registry to store and manage private Docker container images

            System.out.println("Creating an Azure Container Registry");

            long t1 = System.currentTimeMillis();

            Registry azureRegistry = azure.containerRegistries().define(acrName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withBasicSku()
                    .withRegistryNameAsAdminUser()
                    .create();

            long t2 = System.currentTimeMillis();
            System.out.println("Created Azure Container Registry: (took " + ((t2 - t1) / 1000) + " seconds) " + azureRegistry.id());
            Utils.print(azureRegistry);

            //=============================================================
            // Create a Docker client that will be used to push/pull images to/from the Azure Container Registry

            RegistryCredentials acrCredentials = azureRegistry.getCredentials();
            DockerClient dockerClient = DockerUtils.createDockerClient(azure, rgName, region,
                    azureRegistry.loginServerUrl(), acrCredentials.username(), acrCredentials.accessKeys().get(AccessKeyType.PRIMARY));

            String imageName = "tomcat:7.0-slim";
            String privateRepoUrl = azureRegistry.loginServerUrl() + "/todoapp";
            dockerClient.pullImageCmd(imageName)
                .withAuthConfig(new AuthConfig()) // anonymous
                .exec(new PullImageResultCallback())
                .awaitCompletion();

            String imageId = dockerClient.inspectImageCmd(imageName).exec().getId();
            dockerClient.tagImageCmd(imageId, privateRepoUrl, "latest").exec();

            dockerClient.pushImageCmd(privateRepoUrl)
                    .exec(new PushImageResultCallback()).awaitCompletion();

            //============================================================
            // Create a web app with a new app service plan

            System.out.println("Creating web app " + appName + " in resource group " + rgName + "...");

            WebApp app1 = azure.webApps()
                    .define(appName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withNewLinuxPlan(PricingTier.STANDARD_S1)
                    .withPrivateRegistryImage(privateRepoUrl, azureRegistry.loginServerUrl())
                    .withCredentials(acrCredentials.username(), acrCredentials.accessKeys().get(AccessKeyType.PRIMARY))
                    .withAppSetting("AZURE_KEYVAULT_URI", vault.vaultUri())
                    .withAppSetting("AZURE_KEYVAULT_CLIENT_ID", servicePrincipal.applicationId())
                    .withAppSetting("AZURE_KEYVAULT_CLIENT_KEY", password)
                    .create();

            System.out.println("Created web app " + app1.name());
            Utils.print(app1);

            return true;
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
            runSample(azureResourceManager, "");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
