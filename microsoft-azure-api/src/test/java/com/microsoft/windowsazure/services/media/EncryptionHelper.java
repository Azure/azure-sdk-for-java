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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.windowsazure.services.core.storage.utils.Base64;

public class EncryptionHelper {
    public static void RSAOAEP() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        byte[] input = "abc".getBytes();
        Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
        SecureRandom random = new SecureRandom();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

        generator.initialize(386, random);

        KeyPair pair = generator.generateKeyPair();
        Key pubKey = pair.getPublic();

        Key privKey = pair.getPrivate();

        cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);
        byte[] cipherText = cipher.doFinal(input);
        System.out.println("cipher: " + new String(cipherText));

        cipher.init(Cipher.DECRYPT_MODE, privKey);
        byte[] plainText = cipher.doFinal(cipherText);
        System.out.println("plain : " + new String(plainText));
    }

    public static byte[] createRandomVector(int numberOfBits) {
        int numberOfBytes = numberOfBits / 8;
        byte[] aesKey = new byte[numberOfBytes];
        Random random = new Random();
        random.nextBytes(aesKey);
        return aesKey;
    }

    public static void AESCTR() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        byte[] input = "www.java2s.com".getBytes();
        byte[] keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c,
                0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
        byte[] ivBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x00, 0x01, 0x02, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x01 };

        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        System.out.println("input : " + new String(input));

        // encryption pass
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        ByteArrayInputStream bIn = new ByteArrayInputStream(input);
        CipherInputStream cIn = new CipherInputStream(bIn, cipher);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        int ch;
        while ((ch = cIn.read()) >= 0) {
            bOut.write(ch);
        }

        byte[] cipherText = bOut.toByteArray();

        System.out.println("cipher: " + new String(cipherText));

        // decryption pass
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        bOut = new ByteArrayOutputStream();
        CipherOutputStream cOut = new CipherOutputStream(bOut, cipher);
        cOut.write(cipherText);
        cOut.close();
        System.out.println("plain : " + new String(bOut.toByteArray()));
    }

    public static byte[] EncryptSymmetricKey(String protectionKey, byte[] inputData) throws InvalidKeySpecException,
            NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, CertificateException {

        X509Certificate x509Certificate = createX509CertificateFromString(protectionKey);
        //X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(publicKeyString));
        // PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
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
        int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        // teArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);
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
