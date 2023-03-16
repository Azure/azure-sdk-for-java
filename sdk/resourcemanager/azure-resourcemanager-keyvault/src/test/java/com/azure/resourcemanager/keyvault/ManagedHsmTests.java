// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
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
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.keys.models.KeyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManagedHsmTests extends KeyVaultManagementTest {

    @BeforeAll
    public static void setup() {
        AzureEnvironment.AZURE.getEndpoints().put("managedHsm", ".managedhsm.azure.net");
    }

    @Test
    public void canOperateManagedHsmAndKeys() throws Exception {
        ManagedHsm managedHsm = getManagedHsm();

        try {
            // Provisioning state.
            ProvisioningState provisioningState = managedHsm.state();
            Assertions.assertEquals(ProvisioningState.SUCCEEDED, provisioningState);
            activateManagedHsm(managedHsm);

            managedHsm.refresh();
            provisioningState = managedHsm.state();
            Assertions.assertEquals(ProvisioningState.ACTIVATED, provisioningState);

            KeyVaultAccessControlAsyncClient accessControlAsyncClient =
                new KeyVaultAccessControlClientBuilder()
                    .pipeline(keyVaultManager.httpPipeline())
                    .vaultUrl(managedHsm.hsmUri())
                    .buildAsyncClient();

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

            // create role assignments
            // cryptoUser for key operations
            KeyVaultRoleDefinition cryptoUser = getRoleDefinitionByName(accessControlAsyncClient, "Managed HSM Crypto User");
            accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.KEYS, cryptoUser.getId(), managedHsm.initialAdminObjectIds().get(0)).block();

            // key operations, same interface as the key vault
            Keys keys = hsm.keys();
            String keyName = generateRandomResourceName("key", 10);
            Key key = keys.define(keyName)
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(4096)
                .create();

            // for managing individual key
            accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.fromString(String.format("/keys/%s", keyName)), getRoleDefinitionByName(accessControlAsyncClient, "Managed HSM Crypto User").getId(), managedHsm.initialAdminObjectIds().get(0)).block();
            // cryptoOfficer for polling deleted key status
            KeyVaultRoleDefinition cryptoOfficer = getRoleDefinitionByName(accessControlAsyncClient, "Managed HSM Crypto Officer");
            accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.KEYS, cryptoOfficer.getId(), managedHsm.initialAdminObjectIds().get(0)).block();

            keys.deleteById(key.id());
        } finally {
            keyVaultManager.managedHsms().deleteById(managedHsm.id());
            keyVaultManager.serviceClient().getManagedHsms().purgeDeleted(managedHsm.name(), managedHsm.regionName());
        }
    }

    private KeyVaultRoleDefinition getRoleDefinitionByName(KeyVaultAccessControlAsyncClient accessControlAsyncClient, String roleName) {
        return accessControlAsyncClient.listRoleDefinitions(KeyVaultRoleScope.KEYS).toStream().filter(rd -> rd.getRoleName().equals(roleName)).findFirst().get();
    }

    /*
     * 1. generate 3 ssl certificates
     * 2. download security domain using generated certificates
     * 3. poll download status until success
     */
    private void activateManagedHsm(ManagedHsm managedHsm) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);

        // 1. generate ssl certificates
        JsonWebKey key1  = JsonWebKey.fromRsa(keyPairGenerator.generateKeyPair());

        JsonWebKey key2  = JsonWebKey.fromRsa(keyPairGenerator.generateKeyPair());

        JsonWebKey key3  = JsonWebKey.fromRsa(keyPairGenerator.generateKeyPair());

        // 2. download security domain
        downloadScurityDomain(managedHsm, key1, key2, key3);

        // 3. poll download status
        pollUntilSuccess(managedHsm);
    }

    private void downloadScurityDomain(ManagedHsm managedHsm, JsonWebKey key1, JsonWebKey key2, JsonWebKey key3) throws IOException {
        HttpRequest request = createActivateRequest(managedHsm, key1, key2, key3);
        HttpResponse response = keyVaultManager.httpPipeline().send(request).block();
        if (response.getStatusCode() != 202) {
            throw new RuntimeException("Failed to activate managed hsm");
        }
        Map<String, Object> responseBody = SerializerFactory.createDefaultManagementSerializerAdapter().deserialize(response.getBodyAsString().block(), Map.class, SerializerEncoding.JSON);
        String securityDomainDownloadToken = (String) responseBody.get("value");
        Assertions.assertNotNull(securityDomainDownloadToken);
        System.out.println(securityDomainDownloadToken);
    }

    private void pollUntilSuccess(ManagedHsm managedHsm) {
        String url = String.format("%ssecuritydomain/download/pending?api-version=7.2", managedHsm.hsmUri());
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
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
            .delayElements(Duration.ofSeconds(30))
            .takeUntil(status -> status.equals("Success"))
            .blockLast();
    }

    private HttpRequest createActivateRequest(ManagedHsm managedHsm, JsonWebKey key1, JsonWebKey key2, JsonWebKey key3) {
        String url = String.format("%ssecuritydomain/download?api-version=7.2", managedHsm.hsmUri());
        HttpRequest request = new HttpRequest(HttpMethod.POST, url);
        request.setHeader("Content-Type", "application/json charset=utf-8");
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> certificateInfoObject = new HashMap<>();
        certificateInfoObject.put("certificates", Arrays.asList(key1, key2, key3));
        certificateInfoObject.put("required", 2);
        body.put("certificates", Arrays.asList(key1, key2, key3));

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

    public static class JsonWebKey {
        private Collection<String> x5c;
        private String x5t;
        private String x5tS256;
        @JsonProperty("key_ops")
        private List<String> keyOps;
        private String kty = "RSA";
        private String alg = "RSA-OAEP-256";
        private String n;
        private String e;

        public static JsonWebKey fromRsa(KeyPair generateKeyPair) throws Exception{
            X509V3CertificateGenerator x509Generator = new X509V3CertificateGenerator();
            x509Generator.setPublicKey(generateKeyPair.getPublic());
            x509Generator.setSerialNumber(BigInteger.valueOf(1));
            x509Generator.setIssuerDN(new X500Principal("CN=Test"));
            x509Generator.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
            x509Generator.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30)));
            x509Generator.setSubjectDN(new X500Principal("CN=Test"));
            x509Generator.setSignatureAlgorithm("SHA256withRSA");
            X509Certificate x509Certificate = x509Generator.generate(generateKeyPair.getPrivate());

            JsonWebKey jsonWebKey = new JsonWebKey();
            jsonWebKey.x5c = Collections.singleton(base64X5c(x509Certificate.getEncoded()));
            jsonWebKey.x5t = base64Encode(MessageDigest.getInstance("SHA1").digest(x509Certificate.getEncoded()));
            jsonWebKey.x5tS256 = base64Encode(MessageDigest.getInstance("SHA256").digest(x509Certificate.getEncoded()));
            jsonWebKey.keyOps = Arrays.asList("encrypt", "verify", "wrapKey");
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey)generateKeyPair.getPrivate();

            jsonWebKey.n = base64Encode(privateKey.getModulus().toByteArray());
            jsonWebKey.e = base64Encode(privateKey.getPublicExponent().toByteArray());
            return jsonWebKey;
        }

        private static String base64X5c(byte[] publicBytes) throws Exception{
            return new String(Base64.getEncoder().encode(publicBytes), "ascii");
        }

        private static String base64Encode(byte[] digest) throws Exception{
            return new String(Base64.getEncoder().encode(digest), "ascii").strip().replaceAll("\\+", "-").replaceAll("/", "_");
        }

        public Collection<String> getX5c() {
            return x5c;
        }

        public String getX5t() {
            return x5t;
        }

        public String getX5tS256() {
            return x5tS256;
        }

        public List<String> getKeyOps() {
            return keyOps;
        }

        public String getKty() {
            return kty;
        }

        public String getAlg() {
            return alg;
        }

        public String getN() {
            return n;
        }

        public String getE() {
            return e;
        }
    }
}
