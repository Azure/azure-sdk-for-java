package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.List;

/**
 * List all virtual machine extension image publishers and
 * list all virtual machine extension images published by Microsoft.OSTCExtensions, Microsoft.Azure.Extensions
 * by browsing through extension image publishers, types, and versions.
 */
public final class ListVirtualMachineExtensionImages {
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
            // List all virtual machine extension image publishers and
            // list all virtual machine extension images
            // published by Microsoft.OSTCExtensions and Microsoft.Azure.Extensions
            // by browsing through extension image publishers, types, and versions

            List<VirtualMachinePublisher> publishers = azure
                    .virtualMachineImages()
                    .publishers()
                    .listByRegion(Region.US_EAST);

            VirtualMachinePublisher chosenPublisher;

            System.out.println("US East data center: printing list of \n"
                    + "a) Publishers and\n"
                    + "b) virtual machine images published by Microsoft.OSTCExtensions and Microsoft.Azure.Extensions");
            System.out.println("=======================================================");
            System.out.println("\n");

            for (VirtualMachinePublisher publisher : publishers) {

                System.out.println("Publisher - " + publisher.name());

                if (publisher.name().equalsIgnoreCase("Microsoft.OSTCExtensions")
                        | publisher.name().equalsIgnoreCase("Microsoft.Azure.Extensions")) {

                    chosenPublisher = publisher;
                    System.out.print("\n\n");
                    System.out.println("=======================================================");
                    System.out.println("Located " + chosenPublisher.name());
                    System.out.println("=======================================================");
                    System.out.println("Printing entries as publisher/type/version");

                    for (VirtualMachineExtensionImageType imageType : chosenPublisher.extensionTypes().list()) {
                        for (VirtualMachineExtensionImageVersion version: imageType.versions().list()) {
                            VirtualMachineExtensionImage image = version.image();
                                System.out.println("Image - " + chosenPublisher.name() + "/"
                                        + image.typeName() + "/"
                                        + image.versionName());
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

    private ListVirtualMachineExtensionImages() {
    }
}
