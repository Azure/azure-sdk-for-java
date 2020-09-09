// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//
//package com.microsoft.azure.management.compute.samples;
//
//import com.microsoft.azure.AzureEnvironment;
//import com.microsoft.azure.credentials.MSICredentials;
//import com.microsoft.azure.management.Azure;
//import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
//import com.microsoft.azure.management.compute.VirtualMachine;
//import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
//import com.microsoft.azure.management.resources.fluentcore.arm.Region;
//import com.microsoft.azure.management.samples.Utils;
//import com.microsoft.rest.LogLevel;
//
///**
// * Azure Compute sample for managing virtual machine from Managed Service Identity (MSI) enabled virtual machine -
// *   - Create a virtual machine using MSI credentials from System assigned or User Assigned MSI enabled VM.
// */
//public final class ManageVirtualMachineFromMSIEnabledVirtualMachine {
//    /**
//     * Main entry point.
//     *
//     * @param args the parameters
//     */
//    public static void main(String[] args) {
//        try {
//            final Region region = Region.US_WEST_CENTRAL;
//
//            // This sample required to be run from a MSI (User Assigned or System Assigned) enabled virtual machine with role
//            // based contributor access to the resource group specified as the second command line argument.
//            //
//            // see https://github.com/Azure-Samples/compute-java-manage-user-assigned-msi-enabled-virtual-machine.git
//            //
//
//            final String usage = "Usage: mvn clean compile exec:java -Dexec.args=\"<subscription-id> <rg-name> [<client-id>]\"";
//            if (args.length < 2) {
//                throw new IllegalArgumentException(usage);
//            }
//
//            final String subscriptionId = args[0];
//            final String resourceGroupName = args[1];
//            final String clientId = args.length > 2 ? args[2] : null;
//            final String linuxVMName = Utils.createRandomName("VM1");
//            final String userName = "tirekicker";
//            final String password = Utils.password();
//
//            //=============================================================
//            // MSI Authenticate
//
//            MSICredentials credentials = new MSICredentials(AzureEnvironment.AZURE);
//            if (clientId != null) {
//                // If User Assigned MSI client id is specified then switch to "User Assigned MSI" auth mode
//                //
//                credentials = credentials.withClientId(clientId);
//            }
//
//            Azure azure = Azure.configure()
//                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
//                    .authenticate(credentials)
//                    .withSubscription(subscriptionId);
//
//            // Print selected subscription
//            System.out.println("Selected subscription: " + azure.subscriptionId());
//
//            //=============================================================
//            // Create a Linux VM using MSI credentials
//
//            System.out.println("Creating a Linux VM using MSI credentials");
//
//            VirtualMachine virtualMachine = azure.virtualMachines()
//                    .define(linuxVMName)
//                    .withRegion(region)
//                    .withExistingResourceGroup(resourceGroupName)
//                    .withNewPrimaryNetwork("10.0.0.0/28")
//                    .withPrimaryPrivateIPAddressDynamic()
//                    .withoutPrimaryPublicIPAddress()
//                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
//                    .withRootUsername(userName)
//                    .withRootPassword(password)
//                    .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
//                    .create();
//
//            System.out.println("Created virtual machine using MSI credentials");
//            Utils.print(virtualMachine);
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private ManageVirtualMachineFromMSIEnabledVirtualMachine() {
//    }
//}
