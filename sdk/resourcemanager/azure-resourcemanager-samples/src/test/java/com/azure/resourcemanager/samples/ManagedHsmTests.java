// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.models.ManagedHsmInner;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Keys;
import com.azure.resourcemanager.keyvault.models.ManagedHsm;
import com.azure.resourcemanager.keyvault.models.ManagedHsmProperties;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSku;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSkuFamily;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSkuName;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient;
import com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.keys.models.KeyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class ManagedHsmTests extends SamplesTestBase {
    private String rgName;
    private KeyVaultManager keyVaultManager;

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        super.initializeClients(httpPipeline, profile);
        rgName = generateRandomResourceName("javacsmrg", 15);
        keyVaultManager = azureResourceManager.managedHsms().manager();
    }

    @Override
    protected void cleanUpResources() {
        if (rgName != null) {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Note: Managed HSM instance is costly and it'll still cost you even if you delete the instance or the associated
     *       resource group, unless the instance is <string>purged</string>.
     *       <p>So, please be careful when running this test and always double check that the instance has been
     *       {@link com.azure.resourcemanager.keyvault.fluent.ManagedHsmsClient#purgeDeleted(String, String)} after the test. </p>
     *       <p>You can use {@link com.azure.resourcemanager.keyvault.fluent.ManagedHsmsClient#listDeleted()} to list all deleted instances
     *       that's not purged after deletion.</p>
     * @see <a href="https://learn.microsoft.com/en-us/azure/key-vault/managed-hsm/soft-delete-overview">soft-delete-overview</a>
     */
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canOperateManagedHsmAndKeys() throws Exception {
        KeyVaultManager keyVaultManager = azureResourceManager.managedHsms().manager();

        String mhsmName = generateRandomResourceName("mhsm", 10);

        ManagedHsm managedHsm = createManagedHsm(mhsmName);

        try {
            // Provisioning state.
            activateManagedHsm(managedHsm);

            // getByResourceGroup
            ManagedHsm hsm = keyVaultManager.managedHsms()
                .getByResourceGroup(rgName, managedHsm.name());

            KeyVaultAccessControlAsyncClient accessControlAsyncClient =
                new KeyVaultAccessControlClientBuilder()
                    .pipeline(keyVaultManager.httpPipeline())
                    .vaultUrl(managedHsm.hsmUri())
                    .buildAsyncClient();

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

            // cryptoUser for managing individual key
            KeyVaultRoleDefinition cryptoUserForKey = getRoleDefinitionByName(accessControlAsyncClient, "Managed HSM Crypto User");
            accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.fromString(String.format("/keys/%s", keyName)), cryptoUserForKey.getId(), managedHsm.initialAdminObjectIds().get(0)).block();
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
        downloadSecurityDomain(managedHsm, key1, key2, key3);

        // 3. poll download status
        pollDownlaodStatusUntilSuccess(managedHsm);
    }

    private void downloadSecurityDomain(ManagedHsm managedHsm, JsonWebKey key1, JsonWebKey key2, JsonWebKey key3) throws IOException {
        String url = String.format("%ssecuritydomain/download?api-version=7.2", managedHsm.hsmUri());
        HttpRequest request = new HttpRequest(HttpMethod.POST, url);
        request.setHeader("Content-Type", "application/json charset=utf-8");
        Map<String, Object> body = new HashMap<>();
        body.put("certificates", Arrays.asList(key1, key2, key3));
        body.put("required", 2);

        request.setBody(BinaryData.fromObject(body));

        HttpResponse response = keyVaultManager.httpPipeline().send(request).block();
        if (response.getStatusCode() != 202) {
            throw new RuntimeException("Failed to activate managed hsm");
        }
        Map<String, Object> responseBody = SerializerFactory.createDefaultManagementSerializerAdapter().deserialize(response.getBodyAsString().block(), Map.class, SerializerEncoding.JSON);
        String securityDomainDownloadToken = (String) responseBody.get("value");
        Assertions.assertNotNull(securityDomainDownloadToken);
    }

    private void pollDownlaodStatusUntilSuccess(ManagedHsm managedHsm) {
        String url = String.format("%ssecuritydomain/download/pending?api-version=7.2", managedHsm.hsmUri());
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader("Content-Type", "application/json charset=utf-8");
        Flux.interval(Duration.ZERO, ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(keyVaultManager.serviceClient().getDefaultPollInterval()))
            .flatMap(ignored -> keyVaultManager.httpPipeline().send(request))
            .flatMap(httpResponse -> {
                if (httpResponse.getStatusCode() != 200) {
                    return Mono.error(new RuntimeException("Failed to poll security domain status"));
                }
                return httpResponse.getBodyAsString()
                    .flatMap(bodyString -> {
                        try {
                            Map<String, Object> body = SerializerFactory.createDefaultManagementSerializerAdapter()
                                .deserialize(bodyString, Map.class, SerializerEncoding.JSON);
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
                    });
            })
            .takeUntil(status -> status.equals("Success"))
            .blockLast();
    }

    /*
     * create or get managed hsm instance
     */
    private ManagedHsm createManagedHsm(String mhsmName) {
        String objectId = azureResourceManager
            .accessManagement()
            .servicePrincipals()
            .getByNameAsync(clientIdFromFile())
            .block()
            .id();

        keyVaultManager.resourceManager().resourceGroups().define(rgName).withRegion(Region.US_EAST2).create();
        ManagedHsmInner inner = keyVaultManager.serviceClient()
            .getManagedHsms()
            .createOrUpdate(
                rgName,
                mhsmName,
                new ManagedHsmInner()
                    .withLocation(Region.US_EAST2.name())
                    .withSku(
                        new ManagedHsmSku().withFamily(ManagedHsmSkuFamily.B).withName(ManagedHsmSkuName.STANDARD_B1))
                    .withProperties(
                        new ManagedHsmProperties()
                            .withTenantId(UUID.fromString(azureResourceManager.tenantId()))
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

        public static JsonWebKey fromRsa(KeyPair generateKeyPair) throws Exception {
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
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) generateKeyPair.getPrivate();

            jsonWebKey.n = base64Encode(privateKey.getModulus().toByteArray());
            jsonWebKey.e = base64Encode(privateKey.getPublicExponent().toByteArray());
            return jsonWebKey;
        }

        private static String base64X5c(byte[] publicBytes) throws Exception {
            return new String(Base64.getEncoder().encode(publicBytes), "ascii");
        }

        private static String base64Encode(byte[] digest) throws Exception {
            return new String(Base64.getEncoder().encode(digest), "ascii").trim().replaceAll("\\+", "-").replaceAll("/", "_");
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
