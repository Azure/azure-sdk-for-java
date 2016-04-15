package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.storage.implementation.StorageResourceConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class StorageAccountOperationsTests extends StorageManagementTestBase {
    private static String rgName = "javacsmrg";
    private static String location = "southcentralus";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        resourceClient.resourceGroups().define(rgName)
                .withLocation(location)
                .provision();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceClient.resourceGroups().delete(rgName);
    }

    @Test
    public void getStorageAccountFromResource() throws Exception {
        StorageAccounts storageAccounts = resourceClient.resourceGroups()
                .get("my-rg")
                .resourcesInGroup(new StorageResourceConnector.Builder())
                .storageAccounts();
        Usages usages = resourceClient.resourceGroups()
                .get("my-rg")
                .resourcesInGroup(new StorageResourceConnector.Builder())
                .usages();
    }

    @Test
    public void canCreateStorageAccount() throws Exception {
        // Create
        // TODO use fluent style
        /**
        String accountName = "javasto";
        StorageAccountCreateParameters parameters = new StorageAccountCreateParameters();
        parameters.setLocation(location);
        parameters.setAccountType(AccountType.STANDARD_LRS);
        parameters.setTags(new HashMap<String, String>());
        parameters.getTags().put("department", "finance");
        parameters.getTags().put("tagname", "tagvalue");
        StorageAccount storageAccount = storageManagementClient.storageAccounts().create(rgName, accountName, parameters).getBody();
        Assert.assertEquals(location, storageAccount.getLocation());
        Assert.assertEquals(AccountType.STANDARD_LRS, storageAccount.getAccountType());
        Assert.assertEquals(2, storageAccount.getTags().size());
        // List
        List<StorageAccount> listResult = storageManagementClient.storageAccounts().list().getBody();
        StorageAccount storageResult = null;
        for (StorageAccount sa : listResult) {
            if (sa.getName().equals(accountName)) {
                storageResult = sa;
                break;
            }
        }
        Assert.assertNotNull(storageResult);
        Assert.assertEquals("finance", storageResult.getTags().get("department"));
        Assert.assertEquals("tagvalue", storageResult.getTags().get("tagname"));
        Assert.assertEquals(location, storageResult.getLocation());
        // Get
        StorageAccount getResult = storageManagementClient.storageAccounts().getProperties(rgName, accountName).getBody();
        Assert.assertNotNull(getResult);
        Assert.assertEquals("finance", getResult.getTags().get("department"));
        Assert.assertEquals("tagvalue", getResult.getTags().get("tagname"));
        Assert.assertEquals(location, getResult.getLocation());
        // Delete
        storageManagementClient.storageAccounts().delete(rgName, accountName);
        CheckNameAvailabilityResult availabilityResult = storageManagementClient.storageAccounts().checkNameAvailability(accountName, "Microsoft.Storage/storageAccounts").getBody();
        Assert.assertTrue(availabilityResult.getNameAvailable());
        **/
    }
}
