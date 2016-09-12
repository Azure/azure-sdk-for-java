package com.microsoft.azure;

import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import org.junit.Assert;

public class TestBatchUpdateWithNewStorageAccount extends TestBatch {

    @Override
    public BatchAccount updateResource(BatchAccount resource) throws Exception {
        String storageAccountName = "batch2sa" + this.testId;
        resource = resource.update()
                .withNewStorageAccount(storageAccountName)
                .apply();

        Assert.assertEquals(ResourceUtils.nameFromResourceId(resource.autoStorage().storageAccountId()), storageAccountName);

        return resource;
    }
}
