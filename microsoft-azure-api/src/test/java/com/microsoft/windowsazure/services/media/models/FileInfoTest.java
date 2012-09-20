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
package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class FileInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "testId";
        FileInfo file = new FileInfo();

        // Act
        String actualId = file.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testName";
        FileInfo fileInfo = new FileInfo();

        // Act
        String actualName = fileInfo.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetContentFileSize() {
        // Arrange
        int expectedContentFileSize = 1234;
        FileInfo fileInfo = new FileInfo();

        // Act
        int actualContentFileSize = fileInfo.setContentFileSize(expectedContentFileSize).getContentFileSize();

        // Assert
        assertEquals(expectedContentFileSize, actualContentFileSize);

    }

    @Test
    public void testGetSetParentAssetId() {
        // Arrange
        String expectedParentAssetId = "testParentAssetId";
        FileInfo fileInfo = new FileInfo();

        // Act
        String actualParentAssetId = fileInfo.setParentAssetId(expectedParentAssetId).getParentAssetId();

        // Assert
        assertEquals(expectedParentAssetId, actualParentAssetId);
    }

    @Test
    public void testGetSetEncryptionVersion() {
        // Arrange 
        String expectedEncryptionVersion = "testEncryptionVersion";
        FileInfo fileInfo = new FileInfo();

        // Act 
        String actualEncryptionVersion = fileInfo.setEncryptionVersion(expectedEncryptionVersion)
                .getEncryptionVersion();

        // Assert
        assertEquals(expectedEncryptionVersion, actualEncryptionVersion);
    }

    @Test
    public void testGetSetEncryptionScheme() {
        // Arrange
        String expectedEncryptionScheme = "testEncryptionScheme";
        FileInfo fileInfo = new FileInfo();

        // Act
        String actualEncryptionScheme = fileInfo.setEncryptionScheme(expectedEncryptionScheme).getEncryptionScheme();

        // Assert
        assertEquals(expectedEncryptionScheme, actualEncryptionScheme);
    }

    @Test
    public void testGetSetIsEncrypted() {
        // Arrange 
        Boolean expectedIsEncrypted = true;
        FileInfo fileInfo = new FileInfo();

        // Act
        Boolean actualIsEncrypted = fileInfo.setIsEncrypted(expectedIsEncrypted).getIsEncrypted();

        // Assert
        assertEquals(expectedIsEncrypted, actualIsEncrypted);
    }

    @Test
    public void testGetSetEncryptionKeyId() {
        // Arrange 
        String expectedEncryptionKeyId = "testEncryptionKeyId";
        FileInfo fileInfo = new FileInfo();

        // Act
        String actualEncryptionKeyId = fileInfo.setEncryptionKeyId(expectedEncryptionKeyId).getEncryptionKeyId();

        // Assert 
        assertEquals(expectedEncryptionKeyId, actualEncryptionKeyId);
    }

    @Test
    public void testGetSetInitializationVector() {
        // Arrange 
        String expectedInitializationVector = "testInitializationVector";
        FileInfo fileInfo = new FileInfo();

        // Act
        String actualInitializationVector = fileInfo.setInitializationVector(expectedInitializationVector)
                .getInitializationVector();

        // Assert 
        assertEquals(expectedInitializationVector, actualInitializationVector);

    }

    @Test
    public void testGetSetIsPrimary() {
        // Arrange
        Boolean expectedIsPrimary = true;
        FileInfo fileInfo = new FileInfo();

        // Act
        Boolean actualIsPrimary = fileInfo.setIsPrimary(expectedIsPrimary).getIsPrimary();

        // Assert 
        assertEquals(expectedIsPrimary, actualIsPrimary);
    }

    @Test
    public void testGetSetLastModified() {
        // Arrange 
        Date expectedLastModified = new Date();
        FileInfo fileInfo = new FileInfo();

        // Act 
        Date actualLastModified = fileInfo.setLastModified(expectedLastModified).getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetCreated() {
        // Arrange
        Date expectedCreated = new Date();
        FileInfo fileInfo = new FileInfo();

        // Act 
        Date actualCreated = fileInfo.setCreated(expectedCreated).getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);
    }

    @Test
    public void testGetSetMimeType() {
        // Arrange
        String expectedMimeType = "testMimeType";
        FileInfo fileInfo = new FileInfo();

        // Act 
        String actualMimeType = fileInfo.setMimeType(expectedMimeType).getMimeType();

        // Assert 
        assertEquals(expectedMimeType, actualMimeType);
    }

    @Test
    public void testGetSetContentChecksum() {
        // Arrange 
        String expectedContentChecksum = "testContentChecksum";
        FileInfo fileInfo = new FileInfo();

        // Act
        String actualContentChecksum = fileInfo.setContentChecksum(expectedContentChecksum).getContentChecksum();

        // Assert
        assertEquals(expectedContentChecksum, actualContentChecksum);
    }
}
