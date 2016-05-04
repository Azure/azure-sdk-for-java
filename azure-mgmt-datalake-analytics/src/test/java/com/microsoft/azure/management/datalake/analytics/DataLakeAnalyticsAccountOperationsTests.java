package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.implementation.api.AddDataLakeStoreParametersInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.AddStorageAccountParametersInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeAnalyticsAccountInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeAnalyticsAccountProperties;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeStoreAccountInfoInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeStoreAccountInfoProperties;
import com.microsoft.azure.management.datalake.analytics.implementation.api.StorageAccountInfoInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.StorageAccountProperties;
import com.microsoft.azure.management.datalake.store.implementation.api.DataLakeStoreAccountInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountCreateParametersInner;

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
        location =environmentLocation;
        ResourceGroupInner group = new ResourceGroupInner();
        group.setLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccountInner adlsAccount = new DataLakeStoreAccountInner();
        adlsAccount.setLocation(location);
        adlsAccount.setName(adlsAcct);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);
        adlsAccount.setName(adlsAcct2);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct2, adlsAccount);

        StorageAccountCreateParametersInner createParams = new StorageAccountCreateParametersInner();
        createParams.setLocation(location);
        createParams.setAccountType(AccountType.STANDARD_LRS);
        storageManagementClient.storageAccounts().create(rgName, storageAcct, createParams);
        storageAccessKey = storageManagementClient.storageAccounts().listKeys(rgName, storageAcct).getBody().key1();
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
        DataLakeAnalyticsAccountProperties createProperties = new DataLakeAnalyticsAccountProperties();
        List<DataLakeStoreAccountInfoInner> adlsAccts = new ArrayList<DataLakeStoreAccountInfoInner>();
        DataLakeStoreAccountInfoInner adlsInfo = new DataLakeStoreAccountInfoInner();
        adlsInfo.setName(adlsAcct);
        adlsAccts.add(adlsInfo);

        createProperties.setDataLakeStoreAccounts(adlsAccts);
        createProperties.setDefaultDataLakeStoreAccount(adlsAcct);

        DataLakeAnalyticsAccountInner createParams = new DataLakeAnalyticsAccountInner();
        createParams.setLocation(location);
        createParams.setName(adlaAcct);
        createParams.setProperties(createProperties);
        createParams.setTags(new HashMap<String, String>());
        createParams.tags().put("testkey", "testvalue");

        DataLakeAnalyticsAccountInner createResponse = dataLakeAnalyticsAccountManagementClient.accounts().create(rgName, adlaAcct, createParams).getBody();
        Assert.assertEquals(location, createResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", createResponse.type());
        Assert.assertNotNull(createResponse.id());
        Assert.assertTrue(createResponse.id().contains(adlaAcct));
        Assert.assertEquals(1, createResponse.tags().size());
        Assert.assertEquals(1, createResponse.properties().dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, createResponse.properties().dataLakeStoreAccounts().get(0).name());

        // update the tags
        createParams.tags().put("testkey2", "testvalue2");
        createParams.setProperties(null);
        DataLakeAnalyticsAccountInner updateResponse = dataLakeAnalyticsAccountManagementClient.accounts().update(rgName, adlaAcct, createParams).getBody();
        Assert.assertEquals(location, updateResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", updateResponse.type());
        Assert.assertNotNull(updateResponse.id());
        Assert.assertTrue(updateResponse.id().contains(adlaAcct));
        Assert.assertEquals(2, updateResponse.tags().size());
        Assert.assertEquals(1, updateResponse.properties().dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, updateResponse.properties().dataLakeStoreAccounts().get(0).name());

        // get the account
        DataLakeAnalyticsAccountInner getResponse = dataLakeAnalyticsAccountManagementClient.accounts().get(rgName, adlaAcct).getBody();
        Assert.assertEquals(location, getResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", getResponse.type());
        Assert.assertNotNull(getResponse.id());
        Assert.assertTrue(getResponse.id().contains(adlaAcct));
        Assert.assertEquals(2, getResponse.tags().size());
        Assert.assertEquals(1, getResponse.properties().dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, getResponse.properties().dataLakeStoreAccounts().get(0).name());

        // list all accounts and make sure there is one.
        List<DataLakeAnalyticsAccountInner> listResult = dataLakeAnalyticsAccountManagementClient.accounts().list().getBody();
        DataLakeAnalyticsAccountInner discoveredAcct = null;
        for (DataLakeAnalyticsAccountInner acct : listResult) {
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
        Assert.assertEquals(2, discoveredAcct.tags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.properties().dataLakeStoreAccounts());

        // list within a resource group
        listResult = dataLakeAnalyticsAccountManagementClient.accounts().listByResourceGroup(rgName).getBody();
        discoveredAcct = null;
        for (DataLakeAnalyticsAccountInner acct : listResult) {
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
        Assert.assertEquals(2, discoveredAcct.tags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.properties().dataLakeStoreAccounts());

        // Add, list, get and remove a data lake store account
        AddDataLakeStoreParametersInner addAdlsParams = new AddDataLakeStoreParametersInner();

        // This needs to be set and empty for now due to the front end expecting a valid json body
        addAdlsParams.setProperties(new DataLakeStoreAccountInfoProperties());
        dataLakeAnalyticsAccountManagementClient.accounts().addDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2, addAdlsParams);

        // list ADLS accounts
        List<DataLakeStoreAccountInfoInner> adlsListResult = dataLakeAnalyticsAccountManagementClient.accounts().listDataLakeStoreAccounts(rgName, adlaAcct).getBody();
        Assert.assertEquals(2, adlsListResult.size());

        // get the one we just added
        DataLakeStoreAccountInfoInner adlsGetResult = dataLakeAnalyticsAccountManagementClient.accounts().getDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2).getBody();
        Assert.assertEquals(adlsAcct2, adlsGetResult.name());

        // Remove the data source
        dataLakeAnalyticsAccountManagementClient.accounts().deleteDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2);

        // list again, confirming there is only one ADLS account
        adlsListResult = dataLakeAnalyticsAccountManagementClient.accounts().listDataLakeStoreAccounts(rgName, adlaAcct).getBody();
        Assert.assertEquals(1, adlsListResult.size());

        // Add, list get and remove an azure blob account
        AddStorageAccountParametersInner addStoreParams = new AddStorageAccountParametersInner();

        StorageAccountProperties storageAccountProperties = new StorageAccountProperties();
        storageAccountProperties.setAccessKey(storageAccessKey);
        addStoreParams.setProperties(storageAccountProperties);
        dataLakeAnalyticsAccountManagementClient.accounts().addStorageAccount(rgName, adlaAcct, storageAcct, addStoreParams);

        // list ADLS accounts
        List<StorageAccountInfoInner> storeListResult = dataLakeAnalyticsAccountManagementClient.accounts().listStorageAccounts(rgName, adlaAcct).getBody();
        Assert.assertEquals(1, storeListResult.size());

        // get the one we just added
        StorageAccountInfoInner storageGetResult = dataLakeAnalyticsAccountManagementClient.accounts().getStorageAccount(rgName, adlaAcct, storageAcct).getBody();
        Assert.assertEquals(storageAcct, storageGetResult.name());

        // Remove the data source
        dataLakeAnalyticsAccountManagementClient.accounts().deleteStorageAccount(rgName, adlaAcct, storageAcct);

        // list again, confirming there is only one ADLS account
        storeListResult = dataLakeAnalyticsAccountManagementClient.accounts().listStorageAccounts(rgName, adlaAcct).getBody();
        Assert.assertEquals(0, storeListResult.size());

        // Delete the ADLA account
        dataLakeAnalyticsAccountManagementClient.accounts().delete(rgName, adlaAcct);

        // Do it again, it should not throw
        dataLakeAnalyticsAccountManagementClient.accounts().delete(rgName, adlaAcct);
    }
}
