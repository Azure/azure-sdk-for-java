// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.VirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineOffer;
import com.azure.resourcemanager.compute.models.VirtualMachinePublisher;
import com.azure.resourcemanager.compute.models.VirtualMachineSku;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;

/**
 * List all virtual machine image publishers and
 * list all virtual machine images published by Canonical, Red Hat and
 * SUSE by browsing through locations, publishers, offers, SKUs and images.
 */
public final class ListVirtualMachineImages {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_WEST_CENTRAL;

        //=================================================================
        // List all virtual machine image publishers and
        // list all virtual machine images
        // published by Canonical, Red Hat and SUSE
        // by browsing through locations, publishers, offers, SKUs and images

        PagedIterable<VirtualMachinePublisher> publishers = azure
                .virtualMachineImages()
                .publishers()
                .listByRegion(region);

        VirtualMachinePublisher chosenPublisher;

        System.out.println("US East data center: printing list of \n"
                + "a) Publishers and\n"
                + "b) Images published by Canonical, Red Hat and Suse");
        System.out.println("=======================================================");
        System.out.println("\n");

        for (VirtualMachinePublisher publisher : publishers) {

            System.out.println("Publisher - " + publisher.name());

            if (publisher.name().equalsIgnoreCase("Canonical")
                    || publisher.name().equalsIgnoreCase("Suse")
                    || publisher.name().equalsIgnoreCase("RedHat")) {

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
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ListVirtualMachineImages() {
    }
}
