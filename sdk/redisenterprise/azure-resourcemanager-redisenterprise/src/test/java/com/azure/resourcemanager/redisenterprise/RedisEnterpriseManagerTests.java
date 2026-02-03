// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redisenterprise;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.redisenterprise.models.Cluster;
import com.azure.resourcemanager.redisenterprise.models.ManagedServiceIdentity;
import com.azure.resourcemanager.redisenterprise.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.redisenterprise.models.Sku;
import com.azure.resourcemanager.redisenterprise.models.SkuName;
import com.azure.resourcemanager.redisenterprise.models.TlsVersion;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class RedisEnterpriseManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private RedisEnterpriseManager redisEnterpriseManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = TestUtilities.getTokenCredentialForTest(getTestMode());
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        redisEnterpriseManager = RedisEnterpriseManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups().define(resourceGroupName).withRegion(REGION).create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testCreate() {
        Cluster cluster = null;
        try {
            String clusterName = "cluster" + randomPadding();
            // @embedmeStart
            cluster = redisEnterpriseManager.redisEnterprises()
                .define(clusterName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(new Sku().withName(SkuName.ENTERPRISE_E10).withCapacity(2))
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
                .withMinimumTlsVersion(TlsVersion.ONE_TWO)
                .create();
            // @embedmeEnd
            cluster.refresh();
            Assertions.assertEquals(cluster.name(), clusterName);
            Assertions.assertEquals(cluster.name(),
                redisEnterpriseManager.redisEnterprises().getById(cluster.id()).name());
            Assertions.assertTrue(redisEnterpriseManager.redisEnterprises().list().stream().count() > 0);
        } finally {
            if (cluster != null) {
                redisEnterpriseManager.redisEnterprises().deleteById(cluster.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
