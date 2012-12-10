package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.AssetFileType;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityActionOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityUpdateOperation;

public class AssetFileEntityTest {
    private final String exampleAssetId = "nb:cid:UUID:bfe1c840-36c3-4a78-9b63-38e6eebd94c2";
    private final String exampleFileId = "nb:cid:UUID:49a229ad-8cc5-470f-9e0b-11838283aa58";
    private final String encodedAssetId = "'nb%3Acid%3AUUID%3Abfe1c840-36c3-4a78-9b63-38e6eebd94c2'";
    private final String encodedFileId = "'nb%3Acid%3AUUID%3A49a229ad-8cc5-470f-9e0b-11838283aa58'";

    public AssetFileEntityTest() throws Exception {

    }

    @Test
    public void createFileInfosHasExpectedUri() throws Exception {
        EntityActionOperation action = AssetFile.createFileInfos(exampleAssetId);

        assertEquals("CreateFileInfos", action.getUri());
        assertEquals(encodedAssetId, action.getQueryParameters().getFirst("assetid"));
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

        EntityCreationOperation<AssetFileInfo> creator = AssetFile.create(exampleAssetId, expectedName)
                .setContentChecksum(expectedChecksum).setContentFileSize(expectedSize)
                .setEncryptionKeyId(expectedEncryptionKey).setEncryptionScheme(expectedEncryptionScheme)
                .setEncryptionVersion(expectedEncryptionVersion).setInitializationVector(expectedInitializationVector)
                .setIsEncrypted(expectedIsEncrypted).setIsPrimary(expectedIsPrimary).setMimeType(expectedMimeType);

        AssetFileType payload = (AssetFileType) creator.getRequestContents();

        assertEquals(expectedName, payload.getName());
        assertEquals(exampleAssetId, payload.getParentAssetId());
        assertEquals(expectedSize, payload.getContentFileSize());
        assertEquals(expectedChecksum, payload.getContentChecksum());
        assertEquals(expectedEncryptionKey, payload.getEncryptionKeyId());
        assertEquals(expectedEncryptionScheme, payload.getEncryptionScheme());
        assertEquals(expectedEncryptionVersion, payload.getEncryptionVersion());
        assertEquals(expectedInitializationVector, payload.getInitializationVector());
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
    public void listFileInfosForAnAssetHasCorrectUri() throws Exception {
        String expectedUri = String.format("Assets(%s)/Files", encodedAssetId);
        EntityListOperation<AssetFileInfo> lister = AssetFile.list(exampleAssetId);

        assertEquals(expectedUri, lister.getUri());
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

        EntityUpdateOperation updater = AssetFile.update(exampleFileId).setContentChecksum(expectedChecksum)
                .setContentFileSize(expectedSize).setEncryptionKeyId(expectedEncryptionKey)
                .setEncryptionScheme(expectedEncryptionScheme).setEncryptionVersion(expectedEncryptionVersion)
                .setInitializationVector(expectedInitializationVector).setIsEncrypted(expectedIsEncrypted)
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
        assertEquals(expectedInitializationVector, payload.getInitializationVector());
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
