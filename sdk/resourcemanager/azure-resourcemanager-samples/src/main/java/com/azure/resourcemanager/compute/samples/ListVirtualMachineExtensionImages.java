// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImage;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageType;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageVersion;
import com.azure.resourcemanager.compute.models.VirtualMachinePublisher;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;

/**
 * List all virtual machine extension image publishers and
 * list all virtual machine extension images published by Microsoft.OSTCExtensions, Microsoft.Azure.Extensions
 * by browsing through extension image publishers, types, and versions.
 */
public final class ListVirtualMachineExtensionImages {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST_CENTRAL;

        //=================================================================
        // List all virtual machine extension image publishers and
        // list all virtual machine extension images
        // published by Microsoft.OSTCExtensions and Microsoft.Azure.Extensions
        // by browsing through extension image publishers, types, and versions

        PagedIterable<VirtualMachinePublisher> publishers = azureResourceManager
                .virtualMachineImages()
                .publishers()
                .listByRegion(region);

        VirtualMachinePublisher chosenPublisher;

        System.out.println("US East data center: printing list of \n"
                + "a) Publishers and\n"
                + "b) virtual machine extension images published by Microsoft.OSTCExtensions and Microsoft.Azure.Extensions");
        System.out.println("=======================================================");
        System.out.println("\n");

        for (VirtualMachinePublisher publisher : publishers) {

            System.out.println("Publisher - " + publisher.name());

            if (publisher.name().equalsIgnoreCase("Microsoft.OSTCExtensions")
                    || publisher.name().equalsIgnoreCase("Microsoft.Azure.Extensions")) {

                chosenPublisher = publisher;
                System.out.print("\n\n");
                System.out.println("=======================================================");
                System.out.println("Located " + chosenPublisher.name());
                System.out.println("=======================================================");
                System.out.println("Printing entries as publisher/type/version");

                for (VirtualMachineExtensionImageType imageType : chosenPublisher.extensionTypes().list()) {
                    for (VirtualMachineExtensionImageVersion version: imageType.versions().list()) {
                        VirtualMachineExtensionImage image = version.getImage();
                        System.out.println("Image - " + chosenPublisher.name() + "/"
                                + image.typeName() + "/"
                                + image.versionName());
                    }
                }

                System.out.print("\n\n");

            }
        }
        return true;
    }

    /**
     * The main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=================================================================
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

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ListVirtualMachineExtensionImages() {
    }
}
