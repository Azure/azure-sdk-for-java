package com.microsoft.azure.management.storage.v2017_10_01;

import com.microsoft.azure.arm.resources.Region;
import com.microsoft.azure.arm.utils.SdkContext;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.storage.v2017_10_01.implementation.SkuInner;
import org.junit.Assert;
import org.junit.Test;

public class StorageAccountTests extends StorageTestBase {
    String rgName;

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCRUDStorageAccount() {
        rgName = SdkContext.randomResourceName("rg", 20);;
        String saName = SdkContext.randomResourceName("sa", 20);

        ResourceGroup group = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(Region.US_WEST.toString())
                .create();

        // Create
        StorageAccount account = storageManager.storageAccounts()
                .define(saName)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(group.name())
                .withKind(Kind.STORAGE)
                .withSku(new SkuInner().withName(SkuName.STANDARD_LRS))
                .create();

        Assert.assertNotNull(account);
        Assert.assertEquals(SkuName.STANDARD_LRS, account.sku().name());

        // Update
        account = account.update()
                .withSku(new SkuInner().withName(SkuName.STANDARD_GRS))
                .apply();

        Assert.assertNotNull(account);
        Assert.assertEquals(SkuName.STANDARD_GRS, account.sku().name());

        // List
        Assert.assertEquals(1, storageManager.storageAccounts().listByResourceGroup(group.name()).size());

        // Delete
        storageManager.storageAccounts().deleteByIds(account.id());
        Assert.assertEquals(0, storageManager.storageAccounts().listByResourceGroup(group.name()).size());
    }
}
