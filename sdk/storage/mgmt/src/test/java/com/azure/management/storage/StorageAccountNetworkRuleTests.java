/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage;

import com.azure.management.RestClient;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageAccountNetworkRuleTests extends StorageManagementTest {
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
    public void canConfigureNetworkRulesWithCreate() throws Exception {
        String SA_NAME1 = generateRandomResourceName("javacsmsa", 15);
        String SA_NAME2 = generateRandomResourceName("javacsmsa", 15);
        String SA_NAME3 = generateRandomResourceName("javacsmsa", 15);
        String SA_NAME4 = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount1 = storageManager.storageAccounts()
                .define(SA_NAME1)
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

        ResourceGroup resourceGroup = resourceManager
                .resourceGroups()
                .getByName(storageAccount1.resourceGroupName());

        StorageAccount storageAccount2 = storageManager.storageAccounts()
                .define(SA_NAME2)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(resourceGroup)
                .withAccessFromIpAddress("23.20.0.0")
                .create();

        Assertions.assertNotNull(storageAccount2.inner().getNetworkRuleSet());
        Assertions.assertNotNull(storageAccount2.inner().getNetworkRuleSet().getDefaultAction());
        Assertions.assertNotNull(storageAccount2.inner().getNetworkRuleSet().getDefaultAction().equals(DefaultAction.DENY));

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

        StorageAccount storageAccount3 = storageManager.storageAccounts()
                .define(SA_NAME3)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAccessFromAllNetworks()
                .withAccessFromIpAddress("23.20.0.0")
                .create();

        Assertions.assertNotNull(storageAccount3.inner().getNetworkRuleSet());
        Assertions.assertNotNull(storageAccount3.inner().getNetworkRuleSet().getDefaultAction());
        Assertions.assertNotNull(storageAccount3.inner().getNetworkRuleSet().getDefaultAction().equals(DefaultAction.ALLOW));

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

        StorageAccount storageAccount4 = storageManager.storageAccounts()
                .define(SA_NAME4)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(resourceGroup)
                .withReadAccessToLogEntriesFromAnyNetwork()
                .withReadAccessToMetricsFromAnyNetwork()
                .create();

        Assertions.assertNotNull(storageAccount4.inner().getNetworkRuleSet());
        Assertions.assertNotNull(storageAccount4.inner().getNetworkRuleSet().getDefaultAction());
        Assertions.assertNotNull(storageAccount4.inner().getNetworkRuleSet().getDefaultAction().equals(DefaultAction.DENY));

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
        String SA_NAME1 = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount1 = storageManager.storageAccounts()
                .define(SA_NAME1)
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

        storageAccount1.update()
                .withAccessFromSelectedNetworks()
                .withAccessFromIpAddressRange("23.20.0.0/20")
                .apply();

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
