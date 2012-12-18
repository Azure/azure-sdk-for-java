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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.Security;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

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
import com.microsoft.windowsazure.services.media.models.AssetOption;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.JobState;
import com.microsoft.windowsazure.services.media.models.LinkInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.MediaProcessor;
import com.microsoft.windowsazure.services.media.models.MediaProcessorInfo;
import com.microsoft.windowsazure.services.media.models.ProtectionKey;
import com.microsoft.windowsazure.services.media.models.Task;
import com.microsoft.windowsazure.services.media.models.TaskInfo;
import com.microsoft.windowsazure.services.media.models.TaskState;

public class EncryptionIntegrationTest extends IntegrationTestBase {

    private final String strorageDecryptionProcessor = "Storage Decryption";

    private String createContentKeyId(UUID uuid) {
        String randomContentKey = String.format("nb:kid:UUID:%s", uuid);
        return randomContentKey;
    }

    private String getProtectionKeyId() throws ServiceException {
        String protectionKeyId = (String) service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.StorageEncryption));
        return protectionKeyId;
    }

    private JobInfo decodeAsset(String name, AssetInfo assetInfo) throws ServiceException, InterruptedException {
        MediaProcessorInfo mediaProcessorInfo = GetMediaProcessor(strorageDecryptionProcessor);
        JobInfo jobInfo = createJob(name, assetInfo, mediaProcessorInfo);
        return waitJobForCompletion(jobInfo);

    }

    private JobInfo createJob(String name, AssetInfo assetInfo, MediaProcessorInfo mediaProcessorInfo)
            throws ServiceException {
        // String configuration = "H.264 256k DSL CBR";
        String taskBody = "<taskBody>"
                + "<inputAsset>JobInputAsset(0)</inputAsset><outputAsset assetCreationOptions=\"0\" assetName=\"Output\">JobOutputAsset(0)</outputAsset></taskBody>";
        JobInfo jobInfo = service.create(Job.create().addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(Task.create(mediaProcessorInfo.getId(), taskBody).setName(name)));
        return jobInfo;
    }

    private JobInfo waitJobForCompletion(JobInfo jobInfo) throws ServiceException, InterruptedException {
        JobInfo currentJobInfo = jobInfo;
        while (currentJobInfo.getState().getCode() < 3) {
            currentJobInfo = service.get(Job.get(jobInfo.getId()));
            Thread.sleep(4000);
        }
        return currentJobInfo;
    }

    private MediaProcessorInfo GetMediaProcessor(String mediaProcessorName) throws ServiceException {
        List<MediaProcessorInfo> mediaProcessorInfos = service.list(MediaProcessor.list());
        for (MediaProcessorInfo mediaProcessorInfo : mediaProcessorInfos) {
            if (mediaProcessorInfo.getName().equals(mediaProcessorName)) {
                return mediaProcessorInfo;
            }
        }
        return null;
    }

    private void linkContentKey(AssetInfo assetInfo, ContentKeyInfo contentKeyInfo) throws ServiceException,
            UnsupportedEncodingException {
        URI contentKeyUri = createContentKeyUri(contentKeyInfo.getId());
        service.action(Asset.linkContentKey(assetInfo.getId(), contentKeyUri));
    }

    private String getProtectionKey(String protectionKeyId) throws ServiceException {
        String protectionKey = (String) service.action(ProtectionKey.getProtectionKey(protectionKeyId));
        return protectionKey;
    }

    private AssetFileInfo uploadEncryptedAssetFile(AssetInfo assetInfo, LocatorInfo locatorInfo,
            ContentKeyInfo contentKeyInfo, String blobName, byte[] encryptedContent) throws ServiceException {
        WritableBlobContainerContract blobWriter = service.createBlobWriter(locatorInfo);
        InputStream blobContent = new ByteArrayInputStream(encryptedContent);
        blobWriter.createBlockBlob(blobName, blobContent);
        AssetFileInfo assetFileInfo = service.create(AssetFile.create(assetInfo.getId(), blobName).setIsPrimary(true)
                .setIsEncrypted(true).setContentFileSize(new Long(encryptedContent.length))
                .setEncryptionScheme("StorageEncryption").setEncryptionVersion("1.0")
                .setEncryptionKeyId(contentKeyInfo.getId()));
        return assetFileInfo;
    }

    private URI createContentKeyUri(String contentKeyId) throws UnsupportedEncodingException {
        String escapedContentKeyId = URLEncoder.encode(contentKeyId, "UTF-8");
        return URI.create(String.format("ContentKeys('%s')", escapedContentKeyId));
    }

    private ContentKeyInfo createContentKey(byte[] aesKey, ContentKeyType contentKeyType, String protectionKeyId,
            String protectionKey) throws Exception {
        UUID contentKeyIdUuid = UUID.randomUUID();
        String contentKeyId = createContentKeyId(contentKeyIdUuid);
        byte[] encryptedContentKey = EncryptionHelper.EncryptSymmetricKey(protectionKey, aesKey);
        String encryptedContentKeyString = Base64.encode(encryptedContentKey);
        String checksum = EncryptionHelper.calculateChecksum(contentKeyIdUuid, aesKey);
        ContentKeyInfo contentKeyInfo = service.create(ContentKey
                .create(contentKeyId, contentKeyType, encryptedContentKeyString).setChecksum(checksum)
                .setProtectionKeyId(protectionKeyId));
        return contentKeyInfo;
    }

    @Test
    public void uploadAesProtectedAssetAndDownloadSuccess() throws Exception {
        // Arrange
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        InputStream smallWMVInputStream = getClass().getResourceAsStream("/media/SmallWMV.wmv");
        byte[] aesKey = EncryptionHelper.createRandomVector(256);
        byte[] initializationVector = EncryptionHelper.createRandomVector(128);
        int durationInMinutes = 10;

        // Act

        // creates asset
        AssetInfo assetInfo = service.create(Asset.create().setName("uploadAesProtectedAssetSuccess")
                .setOptions(AssetOption.StorageEncrypted));

        // creates writable access policy
        AccessPolicyInfo accessPolicyInfo = service.create(AccessPolicy.create("uploadAesPortectedAssetSuccess",
                durationInMinutes, EnumSet.of(AccessPolicyPermission.WRITE)));

        // creates locator for the input media asset
        LocatorInfo locatorInfo = service.create(Locator.create(accessPolicyInfo.getId(), assetInfo.getId(),
                LocatorType.SAS));

        // gets the public key for storage encryption. 

        String protectionKeyId = getProtectionKeyId();
        String protectionKey = getProtectionKey(protectionKeyId);

        // creates the content key with encrypted 
        ContentKeyInfo contentKeyInfo = createContentKey(aesKey, ContentKeyType.StorageEncryption, protectionKeyId,
                protectionKey);

        // link the content key with the asset. 
        linkContentKey(assetInfo, contentKeyInfo);

        // encrypt the file.
        byte[] encryptedContent = EncryptionHelper.EncryptFile(smallWMVInputStream, aesKey, initializationVector);

        // upload the encrypted file to the server.  
        AssetFileInfo assetFileInfo = uploadEncryptedAssetFile(assetInfo, locatorInfo, contentKeyInfo,
                "uploadAesProtectedAssetSuccess", encryptedContent);

        // submit and execute the decoding job. 
        JobInfo jobInfo = decodeAsset("uploadAesProtectedAssetSuccess", assetInfo);

        // assert 
        LinkInfo taskLinkInfo = jobInfo.getTasksLink();
        List<TaskInfo> taskInfos = service.list(Task.list(taskLinkInfo));
        for (TaskInfo taskInfo : taskInfos) {
            assertEquals(TaskState.Completed, taskInfo.getState());
            ListResult<AssetInfo> outputs = service.list(Asset.list(taskInfo.getOutputAssetsLink()));
            assertEquals(1, outputs.size());
        }
        assertEquals(JobState.Finished, jobInfo.getState());

    }

}
