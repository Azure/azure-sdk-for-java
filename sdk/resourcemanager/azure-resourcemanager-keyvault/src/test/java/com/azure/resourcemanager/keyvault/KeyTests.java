// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.time.Duration;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KeyTests extends KeyVaultManagementTest {

    @Test
    @DoNotRecord
    public void canCRUDKey() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        Vault vault = createVault();
        String keyName = generateRandomResourceName("key", 20);

        // Create
        Key key =
            vault
                .keys()
                .define(keyName)
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeyOperations(KeyOperation.SIGN, KeyOperation.VERIFY)
                .create();

        Assertions.assertNotNull(key);
        Assertions.assertNotNull(key.id());
        Assertions.assertEquals(2, key.getJsonWebKey().getKeyOps().size());

        // Get
        Key key1 = vault.keys().getById(key.id());
        Assertions.assertNotNull(key1);
        Assertions.assertEquals(key.id(), key1.id());

        // Update
        key = key.update().withKeyOperations(KeyOperation.ENCRYPT).apply();

        Assertions.assertEquals(1, key.getJsonWebKey().getKeyOps().size());

        // New version
        key =
            key
                .update()
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeyOperations(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.SIGN)
                .apply();

        Assertions.assertEquals(3, key.getJsonWebKey().getKeyOps().size());

        // List versions
        Iterable<Key> keys = key.listVersions();
        Assertions.assertEquals(2, TestUtilities.getSize(keys));

        // Create RSA key with size
        key = vault
            .keys()
            .define(keyName)
            .withKeyTypeToCreate(KeyType.RSA)
            .withKeyOperations(KeyOperation.SIGN, KeyOperation.VERIFY)
            .withKeySize(2048)
            .create();

        Assertions.assertNotNull(key);
        Assertions.assertNotNull(key.id());
        Assertions.assertEquals(KeyType.RSA, key.getJsonWebKey().getKeyType());

        // Create EC key with curve
        key = vault
            .keys()
            .define(keyName)
            .withKeyTypeToCreate(KeyType.EC)
            .withKeyOperations(KeyOperation.SIGN, KeyOperation.VERIFY)
            .withKeyCurveName(KeyCurveName.P_521)
            .create();

        Assertions.assertNotNull(key);
        Assertions.assertNotNull(key.id());
        Assertions.assertEquals(KeyType.EC, key.getJsonWebKey().getKeyType());
        Assertions.assertEquals(KeyCurveName.P_521, key.getJsonWebKey().getCurveName());
    }

    @Test
    @DoNotRecord
    public void canImportKey() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        Vault vault = createVault();
        String keyName = generateRandomResourceName("key", 20);

        Key key =
            vault
                .keys()
                .define(keyName)
                .withLocalKeyToImport(JsonWebKey.fromRsa(KeyPairGenerator.getInstance("RSA").generateKeyPair()))
                .create();

        Assertions.assertNotNull(key);
        Assertions.assertNotNull(key.id());
    }

    @Test
    @DoNotRecord
    public void canBackupAndRestore() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        Vault vault = createVault();
        String keyName = generateRandomResourceName("key", 20);

        Key key =
            vault
                .keys()
                .define(keyName)
                .withLocalKeyToImport(JsonWebKey.fromRsa(KeyPairGenerator.getInstance("RSA").generateKeyPair()))
                .create();

        Assertions.assertNotNull(key);

        byte[] backup = key.backup();

        vault.keys().deleteById(key.id());
        Assertions.assertEquals(0, TestUtilities.getSize(vault.keys().list()));

        vault.keys().restore(backup);
        PagedIterable<Key> keys = vault.keys().list();
        Assertions.assertEquals(1, TestUtilities.getSize(keys));

        Assertions.assertEquals(key.getJsonWebKey(), keys.iterator().next().getJsonWebKey());
    }

    @Test
    @DoNotRecord
    public void canEncryptAndDecrypt() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        Vault vault = createVault();
        String keyName = generateRandomResourceName("key", 20);

        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        Key key = vault.keys().define(keyName).withLocalKeyToImport(JsonWebKey.fromRsa(keyPair)).create();

        Assertions.assertNotNull(key);

        String s = "the quick brown fox jumps over the lazy dog";
        byte[] data = s.getBytes();

        // Remote encryption
        byte[] encrypted = key.encrypt(EncryptionAlgorithm.RSA1_5, data);
        Assertions.assertNotNull(encrypted);

        byte[] decrypted = key.decrypt(EncryptionAlgorithm.RSA1_5, encrypted);
        Assertions.assertEquals(s, new String(decrypted));

        // Local encryption
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        encrypted = cipher.doFinal(data);

        decrypted = key.decrypt(EncryptionAlgorithm.RSA_OAEP, encrypted);
        Assertions.assertEquals(s, new String(decrypted));
    }

    @Test
    @DoNotRecord
    public void canSignAndVerify() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        Vault vault = createVault();
        String keyName = generateRandomResourceName("key", 20);

        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        Key key = vault.keys().define(keyName).withLocalKeyToImport(JsonWebKey.fromRsa(keyPair)).create();

        Assertions.assertNotNull(key);

        String s = "the quick brown fox jumps over the lazy dog";
        byte[] data = s.getBytes();
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
        byte[] signature = key.sign(SignatureAlgorithm.RS256, digest);
        Assertions.assertNotNull(signature);

        // Local verification
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(keyPair.getPublic());
        sign.update(data);
        Assertions.assertTrue(sign.verify(signature));

        // Remote verification
        Assertions.assertTrue(key.verify(SignatureAlgorithm.RS256, digest, signature));
    }

    @Test
    @DoNotRecord
    public void canWrapAndUnwrap() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        Vault vault = createVault();
        String keyName = generateRandomResourceName("key", 20);

        Key key =
            vault
                .keys()
                .define(keyName)
                .withLocalKeyToImport(JsonWebKey.fromRsa(KeyPairGenerator.getInstance("RSA").generateKeyPair()))
                .create();

        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();

        byte[] wrapped = key.wrapKey(KeyWrapAlgorithm.RSA1_5, secretKey.getEncoded());
        Assertions.assertNotNull(wrapped);

        byte[] unwrapped = key.unwrapKey(KeyWrapAlgorithm.RSA1_5, wrapped);
        Assertions.assertNotNull(unwrapped);
        Assertions.assertEquals(secretKey, new SecretKeySpec(unwrapped, "AES"));
    }

    private Vault createVault() throws Exception {
        String vaultName = generateRandomResourceName("vault", 20);

        Vault vault =
            keyVaultManager
                .vaults()
                .define(vaultName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .defineAccessPolicy()
                .forServicePrincipal(clientIdFromFile())
                .allowKeyAllPermissions()
                .attach()
                .create();

        Assertions.assertNotNull(vault);

        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        return vault;
    }
}
