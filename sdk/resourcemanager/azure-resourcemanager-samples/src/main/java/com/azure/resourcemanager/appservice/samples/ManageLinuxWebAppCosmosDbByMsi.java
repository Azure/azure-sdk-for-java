// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
//import com.azure.management.containerregistry.AccessKeyType;
//import com.azure.management.containerregistry.Registry;
//import com.azure.management.containerregistry.RegistryCredentials;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;

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
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        System.out.println("removed later");
        return true;
//        // New resources
//        final Region region         = Region.US_WEST;
//        final String acrName        = azure.sdkContext().randomResourceName("acr", 20);
//        final String appName        = azure.sdkContext().randomResourceName("webapp1-", 20);
//        final String password       = SdkContext.randomUuid();
//        final String rgName         = azure.sdkContext().randomResourceName("rg1NEMV_", 24);
//        final String vaultName      = azure.sdkContext().randomResourceName("vault", 20);
//        final String cosmosName     = azure.sdkContext().randomResourceName("cosmosdb", 20);
//
//        try {
//            //============================================================
//            // Create a CosmosDB
//
//            System.out.println("Creating a CosmosDB...");
//            CosmosDBAccount cosmosDBAccount = azure.cosmosDBAccounts().define(cosmosName)
//                    .withRegion(region)
//                    .withNewResourceGroup(rgName)
//                    .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
//                    .withEventualConsistency()
//                    .withWriteReplication(Region.US_EAST)
//                    .withReadReplication(Region.US_CENTRAL)
//                    .create();
//
//            System.out.println("Created CosmosDB");
//            Utils.print(cosmosDBAccount);
//
//            //============================================================
//            // Create a service principal
//
//            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
//            final ApplicationTokenCredential credentials = ApplicationTokenCredential.fromFile(credFile);
//
//            ServicePrincipal servicePrincipal = Azure.authenticate(credentials).servicePrincipals()
//                    .define(appName)
//                    .withNewApplication("http://" + appName + ".azurewebsites.net")
//                    .definePasswordCredential("password")
//                        .withPasswordValue(password)
//                        .attach()
//                    .create();
//
//            //============================================================
//            // Create a key vault
//
//            Vault vault = azure.vaults()
//                    .define(vaultName)
//                    .withRegion(region)
//                    .withExistingResourceGroup(rgName)
//                    .defineAccessPolicy()
//                        .forServicePrincipal(credentials.getClientId())
//                        .allowSecretAllPermissions()
//                        .attach()
//                    .defineAccessPolicy()
//                        .forServicePrincipal(servicePrincipal)
//                        .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.LIST)
//                        .attach()
//                    .create();
//
//            SdkContext.sleep(10000);
//
//            KeyVaultClient client = new KeyVaultClient(new KeyVaultCredentials() {
//                @Override
//                public String doAuthenticate(String authorization, String resource, String scope) {
//                    try {
//                        return credentials.getToken(resource);
//                    } catch (IOException e) {
//                        return null;
//                    }
//                }
//            });
//
//            //============================================================
//            // Store Cosmos DB credentials in Key Vault
//
//            client.setSecret(new SetSecretRequest.Builder(vault.vaultUri(), "azure-documentdb-uri", cosmosDBAccount.documentEndpoint()).build());
//            client.setSecret(new SetSecretRequest.Builder(vault.vaultUri(), "azure-documentdb-key", cosmosDBAccount.listKeys().primaryMasterKey()).build());
//            client.setSecret(new SetSecretRequest.Builder(vault.vaultUri(), "azure-documentdb-database", "tododb").build());
////            client.setSecret(new SetSecretRequest.Builder(vault.vaultUri(), "azure.documentdb.uri", cosmosDBAccount.documentEndpoint()).build());
////            client.setSecret(new SetSecretRequest.Builder(vault.vaultUri(), "azure.documentdb.key", cosmosDBAccount.listKeys().primaryMasterKey()).build());
////            client.setSecret(new SetSecretRequest.Builder(vault.vaultUri(), "azure.documentdb.database", "tododb").build());
//
//            //=============================================================
//            // Create an Azure Container Registry to store and manage private Docker container images
//
//            System.out.println("Creating an Azure Container Registry");
//
//            Date t1 = new Date();
//
//            Registry azureRegistry = azure.containerRegistries().define(acrName)
//                    .withRegion(region)
//                    .withNewResourceGroup(rgName)
//                    .withBasicSku()
//                    .withRegistryNameAsAdminUser()
//                    .create();
//
//            Date t2 = new Date();
//            System.out.println("Created Azure Container Registry: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + azureRegistry.id());
//            Utils.print(azureRegistry);
//
//            //=============================================================
//            // Create a Docker client that will be used to push/pull images to/from the Azure Container Registry
//
//            RegistryCredentials acrCredentials = azureRegistry.getCredentials();
//            DockerClient dockerClient = DockerUtils.createDockerClient(azure, rgName, region,
//                    azureRegistry.loginServerUrl(), acrCredentials.username(), acrCredentials.accessKeys().get(AccessKeyType.PRIMARY));
//
//            String privateRepoUrl = azureRegistry.loginServerUrl() + "/todoapp";
//            dockerClient.buildImageCmd(new File(ManageLinuxWebAppCosmosDbByMsi.class.getResource("/todoapp-cosmosdb/Dockerfile").getFile()))
//                    .withTag(privateRepoUrl)
//                    .exec(new BuildImageResultCallback()).awaitCompletion();
//
//            dockerClient.pushImageCmd(privateRepoUrl)
//                    .withAuthConfig(dockerClient.authConfig())
//                    .exec(new PushImageResultCallback()).awaitSuccess();
//
//            //============================================================
//            // Create a web app with a new app service plan
//
//            System.out.println("Creating web app " + appName + " in resource group " + rgName + "...");
//
//            WebApp app1 = azure.webApps()
//                    .define(appName)
//                    .withRegion(Region.US_WEST)
//                    .withNewResourceGroup(rgName)
//                    .withNewLinuxPlan(PricingTier.STANDARD_S1)
//                    .withPrivateRegistryImage(privateRepoUrl, azureRegistry.loginServerUrl())
//                    .withCredentials(acrCredentials.username(), acrCredentials.accessKeys().get(AccessKeyType.PRIMARY))
//                    .withAppSetting("AZURE_KEYVAULT_URI", vault.vaultUri())
//                    .withAppSetting("AZURE_KEYVAULT_CLIENT_ID", servicePrincipal.applicationId())
//                    .withAppSetting("AZURE_KEYVAULT_CLIENT_KEY", password)
//                    .create();
//
//            System.out.println("Created web app " + app1.name());
//            Utils.print(app1);
//
//            return true;
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//            e.printStackTrace();
//        } finally {
//            try {
//                System.out.println("Deleting Resource Group: " + rgName);
//                azure.resourceGroups().beginDeleteByName(rgName);
//                System.out.println("Deleted Resource Group: " + rgName);
//            } catch (NullPointerException npe) {
//                System.out.println("Did not create any resources in Azure. No clean up is necessary");
//            } catch (Exception g) {
//                g.printStackTrace();
//            }
//        }
//        return false;
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
