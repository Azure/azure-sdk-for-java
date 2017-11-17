/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

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
 *    Routing rule 1 = HTTP listener 1 =&gt; backend server pool 1
 *    (round-robin load distribution)
 *
 *  - MODIFY the application gateway - re-configure the Routing Rule 1 for SSL offload and
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
 *    Routing rule 1 = HTTPS listener 1 =&gt; backend server pool 1
 *    (round-robin load distribution)
 *
 */
public final class ManageSimpleApplicationGateway {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgNEAGS", 15);
        try {
            //=======================================================================
            // Create an application gateway

            System.out.println("================= CREATE ======================");
            System.out.println("Creating an application gateway... (this can take about 20 min)");
            long t1 = System.currentTimeMillis();

            ApplicationGateway applicationGateway = azure.applicationGateways().define("myFirstAppGateway")
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)

                    // Request routing rule for HTTP from public 80 to public 8080
                    .defineRequestRoutingRule("HTTP-80-to-8080")
                        .fromPublicFrontend()
                        .fromFrontendHttpPort(80)
                        .toBackendHttpPort(8080)
                        .toBackendIPAddress("11.1.1.1")
                        .toBackendIPAddress("11.1.1.2")
                        .toBackendIPAddress("11.1.1.3")
                        .toBackendIPAddress("11.1.1.4")
                        .attach()
                    .withNewPublicIPAddress()
                    .create();

            long t2 = System.currentTimeMillis();

            System.out.println("Application gateway created: (took " + (t2 - t1) / 1000 + " seconds)");
            Utils.print(applicationGateway);


            //=======================================================================
            // Update an application gateway
            // configure the first routing rule for SSL offload

            System.out.println("================= UPDATE ======================");
            System.out.println("Updating the application gateway");

            t1 = System.currentTimeMillis();

            applicationGateway.update()
                    .withoutRequestRoutingRule("HTTP-80-to-8080")
                    .defineRequestRoutingRule("HTTPs-1443-to-8080")
                        .fromPublicFrontend()
                        .fromFrontendHttpsPort(1443)
                        .withSslCertificateFromPfxFile(new File(ManageSimpleApplicationGateway.class.getClassLoader().getResource("myTest.pfx").getPath()))
                        .withSslCertificatePassword("Abc123")
                        .toBackendHttpPort(8080)
                        .toBackendIPAddress("11.1.1.1")
                        .toBackendIPAddress("11.1.1.2")
                        .toBackendIPAddress("11.1.1.3")
                        .toBackendIPAddress("11.1.1.4")
                        .withHostName("www.contoso.com")
                        .withCookieBasedAffinity()
                        .attach()
                    .apply();

            t2 = System.currentTimeMillis();

            System.out.println("Application gateway updated: (took " + (t2 - t1) / 1000 + " seconds)");
            Utils.print(applicationGateway);
            return true;
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
        return false;
    }

    /**
     * Main entry point.
     * @param args parameters
     */

    public static void main(String[] args) {
         try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageSimpleApplicationGateway() {

    }
}

