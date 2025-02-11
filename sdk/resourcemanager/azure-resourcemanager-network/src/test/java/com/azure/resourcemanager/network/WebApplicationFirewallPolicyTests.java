// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.KnownWebApplicationGatewayManagedRuleSet;
import com.azure.resourcemanager.network.models.ManagedRuleEnabledState;
import com.azure.resourcemanager.network.models.ManagedRuleGroupOverride;
import com.azure.resourcemanager.network.models.ManagedRuleOverride;
import com.azure.resourcemanager.network.models.WebApplicationFirewallMode;
import com.azure.resourcemanager.network.models.WebApplicationFirewallPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class WebApplicationFirewallPolicyTests extends NetworkManagementTest {
    @Test
    public void canCrudWafPolicy() {
        String policyDefaultName = generateRandomResourceName("waf", 15);
        String policyName = generateRandomResourceName("waf", 15);
        Region region = Region.US_WEST;

        // waf policy with default settings
        WebApplicationFirewallPolicy defaultPolicy = networkManager.webApplicationFirewallPolicies()
            .define(policyDefaultName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet.MICROSOFT_DEFAULT_RULESET_2_1)
            .create();

        defaultPolicy.refresh();

        Assertions.assertTrue(defaultPolicy.getManagedRules()
            .managedRuleSets()
            .stream()
            .anyMatch(managedRuleSet -> managedRuleSet.ruleSetType()
                .equals(KnownWebApplicationGatewayManagedRuleSet.MICROSOFT_DEFAULT_RULESET_2_1.type())));
        Assertions.assertNotNull(defaultPolicy.getPolicySettings());
        Assertions.assertEquals(WebApplicationFirewallMode.DETECTION, defaultPolicy.mode());
        Assertions.assertTrue(defaultPolicy.isRequestBodyInspectionEnabled());
        Assertions.assertEquals(128, defaultPolicy.requestBodySizeLimitInKb());
        Assertions.assertEquals(100, defaultPolicy.fileUploadSizeLimitInMb());
        Assertions.assertTrue(defaultPolicy.isEnabled());

        // custom waf policy
        WebApplicationFirewallPolicy policy = networkManager.webApplicationFirewallPolicies()
            .define(policyName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet.OWASP_3_2,
                new ManagedRuleGroupOverride().withRuleGroupName("REQUEST-911-METHOD-ENFORCEMENT")
                    .withRules(Arrays.asList(
                        new ManagedRuleOverride().withRuleId("911012").withState(ManagedRuleEnabledState.DISABLED))))
            .withDetectionMode()
            .withBotProtection()
            .enableRequestBodyInspection()
            .withRequestBodySizeLimitInKb(128)
            .withFileUploadSizeLimitInMb(100)
            .create();

        policy.refresh();

        Assertions.assertEquals(WebApplicationFirewallMode.DETECTION, policy.mode());
        Assertions.assertTrue(policy.isRequestBodyInspectionEnabled());
        Assertions.assertEquals(128, policy.requestBodySizeLimitInKb());
        Assertions.assertEquals(100, policy.fileUploadSizeLimitInMb());
        Assertions.assertTrue(policy.isEnabled());
        Assertions.assertEquals("0.1",
            policy.getManagedRules()
                .managedRuleSets()
                .stream()
                .filter(managedRuleSet -> managedRuleSet.ruleSetType().equals("Microsoft_BotManagerRuleSet"))
                .findFirst()
                .get()
                .ruleSetVersion());
        Assertions.assertTrue(policy.getManagedRules()
            .managedRuleSets()
            .stream()
            .anyMatch(managedRuleSet -> managedRuleSet.ruleSetType()
                .equals(KnownWebApplicationGatewayManagedRuleSet.OWASP_3_2.type())));

        policy.update()
            .withPreventionMode()
            .withoutManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet.OWASP_3_2)
            .withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet.MICROSOFT_DEFAULT_RULESET_2_1,
                new ManagedRuleGroupOverride().withRuleGroupName("PROTOCOL-ENFORCEMENT"))
            .withBotProtection("1.0")
            .disableRequestBodyInspection()
            .disablePolicy()
            .apply();

        policy.refresh();

        Assertions.assertEquals(WebApplicationFirewallMode.PREVENTION, policy.mode());
        Assertions.assertFalse(policy.isRequestBodyInspectionEnabled());
        Assertions.assertFalse(policy.isEnabled());
        Assertions.assertEquals("1.0",
            policy.getManagedRules()
                .managedRuleSets()
                .stream()
                .filter(managedRuleSet -> managedRuleSet.ruleSetType().equals("Microsoft_BotManagerRuleSet"))
                .findFirst()
                .get()
                .ruleSetVersion());
        Assertions.assertTrue((policy.getManagedRules()
            .managedRuleSets()
            .stream()
            .noneMatch(managedRuleSet -> managedRuleSet.ruleSetType()
                .equals(KnownWebApplicationGatewayManagedRuleSet.OWASP_3_2.type()))));
        Assertions.assertTrue(policy.getManagedRules()
            .managedRuleSets()
            .stream()
            .anyMatch(managedRuleSet -> managedRuleSet.ruleSetType()
                .equals(KnownWebApplicationGatewayManagedRuleSet.MICROSOFT_DEFAULT_RULESET_2_1.type())
                && managedRuleSet.ruleGroupOverrides()
                    .stream()
                    .anyMatch(ruleGroupOverride -> "PROTOCOL-ENFORCEMENT".equals(ruleGroupOverride.ruleGroupName()))));

        // test deduplication
        policy.update()
            // no rule group override
            .withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet.MICROSOFT_DEFAULT_RULESET_2_1)
            .apply();

        Assertions.assertTrue(policy.getManagedRules()
            .managedRuleSets()
            .stream()
            .anyMatch(managedRuleSet -> managedRuleSet.ruleSetType()
                .equals(KnownWebApplicationGatewayManagedRuleSet.MICROSOFT_DEFAULT_RULESET_2_1.type())
                && managedRuleSet.ruleGroupOverrides()
                    .stream()
                    .noneMatch(ruleGroupOverride -> "PROTOCOL-ENFORCEMENT".equals(ruleGroupOverride.ruleGroupName()))));
    }
}
