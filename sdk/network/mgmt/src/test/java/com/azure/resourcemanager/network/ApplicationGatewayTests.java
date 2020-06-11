// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayFirewallDisabledRuleGroup;
import com.azure.resourcemanager.network.models.ApplicationGatewayFirewallExclusion;
import com.azure.resourcemanager.network.models.ApplicationGatewayFirewallMode;
import com.azure.resourcemanager.network.models.ApplicationGatewaySkuName;
import com.azure.resourcemanager.network.models.ApplicationGatewayTier;
import com.azure.resourcemanager.network.models.ApplicationGatewayWebApplicationFirewallConfiguration;
import com.azure.resourcemanager.network.models.ManagedServiceIdentity;
import com.azure.resourcemanager.network.models.ManagedServiceIdentityUserAssignedIdentities;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.ResourceIdentityType;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ApplicationGatewayTests extends NetworkManagementTest {

    @Test
    public void canCRUDApplicationGatewayWithWAF() throws Exception {
        String appGatewayName = sdkContext.randomResourceName("agwaf", 15);
        String appPublicIp = sdkContext.randomResourceName("pip", 15);
        PublicIpAddress pip =
            networkManager
                .publicIpAddresses()
                .define(appPublicIp)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withSku(PublicIPSkuType.STANDARD)
                .withStaticIP()
                .create();

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                // Request routing rules
                .defineRequestRoutingRule("rule1")
                .fromPublicFrontend()
                .fromFrontendHttpsPort(443)
                .withSslCertificateFromPfxFile(
                    new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
                .withSslCertificatePassword("Abc123")
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

        Assertions.assertTrue(appGateway != null);
        Assertions.assertTrue(ApplicationGatewayTier.WAF_V2.equals(appGateway.tier()));
        Assertions.assertTrue(ApplicationGatewaySkuName.WAF_V2.equals(appGateway.size()));
        Assertions.assertTrue(appGateway.autoscaleConfiguration().minCapacity() == 2);
        Assertions.assertTrue(appGateway.autoscaleConfiguration().maxCapacity() == 5);

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
        Assertions.assertTrue(appGateway.webApplicationFirewallConfiguration().fileUploadLimitInMb() == 200);
        Assertions.assertTrue(appGateway.webApplicationFirewallConfiguration().requestBodyCheck());
        Assertions
            .assertEquals(appGateway.webApplicationFirewallConfiguration().maxRequestBodySizeInKb(), (Integer) 64);

        Assertions.assertEquals(appGateway.webApplicationFirewallConfiguration().exclusions().size(), 1);

        Assertions
            .assertEquals(
                appGateway.webApplicationFirewallConfiguration().exclusions().get(0).matchVariable(),
                "RequestHeaderNames");
        Assertions
            .assertEquals(
                appGateway.webApplicationFirewallConfiguration().exclusions().get(0).selectorMatchOperator(),
                "StartsWith");
        Assertions
            .assertEquals(
                appGateway.webApplicationFirewallConfiguration().exclusions().get(0).selector(), "User-Agent");

        Assertions.assertEquals(appGateway.webApplicationFirewallConfiguration().disabledRuleGroups().size(), 1);
        Assertions
            .assertEquals(
                appGateway.webApplicationFirewallConfiguration().disabledRuleGroups().get(0).ruleGroupName(),
                "REQUEST-943-APPLICATION-ATTACK-SESSION-FIXATION");
    }

    @Test
    @Disabled("Need client id for key vault usage")
    public void canCreateApplicationGatewayWithSecret() throws Exception {
        String appGatewayName = sdkContext.randomResourceName("agwaf", 15);
        String appPublicIp = sdkContext.randomResourceName("pip", 15);
        String identityName = sdkContext.randomResourceName("id", 10);

        PublicIpAddress pip =
            networkManager
                .publicIpAddresses()
                .define(appPublicIp)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withSku(PublicIPSkuType.STANDARD)
                .withStaticIP()
                .create();

        Identity identity =
            msiManager
                .identities()
                .define(identityName)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .create();

        Assertions.assertNotNull(identity.name());
        Assertions.assertNotNull(identity.principalId());

        Secret secret1 = createKeyVaultSecret(clientIdFromFile(), identity.principalId());
        Secret secret2 = createKeyVaultSecret(clientIdFromFile(), identity.principalId());

        ManagedServiceIdentity serviceIdentity = createManagedServiceIdentityFromIdentity(identity);

        ApplicationGateway appGateway =
            networkManager
                .applicationGateways()
                .define(appGatewayName)
                .withRegion(Region.US_EAST)
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

    private Secret createKeyVaultSecret(String servicePrincipal, String identityPrincipal) throws Exception {
        String vaultName = sdkContext.randomResourceName("vlt", 10);
        String secretName = sdkContext.randomResourceName("srt", 10);
        String secretValue =
            Files
                .readFirstLine(
                    new File(getClass().getClassLoader().getResource("test.certificate").getFile()),
                    Charset.defaultCharset());

        Vault vault =
            keyVaultManager
                .vaults()
                .define(vaultName)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .defineAccessPolicy()
                .forServicePrincipal(servicePrincipal)
                .allowSecretAllPermissions()
                .attach()
                .defineAccessPolicy()
                .forObjectId(identityPrincipal)
                .allowSecretAllPermissions()
                .attach()
                .withAccessFromAzureServices()
                .withDeploymentEnabled()
                // Important!! Only soft delete enabled key vault can be assigned to application gateway
                // See also: https://github.com/MicrosoftDocs/azure-docs/issues/34382
                .withSoftDeleteEnabled()
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
}
