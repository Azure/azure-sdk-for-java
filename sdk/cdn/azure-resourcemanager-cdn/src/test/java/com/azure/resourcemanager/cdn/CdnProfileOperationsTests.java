// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.cdn.models.AfdEndpoint;
import com.azure.resourcemanager.cdn.models.AfdEndpointProtocols;
import com.azure.resourcemanager.cdn.models.Origin;
import com.azure.resourcemanager.cdn.models.OriginGroup;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.cdn.models.DeliveryRuleHttpVersionCondition;
import com.azure.resourcemanager.cdn.models.DeliveryRuleRequestHeaderAction;
import com.azure.resourcemanager.cdn.models.DeliveryRuleRequestSchemeCondition;
import com.azure.resourcemanager.cdn.models.DestinationProtocol;
import com.azure.resourcemanager.cdn.models.EnabledState;
import com.azure.resourcemanager.cdn.models.EnforceMtlsEnabledState;
import com.azure.resourcemanager.cdn.models.ForwardingProtocol;
import com.azure.resourcemanager.cdn.models.HeaderAction;
import com.azure.resourcemanager.cdn.models.HeaderActionParameters;
import com.azure.resourcemanager.cdn.models.HttpVersionMatchConditionParameters;
import com.azure.resourcemanager.cdn.models.HttpVersionOperator;
import com.azure.resourcemanager.cdn.models.HttpsRedirect;
import com.azure.resourcemanager.cdn.models.LinkToDefaultDomain;
import com.azure.resourcemanager.cdn.models.LoadBalancingSettingsParameters;
import com.azure.resourcemanager.cdn.models.MatchProcessingBehavior;
import com.azure.resourcemanager.cdn.models.RedirectType;
import com.azure.resourcemanager.cdn.models.RequestSchemeMatchConditionParameters;
import com.azure.resourcemanager.cdn.models.RequestSchemeMatchConditionParametersMatchValuesItem;
import com.azure.resourcemanager.cdn.models.Route;
import com.azure.resourcemanager.cdn.models.Rule;
import com.azure.resourcemanager.cdn.models.RuleSet;
import com.azure.resourcemanager.cdn.models.SkuName;
import com.azure.resourcemanager.cdn.models.UrlRedirectAction;
import com.azure.resourcemanager.cdn.models.UrlRedirectActionParameters;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        CdnProfile cdnProfile = cdnManager.profiles()
            .define(cdnProfileName)
            .withGlobal()
            .withExistingResourceGroup(resourceGroup)
            .withSku(SkuName.STANDARD_AZURE_FRONT_DOOR)
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

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        CdnProfile cdnProfile = cdnManager.profiles()
            .define(cdnProfileName)
            .withGlobal()
            .withExistingResourceGroup(resourceGroup)
            .withSku(SkuName.STANDARD_AZURE_FRONT_DOOR)
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());
        Assertions.assertEquals(0, cdnProfile.endpoints().size());

        cdnProfile.update()
            .defineAfdEndpoint(cdnEndpointName)
            .withEnabledState(EnabledState.ENABLED)
            .withEnforceMtls(EnforceMtlsEnabledState.ENABLED)
            .attach()
            .defineOriginGroup("originGroup1")
            .withLoadBalancingSettings(
                new LoadBalancingSettingsParameters().withSampleSize(5).withSuccessfulSamplesRequired(3))
            .withSessionAffinityState(EnabledState.ENABLED)
            .attach()
            .apply();

        // add origin to origin group
        cdnProfile.update()
            .updateOriginGroup("originGroup1")
            .defineOrigin("origin1")
            .withHostname("www.somedomain.net")
            .withEnabledState(EnabledState.ENABLED)
            .withHttpPort(80)
            .attach()
            .parent()
            .apply();

        Map<String, AfdEndpoint> cdnEndpointMap = cdnProfile.afdEndpoints();
        AfdEndpoint cdnEndpoint = cdnEndpointMap.get(cdnEndpointName);

        Assertions.assertNotNull(cdnEndpoint);
        Assertions.assertEquals(EnabledState.ENABLED, cdnEndpoint.enabledState());
        Assertions.assertEquals(EnforceMtlsEnabledState.ENABLED, cdnEndpoint.enforceMtls());

        OriginGroup originGroup = cdnProfile.originGroups().get("originGroup1");
        Assertions.assertNotNull(originGroup);
        Assertions.assertEquals(EnabledState.ENABLED, originGroup.sessionAffinityState());
        Assertions.assertEquals(3, originGroup.loadBalancingSettings().successfulSamplesRequired());

        Origin origin = originGroup.origins().get("origin1");
        Assertions.assertNotNull(origin);
        Assertions.assertEquals("www.somedomain.net", origin.hostname());
        Assertions.assertEquals(EnabledState.ENABLED, origin.enabledState());
        Assertions.assertEquals(80, origin.httpPort());
    }

    @Test
    public void canCreateUpdateCdnEndpoint() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);
        String cdnEndpointName = generateRandomResourceName("cdnendp", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        // create profile with endpoint, origin group, and origins
        CdnProfile cdnProfile = cdnManager.profiles()
            .define(cdnProfileName)
            .withGlobal()
            .withExistingResourceGroup(resourceGroup)
            .withSku(SkuName.STANDARD_AZURE_FRONT_DOOR)
            .defineAfdEndpoint(cdnEndpointName)
            .withEnabledState(EnabledState.ENABLED)
            .withEnforceMtls(EnforceMtlsEnabledState.ENABLED)
            .attach()
            .defineOriginGroup("originGroup1")
            .withLoadBalancingSettings(
                new LoadBalancingSettingsParameters().withSampleSize(4).withSuccessfulSamplesRequired(2))
            .withSessionAffinityState(EnabledState.ENABLED)
            .defineOrigin("origin1")
            .withHostname("www.domain1.net")
            .withEnabledState(EnabledState.ENABLED)
            .withHttpPort(80)
            .attach()
            .defineOrigin("origin2")
            .withHostname("www.domain2.net")
            .withEnabledState(EnabledState.ENABLED)
            .withHttpPort(8080)
            .attach()
            .attach()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());

        // verify endpoint
        Map<String, AfdEndpoint> cdnEndpointMap = cdnProfile.afdEndpoints();
        Assertions.assertEquals(1, cdnEndpointMap.size());

        AfdEndpoint cdnEndpoint = cdnEndpointMap.get(cdnEndpointName);
        Assertions.assertNotNull(cdnEndpoint);
        Assertions.assertEquals(EnabledState.ENABLED, cdnEndpoint.enabledState());
        Assertions.assertEquals(EnforceMtlsEnabledState.ENABLED, cdnEndpoint.enforceMtls());

        // verify origin group
        OriginGroup originGroup1 = cdnProfile.originGroups().get("originGroup1");
        Assertions.assertNotNull(originGroup1);
        Assertions.assertEquals(EnabledState.ENABLED, originGroup1.sessionAffinityState());
        Assertions.assertEquals(4, originGroup1.loadBalancingSettings().sampleSize());
        Assertions.assertEquals(2, originGroup1.loadBalancingSettings().successfulSamplesRequired());

        // verify origins
        Origin origin1 = originGroup1.origins().get("origin1");
        Assertions.assertNotNull(origin1);
        Assertions.assertEquals("www.domain1.net", origin1.hostname());
        Assertions.assertEquals(80, origin1.httpPort());

        Origin origin2 = originGroup1.origins().get("origin2");
        Assertions.assertNotNull(origin2);
        Assertions.assertEquals("www.domain2.net", origin2.hostname());
        Assertions.assertEquals(8080, origin2.httpPort());

        // update endpoint, origin group, and origin
        cdnProfile.update()
            .updateAfdEndpoint(cdnEndpointName)
            .withEnabledState(EnabledState.DISABLED)
            .withEnforceMtls(EnforceMtlsEnabledState.DISABLED)
            .parent()
            .updateOriginGroup("originGroup1")
            .withSessionAffinityState(EnabledState.DISABLED)
            .withLoadBalancingSettings(
                new LoadBalancingSettingsParameters().withSampleSize(6).withSuccessfulSamplesRequired(4))
            .updateOrigin("origin1")
            .withHostname("www.updated-domain1.net")
            .withHttpPort(8081)
            .parent()
            .parent()
            .apply();

        cdnEndpoint.refresh();
        Assertions.assertEquals(EnabledState.DISABLED, cdnEndpoint.enabledState());
        Assertions.assertEquals(EnforceMtlsEnabledState.DISABLED, cdnEndpoint.enforceMtls());

        originGroup1.refresh();
        Assertions.assertEquals(EnabledState.DISABLED, originGroup1.sessionAffinityState());
        Assertions.assertEquals(6, originGroup1.loadBalancingSettings().sampleSize());
        Assertions.assertEquals(4, originGroup1.loadBalancingSettings().successfulSamplesRequired());

        origin1 = originGroup1.origins().get("origin1");
        Assertions.assertEquals("www.updated-domain1.net", origin1.hostname());
        Assertions.assertEquals(8081, origin1.httpPort());

        // remove origin from origin group
        cdnProfile.update().updateOriginGroup("originGroup1").withoutOrigin("origin2").parent().apply();

        originGroup1.refresh();
        Assertions.assertNull(originGroup1.origins().get("origin2"));
        Assertions.assertNotNull(originGroup1.origins().get("origin1"));

        // add a new origin group
        cdnProfile.update()
            .defineOriginGroup("originGroup2")
            .withLoadBalancingSettings(
                new LoadBalancingSettingsParameters().withSampleSize(3).withSuccessfulSamplesRequired(2))
            .attach()
            .apply();

        // add origin to the new origin group
        cdnProfile.update()
            .updateOriginGroup("originGroup2")
            .defineOrigin("origin3")
            .withHostname("www.domain3.net")
            .withEnabledState(EnabledState.ENABLED)
            .withHttpPort(80)
            .attach()
            .parent()
            .apply();

        OriginGroup originGroup2 = cdnProfile.originGroups().get("originGroup2");
        Assertions.assertNotNull(originGroup2);
        Assertions.assertEquals(3, originGroup2.loadBalancingSettings().sampleSize());
        Origin origin3 = originGroup2.origins().get("origin3");
        Assertions.assertNotNull(origin3);
        Assertions.assertEquals("www.domain3.net", origin3.hostname());

        // remove origin group
        cdnProfile.update().withoutOriginGroup("originGroup2").apply();

        Assertions.assertNull(cdnProfile.originGroups().get("originGroup2"));

        // remove endpoint
        cdnProfile.update().withoutAfdEndpoint(cdnEndpointName).apply();

        cdnEndpointMap = cdnProfile.afdEndpoints();
        Assertions.assertTrue(cdnEndpointMap.isEmpty());
    }

    @Test
    public void canCrudAfdResources() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);
        String cdnEndpointName = generateRandomResourceName("cdnendp", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        // Step 1: Create AFD profile with origin group (+ origin) and rule set (+ 2 rules).
        // Route is not included yet — it requires the origin group resource ID which is assigned after creation.
        CdnProfile cdnProfile = cdnManager.profiles()
            .define(cdnProfileName)
            .withGlobal()
            .withExistingResourceGroup(resourceGroup)
            .withSku(SkuName.STANDARD_AZURE_FRONT_DOOR)
            .defineAfdEndpoint(cdnEndpointName)
            .withEnabledState(EnabledState.ENABLED)
            .attach()
            .defineOriginGroup("originGroup1")
            .withLoadBalancingSettings(
                new LoadBalancingSettingsParameters().withSampleSize(4).withSuccessfulSamplesRequired(2))
            .defineOrigin("origin1")
            .withHostname("www.domain1.net")
            .withEnabledState(EnabledState.ENABLED)
            .withHttpPort(80)
            .attach()
            .attach()
            .defineRuleSet("ruleSet1")
            .defineRule("rule1")
            .withOrder(1)
            .withActions(Arrays.asList(new DeliveryRuleRequestHeaderAction()
                .withParameters(new HeaderActionParameters().withHeaderAction(HeaderAction.OVERWRITE)
                    .withHeaderName("X-CDN")
                    .withValue("true"))))
            .withConditions(Arrays.asList(
                new DeliveryRuleRequestSchemeCondition().withParameters(new RequestSchemeMatchConditionParameters()
                    .withMatchValues(Arrays.asList(RequestSchemeMatchConditionParametersMatchValuesItem.HTTPS)))))
            .withMatchProcessingBehavior(MatchProcessingBehavior.CONTINUE)
            .attach()
            .defineRule("rule2")
            .withOrder(2)
            .withActions(Arrays.asList(new UrlRedirectAction()
                .withParameters(new UrlRedirectActionParameters().withRedirectType(RedirectType.FOUND)
                    .withDestinationProtocol(DestinationProtocol.HTTPS))))
            .withConditions(Arrays.asList(new DeliveryRuleHttpVersionCondition()
                .withParameters(new HttpVersionMatchConditionParameters().withOperator(HttpVersionOperator.EQUAL)
                    .withMatchValues(Arrays.asList("2.0")))))
            .attach()
            .attach()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());

        // verify endpoint
        Map<String, AfdEndpoint> endpointMap = cdnProfile.afdEndpoints();
        Assertions.assertEquals(1, endpointMap.size());
        AfdEndpoint endpoint = endpointMap.get(cdnEndpointName);
        Assertions.assertNotNull(endpoint);
        Assertions.assertEquals(EnabledState.ENABLED, endpoint.enabledState());

        // verify origin group and origin
        OriginGroup originGroup1 = cdnProfile.originGroups().get("originGroup1");
        Assertions.assertNotNull(originGroup1);
        Assertions.assertEquals(4, originGroup1.loadBalancingSettings().sampleSize());
        Assertions.assertEquals(2, originGroup1.loadBalancingSettings().successfulSamplesRequired());

        Origin origin1 = originGroup1.origins().get("origin1");
        Assertions.assertNotNull(origin1);
        Assertions.assertEquals("www.domain1.net", origin1.hostname());

        // verify rule set and rules
        RuleSet ruleSet1 = cdnProfile.ruleSets().get("ruleSet1");
        Assertions.assertNotNull(ruleSet1);
        Assertions.assertEquals(2, ruleSet1.rules().size());

        Rule rule1 = ruleSet1.rules().get("rule1");
        Assertions.assertNotNull(rule1);
        Assertions.assertEquals(1, rule1.order());
        Assertions.assertEquals(1, rule1.actions().size());
        Assertions.assertTrue(rule1.actions().get(0) instanceof DeliveryRuleRequestHeaderAction);
        Assertions.assertEquals(1, rule1.conditions().size());
        Assertions.assertTrue(rule1.conditions().get(0) instanceof DeliveryRuleRequestSchemeCondition);
        Assertions.assertEquals(MatchProcessingBehavior.CONTINUE, rule1.matchProcessingBehavior());

        Rule rule2 = ruleSet1.rules().get("rule2");
        Assertions.assertNotNull(rule2);
        Assertions.assertEquals(2, rule2.order());
        Assertions.assertTrue(rule2.actions().get(0) instanceof UrlRedirectAction);
        Assertions.assertTrue(rule2.conditions().get(0) instanceof DeliveryRuleHttpVersionCondition);

        // Step 2: Add a route to the existing endpoint.
        // The route references the origin group and rule set by their resource IDs.
        String originGroup1Id = originGroup1.id();
        String ruleSet1Id = ruleSet1.id();

        cdnProfile.update()
            .updateAfdEndpoint(cdnEndpointName)
            .defineRoute("route1")
            .withOriginGroupResourceId(originGroup1Id)
            .withPatternsToMatch(Arrays.asList("/*"))
            .withSupportedProtocols(Arrays.asList(AfdEndpointProtocols.HTTPS))
            .withHttpsRedirect(HttpsRedirect.ENABLED)
            .withForwardingProtocol(ForwardingProtocol.HTTPS_ONLY)
            .withLinkToDefaultDomain(LinkToDefaultDomain.ENABLED)
            .withRuleSetResourceIds(Arrays.asList(ruleSet1Id))
            .withEnabledState(EnabledState.ENABLED)
            .attach()
            .parent()
            .apply();

        // verify route on the endpoint
        endpoint = cdnProfile.afdEndpoints().get(cdnEndpointName);
        Map<String, Route> routeMap = endpoint.routes();
        Assertions.assertEquals(1, routeMap.size());

        Route route1 = routeMap.get("route1");
        Assertions.assertNotNull(route1);
        Assertions.assertEquals(originGroup1Id, route1.originGroupResourceId());
        Assertions.assertEquals(Arrays.asList("/*"), route1.patternsToMatch());
        Assertions.assertEquals(HttpsRedirect.ENABLED, route1.httpsRedirect());
        Assertions.assertEquals(ForwardingProtocol.HTTPS_ONLY, route1.forwardingProtocol());
        Assertions.assertEquals(LinkToDefaultDomain.ENABLED, route1.linkToDefaultDomain());
        Assertions.assertEquals(1, route1.ruleSetResourceIds().size());
        Assertions.assertEquals(ruleSet1Id, route1.ruleSetResourceIds().get(0));
        Assertions.assertEquals(EnabledState.ENABLED, route1.enabledState());

        // Step 3: Update the route — change forwarding protocol
        cdnProfile.update()
            .updateAfdEndpoint(cdnEndpointName)
            .updateRoute("route1")
            .withForwardingProtocol(ForwardingProtocol.MATCH_REQUEST)
            .parent()
            .parent()
            .apply();

        route1 = cdnProfile.afdEndpoints().get(cdnEndpointName).routes().get("route1");
        Assertions.assertEquals(ForwardingProtocol.MATCH_REQUEST, route1.forwardingProtocol());

        // Step 4: Update an existing rule — change order and match processing behavior
        cdnProfile.update()
            .updateRuleSet("ruleSet1")
            .updateRule("rule1")
            .withOrder(3)
            .withMatchProcessingBehavior(MatchProcessingBehavior.STOP)
            .parent()
            .parent()
            .apply();

        rule1 = cdnProfile.ruleSets().get("ruleSet1").rules().get("rule1");
        Assertions.assertEquals(3, rule1.order());
        Assertions.assertEquals(MatchProcessingBehavior.STOP, rule1.matchProcessingBehavior());

        // Step 5: Remove rule2 from the rule set
        cdnProfile.update().updateRuleSet("ruleSet1").withoutRule("rule2").parent().apply();

        ruleSet1 = cdnProfile.ruleSets().get("ruleSet1");
        Assertions.assertEquals(1, ruleSet1.rules().size());
        Assertions.assertNotNull(ruleSet1.rules().get("rule1"));
        Assertions.assertNull(ruleSet1.rules().get("rule2"));

        // Step 6: Remove the route from the endpoint
        cdnProfile.update().updateAfdEndpoint(cdnEndpointName).withoutRoute("route1").parent().apply();

        Assertions.assertTrue(cdnProfile.afdEndpoints().get(cdnEndpointName).routes().isEmpty());

        // Step 7: Remove rule set, origin group, and endpoints
        cdnProfile.update().withoutRuleSet("ruleSet1").apply();
        Assertions.assertNull(cdnProfile.ruleSets().get("ruleSet1"));

        cdnProfile.update().withoutOriginGroup("originGroup1").apply();
        Assertions.assertNull(cdnProfile.originGroups().get("originGroup1"));

        cdnProfile.update().withoutAfdEndpoint(cdnEndpointName).apply();
        Assertions.assertTrue(cdnProfile.afdEndpoints().isEmpty());
    }

    @Disabled("Domain registration is deprecated from appservice")
    @Test
    public void canCreateWithCustomDomain() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);
        String cdnEndpointName = generateRandomResourceName("cdnendp", 15);
        String domainName = generateRandomResourceName("jsdkcdn", 15) + ".com";
        String cname1 = "c1";
        String cname2 = "c2";
        String customDomain1 = cname1 + "." + domainName;
        String customDomain2 = cname2 + "." + domainName;

        // purchase domain
        AppServiceDomain domain = appServiceManager.domains()
            .define(domainName)
            .withExistingResourceGroup(resourceManager.resourceGroups().define(rgName).withRegion(region).create())
            .defineRegistrantContact()
            .withFirstName("Jon")
            .withLastName("Doe")
            .withEmail("jondoe@contoso.com")
            .withAddressLine1("123 4th Ave")
            .withCity("Redmond")
            .withStateOrProvince("WA")
            .withCountry(CountryIsoCode.UNITED_STATES)
            .withPostalCode("98052")
            .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
            .withPhoneNumber("4258828080")
            .attach()
            .withDomainPrivacyEnabled(true)
            .withAutoRenewEnabled(false)
            .create();
        // create cname record for the custom domains and cdn endpoint
        DnsZone dnsZone = dnsZoneManager.zones()
            .define(domainName)
            .withExistingResourceGroup(rgName)
            .withCNameRecordSet(cname1, cdnEndpointName + ".azureedge.net")
            .withCNameRecordSet(cname2, cdnEndpointName + ".azureedge.net")
            .create();

        CdnProfile cdnProfile = cdnManager.profiles()
            .define(cdnProfileName)
            .withGlobal()
            .withNewResourceGroup(rgName)
            .withSku(SkuName.STANDARD_AZURE_FRONT_DOOR)
            .defineNewEndpoint(cdnEndpointName)
            .withOrigin("origin1", "www.someDomain.net")
            .withHttpAllowed(false)
            .withHttpsAllowed(true)
            .withCustomDomain(customDomain1)
            .withCustomDomain(customDomain2)
            .attach()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());

        Map<String, CdnEndpoint> cdnEndpointMap = cdnProfile.endpoints();
        Assertions.assertEquals(1, cdnProfile.endpoints().size());

        CdnEndpoint cdnEndpoint = cdnEndpointMap.get(cdnEndpointName);
        Assertions.assertEquals(2, cdnEndpoint.customDomains().size());

        // delete the cname of custom domain1
        // Starting from April 9th 2021, Azure CDN requires removal of the CNAME records to Azure CDN endpoints before the resources can be deleted.
        // Resources include Azure CDN custom domains, Azure CDN profiles/endpoints or Azure resource groups that has Azure CDN custom domain(s) enabled.
        // https://learn.microsoft.com/answers/questions/1189452/trying-to-delete-cdn-endpoint-custom-domain
        dnsZone.update().withoutCNameRecordSet(cname1).apply();

        // remove custom domain
        cdnProfile.update().updateEndpoint(cdnEndpointName).withoutCustomDomain(customDomain1).parent().apply();

        cdnEndpoint.refresh();
        Assertions.assertEquals(1, cdnEndpoint.customDomains().size());
    }
}
