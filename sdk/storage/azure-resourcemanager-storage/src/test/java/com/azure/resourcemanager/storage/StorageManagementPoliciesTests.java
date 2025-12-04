// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.AccessTier;
import com.azure.resourcemanager.storage.models.BlobTypes;
import com.azure.resourcemanager.storage.models.DateAfterModification;
import com.azure.resourcemanager.storage.models.Kind;
import com.azure.resourcemanager.storage.models.ManagementPolicies;
import com.azure.resourcemanager.storage.models.ManagementPolicy;
import com.azure.resourcemanager.storage.models.ManagementPolicyBaseBlob;
import com.azure.resourcemanager.storage.models.PolicyRule;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        StorageAccount storageAccount = storageManager.storageAccounts()
            .define(saName)
            .withRegion(Region.US_WEST_CENTRAL)
            .withNewResourceGroup(rgName)
            .withBlobStorageAccountKind()
            .withAccessTier(AccessTier.COOL)
            .disableSharedKeyAccess()
            .create();

        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy = managementPolicies.define("management-test")
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
        Assertions.assertEquals(blobTypesToFilterFor,
            managementPolicy.policy().rules().get(0).definition().filters().blobTypes());
        Assertions.assertEquals(prefixesToFilterFor,
            managementPolicy.policy().rules().get(0).definition().filters().prefixMatch());
        Assertions.assertEquals(30,
            managementPolicy.policy()
                .rules()
                .get(0)
                .definition()
                .actions()
                .baseBlob()
                .tierToCool()
                .daysAfterModificationGreaterThan(),
            0.001);
        Assertions.assertEquals(90,
            managementPolicy.policy()
                .rules()
                .get(0)
                .definition()
                .actions()
                .baseBlob()
                .tierToArchive()
                .daysAfterModificationGreaterThan(),
            0.001);
        Assertions.assertEquals(2555,
            managementPolicy.policy()
                .rules()
                .get(0)
                .definition()
                .actions()
                .baseBlob()
                .delete()
                .daysAfterModificationGreaterThan(),
            0.001);
        Assertions.assertEquals(90,
            managementPolicy.policy()
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
        StorageAccount storageAccount = storageManager.storageAccounts()
            .define(saName)
            .withRegion(Region.US_WEST_CENTRAL)
            .withNewResourceGroup(rgName)
            .withBlobStorageAccountKind()
            .withAccessTier(AccessTier.COOL)
            .disableSharedKeyAccess()
            .create();

        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy = managementPolicies.define("management-test")
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
        Assertions.assertArrayEquals(Collections.unmodifiableList(blobTypesToFilterFor).toArray(),
            rules.get(0).blobTypesToFilterFor().toArray());
        Assertions.assertArrayEquals(Collections.unmodifiableList(prefixesToFilterFor).toArray(),
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

        StorageAccount storageAccount = storageManager.storageAccounts()
            .define(saName)
            .withRegion(Region.US_WEST_CENTRAL)
            .withNewResourceGroup(rgName)
            .withBlobStorageAccountKind()
            .withAccessTier(AccessTier.COOL)
            .disableSharedKeyAccess()
            .create();

        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy = managementPolicies.define("management-test")
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

        managementPolicy.update()
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
        Assertions.assertArrayEquals(Collections.unmodifiableList(blobTypesToFilterFor).toArray(),
            rules.get(0).blobTypesToFilterFor().toArray());
        Assertions.assertArrayEquals(Collections.unmodifiableList(prefixesToFilterFor).toArray(),
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
    public void testLcmBaseBlobActionsWithPremiumAccount() {
        String saName = generateRandomResourceName("javacmsa", 15);

        StorageAccount storageAccount = storageManager.storageAccounts()
            .define(saName)
            .withRegion(Region.US_WEST_CENTRAL)
            .withNewResourceGroup(rgName)
            .withSku(StorageAccountSkuType.PREMIUM_LRS)
            .withBlockBlobStorageAccountKind()
            .disableSharedKeyAccess()
            .create();

        Assertions.assertEquals(StorageAccountSkuType.PREMIUM_LRS.name(), storageAccount.skuType().name());
        Assertions.assertEquals(Kind.BLOCK_BLOB_STORAGE, storageAccount.kind());

        // enable last access time tracking policy
        storageManager.blobServices()
            .define("managementPolicyTest")
            .withExistingStorageAccount(rgName, saName)
            .withLastAccessTimeTrackingPolicyEnabled()
            .create();

        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy = managementPolicies.define("management-test")
            .withExistingStorageAccount(rgName, saName)
            .defineRule("tierToHotLMT")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToHot(new DateAfterModification().withDaysAfterModificationGreaterThan(50f)))
            .attach()
            .defineRule("tierToCoolLMT")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToCool(new DateAfterModification().withDaysAfterModificationGreaterThan(50f)))
            .attach()
            .defineRule("tierToArchiveLMT")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToArchive(new DateAfterModification().withDaysAfterModificationGreaterThan(50f)))
            .attach()
            .defineRule("tierToHotCreated")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToHot(new DateAfterModification().withDaysAfterCreationGreaterThan(50f)))
            .attach()
            .defineRule("tierToCoolCreated")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToCool(new DateAfterModification().withDaysAfterCreationGreaterThan(50f)))
            .attach()
            .defineRule("tierToArchiveCreated")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToArchive(new DateAfterModification().withDaysAfterCreationGreaterThan(50f)))
            .attach()
            .defineRule("tierToHotLAT")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToHot(new DateAfterModification().withDaysAfterLastAccessTimeGreaterThan(50f)))
            .attach()
            .defineRule("tierToCoolLAT")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToCool(new DateAfterModification().withDaysAfterLastAccessTimeGreaterThan(50f)))
            .attach()
            .defineRule("tierToArchiveLAT")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToArchive(new DateAfterModification().withDaysAfterLastAccessTimeGreaterThan(50f)))
            .attach()
            .defineRule("tierToCoolAutoUpTierLAT")
            .withLifecycleRuleType()
            .withBlobTypeToFilterFor(BlobTypes.BLOCK_BLOB)
            .withActionsOnBaseBlob(new ManagementPolicyBaseBlob()
                .withTierToCool(new DateAfterModification().withDaysAfterLastAccessTimeGreaterThan(50f))
                .withEnableAutoTierToHotFromCool(true))
            .attach()
            .create();

        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> ResourceManagerUtils
                .toPrimitiveBoolean(rule.actionsOnBaseBlob().enableAutoTierToHotFromCool())));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToHot() != null
                && rule.actionsOnBaseBlob().tierToHot().daysAfterModificationGreaterThan() != null));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToCool() != null
                && rule.actionsOnBaseBlob().tierToCool().daysAfterModificationGreaterThan() != null));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToArchive() != null
                && rule.actionsOnBaseBlob().tierToArchive().daysAfterModificationGreaterThan() != null));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToHot() != null
                && rule.actionsOnBaseBlob().tierToHot().daysAfterCreationGreaterThan() != null));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToCool() != null
                && rule.actionsOnBaseBlob().tierToCool().daysAfterCreationGreaterThan() != null));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToArchive() != null
                && rule.actionsOnBaseBlob().tierToArchive().daysAfterCreationGreaterThan() != null));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToHot() != null
                && rule.actionsOnBaseBlob().tierToHot().daysAfterLastAccessTimeGreaterThan() != null));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToCool() != null
                && rule.actionsOnBaseBlob().tierToCool().daysAfterLastAccessTimeGreaterThan() != null));
        Assertions.assertTrue(managementPolicy.rules()
            .stream()
            .anyMatch(rule -> rule.actionsOnBaseBlob().tierToArchive() != null
                && rule.actionsOnBaseBlob().tierToArchive().daysAfterLastAccessTimeGreaterThan() != null));
    }
}
