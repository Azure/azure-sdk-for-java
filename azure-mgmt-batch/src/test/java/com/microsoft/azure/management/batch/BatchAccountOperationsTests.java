/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

public class BatchAccountOperationsTests extends BatchManagementTest {
    @Test
    public void canCRUDBatchAccount() throws Exception {
        // Create
        Observable<Indexable> resourceStream = batchManager.batchAccounts()
                .define(BATCH_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .createAsync();

        BatchAccount batchAccount = Utils.<BatchAccount>rootResource(resourceStream)
                .toBlocking().last();
        Assert.assertEquals(RG_NAME, batchAccount.resourceGroupName());
        Assert.assertNull(batchAccount.autoStorage());
        // List
        List<BatchAccount> accounts = batchManager.batchAccounts().listByResourceGroup(RG_NAME);
        boolean found = false;
        for (BatchAccount account : accounts) {
            if (account.name().equals(BATCH_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        batchAccount = batchManager.batchAccounts().getByResourceGroup(RG_NAME, BATCH_NAME);
        Assert.assertNotNull(batchAccount);

        // Get Keys
        BatchAccountKeys keys = batchAccount.getKeys();
        Assert.assertNotNull(keys.primary());
        Assert.assertNotNull(keys.secondary());

        BatchAccountKeys newKeys = batchAccount.regenerateKeys(AccountKeyType.PRIMARY);
        Assert.assertNotNull(newKeys.primary());
        Assert.assertNotNull(newKeys.secondary());

        Assert.assertNotEquals(newKeys.primary(), keys.primary());
        Assert.assertEquals(newKeys.secondary(), keys.secondary());

        batchAccount = batchAccount.update()
                .withNewStorageAccount(SA_NAME)
                .apply();

        Assert.assertNotNull(batchAccount.autoStorage().storageAccountId());
        Assert.assertNotNull(batchAccount.autoStorage().lastKeySync());
        DateTime lastSync = batchAccount.autoStorage().lastKeySync();

        batchAccount.synchronizeAutoStorageKeys();
        batchAccount.refresh();

        Assert.assertNotEquals(lastSync, batchAccount.autoStorage().lastKeySync());

        // Test applications.
        String applicationId = "myApplication";
        String applicationDisplayName = "displayName";
        String applicationPackageName = "applicationPackage";

        boolean updatesAllowed = true;
        batchAccount.update()
                .defineNewApplication(applicationId)
                    .defineNewApplicationPackage(applicationPackageName)
                .withDisplayName(applicationDisplayName)
                .withAllowUpdates(updatesAllowed)
                .attach()
                .apply();
        Assert.assertTrue(batchAccount.applications().containsKey(applicationId));

        // Refresh to fetch batch account and application again.
        batchAccount.refresh();
        Assert.assertTrue(batchAccount.applications().containsKey(applicationId));

        Application application = batchAccount.applications().get(applicationId);
        Assert.assertEquals(application.displayName(), applicationDisplayName);
        Assert.assertEquals(application.updatesAllowed(), updatesAllowed);
        Assert.assertEquals(1, application.applicationPackages().size());
        ApplicationPackage applicationPackage = application.applicationPackages().get(applicationPackageName);
        Assert.assertEquals(applicationPackage.name(), applicationPackageName);

        // Delete application package directly.
        applicationPackage.delete();
        batchAccount
                .update()
                .withoutApplication(applicationId)
                .apply();

        SdkContext.sleep(30 * 1000);
        batchAccount.refresh();
        Assert.assertFalse(batchAccount.applications().containsKey(applicationId));

        String applicationPackage1Name = "applicationPackage1";
        String applicationPackage2Name = "applicationPackage2";
        batchAccount.update()
                .defineNewApplication(applicationId)
                    .defineNewApplicationPackage(applicationPackage1Name)
                    .defineNewApplicationPackage(applicationPackage2Name)
                .withDisplayName(applicationDisplayName)
                .withAllowUpdates(updatesAllowed)
                .attach()
                .apply();
        Assert.assertTrue(batchAccount.applications().containsKey(applicationId));
        application.refresh();
        Assert.assertEquals(2, application.applicationPackages().size());

        String newApplicationDisplayName = "newApplicationDisplayName";
        batchAccount
                .update()
                .updateApplication(applicationId)
                    .withoutApplicationPackage(applicationPackage2Name)
                .withDisplayName(newApplicationDisplayName)
                .parent()
                .apply();
        application = batchAccount.applications().get(applicationId);
        Assert.assertEquals(application.displayName(), newApplicationDisplayName);

        batchAccount.refresh();
        application = batchAccount.applications().get(applicationId);

        Assert.assertEquals(application.displayName(), newApplicationDisplayName);
        Assert.assertEquals(1, application.applicationPackages().size());

        applicationPackage = application.applicationPackages().get(applicationPackage1Name);

        Assert.assertNotNull(applicationPackage);
        String id = applicationPackage.id();
        Assert.assertNotNull(applicationPackage.id());
        Assert.assertEquals(applicationPackage.name(), applicationPackage1Name);
        Assert.assertNull(applicationPackage.format());

        batchAccount
                .update()
                .updateApplication(applicationId)
                    .withoutApplicationPackage(applicationPackage1Name)
                .parent()
                .apply();
        batchManager.batchAccounts().deleteByResourceGroup(batchAccount.resourceGroupName(), batchAccount.name());

        batchAccount = batchManager.batchAccounts().getById(batchAccount.id());
        Assert.assertNull(batchAccount);
    }

    @Test
    public void canCreateBatchAccountWithApplication() throws Exception {
        String applicationId = "myApplication";
        String applicationDisplayName = "displayName";
        boolean allowUpdates = true;

        // Create
        Observable<Indexable> resourceStream = batchManager.batchAccounts()
                .define(BATCH_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .defineNewApplication(applicationId)
                .withDisplayName(applicationDisplayName)
                .withAllowUpdates(allowUpdates)
                .attach()
                .withNewStorageAccount(SA_NAME)
                .createAsync();

        BatchAccount batchAccount = Utils.<BatchAccount>rootResource(resourceStream)
                .toBlocking().last();
        Assert.assertEquals(RG_NAME, batchAccount.resourceGroupName());
        Assert.assertNotNull(batchAccount.autoStorage());
        Assert.assertEquals(ResourceUtils.nameFromResourceId(batchAccount.autoStorage().storageAccountId()), SA_NAME);

        // List
        List<BatchAccount> accounts = batchManager.batchAccounts().listByResourceGroup(RG_NAME);
        boolean found = false;
        for (BatchAccount account : accounts) {
            if (account.name().equals(BATCH_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        batchAccount = batchManager.batchAccounts().getByResourceGroup(RG_NAME, BATCH_NAME);
        Assert.assertNotNull(batchAccount);

        Assert.assertTrue(batchAccount.applications().containsKey(applicationId));
        Application application = batchAccount.applications().get(applicationId);

        Assert.assertNotNull(application);
        Assert.assertEquals(application.displayName(), applicationDisplayName);
        Assert.assertEquals(application.updatesAllowed(), allowUpdates);

        batchManager.batchAccounts().deleteByResourceGroup(batchAccount.resourceGroupName(), batchAccount.name());
        batchAccount = batchManager.batchAccounts().getById(batchAccount.id());
        Assert.assertNull(batchAccount);
    }

    @Test
    public void batchAccountListAsyncTest() throws Exception {
        // Create
        Observable<Indexable> resourceStream = batchManager.batchAccounts()
                .define(BATCH_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .createAsync();

        final List<BatchAccount> batchAccounts = new ArrayList<>();
        final List<BatchAccount> createdBatchAccounts  = new ArrayList<>();
        final Action1<BatchAccount> onListBatchAccount = new Action1<BatchAccount>() {
            @Override
            public void call(BatchAccount batchAccountInList) {
                batchAccounts.add(batchAccountInList);
            }
        };

        final Func1<BatchAccount, Observable<BatchAccount>> onCreateBatchAccount = new Func1<BatchAccount, Observable<BatchAccount>>() {
            @Override
            public Observable<BatchAccount> call(final BatchAccount createdBatchAccount) {
                createdBatchAccounts.add(createdBatchAccount);
                return batchManager.batchAccounts().listAsync().doOnNext(onListBatchAccount);
            }
        };

        Utils.<BatchAccount>rootResource(resourceStream).flatMap(onCreateBatchAccount).toBlocking().last();
        Assert.assertEquals(1, createdBatchAccounts.size());
        boolean accountExists = false;
        for (BatchAccount batchAccountInList: batchAccounts) {
            if (createdBatchAccounts.get(0).id().equalsIgnoreCase(batchAccountInList.id())) {
                accountExists = true;
            }
            Assert.assertNotNull(batchAccountInList.id());
        }
        Assert.assertTrue(accountExists);
    }
}
