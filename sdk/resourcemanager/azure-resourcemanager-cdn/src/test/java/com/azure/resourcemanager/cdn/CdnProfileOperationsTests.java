// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.cdn.models.CacheBehavior;
import com.azure.resourcemanager.cdn.models.CacheExpirationActionParameters;
import com.azure.resourcemanager.cdn.models.CacheType;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.cdn.models.DeliveryRule;
import com.azure.resourcemanager.cdn.models.DeliveryRuleCacheExpirationAction;
import com.azure.resourcemanager.cdn.models.DeliveryRuleHttpVersionCondition;
import com.azure.resourcemanager.cdn.models.DeliveryRuleRequestSchemeCondition;
import com.azure.resourcemanager.cdn.models.DestinationProtocol;
import com.azure.resourcemanager.cdn.models.HttpVersionMatchConditionParameters;
import com.azure.resourcemanager.cdn.models.HttpVersionOperator;
import com.azure.resourcemanager.cdn.models.RedirectType;
import com.azure.resourcemanager.cdn.models.RequestSchemeMatchConditionParameters;
import com.azure.resourcemanager.cdn.models.RequestSchemeMatchConditionParametersMatchValuesItem;
import com.azure.resourcemanager.cdn.models.UrlRedirectAction;
import com.azure.resourcemanager.cdn.models.UrlRedirectActionParameters;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

public class CdnProfileOperationsTests extends CdnManagementTest {

    private String rgName = "";
    private final Region region = Region.US_WEST;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateCdnProfile() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        CdnProfile cdnProfile = cdnManager.profiles().define(cdnProfileName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withStandardMicrosoftSku()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());

        boolean foundProfile = false;
        PagedIterable<CdnProfile> cdnProfiles = cdnManager.profiles().list();
        for (CdnProfile profile : cdnProfiles) {
            if (profile.name().equals(cdnProfileName)) {
                foundProfile = true;
            }
        }
        Assertions.assertTrue(foundProfile);
    }

    @Test
    public void canCreateUpdateCdnProfile() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);
        String cdnEndpointName = generateRandomResourceName("cdnendp", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        CdnProfile cdnProfile = cdnManager.profiles().define(cdnProfileName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withStandardMicrosoftSku()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());
        Assertions.assertEquals(0, cdnProfile.endpoints().size());

        cdnProfile.update()
            .defineNewEndpoint(cdnEndpointName)
                .withOrigin("origin1", "www.someDomain.net")
                .withHttpAllowed(true)
                .withHttpsAllowed(true)
                .attach()
            .apply();

        Map<String, CdnEndpoint> cdnEndpointMap = cdnProfile.endpoints();
        CdnEndpoint cdnEndpoint = cdnEndpointMap.get(cdnEndpointName);

        Assertions.assertNotNull(cdnEndpoint);
        Assertions.assertTrue(cdnEndpoint.isHttpAllowed());
        Assertions.assertTrue(cdnEndpoint.isHttpsAllowed());
        Assertions.assertFalse(cdnEndpoint.isCompressionEnabled());
        Assertions.assertEquals("www.someDomain.net", cdnEndpoint.originHostName());
    }

    @Test
    public void canCreateUpdateCdnEndpoint() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);
        String cdnEndpointName = generateRandomResourceName("cdnendp", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        CdnProfile cdnProfile = cdnManager.profiles().define(cdnProfileName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withStandardMicrosoftSku()
            .defineNewEndpoint(cdnEndpointName)
                .withOrigin("origin1", "www.someDomain.net")
                .withHttpAllowed(false)
                .withHttpsAllowed(true)
                .attach()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());

        Map<String, CdnEndpoint> cdnEndpointMap = cdnProfile.endpoints();
        Assertions.assertEquals(1, cdnProfile.endpoints().size());

        CdnEndpoint cdnEndpoint = cdnEndpointMap.get(cdnEndpointName);
        Assertions.assertNotNull(cdnEndpoint);
        Assertions.assertFalse(cdnEndpoint.isHttpAllowed());
        Assertions.assertTrue(cdnEndpoint.isHttpsAllowed());
        Assertions.assertEquals(0, cdnEndpoint.customDomains().size());

        cdnProfile.update()
            .updateEndpoint(cdnEndpointName)
                .withHttpAllowed(true)
                .withHttpsAllowed(false)
                .parent()
            .apply();

        cdnEndpoint.refresh();
        Assertions.assertTrue(cdnEndpoint.isHttpAllowed());
        Assertions.assertFalse(cdnEndpoint.isHttpsAllowed());
        Assertions.assertTrue(TestUtilities.getSize(cdnEndpoint.listResourceUsage()) > 0);
    }

    @Test
    public void canCrudStandardCdnEndpointDeliveryRules() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);
        String cdnEndpointName = generateRandomResourceName("cdnendp", 15);
        String cdnEndpointName2 = generateRandomResourceName("cdnendp", 15);
        String ruleName1 = generateRandomResourceName("cdndr", 15);
        String ruleName2 = generateRandomResourceName("cdndr", 15);
        String ruleName3 = generateRandomResourceName("cdndr", 15);
        String originName1 = generateRandomResourceName("origin", 15);
        String originName2 = generateRandomResourceName("origin", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        // create cdnProfile with one cdnEndpoint, with 1 deliveryRule
        CdnProfile cdnProfile = cdnManager.profiles().define(cdnProfileName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withStandardMicrosoftSku()
            .defineNewEndpoint(cdnEndpointName)
                .withOrigin(originName1, "www.someDomain.net")
                .withHttpAllowed(false)
                .withHttpsAllowed(true)
                .defineNewDeliveryRule(ruleName1)
                    .withOrder(1)
                    .withMatchConditions(
                        new DeliveryRuleRequestSchemeCondition()
                            .withParameters(
                                new RequestSchemeMatchConditionParameters()
                                    .withMatchValues(
                                        Arrays.asList(RequestSchemeMatchConditionParametersMatchValuesItem.HTTP))))
                    .withActions(
                        new UrlRedirectAction()
                            .withParameters(
                                new UrlRedirectActionParameters()
                                    .withRedirectType(RedirectType.FOUND)
                                    .withDestinationProtocol(DestinationProtocol.HTTPS)
                                    .withCustomHostname("")))
                    .attach()
                .attach()
            .create();

        cdnProfile.refresh();

        CdnEndpoint endpoint = cdnProfile.endpoints().get(cdnEndpointName);
        Assertions.assertNotNull(endpoint);
        Assertions.assertEquals(1, endpoint.deliveryRules().size());
        DeliveryRule deliveryRule = endpoint.deliveryRules().get(ruleName1);
        Assertions.assertNotNull(deliveryRule);
        Assertions.assertEquals(1, deliveryRule.conditions().size());
        Assertions.assertEquals(1, deliveryRule.actions().size());
        Assertions.assertTrue(deliveryRule.conditions().iterator().next() instanceof DeliveryRuleRequestSchemeCondition);
        Assertions.assertTrue(deliveryRule.actions().iterator().next() instanceof UrlRedirectAction);
        Assertions.assertEquals(1, deliveryRule.order());

        // update cdnProfile, add 1 additional deliveryRule, update existing deliveryRule
        cdnProfile
            .update()
            .updateEndpoint(cdnEndpointName)
            // define new deliveryRule
            .defineNewDeliveryRule(ruleName2)
                .withOrder(1)
                .withMatchConditions(
                    new DeliveryRuleHttpVersionCondition()
                        .withParameters(
                            new HttpVersionMatchConditionParameters()
                                .withOperator(HttpVersionOperator.EQUAL)
                                .withMatchValues(Arrays.asList("2.0"))))
                .withActions(
                    new DeliveryRuleCacheExpirationAction()
                        .withParameters(
                            new CacheExpirationActionParameters()
                                .withCacheType(CacheType.ALL)
                                .withCacheBehavior(CacheBehavior.BYPASS_CACHE)))
                .attach()
            // update existing deliveryRule
            .updateDeliveryRule(ruleName1)
                .withOrder(2)
                .parent()
            .parent()
            // define new endpoint
            .defineNewEndpoint(cdnEndpointName2)
                .withOrigin(originName2, "www.someDomain.net")
                .withHttpAllowed(false)
                .withHttpsAllowed(true)
                .defineNewDeliveryRule(ruleName3)
                    .withOrder(1)
                    .withMatchConditions(
                        new DeliveryRuleHttpVersionCondition()
                            .withParameters(
                                new HttpVersionMatchConditionParameters()
                                    .withOperator(HttpVersionOperator.EQUAL)
                                    .withMatchValues(Arrays.asList("1.1"))))
                    .withActions(
                        new DeliveryRuleCacheExpirationAction()
                            .withParameters(
                                new CacheExpirationActionParameters()
                                    .withCacheType(CacheType.ALL)
                                    .withCacheDuration("00:05:00")
                                    .withCacheBehavior(CacheBehavior.OVERRIDE)))
                    .attach()
            .attach()
        .apply();

        cdnProfile.refresh();

        // endpoint1
        endpoint = cdnProfile.endpoints().get(cdnEndpointName);
        Assertions.assertNotNull(endpoint);
        Assertions.assertEquals(2, endpoint.deliveryRules().size());

        // rule1
        deliveryRule = endpoint.deliveryRules().get(ruleName1);
        Assertions.assertNotNull(deliveryRule);
        Assertions.assertEquals(2, deliveryRule.order());

        // rule2
        DeliveryRule deliveryRule2 = endpoint.deliveryRules().get(ruleName2);
        Assertions.assertNotNull(deliveryRule2);
        Assertions.assertEquals(1, deliveryRule2.conditions().size());
        Assertions.assertEquals(1, deliveryRule2.actions().size());
        Assertions.assertTrue(deliveryRule2.conditions().iterator().next() instanceof DeliveryRuleHttpVersionCondition);
        Assertions.assertTrue(deliveryRule2.actions().iterator().next() instanceof DeliveryRuleCacheExpirationAction);
        Assertions.assertEquals(1, deliveryRule2.order());

        // endpoint2
        CdnEndpoint endpoint2 = cdnProfile.endpoints().get(cdnEndpointName2);
        Assertions.assertNotNull(endpoint2);

        // rule3
        DeliveryRule deliveryRule3 = endpoint2.deliveryRules().get(ruleName3);
        Assertions.assertNotNull(deliveryRule3);

        cdnProfile.update()
            .updateEndpoint(cdnEndpointName)
                .withoutDeliveryRule(ruleName1)
                .parent()
            .apply();

        cdnProfile.refresh();

        Assertions.assertEquals(1, cdnProfile.endpoints().get(cdnEndpointName).deliveryRules().size());
    }
}
