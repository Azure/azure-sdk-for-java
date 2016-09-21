/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccounts;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import org.junit.Assert;
import rx.functions.Action1;

public class TestBatch extends TestTemplate<BatchAccount, BatchAccounts>  {
    @Override
    public BatchAccount createResource(BatchAccounts resources) throws Exception {
        final String batchAccountName = "batch" + this.testId;
        final BatchAccount[] batchAccounts = new BatchAccount[1];
        final SettableFuture<BatchAccount> future = SettableFuture.create();
        String storageAccountName = "batchsa" + this.testId;
        resources.define(batchAccountName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withNewStorageAccount(storageAccountName)
                .withTag("mytag", "testtag")
                .createAsync()
                .subscribe(new Action1<BatchAccount>() {
                    @Override
                    public void call(BatchAccount batchAccount) {
                        future.set(batchAccount);
                    }
                });

        batchAccounts[0] = future.get();

        Assert.assertEquals(ResourceUtils.nameFromResourceId(batchAccounts[0].autoStorage().storageAccountId()), storageAccountName);

        return batchAccounts[0];
    }

    @Override
    public BatchAccount updateResource(BatchAccount resource) throws Exception {
        resource = resource.update()
                .withoutStorageAccount()
                .apply();

        Assert.assertNull(resource.autoStorage());

        return resource;
    }

    @Override
    public void print(BatchAccount resource) {
        System.out.println(new StringBuilder().append("BatchAccount account: ").append(resource.id()).append(", Name: ").append(resource.name()).toString());
    }
}
