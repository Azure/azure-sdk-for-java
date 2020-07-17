/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.ListFileItem;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure Container Instance sample for managing container instances with Azure File Share mount.
 *    - Create an Azure container instance using Docker image "seanmckenna/aci-hellofiles" with a mount to a new file share
 *    - Test that the container app can be reached via "curl" like HTTP GET calls
 *    - Retrieve container log content
 */
public class ManageContainerInstanceWithAzureFileShareMount {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgACI", 15);
        final String aciName = SdkContext.randomResourceName("acisample", 20);
        final String shareName = SdkContext.randomResourceName("fileshare", 20);
        final String containerImageName = "seanmckenna/aci-hellofiles";
        final String volumeMountName = "aci-helloshare";

        try {

            //=============================================================
            // Create a container group with one container instance of default CPU core count and memory size
            //   using public Docker image "seanmckenna/aci-hellofiles" which mounts the file share created previously
            //   as read/write shared container volume.

            ContainerGroup containerGroup = azure.containerGroups().define(aciName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withNewAzureFileShareVolume(volumeMountName, shareName)
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
            SdkContext.sleep(30000);
            System.out.println("CURLing " + containerGroup.ipAddress());
            System.out.println(Utils.curl("http://" + containerGroup.ipAddress()));

            //=============================================================
            // Check the container instance logs

            String logContent = containerGroup.getLogContent(aciName);
            System.out.format("Logs for container instance: %s\n%s", aciName, logContent);

            //=============================================================
            // List the file share content

            String storageAccountName = containerGroup.volumes().get(volumeMountName).azureFile().storageAccountName();
            StorageAccount storageAccount = azure.storageAccounts().getByResourceGroup(rgName, storageAccountName);
            CloudFileShare cloudFileShare = CloudStorageAccount.parse(String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                storageAccountName,
                storageAccount.getKeys().get(0).value()))
                .createCloudFileClient()
                .getShareReference(shareName);
            Iterable<ListFileItem> shareContent = cloudFileShare.getRootDirectoryReference().listFilesAndDirectories();

            for (ListFileItem item : shareContent) {
                System.out.format("Found shared file %s:\n", item.getUri().toString());
            }

            //=============================================================
            // Remove the container group

            azure.containerGroups().deleteById(containerGroup.id());

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

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                .withLogLevel(LogLevel.BODY)
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
