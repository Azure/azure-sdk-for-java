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
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import com.microsoft.azure.management.storage.implementation.api.Sku;
import com.microsoft.azure.management.storage.implementation.api.SkuName;
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
        location = environmentLocation;
        ResourceGroupInner group = new ResourceGroupInner();
        group.withLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccount adlsAccount = new DataLakeStoreAccount();
        adlsAccount.withLocation(location);
        adlsAccount.withName(adlsAcct);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);
        adlsAccount.withName(adlsAcct2);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct2, adlsAccount);

        StorageAccountCreateParametersInner createParams = new StorageAccountCreateParametersInner();
        createParams.withLocation(location);
        createParams.withSku(new Sku().withName(SkuName.STANDARD_LRS));
        storageManagementClient.storageAccounts().create(rgName, storageAcct, createParams);
        storageAccessKey = storageManagementClient.storageAccounts().listKeys(rgName, storageAcct).getBody().keys().get(0).value();
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
        createParams.withTags(new HashMap<String, String>());
        createParams.tags().put("testkey", "testvalue");

        DataLakeAnalyticsAccount createResponse = dataLakeAnalyticsAccountManagementClient.accounts().create(rgName, adlaAcct, createParams).getBody();
        Assert.assertEquals(location, createResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", createResponse.type());
        Assert.assertNotNull(createResponse.id());
        Assert.assertTrue(createResponse.id().contains(adlaAcct));
        Assert.assertEquals(1, createResponse.tags().size());
        Assert.assertEquals(1, createResponse.properties().dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, createResponse.properties().dataLakeStoreAccounts().get(0).name());

        // update the tags
        createParams.tags().put("testkey2", "testvalue2");
        createParams.withProperties(null);
        DataLakeAnalyticsAccount updateResponse = dataLakeAnalyticsAccountManagementClient.accounts().update(rgName, adlaAcct, createParams).getBody();
        Assert.assertEquals(location, updateResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", updateResponse.type());
        Assert.assertNotNull(updateResponse.id());
        Assert.assertTrue(updateResponse.id().contains(adlaAcct));
        Assert.assertEquals(2, updateResponse.tags().size());
        Assert.assertEquals(1, updateResponse.properties().dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, updateResponse.properties().dataLakeStoreAccounts().get(0).name());

        // get the account
        DataLakeAnalyticsAccount getResponse = dataLakeAnalyticsAccountManagementClient.accounts().get(rgName, adlaAcct).getBody();
        Assert.assertEquals(location, getResponse.location());
        Assert.assertEquals("Microsoft.DataLakeAnalytics/accounts", getResponse.type());
        Assert.assertNotNull(getResponse.id());
        Assert.assertTrue(getResponse.id().contains(adlaAcct));
        Assert.assertEquals(2, getResponse.tags().size());
        Assert.assertEquals(1, getResponse.properties().dataLakeStoreAccounts().size());
        Assert.assertEquals(adlsAcct, getResponse.properties().dataLakeStoreAccounts().get(0).name());

        // list all accounts and make sure there is one.
        List<DataLakeAnalyticsAccount> listResult = dataLakeAnalyticsAccountManagementClient.accounts().list().getBody();
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
        Assert.assertEquals(2, discoveredAcct.tags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.properties().dataLakeStoreAccounts());

        // list within a resource group
        listResult = dataLakeAnalyticsAccountManagementClient.accounts().listByResourceGroup(rgName).getBody();
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
        Assert.assertEquals(2, discoveredAcct.tags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.properties().dataLakeStoreAccounts());

        // Add, list, get and remove a data lake store account
        AddDataLakeStoreParameters addAdlsParams = new AddDataLakeStoreParameters();

        // This needs to be set and empty for now due to the front end expecting a valid json body
        addAdlsParams.withProperties(new DataLakeStoreAccountInfoProperties());
        dataLakeAnalyticsAccountManagementClient.accounts().addDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2, addAdlsParams);

        // list ADLS accounts
        List<DataLakeStoreAccountInfo> adlsListResult = dataLakeAnalyticsAccountManagementClient.accounts().listDataLakeStoreAccounts(rgName, adlaAcct).getBody();
        Assert.assertEquals(2, adlsListResult.size());

        // get the one we just added
        DataLakeStoreAccountInfo adlsGetResult = dataLakeAnalyticsAccountManagementClient.accounts().getDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2).getBody();
        Assert.assertEquals(adlsAcct2, adlsGetResult.name());

        // Remove the data source
        dataLakeAnalyticsAccountManagementClient.accounts().deleteDataLakeStoreAccount(rgName, adlaAcct, adlsAcct2);

        // list again, confirming there is only one ADLS account
        adlsListResult = dataLakeAnalyticsAccountManagementClient.accounts().listDataLakeStoreAccounts(rgName, adlaAcct).getBody();
        Assert.assertEquals(1, adlsListResult.size());

        // Add, list get and remove an azure blob account
        AddStorageAccountParameters addStoreParams = new AddStorageAccountParameters();

        StorageAccountProperties storageAccountProperties = new StorageAccountProperties();
        storageAccountProperties.withAccessKey(storageAccessKey);
        addStoreParams.withProperties(storageAccountProperties);
        dataLakeAnalyticsAccountManagementClient.accounts().addStorageAccount(rgName, adlaAcct, storageAcct, addStoreParams);

        // list ADLS accounts
        List<StorageAccountInfo> storeListResult = dataLakeAnalyticsAccountManagementClient.accounts().listStorageAccounts(rgName, adlaAcct).getBody();
        Assert.assertEquals(1, storeListResult.size());

        // get the one we just added
        StorageAccountInfo storageGetResult = dataLakeAnalyticsAccountManagementClient.accounts().getStorageAccount(rgName, adlaAcct, storageAcct).getBody();
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
