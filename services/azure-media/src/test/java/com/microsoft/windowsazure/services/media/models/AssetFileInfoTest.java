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

import java.util.Date;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.AssetFileType;

public class AssetFileInfoTest {

    @Test
    public void testGetSetId() {
        String expectedId = "testId";
        AssetFileInfo file = new AssetFileInfo(null,
                new AssetFileType().setId(expectedId));

        String actualId = file.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetName() {
        String expectedName = "testName";
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setName(expectedName));

        String actualName = fileInfo.getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetContentFileSize() {
        // Arrange
        Long expectedContentFileSize = 1234l;
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setContentFileSize(expectedContentFileSize));

        // Act
        long actualContentFileSize = fileInfo.getContentFileSize();

        // Assert
        assertEquals(expectedContentFileSize.longValue(), actualContentFileSize);

    }

    @Test
    public void testGetSetParentAssetId() {
        String expectedParentAssetId = "testParentAssetId";
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setParentAssetId(expectedParentAssetId));

        String actualParentAssetId = fileInfo.getParentAssetId();

        assertEquals(expectedParentAssetId, actualParentAssetId);
    }

    @Test
    public void testGetSetEncryptionVersion() {
        String expectedEncryptionVersion = "testEncryptionVersion";
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType()
                        .setEncryptionVersion(expectedEncryptionVersion));

        String actualEncryptionVersion = fileInfo.getEncryptionVersion();

        assertEquals(expectedEncryptionVersion, actualEncryptionVersion);
    }

    @Test
    public void testGetSetEncryptionScheme() {
        // Arrange
        String expectedEncryptionScheme = "testEncryptionScheme";
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType()
                        .setEncryptionScheme(expectedEncryptionScheme));

        // Act
        String actualEncryptionScheme = fileInfo.getEncryptionScheme();

        // Assert
        assertEquals(expectedEncryptionScheme, actualEncryptionScheme);
    }

    @Test
    public void testGetSetIsEncrypted() {
        // Arrange
        Boolean expectedIsEncrypted = true;
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setIsEncrypted(expectedIsEncrypted));

        // Act
        Boolean actualIsEncrypted = fileInfo.getIsEncrypted();

        // Assert
        assertEquals(expectedIsEncrypted, actualIsEncrypted);
    }

    @Test
    public void testGetSetEncryptionKeyId() {
        String expectedEncryptionKeyId = "testEncryptionKeyId";
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setEncryptionKeyId(expectedEncryptionKeyId));

        String actualEncryptionKeyId = fileInfo.getEncryptionKeyId();

        assertEquals(expectedEncryptionKeyId, actualEncryptionKeyId);
    }

    @Test
    public void testGetSetInitializationVector() {
        String expectedInitializationVector = "testInitializationVector";
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType()
                        .setInitializationVector(expectedInitializationVector));

        String actualInitializationVector = fileInfo.getInitializationVector();

        assertEquals(expectedInitializationVector, actualInitializationVector);

    }

    @Test
    public void testGetSetIsPrimary() {
        // Arrange
        Boolean expectedIsPrimary = true;
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setIsPrimary(expectedIsPrimary));

        // Act
        Boolean actualIsPrimary = fileInfo.getIsPrimary();

        // Assert
        assertEquals(expectedIsPrimary, actualIsPrimary);
    }

    @Test
    public void testGetSetLastModified() {
        Date expectedLastModified = new Date();
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setLastModified(expectedLastModified));

        Date actualLastModified = fileInfo.getLastModified();

        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetCreated() {
        Date expectedCreated = new Date();
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setCreated(expectedCreated));

        Date actualCreated = fileInfo.getCreated();

        assertEquals(expectedCreated, actualCreated);
    }

    @Test
    public void testGetSetMimeType() {
        String expectedMimeType = "testMimeType";
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setMimeType(expectedMimeType));

        String actualMimeType = fileInfo.getMimeType();

        assertEquals(expectedMimeType, actualMimeType);
    }

    @Test
    public void testGetSetContentChecksum() {
        String expectedContentChecksum = "testContentChecksum";
        AssetFileInfo fileInfo = new AssetFileInfo(null,
                new AssetFileType().setContentChecksum(expectedContentChecksum));

        String actualContentChecksum = fileInfo.getContentChecksum();

        assertEquals(expectedContentChecksum, actualContentChecksum);
    }
}
