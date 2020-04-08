// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.google.common.io.BaseEncoding;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

public class TestUtils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static AeadAes256CbcHmac256EncryptionKey instantiateAeadAes256CbcHmac256EncryptionKey(byte[] key) {
        return new AeadAes256CbcHmac256EncryptionKey(key, "AES");
    }

    public static AeadAes256CbcHmac256Algorithm instantiateAeadAes256CbcHmac256Algorithm(AeadAes256CbcHmac256EncryptionKey aeadAesKey, EncryptionType encryptionType, byte version) {
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
}
