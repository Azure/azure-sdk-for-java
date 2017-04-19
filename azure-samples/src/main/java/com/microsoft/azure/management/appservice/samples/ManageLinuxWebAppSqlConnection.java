/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.RuntimeStack;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.rest.LogLevel;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Azure App Service basic sample for managing web apps.
 *  - Create a SQL database in a new SQL server
 *  - Create a web app deployed with Project Nami (WordPress's SQL Server variant)
 *      that contains the app settings to connect to the SQL database
 *  - Update the SQL server's firewall rules to allow the web app to access
 *  - Clean up
 */
public final class ManageLinuxWebAppSqlConnection {

    private static OkHttpClient httpClient;

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String appName        = SdkContext.randomResourceName("webapp1-", 20);
        final String appUrl         = appName + suffix;
        final String sqlServerName  = SdkContext.randomResourceName("jsdkserver", 20);
        final String sqlDbName      = SdkContext.randomResourceName("jsdkdb", 20);
        final String admin          = "jsdkadmin";
        final String password       = "StrongPass!123";
        final String rgName         = SdkContext.randomResourceName("rg1NEMV_", 24);

        try {


            //============================================================
            // Create a sql server

            System.out.println("Creating SQL server " + sqlServerName + "...");

            SqlServer server = azure.sqlServers().define(sqlServerName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withAdministratorLogin(admin)
                    .withAdministratorPassword(password)
                    .create();

            System.out.println("Created SQL server " + server.name());

            //============================================================
            // Create a sql database for the web app to use

            System.out.println("Creating SQL database " + sqlDbName + "...");

            SqlDatabase db = server.databases().define(sqlDbName).create();

            System.out.println("Created SQL database " + db.name());

            //============================================================
            // Create a web app with a new app service plan

            System.out.println("Creating web app " + appName + "...");

            WebApp app = azure.webApps().define(appName)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rgName)
                    .withNewLinuxPlan(PricingTier.STANDARD_S1)
                    .withBuiltInImage(RuntimeStack.PHP_5_6_23)
                    .defineSourceControl()
                        .withPublicGitRepository("https://github.com/ProjectNami/projectnami")
                        .withBranch("master")
                        .attach()
                    .withAppSetting("ProjectNami.DBHost", server.fullyQualifiedDomainName())
                    .withAppSetting("ProjectNami.DBName", db.name())
                    .withAppSetting("ProjectNami.DBUser", admin)
                    .withAppSetting("ProjectNami.DBPass", password)
                    .create();

            System.out.println("Created web app " + app.name());
            Utils.print(app);

            //============================================================
            // Allow web app to access the SQL server

            System.out.println("Allowing web app " + appName + " to access SQL server...");

            SqlServer.Update update = server.update();
            for (String ip : app.outboundIPAddresses()) {
                update = update.withNewFirewallRule(ip);
            }
            server = update.apply();

            System.out.println("Firewall rules added for web app " + appName);
            Utils.print(server);

            System.out.println("Your WordPress app is ready.");
            System.out.println("Please navigate to http://" + appUrl + " to finish the GUI setup. Press enter to exit.");
            System.in.read();

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
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

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BASIC)
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

    static {
        httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    }
}
