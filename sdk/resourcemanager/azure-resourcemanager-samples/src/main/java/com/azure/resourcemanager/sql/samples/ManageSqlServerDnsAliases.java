// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerDnsAlias;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

/**
 * Azure SQL sample for managing SQL Server DNS Aliases.
 *  - Create two SQL Servers "test" and "production", each with an empty database.
 *  - Create a new table and insert some expected values into each database.
 *  - Create a SQL Server DNS Alias to the "test" SQL database.
 *  - Query the "test" SQL database via the DNS alias and print the result.
 *  - Use the SQL Server DNS alias to acquire the "production" SQL database.
 *  - Query the "production" SQL database via the DNS alias and print the result.
 *  - Delete the SQL Servers
 */

public class ManageSqlServerDnsAliases {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws ClassNotFoundException, SQLException {
        final String sqlServerForTestName = Utils.randomResourceName(azureResourceManager, "sqltest", 20);
        final String sqlServerForProdName = Utils.randomResourceName(azureResourceManager, "sqlprod", 20);
        final String sqlServerDnsAlias = Utils.randomResourceName(azureResourceManager, "sqlserver", 20);
        final String dbName = "dbSample";
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgRSSDFW", 20);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();
        try {
            // Check if the expected SQL driver is available
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // ============================================================
            // Create a "test" SQL Server.
            System.out.println("Creating a SQL server for test related activities");

            SqlServer sqlServerForTest = azureResourceManager.sqlServers().define(sqlServerForTestName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineFirewallRule("allowAll")
                    .withIpAddressRange("0.0.0.1", "255.255.255.255")
                    .attach()
                .defineDatabase(dbName)
                    .withBasicEdition()
                    .attach()
                .create();

            Utils.print(sqlServerForTest);

            // ============================================================
            // Create a connection to the "test" SQL Server.
            System.out.println("Creating a connection to the \"test\" SQL Server");
            String connectionToSqlTestUrl = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;",
                sqlServerForTest.fullyQualifiedDomainName(),
                dbName,
                administratorLogin,
                administratorPassword);

            // Establish the connection.
            try (Connection conTest = DriverManager.getConnection(connectionToSqlTestUrl);
                 Statement stmt = conTest.createStatement();) {


                // ============================================================
                // Create a new table into the "test" SQL Server database and insert one value.
                System.out.println("Creating a new table into the \"test\" SQL Server database and insert one value");

                String sqlCommand = "CREATE TABLE [Dns_Alias_Sample_Test] ([Name] [varchar](30) NOT NULL)";
                stmt.execute(sqlCommand);

                sqlCommand = "INSERT Dns_Alias_Sample_Test VALUES ('Test')";
                stmt.execute(sqlCommand);
            }


            // ============================================================
            // Create a "production" SQL Server.
            System.out.println("Creating a SQL server for production related activities");

            SqlServer sqlServerForProd = azureResourceManager.sqlServers().define(sqlServerForProdName)
                .withRegion(Region.US_EAST2)
                .withExistingResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineFirewallRule("allowAll")
                    .withIpAddressRange("0.0.0.1", "255.255.255.255")
                    .attach()
                .defineDatabase(dbName)
                    .withBasicEdition()
                    .attach()
                .create();

            Utils.print(sqlServerForProd);

            // ============================================================
            // Create a connection to the "production" SQL Server.
            System.out.println("Creating a connection to the \"production\" SQL Server");

            String connectionToSqlProdUrl = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;",
                sqlServerForProd.fullyQualifiedDomainName(),
                dbName,
                administratorLogin,
                administratorPassword);

            // Establish the connection.
            try (Connection conProd = DriverManager.getConnection(connectionToSqlProdUrl);
                 Statement stmt1 = conProd.createStatement();) {


                // ============================================================
                // Create a new table into the "production" SQL Server database and insert one value.
                System.out.println("Creating a new table into the \"production\" SQL Server database and insert one value");


                String sqlCommand = "CREATE TABLE [Dns_Alias_Sample_Prod] ([Name] [varchar](30) NOT NULL)";
                stmt1.execute(sqlCommand);

                sqlCommand = "INSERT Dns_Alias_Sample_Prod VALUES ('Production')";
                stmt1.execute(sqlCommand);
            }


            // ============================================================
            // Create a SQL Server DNS alias and use it to query the "test" database.
            System.out.println("Creating a SQL Server DNS alias and use it to query the \"test\" database");

            SqlServerDnsAlias dnsAlias = sqlServerForTest.dnsAliases()
                .define(sqlServerDnsAlias)
                .create();
            ResourceManagerUtils.sleep(Duration.ofMinutes(5));

            String connectionUrl = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;",
                dnsAlias.azureDnsRecord(),
                dbName,
                administratorLogin,
                administratorPassword);

            // Establish the connection.
            try (Connection conDnsAlias = DriverManager.getConnection(connectionUrl);
                 Statement stmt2 = conDnsAlias.createStatement();) {

                String sqlCommand = "SELECT * FROM Dns_Alias_Sample_Test;";
                try (ResultSet resultSet = stmt2.executeQuery(sqlCommand);) {
                    // Print results from select statement
                    System.out.println("SELECT * FROM Dns_Alias_Sample_Test");
                    while (resultSet.next()) {
                        System.out.format("\t%s%n", resultSet.getString(1));
                    }
                }
            }


            // ============================================================
            // Use the "production" SQL Server to acquire the SQL Server DNS Alias and use it to query the "production" database.
            System.out.println("Using the \"production\" SQL Server to acquire the SQL Server DNS Alias and use it to query the \"production\" database");

            sqlServerForProd.dnsAliases().acquire(sqlServerDnsAlias, sqlServerForTest.id());

            // It takes some time for the DNS alias to reflect the new Server connection
            ResourceManagerUtils.sleep(Duration.ofMinutes(10));

            // Re-establish the connection.
            try (Connection conDnsAlias = DriverManager.getConnection(connectionUrl);
                 Statement stmt = conDnsAlias.createStatement();) {

                String sqlCommand = "SELECT * FROM Dns_Alias_Sample_Prod;";
                try (ResultSet resultSet = stmt.executeQuery(sqlCommand);) {
                    // Print results from select statement
                    System.out.println("SELECT * FROM Dns_Alias_Sample_Prod");
                    while (resultSet.next()) {
                        System.out.format("\t%s%n", resultSet.getString(1));
                    }
                }
            }

            // Delete the SQL Servers.
            System.out.println("Deleting the Sql Servers");
            azureResourceManager.sqlServers().deleteById(sqlServerForTest.id());
            azureResourceManager.sqlServers().deleteById(sqlServerForProd.id());
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            }
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
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
