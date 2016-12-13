/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.ConnectionStringType;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebContainer;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.concurrent.TimeUnit;

/**
 * Azure App Service basic sample for managing web apps.
 *  - Create a storage account and upload a couple blobs
 *  - Create a web app that contains the connection string to the storage account
 *  - Deploy a Tomcat application that reads from the storage account
 *  - Clean up
 */
public final class ManageWebAppStorageAccountConnection {

    private static OkHttpClient httpClient;

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = ResourceNamer.randomResourceName("webapp1-", 20);
        final String app1Url        = app1Name + suffix;
        final String storageName    = ResourceNamer.randomResourceName("jsdkstore", 20);
        final String containerName  = ResourceNamer.randomResourceName("jcontainer", 20);
        final String planName       = ResourceNamer.randomResourceName("jplan_", 15);
        final String rgName         = ResourceNamer.randomResourceName("rg1NEMV_", 24);

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            try {


                //============================================================
                // Create a storage account for the web app to use

                System.out.println("Creating storage account " + storageName + "...");

                StorageAccount storageAccount = azure.storageAccounts()
                        .define(storageName)
                        .withRegion(Region.US_WEST)
                        .withNewResourceGroup(rgName)
                        .create();

                String accountKey = storageAccount.getKeys().get(0).value();

                String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                        storageAccount.name(), accountKey);

                System.out.println("Created storage account " + storageAccount.name());

                //============================================================
                // Upload a few files to the storage account blobs

                System.out.println("Uploading 2 blobs to container " + containerName + "...");

                CloudBlobContainer container = setUpStorageAccount(connectionString, containerName);
                uploadFileToContainer(container, "helloworld.war", ManageWebAppStorageAccountConnection.class.getResource("/helloworld.war").getPath());
                uploadFileToContainer(container, "install_apache.sh", ManageWebAppStorageAccountConnection.class.getResource("/install_apache.sh").getPath());

                System.out.println("Uploaded 2 blobs to container " + container.getName());

                //============================================================
                // Create a web app with a new app service plan

                System.out.println("Creating web app " + app1Name + "...");

                WebApp app1 = azure.webApps()
                        .define(app1Name)
                        .withExistingResourceGroup(rgName)
                        .withNewAppServicePlan(planName)
                        .withRegion(Region.US_WEST)
                        .withPricingTier(AppServicePricingTier.STANDARD_S1)
                        .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                        .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                        .withConnectionString("storage.connectionString", connectionString, ConnectionStringType.CUSTOM)
                        .withAppSetting("storage.containerName", containerName)
                        .create();

                System.out.println("Created web app " + app1.name());
                Utils.print(app1);

                //============================================================
                // Deploy a web app that connects to the storage account
                // Source code: https://github.com/jianghaolu/azure-samples-blob-explorer

                System.out.println("Deploying azure-samples-blob-traverser.war to " + app1Name + " through FTP...");

                uploadFileToFtp(app1.getPublishingProfile(), "azure-samples-blob-traverser.war", ManageWebAppStorageAccountConnection.class.getResourceAsStream("/azure-samples-blob-traverser.war"));

                System.out.println("Deployment azure-samples-blob-traverser.war to web app " + app1.name() + " completed");
                Utils.print(app1);

                // warm up
                System.out.println("Warming up " + app1Url + "/azure-samples-blob-traverser...");
                curl("http://" + app1Url + "/azure-samples-blob-traverser");
                Thread.sleep(5000);
                System.out.println("CURLing " + app1Url + "/azure-samples-blob-traverser...");
                System.out.println(curl("http://" + app1Url + "/azure-samples-blob-traverser"));




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

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String curl(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    static {
        httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    }

    private static void uploadFileToFtp(PublishingProfile profile, String fileName, InputStream file) throws Exception {
        FTPClient ftpClient = new FTPClient();
        String[] ftpUrlSegments = profile.ftpUrl().split("/", 2);
        String server = ftpUrlSegments[0];
        String path = "./site/wwwroot/webapps";
        ftpClient.connect(server);
        ftpClient.login(profile.ftpUsername(), profile.ftpPassword());
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.changeWorkingDirectory(path);
        ftpClient.storeFile(fileName, file);
        ftpClient.disconnect();
    }

    private static CloudBlobContainer setUpStorageAccount(String connectionString, String containerName) {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            // Create a blob service client
            CloudBlobClient blobClient = account.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(containerName);
            container.createIfNotExists();
            BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
            // Include public access in the permissions object
            containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
            // Set the permissions on the container
            container.uploadPermissions(containerPermissions);
            return container;
        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

    }

    private static void uploadFileToContainer(CloudBlobContainer container, String fileName, String filePath) {
        try {
            CloudBlob blob = container.getBlockBlobReference(fileName);
            blob.uploadFromFile(filePath);
        } catch (StorageException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
