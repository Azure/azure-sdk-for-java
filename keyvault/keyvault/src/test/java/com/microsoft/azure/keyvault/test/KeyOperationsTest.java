/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.test;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.crypto.Cipher;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyIdentifier;
import com.microsoft.azure.keyvault.models.KeyItem;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.ListKeysResponseMessage;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;
import com.microsoft.azure.keyvault.webkey.JsonWebKeySignatureAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;
import com.microsoft.windowsazure.exception.ServiceException;

public class KeyOperationsTest extends KeyVaultClientIntegrationTestBase {

    private static final String KEY_NAME = "javaKey";

    @Test
    public void transparentAuthentication() throws Exception {

        if (!handling401) {
            // TODO: Is there a way to report "not tested" without generating a
            // failure?
            return;
        }

        // Create a key on a vault.
        {
            Future<KeyBundle> result = keyVaultClient.createKeyAsync(getVaultUri(), KEY_NAME, "RSA", null, null, null, null);
            KeyBundle bundle = result.get();
            validateRsaKeyBundle(bundle, getVaultUri(), KEY_NAME, "RSA", null);
        }

        // Create a key on a different vault. Key Vault Data Plane returns 401,
        // which must be transparently handled by KeyVaultCredentials.
        {
            Future<KeyBundle> result = keyVaultClient.createKeyAsync(getSecondaryVaultUri(), KEY_NAME, "RSA", null, null, null, null);
            KeyBundle bundle = result.get();
            validateRsaKeyBundle(bundle, getSecondaryVaultUri(), KEY_NAME, "RSA", null);
        }

    }

    @Test
    public void importKeyOperation() throws Exception {

        KeyBundle keyBundle = new KeyBundle();
        JsonWebKey key = JsonWebKey.fromRSA(getTestKeyMaterial());

        key.setKeyOps(new String[] { JsonWebKeyOperation.ENCRYPT, JsonWebKeyOperation.DECRYPT });

        keyBundle.setKey(key);

        checkImportOperation(keyBundle, false);
        checkImportOperation(keyBundle, true);
    }

    private void checkImportOperation(KeyBundle keyBundle, boolean importToHardware) throws Exception {
        JsonWebKey importedJwk = keyBundle.getKey();
        Future<KeyBundle> result = keyVaultClient.importKeyAsync(getVaultUri(), KEY_NAME, keyBundle, importToHardware);
        KeyBundle importResultBundle = result.get();
        validateRsaKeyBundle(importResultBundle, getVaultUri(), KEY_NAME, importToHardware ? "RSA-HSM" : "RSA", importedJwk.getKeyOps());
        checkEncryptDecryptSequence(importedJwk, importResultBundle);
    }

    private void checkEncryptDecryptSequence(JsonWebKey importedKey, KeyBundle importedKeyBundle) throws Exception {

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        byte[] cipherText;

        // Encrypt in the service.
        {
            Future<KeyOperationResult> promise = keyVaultClient.encryptAsync(importedKeyBundle.getKey().getKid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            KeyOperationResult result = promise.get();
            cipherText = result.getResult();
        }

        // Decrypt in the client, notice OAEP algorithm instance to use.
        {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, importedKey.toRSA(true).getPrivate());

            byte[] beforeEncrypt = plainText;
            byte[] afterDecrypt = cipher.doFinal(cipherText);
            Assert.assertArrayEquals(beforeEncrypt, afterDecrypt);
        }

        // Encrypt in the client, using the service provided material. Also use
        // standard padding.
        {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, importedKeyBundle.getKey().toRSA().getPublic());

            cipherText = cipher.doFinal(plainText);
        }

        // Decrypt in the service.
        {
            Future<KeyOperationResult> promise = keyVaultClient.decryptAsync(importedKeyBundle.getKey().getKid(), JsonWebKeyEncryptionAlgorithm.RSA15, cipherText);
            KeyOperationResult result = promise.get();

            byte[] beforeEncrypt = plainText;
            byte[] afterDecrypt = result.getResult();
            Assert.assertArrayEquals(beforeEncrypt, afterDecrypt);
        }
    }

    @SuppressWarnings("serial")
    @Test
    public void crudOperations() throws Exception {

        KeyBundle createdBundle;
        {
            // Create key
            Future<KeyBundle> result = keyVaultClient.createKeyAsync(getVaultUri(), KEY_NAME, "RSA", null, null, null, null);
            createdBundle = result.get();
            validateRsaKeyBundle(createdBundle, getVaultUri(), KEY_NAME, "RSA", null);
        }

        // Key identifier.
        KeyIdentifier keyId = new KeyIdentifier(createdBundle.getKey().getKid());

        {
            // Get key using kid WO version
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(keyId.getBaseIdentifier());
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get key using full kid as defined in the bundle
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(createdBundle.getKey().getKid());
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get key using vault and key name.
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(getVaultUri(), KEY_NAME, null);
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get key using vault, key name and version.
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(getVaultUri(), KEY_NAME, keyId.getVersion());
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get key using vault, key name and a null version.
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(getVaultUri(), KEY_NAME, null);
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Update key using the kid as defined in the bundle

            // First we create a bundle with the modified attributes.
            KeyBundle updatingBundle = cloneWithJson(createdBundle); // Start
                                                                     // with a
                                                                     // copy of
                                                                     // original
                                                                     // bundle.
            updatingBundle.getAttributes().setExpires(newDate(2050, 1, 2));
            String[] key_ops;
            updatingBundle.getKey().setKeyOps(key_ops = new String[] { "encrypt", "decrypt" });
            updatingBundle.setTags(new HashMap<String, String>() {
                {
                    put("foo", "baz");
                }
            });

            // Perform the operation.
            Future<KeyBundle> result = keyVaultClient.updateKeyAsync(createdBundle.getKey().getKid(), key_ops, updatingBundle.getAttributes(), updatingBundle.getTags());
            KeyBundle updatedBundle = result.get();

            // Compare the JSON value.
            updatingBundle.getAttributes().setUpdatedUnixTime(updatedBundle.getAttributes().getUpdatedUnixTime()); // Nonsense
                                                                                                                   // comparing
                                                                                                                   // this.
            Assert.assertEquals(jsonWriter.writeValueAsString(updatingBundle), jsonWriter.writeValueAsString(updatedBundle));

            // Subsequent operations must use the updated bundle for comparison.
            createdBundle = updatedBundle;
        }

        {
            // Update key using vault and key name.

            // First we create a bundle with the modified attributes.
            KeyBundle updatingBundle = cloneWithJson(createdBundle); // Start
                                                                     // with a
                                                                     // copy of
                                                                     // original
                                                                     // bundle.
            updatingBundle.getAttributes().setNotBefore(newDate(2000, 1, 2));
            String[] key_ops;
            updatingBundle.getKey().setKeyOps(key_ops = new String[] { "sign", "verify" });
            updatingBundle.setTags(new HashMap<String, String>() {
                {
                    put("rex", "woof");
                }
            });

            // Perform the operation.
            Future<KeyBundle> result = keyVaultClient.updateKeyAsync(getVaultUri(), KEY_NAME, key_ops, updatingBundle.getAttributes(), updatingBundle.getTags());
            KeyBundle updatedBundle = result.get();

            // Compare the JSON value.
            updatingBundle.getAttributes().setUpdatedUnixTime(updatedBundle.getAttributes().getUpdatedUnixTime()); // Nonsense
                                                                                                                   // comparing
                                                                                                                   // this.
            Assert.assertEquals(jsonWriter.writeValueAsString(updatingBundle), jsonWriter.writeValueAsString(updatedBundle));

            // Subsequent operations must use the updated bundle for comparison.
            createdBundle = updatedBundle;
        }

        {
            // Delete key
            Future<KeyBundle> result = keyVaultClient.deleteKeyAsync(getVaultUri(), KEY_NAME);
            KeyBundle deleteBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(deleteBundle));
        }

        {
            // Expects a key not found
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(keyId.getBaseIdentifier());
            try {
                result.get();
            } catch (ExecutionException e) {
                ServiceException cause = (ServiceException) e.getCause();
                Assert.assertNotNull(cause.getError());
                Assert.assertEquals("KeyNotFound", cause.getError().getCode());
            }
        }

    }

    @Test
    public void backupRestore() throws Exception {

        KeyBundle createdBundle;

        // Creates a key
        {
            Future<KeyBundle> result = keyVaultClient.createKeyAsync(getVaultUri(), KEY_NAME, "RSA", null, null, null, null);
            createdBundle = result.get();
            validateRsaKeyBundle(createdBundle, getVaultUri(), KEY_NAME, "RSA", null);
        }

        // Creates a backup of key.
        byte[] keyBackup;
        {
            Future<byte[]> promise = keyVaultClient.backupKeyAsync(getVaultUri(), KEY_NAME);
            keyBackup = promise.get();
        }

        // Deletes the key.
        {
            Future<KeyBundle> promise = keyVaultClient.deleteKeyAsync(getVaultUri(), KEY_NAME);
            promise.get();
        }

        // Restores the key.
        {
            Future<KeyBundle> promise = keyVaultClient.restoreKeyAsync(getVaultUri(), keyBackup);
            KeyBundle restoredBundle = promise.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(restoredBundle));
        }

    }

    @Test
    public void listKeys() throws Exception {

        HashSet<String> keys = new HashSet<String>();
        for (int i = 0; i < 50; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    Future<KeyBundle> result = keyVaultClient.createKeyAsync(getVaultUri(), KEY_NAME + i, "RSA", null, null, null, null);
                    if (!IS_MOCKED) {
                        Thread.sleep(200); // avoids being throttled
                    }
                    KeyBundle createdBundle = result.get();
                    KeyIdentifier kid = new KeyIdentifier(createdBundle.getKey().getKid());
                    keys.add(kid.getBaseIdentifier());
                    break;
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof ServiceException) {
                        ++failureCount;
                        ServiceException se = (ServiceException) e.getCause();
                        if (se.getError().getCode().equals("Throttled")) {
                            System.out.println("Waiting to avoid throttling");
                            if (!IS_MOCKED) {
                                Thread.sleep(failureCount * 1500);
                            }
                            continue;
                        }
                    }
                    throw e;
                }
            }
        }

        Future<ListKeysResponseMessage> promise = keyVaultClient.getKeysAsync(getVaultUri(), 11);
        ListKeysResponseMessage listResult = promise.get();
        Assert.assertEquals(11, listResult.getValue().length);

        HashSet<String> toDelete = new HashSet<String>();

        promise = keyVaultClient.getKeysAsync(getVaultUri(), null);
        listResult = promise.get();
        for (;;) {
            for (KeyItem item : listResult.getValue()) {
                KeyIdentifier kid = new KeyIdentifier(item.getKid());
                toDelete.add(kid.getName());
                keys.remove(item.getKid());
            }
            String nextLink = listResult.getNextLink();
            if (nextLink == null) {
                break;
            }
            promise = keyVaultClient.getKeysNextAsync(nextLink);
            listResult = promise.get();
        }

        Assert.assertEquals(0, keys.size());

        for (String keyName : toDelete) {
            Future<KeyBundle> delPromise = keyVaultClient.deleteKeyAsync(getVaultUri(), keyName);
            delPromise.get();
        }

        if (!IS_MOCKED) {
            Thread.sleep(5000); // Avoid throttling in the next test.
        }
    }

    @Test
    public void listKeyVersions() throws Exception {

        HashSet<String> keys = new HashSet<String>();
        for (int i = 0; i < 50; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    Future<KeyBundle> result = keyVaultClient.createKeyAsync(getVaultUri(), KEY_NAME, "RSA", null, null, null, null);
                    if (!IS_MOCKED) {
                        Thread.sleep(200); // avoids being throttled
                    }
                    KeyBundle createdBundle = result.get();
                    keys.add(createdBundle.getKey().getKid());
                    break;
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof ServiceException) {
                        ++failureCount;
                        ServiceException se = (ServiceException) e.getCause();
                        if (se.getError().getCode().equals("Throttled")) {
                            System.out.println("Waiting to avoid throttling");
                            if (!IS_MOCKED) {
                                Thread.sleep(failureCount * 1500);
                            }
                            continue;
                        }
                    }
                    throw e;
                }
            }
        }

        Future<ListKeysResponseMessage> promise = keyVaultClient.getKeyVersionsAsync(getVaultUri(), KEY_NAME, 11);
        ListKeysResponseMessage listResult = promise.get();
        Assert.assertEquals(11, listResult.getValue().length);

        promise = keyVaultClient.getKeyVersionsAsync(getVaultUri(), KEY_NAME, null);
        listResult = promise.get();
        for (;;) {
            for (KeyItem item : listResult.getValue()) {
                keys.remove(item.getKid());
            }
            String nextLink = listResult.getNextLink();
            if (nextLink == null) {
                break;
            }
            promise = keyVaultClient.getKeyVersionsNextAsync(nextLink);
            listResult = promise.get();
        }

        Assert.assertEquals(0, keys.size());

        Future<KeyBundle> delPromise = keyVaultClient.deleteKeyAsync(getVaultUri(), KEY_NAME);
        delPromise.get();

        if (!IS_MOCKED) {
            Thread.sleep(5000); // Avoid throttling in the next test.
        }
    }

    @Test
    public void encryptDecryptOperations() throws Exception {

        JsonWebKey testKey = importTestKey();
        KeyIdentifier keyId = new KeyIdentifier(testKey.getKid());

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        byte[] cipherText;

        Future<KeyOperationResult> promise;
        KeyOperationResult result;

        // encrypt and decrypt using kid WO version
        {
            promise = keyVaultClient.encryptAsync(keyId.getBaseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            result = promise.get();
            cipherText = result.getResult();

            promise = keyVaultClient.decryptAsync(keyId.getBaseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());
        }

        // encrypt and decrypt using full kid
        {
            promise = keyVaultClient.encryptAsync(testKey.getKid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            result = promise.get();
            cipherText = result.getResult();

            promise = keyVaultClient.decryptAsync(testKey.getKid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());
        }

        // encrypt and decrypt using vault and key name, but no version.
        {
            promise = keyVaultClient.encryptAsync(getVaultUri(), KEY_NAME, null, JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            result = promise.get();
            cipherText = result.getResult();

            // Decrypt with supplied kid.
            promise = keyVaultClient.decryptAsync(result.getKid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());

            // Decrypt with base identifier to check if it uses the latest
            // version.
            promise = keyVaultClient.decryptAsync(keyId.getBaseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());
        }

        // encrypt and decrypt using vault, key name and version
        {
            promise = keyVaultClient.encryptAsync(getVaultUri(), KEY_NAME, keyId.getVersion(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            result = promise.get();
            cipherText = result.getResult();

            String kid = new KeyIdentifier(getVaultUri(), KEY_NAME, keyId.getVersion()).getIdentifier();
            promise = keyVaultClient.decryptAsync(kid, JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());
        }

    }

    @Test
    public void wrapUnwrapOperations() throws Exception {

        JsonWebKey testKey = importTestKey();
        KeyIdentifier keyId = new KeyIdentifier(testKey.getKid());

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        byte[] cipherText;

        Future<KeyOperationResult> promise;
        KeyOperationResult result;

        // wrap and unwrap using kid WO version
        {
            promise = keyVaultClient.wrapKeyAsync(keyId.getBaseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            result = promise.get();
            cipherText = result.getResult();

            promise = keyVaultClient.unwrapKeyAsync(keyId.getBaseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());
        }

        // wrap and unwrap using full kid
        {
            promise = keyVaultClient.wrapKeyAsync(testKey.getKid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            result = promise.get();
            cipherText = result.getResult();

            promise = keyVaultClient.unwrapKeyAsync(testKey.getKid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());
        }

        // wrap and unwrap using vault and key name, but no version.
        {
            promise = keyVaultClient.wrapKeyAsync(getVaultUri(), KEY_NAME, null, JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            result = promise.get();
            cipherText = result.getResult();

            // unwrap with supplied kid.
            promise = keyVaultClient.unwrapKeyAsync(result.getKid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());

            // unwrap with base identifier to check if it uses the latest
            // version.
            promise = keyVaultClient.unwrapKeyAsync(keyId.getBaseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());
        }

        // wrap and unwrap using vault, key name and version
        {
            promise = keyVaultClient.wrapKeyAsync(getVaultUri(), KEY_NAME, keyId.getVersion(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText);
            result = promise.get();
            cipherText = result.getResult();

            String kid = new KeyIdentifier(getVaultUri(), KEY_NAME, keyId.getVersion()).getIdentifier();
            promise = keyVaultClient.unwrapKeyAsync(kid, JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText);
            result = promise.get();
            Assert.assertArrayEquals(plainText, result.getResult());
        }

    }

    @Test
    public void signVerifyOperations() throws Exception {

        JsonWebKey testKey = importTestKey();
        KeyIdentifier keyId = new KeyIdentifier(testKey.getKid());

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plainText);
        byte[] digest = md.digest();
        byte[] signature;

        Future<KeyOperationResult> promise;
        KeyOperationResult result;
        Future<Boolean> verifyPromise;

        // Using kid WO version
        {
            promise = keyVaultClient.signAsync(keyId.getBaseIdentifier(), JsonWebKeySignatureAlgorithm.RS256, digest);
            result = promise.get();
            signature = result.getResult();

            verifyPromise = keyVaultClient.verifyAsync(keyId.getBaseIdentifier(), JsonWebKeySignatureAlgorithm.RS256, digest, signature);
            Assert.assertEquals(new Boolean(true), verifyPromise.get());
        }

        // Using full kid
        {
            promise = keyVaultClient.signAsync(testKey.getKid(), JsonWebKeySignatureAlgorithm.RS256, digest);
            result = promise.get();
            signature = result.getResult();

            verifyPromise = keyVaultClient.verifyAsync(testKey.getKid(), JsonWebKeySignatureAlgorithm.RS256, digest, signature);
            Assert.assertEquals(new Boolean(true), verifyPromise.get());

        }

        // Using vault and key name, but no version.
        {
            promise = keyVaultClient.signAsync(getVaultUri(), KEY_NAME, null, JsonWebKeySignatureAlgorithm.RS256, digest);
            result = promise.get();
            signature = result.getResult();

            // Verify with supplied kid.
            verifyPromise = keyVaultClient.verifyAsync(result.getKid(), JsonWebKeySignatureAlgorithm.RS256, digest, signature);
            Assert.assertEquals(new Boolean(true), verifyPromise.get());

            // Verify with base identifier to check if it uses the latest
            // version.
            verifyPromise = keyVaultClient.verifyAsync(keyId.getBaseIdentifier(), JsonWebKeySignatureAlgorithm.RS256, digest, signature);
            Assert.assertEquals(new Boolean(true), verifyPromise.get());
        }

        // Using vault, key name and version
        {
            promise = keyVaultClient.signAsync(getVaultUri(), KEY_NAME, keyId.getVersion(), JsonWebKeySignatureAlgorithm.RS256, digest);
            result = promise.get();
            signature = result.getResult();

            String kid = new KeyIdentifier(getVaultUri(), KEY_NAME, keyId.getVersion()).getIdentifier();
            verifyPromise = keyVaultClient.verifyAsync(kid, JsonWebKeySignatureAlgorithm.RS256, digest, signature);
            Assert.assertEquals(new Boolean(true), verifyPromise.get());
        }

    }

    private static JsonWebKey importTestKey() throws Exception {

        KeyBundle keyBundle = new KeyBundle();
        JsonWebKey key = JsonWebKey.fromRSA(getTestKeyMaterial());

        key.setKty(JsonWebKeyType.RSA);
        key.setKeyOps(new String[] { JsonWebKeyOperation.ENCRYPT, JsonWebKeyOperation.DECRYPT, JsonWebKeyOperation.SIGN, JsonWebKeyOperation.VERIFY, JsonWebKeyOperation.WRAP, JsonWebKeyOperation.UNWRAP });

        keyBundle.setKey(key);

        Future<KeyBundle> promise = keyVaultClient.importKeyAsync(getVaultUri(), KEY_NAME, keyBundle, false);
        keyBundle = promise.get();
        validateRsaKeyBundle(keyBundle, getVaultUri(), KEY_NAME, "RSA", null);

        return keyBundle.getKey();
    }

    private static KeyPair getTestKeyMaterial() throws Exception {

        KeyPair result;

        if ("live".equals(System.getenv("test.mode"))) {
            // Create a 2048 bit RSA private key
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            result = kpg.genKeyPair();
            // privateKey = (RSAPrivateCrtKey) kp.getPrivate();
        } else {
            result = getWellKnownKey();
        }

        return result;
    }

    private static KeyPair getWellKnownKey() throws Exception {
        BigInteger modulus = new BigInteger("27266783713040163753473734334021230592631652450892850648620119914958066181400432364213298181846462385257448168605902438305568194683691563208578540343969522651422088760509452879461613852042845039552547834002168737350264189810815735922734447830725099163869215360401162450008673869707774119785881115044406101346450911054819448375712432746968301739007624952483347278954755460152795801894283389540036131881712321193750961817346255102052653789197325341350920441746054233522546543768770643593655942246891652634114922277138937273034902434321431672058220631825053788262810480543541597284376261438324665363067125951152574540779");
        BigInteger publicExponent = new BigInteger("65537");
        BigInteger privateExponent = new BigInteger("10466613941269075477152428927796086150095892102279802916937552172064636326433780566497000814207416485739683286961848843255766652023400959086290344987308562817062506476465756840999981989957456897020361717197805192876094362315496459535960304928171129585813477132331538577519084006595335055487028872410579127692209642938724850603554885478763205394868103298473476811627231543504190652483290944218004086457805431824328448422034887148115990501701345535825110962804471270499590234116100216841170344686381902328362376624405803648588830575558058257742073963036264273582756620469659464278207233345784355220317478103481872995809");
        BigInteger primeP = new BigInteger("175002941104568842715096339107566771592009112128184231961529953978142750732317724951747797764638217287618769007295505214923187971350518217670604044004381362495186864051394404165602744235299100790551775147322153206730562450301874236875459336154569893255570576967036237661594595803204808064127845257496057219227");
        BigInteger primeQ = new BigInteger("155807574095269324897144428622185380283967159190626345335083690114147315509962698765044950001909553861571493035240542031420213144237033208612132704562174772894369053916729901982420535940939821673277140180113593951522522222348910536202664252481405241042414183668723338300649954708432681241621374644926879028977");
        BigInteger primeExponentP = new BigInteger("79745606804504995938838168837578376593737280079895233277372027184693457251170125851946171360348440134236338520742068873132216695552312068793428432338173016914968041076503997528137698610601222912385953171485249299873377130717231063522112968474603281996190849604705284061306758152904594168593526874435238915345");
        BigInteger primeExponentQ = new BigInteger("80619964983821018303966686284189517841976445905569830731617605558094658227540855971763115484608005874540349730961777634427740786642996065386667564038755340092176159839025706183161615488856833433976243963682074011475658804676349317075370362785860401437192843468423594688700132964854367053490737073471709030801");
        BigInteger crtCoefficient = new BigInteger("2157818511040667226980891229484210846757728661751992467240662009652654684725325675037512595031058612950802328971801913498711880111052682274056041470625863586779333188842602381844572406517251106159327934511268610438516820278066686225397795046020275055545005189953702783748235257613991379770525910232674719428");

        KeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeySpec privateKeySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return new KeyPair(keyFactory.generatePublic(publicKeySpec), keyFactory.generatePrivate(privateKeySpec));
    }

    private static KeyBundle cloneWithJson(KeyBundle template) throws Exception {
        String json = jsonWriter.writeValueAsString(template);
        return jsonReader.withType(KeyBundle.class).readValue(json);
    }

    private static Date newDate(int y, int m, int d) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        calendar.clear();
        calendar.set(y, m, d);
        return calendar.getTime();
    }

    private static void validateRsaKeyBundle(KeyBundle bundle, String vault, String keyName, String kty, String[] key_ops) throws Exception {
        String prefix = vault + "/keys/" + keyName + "/";
        String kid = bundle.getKey().getKid();
        Assert.assertTrue( //
                String.format("\"kid\" should start with \"%s\", but instead the value is \"%s\".", prefix, kid), //
                kid.startsWith(prefix));
        Assert.assertEquals(kty, bundle.getKey().getKty());
        Assert.assertNotNull("\"n\" should not be null.", bundle.getKey().getN());
        Assert.assertNotNull("\"e\" should not be null.", bundle.getKey().getE());
        if (key_ops != null) {
            String expected = jsonWriter.writeValueAsString(Arrays.asList(key_ops));
            String actual = jsonWriter.writeValueAsString(bundle.getKey().getKeyOps());
            Assert.assertEquals(expected, actual);
        }
        Assert.assertNotNull("\"created\" should not be null.", bundle.getAttributes().getCreated());
        Assert.assertNotNull("\"updated\" should not be null.", bundle.getAttributes().getUpdated());
    }

}
