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

package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.entityoperations.EntityActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.AssetFileType;

public class AssetFileEntityTest {
    private final String exampleAssetId = "nb:cid:UUID:bfe1c840-36c3-4a78-9b63-38e6eebd94c2";
    private final String exampleFileId = "nb:cid:UUID:49a229ad-8cc5-470f-9e0b-11838283aa58";
    private final String encodedAssetId = "'nb%3Acid%3AUUID%3Abfe1c840-36c3-4a78-9b63-38e6eebd94c2'";
    private final String encodedFileId = "'nb%3Acid%3AUUID%3A49a229ad-8cc5-470f-9e0b-11838283aa58'";

    public AssetFileEntityTest() throws Exception {

    }

    @Test
    public void createFileInfosHasExpectedUri() throws Exception {
        EntityActionOperation action = AssetFile
                .createFileInfos(exampleAssetId);

        assertEquals("CreateFileInfos", action.getUri());
        assertEquals(encodedAssetId,
                action.getQueryParameters().getFirst("assetid"));
    }

    @Test
    public void createWithOptionsSetsOptionsAsExpected() throws Exception {
        String expectedName = "newFile.mp4";
        Long expectedSize = 65432L;
        String expectedChecksum = "check";
        String expectedEncryptionKey = "ooh, secret!";
        String expectedEncryptionScheme = "scheme";
        String expectedEncryptionVersion = "some version";
        String expectedInitializationVector = "cross product";
        Boolean expectedIsEncrypted = true;
        Boolean expectedIsPrimary = true;
        String expectedMimeType = "application/octet-stream";

        EntityCreateOperation<AssetFileInfo> creator = AssetFile
                .create(exampleAssetId, expectedName)
                .setContentChecksum(expectedChecksum)
                .setContentFileSize(expectedSize)
                .setEncryptionKeyId(expectedEncryptionKey)
                .setEncryptionScheme(expectedEncryptionScheme)
                .setEncryptionVersion(expectedEncryptionVersion)
                .setInitializationVector(expectedInitializationVector)
                .setIsEncrypted(expectedIsEncrypted)
                .setIsPrimary(expectedIsPrimary).setMimeType(expectedMimeType);

        AssetFileType payload = (AssetFileType) creator.getRequestContents();

        assertEquals(expectedName, payload.getName());
        assertEquals(exampleAssetId, payload.getParentAssetId());
        assertEquals(expectedSize, payload.getContentFileSize());
        assertEquals(expectedChecksum, payload.getContentChecksum());
        assertEquals(expectedEncryptionKey, payload.getEncryptionKeyId());
        assertEquals(expectedEncryptionScheme, payload.getEncryptionScheme());
        assertEquals(expectedEncryptionVersion, payload.getEncryptionVersion());
        assertEquals(expectedInitializationVector,
                payload.getInitializationVector());
        assertEquals(expectedIsEncrypted, payload.getIsEncrypted());
        assertEquals(expectedIsPrimary, payload.getIsPrimary());
        assertEquals(expectedMimeType, payload.getMimeType());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
    }

    @Test
    public void getByIdHasCorrectUri() throws Exception {
        String expectedUri = String.format("Files(%s)", encodedFileId);
        EntityGetOperation<AssetFileInfo> getter = AssetFile.get(exampleFileId);

        assertEquals(expectedUri, getter.getUri());
    }

    @Test
    public void listAllFileInfosHasCorrectUri() throws Exception {
        String expectedUri = "Files";
        EntityListOperation<AssetFileInfo> lister = AssetFile.list();
        assertEquals(expectedUri, lister.getUri());
    }

    @Test
    public void updateWithAllOptionsHasCorrectPayload() throws Exception {
        Long expectedSize = 65432L;
        String expectedChecksum = "check";
        String expectedEncryptionKey = "ooh, secret!";
        String expectedEncryptionScheme = "scheme";
        String expectedEncryptionVersion = "some version";
        String expectedInitializationVector = "cross product";
        Boolean expectedIsEncrypted = true;
        Boolean expectedIsPrimary = true;
        String expectedMimeType = "application/octet-stream";

        EntityUpdateOperation updater = AssetFile.update(exampleFileId)
                .setContentChecksum(expectedChecksum)
                .setContentFileSize(expectedSize)
                .setEncryptionKeyId(expectedEncryptionKey)
                .setEncryptionScheme(expectedEncryptionScheme)
                .setEncryptionVersion(expectedEncryptionVersion)
                .setInitializationVector(expectedInitializationVector)
                .setIsEncrypted(expectedIsEncrypted)
                .setIsPrimary(expectedIsPrimary).setMimeType(expectedMimeType);

        AssetFileType payload = (AssetFileType) updater.getRequestContents();

        assertNull(payload.getName());
        assertNull(payload.getId());
        assertNull(payload.getParentAssetId());
        assertEquals(expectedSize, payload.getContentFileSize());
        assertEquals(expectedChecksum, payload.getContentChecksum());
        assertEquals(expectedEncryptionKey, payload.getEncryptionKeyId());
        assertEquals(expectedEncryptionScheme, payload.getEncryptionScheme());
        assertEquals(expectedEncryptionVersion, payload.getEncryptionVersion());
        assertEquals(expectedInitializationVector,
                payload.getInitializationVector());
        assertEquals(expectedIsEncrypted, payload.getIsEncrypted());
        assertEquals(expectedIsPrimary, payload.getIsPrimary());
        assertEquals(expectedMimeType, payload.getMimeType());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
    }

    @Test
    public void deleteHasCorrectUri() throws Exception {
        String expectedUri = String.format("Files(%s)", encodedFileId);
        EntityDeleteOperation deleter = AssetFile.delete(exampleFileId);

        assertEquals(expectedUri, deleter.getUri());
    }
}
