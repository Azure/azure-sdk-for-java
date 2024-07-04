// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.apimanagement;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceIdentity;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceSkuProperties;
import com.azure.resourcemanager.apimanagement.models.ApimIdentityType;
import com.azure.resourcemanager.apimanagement.models.SkuType;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class ApiManagementManagerTest extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private ApiManagementManager apiManagementManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        apiManagementManager = ApiManagementManager
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
    @LiveOnly
    public void testCreateApiManagementService() {
        ApiManagementServiceResource resource = null;
        try {
            String serviceName = "apimService" + randomPadding();
            // @embedmeStart
            resource = apiManagementManager
                .apiManagementServices()
                .define(serviceName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(new ApiManagementServiceSkuProperties().withName(SkuType.CONSUMPTION).withCapacity(0))
                .withPublisherEmail("apim@autorestsdk.com")
                .withPublisherName("autorestsdk")
                .withIdentity(new ApiManagementServiceIdentity().withType(ApimIdentityType.SYSTEM_ASSIGNED))
                .create();
            // @embedmeEnd
            resource.refresh();
            Assertions.assertEquals(resource.name(), serviceName);
            Assertions.assertEquals(resource.name(), apiManagementManager.apiManagementServices().getById(resource.id()).name());
            Assertions.assertTrue(apiManagementManager.apiManagementServices().list().stream().count() > 0);
        } finally {
            if (resource != null) {
                apiManagementManager.apiManagementServices().deleteById(resource.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
