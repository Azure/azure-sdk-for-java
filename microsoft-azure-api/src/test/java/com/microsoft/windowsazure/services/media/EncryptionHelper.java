/**
 * Copyright Microsoft Corporation
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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.windowsazure.services.core.storage.utils.Base64;

class EncryptionHelper {
    public static boolean canUseStrongCrypto() {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(new byte[32], "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public static byte[] encryptSymmetricKey(String protectionKey, byte[] inputData) throws Exception {
        byte[] protectionKeyBytes = Base64.decode(protectionKey);
        return encryptSymmetricKey(protectionKeyBytes, inputData);
    }

    public static byte[] encryptSymmetricKey(byte[] protectionKey, byte[] inputData) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(protectionKey);
        Certificate certificate = certificateFactory.generateCertificate(byteArrayInputStream);
        return encryptSymmetricKey(certificate, inputData);
    }

    public static byte[] encryptSymmetricKey(Certificate certificate, byte[] inputData) throws Exception {
        // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
        Key publicKey = certificate.getPublicKey();
        SecureRandom secureRandom = new SecureRandom();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, secureRandom);
        byte[] cipherText = cipher.doFinal(inputData);
        return cipherText;
    }

    public static String calculateContentKeyChecksum(String uuid, byte[] aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptionResult = cipher.doFinal(uuid.getBytes("UTF8"));
        byte[] checksumByteArray = new byte[8];
        System.arraycopy(encryptionResult, 0, checksumByteArray, 0, 8);
        String checksum = Base64.encode(checksumByteArray);
        return checksum;
    }

    public static InputStream encryptFile(InputStream inputStream, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
        return cipherInputStream;
    }

    public static byte[] decryptSymmetricKey(String rebindedContentKey, PrivateKey privateKey) throws Exception {
        for (Provider provider : Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key : provider.stringPropertyNames())
                System.out.println("\t" + key + "\t" + provider.getProperty(key));
        }

        byte[] rebindedContentKeyByteArray = Base64.decode(rebindedContentKey);
        return decryptSymmetricKey(rebindedContentKeyByteArray, privateKey);
    }

    public static byte[] decryptSymmetricKey(byte[] rebindedContentKey, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(rebindedContentKey);
        return decrypted;
    }

    public static X509Certificate loadX509Certificate(String certificateFileName) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        FileInputStream certificateInputStream = new FileInputStream(certificateFileName);
        X509Certificate x509Certificate = (X509Certificate) certificateFactory
                .generateCertificate(certificateInputStream);
        return x509Certificate;
    }

    public static PrivateKey getPrivateKey(String filename) throws Exception {

        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
