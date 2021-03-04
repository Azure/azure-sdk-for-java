// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.apache.commons.lang.time.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure network sample for managing application gateways.
 * <p>
 * - CREATE an application gateway for load balancing
 * HTTP/HTTPS requests to backend server pools of virtual machines
 * <p>
 * This application gateway serves traffic for multiple
 * domain names
 * <p>
 * Routing Rule 1
 * Hostname 1 = None
 * Backend server pool 1 = 4 virtual machines with IP addresses
 * Backend server pool 1 settings = HTTP:8080
 * Front end port 1 = HTTP:80
 * Listener 1 = HTTP
 * Routing rule 1 = HTTP listener 1 =&gt; backend server pool 1
 * (round-robin load distribution)
 * <p>
 * Routing Rule 2
 * Hostname 2 = None
 * Backend server pool 2 = 4 virtual machines with IP addresses
 * Backend server pool 2 settings = HTTP:8080
 * Front end port 2 = HTTPS:443
 * Listener 2 = HTTPS
 * Routing rule 2 = HTTPS listener 2 =&gt; backend server pool 2
 * (round-robin load distribution)
 * <p>
 * - MODIFY the application gateway - re-configure the Routing Rule 1 for SSL offload and
 * add a host name, www.contoso.com
 * <p>
 * Change listener 1 from HTTP to HTTPS
 * Add SSL certificate to the listener
 * Update front end port 1 to HTTPS:1443
 * Add a host name, www.contoso.com
 * Enable cookie-based affinity
 * <p>
 * Modified Routing Rule 1
 * Hostname 1 = www.contoso.com
 * Backend server pool 1 = 4 virtual machines with IP addresses
 * Backend server pool 1 settings = HTTP:8080
 * Front end port 1 = HTTPS:1443
 * Listener 1 = HTTPS
 * Routing rule 1 = HTTPS listener 1 =&gt; backend server pool 1
 * (round-robin load distribution)
 */
public final class ManageApplicationGateway {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws IOException {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEAG", 15);
        final String pipName = Utils.randomResourceName(azureResourceManager, "pip" + "-", 18);

        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";

        int backendPools = 2;
        int vmCountInAPool = 4;

        Region[] regions = {Region.US_EAST, Region.US_WEST};
        String[] addressSpaces = {"172.16.0.0/16", "172.17.0.0/16"};
        String[][] publicIpCreatableKeys = new String[backendPools][vmCountInAPool];
        String[][] ipAddresses = new String[backendPools][vmCountInAPool];

        try {

            //=============================================================
            // Create a resource group (Where all resources gets created)
            //
            ResourceGroup resourceGroup = azureResourceManager.resourceGroups().define(rgName)
                    .withRegion(Region.US_EAST)
                    .create();

            System.out.println("Created a new resource group - " + resourceGroup.id());


            //=============================================================
            // Create a public IP address for the Application Gateway
            System.out.println("Creating a public IP address for the application gateway ...");

            PublicIpAddress publicIPAddress = azureResourceManager.publicIpAddresses().define(pipName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .create();

            System.out.println("Created a public IP address");
            // Print the virtual network details
            Utils.print(publicIPAddress);

            //=============================================================
            // Create backend pools

            // Prepare a batch of Creatable definitions
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();

            for (int i = 0; i < backendPools; i++) {


                //=============================================================
                // Create 1 network creatable per region
                // Prepare Creatable Network definition (Where all the virtual machines get added to)
                String networkName = Utils.randomResourceName(azureResourceManager, "vnetNEAG-", 20);

                Creatable<Network> networkCreatable = azureResourceManager.networks().define(networkName)
                        .withRegion(regions[i])
                        .withExistingResourceGroup(resourceGroup)
                        .withAddressSpace(addressSpaces[i]);


                //=============================================================
                // Create 1 storage creatable per region (For storing VMs disk)
                String storageAccountName = Utils.randomResourceName(azureResourceManager, "stgneag", 20);
                Creatable<StorageAccount> storageAccountCreatable = azureResourceManager.storageAccounts().define(storageAccountName)
                        .withRegion(regions[i])
                        .withExistingResourceGroup(resourceGroup);

                String linuxVMNamePrefix = Utils.randomResourceName(azureResourceManager, "vm-", 15);

                for (int j = 0; j < vmCountInAPool; j++) {


                    //=============================================================
                    // Create 1 public IP address creatable
                    Creatable<PublicIpAddress> publicIPAddressCreatable = azureResourceManager.publicIpAddresses()
                            .define(String.format("%s-%d", linuxVMNamePrefix, j))
                            .withRegion(regions[i])
                            .withExistingResourceGroup(resourceGroup)
                            .withLeafDomainLabel(String.format("%s-%d", linuxVMNamePrefix, j));

                    publicIpCreatableKeys[i][j] = publicIPAddressCreatable.key();


                    //=============================================================
                    // Create 1 virtual machine creatable
                    Creatable<VirtualMachine> virtualMachineCreatable = azureResourceManager.virtualMachines()
                            .define(String.format("%s-%d", linuxVMNamePrefix, j))
                            .withRegion(regions[i])
                            .withExistingResourceGroup(resourceGroup)
                            .withNewPrimaryNetwork(networkCreatable)
                            .withPrimaryPrivateIPAddressDynamic()
                            .withNewPrimaryPublicIPAddress(publicIPAddressCreatable)
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUsername(userName)
                            .withSsh(sshKey)
                            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                            .withNewStorageAccount(storageAccountCreatable);
                    creatableVirtualMachines.add(virtualMachineCreatable);
                }
            }


            //=============================================================
            // Create two backend pools of virtual machines

            StopWatch stopwatch = new StopWatch();
            System.out.println("Creating virtual machines (two backend pools)");

            stopwatch.start();
            CreatedResources<VirtualMachine> virtualMachines = azureResourceManager.virtualMachines().create(creatableVirtualMachines);
            stopwatch.stop();

            System.out.println("Created virtual machines (two backend pools)");

            for (VirtualMachine virtualMachine : virtualMachines.values()) {
                System.out.println(virtualMachine.id());
            }

            System.out.println("Virtual machines created: (took " + (stopwatch.getTime() / 1000) + " seconds) to create == " + virtualMachines.size()
                    + " == virtual machines (4 virtual machines per backend pool)");


            //=======================================================================
            // Get IP addresses from created resources

            System.out.println("IP Addresses in the backend pools are - ");
            for (int i = 0; i < backendPools; i++) {
                for (int j = 0; j < vmCountInAPool; j++) {
                    PublicIpAddress pip = (PublicIpAddress) virtualMachines
                            .createdRelatedResource(publicIpCreatableKeys[i][j]);
                    pip.refresh();
                    ipAddresses[i][j] = pip.ipAddress();
                    System.out.println(String.format("[backend pool = %d][vm = %d] = %s", i, j, ipAddresses[i][j]));
                }

                System.out.println("======");
            }


            //=======================================================================
            // Create an application gateway

            System.out.println("================= CREATE ======================");
            System.out.println("Creating an application gateway");
            stopwatch.reset();
            stopwatch.start();

            ApplicationGateway applicationGateway = azureResourceManager.applicationGateways().define("myFirstAppGateway")
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(resourceGroup)

                    // Request routing rule for HTTP from public 80 to public 8080
                    .defineRequestRoutingRule("HTTP-80-to-8080")
                    .fromPublicFrontend()
                    .fromFrontendHttpPort(80)
                    .toBackendHttpPort(8080)
                    .toBackendIPAddress(ipAddresses[0][0])
                    .toBackendIPAddress(ipAddresses[0][1])
                    .toBackendIPAddress(ipAddresses[0][2])
                    .toBackendIPAddress(ipAddresses[0][3])
                    .attach()

                    .withExistingPublicIpAddress(publicIPAddress)
                    .create();

            stopwatch.stop();
            System.out.println("Application gateway created: (took " + (stopwatch.getTime() / 1000) + " seconds)");
            Utils.print(applicationGateway);


            //=======================================================================
            // Update an application gateway
            // configure the first routing rule for SSL offload

            System.out.println("================= UPDATE ======================");
            System.out.println("Updating the application gateway");
            stopwatch.reset();
            stopwatch.start();

            applicationGateway.update()
                    .withoutRequestRoutingRule("HTTP-80-to-8080")
                    .defineRequestRoutingRule("HTTP-81-to-8080")
                    .fromPublicFrontend()
                    .fromFrontendHttpPort(81)
                    .toBackendHttpPort(8080)
                    .toBackendIPAddress(ipAddresses[0][0])
                    .toBackendIPAddress(ipAddresses[0][1])
                    .toBackendIPAddress(ipAddresses[0][2])
                    .toBackendIPAddress(ipAddresses[0][3])
                    .withHostname("www.contoso.com")
                    .withCookieBasedAffinity()
                    .attach()
                    .apply();

            stopwatch.stop();
            System.out.println("Application gateway updated: (took " + (stopwatch.getTime() / 1000) + " seconds)");
            Utils.print(applicationGateway);
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     *
     * @param args parameters
     */

    public static void main(String[] args) {
        try {

            //=============================================================
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

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageApplicationGateway() {

    }
}
