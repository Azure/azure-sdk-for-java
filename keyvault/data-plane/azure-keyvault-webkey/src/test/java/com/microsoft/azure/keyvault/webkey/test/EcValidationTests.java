// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.webkey.test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyCurveName;

public class EcValidationTests {

    @Test
    public void ecPublicKeyValidation() throws Exception {

        for (String keyStr : keys.values()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonWebKey key = mapper.readValue(keyStr, JsonWebKey.class);
            Assert.assertTrue(key.hasPrivateKey());
            Assert.assertTrue(key.isValid());

            KeyPair keyPair = key.toEC();
            validateEcKey(keyPair, key);
            Assert.assertNull(keyPair.getPrivate());

            // Compare equal JSON web keys
            JsonWebKey sameKey = mapper.readValue(keyStr, JsonWebKey.class);
            Assert.assertEquals(key, key);
            Assert.assertEquals(key, sameKey);
            Assert.assertEquals(key.hashCode(), sameKey.hashCode());
        }
    }

    @Test
    public void ecPrivateKeyValidation() throws Exception {
        for (String keyStr : keys.values()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonWebKey key = mapper.readValue(keyStr, JsonWebKey.class);
            KeyPair keyPairWithPrivate = key.toEC(true);
            validateEcKey(keyPairWithPrivate, key);
            signVerify(keyPairWithPrivate.getPublic(), keyPairWithPrivate.getPrivate(), key.crv());
        }
    }

    private static void validateEcKey(KeyPair keyPair, JsonWebKey key) throws Exception {
        JsonWebKey jsonWebKey = JsonWebKey.fromEC(keyPair, Security.getProvider("SunEC"));
        boolean includePrivateKey = keyPair.getPrivate() != null;
        KeyPair keyPair2 = jsonWebKey.toEC(includePrivateKey);

        Assert.assertTrue(includePrivateKey == jsonWebKey.hasPrivateKey());

        PublicKey publicKey = keyPair2.getPublic();
        PrivateKey privateKey = keyPair2.getPrivate();

        if (includePrivateKey) {

            // set the missing properties to compare the keys
            jsonWebKey.withKid(new String(key.kid()));
            Assert.assertNotNull(privateKey);
            Assert.assertEquals(jsonWebKey, key);
            Assert.assertEquals(key.hashCode(), jsonWebKey.hashCode());
            signVerify(publicKey, privateKey, jsonWebKey.crv());
        }
    }

    private static void signVerify(PublicKey publicKey, PrivateKey privateKey, JsonWebKeyCurveName curve) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature signature = Signature.getInstance(CURVE_TO_SIGNATURE.get(curve), Security.getProvider("SunEC"));
        signature.initSign(privateKey);
        MessageDigest digest = MessageDigest.getInstance(algorithm.get(curve));
        byte[] plaintext = new byte[10];
        new Random().nextBytes(plaintext);
        byte[] hash = digest.digest(plaintext);
        signature.update(hash);
        byte[] signedHash = signature.sign();

        signature.initVerify(publicKey);
        signature.update(hash);
        Assert.assertTrue(signature.verify(signedHash));

    }

    static Map<JsonWebKeyCurveName, String> algorithm = ImmutableMap.<JsonWebKeyCurveName, String>builder()
            .put(JsonWebKeyCurveName.P_256, "SHA-256")
            .put(JsonWebKeyCurveName.P_384, "SHA-384")
            .put(JsonWebKeyCurveName.P_521, "SHA-512")
            .put(JsonWebKeyCurveName.P_256K, "SHA-256")
            .build();

    Map<Integer, String> keys = ImmutableMap.<Integer, String>builder()
        .put(256, "{\"kid\":\"key_id\",\"kty\":\"EC\",\"key_ops\":null,\"n\":null,\"e\":null,\"d\":\"AM_iqldq9VSqlf9v3w7lren4pJvZTG81v6_V5ZBLP7ZI\",\"dp\":null,\"dq\":null,\"qi\":null,\"p\":null,\"q\":null,\"k\":null,\"key_hsm\":null,\"crv\":\"P-256\",\"x\":\"KyjF795jLyVIgswKSQInEGYHNBKSKyPgNojEgYlldMI\",\"y\":\"AIl_ca1ZIKbJ5YGdgGr_7HySldI2aWeBaOImZEYIMpVe\"}")
        .put(384, "{\"kid\":\"key_id\",\"kty\":\"EC\",\"key_ops\":null,\"n\":null,\"e\":null,\"d\":\"AJEYT00mAfa-_uJ8S9ob0-9uZbPEr56CFebUQW9O-jZQBrtrMSPeqVbjJvTVlzOwbg\",\"dp\":null,\"dq\":null,\"qi\":null,\"p\":null,\"q\":null,\"k\":null,\"key_hsm\":null,\"crv\":\"P-384\",\"x\":\"AKOdkhxTtVkLtaslZIOPQGnsdKRT2xo3Ynk-bnAVvTCf3iGrTpRiMxUmyq_tvzBLEg\",\"y\":\"QoHux2O2XGMh8w7a5sWwskAyCR0g3Lj7kPGuvnDq_bQ_-_VoTvsGMAe9MFexv68I\"}")
        .put(521, "{\"kid\":\"key_id\",\"kty\":\"EC\",\"key_ops\":null,\"n\":null,\"e\":null,\"d\":\"AVW7TFJVOJ8jY5PqK0nnKyVYQwhkBEGKt0nhSZTS5io7U32dR7xZle77Gq6SpjrdFVa32jvGWgchlSguV3WKy3sj\",\"dp\":null,\"dq\":null,\"qi\":null,\"p\":null,\"q\":null,\"k\":null,\"key_hsm\":null,\"crv\":\"P-521\",\"x\":\"AIDmImOrJNKOjOGp7wD8Dzi_uz-00E7cs8iN5SwBkzBXktyRrLDFS_SMwVdnIWpLcdJQn5sTGDS121DhjQA2i2dO\",\"y\":\"AWRoeIfIoRoEx8V9ijjwaco3V6vUPUYvKMKxtCPvm8iwhB7pZAI7-mODSfkb3rZo3gxuWoM3G7L66FttUlKSLK4w\"}")
        .put(265, "{\"kid\":\"key_id\",\"kty\":\"EC\",\"key_ops\":null,\"n\":null,\"e\":null,\"d\":\"YKv22AkpwBpKUcDodNhKhvI-bRpiWqoN8l0kNCo-Mds\",\"dp\":null,\"dq\":null,\"qi\":null,\"p\":null,\"q\":null,\"k\":null,\"key_hsm\":null,\"crv\":\"P-256K\",\"x\":\"Yw9Sln8gYf_oiFY1anQm0V_WwsRaCIcEccfbhu5hSJo\",\"y\":\"AJq3JT2YldszaohHaS7LkngPWS9y0yAn7HhHb5p0IUDS\"}")
        .build();

    public static final Map<JsonWebKeyCurveName, String> CURVE_TO_SIGNATURE = ImmutableMap.<JsonWebKeyCurveName, String>builder()
            .put(JsonWebKeyCurveName.P_256, "SHA256withECDSA")
            .put(JsonWebKeyCurveName.P_384, "SHA384withECDSA")
            .put(JsonWebKeyCurveName.P_521, "SHA512withECDSA")
            .put(JsonWebKeyCurveName.P_256K, "NONEwithECDSA")
            .build();
}
