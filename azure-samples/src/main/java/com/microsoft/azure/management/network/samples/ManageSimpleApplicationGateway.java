

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.Date;

/**
 * Azure network sample for managing application gateways.
 *
 *  - CREATE an application gateway for load balancing
 *    HTTP/HTTPS requests to backend server pools of virtual machines
 *
 *    This application gateway serves traffic for multiple
 *    domain names
 *
 *    Routing Rule 1
 *    Hostname 1 = None
 *    Backend server pool 1 = 4 virtual machines with IP addresses
 *    Backend server pool 1 settings = HTTP:8080
 *    Front end port 1 = HTTP:80
 *    Listener 1 = HTTP
 *    Routing rule 1 = HTTP listener 1 => backend server pool 1
 *    (round-robin load distribution)
 *
 *  - MODIFY the application gateway - re-configure the Routing Rule 1 for SSL offload &
 *    add a host name, www.contoso.com
 *
 *    Change listener 1 from HTTP to HTTPS
 *    Add SSL certificate to the listener
 *    Update front end port 1 to HTTPS:1443
 *    Add a host name, www.contoso.com
 *    Enable cookie-based affinity
 *
 *    Modified Routing Rule 1
 *    Hostname 1 = www.contoso.com
 *    Backend server pool 1 = 4 virtual machines with IP addresses
 *    Backend server pool 1 settings = HTTP:8080
 *    Front end port 1 = HTTPS:1443
 *    Listener 1 = HTTPS
 *    Routing rule 1 = HTTPS listener 1 => backend server pool 1
 *    (round-robin load distribution)
 *
 */
public final class ManageSimpleApplicationGateway {

    /**
     * Main entry point.
     * @param args parameters
     */

    public static void main(String[] args) {

        final String rgName = ResourceNamer.randomResourceName("rgNEAGS", 15);
        final String pipName = ResourceNamer.randomResourceName("pip" + "-", 18);

         try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            try {

                //=============================================================
                // Create a resource group (Where all resources gets created)
                //
                ResourceGroup resourceGroup = azure.resourceGroups()
                        .define(rgName)
                        .withRegion(Region.US_EAST)
                        .create();

                System.out.println("Created a new resource group - " + resourceGroup.id());


                //=============================================================
                // Create a public IP address for the Application Gateway
                System.out.println("Creating a public IP address for the application gateway ...");

                PublicIpAddress publicIpAddress = azure.publicIpAddresses().define(pipName)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
                        .create();

                System.out.println("Created a public IP address");
                // Print the virtual network details
                Utils.print(publicIpAddress);


                //=======================================================================
                // Create an application gateway

                Date t3 = new Date();
                System.out.println("================= CREATE ======================");
                System.out.println("Creating an application gateway");

                ApplicationGateway applicationGateway = azure.applicationGateways().define("myFirstAppGateway")
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup)
                        // Request routing rule for HTTP from public 80 to public 8080
                        .defineRequestRoutingRule("HTTP-80-to-8080")
                            .fromPublicFrontend()
                            .fromFrontendHttpPort(80)
                            .toBackendHttpPort(8080)
                            .toBackendIpAddress("11.1.1.1")
                            .toBackendIpAddress("11.1.1.2")
                            .toBackendIpAddress("11.1.1.3")
                            .toBackendIpAddress("11.1.1.4")
                            .attach()
                        .withExistingPublicIpAddress(publicIpAddress)
                        .create();

                Date t4 = new Date();

                System.out.println("Application gateway created: (took " + ((t4.getTime() - t3.getTime()) / 1000) + " seconds)");
                Utils.print(applicationGateway);


                //=======================================================================
                // Update an application gateway
                // configure the first routing rule for SSL offload

                System.out.println("================= UPDATE ======================");
                System.out.println("Updating the application gateway");

                Date t5 = new Date();

                applicationGateway.update()
                        .withoutRequestRoutingRule("HTTP-80-to-8080")
                        .defineRequestRoutingRule("HTTPs-1443-to-8080")
                            .fromPublicFrontend()
                            .fromFrontendHttpsPort(1443)
                            .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                            .withSslCertificatePassword("Abc123")
                            .toBackendHttpPort(8080)
                            .toBackendIpAddress("11.1.1.1")
                            .toBackendIpAddress("11.1.1.2")
                            .toBackendIpAddress("11.1.1.3")
                            .toBackendIpAddress("11.1.1.4")
                            .withHostName("www.contoso.com")
                            .withCookieBasedAffinity()
                            .attach()
                        .apply();

                Date t6 = new Date();

                System.out.println("Application gateway updated: (took " + ((t6.getTime() - t5.getTime()) / 1000) + " seconds)");
                Utils.print(applicationGateway);

            } catch (Exception f) {

                System.out.println(f.getMessage());
                f.printStackTrace();

            } finally {
                try {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().deleteByName(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } catch (NullPointerException npe) {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                } catch (Exception g) {
                    g.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageSimpleApplicationGateway() {

    }
}

