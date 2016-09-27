/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class BatchAccountOperationsTests extends BatchManagementTestBase {
    private static final String RG_NAME = "javacbatch382";
    private static final String BATCH_NAME = "javacsmsa382";
    private static final String SA_NAME = "javacsmsa382";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCRUDBatchAccount() throws Exception {
        // Create
        BatchAccount batchAccount = batchManager.batchAccounts()
                .define(BATCH_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .createAsync()
                .toBlocking().last();
        Assert.assertEquals(RG_NAME, batchAccount.resourceGroupName());
        Assert.assertNull(batchAccount.autoStorage());
        // List
        List<BatchAccount> accounts = batchManager.batchAccounts().listByGroup(RG_NAME);
        boolean found = false;
        for (BatchAccount account : accounts) {
            if (account.name().equals(BATCH_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        batchAccount = batchManager.batchAccounts().getByGroup(RG_NAME, BATCH_NAME);
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
        batchManager.batchAccounts().delete(batchAccount.resourceGroupName(), batchAccount.name());
        try {
            batchManager.batchAccounts().getById(batchAccount.id());
            Assert.assertTrue(false);
        }
        catch (CloudException exception) {
            Assert.assertEquals(exception.getResponse().code(), 404);
        }
    }

    @Test
    public void canCreateBatchAccountWithApplication() throws Exception {
        String applicationId = "myApplication";
        String applicationDisplayName = "displayName";
        boolean allowUpdates = true;

        // Create
        BatchAccount batchAccount = batchManager.batchAccounts()
                .define(BATCH_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .defineNewApplication(applicationId)
                    .withDisplayName(applicationDisplayName)
                    .withAllowUpdates(allowUpdates)
                    .attach()
                .withNewStorageAccount(SA_NAME)
                .createAsync()
                .toBlocking().last();
        Assert.assertEquals(RG_NAME, batchAccount.resourceGroupName());
        Assert.assertNotNull(batchAccount.autoStorage());
        Assert.assertEquals(ResourceUtils.nameFromResourceId(batchAccount.autoStorage().storageAccountId()), SA_NAME);

        // List
        List<BatchAccount> accounts = batchManager.batchAccounts().listByGroup(RG_NAME);
        boolean found = false;
        for (BatchAccount account : accounts) {
            if (account.name().equals(BATCH_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        batchAccount = batchManager.batchAccounts().getByGroup(RG_NAME, BATCH_NAME);
        Assert.assertNotNull(batchAccount);

        Assert.assertTrue(batchAccount.applications().containsKey(applicationId));
        Application application = batchAccount.applications().get(applicationId);

        Assert.assertNotNull(application);
        Assert.assertEquals(application.displayName(), applicationDisplayName);
        Assert.assertEquals(application.updatesAllowed(), allowUpdates);

        batchManager.batchAccounts().delete(batchAccount.resourceGroupName(), batchAccount.name());
        try {
            batchManager.batchAccounts().getById(batchAccount.id());
            Assert.assertTrue(false);
        }
        catch (CloudException exception) {
            Assert.assertEquals(exception.getResponse().code(), 404);
        }
    }
}
