// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.SampleUtils;
import com.azure.resourcemanager.storage.models.StorageAccount;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Azure App Service sample for connecting a web app to a storage account without secrets (passwordless).
 *  - Create a storage account with shared-key access disabled
 *  - Create a Node.js web app with a system-assigned managed identity
 *  - Grant that identity the Storage Blob Data Contributor role on the account
 *  - Expose only the blob endpoint (no account key or connection string) to the web app
 *  - Deploy a small Node.js app that reads/writes a blob using that managed identity
 * <p>
 * The deployed app authenticates to Blob storage with {@code DefaultAzureCredential}, which picks up the web app's
 * managed identity. Its source is under {@code src/samples/resources/appservice/mi-blob-verify}.
 */
public final class ConnectWebAppToStorageAccount {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);
        final String appName = SampleUtils.randomResourceName(azureResourceManager, "webapp-", 20);
        final String storageName = SampleUtils.randomResourceName(azureResourceManager, "jsdkstore", 20);
        final String containerName = SampleUtils.randomResourceName(azureResourceManager, "jcontainer", 20);

        try {
            // Create a storage account with shared-key access disabled, so data-plane access requires
            // Microsoft Entra credentials (passwordless) rather than an account key.
            StorageAccount storageAccount = azureResourceManager.storageAccounts()
                .define(storageName)
                .withRegion(Region.JAPAN_EAST)
                .withNewResourceGroup(rgName)
                .disableSharedKeyAccess()
                .create();

            // Create a Node.js web app with a system-assigned managed identity and expose only the blob endpoint.
            // HTTPS-only is enforced here; minimum TLS 1.2 and FTPS-only are already the App Service defaults.
            WebApp app = azureResourceManager.webApps()
                .define(appName)
                .withRegion(Region.JAPAN_EAST)
                .withExistingResourceGroup(rgName)
                .withNewLinuxPlan(PricingTier.STANDARD_S1)
                .withBuiltInImage(RuntimeStack.NODEJS_22_LTS)
                .withHttpsOnly(true)
                .withSystemAssignedManagedServiceIdentity()
                .withAppSetting("STORAGE_BLOB_ENDPOINT", storageAccount.endPoints().primary().blob())
                .withAppSetting("STORAGE_CONTAINER_NAME", containerName)
                .create();

            // Grant the web app's managed identity data-plane access to blobs (least-privilege, passwordless).
            azureResourceManager.accessManagement()
                .roleAssignments()
                .define(SampleUtils.randomUuid(azureResourceManager))
                .forObjectId(app.systemAssignedManagedServiceIdentityPrincipalId())
                .withBuiltInRole(BuiltInRole.STORAGE_BLOB_DATA_CONTRIBUTOR)
                .withResourceScope(storageAccount)
                .create();

            // Deploy the small Node.js app that touches a blob using the web app's managed identity.
            // A freshly created web app may briefly have an initial deployment in progress, so retry on HTTP 409.
            File appPackage = nodeAppPackage();
            for (int i = 0;; i++) {
                try {
                    app.zipDeploy(appPackage);
                    break;
                } catch (HttpResponseException e) {
                    if (i >= 5 || e.getResponse() == null || e.getResponse().getStatusCode() != 409) {
                        throw e;
                    }
                    ResourceManagerUtils.sleep(Duration.ofSeconds(30));
                }
            }

            System.out.println("Connected web app " + app.name() + " to storage account " + storageAccount.name());
            // To verify manually, browse to https://<app>.azurewebsites.net/ once role assignment has propagated;
            // the app returns {"ok":true,...} when it can read/write a blob using its managed identity.
            System.out.println("Verify passwordless access at: https://" + app.defaultHostname() + "/");
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a zip package (server.js + package.json) for the small Node.js verification app from the sample
     * resources. The verifier has no npm dependencies, so App Service deploys it without a build step.
     *
     * @return the zip package file
     */
    private static File nodeAppPackage() {
        String[] files = { "server.js", "package.json" };
        try {
            File zipFile = File.createTempFile("mi-blob-verify", ".zip");
            zipFile.deleteOnExit();
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                byte[] buffer = new byte[4096];
                for (String file : files) {
                    zos.putNextEntry(new ZipEntry(file));
                    try (InputStream is = ConnectWebAppToStorageAccount.class
                        .getResourceAsStream("/appservice/mi-blob-verify/" + file)) {
                        if (is == null) {
                            throw new FileNotFoundException(
                                "Sample resource not found on classpath: /appservice/mi-blob-verify/" + file);
                        }
                        int read;
                        while ((read = is.read(buffer)) > 0) {
                            zos.write(buffer, 0, read);
                        }
                    }
                    zos.closeEntry();
                }
            }
            return zipFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ConnectWebAppToStorageAccount() {
    }
}
