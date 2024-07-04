// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.paloaltonetworks.ngfw;

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
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.*;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class PaloAltoNetworksNgfwManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private PaloAltoNetworksNgfwManager paloAltoNetworksNgfwManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        paloAltoNetworksNgfwManager = PaloAltoNetworksNgfwManager
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
    public void testCreateLocalRulestack() {
        LocalRulestackResource localRulestackResource = null;
        try {
            String localRulestackName = "localRulestack" + randomPadding();
            // @embedStart
            localRulestackResource = paloAltoNetworksNgfwManager.localRulestacks()
                .define(localRulestackName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withDefaultMode(DefaultMode.NONE)
                .withScope(ScopeType.LOCAL)
                .create();
            // @embedEnd
            localRulestackResource.refresh();
            Assertions.assertEquals(localRulestackResource.name(), localRulestackName);
            Assertions.assertEquals(localRulestackResource.name(), paloAltoNetworksNgfwManager.localRulestacks().getById(localRulestackResource.id()).name());
            Assertions.assertTrue(paloAltoNetworksNgfwManager.localRulestacks().list().stream().findAny().isPresent());
        } finally {
            if (localRulestackResource != null) {
                paloAltoNetworksNgfwManager.localRulestacks().deleteById(localRulestackResource.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
