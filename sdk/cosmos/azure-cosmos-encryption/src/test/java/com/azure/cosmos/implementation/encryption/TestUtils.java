// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.encryption.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.EncryptionType;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

public class TestUtils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void initialized() {}

    public static AeadAes256CbcHmac256EncryptionKey instantiateAeadAes256CbcHmac256EncryptionKey(byte[] key) {
        return new AeadAes256CbcHmac256EncryptionKey(key, "AES");
    }

    public static AeadAes256CbcHmac256Algorithm instantiateAeadAes256CbcHmac256Algorithm(AeadAes256CbcHmac256EncryptionKey aeadAesKey,
                                                                                         EncryptionType encryptionType,
                                                                                         byte version) {
        return new AeadAes256CbcHmac256Algorithm(aeadAesKey, EncryptionType.RANDOMIZED, version);
    }

    private static byte[] hexToByteArray(String hex) {
        return BaseEncoding.base16().decode(hex);
    }

    public static byte[] generatePBEKeySpec(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Random random = new Random();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256); // AES-256
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        return keySpec.getEncoded();
    }

    public static InputStream getResourceAsInputStream(String path) {
        return TestUtils.class.getClassLoader().getResourceAsStream(path);
    }

    public static byte[] getResourceAsByteArray(String path) {
        try {
            return ByteStreams.toByteArray(getResourceAsInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T loadPojo(String path, Class<T> classType) {
        try {
            return Utils.getSimpleObjectMapper().readValue(getResourceAsByteArray(path), classType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataEncryptionKey createDataEncryptionKey() throws Exception {
        byte[] key = TestUtils.generatePBEKeySpec("testPass");

        AeadAes256CbcHmac256EncryptionKey aeadAesKey = TestUtils.instantiateAeadAes256CbcHmac256EncryptionKey(key);
        AeadAes256CbcHmac256Algorithm encryptionAlgorithm = TestUtils.instantiateAeadAes256CbcHmac256Algorithm(aeadAesKey, EncryptionType.RANDOMIZED, (byte) 0x01);
        DataEncryptionKey javaDataEncryptionKey = new DataEncryptionKey() {

            @Override
            public byte[] getRawKey() {
                return key;
            }

            @Override
            public String getEncryptionAlgorithm() {
                return CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED;
            }

            @Override
            public byte[] encryptData(byte[] plainText) {
                return encryptionAlgorithm.encryptData(plainText);
            }

            @Override
            public byte[] decryptData(byte[] cipherText) {
                return encryptionAlgorithm.decryptData(cipherText);
            }
        };
        return javaDataEncryptionKey;
    }
}
