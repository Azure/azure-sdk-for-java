package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.models.*;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.resources.implementation.ResourceGroupInner;
import com.microsoft.azure.management.storage.Kind;
import com.microsoft.azure.management.storage.Sku;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.implementation.StorageAccountCreateParametersInner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataLakeAnalyticsAccountOperationsTests extends DataLakeAnalyticsManagementTestBase {
    private static String rgName = generateName("javaadlarg");
    private static String location;
    private static String storageAcct = generateName("javasto");
    private static String adlsAcct = generateName("javaadlsacct");
    private static String adlsAcct2 = generateName("javaadlsacct2");
    private static String adlaAcct = generateName("javaadlaacct");
    private static String storageAccessKey = "";

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
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct2, adlsAccount);

        StorageAccountCreateParametersInner createParams = new StorageAccountCreateParametersInner();
        createParams.withLocation(location);
        createParams.withSku(new Sku().withName(SkuName.STANDARD_LRS));
        createParams.withKind(Kind.STORAGE);
        storageManagementClient.storageAccounts().create(rgName, storageAcct, createParams);
        storageAccessKey = storageManagementClient.storageAccounts().listKeys(rgName, storageAcct).keys().get(0).value();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            dataLakeAnalyticsAccountManagementClient.accounts().delete(rgName, adlaAcct);
            resourceManagementClient.resourceGroups().delete(rgName);
        }
        catch (Exception e) {
            // ignore failures during cleanup, as it is best effort
        }
    }
    @Test
    public void canCreateGetUpdateDeleteAdlaAccount() throws Exception {
        // Create
        List<DataLakeStoreAccountInfo> adlsAccts = new ArrayList<DataLakeStoreAccountInfo>();
        DataLakeStoreAccountInfo adlsInfo = new DataLakeStoreAccountInfo();
        adlsInfo.withName(adlsAcct);
        adlsAccts.add(adlsInfo);

        DataLakeAnalyticsAccount createParams = new DataLakeAnalyticsAccount();
        createParams.withLocation(location);
        createParams.withDataLakeStoreAccounts(adlsAccts);
        createParams.withDefaultDataLakeStoreAccount(adlsAcct);
        HashMap<String, String> tags = new HashMap<String, String>();
        tags.put("testkey", "testvalue");
        createParams.withTags(tags);

        DataLakeAnalyticsAccount createResponse = dataLakeAnalyticsAccountManagementClient.accounts().create(rgName, adlaAcct, createParams);
        Assert.assertEquals(location, createResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", createResponse.type());
        Assert.assertNotNull(createResponse.id());
        Assert.assertTrue(createResponse.id().contains(adlaAcct));
        Assert.assertEquals(1, createResponse.getTags().size());
        Assert.assertEquals(1, createResponse.dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, createResponse.dataLakeStoreAccounts().get(0).name());

        // update the tags
        DataLakeAnalyticsAccountUpdateParameters updateParams = new DataLakeAnalyticsAccountUpdateParameters();
        createParams.getTags().put("testkey2", "testvalue2");
        updateParams.withTags(createParams.getTags());
        DataLakeAnalyticsAccount updateResponse = dataLakeAnalyticsAccountManagementClient.accounts().update(rgName, adlaAcct, updateParams);
        Assert.assertEquals(location, updateResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", updateResponse.type());
        Assert.assertNotNull(updateResponse.id());
        Assert.assertTrue(updateResponse.id().contains(adlaAcct));
        Assert.assertEquals(2, updateResponse.getTags().size());
        Assert.assertEquals(1, updateResponse.dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, updateResponse.dataLakeStoreAccounts().get(0).name());

        // get the account
        DataLakeAnalyticsAccount getResponse = dataLakeAnalyticsAccountManagementClient.accounts().get(rgName, adlaAcct);
        Assert.assertEquals(location, getResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", getResponse.type());
        Assert.assertNotNull(getResponse.id());
        Assert.assertTrue(getResponse.id().contains(adlaAcct));
        Assert.assertEquals(2, getResponse.getTags().size());
        Assert.assertEquals(1, getResponse.dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, getResponse.dataLakeStoreAccounts().get(0).name());

        // list all accounts and make sure there is one.
        List<DataLakeAnalyticsAccount> listResult = dataLakeAnalyticsAccountManagementClient.accounts().list();
        DataLakeAnalyticsAccount discoveredAcct = null;
        for (DataLakeAnalyticsAccount acct : listResult) {
            if (acct.name().equals(adlaAcct)) {
                discoveredAcct = acct;
                break;
            }
        }

        Assert.assertNotNull(discoveredAcct);
        Assert.assertEquals(location, discoveredAcct.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", discoveredAcct.type());
        Assert.assertNotNull(discoveredAcct.id());
        Assert.assertTrue(discoveredAcct.id().contains(adlaAcct));
        Assert.assertEquals(2, discoveredAcct.getTags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.dataLakeStoreAccounts());

        // list within a resource group
        listResult = dataLakeAnalyticsAccountManagementClient.accounts().listByResourceGroup(rgName);
        discoveredAcct = null;
        for (DataLakeAnalyticsAccount acct : listResult) {
            if (acct.name().equals(adlaAcct)) {
                discoveredAcct = acct;
                break;
            }
        }

        Assert.assertNotNull(discoveredAcct);
        Assert.assertEquals(location, discoveredAcct.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", discoveredAcct.type());
        Assert.assertNotNull(discoveredAcct.id());
        Assert.assertTrue(discoveredAcct.id().contains(adlaAcct));
        Assert.assertEquals(2, discoveredAcct.getTags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.dataLakeStoreAccounts());

        // Add, list, get and remove a data lake store account
        AddDataLakeStoreParameters addAdlsParams = new AddDataLakeStoreParameters();

        // This needs to be set and empty for now due to the front end expecting a valid json body
        dataLakeAnalyticsAccountManagementClient.dataLakeStoreAccounts().add(rgName, adlaAcct, adlsAcct2, addAdlsParams);

        // list ADLS accounts
        List<DataLakeStoreAccountInfo> adlsListResult = dataLakeAnalyticsAccountManagementClient.dataLakeStoreAccounts().listByAccount(rgName, adlaAcct);
        Assert.assertEquals(2, adlsListResult.size());

        // get the one we just added
        DataLakeStoreAccountInfo adlsGetResult = dataLakeAnalyticsAccountManagementClient.dataLakeStoreAccounts().get(rgName, adlaAcct, adlsAcct2);
        Assert.assertEquals(adlsAcct2, adlsGetResult.name());

        // Remove the data source
        dataLakeAnalyticsAccountManagementClient.dataLakeStoreAccounts().delete(rgName, adlaAcct, adlsAcct2);

        // list again, confirming there is only one ADLS account
        adlsListResult = dataLakeAnalyticsAccountManagementClient.dataLakeStoreAccounts().listByAccount(rgName, adlaAcct);
        Assert.assertEquals(1, adlsListResult.size());

        // Add, list get and remove an azure blob account
        AddStorageAccountParameters addStoreParams = new AddStorageAccountParameters();

        addStoreParams.withAccessKey(storageAccessKey);
        dataLakeAnalyticsAccountManagementClient.storageAccounts().add(rgName, adlaAcct, storageAcct, addStoreParams);

        // list ADLS accounts
        List<StorageAccountInfo> storeListResult = dataLakeAnalyticsAccountManagementClient.storageAccounts().listByAccount(rgName, adlaAcct);
        Assert.assertEquals(1, storeListResult.size());

        // get the one we just added
        StorageAccountInfo storageGetResult = dataLakeAnalyticsAccountManagementClient.storageAccounts().get(rgName, adlaAcct, storageAcct);
        Assert.assertEquals(storageAcct, storageGetResult.name());

        // Remove the data source
        dataLakeAnalyticsAccountManagementClient.storageAccounts().delete(rgName, adlaAcct, storageAcct);

        // list again, confirming there is only one ADLS account
        storeListResult = dataLakeAnalyticsAccountManagementClient.storageAccounts().listByAccount(rgName, adlaAcct);
        Assert.assertEquals(0, storeListResult.size());

        // Delete the ADLA account
        dataLakeAnalyticsAccountManagementClient.accounts().delete(rgName, adlaAcct);

        // Do it again, it should not throw
        dataLakeAnalyticsAccountManagementClient.accounts().delete(rgName, adlaAcct);
    }
}
