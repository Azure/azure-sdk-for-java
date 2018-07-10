/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.datalake.analytics.models.*;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

public class DataLakeAnalyticsCatalogOperationsTests extends DataLakeAnalyticsManagementTestBase
{
    // Catalog names
    protected static String dbName;
    protected static String tableName;
    protected static String schemaName;
    protected static String tvfName;
    protected static String procName;
    protected static String viewName;
    protected static String credentialName;
    protected static String credentialName2;
    protected static String secretPwd;
    protected static String userId;
    protected static String userId2;
    protected static String userId3;
    protected static String catalogCreationScript;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) throws IOException
    {
        super.initializeClients(restClient, defaultSubscription, domain);

        // Define catalog items
        dbName = generateRandomResourceName("testdb1", 15);
        tableName = generateRandomResourceName("testtable1", 15);
        schemaName = "dbo";
        tvfName = generateRandomResourceName("testtvf1", 15);
        procName = generateRandomResourceName("testproc1", 15);
        viewName = generateRandomResourceName("testview1", 15);
        credentialName = generateRandomResourceName("testcred1", 15);
        credentialName2 = generateRandomResourceName("testcred2", 15);
        secretPwd = generateRandomResourceName("testsecretpwd1", 15);
        userId = generateRandomResourceName("fakeuserid01", 15);
        userId2 = generateRandomResourceName("fakeuserid02", 15);
        userId3 = generateRandomResourceName("fakeuserid03", 15);
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
                "    PARTITIONED BY (UserId) HASH (Region) //Column to partition by\r\n" +
                ");\r\n" +
                "\r\n" +
                "ALTER TABLE {0}.dbo.{1} ADD IF NOT EXISTS PARTITION (1);\r\n" +
                "\r\n" +
                "INSERT INTO {0}.dbo.{1}" +
                "(UserId, Start, Region, Query, Duration, Urls, ClickedUrls)" +
                "ON INTEGRITY VIOLATION MOVE TO PARTITION (1)" +
                "VALUES" +
                "(1, new DateTime(2018, 04, 25), \"US\", @\"fake query\", 34, \"http://url1.fake.com\", \"http://clickedUrl1.fake.com\")," +
                "(1, new DateTime(2018, 04, 26), \"EN\", @\"fake query\", 23, \"http://url2.fake.com\", \"http://clickedUrl2.fake.com\");" +
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

        // Create the catalog
        try
        {
            UUID mockId = UUID.fromString("b9e2ef31-a25d-4a8f-be26-f558b823376f");
            UUID idToUse;

            if (isRecordMode())
            {
                idToUse = UUID.randomUUID();
                addTextReplacementRule(idToUse.toString(), mockId.toString());
            }
            else
            {
                idToUse = mockId;
            }

            runJobToCompletion(jobAndCatalogAdlaName, idToUse, catalogCreationScript);
        }
        catch (Exception e)
        {
            Assert.fail("Catalog creation during setup failed with error: " + e.getMessage());
        }
    }

    @Test
    public void canGetCatalogItems() throws Exception
    {
        UUID principalId;
        UUID mockedId = UUID.fromString("7eafadbc-53f2-435c-83dc-d4794e017f87");

        if (isRecordMode())
        {
            principalId = UUID.fromString(SdkContext.randomUuid());
            addTextReplacementRule(principalId.toString(), mockedId.toString());
        }
        else
        {
            principalId = mockedId;
        }

        List<USqlDatabase> dbListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listDatabases(
                        jobAndCatalogAdlaName
                );

        Assert.assertTrue(dbListResponse.size() >= 1);

        // Look for the DB we created
        boolean foundCatalogElement = false;
        for (USqlDatabase db: dbListResponse)
        {
            if (db.name().equals(dbName))
            {
                foundCatalogElement = true;
                break;
            }
        }

        Assert.assertTrue(foundCatalogElement);

        // Get the specific Database as well
        USqlDatabase dbGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getDatabase(
                        jobAndCatalogAdlaName,
                        dbName
                );

        Assert.assertEquals(dbName, dbGetResponse.name());

        // Get the table list
        List<USqlTable> tableListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listTables(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName
                );

        Assert.assertTrue(tableListResponse.size() >= 1);

        // Look for the table we created
        foundCatalogElement = false;
        for (USqlTable table: tableListResponse)
        {
            if (table.name().equals(tableName))
            {
                foundCatalogElement = true;
                break;
            }
        }

        Assert.assertTrue(foundCatalogElement);

        // Get preview of the specific table
        USqlTablePreview tablePreviewGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().previewTable(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        tableName
                );

        Assert.assertTrue(tablePreviewGetResponse.totalRowCount() > 0);
        Assert.assertTrue(tablePreviewGetResponse.totalColumnCount() > 0);
        Assert.assertTrue(tablePreviewGetResponse.rows() != null && tablePreviewGetResponse.rows().size() > 0);
        Assert.assertTrue(tablePreviewGetResponse.schema() != null && tablePreviewGetResponse.schema().size() > 0);
        Assert.assertNotNull(tablePreviewGetResponse.schema().get(0).name());
        Assert.assertNotNull(tablePreviewGetResponse.schema().get(0).type());

        // Get the specific table as well
        USqlTable tableGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getTable(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        tableName
                );

        Assert.assertEquals(tableName, tableGetResponse.name());

        // Get the TVF list
        List<USqlTableValuedFunction> tvfListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listTableValuedFunctions(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName
                );

        Assert.assertTrue(tvfListResponse.size() >= 1);

        // Look for the tvf we created
        foundCatalogElement = false;
        for (USqlTableValuedFunction tvf: tvfListResponse)
        {
            if (tvf.name().equals(tvfName))
            {
                foundCatalogElement = true;
                break;
            }
        }

        Assert.assertTrue(foundCatalogElement);

        // Get the specific TVF as well
        USqlTableValuedFunction tvfGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getTableValuedFunction(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        tvfName
                );

        Assert.assertEquals(tvfName, tvfGetResponse.name());

        // Get the View list
        List<USqlView> viewListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listViews(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName
                );

        Assert.assertTrue(viewListResponse.size() >= 1);

        // Look for the view we created
        foundCatalogElement = false;
        for (USqlView view: viewListResponse)
        {
            if (view.name().equals(viewName))
            {
                foundCatalogElement = true;
                break;
            }
        }

        Assert.assertTrue(foundCatalogElement);

        // Get the specific view as well
        USqlView viewGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getView(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        viewName
                );

        Assert.assertEquals(viewName, viewGetResponse.name());

        // Get the Procedure list
        List<USqlProcedure> procListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listProcedures(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName
                );

        Assert.assertTrue(procListResponse.size() >= 1);

        // Look for the procedure we created
        foundCatalogElement = false;
        for (USqlProcedure proc: procListResponse)
        {
            if (proc.name().equals(procName))
            {
                foundCatalogElement = true;
                break;
            }
        }

        Assert.assertTrue(foundCatalogElement);

        // Get the specific procedure as well
        USqlProcedure procGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getProcedure(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        procName
                );

        Assert.assertEquals(procName, procGetResponse.name());

        List<USqlTablePartition> partitionList =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listTablePartitions(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        tableName
                );

        Assert.assertTrue(partitionList.size() >= 1);

        USqlTablePartition specificPartition = partitionList.get(0);

        // Get preview of the specific partition
        USqlTablePreview partitionPreviewGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().previewTablePartition(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        tableName,
                        specificPartition.name()
                );

        Assert.assertTrue(partitionPreviewGetResponse.totalRowCount() > 0);
        Assert.assertTrue(partitionPreviewGetResponse.totalColumnCount() > 0);
        Assert.assertTrue(partitionPreviewGetResponse.rows() != null && partitionPreviewGetResponse.rows().size() > 0);
        Assert.assertTrue(partitionPreviewGetResponse.schema() != null && partitionPreviewGetResponse.schema().size() > 0);
        Assert.assertNotNull(partitionPreviewGetResponse.schema().get(0).name());
        Assert.assertNotNull(partitionPreviewGetResponse.schema().get(0).type());

        // Get the specific partition as well
        USqlTablePartition partitionGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getTablePartition(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        tableName,
                        specificPartition.name()
                );

        Assert.assertEquals(specificPartition.name(), partitionGetResponse.name());

        // Get the fragment list
        PagedList<USqlTableFragment> fragmentList =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listTableFragments(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        tableName
                );

        Assert.assertNotNull(fragmentList);
        Assert.assertTrue(fragmentList.size() > 0);

        // Get all the types
        List<USqlType> typeGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listTypes(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName
                );

        Assert.assertNotNull(typeGetResponse);
        Assert.assertTrue(typeGetResponse.size() > 0);

        // Get all the types that are not complex
        typeGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listTypes(
                        jobAndCatalogAdlaName,
                        dbName,
                        schemaName,
                        "isComplexType eq false",
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Assert.assertNotNull(typeGetResponse);
        Assert.assertTrue(typeGetResponse.size() > 0);

        foundCatalogElement = false;
        for (USqlType type: typeGetResponse)
        {
            if (type.isComplexType())
            {
                foundCatalogElement = true;
                break;
            }
        }

        Assert.assertFalse(foundCatalogElement);

        // Prepare to grant/revoke ACLs
        AclCreateOrUpdateParameters grantAclParams = new AclCreateOrUpdateParameters()
                .withAceType(AclType.USER)
                .withPrincipalId(principalId)
                .withPermission(PermissionType.USE);

        AclDeleteParameters revokeAclParams = new AclDeleteParameters()
                .withAceType(AclType.USER)
                .withPrincipalId(principalId);

        // Get the initial number of ACLs by db
        List<Acl> aclByDbListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listAclsByDatabase(
                        jobAndCatalogAdlaName,
                        dbName
                );

        int aclByDbCount = aclByDbListResponse.size();

        // Get the initial number of ACLs by catalog
        List<Acl> aclListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listAcls(
                        jobAndCatalogAdlaName
                );

        int aclCount = aclListResponse.size();

        // Grant ACL to the db
        dataLakeAnalyticsCatalogManagementClient.catalogs().grantAclToDatabase(
                jobAndCatalogAdlaName,
                dbName,
                grantAclParams
        );

        aclByDbListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listAclsByDatabase(
                        jobAndCatalogAdlaName,
                        dbName
                );

        int listResponseCount = aclByDbListResponse.size();
        Acl acl = aclByDbListResponse.get(listResponseCount - 1);

        // Confirm the ACL's information
        Assert.assertEquals(aclByDbCount + 1, listResponseCount);
        Assert.assertEquals(AclType.USER, acl.aceType());
        Assert.assertEquals(principalId, acl.principalId());
        Assert.assertEquals(PermissionType.USE, acl.permission());

        // Revoke ACL from the db
        dataLakeAnalyticsCatalogManagementClient.catalogs().revokeAclFromDatabase(
                jobAndCatalogAdlaName,
                dbName,
                revokeAclParams
        );

        aclByDbListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listAclsByDatabase(
                        jobAndCatalogAdlaName,
                        dbName
                );

        Assert.assertEquals(aclByDbCount, aclByDbListResponse.size());

        // Grant ACL to the catalog
        dataLakeAnalyticsCatalogManagementClient.catalogs().grantAcl(
                jobAndCatalogAdlaName,
                grantAclParams
        );

        aclListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listAcls(
                        jobAndCatalogAdlaName
                );

        listResponseCount = aclListResponse.size();
        acl = aclListResponse.get(listResponseCount - 1);

        // Confirm the ACL's information
        Assert.assertEquals(aclCount + 1, listResponseCount);
        Assert.assertEquals(AclType.USER, acl.aceType());
        Assert.assertEquals(principalId, acl.principalId());
        Assert.assertEquals(PermissionType.USE, acl.permission());

        // Revoke ACL from the catalog
        dataLakeAnalyticsCatalogManagementClient.catalogs().revokeAcl(
                jobAndCatalogAdlaName,
                revokeAclParams
        );

        aclListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listAcls(
                        jobAndCatalogAdlaName
                );

        Assert.assertEquals(aclCount, aclListResponse.size());
    }

    @Test
    public void  canCreateUpdateDeleteCredentials() throws Exception
    {
        // Create the credential
        DataLakeAnalyticsCatalogCredentialCreateParameters createParams = new DataLakeAnalyticsCatalogCredentialCreateParameters()
                .withPassword(secretPwd)
                .withUri("https://adlasecrettest.contoso.com:443")
                .withUserId(userId);

        dataLakeAnalyticsCatalogManagementClient.catalogs().createCredential(
                jobAndCatalogAdlaName,
                dbName,
                credentialName,
                createParams
        );

        // Attempt to create the secret again, which should throw
        try
        {
            createParams
                    .withPassword(secretPwd)
                    .withUri("https://adlasecrettest.contoso.com:443")
                    .withUserId(userId2);

            dataLakeAnalyticsCatalogManagementClient.catalogs().createCredential(
                    jobAndCatalogAdlaName,
                    dbName,
                    credentialName,
                    createParams
            );

            // Should never make it here
            Assert.assertTrue(false);
        }
        catch(Exception e)
        {
            // Expected
        }

        // Create another credential
        createParams
                .withPassword(secretPwd)
                .withUri("https://adlasecrettest.contoso.com:443")
                .withUserId(userId3);

        dataLakeAnalyticsCatalogManagementClient.catalogs().createCredential(
                jobAndCatalogAdlaName,
                dbName,
                credentialName2,
                createParams
        );

        // Get the credential and ensure that the response is valid
        USqlCredential credGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getCredential(
                        jobAndCatalogAdlaName,
                        dbName,
                        credentialName
                );

        Assert.assertNotNull(credGetResponse);
        Assert.assertEquals(credentialName, credGetResponse.name());

        // Get the credential list
        List<USqlCredential> credListResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().listCredentials(
                        jobAndCatalogAdlaName,
                        dbName
                );

        Assert.assertTrue(credListResponse.size() >= 1);

        // Look for the credential we created
        boolean foundCatalogElement = false;
        for (USqlCredential cred : credListResponse)
        {
            if (cred.name().equals(credentialName))
            {
                foundCatalogElement = true;
                break;
            }
        }

        Assert.assertTrue(foundCatalogElement);

        // Delete the credential
        dataLakeAnalyticsCatalogManagementClient.catalogs().deleteCredential(
                jobAndCatalogAdlaName,
                dbName,
                credentialName,
                null,
                secretPwd
        );

        // Try to get the credential, which should return a null response
        credGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getCredential(
                        jobAndCatalogAdlaName,
                        dbName,
                        credentialName
                );

        Assert.assertNull(credGetResponse);

        // Re-create and delete the credential using cascade = true, which should still succeed
        createParams
                .withPassword(secretPwd)
                .withUri("https://adlasecrettest.contoso.com:443")
                .withUserId(userId);

        dataLakeAnalyticsCatalogManagementClient.catalogs().createCredential(
                jobAndCatalogAdlaName,
                dbName,
                credentialName,
                createParams
        );

        dataLakeAnalyticsCatalogManagementClient.catalogs().deleteCredential(
                jobAndCatalogAdlaName,
                dbName,
                credentialName,
                true,
                secretPwd
        );

        // Try to get the credential, which should return a null response
        credGetResponse =
                dataLakeAnalyticsCatalogManagementClient.catalogs().getCredential(
                        jobAndCatalogAdlaName,
                        dbName,
                        credentialName
                );

        Assert.assertNull(credGetResponse);
    }
}
