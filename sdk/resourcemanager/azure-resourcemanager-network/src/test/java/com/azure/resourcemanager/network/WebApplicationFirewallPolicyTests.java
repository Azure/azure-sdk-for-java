// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.WebApplicationFirewallMode;
import com.azure.resourcemanager.network.models.WebApplicationFirewallPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebApplicationFirewallPolicyTests extends NetworkManagementTest {
    @Test
    public void canCrudWafPolicy() {
        String policyName = generateRandomResourceName("waf", 15);
        Region region = Region.US_WEST;

        WebApplicationFirewallPolicy policy =
            networkManager.webApplicationFirewallPolicies()
                .define(policyName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withMode(WebApplicationFirewallMode.DETECTION)
                .enablePolicy()
                .withBotDetection("1.0")
                .withInspectRequestBody()
                .withRequestBodySizeLimitInKb(128)
                .withFileUploadSizeLimitInMb(100)
                .create();

        Assertions.assertEquals(WebApplicationFirewallMode.DETECTION, policy.mode());
        Assertions.assertTrue(policy.isInspectRequestBodyEnabled());
        Assertions.assertEquals(128, policy.requestBodySizeLimitInKb());
        Assertions.assertEquals(100, policy.fileUploadSizeLimitInMb());
        Assertions.assertTrue(policy.isEnabled());

        policy.update()
            .withMode(WebApplicationFirewallMode.PREVENTION)
            .withBotDetection("0.1")
            .withoutInspectRequestBody()
            .apply();

        Assertions.assertEquals(WebApplicationFirewallMode.PREVENTION, policy.mode());
        Assertions.assertfalse(policy.isInspectRequestBodyEnabled());
        Assertions.assertEquals(0, policy.requestBOdySizeLimitInKb());
        Assertions.assertEquals(0, policy.fileUploadSizeLimitInMb());
    }
}
