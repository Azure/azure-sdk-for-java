package com.azure.management.storage;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageManagementPoliciesTests extends StorageManagementTest {
    private String rgName = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        rgName = generateRandomResourceName("javacsmrg", 15);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateManagementPolicies() {
        String SA_NAME = generateRandomResourceName("javacmsa", 15);
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.COOL)
                .create();


        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy = managementPolicies.define("management-test")
                .withExistingStorageAccount(rgName, SA_NAME)
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

        //Assertions.assertEquals("management-test", managementPolicy.policy().);
        Assertions.assertEquals("rule1", managementPolicy.policy().getRules().get(0).getName());
        Assertions.assertEquals(blobTypesToFilterFor, managementPolicy.policy().getRules().get(0).getDefinition().getFilters().getBlobTypes());
        Assertions.assertEquals(prefixesToFilterFor, managementPolicy.policy().getRules().get(0).getDefinition().getFilters().getPrefixMatch());
        Assertions.assertEquals(30, managementPolicy.policy().getRules().get(0).getDefinition().getActions().getBaseBlob().getTierToCool().getDaysAfterModificationGreaterThan(), 0.001);
        Assertions.assertEquals(90, managementPolicy.policy().getRules().get(0).getDefinition().getActions().getBaseBlob().getTierToArchive().getDaysAfterModificationGreaterThan(), 0.001);
        Assertions.assertEquals(2555, managementPolicy.policy().getRules().get(0).getDefinition().getActions().getBaseBlob().getDelete().getDaysAfterModificationGreaterThan(), 0.001);
        Assertions.assertEquals(90, managementPolicy.policy().getRules().get(0).getDefinition().getActions().getSnapshot().getDelete().getDaysAfterCreationGreaterThan(),0.001);
    }

    @Test
    public void managementPolicyGetters() {
        String SA_NAME = generateRandomResourceName("javacmsa", 15);
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.COOL)
                .create();


        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy = managementPolicies.define("management-test")
                .withExistingStorageAccount(rgName, SA_NAME)
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
        Assertions.assertArrayEquals(Collections.unmodifiableList(blobTypesToFilterFor).toArray(), rules.get(0).blobTypesToFilterFor().toArray());
        Assertions.assertArrayEquals(Collections.unmodifiableList(prefixesToFilterFor).toArray(), rules.get(0).prefixesToFilterFor().toArray());
        Assertions.assertEquals(30, rules.get(0).daysAfterBaseBlobModificationUntilCooling().intValue());
        Assertions.assertTrue(rules.get(0).tierToCoolActionOnBaseBlobEnabled());
        Assertions.assertEquals(90, rules.get(0).daysAfterBaseBlobModificationUntilArchiving().intValue());
        Assertions.assertTrue(rules.get(0).tierToArchiveActionOnBaseBlobEnabled());
        Assertions.assertEquals(2555, rules.get(0).daysAfterBaseBlobModificationUntilDeleting().intValue());
        Assertions.assertTrue(rules.get(0).deleteActionOnBaseBlobEnabled());
        Assertions.assertEquals(90,rules.get(0).daysAfterSnapShotCreationUntilDeleting().intValue());
        Assertions.assertTrue(rules.get(0).deleteActionOnSnapShotEnabled());
    }

    @Test
    public void canUpdateManagementPolicy() {
        String SA_NAME = generateRandomResourceName("javacmsa", 15);
        List<BlobTypes> blobTypesToFilterFor = new ArrayList<>();
        blobTypesToFilterFor.add(BlobTypes.BLOCK_BLOB);

        List<String> prefixesToFilterFor = new ArrayList<>();
        prefixesToFilterFor.add("container1/foo");

        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.COOL)
                .create();


        ManagementPolicies managementPolicies = this.storageManager.managementPolicies();
        ManagementPolicy managementPolicy = managementPolicies.define("management-test")
                .withExistingStorageAccount(rgName, SA_NAME)
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

        managementPolicy.update().
                updateRule("rule1")
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
        Assertions.assertArrayEquals(Collections.unmodifiableList(blobTypesToFilterFor).toArray(), rules.get(0).blobTypesToFilterFor().toArray());
        Assertions.assertArrayEquals(Collections.unmodifiableList(prefixesToFilterFor).toArray(), rules.get(0).prefixesToFilterFor().toArray());
        Assertions.assertEquals(30, rules.get(0).daysAfterBaseBlobModificationUntilCooling().intValue());
        Assertions.assertTrue(rules.get(0).tierToCoolActionOnBaseBlobEnabled());
        Assertions.assertEquals(90, rules.get(0).daysAfterBaseBlobModificationUntilArchiving().intValue());
        Assertions.assertTrue(rules.get(0).tierToArchiveActionOnBaseBlobEnabled());
        Assertions.assertEquals(2555, rules.get(0).daysAfterBaseBlobModificationUntilDeleting().intValue());
        Assertions.assertTrue(rules.get(0).deleteActionOnBaseBlobEnabled());
        Assertions.assertEquals(90,rules.get(0).daysAfterSnapShotCreationUntilDeleting().intValue());
        Assertions.assertTrue(rules.get(0).deleteActionOnSnapShotEnabled());
    }
}