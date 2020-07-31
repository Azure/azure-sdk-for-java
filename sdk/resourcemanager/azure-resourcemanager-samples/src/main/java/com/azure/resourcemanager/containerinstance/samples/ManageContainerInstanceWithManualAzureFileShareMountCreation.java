// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareClientBuilder;
import com.azure.storage.file.share.models.ShareFileItem;

/**
 * Azure Container Instance sample for managing container instances with Azure File Share mount.
 *    - Create a storage account and an Azure file share resource
 *    - Create an Azure container instance using Docker image "seanmckenna/aci-hellofiles" with a mount to the file share from above
 *    - Test that the container app can be reached via "curl" like HTTP GET calls
 *    - Retrieve container log content
 */
public class ManageContainerInstanceWithManualAzureFileShareMountCreation {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = azure.sdkContext().randomResourceName("rgACI", 15);
        final String aciName = azure.sdkContext().randomResourceName("acisample", 20);
        final String saName = azure.sdkContext().randomResourceName("sa", 20);
        final String shareName = azure.sdkContext().randomResourceName("fileshare", 20);
        final String containerImageName = "seanmckenna/aci-hellofiles";
        final String volumeMountName = "aci-helloshare";

        try {

            //=============================================================
            // Create a new storage account and an Azure file share resource

            StorageAccount storageAccount = azure.storageAccounts().define(saName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .create();

            StorageAccountKey storageAccountKey = storageAccount.getKeys().get(0);

            ShareClient shareClient = new ShareClientBuilder()
                .connectionString(com.azure.resourcemanager.resources.fluentcore.utils.Utils.getStorageConnectionString(
                    saName,
                    storageAccountKey.value(),
                    azure.containerGroups().manager().environment()
                ))
                .shareName(shareName)
                .buildClient();
            shareClient.create();

            //=============================================================
            // Create a container group with one container instance of default CPU core count and memory size
            //   using public Docker image "seanmckenna/aci-hellofiles" which mounts the file share created previously
            //   as read/write shared container volume.

            ContainerGroup containerGroup = azure.containerGroups().define(aciName)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .defineVolume(volumeMountName)
                    .withExistingReadWriteAzureFileShare(shareName)
                    .withStorageAccountName(saName)
                    .withStorageAccountKey(storageAccountKey.value())
                    .attach()
                .defineContainerInstance(aciName)
                    .withImage(containerImageName)
                    .withExternalTcpPort(80)
                    .withVolumeMountSetting(volumeMountName, "/aci/logs/")
                    .attach()
                .withDnsPrefix(aciName)
                .create();

            Utils.print(containerGroup);

            //=============================================================
            // Check that the container instance is up and running

            // warm up
            System.out.println("Warming up " + containerGroup.ipAddress());
            Utils.curl("http://" + containerGroup.ipAddress());
            SdkContext.sleep(15000);
            System.out.println("CURLing " + containerGroup.ipAddress());
            System.out.println(Utils.curl("http://" + containerGroup.ipAddress()));

            //=============================================================
            // Check the container instance logs

            String logContent = containerGroup.getLogContent(aciName);
            System.out.format("Logs for container instance: %s%n%s", aciName, logContent);

            //=============================================================
            // Remove the container group

            azure.containerGroups().deleteById(containerGroup.id());

            //=============================================================
            // List the file share content

            Iterable<ShareFileItem> shareContent = shareClient.getRootDirectoryClient().listFilesAndDirectories();

            for (ShareFileItem item : shareContent) {
                System.out.format("Found shared file %s:%n", item.getName());
            }

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
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
     *
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
