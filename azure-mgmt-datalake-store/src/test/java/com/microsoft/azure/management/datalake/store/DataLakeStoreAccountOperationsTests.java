package com.microsoft.azure.management.datalake.store;

import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccountProperties;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
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
        DataLakeStoreAccountProperties createProperties = new DataLakeStoreAccountProperties();

        DataLakeStoreAccount createParams = new DataLakeStoreAccount();
        createParams.withLocation(location);
        createParams.withName(adlsAcct);
        createParams.withProperties(createProperties);
        createParams.withTags(new HashMap<String, String>());
        createParams.tags().put("testkey", "testvalue");

        DataLakeStoreAccount createResponse = dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, createParams).getBody();
        Assert.assertEquals(location, createResponse.location());
        Assert.assertEquals("Microsoft.DataLakeStore/accounts", createResponse.type());
        Assert.assertNotNull(createResponse.id());
        Assert.assertTrue(createResponse.id().contains(adlsAcct));
        Assert.assertEquals(1, createResponse.tags().size());

        // update the tags
        createParams.tags().put("testkey2", "testvalue2");
        createParams.withProperties(null);
        DataLakeStoreAccount updateResponse = dataLakeStoreAccountManagementClient.accounts().update(rgName, adlsAcct, createParams).getBody();
        Assert.assertEquals(location, updateResponse.location());
        Assert.assertEquals("Microsoft.DataLakeStore/accounts", updateResponse.type());
        Assert.assertNotNull(updateResponse.id());
        Assert.assertTrue(updateResponse.id().contains(adlsAcct));
        Assert.assertEquals(2, updateResponse.tags().size());

        // get the account
        DataLakeStoreAccount getResponse = dataLakeStoreAccountManagementClient.accounts().get(rgName, adlsAcct).getBody();
        Assert.assertEquals(location, getResponse.location());
        Assert.assertEquals("Microsoft.DataLakeStore/accounts", getResponse.type());
        Assert.assertNotNull(getResponse.id());
        Assert.assertTrue(getResponse.id().contains(adlsAcct));
        Assert.assertEquals(2, getResponse.tags().size());

        // list all accounts and make sure there is one.
        List<DataLakeStoreAccount> listResult = dataLakeStoreAccountManagementClient.accounts().list().getBody();
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
        Assert.assertEquals(2, discoveredAcct.tags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.properties().defaultGroup());

        // list within a resource group
        listResult = dataLakeStoreAccountManagementClient.accounts().listByResourceGroup(rgName).getBody();
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
        Assert.assertEquals(2, discoveredAcct.tags().size());

        // the properties should be empty when we do list calls
        Assert.assertNull(discoveredAcct.properties().defaultGroup());

        // Delete the ADLS account
        dataLakeStoreAccountManagementClient.accounts().delete(rgName, adlsAcct);

        // Do it again, it should not throw
        dataLakeStoreAccountManagementClient.accounts().delete(rgName, adlsAcct);
    }
}
