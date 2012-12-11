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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

    public static byte[] create256BitRandomVector() {
        int numberOfBits = 256;
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

}
