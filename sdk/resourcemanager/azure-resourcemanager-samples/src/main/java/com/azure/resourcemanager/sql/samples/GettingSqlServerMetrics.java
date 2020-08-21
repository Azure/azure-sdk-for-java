// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.monitor.fluent.inner.MetadataValueInner;
import com.azure.resourcemanager.monitor.models.Metric;
import com.azure.resourcemanager.monitor.models.MetricCollection;
import com.azure.resourcemanager.monitor.models.MetricDefinition;
import com.azure.resourcemanager.monitor.models.MetricValue;
import com.azure.resourcemanager.monitor.models.TimeSeriesElement;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetric;
import com.azure.resourcemanager.sql.models.SqlDatabaseUsageMetric;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlSubscriptionUsageMetric;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Azure SQL sample for getting SQL Server and Databases metrics
 *  - Create a primary SQL Server with a sample database.
 *  - Run some queries on the sample database.
 *  - Create a new table and insert some values into the database.
 *  - List the SQL subscription usage metrics, the database usage metrics and the other database metrics
 *  - Use the Monitor Service Fluent APIs to list the SQL Server metrics and the SQL Database metrics
 *  - Delete Sql Server
 */
public class GettingSqlServerMetrics {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String sqlServerName = azure.sdkContext().randomResourceName("sqltest", 20);
        final String dbName = "dbSample";
        final String epName = "epSample";
        final String rgName = azure.sdkContext().randomResourceName("rgsql", 20);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);

        try {
            // Check if the expected SQL driver is available
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // ============================================================
            // Create a SQL Server.
            System.out.println("Creating a SQL server to be used for getting various metrics");

            SqlServer sqlServer = azure.sqlServers().define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineFirewallRule("allowAll")
                    .withIpAddressRange("0.0.0.1", "255.255.255.255")
                    .attach()
                .defineElasticPool(epName)
                    .withStandardPool()
                    .attach()
                .defineDatabase(dbName)
                    .withExistingElasticPool(epName)
                    .fromSample(SampleName.ADVENTURE_WORKS_LT)
                    .attach()
                .create();

            Utils.print(sqlServer);

            // ============================================================
            // Create a connection to the SQL Server.
            System.out.println("Creating a connection to the SQL Server");
            String connectionToSqlTestUrl = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;",
                sqlServer.fullyQualifiedDomainName(),
                dbName,
                administratorLogin,
                administratorPassword);

            // Establish the connection.
            try (Connection connection = DriverManager.getConnection(connectionToSqlTestUrl);
                 Statement statement = connection.createStatement();) {

                // ============================================================
                // Create and execute a "select" SQL statement on the sample database.
                System.out.println("Creating and executing a \"SELECT\" SQL statement on the sample database");

                String selectSql = "SELECT TOP 10 Title, FirstName, LastName from SalesLT.Customer";

                System.out.println("SELECT TOP 10 Title, FirstName, LastName from SalesLT.Customer");
                try (ResultSet resultSet = statement.executeQuery(selectSql);) {

                    // Print results from select statement
                    while (resultSet.next()) {
                        System.out.println(resultSet.getString(2) + " "
                            + resultSet.getString(3));
                    }
                }

                // ============================================================
                // Create and execute an "INSERT" SQL statement on the sample database.
                System.out.println("Creating and executing an \"INSERT\" SQL statement on the sample database");
                // Create and execute an INSERT SQL prepared statement.
                String insertSql = "INSERT INTO SalesLT.Product (Name, ProductNumber, Color, StandardCost, ListPrice, SellStartDate) VALUES "
                    + "('Bike', 'B1', 'Blue', 50, 120, '2016-01-01');";

                try (PreparedStatement prepsInsertProduct = connection.prepareStatement(
                    insertSql,
                    Statement.RETURN_GENERATED_KEYS);) {
                    prepsInsertProduct.execute();

                    // Retrieve the generated key from the insert.
                    try (ResultSet resultSet2 = prepsInsertProduct.getGeneratedKeys();) {
                        // Print the ID of the inserted row.
                        while (resultSet2.next()) {
                            System.out.println("Generated: " + resultSet2.getString(1));
                        }
                    }
                }

                // ============================================================
                // Create a new table into the SQL Server database and insert one value.
                System.out.println("Creating a new table into the SQL Server database and insert one value");

                try (Statement stmt = connection.createStatement();) {

                    String sqlCommand = "CREATE TABLE [Sample_Test] ([Name] [varchar](30) NOT NULL)";
                    stmt.execute(sqlCommand);

                    sqlCommand = "INSERT Sample_Test VALUES ('Test')";
                    stmt.execute(sqlCommand);

                    // ============================================================
                    // Run a "select" query for the new table.
                    System.out.println("Running a \"SELECT\" query for the new table");

                    sqlCommand = "SELECT * FROM Sample_Test;";
                    try (ResultSet resultSet = stmt.executeQuery(sqlCommand);) {
                        // Print results from select statement
                        System.out.println("SELECT * FROM Sample_Test");
                        while (resultSet.next()) {
                            System.out.format("\t%s%n", resultSet.getString(1));
                        }
                    }
                }
            }

            SdkContext.sleep(6 * 60 * 1000);


            // ============================================================
            // List the SQL subscription usage metrics for the current selected region.
            System.out.println("Listing the SQL subscription usage metrics for the current selected region");


            List<SqlSubscriptionUsageMetric> subscriptionUsageMetrics = azure.sqlServers().listUsageByRegion(Region.US_EAST);
            for (SqlSubscriptionUsageMetric usageMetric : subscriptionUsageMetrics) {
                Utils.print(usageMetric);
            }

            // ============================================================
            // List the SQL database usage metrics for the sample database.
            System.out.println("Listing the SQL database usage metrics for the sample database");

            SqlDatabase db = sqlServer.databases().get(dbName);

            List<SqlDatabaseUsageMetric> databaseUsageMetrics = db.listUsageMetrics();
            for (SqlDatabaseUsageMetric usageMetric : databaseUsageMetrics) {
                Utils.print(usageMetric);
            }

            // ============================================================
            // List the SQL database CPU metrics for the sample database.
            System.out.println("Listing the SQL database CPU metrics for the sample database");

            OffsetDateTime endTime = OffsetDateTime.now();
            String filter = String.format("name/value eq 'cpu_percent' and startTime eq '%s' and endTime eq '%s'", startTime.toInstant(), endTime.toInstant());

            List<SqlDatabaseMetric> dbMetrics = db.listMetrics(filter);
            for (SqlDatabaseMetric metric : dbMetrics) {
                Utils.print(metric);
            }

            // ============================================================
            // List the SQL database metrics for the sample database.
            System.out.println("Listing the SQL database metrics for the sample database");

            endTime = OffsetDateTime.now();
            filter = String.format("startTime eq '%s' and endTime eq '%s'", startTime.toInstant(), endTime.toInstant());

            dbMetrics = db.listMetrics(filter);
            for (SqlDatabaseMetric metric : dbMetrics) {
                Utils.print(metric);
            }

            // ============================================================
            // Use Monitor Service to list the SQL server metrics.

            System.out.println("Using Monitor Service to list the SQL server metrics");
            PagedIterable<MetricDefinition> metricDefinitions = azure.metricDefinitions().listByResource(sqlServer.id());

            for (MetricDefinition metricDefinition : metricDefinitions) {
                // find metric definition for "DTU used" and "Storage used"
                if (metricDefinition.name().localizedValue().equalsIgnoreCase("dtu used")
                    || metricDefinition.name().localizedValue().equalsIgnoreCase("storage used")) {
                    // get metric records
                    MetricCollection metricCollection = metricDefinition.defineQuery()
                        .startingFrom(startTime)
                        .endsBefore(endTime)
                        .withAggregation("Average")
                        .withInterval(Duration.ofMinutes(5))
                        .withOdataFilter(String.format("DatabaseResourceId eq '%s'", db.id()))
                        .execute();

                    System.out.format("SQL server \"%s\" %s metrics%n", sqlServer.name(), metricDefinition.name().localizedValue());
                    System.out.println("\tNamespace: " + metricCollection.namespace());
                    System.out.println("\tQuery time: " + metricCollection.timespan());
                    System.out.println("\tTime Grain: " + metricCollection.interval());
                    System.out.println("\tCost: " + metricCollection.cost());

                    for (Metric metric : metricCollection.metrics()) {
                        System.out.println("\tMetric: " + metric.name().localizedValue());
                        System.out.println("\tType: " + metric.type());
                        System.out.println("\tUnit: " + metric.unit());
                        System.out.println("\tTime Series: ");
                        for (TimeSeriesElement timeElement : metric.timeseries()) {
                            System.out.println("\t\tMetadata: ");
                            for (MetadataValueInner metadata : timeElement.metadatavalues()) {
                                System.out.println("\t\t\t" + metadata.name().localizedValue() + ": " + metadata.value());
                            }
                            System.out.println("\t\tData: ");
                            for (MetricValue data : timeElement.data()) {
                                System.out.println("\t\t\t" + data.timestamp()
                                    + " : (Min) " + data.minimum()
                                    + " : (Max) " + data.maximum()
                                    + " : (Avg) " + data.average()
                                    + " : (Total) " + data.total()
                                    + " : (Count) " + data.count());
                            }
                        }
                    }
                    break;
                }
            }

            // ============================================================
            // Use Monitor Service to list the SQL Database metrics.
            System.out.println("Using Monitor Service to list the SQL Database metrics");
            metricDefinitions = azure.metricDefinitions().listByResource(db.id());

            for (MetricDefinition metricDefinition : metricDefinitions) {
                // find metric definition for Transactions
                if (metricDefinition.name().localizedValue().equalsIgnoreCase("dtu used")
                    || metricDefinition.name().localizedValue().equalsIgnoreCase("cpu used")
                    || metricDefinition.name().localizedValue().equalsIgnoreCase("storage used")) {
                    // get metric records
                    MetricCollection metricCollection = metricDefinition.defineQuery()
                        .startingFrom(startTime)
                        .endsBefore(endTime)
                        .execute();

                    System.out.println("Metrics for '" + db.id() + "':");
                    System.out.println("Namespace: " + metricCollection.namespace());
                    System.out.println("Query time: " + metricCollection.timespan());
                    System.out.println("Time Grain: " + metricCollection.interval());
                    System.out.println("Cost: " + metricCollection.cost());

                    for (Metric metric : metricCollection.metrics()) {
                        System.out.println("\tMetric: " + metric.name().localizedValue());
                        System.out.println("\tType: " + metric.type());
                        System.out.println("\tUnit: " + metric.unit());
                        System.out.println("\tTime Series: ");
                        for (TimeSeriesElement timeElement : metric.timeseries()) {
                            System.out.println("\t\tMetadata: ");
                            for (MetadataValueInner metadata : timeElement.metadatavalues()) {
                                System.out.println("\t\t\t" + metadata.name().localizedValue() + ": " + metadata.value());
                            }
                            System.out.println("\t\tData: ");
                            for (MetricValue data : timeElement.data()) {
                                System.out.println("\t\t\t" + data.timestamp()
                                    + " : (Min) " + data.minimum()
                                    + " : (Max) " + data.maximum()
                                    + " : (Avg) " + data.average()
                                    + " : (Total) " + data.total()
                                    + " : (Count) " + data.count());
                            }
                        }
                    }
                    break;
                }
            }

            // Delete the SQL Servers.
            System.out.println("Deleting the Sql Server");
            azure.sqlServers().deleteById(sqlServer.id());
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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
}
