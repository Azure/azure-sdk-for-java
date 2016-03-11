package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.models.AddDataLakeStoreParameters;
import com.microsoft.azure.management.datalake.analytics.models.AddStorageAccountParameters;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsAccount;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsAccountProperties;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeStoreAccountInfo;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeStoreAccountInfoProperties;
import com.microsoft.azure.management.datalake.analytics.models.StorageAccountInfo;
import com.microsoft.azure.management.datalake.analytics.models.StorageAccountProperties;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataLakeAnalyticsAccountOperationsTests extends DataLakeAnalyticsManagementTestBase {
    private static String rgName = generateName("javaadlarg");
    private static String location = "eastus2";
    private static String storageAcct = generateName("javasto");
    private static String adlsAcct = generateName("javaadlsacct");
    private static String adlsAcct2 = generateName("javaadlsacct2");
    private static String adlaAcct = generateName("javaadlaacct");
    private static String storageAccessKey = "";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        ResourceGroup group = new ResourceGroup();
        group.setLocation(location);
        resourceManagementClient.getResourceGroupsOperations().createOrUpdate(rgName, group);
        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccount adlsAccount = new DataLakeStoreAccount();
        adlsAccount.setLocation(location);
        adlsAccount.setName(adlsAcct);
        dataLakeStoreAccountManagementClient.getAccountOperations().create(rgName, adlsAcct, adlsAccount);
        adlsAccount.setName(adlsAcct2);
        dataLakeStoreAccountManagementClient.getAccountOperations().create(rgName, adlsAcct2, adlsAccount);

        StorageAccountCreateParameters createParams = new StorageAccountCreateParameters();
        createParams.setLocation(location);
        createParams.setAccountType(AccountType.STANDARD_LRS);
        storageManagementClient.getStorageAccountsOperations().create(rgName, storageAcct, createParams);
        storageAccessKey = storageManagementClient.getStorageAccountsOperations().listKeys(rgName, storageAcct).getBody().getKey1();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        // delete the ADLA account first
        dataLakeAnalyticsAccountManagementClient.getAccountOperations().delete(rgName, adlaAcct);
        resourceManagementClient.getResourceGroupsOperations().delete(rgName);
    }
    @Test
    public void canCreateGetUpdateDeleteAdlaAccount() throws Exception {
        // Create
        DataLakeAnalyticsAccountProperties createProperties = new DataLakeAnalyticsAccountProperties();
        List<DataLakeStoreAccountInfo> adlsAccts = new ArrayList<DataLakeStoreAccountInfo>();
        DataLakeStoreAccountInfo adlsInfo = new DataLakeStoreAccountInfo();
        adlsInfo.setName(adlsAcct);
        adlsAccts.add(adlsInfo);

        createProperties.setDataLakeStoreAccounts(adlsAccts);
        createProperties.setDefaultDataLakeStoreAccount(adlsAcct);

        DataLakeAnalyticsAccount createParams = new DataLakeAnalyticsAccount();
        createParams.setLocation(location);
        createParams.setName(adlaAcct);
        createParams.setProperties(createProperties);
        createParams.setTags(new HashMap<String, String>());
        createParams.getTags().put("testkey", "testvalue");

        DataLakeAnalyticsAccount createResponse = dataLakeAnalyticsAccountManagementClient.getAccountOperations().create(rgName, adlaAcct, createParams).getBody();
        Assert.assertEquals(location, createResponse.getLocation());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", createResponse.getType());
        Assert.assertNotNull(createResponse.getId());
        Assert.assertTrue(createResponse.getId().contains(adlaAcct));
        Assert.assertEquals(1, createResponse.getTags().size());
        Assert.assertEquals(1, createResponse.getProperties().getDataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, createResponse.getProperties().getDataLakeStoreAccounts().get(0).getName());

        // update the tags
        createParams.getTags().put("testkey2", "testvalue2");
        createParams.setProperties(null);
        DataLakeAnalyticsAccount updateResponse = dataLakeAnalyticsAccountManagementClient.getAccountOperations().update(rgName, adlaAcct, createParams).getBody();
        Assert.assertEquals(location, updateResponse.getLocation());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", updateResponse.getType());
        Assert.assertNotNull(updateResponse.getId());
        Assert.assertTrue(updateResponse.getId().contains(adlaAcct));
        Assert.assertEquals(2, updateResponse.getTags().size());
        Assert.assertEquals(1, updateResponse.getProperties().getDataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, updateResponse.getProperties().getDataLakeStoreAccounts().get(0).getName());

        // get the account
        DataLakeAnalyticsAccount getResponse = dataLakeAnalyticsAccountManagementClient.getAccountOperations().get(rgName, adlaAcct).getBody();
        Assert.assertEquals(location, getResponse.getLocation());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", getResponse.getType());
        Assert.assertNotNull(getResponse.getId());
        Assert.assertTrue(getResponse.getId().contains(adlaAcct));
        Assert.assertEquals(2, getResponse.getTags().size());
        Assert.assertEquals(1, getResponse.getProperties().getDataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, getResponse.getProperties().getDataLakeStoreAccounts().get(0).getName());

        // list all accounts and make sure there is one.
        List<DataLakeAnalyticsAccount> listResult = dataLakeAnalyticsAccountManagementClient.getAccountOperations().list(null, null, null, null, null, null, null, null, null).getBody();
        DataLakeAnalyticsAccount discoveredAcct = null;
        for (DataLakeAnalyticsAccount acct : listResult) {
            if (acct.getName().equals(adlaAcct)) {
                discoveredAcct = acct;
                break;
            }
        }

        Assert.assertNotNull(discoveredAcct);
        Assert.assertEquals(location, discoveredAcct.getLocation());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", discoveredAcct.getType());
        Assert.assertNotNull(discoveredAcct.getId());
        Assert.assertTrue(discoveredAcct.getId().contains(adlaAcct));
        Assert.assertEquals(2, discoveredAcct.getTags().size());
        Assert.assertEquals(1, discoveredAcct.getProperties().getDataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, discoveredAcct.getProperties().getDataLakeStoreAccounts().get(0).getName());

        // list within a resource group
        listResult = dataLakeAnalyticsAccountManagementClient.getAccountOperations().listByResourceGroup(rgName, null, null, null, null, null, null, null, null, null).getBody();
        discoveredAcct = null;
        for (DataLakeAnalyticsAccount acct : listResult) {
            if (acct.getName().equals(adlaAcct)) {
                discoveredAcct = acct;
                break;
            }
        }

        Assert.assertNotNull(discoveredAcct);
        Assert.assertEquals(location, discoveredAcct.getLocation());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", discoveredAcct.getType());
        Assert.assertNotNull(discoveredAcct.getId());
        Assert.assertTrue(discoveredAcct.getId().contains(adlaAcct));
        Assert.assertEquals(2, discoveredAcct.getTags().size());
        Assert.assertEquals(1, discoveredAcct.getProperties().getDataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, discoveredAcct.getProperties().getDataLakeStoreAccounts().get(0).getName());

        // Add, list, get and remove a data lake store account
        AddDataLakeStoreParameters addAdlsParams = new AddDataLakeStoreParameters();

        // This needs to be set and empty for now due to the front end expecting a valid json body
        addAdlsParams.setProperties(new DataLakeStoreAccountInfoProperties());
        dataLakeAnalyticsAccountManagementClient.getAccountOperations().addDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2, addAdlsParams);

        // list ADLS accounts
        List<DataLakeStoreAccountInfo> adlsListResult = dataLakeAnalyticsAccountManagementClient.getAccountOperations().listDataLakeStoreAccounts(rgName, adlaAcct, null, null, null, null, null, null, null, null, null).getBody();
        Assert.assertEquals(2, adlsListResult.size());

        // get the one we just added
        DataLakeStoreAccountInfo adlsGetResult = dataLakeAnalyticsAccountManagementClient.getAccountOperations().getDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2).getBody();
        Assert.assertEquals(adlsAcct2, adlsGetResult.getName());

        // Remove the data source
        dataLakeAnalyticsAccountManagementClient.getAccountOperations().deleteDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2);

        // list again, confirming there is only one ADLS account
        adlsListResult = dataLakeAnalyticsAccountManagementClient.getAccountOperations().listDataLakeStoreAccounts(rgName, adlaAcct, null, null, null, null, null, null, null, null, null).getBody();
        Assert.assertEquals(1, adlsListResult.size());

        // Add, list get and remove an azure blob account
        AddStorageAccountParameters addStoreParams = new AddStorageAccountParameters();

        StorageAccountProperties storageAccountProperties = new StorageAccountProperties();
        storageAccountProperties.setAccessKey(storageAccessKey);
        addStoreParams.setProperties(storageAccountProperties);
        dataLakeAnalyticsAccountManagementClient.getAccountOperations().addStorageAccount(rgName, adlaAcct, storageAcct, addStoreParams);

        // list ADLS accounts
        List<StorageAccountInfo> storeListResult = dataLakeAnalyticsAccountManagementClient.getAccountOperations().listStorageAccounts(rgName, adlaAcct, null, null, null, null, null, null, null, null, null).getBody();
        Assert.assertEquals(1, storeListResult.size());

        // get the one we just added
        StorageAccountInfo storageGetResult = dataLakeAnalyticsAccountManagementClient.getAccountOperations().getStorageAccount(rgName, adlaAcct, storageAcct).getBody();
        Assert.assertEquals(storageAcct, storageGetResult.getName());

        // Remove the data source
        dataLakeAnalyticsAccountManagementClient.getAccountOperations().deleteStorageAccount(rgName, adlaAcct, storageAcct);

        // list again, confirming there is only one ADLS account
        storeListResult = dataLakeAnalyticsAccountManagementClient.getAccountOperations().listStorageAccounts(rgName, adlaAcct, null, null, null, null, null, null, null, null, null).getBody();
        Assert.assertEquals(0, storeListResult.size());

        // Delete the ADLA account
        dataLakeAnalyticsAccountManagementClient.getAccountOperations().delete(rgName, adlaAcct);

        // Do it again, it should not throw
        dataLakeAnalyticsAccountManagementClient.getAccountOperations().delete(rgName, adlaAcct);
    }
}
