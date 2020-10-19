// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.DefaultAction;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageAccountNetworkRuleTests extends StorageManagementTest {
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
    public void canConfigureNetworkRulesWithCreate() throws Exception {
        String saName1 = generateRandomResourceName("javacsmsa", 15);
        String saName2 = generateRandomResourceName("javacsmsa", 15);
        String saName3 = generateRandomResourceName("javacsmsa", 15);
        String saName4 = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount1 =
            storageManager
                .storageAccounts()
                .define(saName1)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        Assertions.assertNotNull(storageAccount1.networkSubnetsWithAccess());
        Assertions.assertEquals(0, storageAccount1.networkSubnetsWithAccess().size());

        Assertions.assertNotNull(storageAccount1.ipAddressesWithAccess());
        Assertions.assertEquals(0, storageAccount1.ipAddressesWithAccess().size());

        Assertions.assertNotNull(storageAccount1.ipAddressRangesWithAccess());
        Assertions.assertEquals(0, storageAccount1.ipAddressRangesWithAccess().size());

        Assertions.assertTrue(storageAccount1.isAccessAllowedFromAllNetworks());
        Assertions.assertTrue(storageAccount1.canAccessFromAzureServices());
        Assertions.assertTrue(storageAccount1.canReadMetricsFromAnyNetwork());
        Assertions.assertTrue(storageAccount1.canReadMetricsFromAnyNetwork());

        ResourceGroup resourceGroup = resourceManager.resourceGroups().getByName(storageAccount1.resourceGroupName());

        StorageAccount storageAccount2 =
            storageManager
                .storageAccounts()
                .define(saName2)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(resourceGroup)
                .withAccessFromIpAddress("23.20.0.0")
                .create();

        Assertions.assertNotNull(storageAccount2.innerModel().networkRuleSet());
        Assertions.assertNotNull(storageAccount2.innerModel().networkRuleSet().defaultAction());
        Assertions.assertNotNull(storageAccount2.innerModel().networkRuleSet().defaultAction().equals(DefaultAction.DENY));

        Assertions.assertNotNull(storageAccount2.networkSubnetsWithAccess());
        Assertions.assertEquals(0, storageAccount2.networkSubnetsWithAccess().size());

        Assertions.assertNotNull(storageAccount2.ipAddressesWithAccess());
        Assertions.assertEquals(1, storageAccount2.ipAddressesWithAccess().size());

        Assertions.assertNotNull(storageAccount2.ipAddressRangesWithAccess());
        Assertions.assertEquals(0, storageAccount2.ipAddressRangesWithAccess().size());

        Assertions.assertFalse(storageAccount2.isAccessAllowedFromAllNetworks());
        Assertions.assertFalse(storageAccount2.canAccessFromAzureServices());
        Assertions.assertFalse(storageAccount2.canReadMetricsFromAnyNetwork());
        Assertions.assertFalse(storageAccount2.canReadMetricsFromAnyNetwork());

        StorageAccount storageAccount3 =
            storageManager
                .storageAccounts()
                .define(saName3)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAccessFromAllNetworks()
                .withAccessFromIpAddress("23.20.0.0")
                .create();

        Assertions.assertNotNull(storageAccount3.innerModel().networkRuleSet());
        Assertions.assertNotNull(storageAccount3.innerModel().networkRuleSet().defaultAction());
        Assertions.assertNotNull(storageAccount3.innerModel().networkRuleSet().defaultAction().equals(DefaultAction.ALLOW));

        Assertions.assertNotNull(storageAccount3.networkSubnetsWithAccess());
        Assertions.assertEquals(0, storageAccount3.networkSubnetsWithAccess().size());

        Assertions.assertNotNull(storageAccount3.ipAddressesWithAccess());
        Assertions.assertEquals(1, storageAccount3.ipAddressesWithAccess().size());

        Assertions.assertNotNull(storageAccount3.ipAddressRangesWithAccess());
        Assertions.assertEquals(0, storageAccount3.ipAddressRangesWithAccess().size());

        Assertions.assertTrue(storageAccount3.isAccessAllowedFromAllNetworks());
        Assertions.assertTrue(storageAccount3.canAccessFromAzureServices());
        Assertions.assertTrue(storageAccount3.canReadMetricsFromAnyNetwork());
        Assertions.assertTrue(storageAccount3.canReadLogEntriesFromAnyNetwork());

        StorageAccount storageAccount4 =
            storageManager
                .storageAccounts()
                .define(saName4)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(resourceGroup)
                .withReadAccessToLogEntriesFromAnyNetwork()
                .withReadAccessToMetricsFromAnyNetwork()
                .create();

        Assertions.assertNotNull(storageAccount4.innerModel().networkRuleSet());
        Assertions.assertNotNull(storageAccount4.innerModel().networkRuleSet().defaultAction());
        Assertions.assertNotNull(storageAccount4.innerModel().networkRuleSet().defaultAction().equals(DefaultAction.DENY));

        Assertions.assertNotNull(storageAccount4.networkSubnetsWithAccess());
        Assertions.assertEquals(0, storageAccount4.networkSubnetsWithAccess().size());

        Assertions.assertNotNull(storageAccount4.ipAddressesWithAccess());
        Assertions.assertEquals(0, storageAccount4.ipAddressesWithAccess().size());

        Assertions.assertNotNull(storageAccount3.ipAddressRangesWithAccess());
        Assertions.assertEquals(0, storageAccount4.ipAddressRangesWithAccess().size());

        Assertions.assertFalse(storageAccount4.isAccessAllowedFromAllNetworks());
        Assertions.assertFalse(storageAccount4.canAccessFromAzureServices());
        Assertions.assertTrue(storageAccount4.canReadMetricsFromAnyNetwork());
        Assertions.assertTrue(storageAccount4.canReadLogEntriesFromAnyNetwork());
    }

    @Test
    public void canConfigureNetworkRulesWithUpdate() throws Exception {
        String saName1 = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount1 =
            storageManager
                .storageAccounts()
                .define(saName1)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        Assertions.assertNotNull(storageAccount1.networkSubnetsWithAccess());
        Assertions.assertEquals(0, storageAccount1.networkSubnetsWithAccess().size());

        Assertions.assertNotNull(storageAccount1.ipAddressesWithAccess());
        Assertions.assertEquals(0, storageAccount1.ipAddressesWithAccess().size());

        Assertions.assertNotNull(storageAccount1.ipAddressRangesWithAccess());
        Assertions.assertEquals(0, storageAccount1.ipAddressRangesWithAccess().size());

        Assertions.assertTrue(storageAccount1.isAccessAllowedFromAllNetworks());
        Assertions.assertTrue(storageAccount1.canAccessFromAzureServices());
        Assertions.assertTrue(storageAccount1.canReadMetricsFromAnyNetwork());
        Assertions.assertTrue(storageAccount1.canReadMetricsFromAnyNetwork());

        storageAccount1.update().withAccessFromSelectedNetworks().withAccessFromIpAddressRange("23.20.0.0/20").apply();

        Assertions.assertNotNull(storageAccount1.networkSubnetsWithAccess());
        Assertions.assertEquals(0, storageAccount1.networkSubnetsWithAccess().size());

        Assertions.assertNotNull(storageAccount1.ipAddressesWithAccess());
        Assertions.assertEquals(0, storageAccount1.ipAddressesWithAccess().size());

        Assertions.assertNotNull(storageAccount1.ipAddressRangesWithAccess());
        Assertions.assertEquals(1, storageAccount1.ipAddressRangesWithAccess().size());

        Assertions.assertFalse(storageAccount1.isAccessAllowedFromAllNetworks());
        Assertions.assertTrue(storageAccount1.canAccessFromAzureServices());
        Assertions.assertFalse(storageAccount1.canReadMetricsFromAnyNetwork());
        Assertions.assertFalse(storageAccount1.canReadMetricsFromAnyNetwork());
    }
}
