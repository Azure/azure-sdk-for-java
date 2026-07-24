// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.samples.SampleUtils;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;

/**
 * Azure App Service sample for connecting a web app to a SQL database.
 *  - Create a SQL Server and database
 *  - Create a web app whose settings hold the SQL connection information
 *  - Add firewall rules so the web app can reach the SQL Server
 */
public final class ConnectWebAppToSqlDatabase {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);
        final String appName = SampleUtils.randomResourceName(azureResourceManager, "webapp-", 20);
        final String sqlServerName = SampleUtils.randomResourceName(azureResourceManager, "jsdkserver", 20);
        final String sqlDbName = SampleUtils.randomResourceName(azureResourceManager, "jsdkdb", 20);
        final String admin = "jsdkadmin";
        final String password = SampleUtils.password();

        try {
            // Create a SQL Server and a database for the web app to use.
            SqlServer server = azureResourceManager.sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(admin)
                .withAdministratorPassword(password)
                .create();

            SqlDatabase db = server.databases().define(sqlDbName).create();

            // Create a web app that stores the SQL connection information in its app settings.
            WebApp app = azureResourceManager.webApps()
                .define(appName)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.STANDARD_S1)
                .withJavaVersion(JavaVersion.JAVA_11)
                .withWebContainer(WebContainer.TOMCAT_9_0_NEWEST)
                .withAppSetting("DBHost", server.fullyQualifiedDomainName())
                .withAppSetting("DBName", db.name())
                .withAppSetting("DBUser", admin)
                .withAppSetting("DBPass", password)
                .create();

            // Allow the web app's outbound IP addresses to reach the SQL Server.
            SqlServer.Update update = server.update();
            int i = 0;
            for (String ip : app.outboundIPAddresses()) {
                update = update.defineFirewallRule("webappRule" + i++).withIpAddress(ip).attach();
            }
            update.apply();

            System.out.println("Connected web app " + app.name() + " to SQL database " + db.name());
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ConnectWebAppToSqlDatabase() {
    }
}
