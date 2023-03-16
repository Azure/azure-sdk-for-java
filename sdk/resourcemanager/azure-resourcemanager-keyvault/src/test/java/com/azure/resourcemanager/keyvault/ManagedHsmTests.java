// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.keyvault.fluent.models.ManagedHsmInner;
import com.azure.resourcemanager.keyvault.models.CreateMode;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Keys;
import com.azure.resourcemanager.keyvault.models.ManagedHsm;
import com.azure.resourcemanager.keyvault.models.ManagedHsmProperties;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSku;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSkuFamily;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSkuName;
import com.azure.resourcemanager.keyvault.models.MhsmNetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.ProvisioningState;
import com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient;
import com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManagedHsmTests extends KeyVaultManagementTest {
    @Test
    public void canOperateManagedHsmAndKeys() throws Exception {
        ManagedHsm managedHsm = getManagedHsm();

        try {
            KeyVaultAccessControlAsyncClient accessControlAsyncClient =
                new KeyVaultAccessControlClientBuilder()
                    .pipeline(keyVaultManager.httpPipeline())
                    .vaultUrl(managedHsm.hsmUri())
                    .buildAsyncClient();

            prepareManagedHsm(managedHsm, accessControlAsyncClient);

            // listByResourceGroups
            PagedIterable<ManagedHsm> hsms = keyVaultManager.managedHsms()
                .listByResourceGroup(rgName);
            // getByResourceGroup
            ManagedHsm hsm = keyVaultManager.managedHsms()
                .getByResourceGroup(rgName, managedHsm.name());

            // ManagedHsm properties
            // The Azure Active Directory tenant ID that should be used for authenticating requests to the managed HSM pool.
            String tenantId = hsm.tenantId();
            Assertions.assertNotNull(tenantId);

            ManagedHsmSku sku = hsm.sku();
            Assertions.assertNotNull(sku);

            // Array of initial administrators object ids for this managed hsm pool.
            List<String> initialAdminObjectIds = hsm.initialAdminObjectIds();
            Assertions.assertEquals(1, initialAdminObjectIds.size());

            // The URI of the managed hsm pool for performing operations on keys.
            String hsmUri = hsm.hsmUri();
            Assertions.assertNotNull(hsmUri);

            // Property to specify whether the 'soft delete' functionality is enabled for this managed HSM pool.
            boolean softDelete = hsm.isSoftDeleteEnabled();
            Assertions.assertTrue(softDelete);

            // softDelete data retention days. It accepts >=7 and <=90.
            Integer softDeleteRetentionDays = hsm.softDeleteRetentionDays();
            Assertions.assertEquals(7, softDeleteRetentionDays);

            // Property specifying whether protection against purge is enabled for this managed HSM pool.
            boolean purgeProtectionEnabled = hsm.isPurgeProtectionEnabled();
            Assertions.assertFalse(purgeProtectionEnabled);

            // he create mode to indicate whether the resource is being created or is being recovered from a deleted resource.
            CreateMode createMode = hsm.createMode();
            Assertions.assertEquals(CreateMode.DEFAULT, createMode);

            // Rules governing the accessibility of the key vault from specific network locations.
            MhsmNetworkRuleSet ruleSet = hsm.networkRuleSet();
            // Provisioning state.
            ProvisioningState provisioningState = hsm.state();

            // key operations, same interface as the key vault
            Keys keys = hsm.keys();
            String keyName = generateRandomResourceName("key", 10);
            Key key = keys.define(keyName)
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(4096)
                .create();

            accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.fromString(String.format("/keys/%s", keyName)), "21dbd100-6940-42c2-9190-5d6cb909625b", managedHsm.initialAdminObjectIds().get(0)).block();
            keys.deleteById(key.id());
        } finally {
            keyVaultManager.managedHsms().deleteById(managedHsm.id());
            keyVaultManager.serviceClient().getManagedHsms().purgeDeleted(managedHsm.name(), managedHsm.regionName());
        }
    }

    private void prepareManagedHsm(ManagedHsm managedHsm, KeyVaultAccessControlAsyncClient accessControlAsyncClient) throws Exception {
        activateManagedHsm(managedHsm);
        accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.KEYS, "21dbd100-6940-42c2-9190-5d6cb909625b", managedHsm.initialAdminObjectIds().get(0)).block();
    }

    /*
     * 1. generate 3 ssl certificates
     * 2. download security domain using generated certificates
     * 3. poll download status until success
     */
    private void activateManagedHsm(ManagedHsm managedHsm) throws Exception {

        // 1. generate ssl certificates
        JsonWebKey key1  = JsonWebKey.fromRsa(KeyPairGenerator.getInstance("RSA").generateKeyPair());
        JsonWebKey key2  = JsonWebKey.fromRsa(KeyPairGenerator.getInstance("RSA").generateKeyPair());
        JsonWebKey key3  = JsonWebKey.fromRsa(KeyPairGenerator.getInstance("RSA").generateKeyPair());

        // 2. download security domain
        HttpRequest request = createActivateRequest(managedHsm, key1, key2, key3);
        HttpResponse response = keyVaultManager.httpPipeline().send(request).block();
        if (response.getStatusCode() != 202) {
            throw new RuntimeException("Failed to activate managed hsm");
        }
        Map<String, Object> responseBody = SerializerFactory.createDefaultManagementSerializerAdapter().deserialize(response.getBodyAsString().block(), Map.class, SerializerEncoding.JSON);
        String securityDomainDownloadToken = (String) responseBody.get("SecurityDomainObject");
        Assertions.assertNotNull(securityDomainDownloadToken);
        System.out.println(securityDomainDownloadToken);

        // 3. poll download status
        pollUntilSuccess(managedHsm);
    }

    private void pollUntilSuccess(ManagedHsm managedHsm) {
        String url = String.format("%s/securitydomain/download/pending?api-version=7.2", managedHsm.hsmUri());
        HttpRequest request = new HttpRequest(HttpMethod.POST, url);
        request.setHeader("Content-Type", "application/json charset=utf-8");
        keyVaultManager.httpPipeline().send(request)
            .map(httpResponse -> {
                if (httpResponse.getStatusCode() == 200) {
                    try {
                        Map<String, Object> body = (Map<String, Object>) Mono.just(SerializerFactory.createDefaultManagementSerializerAdapter().deserialize(httpResponse.getBodyAsBinaryData().toBytes(), Map.class, SerializerEncoding.JSON));
                        String status = (String) body.get("status");
                        if (status == null) {
                            return Mono.error(new NullPointerException("status null"));
                        }
                        if (status.equals("Failed")) {
                            return Mono.error(new RuntimeException(String.format("Download security domain failed, message:%s", body.get("status_details"))));
                        }
                        return Mono.just(status);
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                } else {
                    return Mono.error(new RuntimeException("Failed to poll security domain status"));
                }
            })
            .repeat()
            .delaySubscription(Duration.ofSeconds(30))
            .takeUntil(status -> status.equals("Success"))
            .blockLast();
    }

    private HttpRequest createActivateRequest(ManagedHsm managedHsm, JsonWebKey key1, JsonWebKey key2, JsonWebKey key3) {
        String url = String.format("%s/securitydomain/download?api-version=7.2", managedHsm.hsmUri());
        HttpRequest request = new HttpRequest(HttpMethod.POST, url);
        request.setHeader("Content-Type", "application/json charset=utf-8");
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> certificateInfoObject = new HashMap<>();
        certificateInfoObject.put("certificates", Arrays.asList(key1, key2, key3));
        certificateInfoObject.put("required", 2);
        body.put("CertificateInfoObject", certificateInfoObject);
        request.setBody(BinaryData.fromObject(body));
        return request;
    }

    /*
     * create or get managed hsm instance
     */
    private ManagedHsm getManagedHsm() {
        String objectId = authorizationManager
            .servicePrincipals()
            .getByNameAsync(clientIdFromFile())
            .block()
            .id();

        keyVaultManager.resourceManager().resourceGroups().define(rgName).withRegion(Region.US_EAST2).create();
        ManagedHsmInner inner = keyVaultManager.serviceClient()
            .getManagedHsms()
            .createOrUpdate(
                rgName,
                generateRandomResourceName("mhsm", 10),
                new ManagedHsmInner()
                    .withLocation(Region.US_EAST2.name())
                    .withSku(
                        new ManagedHsmSku().withFamily(ManagedHsmSkuFamily.B).withName(ManagedHsmSkuName.STANDARD_B1))
                    .withProperties(
                        new ManagedHsmProperties()
                            .withTenantId(UUID.fromString(authorizationManager.tenantId()))
                            .withInitialAdminObjectIds(Arrays.asList(objectId))
                            .withEnableSoftDelete(true)
                            .withSoftDeleteRetentionInDays(7)
                            .withEnablePurgeProtection(false)),
                Context.NONE);

        keyVaultManager.serviceClient()
            .getManagedHsms()
            .createOrUpdate(rgName, inner.name(), inner);
        return keyVaultManager.managedHsms().getByResourceGroup(rgName, inner.name());
    }
}
