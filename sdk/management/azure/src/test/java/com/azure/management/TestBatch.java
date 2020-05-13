// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//
// package com.azure.management;
//
// import com.google.common.util.concurrent.SettableFuture;
// import com.microsoft.azure.management.batch.BatchAccount;
// import com.microsoft.azure.management.batch.BatchAccounts;
// import com.microsoft.azure.management.resources.fluentcore.arm.Region;
// import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
// import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
// import org.junit.Assert;
// import rx.Observable;
// import rx.functions.Action1;
//
// public class TestBatch extends TestTemplate<BatchAccount, BatchAccounts>  {
//    @Override
//    public BatchAccount createResource(BatchAccounts resources) throws Exception {
//        final String batchAccountName = "batch" + this.testId;
//        final BatchAccount[] batchAccounts = new BatchAccount[1];
//        final SettableFuture<BatchAccount> future = SettableFuture.create();
//
//
//        Observable<Indexable> resourceStream = resources.define(batchAccountName)
//                .withRegion(Region.INDIA_CENTRAL)
//                .withNewResourceGroup()
//                .withTag("mytag", "testtag")
//                .createAsync();
//
//        Utils.<BatchAccount>rootResource(resourceStream)
//                .subscribe(new Action1<BatchAccount>() {
//                    @Override
//                    public void call(BatchAccount batchAccount) {
//                        future.set(batchAccount);
//                    }
//                });
//
//        batchAccounts[0] = future.get();
//        Assert.assertNull(batchAccounts[0].autoStorage());
//
//        return batchAccounts[0];
//    }
//
//    @Override
//    public BatchAccount updateResource(BatchAccount resource) throws Exception {
//        String storageAccountName = "batchsa" + this.testId;
//
//        resource = resource.update()
//                .withNewStorageAccount(storageAccountName)
//                .apply();
//
//        Assert.assertNotNull(resource.autoStorage());
//
//        return resource;
//    }
//
//    @Override
//    public void print(BatchAccount resource) {
//        System.out.println(new StringBuilder().append("BatchAccount account: ").append(resource.id()).append(", Name:
// ").append(resource.name()).toString());
//    }
// }
