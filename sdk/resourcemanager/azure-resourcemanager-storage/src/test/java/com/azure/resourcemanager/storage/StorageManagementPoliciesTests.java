// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.AccessTier;
import com.azure.resourcemanager.storage.models.BlobTypes;
import com.azure.resourcemanager.storage.models.ManagementPolicies;
import com.azure.resourcemanager.storage.models.ManagementPolicy;
import com.azure.resourcemanager.storage.models.PolicyRule;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageManagementPoliciesTests extends StorageManagementTest {
    private String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateManagementPolicies() {
        String saName = generateRandomResourceName("javacmsa", 15);
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.COOL)
                .create();

        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy =
            managementPolicies
                .define("management-test")
                .withExistingStorageAccount(rgName, saName)
                .defineRule("rule1")
                .withLifecycleRuleType()
                .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
                .withPrefixToFilterFor("container1/foo")
                .withTierToCoolActionOnBaseBlob(30)
                .withTierToArchiveActionOnBaseBlob(90)
                .withDeleteActionOnBaseBlob(2555)
                .withDeleteActionOnSnapShot(90)
                .attach()
                .create();

        List<String> blobTypesToFilterFor = new ArrayList<>();
        blobTypesToFilterFor.add("blockBlob");

        List<String> prefixesToFilterFor = new ArrayList<>();
        prefixesToFilterFor.add("container1/foo");

        // Assertions.assertEquals("management-test", managementPolicy.policy().);
        Assertions.assertEquals("rule1", managementPolicy.policy().rules().get(0).name());
        Assertions
            .assertEquals(
                blobTypesToFilterFor, managementPolicy.policy().rules().get(0).definition().filters().blobTypes());
        Assertions
            .assertEquals(
                prefixesToFilterFor, managementPolicy.policy().rules().get(0).definition().filters().prefixMatch());
        Assertions
            .assertEquals(
                30,
                managementPolicy
                    .policy()
                    .rules()
                    .get(0)
                    .definition()
                    .actions()
                    .baseBlob()
                    .tierToCool()
                    .daysAfterModificationGreaterThan(),
                0.001);
        Assertions
            .assertEquals(
                90,
                managementPolicy
                    .policy()
                    .rules()
                    .get(0)
                    .definition()
                    .actions()
                    .baseBlob()
                    .tierToArchive()
                    .daysAfterModificationGreaterThan(),
                0.001);
        Assertions
            .assertEquals(
                2555,
                managementPolicy
                    .policy()
                    .rules()
                    .get(0)
                    .definition()
                    .actions()
                    .baseBlob()
                    .delete()
                    .daysAfterModificationGreaterThan(),
                0.001);
        Assertions
            .assertEquals(
                90,
                managementPolicy
                    .policy()
                    .rules()
                    .get(0)
                    .definition()
                    .actions()
                    .snapshot()
                    .delete()
                    .daysAfterCreationGreaterThan(),
                0.001);
    }

    @Test
    public void managementPolicyGetters() {
        String saName = generateRandomResourceName("javacmsa", 15);
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.COOL)
                .create();

        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy =
            managementPolicies
                .define("management-test")
                .withExistingStorageAccount(rgName, saName)
                .defineRule("rule1")
                .withLifecycleRuleType()
                .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
                .withPrefixToFilterFor("container1/foo")
                .withTierToCoolActionOnBaseBlob(30)
                .withTierToArchiveActionOnBaseBlob(90)
                .withDeleteActionOnBaseBlob(2555)
                .withDeleteActionOnSnapShot(90)
                .attach()
                .create();

        List<BlobTypes> blobTypesToFilterFor = new ArrayList<>();
        blobTypesToFilterFor.add(BlobTypes.BLOCK_BLOB);

        List<String> prefixesToFilterFor = new ArrayList<>();
        prefixesToFilterFor.add("container1/foo");

        List<PolicyRule> rules = managementPolicy.rules();
        Assertions.assertEquals("rule1", rules.get(0).name());
        Assertions
            .assertArrayEquals(
                Collections.unmodifiableList(blobTypesToFilterFor).toArray(),
                rules.get(0).blobTypesToFilterFor().toArray());
        Assertions
            .assertArrayEquals(
                Collections.unmodifiableList(prefixesToFilterFor).toArray(),
                rules.get(0).prefixesToFilterFor().toArray());
        Assertions.assertEquals(30, rules.get(0).daysAfterBaseBlobModificationUntilCooling().intValue());
        Assertions.assertTrue(rules.get(0).tierToCoolActionOnBaseBlobEnabled());
        Assertions.assertEquals(90, rules.get(0).daysAfterBaseBlobModificationUntilArchiving().intValue());
        Assertions.assertTrue(rules.get(0).tierToArchiveActionOnBaseBlobEnabled());
        Assertions.assertEquals(2555, rules.get(0).daysAfterBaseBlobModificationUntilDeleting().intValue());
        Assertions.assertTrue(rules.get(0).deleteActionOnBaseBlobEnabled());
        Assertions.assertEquals(90, rules.get(0).daysAfterSnapShotCreationUntilDeleting().intValue());
        Assertions.assertTrue(rules.get(0).deleteActionOnSnapShotEnabled());
    }

    @Test
    public void canUpdateManagementPolicy() {
        String saName = generateRandomResourceName("javacmsa", 15);
        List<BlobTypes> blobTypesToFilterFor = new ArrayList<>();
        blobTypesToFilterFor.add(BlobTypes.BLOCK_BLOB);

        List<String> prefixesToFilterFor = new ArrayList<>();
        prefixesToFilterFor.add("container1/foo");

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.COOL)
                .create();

        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy =
            managementPolicies
                .define("management-test")
                .withExistingStorageAccount(rgName, saName)
                .defineRule("rule1")
                .withLifecycleRuleType()
                .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
                .withPrefixToFilterFor("asdf")
                .withDeleteActionOnSnapShot(100)
                .attach()
                .defineRule("rule2")
                .withLifecycleRuleType()
                .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
                .withDeleteActionOnBaseBlob(30)
                .attach()
                .create();

        managementPolicy
            .update()
            .updateRule("rule1")
            .withPrefixesToFilterFor(prefixesToFilterFor)
            .withTierToCoolActionOnBaseBlob(30)
            .withTierToArchiveActionOnBaseBlob(90)
            .withDeleteActionOnBaseBlob(2555)
            .withDeleteActionOnSnapShot(90)
            .parent()
            .withoutRule("rule2")
            .apply();

        List<PolicyRule> rules = managementPolicy.rules();
        Assertions.assertEquals(1, rules.size());
        Assertions.assertEquals("rule1", rules.get(0).name());
        Assertions
            .assertArrayEquals(
                Collections.unmodifiableList(blobTypesToFilterFor).toArray(),
                rules.get(0).blobTypesToFilterFor().toArray());
        Assertions
            .assertArrayEquals(
                Collections.unmodifiableList(prefixesToFilterFor).toArray(),
                rules.get(0).prefixesToFilterFor().toArray());
        Assertions.assertEquals(30, rules.get(0).daysAfterBaseBlobModificationUntilCooling().intValue());
        Assertions.assertTrue(rules.get(0).tierToCoolActionOnBaseBlobEnabled());
        Assertions.assertEquals(90, rules.get(0).daysAfterBaseBlobModificationUntilArchiving().intValue());
        Assertions.assertTrue(rules.get(0).tierToArchiveActionOnBaseBlobEnabled());
        Assertions.assertEquals(2555, rules.get(0).daysAfterBaseBlobModificationUntilDeleting().intValue());
        Assertions.assertTrue(rules.get(0).deleteActionOnBaseBlobEnabled());
        Assertions.assertEquals(90, rules.get(0).daysAfterSnapShotCreationUntilDeleting().intValue());
        Assertions.assertTrue(rules.get(0).deleteActionOnSnapShotEnabled());
    }
}
