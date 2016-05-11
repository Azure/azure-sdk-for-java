package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.implementation.StorageResourceConnector;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StorageAccountOperationsTests extends StorageManagementTestBase {
    private static final String RG_NAME = "javacsmrg";
    private static final String RG_NAME_2 = "javacsmrg2";
    private static final String SA_NAME = "javacsmsa6";
    private static ResourceGroup resourceGroup;

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        resourceGroup = resourceManager.resourceGroups().define(RG_NAME)
                .withLocation(Region.US_SOUTH_CENTRAL)
                .provision();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
        resourceManager.resourceGroups().delete(RG_NAME_2);
    }

    @Test
    public void getStorageAccountFromResource() throws Exception {
        StorageAccounts.InGroup storageAccountsInGroup = resourceGroup
                .connectToResource(new StorageResourceConnector.Builder())
                .storageAccounts();

        StorageAccount storageAccount1 = resourceGroup
                .connectToResource(new StorageResourceConnector.Builder())
                .storageAccounts()
                .define(SA_NAME)
                .withAccountType(AccountType.PREMIUM_LRS)
                .provision();

        StorageAccount storageAccount2 = storageManager.storageAccounts()
                .define(SA_NAME + "2")
                .withRegion(Region.ASIA_EAST)
                .withNewGroup(RG_NAME_2)
                .withAccountType(AccountType.PREMIUM_LRS)
                .provision();

        Assert.assertEquals(RG_NAME, storageAccount1.group());
        Assert.assertEquals(RG_NAME_2, storageAccount2.group());
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
