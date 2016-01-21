package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.CheckNameAvailabilityResult;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.StorageAccountCheckNameAvailabilityParameters;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.azure.management.storage.models.StorageAccountListResult;
import com.microsoft.azure.Page;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

public class StorageAccountOperationsTests extends StorageManagementTestBase {
    private static String rgName = "javacsmrg";
    private static String location = "southcentralus";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        ResourceGroup group = new ResourceGroup();
        group.setLocation(location);
        resourceManagementClient.getResourceGroups().createOrUpdate(rgName, group);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManagementClient.getResourceGroups().delete(rgName);
    }

    @Test
    public void canCreateStorageAccount() throws Exception {
        // Create
        String accountName = "javasto";
        StorageAccountCreateParameters parameters = new StorageAccountCreateParameters();
        parameters.setLocation(location);
        parameters.setAccountType(AccountType.STANDARD_GRS);
        parameters.setTags(new HashMap<String, String>());
        parameters.getTags().put("department", "finance");
        parameters.getTags().put("tagname", "tagvalue");
        StorageAccount storageAccount = storageManagementClient.getStorageAccounts().create(rgName, accountName, parameters).getBody();
        Assert.assertEquals(location, storageAccount.getLocation());
        Assert.assertEquals(AccountType.STANDARD_GRS, storageAccount.getAccountType());
        Assert.assertEquals(2, storageAccount.getTags().size());
        // List
        StorageAccountListResult listResult = storageManagementClient.getStorageAccounts().list().getBody();
        StorageAccount storageResult = null;
        for (StorageAccount sa : listResult.getValue()) {
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
        StorageAccount getResult = storageManagementClient.getStorageAccounts().getProperties(rgName, accountName).getBody();
        Assert.assertNotNull(getResult);
        Assert.assertEquals("finance", getResult.getTags().get("department"));
        Assert.assertEquals("tagvalue", getResult.getTags().get("tagname"));
        Assert.assertEquals(location, getResult.getLocation());
        // Delete
        storageManagementClient.getStorageAccounts().delete(rgName, accountName);
        StorageAccountCheckNameAvailabilityParameters availabilityParameters = new StorageAccountCheckNameAvailabilityParameters();
        availabilityParameters.setName(accountName);
        availabilityParameters.setType("Microsoft.Storage/storageAccounts");
        CheckNameAvailabilityResult availabilityResult = storageManagementClient.getStorageAccounts().checkNameAvailability(availabilityParameters).getBody();
        Assert.assertTrue(availabilityResult.getNameAvailable());
    }
}
