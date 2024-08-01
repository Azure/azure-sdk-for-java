// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayFirewallDisabledRuleGroup;
import com.azure.resourcemanager.network.models.ApplicationGatewayFirewallExclusion;
import com.azure.resourcemanager.network.models.ApplicationGatewayFirewallMode;
import com.azure.resourcemanager.network.models.ApplicationGatewaySkuName;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslCipherSuite;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslPolicy;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslPolicyName;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslPolicyType;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslProtocol;
import com.azure.resourcemanager.network.models.ApplicationGatewayTier;
import com.azure.resourcemanager.network.models.ApplicationGatewayWebApplicationFirewallConfiguration;
import com.azure.resourcemanager.network.models.KnownWebApplicationGatewayManagedRuleSet;
import com.azure.resourcemanager.network.models.ManagedServiceIdentity;
import com.azure.resourcemanager.network.models.ManagedServiceIdentityUserAssignedIdentities;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.ResourceIdentityType;
import com.azure.resourcemanager.network.models.WebApplicationFirewallMode;
import com.azure.resourcemanager.network.models.WebApplicationFirewallPolicy;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationGatewayTests extends NetworkManagementTest {

    private static final Region REGION = Region.US_EAST;

    @Test
    public void canCRUDApplicationGatewayWithWAF() throws Exception {
        String appGatewayName = generateRandomResourceName("agwaf", 15);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                // Request routing rules
                .defineRequestRoutingRule("rule1")
                .fromPublicFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .toBackendIPAddress("11.1.1.2")
                .attach()
                .withExistingPublicIpAddress(pip)
                .withTier(ApplicationGatewayTier.WAF_V2)
                .withSize(ApplicationGatewaySkuName.WAF_V2)
                .withAutoScale(2, 5)
                .withWebApplicationFirewall(true, ApplicationGatewayFirewallMode.PREVENTION)
                .create();

        Assertions.assertNotNull(appGateway);
        Assertions.assertEquals(ApplicationGatewayTier.WAF_V2, appGateway.tier());
        Assertions.assertEquals(ApplicationGatewaySkuName.WAF_V2, appGateway.size());
        Assertions.assertEquals(2, appGateway.autoscaleConfiguration().minCapacity());
        Assertions.assertEquals(5, (int) appGateway.autoscaleConfiguration().maxCapacity());

        ApplicationGatewayWebApplicationFirewallConfiguration config = appGateway.webApplicationFirewallConfiguration();
        config.withFileUploadLimitInMb(200);
        config
            .withDisabledRuleGroups(
                Arrays
                    .asList(
                        new ApplicationGatewayFirewallDisabledRuleGroup()
                            .withRuleGroupName("REQUEST-943-APPLICATION-ATTACK-SESSION-FIXATION")));
        config.withRequestBodyCheck(true);
        config.withMaxRequestBodySizeInKb(64);
        config
            .withExclusions(
                Arrays
                    .asList(
                        new ApplicationGatewayFirewallExclusion()
                            .withMatchVariable("RequestHeaderNames")
                            .withSelectorMatchOperator("StartsWith")
                            .withSelector("User-Agent")));
        appGateway.update().withWebApplicationFirewall(config).apply();

        appGateway.refresh();

        // Verify WAF
        Assertions.assertEquals(200, (int) appGateway.webApplicationFirewallConfiguration().fileUploadLimitInMb());
        Assertions.assertTrue(appGateway.webApplicationFirewallConfiguration().requestBodyCheck());
        Assertions.assertEquals(64, (int) appGateway.webApplicationFirewallConfiguration().maxRequestBodySizeInKb());

        Assertions.assertEquals(1, appGateway.webApplicationFirewallConfiguration().exclusions().size());

        Assertions.assertEquals(
            "RequestHeaderNames",
            appGateway.webApplicationFirewallConfiguration().exclusions().get(0).matchVariable());
        Assertions.assertEquals(
            "StartsWith",
            appGateway.webApplicationFirewallConfiguration().exclusions().get(0).selectorMatchOperator());
        Assertions.assertEquals(
            "User-Agent",
            appGateway.webApplicationFirewallConfiguration().exclusions().get(0).selector());

        Assertions.assertEquals(1, appGateway.webApplicationFirewallConfiguration().disabledRuleGroups().size());
        Assertions.assertEquals(
            "REQUEST-943-APPLICATION-ATTACK-SESSION-FIXATION",
            appGateway.webApplicationFirewallConfiguration().disabledRuleGroups().get(0).ruleGroupName());
    }

    @Test
    public void canSpecifyWildcardListeners() {
        String appGatewayName = generateRandomResourceName("agwaf", 15);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        String listener1 = "listener1";
        // regular hostname
        String hostname1 = "my.contoso.com";
        ApplicationGateway gateway = networkManager.applicationGateways()
            .define(appGatewayName)
            .withRegion(REGION)
            .withExistingResourceGroup(rgName)

            // Request routing rules
            .defineRequestRoutingRule("rule80")
            .fromPublicFrontend()
            .fromFrontendHttpPort(80)
            .toBackendHttpPort(8080)
            .toBackendIPAddress("11.1.1.1")
            .toBackendIPAddress("11.1.1.2")
            .withCookieBasedAffinity()
            .attach()

            // Additional/explicit frontend listeners
            .defineListener(listener1)
            .withPublicFrontend()
            .withFrontendPort(9000)
            .withHttp()
            .withHostname(hostname1)
            .attach()

            .withTier(ApplicationGatewayTier.WAF_V2)
            .withSize(ApplicationGatewaySkuName.WAF_V2)
            .withAutoScale(2, 5)
            .withExistingPublicIpAddress(pip)
            .create();

        Assertions.assertEquals(hostname1, gateway.listeners().get(listener1).hostname());

        // wildcard hostname
        String hostname2 = "*.contoso.com";
        gateway.update()
            .updateListener(listener1)
            .withHostname(hostname2)
            .parent()
            .apply();

        Assertions.assertEquals(hostname2, gateway.listeners().get(listener1).hostname());

        // multiple host names, mixed regular and wildcard
        List<String> hostnames = new ArrayList<>();
        hostnames.add(hostname1);
        hostnames.add(hostname2);

        gateway.update()
            .updateListener(listener1)
            .withHostnames(hostnames)
            .parent()
            .apply();

        Assertions.assertEquals(hostnames, gateway.listeners().get(listener1).hostnames());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canCreateApplicationGatewayWithSecret() throws Exception {
        String appGatewayName = generateRandomResourceName("agwaf", 15);
        String identityName = generateRandomResourceName("id", 10);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        Identity identity =
            msiManager
                .identities()
                .define(identityName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                .create();

        Assertions.assertNotNull(identity.name());
        Assertions.assertNotNull(identity.principalId());

        Secret secret1 = createKeyVaultSecret(azureCliSignedInUser().userPrincipalName(),
            identity.principalId());
        Secret secret2 = createKeyVaultSecret(azureCliSignedInUser().userPrincipalName(),
            identity.principalId());

        ManagedServiceIdentity serviceIdentity = createManagedServiceIdentityFromIdentity(identity);

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                // Request routing rules
                .defineRequestRoutingRule("rule1")
                .fromPublicFrontend()
                .fromFrontendHttpsPort(443)
                .withSslCertificate("ssl1")
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .toBackendIPAddress("11.1.1.2")
                .attach()
                .withIdentity(serviceIdentity)
                .defineSslCertificate("ssl1")
                .withKeyVaultSecretId(secret1.id())
                .attach()
                .withExistingPublicIpAddress(pip)
                .withTier(ApplicationGatewayTier.WAF_V2)
                .withSize(ApplicationGatewaySkuName.WAF_V2)
                .withAutoScale(2, 5)
                .withWebApplicationFirewall(true, ApplicationGatewayFirewallMode.PREVENTION)
                .create();

        Assertions.assertEquals(secret1.id(), appGateway.sslCertificates().get("ssl1").keyVaultSecretId());
        Assertions
            .assertEquals(
                secret1.id(), appGateway.requestRoutingRules().get("rule1").sslCertificate().keyVaultSecretId());

        appGateway =
            appGateway.update().defineSslCertificate("ssl2").withKeyVaultSecretId(secret2.id()).attach().apply();

        Assertions.assertEquals(secret2.id(), appGateway.sslCertificates().get("ssl2").keyVaultSecretId());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canCreateApplicationGatewayWithSslCertificate() throws Exception {
        String appGatewayName = generateRandomResourceName("agwaf", 15);
        String identityName = generateRandomResourceName("id", 10);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        Identity identity =
            msiManager
                .identities()
                .define(identityName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                .create();

        Assertions.assertNotNull(identity.name());
        Assertions.assertNotNull(identity.principalId());

        ManagedServiceIdentity serviceIdentity = createManagedServiceIdentityFromIdentity(identity);

        String secretId = createKeyVaultCertificate(
            azureCliSignedInUser().userPrincipalName(),
            identity.principalId());

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                // Request routing rules
                .defineRequestRoutingRule("rule1")
                    .fromPublicFrontend()
                    .fromFrontendHttpsPort(443)
                    .withSslCertificate("ssl1")
                    .toBackendHttpPort(8080)
                    .toBackendIPAddress("11.1.1.1")
                    .toBackendIPAddress("11.1.1.2")
                    .attach()
                .withIdentity(serviceIdentity)
                .defineSslCertificate("ssl1")
                    .withKeyVaultSecretId(secretId)
                    .attach()
                .withExistingPublicIpAddress(pip)
                .withTier(ApplicationGatewayTier.WAF_V2)
                .withSize(ApplicationGatewaySkuName.WAF_V2)
                .withAutoScale(2, 5)
                .withWebApplicationFirewall(true, ApplicationGatewayFirewallMode.PREVENTION)
                .create();

        Assertions.assertEquals(secretId, appGateway.sslCertificates().get("ssl1").keyVaultSecretId());
        Assertions.assertEquals(secretId, appGateway.requestRoutingRules().get("rule1").sslCertificate().keyVaultSecretId());
    }

    @Test
    public void canAutoAssignPriorityForRequestRoutingRulesWithWAF() {
        // auto-assign 3 rules, user-assign with 1 (highest) and 20000 (lowest)
        String appGatewayName = generateRandomResourceName("agwaf", 15);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                // Request routing rules
                // rule1 with no priority
                .defineRequestRoutingRule("rule1")
                .fromPublicFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .toBackendIPAddress("11.1.1.2")
                .attach()
                // rule2 with no priority
                .defineRequestRoutingRule("rule2")
                .fromPublicFrontend()
                .fromFrontendHttpPort(81)
                .toBackendHttpPort(8181)
                .toBackendIPAddress("11.1.1.3")
                .attach()
                // rule3 with priority 1
                .defineRequestRoutingRule("rule3")
                .fromPublicFrontend()
                .fromFrontendHttpPort(83)
                .toBackendHttpPort(8383)
                .toBackendIPAddress("11.1.1.4")
                .withPriority(1)
                .attach()
                // rule4 with priority 20000
                .defineRequestRoutingRule("rule4")
                .fromPublicFrontend()
                .fromFrontendHttpPort(84)
                .toBackendHttpPort(8384)
                .toBackendIPAddress("11.1.1.5")
                .withPriority(20000)
                .attach()
                .withExistingPublicIpAddress(pip)
                .withTier(ApplicationGatewayTier.WAF_V2)
                .withSize(ApplicationGatewaySkuName.WAF_V2)
                .withAutoScale(2, 5)
                .withWebApplicationFirewall(true, ApplicationGatewayFirewallMode.PREVENTION)
                .create();
        // add a rule5 with no priority
        appGateway.update()
            .defineRequestRoutingRule("rule5")
            .fromPublicFrontend()
            .fromFrontendHttpPort(82)
            .toBackendHttpPort(8282)
            .toBackendIPAddress("11.1.1.6")
            .attach()
            .apply();
        Integer rule1Priority = appGateway.requestRoutingRules().get("rule1").priority();
        Integer rule2Priority = appGateway.requestRoutingRules().get("rule2").priority();
        Integer rule5Priority = appGateway.requestRoutingRules().get("rule5").priority();
        Assertions.assertTrue(rule1Priority < rule5Priority && rule2Priority < rule5Priority);
        Assertions.assertEquals(1, appGateway.requestRoutingRules().get("rule3").priority());
        Assertions.assertEquals(20000, appGateway.requestRoutingRules().get("rule4").priority());

        // add a rule6 with no priority and another rule7 with priority 10040
        appGateway.update()
            // rule6 with no priority
            .defineRequestRoutingRule("rule6")
            .fromPublicFrontend()
            .fromFrontendHttpPort(85)
            .toBackendHttpPort(8585)
            .toBackendIPAddress("11.1.1.7")
            .attach()
            // rule7 with priority 10040
            .defineRequestRoutingRule("rule7")
            .fromPublicFrontend()
            .fromFrontendHttpPort(86)
            .toBackendHttpPort(8686)
            .toBackendIPAddress("11.1.1.8")
            .withPriority(10040)
            .attach()
            .apply();
        Assertions.assertEquals(10050, appGateway.requestRoutingRules().get("rule6").priority());

        // update rule3's priority from 1 to 2
        appGateway.update()
            .updateRequestRoutingRule("rule3")
            .withPriority(2)
            .parent()
            .apply();
        Assertions.assertEquals(2, appGateway.requestRoutingRules().get("rule3").priority());
    }

    @Test
    public void testAddRemoveIpAddressFromWafV2WithExclusionsEqualsAny() {
        String appGatewayName = generateRandomResourceName("agwaf", 15);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(REGION)
                .withNewResourceGroup(rgName)
                .defineRequestRoutingRule("rule1")
                .fromPublicFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .attach()
                .withExistingPublicIpAddress(pip)
                .withTier(ApplicationGatewayTier.WAF_V2)
                .withSize(ApplicationGatewaySkuName.WAF_V2)
                .withAutoScale(2, 5)
                .withWebApplicationFirewall(
                    new ApplicationGatewayWebApplicationFirewallConfiguration()
                        .withEnabled(true)
                        .withFirewallMode(ApplicationGatewayFirewallMode.PREVENTION)
                        .withRuleSetType("OWASP")
                        .withRuleSetVersion("3.0")
                        .withExclusions(Collections.singletonList(
                            new ApplicationGatewayFirewallExclusion()
                                .withMatchVariable("RequestHeaderNames")
                                .withSelectorMatchOperator(null) // Equals any
                                .withSelector(null) // *
                        ))
                )
                .create();

        Assertions.assertEquals("RequestHeaderNames", appGateway.webApplicationFirewallConfiguration().exclusions().iterator().next().matchVariable());
        Assertions.assertNull(appGateway.webApplicationFirewallConfiguration().exclusions().iterator().next().selectorMatchOperator());

        Map<String, ApplicationGatewayBackend> backends = appGateway.backends();

        backends.forEach((name, backend) ->
            backend.addresses().forEach(addr ->
                appGateway.update()
                    .updateBackend(name)
                    .withoutIPAddress(addr.ipAddress())
                    .parent()
                    .apply()));
    }

    @Test
    public void canAssociateWafPolicy() {
        String appGatewayName = generateRandomResourceName("agwaf", 15);
        String wafPolicyName = generateRandomResourceName("waf", 15);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        WebApplicationFirewallPolicy wafPolicy =
            networkManager
                .webApplicationFirewallPolicies()
                .define(wafPolicyName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                .withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet.OWASP_3_2)
                .create();

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                .defineRequestRoutingRule("rule1")
                .fromPublicFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .toBackendIPAddress("11.1.1.2")
                .attach()
                .withExistingPublicIpAddress(pip)
                .withTier(ApplicationGatewayTier.WAF_V2)
                .withSize(ApplicationGatewaySkuName.WAF_V2)
                .withExistingWebApplicationFirewallPolicy(wafPolicy)
                .create();

        Assertions.assertNotNull(appGateway.getWebApplicationFirewallPolicy());
        Assertions.assertNull(appGateway.webApplicationFirewallConfiguration());

        wafPolicy.refresh();
        // check association
        Assertions.assertEquals(appGateway.id(), wafPolicy.getAssociatedApplicationGateways().iterator().next().id());
        Assertions.assertEquals(wafPolicy.id(), appGateway.getWebApplicationFirewallPolicy().id());

        appGateway.update()
            .withNewWebApplicationFirewallPolicy(WebApplicationFirewallMode.PREVENTION)
            .apply();

        WebApplicationFirewallPolicy newPolicy = appGateway.getWebApplicationFirewallPolicy();

        Assertions.assertNotNull(newPolicy);
        Assertions.assertTrue(newPolicy.isEnabled());
        Assertions.assertEquals(WebApplicationFirewallMode.PREVENTION, newPolicy.mode());
        Assertions.assertNotEquals(newPolicy.id(), wafPolicy.id());
        // check updated association
        Assertions.assertEquals(appGateway.id(), newPolicy.getAssociatedApplicationGateways().iterator().next().id());
        Assertions.assertEquals(newPolicy.id(), appGateway.getWebApplicationFirewallPolicy().id());

        // invalid application gateway with mixed legacy WAF configuration and WAF policy
        String invalidPolicyName = "invalid";

        Assertions.assertThrows(IllegalStateException.class, () -> {
            networkManager.applicationGateways()
                .define("invalid")
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                .defineRequestRoutingRule("rule1")
                .fromPublicFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .toBackendIPAddress("11.1.1.2")
                .attach()
                .withNewPublicIpAddress()
                .withTier(ApplicationGatewayTier.WAF_V2)
                .withSize(ApplicationGatewaySkuName.WAF_V2)
                // mixed legacy WAF configuration and WAF policy
                .withNewWebApplicationFirewallPolicy(
                    networkManager
                        .webApplicationFirewallPolicies()
                        .define(invalidPolicyName)
                        .withRegion(REGION)
                        .withExistingResourceGroup(rgName)
                        .withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet.OWASP_3_2))
                .withWebApplicationFirewall(true, ApplicationGatewayFirewallMode.PREVENTION)
                .create();
        });

        // assert no policy is created
        Assertions.assertTrue(
            networkManager
                .webApplicationFirewallPolicies()
                .listByResourceGroup(rgName)
                .stream()
                .noneMatch(policy -> policy.name().equals(invalidPolicyName)));
    }

    @Test
    public void canSetSslPolicy() {
        String appGatewayName = generateRandomResourceName("agw", 15);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        // create with predefined ssl policy
        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                // Request routing rules
                .defineRequestRoutingRule("rule1")
                    .fromPublicFrontend()
                    .fromFrontendHttpPort(80)
                    .toBackendHttpPort(8080)
                    .toBackendIPAddress("11.1.1.1")
                    .attach()
                .withExistingPublicIpAddress(pip)
                .withTier(ApplicationGatewayTier.WAF_V2)
                .withSize(ApplicationGatewaySkuName.WAF_V2)
                .withPredefinedSslPolicy(ApplicationGatewaySslPolicyName.APP_GW_SSL_POLICY20150501)
                .create();

        ApplicationGatewaySslPolicy sslPolicy = appGateway.sslPolicy();
        Assertions.assertNotNull(sslPolicy);
        Assertions.assertEquals(ApplicationGatewaySslPolicyType.PREDEFINED, sslPolicy.policyType());
        Assertions.assertEquals(ApplicationGatewaySslPolicyName.APP_GW_SSL_POLICY20150501, sslPolicy.policyName());

        // update with custom ssl policy
        appGateway.update()
            .withCustomV2SslPolicy(ApplicationGatewaySslProtocol.TLSV1_2, Collections.singletonList(ApplicationGatewaySslCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256))
            .apply();

        sslPolicy = appGateway.sslPolicy();
        Assertions.assertNotNull(sslPolicy);
        Assertions.assertEquals(ApplicationGatewaySslPolicyType.CUSTOM_V2, sslPolicy.policyType());
        Assertions.assertNull(sslPolicy.policyName());
        Assertions.assertEquals(ApplicationGatewaySslProtocol.TLSV1_2, sslPolicy.minProtocolVersion());
        Assertions.assertTrue(sslPolicy.cipherSuites().contains(ApplicationGatewaySslCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256));

        // predefined policy doesn't not support minProtocolVersion
        Assertions.assertThrows(ManagementException.class, () -> {
            appGateway.update()
                .withSslPolicy(new ApplicationGatewaySslPolicy()
                    .withPolicyType(ApplicationGatewaySslPolicyType.PREDEFINED)
                    .withPolicyName(ApplicationGatewaySslPolicyName.APP_GW_SSL_POLICY20150501)
                    .withMinProtocolVersion(ApplicationGatewaySslProtocol.TLSV1_1))
                .apply();
        });
    }

    @Test
    public void canCreateApplicationGatewayWithDefaultSku() {
        String appGatewayName = generateRandomResourceName("agw", 15);

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(REGION)
                .withNewResourceGroup(rgName)
                // Request routing rules
                .defineRequestRoutingRule("rule1")
                // BASIC still needs a public frontend. With private only, it'll report error:
                // "Application Gateway does not support Application Gateway without Public IP for the selected SKU tier Basic.
                // Supported SKU tiers are Standard,WAF."
                .fromPublicFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .attach()
                .withExistingPublicIpAddress(pip)
                .create();

        Assertions.assertEquals(ApplicationGatewayTier.BASIC, appGateway.tier());
        // BASIC still supports request routing rule priority.
        Assertions.assertNotNull(appGateway.requestRoutingRules().get("rule1").priority());
    }

    @Test
    public void canCRUDProbes() {
        String appGatewayName = generateRandomResourceName("agw", 15);
        String probeName = "probe1";

        PublicIpAddress pip = createResourceGroupAndPublicIpAddress();

        ApplicationGateway appGateway = networkManager.applicationGateways().define(appGatewayName)
            .withRegion(REGION)
            .withNewResourceGroup(rgName)
            // Request routing rules
            .defineRequestRoutingRule("rule1")
                // BASIC still needs a public frontend. With private only, it'll report error:
                // "Application Gateway does not support Application Gateway without Public IP for the selected SKU tier Basic.
                // Supported SKU tiers are Standard,WAF."
                .fromPublicFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .attach()
            .defineProbe(probeName)
                .withHostNameFromBackendHttpSettings()
                .withPath("/")
                .withHttp()
                .withTimeoutInSeconds(10)
                .withTimeBetweenProbesInSeconds(9)
                .withRetriesBeforeUnhealthy(5)
                .withHealthyHttpResponseStatusCodeRange(200, 249)
                .attach()
            .withExistingPublicIpAddress(pip)
            .create();

        Assertions.assertEquals(1, appGateway.probes().size());
        Assertions.assertNull(appGateway.probes().get(probeName).host());
        Assertions.assertTrue(appGateway.probes().get(probeName).isHostNameFromBackendHttpSettings());

        appGateway.update()
            .updateProbe(probeName)
                .withoutHostNameFromBackendHttpSettings()
                .withHost("microsoft.com")
                .parent()
            .apply();

        Assertions.assertEquals(1, appGateway.probes().size());
        Assertions.assertNotNull(appGateway.probes().get(probeName).host());
        Assertions.assertFalse(appGateway.probes().get(probeName).isHostNameFromBackendHttpSettings());

        appGateway.update()
            .withoutProbe(probeName)
            .apply();

        Assertions.assertTrue(appGateway.probes().isEmpty());
    }

    private String createKeyVaultCertificate(String signedInUser, String identityPrincipal) {
        String vaultName = generateRandomResourceName("vlt", 10);
        String secretName = generateRandomResourceName("srt", 10);

        Vault vault =
            keyVaultManager
                .vaults()
                .define(vaultName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                .defineAccessPolicy()
                    .forUser(signedInUser)
                    .allowSecretAllPermissions()
                    .allowCertificateAllPermissions()
                    .attach()
                .defineAccessPolicy()
                    .forObjectId(identityPrincipal)
                    .allowSecretAllPermissions()
                    .attach()
                .withAccessFromAzureServices()
                .withDeploymentEnabled()
//                // Important!! Only soft delete enabled key vault can be assigned to application gateway
//                // See also: https://github.com/MicrosoftDocs/azure-docs/issues/34382
//                .withSoftDeleteEnabled()
                .create();

        // create certificate
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vault.vaultUri())
            .pipeline(vault.vaultHttpPipeline())
            .buildClient();
        KeyVaultCertificateWithPolicy certificate = certificateClient.beginCreateCertificate(secretName, CertificatePolicy.getDefault()).getFinalResult();

        // take secret ID of the certificate
        return certificate.getSecretId();
    }

    private Secret createKeyVaultSecret(String signedInUser, String identityPrincipal) throws Exception {
        String vaultName = generateRandomResourceName("vlt", 10);
        String secretName = generateRandomResourceName("srt", 10);
        BufferedReader buff = new BufferedReader(new FileReader(new File(getClass().getClassLoader()
            .getResource("test.certificate").getFile())));
        String secretValue = buff.readLine();

        Vault vault =
            keyVaultManager
                .vaults()
                .define(vaultName)
                .withRegion(REGION)
                .withExistingResourceGroup(rgName)
                .defineAccessPolicy()
                .forUser(signedInUser)
                .allowSecretAllPermissions()
                .attach()
                .defineAccessPolicy()
                .forObjectId(identityPrincipal)
                .allowSecretAllPermissions()
                .attach()
                .withAccessFromAzureServices()
                .withDeploymentEnabled()
//                // Important!! Only soft delete enabled key vault can be assigned to application gateway
//                // See also: https://github.com/MicrosoftDocs/azure-docs/issues/34382
//                .withSoftDeleteEnabled()
                .create();

        return vault.secrets().define(secretName).withValue(secretValue).create();
    }

    private static ManagedServiceIdentity createManagedServiceIdentityFromIdentity(Identity identity) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userAssignedIdentitiesValueObject = mapper.createObjectNode();
        ((ObjectNode) userAssignedIdentitiesValueObject).put("principalId", identity.principalId());
        ((ObjectNode) userAssignedIdentitiesValueObject).put("clientId", identity.clientId());
        ManagedServiceIdentityUserAssignedIdentities userAssignedIdentitiesValue =
            new JacksonAdapter()
                .deserialize(
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(userAssignedIdentitiesValueObject),
                    ManagedServiceIdentityUserAssignedIdentities.class,
                    SerializerEncoding.JSON);

        Map<String, ManagedServiceIdentityUserAssignedIdentities> userAssignedIdentities = new HashMap<>();
        userAssignedIdentities.put(identity.id(), userAssignedIdentitiesValue);

        ManagedServiceIdentity serviceIdentity = new ManagedServiceIdentity();
        serviceIdentity.withType(ResourceIdentityType.USER_ASSIGNED);
        serviceIdentity.withUserAssignedIdentities(userAssignedIdentities);
        return serviceIdentity;
    }

    private PublicIpAddress createResourceGroupAndPublicIpAddress() {
        String appPublicIp = generateRandomResourceName("pip", 15);
        return networkManager
            .publicIpAddresses()
            .define(appPublicIp)
            .withRegion(REGION)
            .withNewResourceGroup(rgName)
            .withSku(PublicIPSkuType.STANDARD)
            .withStaticIP()
            .create();
    }
}
