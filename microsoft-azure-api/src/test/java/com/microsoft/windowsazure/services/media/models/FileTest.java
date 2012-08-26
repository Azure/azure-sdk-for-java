/**
 * Copyright 2011 Microsoft Corporation
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

public class FileTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "testId";
        File file = new File();

        // Act
        String actualId = file.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testName";
        File file = new File();

        // Act
        String actualName = file.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetContentFileSize() {
        // Arrange
        int expectedContentFileSize = 1234;
        File file = new File();

        // Act
        int actualContentFileSize = file.setContentFileSize(expectedContentFileSize).getContentFileSize();

        // Assert
        assertEquals(expectedContentFileSize, actualContentFileSize);

    }

    @Test
    public void testGetSetParentAssetId() {
        // Arrange
        String expectedParentAssetId = "testParentAssetId";
        File file = new File();

        // Act
        String actualParentAssetId = file.setParentAssetId(expectedParentAssetId).getParentAssetId();

        // Assert
        assertEquals(expectedParentAssetId, actualParentAssetId);
    }

    @Test
    public void testGetSetEncryptionVersion() {
        // Arrange 
        String expectedEncryptionVersion = "testEncryptionVersion";
        File file = new File();

        // Act 
        String actualEncryptionVersion = file.setEncryptionVersion(expectedEncryptionVersion).getEncryptionVersion();

        // Assert
        assertEquals(expectedEncryptionVersion, actualEncryptionVersion);
    }

    @Test
    public void testGetSetEncryptionScheme() {
        // Arrange
        String expectedEncryptionScheme = "testEncryptionScheme";
        File file = new File();

        // Act
        String actualEncryptionScheme = file.setEncryptionScheme(expectedEncryptionScheme).getEncryptionScheme();

        // Assert
        assertEquals(expectedEncryptionScheme, actualEncryptionScheme);
    }

    @Test
    public void testGetSetIsEncrypted() {
        // Arrange 
        Boolean expectedIsEncrypted = true;
        File file = new File();

        // Act
        Boolean actualIsEncrypted = file.setIsEncrypted(expectedIsEncrypted).getIsEncrypted();

        // Assert
        assertEquals(expectedIsEncrypted, actualIsEncrypted);
    }

    @Test
    public void testGetSetEncryptionKeyId() {
        // Arrange 
        String expectedEncryptionKeyId = "testEncryptionKeyId";
        File file = new File();

        // Act
        String actualEncryptionKeyId = file.setEncryptionKeyId(expectedEncryptionKeyId).getEncryptionKeyId();

        // Assert 
        assertEquals(expectedEncryptionKeyId, actualEncryptionKeyId);
    }

    @Test
    public void testGetSetInitializationVector() {
        // Arrange 
        String expectedInitializationVector = "testInitializationVector";
        File file = new File();

        // Act
        String actualInitializationVector = file.setInitializationVector(expectedInitializationVector)
                .getInitializationVector();

        // Assert 
        assertEquals(expectedInitializationVector, actualInitializationVector);

    }

    @Test
    public void testGetSetIsPrimary() {
        // Arrange
        Boolean expectedIsPrimary = true;
        File file = new File();

        // Act
        Boolean actualIsPrimary = file.setIsPrimary(expectedIsPrimary).getIsPrimary();

        // Assert 
        assertEquals(expectedIsPrimary, actualIsPrimary);
    }

    @Test
    public void testGetSetLastModified() {
        // Arrange 
        Date expectedLastModified = new Date();
        File file = new File();

        // Act 
        Date actualLastModified = file.setLastModified(expectedLastModified).getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetCreated() {
        // Arrange
        Date expectedCreated = new Date();
        File file = new File();

        // Act 
        Date actualCreated = file.setCreated(expectedCreated).getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);
    }

    @Test
    public void testGetSetMimeType() {
        // Arrange
        String expectedMimeType = "testMimeType";
        File file = new File();

        // Act 
        String actualMimeType = file.setMimeType(expectedMimeType).getMimeType();

        // Assert 
        assertEquals(expectedMimeType, actualMimeType);
    }

    @Test
    public void testGetSetContentChecksum() {
        // Arrange 
        String expectedContentChecksum = "testContentChecksum";
        File file = new File();

        // Act
        String actualContentChecksum = file.setContentChecksum(expectedContentChecksum).getContentChecksum();

        // Assert
        assertEquals(expectedContentChecksum, actualContentChecksum);
    }
}
