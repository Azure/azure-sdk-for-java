/**
 * Copyright 2013 Microsoft Corporation
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
import java.io.InputStream;
import java.security.Key;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.windowsazure.services.core.storage.utils.Base64;

class EncryptionHelper {
    public static byte[] encryptSymmetricKey(String protectionKey, byte[] inputData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        byte[] protectionKeyBytes = Base64.decode(protectionKey);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(protectionKeyBytes);
        Certificate certificate = certificateFactory.generateCertificate(byteArrayInputStream);
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
}
