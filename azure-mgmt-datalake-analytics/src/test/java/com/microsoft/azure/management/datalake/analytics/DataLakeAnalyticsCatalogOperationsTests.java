/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters;
import com.microsoft.azure.management.datalake.analytics.models.USqlCredential;
import com.microsoft.azure.management.datalake.analytics.models.USqlDatabase;
import com.microsoft.azure.management.datalake.analytics.models.USqlProcedure;
import com.microsoft.azure.management.datalake.analytics.models.USqlSecret;
import com.microsoft.azure.management.datalake.analytics.models.USqlTable;
import com.microsoft.azure.management.datalake.analytics.models.USqlTableValuedFunction;
import com.microsoft.azure.management.datalake.analytics.models.USqlType;
import com.microsoft.azure.management.datalake.analytics.models.USqlView;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

public class DataLakeAnalyticsCatalogOperationsTests extends DataLakeAnalyticsManagementTestBase {
    // catalog names
    protected static String dbName;
    protected static String tableName;
    protected static String tvfName;
    protected static String procName;
    protected static String viewName;
    protected static String credentialName;
    protected static String secretName;
    protected static String secretPwd;
    protected static String catalogCreationScript;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) throws IOException {
        super.initializeClients(restClient, defaultSubscription, domain);
        // define catalog items
        dbName = generateRandomResourceName("testdb1", 15);
        tableName = generateRandomResourceName("testtable1", 15);
        tvfName = generateRandomResourceName("testtvf1", 15);
        procName = generateRandomResourceName("testproc1", 15);
        viewName = generateRandomResourceName("testview1", 15);
        credentialName = generateRandomResourceName("testcred1", 15);
        secretName = generateRandomResourceName("testsecret1", 15);
        secretPwd = generateRandomResourceName("testsecretpwd1", 15);
        catalogCreationScript = MessageFormat.format("DROP DATABASE IF EXISTS {0}; CREATE DATABASE {0}; \r\n" +
                "//Create Table\r\n" +
                "CREATE TABLE {0}.dbo.{1}\r\n" +
                "(\r\n" +
                "        //Define schema of table\r\n" +
                "        UserId          int, \r\n" +
                "        Start           DateTime, \r\n" +
                "        Region          string, \r\n" +
                "        Query           string, \r\n" +
                "        Duration        int, \r\n" +
                "        Urls            string, \r\n" +
                "        ClickedUrls     string,\r\n" +
                "    INDEX idx1 //Name of index\r\n" +
                "    CLUSTERED (Region ASC) //Column to cluster by\r\n" +
                "    PARTITIONED BY BUCKETS (UserId) HASH (Region) //Column to partition by\r\n" +
                ");\r\n" +
                "\r\n" +
                "ALTER TABLE {0}.dbo.{1} ADD IF NOT EXISTS PARTITION (1);\r\n" +
                "\r\n" +
                "DROP FUNCTION IF EXISTS {0}.dbo.{2};\r\n" +
                "\r\n" +
                "//create table weblogs on space-delimited website log data\r\n" +
                "CREATE FUNCTION {0}.dbo.{2}()\r\n" +
                "RETURNS @result TABLE\r\n" +
                "(\r\n" +
                "    s_date DateTime,\r\n" +
                "    s_time string,\r\n" +
                "    s_sitename string,\r\n" +
                "    cs_method string, \r\n" +
                "    cs_uristem string,\r\n" +
                "    cs_uriquery string,\r\n" +
                "    s_port int,\r\n" +
                "    cs_username string, \r\n" +
                "    c_ip string,\r\n" +
                "    cs_useragent string,\r\n" +
                "    cs_cookie string,\r\n" +
                "    cs_referer string, \r\n" +
                "    cs_host string,\r\n" +
                "    sc_status int,\r\n" +
                "    sc_substatus int,\r\n" +
                "    sc_win32status int, \r\n" +
                "    sc_bytes int,\r\n" +
                "    cs_bytes int,\r\n" +
                "    s_timetaken int\r\n" +
                ")\r\n" +
                "AS\r\n" +
                "BEGIN\r\n" +
                "\r\n" +
                "    @result = EXTRACT\r\n" +
                "        s_date DateTime,\r\n" +
                "        s_time string,\r\n" +
                "        s_sitename string,\r\n" +
                "        cs_method string,\r\n" +
                "        cs_uristem string,\r\n" +
                "        cs_uriquery string,\r\n" +
                "        s_port int,\r\n" +
                "        cs_username string,\r\n" +
                "        c_ip string,\r\n" +
                "        cs_useragent string,\r\n" +
                "        cs_cookie string,\r\n" +
                "        cs_referer string,\r\n" +
                "        cs_host string,\r\n" +
                "        sc_status int,\r\n" +
                "        sc_substatus int,\r\n" +
                "        sc_win32status int,\r\n" +
                "        sc_bytes int,\r\n" +
                "        cs_bytes int,\r\n" +
                "        s_timetaken int\r\n" +
                "    FROM @\"/Samples/Data/WebLog.log\"\r\n" +
                "    USING Extractors.Text(delimiter:''' ''');\r\n" +
                "\r\n" +
                "RETURN;\r\n" +
                "END;\r\n" +
                "CREATE VIEW {0}.dbo.{3} \r\n" +
                "AS \r\n" +
                "    SELECT * FROM \r\n" +
                "    (\r\n" +
                "        VALUES(1,2),(2,4)\r\n" +
                "    ) \r\n" +
                "AS \r\n" +
                "T(a, b);\r\n" +
                "CREATE PROCEDURE {0}.dbo.{4}()\r\n" +
                "AS BEGIN\r\n" +
                "  CREATE VIEW {0}.dbo.{3} \r\n" +
                "  AS \r\n" +
                "    SELECT * FROM \r\n" +
                "    (\r\n" +
                "        VALUES(1,2),(2,4)\r\n" +
                "    ) \r\n" +
                "  AS \r\n" +
                "  T(a, b);\r\n" +
                "END;", dbName, tableName, tvfName, viewName, procName);

        // create the catalog
        try {
            UUID mockId = UUID.fromString("b9e2ef31-a25d-4a8f-be26-f558b823376f");
            UUID idToUse;
            if(isRecordMode()) {
                idToUse = UUID.randomUUID();
                addTextReplacementRule(idToUse.toString(), mockId.toString());
            }
            else {
                idToUse = mockId;
            }

            runJobToCompletion(jobAndCatalogAdlaName, idToUse, catalogCreationScript);
        }
        catch (Exception e){
            Assert.fail("Catalog creation during setup failed with error: " + e.getMessage());
        }
    }

    // TODO: re-enable this test once the underlying issue is investigated and resolved by the product team.
    //@Test
    public void canGetCatalogItems() throws Exception {
        List<USqlDatabase> dbListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listDatabases(jobAndCatalogAdlaName);
        Assert.assertTrue(dbListResponse.size() >= 1);

        // look for the DB we created
        boolean foundCatalogElement = false;
        for (USqlDatabase db: dbListResponse) {
            if (db.name().equals(dbName)) {
                foundCatalogElement = true;
                break;
            }
        }
        Assert.assertTrue(foundCatalogElement);

        // Get the specific Database as well
        USqlDatabase dbGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getDatabase(jobAndCatalogAdlaName, dbName);

        Assert.assertEquals(dbName, dbGetResponse.name());

        // Get the table list
        List<USqlTable> tableListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listTables(jobAndCatalogAdlaName, dbName, "dbo");

        Assert.assertTrue(tableListResponse.size() >= 1);

        // look for the table we created
        foundCatalogElement = false;
        for (USqlTable table: tableListResponse) {
            if (table.name().equals(tableName)) {
                foundCatalogElement = true;
                break;
            }
        }
        Assert.assertTrue(foundCatalogElement);

        // Get the specific table as well
        USqlTable tableGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getTable(
                jobAndCatalogAdlaName, dbName, "dbo", tableName);

        Assert.assertEquals(tableName, tableGetResponse.name());

        // Get the TVF list
        List<USqlTableValuedFunction> tvfListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listTableValuedFunctions(jobAndCatalogAdlaName, dbName, "dbo");

        Assert.assertTrue(tvfListResponse.size() >= 1);

        // look for the tvf we created
        foundCatalogElement = false;
        for (USqlTableValuedFunction tvf: tvfListResponse) {
            if (tvf.name().equals(tvfName)) {
                foundCatalogElement = true;
                break;
            }
        }
        Assert.assertTrue(foundCatalogElement);

        // Get the specific TVF as well
        USqlTableValuedFunction tvfGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getTableValuedFunction(
                jobAndCatalogAdlaName, dbName, "dbo", tvfName);

        Assert.assertEquals(tvfName, tvfGetResponse.name());

        // Get the View list
        List<USqlView> viewListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listViews(jobAndCatalogAdlaName, dbName, "dbo");

        Assert.assertTrue(viewListResponse.size() >= 1);

        // look for the view we created
        foundCatalogElement = false;
        for (USqlView view: viewListResponse) {
            if (view.name().equals(viewName)) {
                foundCatalogElement = true;
                break;
            }
        }
        Assert.assertTrue(foundCatalogElement);

        // Get the specific view as well
        USqlView viewGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getView(
                jobAndCatalogAdlaName, dbName, "dbo", viewName);

        Assert.assertEquals(viewName, viewGetResponse.name());

        // Get the Procedure list
        List<USqlProcedure> procListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listProcedures(
                jobAndCatalogAdlaName, dbName, "dbo");

        Assert.assertTrue(procListResponse.size() >= 1);

        // look for the procedure we created
        foundCatalogElement = false;
        for (USqlProcedure proc: procListResponse) {
            if (proc.name().equals(procName)) {
                foundCatalogElement = true;
                break;
            }
        }
        Assert.assertTrue(foundCatalogElement);

        // Get the specific procedure as well
        USqlProcedure procGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getProcedure(
                jobAndCatalogAdlaName, dbName, "dbo", procName);

        Assert.assertEquals(procName, procGetResponse.name());

        // Get all the types
        List<USqlType> typeGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listTypes(
                jobAndCatalogAdlaName, dbName, "dbo");


        Assert.assertNotNull(typeGetResponse);
        Assert.assertTrue(typeGetResponse.size() > 0);

        // Get all the types that are not complex
        typeGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listTypes(
                jobAndCatalogAdlaName, dbName, "dbo", "isComplexType eq false", null, null, null, null, null);


        Assert.assertNotNull(typeGetResponse);
        Assert.assertTrue(typeGetResponse.size() > 0);
        foundCatalogElement = false;
        for (USqlType type: typeGetResponse) {
            if (type.isComplexType()) {
                foundCatalogElement = true;
                break;
            }
        }
        Assert.assertFalse(foundCatalogElement);
    }

    @Test
    public void  canCreateUpdateDeleteSecretsAndCredentials() throws Exception {
        // create the secret
        DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters createParams = new DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters();
        createParams.withPassword(secretPwd);
        createParams.withUri("https://adlasecrettest.contoso.com:443");
        dataLakeAnalyticsCatalogManagementClient.catalogs().createSecret(
                jobAndCatalogAdlaName, dbName, secretName,
                createParams);
        
        // Attempt to create the secret again, which should throw
        try {
            dataLakeAnalyticsCatalogManagementClient.catalogs().createSecret(
                jobAndCatalogAdlaName,
                dbName, secretName,
                createParams);
            // should never make it here
            Assert.assertTrue(false);
        }
        catch(Exception e) {
            // expected.
        }

        // Get the secret and ensure the response contains a date.
        USqlSecret secretGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getSecret(
                jobAndCatalogAdlaName, dbName, secretName);

        Assert.assertNotNull(secretGetResponse);
        Assert.assertNotNull(secretGetResponse.creationTime());

        // Create a credential with the secret
        String credentialCreationScript =
                String.format("USE %s; CREATE CREDENTIAL %s WITH USER_NAME = \"scope@rkm4grspxa\", IDENTITY = \"%s\";",
                        dbName, credentialName, secretName);

        // setup job ids for mocks
        UUID mockedId = UUID.fromString("fa9fa5bf-ff12-48af-8a0b-2800049ef23e");
        UUID toUse;
        if(isRecordMode()) {
            toUse = UUID.randomUUID();
            addTextReplacementRule(toUse.toString(), mockedId.toString());
        }
        else {
            toUse = mockedId;
        }
        runJobToCompletion(jobAndCatalogAdlaName, toUse, credentialCreationScript);

        // Get the Credential list
        List<USqlCredential> credListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listCredentials(
                jobAndCatalogAdlaName, dbName);
        Assert.assertTrue(credListResponse.size() >= 1);

        // look for the credential we created
        boolean foundCatalogElement = false;
        for (USqlCredential cred: credListResponse) {
            if (cred.name().equals(credentialName)) {
                foundCatalogElement = true;
                break;
            }
        }
        Assert.assertTrue(foundCatalogElement);

        // Get the specific credential as well
        USqlCredential credGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getCredential(
                jobAndCatalogAdlaName, dbName, credentialName);
        Assert.assertEquals(credentialName, credGetResponse.name());

        // Drop the credential (to enable secret deletion)
        String credentialDropScript =
                String.format("USE %s; DROP CREDENTIAL %s;", dbName, credentialName);

        // setup job ids for mocks
        UUID mockedId2 = UUID.fromString("54a54b5b-6209-455e-8bd1-832a6fcff3d8");
        UUID toUse2;
        if(isRecordMode()) {
            toUse2 = UUID.randomUUID();
            addTextReplacementRule(toUse2.toString(), mockedId2.toString());
        }
        else {
            toUse2 = mockedId2;
        }
        runJobToCompletion(jobAndCatalogAdlaName, toUse2,
                credentialDropScript);

        // Delete the secret
        dataLakeAnalyticsCatalogManagementClient.catalogs().deleteSecret(
                jobAndCatalogAdlaName, dbName, secretName);

        // Try to get the secret which should throw
        try {
            USqlSecret nothing = dataLakeAnalyticsCatalogManagementClient.catalogs().getSecret(
                    jobAndCatalogAdlaName, dbName, secretName);

            // should never make it here and if we do there should not be a secret.
            Assert.assertNull("Was able to retrieve a deleted secret", nothing);
        }
        catch (Exception e) {
            // expected
        }

    }
}
