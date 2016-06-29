/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.List;

/**
 * List all virtual machine image publishers and
 * list all virtual machine images published by Canonical, Red Hat and
 * SUSE by browsing through locations, publishers, offers, SKUs and images.
 */
public final class ListVirtualMachineImages {
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

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();


            //=================================================================
            // List all virtual machine image publishers and
            // list all virtual machine images
            // published by Canonical, Red Hat and SUSE
            // by browsing through locations, publishers, offers, SKUs and images

            List<VirtualMachinePublisher> publishers = azure
                    .virtualMachineImages()
                    .publishers()
                    .listByRegion(Region.US_EAST);

            VirtualMachinePublisher chosenPublisher;

            System.out.println("US East data center: printing list of \n"
                    + "a) Publishers and\n"
                    + "b) Images published by Canonical, Red Hat and Suse");
            System.out.println("=======================================================");
            System.out.println("\n");

            for (VirtualMachinePublisher publisher : publishers) {

                System.out.println("Publisher - " + publisher.name());

                if (publisher.name().equalsIgnoreCase("Canonical")
                        | publisher.name().equalsIgnoreCase("Suse")
                        | publisher.name().equalsIgnoreCase("RedHat")) {

                    chosenPublisher = publisher;
                    System.out.print("\n\n");
                    System.out.println("=======================================================");
                    System.out.println("Located " + chosenPublisher.name());
                    System.out.println("=======================================================");
                    System.out.println("Printing entries as publisher/offer/sku/image.version()");

                    for (VirtualMachineOffer offer : chosenPublisher.offers().list()) {
                        for (VirtualMachineSku sku: offer.skus().list()) {
                            for (VirtualMachineImage image : sku.images().list()) {
                                System.out.println("Image - " + chosenPublisher.name() + "/"
                                        + offer.name() + "/"
                                        + sku.name() + "/" + image.version());
                            }
                        }

                    }

                    System.out.print("\n\n");

                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ListVirtualMachineImages() {
    }
}