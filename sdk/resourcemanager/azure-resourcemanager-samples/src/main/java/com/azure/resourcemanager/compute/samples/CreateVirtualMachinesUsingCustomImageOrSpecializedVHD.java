// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.SSHShell;
import com.azure.resourcemanager.samples.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a virtual machine
 *  - Deallocate the virtual machine
 *  - Generalize the virtual machine
 *  - Capture the virtual machine to create a generalized image
 *  - Create a second virtual machine using the generalized image
 *  - Delete the second virtual machine
 *  - Create a new virtual machine by attaching OS disk of deleted VM to it.
 */
public final class CreateVirtualMachinesUsingCustomImageOrSpecializedVHD {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String linuxVMName1 = azure.sdkContext().randomResourceName("VM1", 15);
        final String linuxVMName2 = azure.sdkContext().randomResourceName("VM2", 15);
        final String linuxVMName3 = azure.sdkContext().randomResourceName("VM3", 15);
        final String rgName = azure.sdkContext().randomResourceName("rgCOMV", 15);
        final String publicIPDnsLabel = azure.sdkContext().randomResourceName("pip", 15);
        final String userName = "tirekicker";
        final String password = Utils.password();

        final String apacheInstallScript = "https://raw.githubusercontent.com/Azure/azure-libraries-for-java/master/azure-samples/src/main/resources/install_apache.sh";
        final String apacheInstallCommand = "bash install_apache.sh";
        List<String> apacheInstallScriptUris = new ArrayList<>();
        apacheInstallScriptUris.add(apacheInstallScript);

        try {
            //=============================================================
            // Create a Linux VM using an image from PIR (Platform Image Repository)

            System.out.println("Creating a Linux VM");

            VirtualMachine linuxVM = azure.virtualMachines().define(linuxVMName1)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPDnsLabel)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withUnmanagedDisks()
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", apacheInstallScriptUris)
                        .withPublicSetting("commandToExecute", apacheInstallCommand)
                        .attach()
                    .create();

            System.out.println("Created a Linux VM: " + linuxVM.id());
            Utils.print(linuxVM);

            // De-provision the virtual machine
            deprovisionAgentInLinuxVM(linuxVM.getPrimaryPublicIPAddress().fqdn(), 22, userName, password);

            //=============================================================
            // Deallocate the virtual machine
            System.out.println("Deallocate VM: " + linuxVM.id());

            linuxVM.deallocate();

            System.out.println("Deallocated VM: " + linuxVM.id() + "; state = " + linuxVM.powerState());

            //=============================================================
            // Generalize the virtual machine
            System.out.println("Generalize VM: " + linuxVM.id());

            linuxVM.generalize();

            System.out.println("Generalized VM: " + linuxVM.id());

            //=============================================================
            // Capture the virtual machine to get a 'Generalized image' with Apache
            System.out.println("Capturing VM: " + linuxVM.id());

            String capturedResultJson = linuxVM.capture("capturedvhds", "img", true);

            System.out.println("Captured VM: " + linuxVM.id());

            //=============================================================
            // Create a Linux VM using captured image (Generalized image)
            String capturedImageUri = extractCapturedImageUri(capturedResultJson);

            System.out.println("Creating a Linux VM using captured image - " + capturedImageUri);

            VirtualMachine linuxVM2 = azure.virtualMachines().define(linuxVMName2)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withStoredLinuxImage(capturedImageUri) // Note: A Generalized Image can also be an uploaded VHD prepared from an on-premise generalized VM.
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            Utils.print(linuxVM2);

            String specializedVhd = linuxVM2.osUnmanagedDiskVhdUri();
            //=============================================================
            // Deleting the virtual machine
            System.out.println("Deleting VM: " + linuxVM2.id());

            azure.virtualMachines().deleteById(linuxVM2.id()); // VM required to be deleted to be able to attach it's
            // OS Disk VHD to another VM (Deallocate is not sufficient)

            System.out.println("Deleted VM");

            //=============================================================
            // Create a Linux VM using 'specialized VHD' of previous VM

            System.out.println("Creating a new Linux VM by attaching OS Disk vhd - "
                    + specializedVhd
                    + " of deleted VM");

            VirtualMachine linuxVM3 = azure.virtualMachines().define(linuxVMName3)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withSpecializedOSUnmanagedDisk(specializedVhd, OperatingSystemTypes.LINUX) // New user credentials cannot be specified
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)       // when attaching a specialized VHD
                    .create();

            Utils.print(linuxVM3);
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

    private CreateVirtualMachinesUsingCustomImageOrSpecializedVHD() {
    }

    private static String extractCapturedImageUri(String capturedResultJson) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(capturedResultJson);
        } catch (IOException exception) {
            throw new RuntimeException("Parsing JSON failed -" + capturedResultJson, exception);
        }

        JsonNode resourcesNode = rootNode.path("resources");
        if (resourcesNode instanceof MissingNode) {
            throw new IllegalArgumentException("Expected 'resources' node not found in the capture result -" + capturedResultJson);
        }

        String imageUri = null;
        for (JsonNode resourceNode : resourcesNode) {
            JsonNode propertiesNodes = resourceNode.path("properties");
            if (!(propertiesNodes instanceof MissingNode)) {
                JsonNode storageProfileNode = propertiesNodes.path("storageProfile");
                if (!(storageProfileNode instanceof MissingNode)) {
                    JsonNode osDiskNode = storageProfileNode.path("osDisk");
                    if (!(osDiskNode instanceof MissingNode)) {
                        JsonNode imageNode = osDiskNode.path("image");
                        if (!(imageNode instanceof MissingNode)) {
                            JsonNode uriNode = imageNode.path("uri");
                            if (!(uriNode instanceof MissingNode)) {
                                imageUri = uriNode.asText();
                            }
                        }
                    }
                }
            }
        }

        if (imageUri == null) {
            throw new IllegalArgumentException("Could not locate image uri under expected section in the capture result -" + capturedResultJson);
        }
        return imageUri;
    }

    /**
     * De-provision an Azure linux virtual machine.
     *
     * @param host the public host name
     * @param port the ssh port
     * @param userName the ssh user name
     * @param password the ssh user password
     */
    protected static void deprovisionAgentInLinuxVM(String host, int port, String userName, String password) {
        SSHShell shell = null;
        try {
            System.out.println("Trying to de-provision: " + host);
            shell = SSHShell.open(host, port, userName, password);
            List<String> deprovisionCommand = new ArrayList<>();
            deprovisionCommand.add("sudo waagent -deprovision+user --force");
            String output = shell.runCommands(deprovisionCommand);
            System.out.println(output);
        } catch (JSchException jSchException) {
            System.out.println(jSchException.getMessage());
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        } finally {
            if (shell != null) {
                shell.close();
            }
        }
    }
}
