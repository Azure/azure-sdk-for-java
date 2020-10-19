// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.PhpVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.core.http.policy.HttpLogDetailLevel;

import java.io.IOException;

/**
 * Azure App Service basic sample for managing web apps.
 *  - Create a SQL database in a new SQL server
 *  - Create a web app deployed with Project Nami (WordPress's SQL Server variant)
 *      that contains the app settings to connect to the SQL database
 *  - Update the SQL server's firewall rules to allow the web app to access
 *  - Clean up
 */
public final class ManageWebAppSqlConnection {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws IOException {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String appName        = Utils.randomResourceName(azureResourceManager, "webapp1-", 20);
        final String appUrl         = appName + suffix;
        final String sqlServerName  = Utils.randomResourceName(azureResourceManager, "jsdkserver", 20);
        final String sqlDbName      = Utils.randomResourceName(azureResourceManager, "jsdkdb", 20);
        final String admin          = "jsdkadmin";
        final String password       = Utils.password();
        final String rgName         = Utils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);

        try {


            //============================================================
            // Create a sql server

            System.out.println("Creating SQL server " + sqlServerName + "...");

            SqlServer server = azureResourceManager.sqlServers().define(sqlServerName)
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

            WebApp app = azureResourceManager.webApps().define(appName)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rgName)
                    .withNewWindowsPlan(PricingTier.STANDARD_S1)
                    .withPhpVersion(PhpVersion.PHP5_6)
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
                update = update.defineFirewallRule("filewallRule1").withIpAddress(ip).attach();
            }
            server = update.apply();

            System.out.println("Firewall rules added for web app " + appName);
            Utils.print(server);

            System.out.println("Your WordPress app is ready.");
            System.out.println("Please navigate to http://" + appUrl + " to finish the GUI setup. Press enter to exit.");
            System.in.read();

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
     * @param args the parameters
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
}
