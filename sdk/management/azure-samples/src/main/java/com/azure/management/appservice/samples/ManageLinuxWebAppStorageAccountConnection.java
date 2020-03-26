/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.samples;

import com.azure.management.Azure;
import com.azure.management.appservice.ConnectionStringType;
import com.azure.management.appservice.PricingTier;
import com.azure.management.appservice.RuntimeStack;
import com.azure.management.appservice.WebApp;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.samples.Utils;
import com.azure.management.storage.StorageAccount;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.PublicAccessType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collections;


/**
 * Azure App Service basic sample for managing web apps.
 *  - Create a storage account and upload a couple blobs
 *  - Create a web app that contains the connection string to the storage account
 *  - Deploy a Tomcat application that reads from the storage account
 *  - Clean up
 */
public final class ManageLinuxWebAppStorageAccountConnection {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = azure.sdkContext().randomResourceName("webapp1-", 20);
        final String app1Url        = app1Name + suffix;
        final String storageName    = azure.sdkContext().randomResourceName("jsdkstore", 20);
        final String containerName  = azure.sdkContext().randomResourceName("jcontainer", 20);
        final String rgName         = azure.sdkContext().randomResourceName("rg1NEMV_", 24);

        try {

            //============================================================
            // Create a storage account for the web app to use

            System.out.println("Creating storage account " + storageName + "...");

            StorageAccount storageAccount = azure.storageAccounts().define(storageName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .create();

            String accountKey = storageAccount.getKeys().get(0).getValue();

            String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                    storageAccount.name(), accountKey);

            System.out.println("Created storage account " + storageAccount.name());

            //============================================================
            // Upload a few files to the storage account blobs

            System.out.println("Uploading 2 blobs to container " + containerName + "...");

            BlobContainerClient container = setUpStorageAccount(connectionString, containerName);
            uploadFileToContainer(container, "helloworld.war", ManageLinuxWebAppStorageAccountConnection.class.getResource("/helloworld.war").getPath());
            uploadFileToContainer(container, "install_apache.sh", ManageLinuxWebAppStorageAccountConnection.class.getResource("/install_apache.sh").getPath());

            System.out.println("Uploaded 2 blobs to container " + container.getBlobContainerName());

            //============================================================
            // Create a web app with a new app service plan

            System.out.println("Creating web app " + app1Name + "...");

            // FIXME the env variable will not work in linux since dot is not allowed in env variable name
            WebApp app1 = azure.webApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rgName)
                    .withNewLinuxPlan(PricingTier.STANDARD_S1)
                    .withBuiltInImage(RuntimeStack.TOMCAT_8_5_JRE8)
                    .withConnectionString("storage.connectionString", connectionString, ConnectionStringType.CUSTOM)
                    .withAppSetting("storage.containerName", containerName)
                    .create();

            System.out.println("Created web app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Deploy a web app that connects to the storage account
            // Source code: https://github.com/jianghaolu/azure-samples-blob-explorer

            System.out.println("Deploying azure-samples-blob-traverser.war to " + app1Name + " through FTP...");

            Utils.uploadFileToWebApp(app1.getPublishingProfile(), "azure-samples-blob-traverser.war", ManageLinuxWebAppStorageAccountConnection.class.getResourceAsStream("/azure-samples-blob-traverser.war"));

            System.out.println("Deployment azure-samples-blob-traverser.war to web app " + app1.name() + " completed");
            Utils.print(app1);

            // warm up
            System.out.println("Warming up " + app1Url + "/azure-samples-blob-traverser...");
            Utils.curl("http://" + app1Url + "/azure-samples-blob-traverser/");
            SdkContext.sleep(5000);
            System.out.println("CURLing " + app1Url + "/azure-samples-blob-traverser...");
            System.out.println(Utils.curl("http://" + app1Url + "/azure-samples-blob-traverser/"));

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

            Azure azure = Azure.configure()
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
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

    private static BlobContainerClient setUpStorageAccount(String connectionString, String containerName) {
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        blobContainerClient.create();

        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
                .setId("webapp")
                .setAccessPolicy(new BlobAccessPolicy()
                        .setStartsOn(OffsetDateTime.now())
                        .setExpiresOn(OffsetDateTime.now().plusDays(7))
                        .setPermissions("rl"));

        blobContainerClient.setAccessPolicy(PublicAccessType.CONTAINER, Collections.singletonList(identifier));

        return blobContainerClient;
    }

    private static void uploadFileToContainer(BlobContainerClient blobContainerClient, String fileName, String filePath) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        File file = new File(filePath);
        try (InputStream is = new FileInputStream(file)) {
            blobClient.upload(is, file.length());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}