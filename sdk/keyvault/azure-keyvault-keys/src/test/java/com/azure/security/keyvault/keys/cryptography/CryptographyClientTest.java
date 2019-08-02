package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

import static org.junit.Assert.*;

public class CryptographyClientTest extends CryptographyClientTestBase {

    private KeyClient client;
    private HttpPipeline pipeline;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(pipeline -> {
                this.pipeline = pipeline;
                return new KeyClientBuilder()
                    .pipeline(pipeline)
                    .endpoint(getEndpoint())
                    .buildClient();
            });
        } else {
            client = clientSetup(pipeline -> {
                this.pipeline = pipeline;
                 return new KeyClientBuilder()
                    .pipeline(pipeline)
                    .endpoint(getEndpoint())
                    .buildClient();
            });
        }
    }

    @Override
    public void encryptDecryptRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRSA(keyPair);
            String keyName = "testRsaKey";
            Key importedKey = client.importKey(keyName, key);
            key.kid(importedKey.id());
            key.keyOps(importedKey.keyMaterial().keyOps());
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .jsonWebKey(key)
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.RSA1_5, EncryptionAlgorithm.RSA_OAEP, EncryptionAlgorithm.RSA_OAEP_256);

            for (EncryptionAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] cipherText = cryptoClient.encrypt(algorithm, plainText).cipherText();
                byte[] decryptedText = serviceClient.decrypt(algorithm, cipherText, Context.NONE).block().plainText();

                assertArrayEquals(decryptedText, plainText);

                cipherText = serviceClient.encrypt(algorithm, plainText, Context.NONE).block().cipherText();
                decryptedText = cryptoClient.decrypt(algorithm, cipherText).plainText();

                assertArrayEquals(decryptedText, plainText);
            }

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });
    }

    @Override
    public void wrapUnwraptRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRSA(keyPair);
            String keyName = "testRsaKeyWrapUnwrap";
            Key importedKey = client.importKey(keyName, key);
            key.kid(importedKey.id());
            key.keyOps(importedKey.keyMaterial().keyOps());
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .jsonWebKey(key)
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<KeyWrapAlgorithm> algorithms = Arrays.asList(KeyWrapAlgorithm.RSA1_5, KeyWrapAlgorithm.RSA_OAEP, KeyWrapAlgorithm.RSA_OAEP_256);

            for (KeyWrapAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] encryptedKey = cryptoClient.wrapKey(algorithm, plainText).encryptedKey();
                byte[] decryptedKey = serviceClient.unwrapKey(algorithm, encryptedKey, Context.NONE).block().key();

                assertArrayEquals(decryptedKey, plainText);

                encryptedKey = serviceClient.wrapKey(algorithm, plainText, Context.NONE).block().encryptedKey();
                decryptedKey = cryptoClient.unwrapKey(algorithm, encryptedKey).key();

                assertArrayEquals(decryptedKey, plainText);
            }

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });
    }


    @Override
    public void SignVerifyRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRSA(keyPair);
            String keyName = "testRsaKeySignVerify";
            Key importedKey = client.importKey(keyName, key);
            key.kid(importedKey.id());
            key.keyOps(importedKey.keyMaterial().keyOps());
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .jsonWebKey(key)
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<SignatureAlgorithm> algorithms = Arrays.asList(SignatureAlgorithm.RS256, SignatureAlgorithm.RS384, SignatureAlgorithm.RS512);

            for (SignatureAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] signature = cryptoClient.signData(algorithm, plainText).signature();
                Boolean verifyStatus = serviceClient.verifyData(algorithm, plainText, signature, Context.NONE).block().isValid();

                assertTrue(verifyStatus);

                signature = serviceClient.signData(algorithm, plainText, Context.NONE).block().signature();
                verifyStatus = cryptoClient.verifyData(algorithm, plainText, signature).isValid();

                assertTrue(verifyStatus);
            }

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });
    }

    @Override
    public void signVerifyEc() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Map<KeyCurveName, SignatureAlgorithm> curveToSignature = new HashMap<>();
        curveToSignature.put(KeyCurveName.P_256, SignatureAlgorithm.ES256);
        curveToSignature.put(KeyCurveName.P_384, SignatureAlgorithm.ES384);
        curveToSignature.put(KeyCurveName.P_521, SignatureAlgorithm.ES512);
        curveToSignature.put(KeyCurveName.P_256K, SignatureAlgorithm.ES256K);

        Map<KeyCurveName, String> curveToSpec = new HashMap<>();
        curveToSpec.put(KeyCurveName.P_256, "secp256r1");
        curveToSpec.put(KeyCurveName.P_384, "secp384r1");
        curveToSpec.put(KeyCurveName.P_521, "secp521r1");
        curveToSpec.put(KeyCurveName.P_256K, "secp256k1") ;

        List<KeyCurveName> curveList =  Arrays.asList(KeyCurveName.P_256, KeyCurveName.P_384, KeyCurveName.P_521, KeyCurveName.P_256K);
        Provider provider = Security.getProvider("SunEC");
        for (KeyCurveName crv : curveList) {

            final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", provider);
            ECGenParameterSpec gps = new ECGenParameterSpec(curveToSpec.get(crv));
            generator.initialize(gps);
            KeyPair keyPair = generator.generateKeyPair();

            JsonWebKey key = JsonWebKey.fromEC(keyPair, provider);
            String keyName = "testEcKey" + crv.toString();
            Key imported = client.importKey(keyName, key);
            key.kid(imported.id());
            key.keyOps(imported.keyMaterial().keyOps());
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .jsonWebKey(key)
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            byte[] plainText = new byte[100];
            new Random(0x1234567L).nextBytes(plainText);

            byte[] signature = cryptoClient.signData(curveToSignature.get(crv), plainText).signature();

            Boolean verifyStatus = serviceClient.verifyData(curveToSignature.get(crv), plainText, signature, Context.NONE).block().isValid();
            assertTrue(verifyStatus);

            signature = serviceClient.signData(curveToSignature.get(crv), plainText, Context.NONE).block().signature();
            verifyStatus = cryptoClient.verifyData(curveToSignature.get(crv), plainText, signature).isValid();
            if(!interceptorManager.isPlaybackMode()) {
                assertTrue(verifyStatus);
            }

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        }

    }

    @Override
    public void wrapUnwrapSymmetricKeyAES128Kw() {
        // Arrange
        byte[] kek = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
        byte[] cek = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] ek = { 0x1F, (byte) 0xA6, (byte) 0x8B, 0x0A, (byte) 0x81, 0x12, (byte) 0xB4, 0x47, (byte) 0xAE, (byte) 0xF3, 0x4B, (byte) 0xD8, (byte) 0xFB, 0x5A, 0x7B, (byte) 0x82, (byte) 0x9D, 0x3E, (byte) 0x86, 0x23, 0x71, (byte) 0xD2, (byte) 0xCF, (byte) 0xE5 };

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(kek, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)))
            .buildClient();

        byte[] encrypted = cryptoClient.wrapKey(KeyWrapAlgorithm.A128KW, cek).encryptedKey();

        assertArrayEquals(ek, encrypted);

        byte[] decrypted = cryptoClient.unwrapKey(KeyWrapAlgorithm.A128KW, encrypted).key();

        assertArrayEquals(cek, decrypted);

    }

    private void pollOnKeyDeletion(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 30) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKey(keyName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey == null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Key %s not found \n", keyName);
    }

    private void pollOnKeyPurge(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKey(keyName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey != null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Key %s was not purged \n", keyName);
    }
}
