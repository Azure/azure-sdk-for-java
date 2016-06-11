package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsAccount;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsAccountProperties;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeStoreAccountInfo;
import com.microsoft.azure.management.datalake.analytics.models.USqlCredential;
import com.microsoft.azure.management.datalake.analytics.models.USqlDatabase;
import com.microsoft.azure.management.datalake.analytics.models.USqlProcedure;
import com.microsoft.azure.management.datalake.analytics.models.USqlSecret;
import com.microsoft.azure.management.datalake.analytics.models.USqlTable;
import com.microsoft.azure.management.datalake.analytics.models.USqlTableValuedFunction;
import com.microsoft.azure.management.datalake.analytics.models.USqlType;
import com.microsoft.azure.management.datalake.analytics.models.USqlView;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataLakeAnalyticsCatalogOperationsTests extends DataLakeAnalyticsManagementTestBase {
    private static String rgName = generateName("javaadlarg");
    private static String location;
    private static String adlsAcct = generateName("javaadlsacct");
    private static String adlaAcct = generateName("javaadlaacct");

    // catalog names
    private static String dbName = generateName("testdb1");
    private static String tableName = generateName("testtable1");
    private static String tvfName = generateName("testtvf1");
    private static String procName = generateName("testproc1");
    private static String viewName = generateName("testview1");
    private static String credentialName = generateName("testcred1");
    private static String secretName = generateName("testsecret1");
    private static String secretPwd = generateName("testsecretpwd1");
    private static String catalogCreationScript = MessageFormat.format("DROP DATABASE IF EXISTS {0}; CREATE DATABASE {0}; \r\n" +
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

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        location = environmentLocation;
        ResourceGroupInner group = new ResourceGroupInner();
        group.withLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccount adlsAccount = new DataLakeStoreAccount();
        adlsAccount.withLocation(location);
        adlsAccount.withName(adlsAcct);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);

        // Create the ADLA acct to use.
        DataLakeAnalyticsAccountProperties createProperties = new DataLakeAnalyticsAccountProperties();
        List<DataLakeStoreAccountInfo> adlsAccts = new ArrayList<DataLakeStoreAccountInfo>();
        DataLakeStoreAccountInfo adlsInfo = new DataLakeStoreAccountInfo();
        adlsInfo.withName(adlsAcct);
        adlsAccts.add(adlsInfo);

        createProperties.withDataLakeStoreAccounts(adlsAccts);
        createProperties.withDefaultDataLakeStoreAccount(adlsAcct);

        DataLakeAnalyticsAccount createParams = new DataLakeAnalyticsAccount();
        createParams.withLocation(location);
        createParams.withName(adlaAcct);
        createParams.withProperties(createProperties);
        dataLakeAnalyticsAccountManagementClient.accounts().create(rgName, adlaAcct, createParams);
        // Sleep for two minutes to ensure the account is totally provisioned.
        Thread.sleep(120000);

        // create the catalog
        runJobToCompletion(dataLakeAnalyticsJobManagementClient, adlaAcct, UUID.randomUUID(), catalogCreationScript);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        // delete the ADLA account first
        try {
            dataLakeAnalyticsAccountManagementClient.accounts().delete(rgName, adlaAcct);
            resourceManagementClient.resourceGroups().delete(rgName);
        }
        catch (Exception e) {
            // ignore failures during cleanup, as it is best effort
        }
    }
    @Test
    public void canGetCatalogItems() throws Exception {
        List<USqlDatabase> dbListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listDatabases(adlaAcct).getBody();
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
        USqlDatabase dbGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getDatabase(adlaAcct, dbName).getBody();

        Assert.assertEquals(dbName, dbGetResponse.name());

        // Get the table list
        List<USqlTable> tableListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listTables(adlaAcct, dbName, "dbo").getBody();

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
                adlaAcct, dbName, "dbo", tableName).getBody();

        Assert.assertEquals(tableName, tableGetResponse.name());

        // Get the TVF list
        List<USqlTableValuedFunction> tvfListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listTableValuedFunctions(adlaAcct, dbName, "dbo").getBody();

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
                adlaAcct, dbName, "dbo", tvfName).getBody();

        Assert.assertEquals(tvfName, tvfGetResponse.name());

        // Get the View list
        List<USqlView> viewListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listViews(adlaAcct, dbName, "dbo").getBody();

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
                adlaAcct, dbName, "dbo", viewName).getBody();

        Assert.assertEquals(viewName, viewGetResponse.name());

        // Get the Procedure list
        List<USqlProcedure> procListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listProcedures(
                adlaAcct, dbName, "dbo").getBody();

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
                adlaAcct, dbName, "dbo", procName).getBody();

        Assert.assertEquals(procName, procGetResponse.name());

        // Get all the types
        List<USqlType> typeGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listTypes(
                adlaAcct, dbName, "dbo").getBody();


        Assert.assertNotNull(typeGetResponse);
        Assert.assertTrue(typeGetResponse.size() > 0);

        // Get all the types that are not complex
        typeGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listTypes(
                adlaAcct, dbName, "dbo", "isComplexType=false", null, null, null, null, null, null).getBody();


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
        USqlSecret secretCreateResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().createSecret(
                adlaAcct, dbName, secretName,
                createParams).getBody();
        
        // Attempt to create the secret again, which should throw
        try {
            USqlSecret secondTry = dataLakeAnalyticsCatalogManagementClient.catalogs().createSecret(
                adlaAcct,
                dbName, secretName,
                createParams).getBody();
            // should never make it here
            Assert.assertTrue(false);
        }
        catch(Exception e) {
            // expected.
        }

        // Get the secret and ensure the response contains a date.
        USqlSecret secretGetResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().getSecret(
                adlaAcct, dbName, secretName).getBody();

        Assert.assertNotNull(secretGetResponse);
        Assert.assertNotNull(secretGetResponse.creationTime());

        // Create a credential with the secret
        String credentialCreationScript =
                String.format("USE %s; CREATE CREDENTIAL %s WITH USER_NAME = \"scope@rkm4grspxa\", IDENTITY = \"%s\";",
                        dbName, credentialName, secretName);

        runJobToCompletion(dataLakeAnalyticsJobManagementClient, adlaAcct, UUID.randomUUID(), credentialCreationScript);

        // Get the Credential list
        List<USqlCredential> credListResponse = dataLakeAnalyticsCatalogManagementClient.catalogs().listCredentials(
                adlaAcct, dbName).getBody();
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
                adlaAcct, dbName, credentialName).getBody();
        Assert.assertEquals(credentialName, credGetResponse.name());

        // Drop the credential (to enable secret deletion)
        String credentialDropScript =
                String.format("USE %s; DROP CREDENTIAL %s;", dbName, credentialName);
        runJobToCompletion(dataLakeAnalyticsJobManagementClient,
                adlaAcct, UUID.randomUUID(),
                credentialDropScript);

        // Delete the secret
        dataLakeAnalyticsCatalogManagementClient.catalogs().deleteSecret(
                adlaAcct, dbName, secretName);

        // Try to get the secret which should throw
        try {
            dataLakeAnalyticsCatalogManagementClient.catalogs().getSecret(
                    adlaAcct, dbName, secretName);

            // should never make it here
            Assert.assertTrue("Was able to retrieve a deleted secret", false);
        }
        catch (Exception e) {
            // expected
        }

    }
}
