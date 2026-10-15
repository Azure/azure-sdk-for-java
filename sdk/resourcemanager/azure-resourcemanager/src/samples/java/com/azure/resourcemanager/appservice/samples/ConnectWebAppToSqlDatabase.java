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
import com.azure.resourcemanager.sql.models.PrincipalType;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;

/**
 * Azure App Service sample for connecting a web app to a SQL database using passwordless authentication.
 *  - Create a web app with a system-assigned managed identity
 *  - Create a Microsoft Entra-only SQL Server whose administrator is that managed identity, plus a database
 *  - Store only the (secret-free) connection information in the web app settings
 *  - Add firewall rules so the web app can reach the SQL Server
 * <p>
 * The web app authenticates to SQL with its managed identity (no SQL login or password), for example with a JDBC
 * connection string that uses {@code authentication=ActiveDirectoryMSI}.
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

        try {
            // Create a web app with a system-assigned managed identity; that identity is used to reach SQL.
            // HTTPS-only is enforced; minimum TLS 1.2 and FTPS-only are already the App Service defaults.
            WebApp app = azureResourceManager.webApps()
                .define(appName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.STANDARD_S1)
                .withJavaVersion(JavaVersion.JAVA_11)
                .withWebContainer(WebContainer.TOMCAT_9_0_NEWEST)
                .withHttpsOnly(true)
                .withSystemAssignedManagedServiceIdentity()
                .create();

            // Create a Microsoft Entra-only SQL Server whose administrator is the web app's managed identity.
            // No SQL login/password is created, so there is no secret to store or leak.
            SqlServer server = azureResourceManager.sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_WEST3)
                .withExistingResourceGroup(rgName)
                .withAzureActiveDirectoryOnlyAuthentication()
                .withExternalActiveDirectoryAdministrator(appName,
                    app.systemAssignedManagedServiceIdentityPrincipalId(), PrincipalType.APPLICATION)
                .create();

            SqlDatabase db = server.databases().define(sqlDbName).create();

            // Allow the web app's outbound IP addresses to reach the SQL Server.
            SqlServer.Update firewall = server.update();
            int i = 0;
            for (String ip : app.outboundIPAddresses()) {
                firewall = firewall.defineFirewallRule("webappRule" + i++).withIpAddress(ip).attach();
            }
            firewall.apply();

            // Store only secret-free connection information. The web app authenticates with its managed identity.
            String connectionString = "jdbc:sqlserver://" + server.fullyQualifiedDomainName()
                + ":1433;database=" + db.name() + ";authentication=ActiveDirectoryMSI;encrypt=true";
            app.update()
                .withAppSetting("SQL_CONNECTION_STRING", connectionString)
                .apply();

            System.out.println("Connected web app " + app.name() + " to SQL database " + db.name()
                + " using managed identity");
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
