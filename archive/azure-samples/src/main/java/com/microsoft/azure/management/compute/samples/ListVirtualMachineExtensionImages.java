/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.List;

/**
 * List all virtual machine extension image publishers and
 * list all virtual machine extension images published by Microsoft.OSTCExtensions, Microsoft.Azure.Extensions
 * by browsing through extension image publishers, types, and versions.
 */
public final class ListVirtualMachineExtensionImages {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_WEST_CENTRAL;

        //=================================================================
        // List all virtual machine extension image publishers and
        // list all virtual machine extension images
        // published by Microsoft.OSTCExtensions and Microsoft.Azure.Extensions
        // by browsing through extension image publishers, types, and versions

        List<VirtualMachinePublisher> publishers = azure
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

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ListVirtualMachineExtensionImages() {
    }
}
