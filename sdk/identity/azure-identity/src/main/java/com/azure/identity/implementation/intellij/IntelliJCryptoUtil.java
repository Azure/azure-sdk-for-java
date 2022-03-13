// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.intellij;

import com.azure.identity.CredentialUnavailableException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class IntelliJCryptoUtil {

    private static final byte[] SALSA20_IV = decodeHexString("E830094B97205D2A");

    public static MessageDigest getMessageDigestSHA256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new CredentialUnavailableException("Algorithm SHA-256 is not supported."
                + " Decryption of IntelliJ Token data is not possible.", e);
        }
    }

    public static byte[] createKey(byte[] key, byte[] baseSeed, byte[] transformSeed, long rounds) {

        final byte[] transformedKey = Aes.transformKey(transformSeed, key, rounds);

        final MessageDigest md = getMessageDigestSHA256();
        final byte[] transformedKeyDigest = md.digest(transformedKey);

        md.update(baseSeed);
        return md.digest(transformedKeyDigest);
    }


    public static InputStream getDecryptedInputStream(InputStream encryptedInputStream, byte[] keyData, byte[] ivData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            SecretKey key = new SecretKeySpec(keyData, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivData);

            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return new CipherInputStream(encryptedInputStream, cipher);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
            | InvalidAlgorithmParameterException e) {
            throw new CredentialUnavailableException("Error Decrypting the IntelliJ cache database.", e);
        }
    }


    public static Salsa20 createSalsa20CryptoEngine(byte[] key) {
        Salsa20 salsa20Decrypt;
        try {
            MessageDigest md = getMessageDigestSHA256();
            byte[] mdKey = md.digest(key);

            salsa20Decrypt = new Salsa20();
            salsa20Decrypt.engineInitDecrypt(mdKey, SALSA20_IV);
        } catch (Exception e) {
            throw new CredentialUnavailableException("Error creating the Salsa 20 Decryption Engine.", e);
        }
        return salsa20Decrypt;
    }

    public static byte[] decrypt(byte[] encryptedText, Salsa20 salsa20) {
        try {
            return salsa20.crypt(encryptedText, 0, encryptedText.length);
        } catch (Exception e) {
            throw new CredentialUnavailableException("Error decrypting the IntelliJ database.", e);
        }
    }

    public static InputStream createDecryptedStream(byte[] digest, InputStream inputStream,
                                             IntelliJKdbxMetadata kdbxMetadata) {
        byte[] finalKeyDigest = IntelliJCryptoUtil.createKey(digest, kdbxMetadata.getBaseSeed(),
            kdbxMetadata.getTransformSeed(), kdbxMetadata.getTransformRounds());
        return IntelliJCryptoUtil.getDecryptedInputStream(inputStream, finalKeyDigest, kdbxMetadata.getEncryptionIv());
    }

    public static byte[] decodeHexString(String string) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < string.length(); i += 2) {
            int b = Integer.parseInt(string.substring(i, i + 2), 16);
            outputStream.write(b);
        }
        return outputStream.toByteArray();
    }
}
