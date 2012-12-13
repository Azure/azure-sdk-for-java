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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFileInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.EncryptionOption;
import com.microsoft.windowsazure.services.media.models.ProtectionKey;

public class EncryptionIntegrationTest extends IntegrationTestBase {

    private final String validButNonexistContentKeyId = "nb:kid:UUID:80dfe751-e5a1-4b29-a992-4a75276473af";
    private final ContentKeyType testContentKeyType = ContentKeyType.CommonEncryption;
    private final String testEncryptedContentKey = "ThisIsEncryptedContentKey";

    private String createRandomContentKeyId() {
        UUID uuid = UUID.randomUUID();
        String randomContentKey = String.format("nb:kid:UUID:%s", uuid);
        return randomContentKey;
    }

    private String getProtectionKey(ContentKeyType contentKeyType) {
        String protectionKeyId = null;
        try {
            protectionKeyId = (String) service.action(ProtectionKey.getProtectionKeyId(contentKeyType));
        }
        catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        String protectionKey;
        try {
            protectionKey = (String) service.action(ProtectionKey.getProtectionKey(protectionKeyId));
        }
        catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        return protectionKey;
    }

    @Test
    public void uploadAesProtectedAssetAndDownloadSuccess() throws Exception {
        // Arrange
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        InputStream smallWMVInputStream = getClass().getResourceAsStream("/media/SmallWMV.wmv");

        // Act
        byte[] aesKey = EncryptionHelper.createRandomVector(128);
        byte[] initializationVector = EncryptionHelper.createRandomVector(128);

        AssetInfo assetInfo = service.create(Asset.create().setName("uploadAesProtectedAssetSuccess")
                .setOptions(EncryptionOption.StorageEncrypted));
        String protectionKeyId = (String) service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.StorageEncryption));
        String protectionKey = (String) service.action(ProtectionKey.getProtectionKey(protectionKeyId));
        ContentKeyInfo contentKeyInfo = createContentKey(aesKey, ContentKeyType.StorageEncryption, protectionKey);
        URI contentKeyUri = createContentKeyUri(service.getRestServiceUri(), contentKeyInfo.getId());
        service.action(Asset.linkContentKey(assetInfo.getId(), contentKeyUri));

        byte[] encryptedContent = EncryptFile(smallWMVInputStream, aesKey, initializationVector);
        AssetFileInfo assetFileInfo = uploadEncryptedAssetFile(assetInfo, protectionKeyId, encryptedContent, aesKey,
                initializationVector);

        // executeDecodingJob(assetInfo);

        // Assert
        // verify the file downloaded is identical to the one that is uploaded. 

    }

    private AssetFileInfo uploadEncryptedAssetFile(AssetInfo assetInfo, String protectionKeyId,
            byte[] encryptedContent, byte[] aesKey, byte[] initializationVector) {
        // TODO Auto-generated method stub
        return null;
    }

    private URI createContentKeyUri(URI rootUri, String contentKeyId) {
        return URI.create(String.format("%s/ContentKeys('%s')", rootUri, contentKeyId));
    }

    private byte[] EncryptFile(InputStream inputStream, byte[] aesKey, byte[] initializationVector)
            throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException {
        // preparation
        SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");

        // encryption
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
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

    private ContentKeyInfo createContentKey(byte[] aesKey, ContentKeyType contentKeyType, String protectionKey)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ServiceException,
            CertificateException {
        String contentKeyId = createRandomContentKeyId();
        byte[] encryptedContentKey = EncryptionHelper.EncryptSymmetricKey(protectionKey, aesKey);
        ContentKeyInfo contentKeyInfo = service.create(ContentKey.create(contentKeyId, contentKeyType,
                Base64.encode(encryptedContentKey)));
        return contentKeyInfo;
    }

    private AssetInfo createAsset(String string) {
        // TODO Auto-generated method stub
        return null;
    }

}
