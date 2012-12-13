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
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.EnumSet;
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
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetFileInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.EncryptionOption;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.ProtectionKey;

public class EncryptionIntegrationTest extends IntegrationTestBase {

    private final String validButNonexistContentKeyId = "nb:kid:UUID:80dfe751-e5a1-4b29-a992-4a75276473af";
    private final ContentKeyType testContentKeyType = ContentKeyType.CommonEncryption;
    private final String testEncryptedContentKey = "ThisIsEncryptedContentKey";

    private String createContentKeyId(UUID uuid) {
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
        byte[] aesKey = EncryptionHelper.createRandomVector(128);
        byte[] initializationVector = EncryptionHelper.createRandomVector(128);
        int durationInMinutes = 10;

        // Act
        AssetInfo assetInfo = service.create(Asset.create().setName("uploadAesProtectedAssetSuccess")
                .setOptions(EncryptionOption.StorageEncrypted));

        AccessPolicyInfo accessPolicyInfo = service.create(AccessPolicy.create("uploadAesPortectedAssetSuccess",
                durationInMinutes, EnumSet.of(AccessPolicyPermission.WRITE)));

        LocatorInfo locatorInfo = service.create(Locator.create(accessPolicyInfo.getId(), assetInfo.getId(),
                LocatorType.SAS));

        String protectionKey = getProtectionKey();

        ContentKeyInfo contentKeyInfo = createContentKey(aesKey, ContentKeyType.StorageEncryption, protectionKey);

        linkContentKey(assetInfo, contentKeyInfo);

        byte[] encryptedContent = EncryptFile(smallWMVInputStream, aesKey, initializationVector);
        AssetFileInfo assetFileInfo = uploadEncryptedAssetFile(assetInfo, locatorInfo, contentKeyInfo,
                "uploadAesProtectedAssetSuccess", encryptedContent);

        // executeDecodingJob(assetInfo);

        // Assert
        // verify the file downloaded is identical to the one that is uploaded. 

    }

    private void linkContentKey(AssetInfo assetInfo, ContentKeyInfo contentKeyInfo) throws ServiceException {
        URI contentKeyUri = createContentKeyUri(service.getRestServiceUri(), contentKeyInfo.getId());
        service.action(Asset.linkContentKey(assetInfo.getId(), contentKeyUri));
    }

    private String getProtectionKey() throws ServiceException {
        String protectionKeyId = (String) service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.StorageEncryption));
        String protectionKey = (String) service.action(ProtectionKey.getProtectionKey(protectionKeyId));
        return protectionKey;
    }

    private AssetFileInfo uploadEncryptedAssetFile(AssetInfo assetInfo, LocatorInfo locatorInfo,
            ContentKeyInfo contentKeyInfo, String blobName, byte[] encryptedContent) throws ServiceException {
        WritableBlobContainerContract blobWriter = MediaService.createBlobWriter(locatorInfo);
        InputStream blobContent = new ByteArrayInputStream(encryptedContent);
        blobWriter.createBlockBlob(blobName, blobContent);
        AssetFileInfo assetFileInfo = service.create(AssetFile.create(assetInfo.getId(), blobName).setIsPrimary(true)
                .setIsEncrypted(true).setContentFileSize(new Long(encryptedContent.length))
                .setEncryptionScheme("StorageEncryption").setEncryptionVersion("1.0")
                .setEncryptionKeyId(contentKeyInfo.getId()));
        return assetFileInfo;
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
        UUID contentKeyIdUuid = UUID.randomUUID();
        String contentKeyId = createContentKeyId(contentKeyIdUuid);
        byte[] encryptedContentKey = EncryptionHelper.EncryptSymmetricKey(protectionKey, aesKey);
        String encryptedContentKeyString = Base64.encode(encryptedContentKey);
        String checksum = EncryptionHelper.calculateChecksum(contentKeyIdUuid, aesKey);
        ContentKeyInfo contentKeyInfo = service.create(ContentKey.create(contentKeyId, contentKeyType,
                encryptedContentKeyString).setChecksum(checksum));
        return contentKeyInfo;
    }

    private AssetInfo createAsset(String string) {
        // TODO Auto-generated method stub
        return null;
    }

}
