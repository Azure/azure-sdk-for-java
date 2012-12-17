/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.windowsazure.services.core.storage.utils.Base64;

public class EncryptionHelper {

    public static byte[] createRandomVector(int numberOfBits) {
        int numberOfBytes = numberOfBits / 8;
        byte[] aesKey = new byte[numberOfBytes];
        Random random = new Random();
        random.nextBytes(aesKey);
        return aesKey;
    }

    public static byte[] EncryptSymmetricKey(String protectionKey, byte[] inputData) throws InvalidKeySpecException,
            NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, CertificateException {

        X509Certificate x509Certificate = createX509CertificateFromString(protectionKey);
        return EncryptSymmetricKey(x509Certificate.getPublicKey(), inputData);
    }

    private static X509Certificate createX509CertificateFromString(String protectionKey) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.decode(protectionKey));
        X509Certificate x509cert = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
        return x509cert;

    }

    public static byte[] EncryptSymmetricKey(Key publicKey, byte[] inputData) throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
        SecureRandom secureRandom = new SecureRandom();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, secureRandom);
        byte[] cipherText = cipher.doFinal(inputData);
        return cipherText;
    }

    public static String calculateChecksum(UUID uuid, byte[] aesKey) throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptionResult = cipher.doFinal(uuid.toString().getBytes());
        byte[] checksumByteArray = new byte[8];
        for (int i = 0; i < 8; i++) {
            checksumByteArray[i] = encryptionResult[i];
        }
        String checksum = Base64.encode(checksumByteArray);
        return checksum;
    }

    public static byte[] EncryptFile(InputStream inputStream, byte[] aesKey, byte[] initializationVector)
            throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException {
        // preparation
        SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");

        // encryption
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ch;
        while ((ch = cipherInputStream.read()) >= 0) {
            byteArrayOutputStream.write(ch);
        }

        byte[] cipherText = byteArrayOutputStream.toByteArray();
        return cipherText;
    }

}
