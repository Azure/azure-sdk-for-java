/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.VirtualMachineImage;
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

            final File credFile = new File("my.azureauth");

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

            List<Publisher> publishers = azure
                    .virtualMachineImages()
                    .publishers()
                    .listByRegion(Region.US_EAST);

            Publisher chosenPublisher;

            System.out.println("Printing list of publishers and images published by \n"
                    + "Canonical, Red Hat and Suse for the US East data center");
            System.out.println("=======================================================");
            System.out.println("\n");

            for (Publisher publisher : publishers) {

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

                    for (Offer offer : chosenPublisher.offers().list()) {
                        for (Sku sku: offer.skus().list()) {
                            for (VirtualMachineImage image : sku.listImages()) {
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