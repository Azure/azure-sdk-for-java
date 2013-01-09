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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
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

    @Test
    public void uploadAesProtectedAssetAndDownloadSuccess() throws Exception {
        // Arrange

        // Media Services requires 256-bit (32-byte) keys and
        // 128-bit (16-byte) initialization vectors (IV) for AES encryption,
        // and also requires that only the first 8 bytes of the IV is filled.
        Random random = new Random();
        byte[] aesKey = new byte[32];
        random.nextBytes(aesKey);
        byte[] effectiveIv = new byte[8];
        random.nextBytes(effectiveIv);
        byte[] iv = new byte[16];
        System.arraycopy(effectiveIv, 0, iv, 0, effectiveIv.length);

        InputStream mpeg4H264InputStream = getClass().getResourceAsStream("/media/MPEG4-H264.mp4");
        InputStream encryptedContent = EncryptionHelper.encryptFile(mpeg4H264InputStream, aesKey, iv);
        int durationInMinutes = 10;

        // Act
        AssetInfo assetInfo = service.create(Asset.create().setName(testAssetPrefix + "uploadAesProtectedAssetSuccess")
                .setOptions(AssetOption.StorageEncrypted));
        WritableBlobContainerContract blobWriter = getBlobWriter(assetInfo.getId(), durationInMinutes);

        // gets the public key for storage encryption.
        String contentKeyId = makeContentKeyId(aesKey);

        // link the content key with the asset.
        service.action(Asset.linkContentKey(assetInfo.getId(), contentKeyId));

        // upload the encrypted file to the server.
        uploadEncryptedAssetFile(assetInfo, blobWriter, "MPEG4-H264.mp4", encryptedContent, contentKeyId, iv);

        // submit and execute the decoding job.
        JobInfo jobInfo = decodeAsset("uploadAesProtectedAssetSuccess", assetInfo.getId());

        // assert
        LinkInfo<TaskInfo> taskLinkInfo = jobInfo.getTasksLink();
        List<TaskInfo> taskInfos = service.list(Task.list(taskLinkInfo));
        for (TaskInfo taskInfo : taskInfos) {
            assertEquals(TaskState.Completed, taskInfo.getState());
            ListResult<AssetInfo> outputs = service.list(Asset.list(taskInfo.getOutputAssetsLink()));
            assertEquals(1, outputs.size());
        }
        assertEquals(JobState.Finished, jobInfo.getState());

        // Verify that the contents match
        InputStream expected = getClass().getResourceAsStream("/media/MPEG4-H264.mp4");

        ListResult<AssetInfo> outputAssets = service.list(Asset.list(jobInfo.getOutputAssetsLink()));
        assertEquals(1, outputAssets.size());
        AssetInfo outputAsset = outputAssets.get(0);
        ListResult<AssetFileInfo> assetFiles = service.list(AssetFile.list(assetInfo.getAssetFilesLink()));
        assertEquals(1, assetFiles.size());
        AssetFileInfo outputFile = assetFiles.get(0);

        InputStream actual = getFileContents(outputAsset.getId(), outputFile.getName(), durationInMinutes);
        assertStreamsEqual(expected, actual);
    }

    private JobInfo decodeAsset(String name, String inputAssetId) throws ServiceException, InterruptedException {
        MediaProcessorInfo mediaProcessorInfo = service.list(
                MediaProcessor.list().set("$filter", "Name eq '" + strorageDecryptionProcessor + "'")).get(0);

        String taskBody = "<taskBody>"
                + "<inputAsset>JobInputAsset(0)</inputAsset><outputAsset assetCreationOptions=\"0\" assetName=\"Output\">JobOutputAsset(0)</outputAsset></taskBody>";
        JobInfo jobInfo = service.create(Job.create().addInputMediaAsset(inputAssetId)
                .addTaskCreator(Task.create(mediaProcessorInfo.getId(), taskBody).setName(name)));

        JobInfo currentJobInfo = jobInfo;
        int retryCounter = 0;
        while (currentJobInfo.getState().getCode() < 3 && retryCounter < 30) {
            Thread.sleep(10000);
            currentJobInfo = service.get(Job.get(jobInfo.getId()));
            retryCounter++;
        }
        return currentJobInfo;
    }

    private String makeContentKeyId(byte[] aesKey) throws ServiceException, Exception {
        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(ContentKeyType.StorageEncryption));
        String protectionKey = service.action(ProtectionKey.getProtectionKey(protectionKeyId));

        String contentKeyIdUuid = UUID.randomUUID().toString();
        String contentKeyId = String.format("nb:kid:UUID:%s", contentKeyIdUuid);

        byte[] encryptedContentKey = EncryptionHelper.encryptSymmetricKey(protectionKey, aesKey);
        String encryptedContentKeyString = Base64.encode(encryptedContentKey);
        String checksum = EncryptionHelper.calculateContentKeyChecksum(contentKeyIdUuid, aesKey);

        ContentKeyInfo contentKeyInfo = service.create(ContentKey
                .create(contentKeyId, ContentKeyType.StorageEncryption, encryptedContentKeyString)
                .setChecksum(checksum).setProtectionKeyId(protectionKeyId));
        return contentKeyInfo.getId();
    }

    private void uploadEncryptedAssetFile(AssetInfo asset, WritableBlobContainerContract blobWriter, String blobName,
            InputStream blobContent, String encryptionKeyId, byte[] iv) throws ServiceException {
        blobWriter.createBlockBlob(blobName, blobContent);
        service.action(AssetFile.createFileInfos(asset.getId()));
        ListResult<AssetFileInfo> files = service.list(AssetFile.list(asset.getAssetFilesLink()).set("$filter",
                "Name eq '" + blobName + "'"));
        assertEquals(1, files.size());
        AssetFileInfo file = files.get(0);
        byte[] sub = new byte[9];
        // Offset bytes to ensure that the sign-bit is not set.
        // Media Services expects unsigned Int64 values.
        System.arraycopy(iv, 0, sub, 1, 8);
        BigInteger longIv = new BigInteger(sub);
        String initializationVector = longIv.toString();

        service.update(AssetFile.update(file.getId()).setIsEncrypted(true).setEncryptionKeyId(encryptionKeyId)
                .setEncryptionScheme("StorageEncryption").setEncryptionVersion("1.0")
                .setInitializationVector(initializationVector));
    }

    private WritableBlobContainerContract getBlobWriter(String assetId, int durationInMinutes) throws ServiceException {
        AccessPolicyInfo accessPolicyInfo = service.create(AccessPolicy.create(testPolicyPrefix
                + "uploadAesPortectedAssetSuccess", durationInMinutes, EnumSet.of(AccessPolicyPermission.WRITE)));

        // creates locator for the input media asset
        LocatorInfo locatorInfo = service.create(Locator.create(accessPolicyInfo.getId(), assetId, LocatorType.SAS));
        WritableBlobContainerContract blobWriter = service.createBlobWriter(locatorInfo);
        return blobWriter;
    }

    private InputStream getFileContents(String assetId, String fileName, int availabilityWindowInMinutes)
            throws ServiceException, InterruptedException, IOException {
        AccessPolicyInfo readAP = service.create(AccessPolicy.create(testPolicyPrefix + "tempAccessPolicy",
                availabilityWindowInMinutes, EnumSet.of(AccessPolicyPermission.READ)));
        LocatorInfo readLocator = service.create(Locator.create(readAP.getId(), assetId, LocatorType.SAS));
        URL file = new URL(readLocator.getBaseUri() + "/" + fileName + readLocator.getContentAccessToken());

        // There can be a delay before a new read locator is applied for the asset files.
        InputStream reader = null;
        for (int counter = 0; true; counter++) {
            try {
                reader = file.openConnection().getInputStream();
                break;
            }
            catch (IOException e) {
                System.out.println("Got error, wait a bit and try again");
                if (counter < 6) {
                    Thread.sleep(10000);
                }
                else {
                    // No more retries.
                    throw e;
                }
            }
        }

        return reader;
    }

    private void assertStreamsEqual(InputStream inputStream1, InputStream inputStream2) throws IOException {
        byte[] buffer1 = new byte[1024];
        byte[] buffer2 = new byte[1024];
        try {
            while (true) {
                int n1 = inputStream1.read(buffer1);
                int n2 = inputStream2.read(buffer2);
                assertEquals("number of bytes read from streams", n1, n2);
                if (n1 == -1) {
                    break;
                }
                for (int i = 0; i < n1; i++) {
                    assertEquals("byte " + i + " read from streams", buffer1[i], buffer2[i]);
                }
            }
        }
        finally {
            inputStream1.close();
            inputStream2.close();
        }
    }
}
