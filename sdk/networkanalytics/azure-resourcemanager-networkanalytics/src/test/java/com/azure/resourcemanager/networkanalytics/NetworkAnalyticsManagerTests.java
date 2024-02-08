// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.networkanalytics;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.networkanalytics.models.DataProduct;
import com.azure.resourcemanager.networkanalytics.models.DataProductProperties;
import com.azure.resourcemanager.networkanalytics.models.ManagedServiceIdentity;
import com.azure.resourcemanager.networkanalytics.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class NetworkAnalyticsManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.create("centraluseuap", "CENTRAL US EUAP");
    private String resourceGroupName = "rg" + randomPadding();
    private NetworkAnalyticsManager networkAnalyticsManager = null;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        networkAnalyticsManager = NetworkAnalyticsManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(REGION)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testCreateDataProduct() {
        DataProduct dataProduct = null;
        try {
            String productName = "product" + randomPadding();
            // @embedStart
            dataProduct = networkAnalyticsManager.dataProducts()
                .define(productName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
                .withProperties(new DataProductProperties().withPublisher("Microsoft").withProduct("MCC").withMajorVersion("1"))
                .create();
            // @embedEnd
            dataProduct.refresh();
            Assertions.assertEquals(dataProduct.name(), productName);
            Assertions.assertEquals(dataProduct.name(), networkAnalyticsManager.dataProducts().getById(dataProduct.id()).name());
            Assertions.assertTrue(networkAnalyticsManager.dataProducts().listByResourceGroup(resourceGroupName).stream().findAny().isPresent());
        } finally {
            if (dataProduct != null) {
                networkAnalyticsManager.dataProducts().deleteById(dataProduct.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

}
