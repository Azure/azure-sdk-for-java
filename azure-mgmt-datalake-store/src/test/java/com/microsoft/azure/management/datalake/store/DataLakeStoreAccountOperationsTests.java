package com.microsoft.azure.management.datalake.store;

import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccountUpdateParameters;
import com.microsoft.azure.management.resources.implementation.ResourceGroupInner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class DataLakeStoreAccountOperationsTests extends DataLakeStoreManagementTestBase {
    private static String rgName = generateName("javaadlsrg");
    private static String location = "eastus2";
    private static String adlsAcct = generateName("javaadlsacct");

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        ResourceGroupInner group = new ResourceGroupInner();
        group.withLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            resourceManagementClient.resourceGroups().delete(rgName);
        }
        catch (Exception e) {
            // ignore failures during cleanup, as it is best effort
        }
    }
    @Test
    public void canCreateGetUpdateDeleteAdlsAccount() throws Exception {
        // Create
        DataLakeStoreAccount createParams = new DataLakeStoreAccount();
        createParams.withLocation(location);
        createParams.withTags(new HashMap<String, String>());
        createParams.getTags().put("testkey", "testvalue");

        DataLakeStoreAccount createResponse = dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, createParams);
        Assert.assertEquals(location, createResponse.location());
        Assert.assertEquals("Microsoft.DataLakeStore/accounts", createResponse.type());
        Assert.assertNotNull(createResponse.id());
        Assert.assertTrue(createResponse.id().contains(adlsAcct));
        Assert.assertEquals(1, createResponse.getTags().size());

        // update the tags
        createParams.getTags().put("testkey2", "testvalue2");
        DataLakeStoreAccountUpdateParameters updateParams = new DataLakeStoreAccountUpdateParameters();
        updateParams.withTags((createParams.getTags()));
        DataLakeStoreAccount updateResponse = dataLakeStoreAccountManagementClient.accounts().update(rgName, adlsAcct, updateParams);
        Assert.assertEquals(location, updateResponse.location());
        Assert.assertEquals("Microsoft.DataLakeStore/accounts", updateResponse.type());
        Assert.assertNotNull(updateResponse.id());
        Assert.assertTrue(updateResponse.id().contains(adlsAcct));
        Assert.assertEquals(2, updateResponse.getTags().size());

        // get the account
        DataLakeStoreAccount getResponse = dataLakeStoreAccountManagementClient.accounts().get(rgName, adlsAcct);
        Assert.assertEquals(location, getResponse.location());
        Assert.assertEquals("Microsoft.DataLakeStore/accounts", getResponse.type());
        Assert.assertNotNull(getResponse.id());
        Assert.assertTrue(getResponse.id().contains(adlsAcct));
        Assert.assertEquals(2, getResponse.getTags().size());

        // list all accounts and make sure there is one.
        List<DataLakeStoreAccount> listResult = dataLakeStoreAccountManagementClient.accounts().list();
        DataLakeStoreAccount discoveredAcct = null;
        for (DataLakeStoreAccount acct : listResult) {
            if (acct.name().equals(adlsAcct)) {
                discoveredAcct = acct;
                break;
            }
        }

        Assert.assertNotNull(discoveredAcct);
        Assert.assertEquals(location, discoveredAcct.location());
        Assert.assertEquals("Microsoft.DataLakeStore/accounts", discoveredAcct.type());
        Assert.assertNotNull(discoveredAcct.id());
        Assert.assertTrue(discoveredAcct.id().contains(adlsAcct));
        Assert.assertEquals(2, discoveredAcct.getTags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.defaultGroup());

        // list within a resource group
        listResult = dataLakeStoreAccountManagementClient.accounts().listByResourceGroup(rgName);
        discoveredAcct = null;
        for (DataLakeStoreAccount acct : listResult) {
            if (acct.name().equals(adlsAcct)) {
                discoveredAcct = acct;
                break;
            }
        }

        Assert.assertNotNull(discoveredAcct);
        Assert.assertEquals(location, discoveredAcct.location());
        Assert.assertEquals("Microsoft.DataLakeStore/accounts", discoveredAcct.type());
        Assert.assertNotNull(discoveredAcct.id());
        Assert.assertTrue(discoveredAcct.id().contains(adlsAcct));
        Assert.assertEquals(2, discoveredAcct.getTags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.defaultGroup());

        // Delete the ADLS account
        dataLakeStoreAccountManagementClient.accounts().delete(rgName, adlsAcct);

        // Do it again, it should not throw
        dataLakeStoreAccountManagementClient.accounts().delete(rgName, adlsAcct);
    }
}
