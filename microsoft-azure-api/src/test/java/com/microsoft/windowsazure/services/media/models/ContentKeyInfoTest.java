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

public class ContentKeyInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act 
        String actualId = contentKeyInfo.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetCreated() {
        // Arrange
        Date expectedCreated = new Date();
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act
        Date actualCreated = contentKeyInfo.setCreated(expectedCreated).getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);
    }

    @Test
    public void testGetSetLastModified() {
        // Arrange
        Date expectedLastModified = new Date();
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act
        Date actualLastModified = contentKeyInfo.setLastModified(expectedLastModified).getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetContentKeyType() {
        // Arrange
        ContentKeyType expectedContentKeyType = ContentKeyType.ConfigurationEncryption;
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act 
        ContentKeyType actualContentKeyType = contentKeyInfo.setContentKeyType(expectedContentKeyType)
                .getContentKeyType();

        // Assert
        assertEquals(expectedContentKeyType, actualContentKeyType);

    }

    @Test
    public void testGetSetEncryptedContentKey() {
        // Arrange 
        String expectedEncryptedContentKey = "testX509Certificate";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act
        String actualEncryptedContentKey = contentKeyInfo.setEncryptedContentKey(expectedEncryptedContentKey)
                .getEncryptedContentKey();

        // Assert
        assertEquals(expectedEncryptedContentKey, actualEncryptedContentKey);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "expectedName";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act
        String actualName = contentKeyInfo.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetProtectionKeyId() {
        // Arrange 
        String expectedProtectionKeyId = "expectedProtectionKeyId";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act
        String actualProtectionKeyId = contentKeyInfo.setProtectionKeyId(expectedProtectionKeyId).getProtectionKeyId();

        // Assert 
        assertEquals(expectedProtectionKeyId, actualProtectionKeyId);

    }

    @Test
    public void testGetSetProtectionKeyType() {
        // Arrange
        ProtectionKeyType expectedProtectionKeyType = ProtectionKeyType.X509CertificateThumbprint;
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act
        ProtectionKeyType actualProtectionKeyType = contentKeyInfo.setProtectionKeyType(expectedProtectionKeyType)
                .getProtectionKeyType();

        // Assert
        assertEquals(expectedProtectionKeyType, actualProtectionKeyType);
    }

    @Test
    public void testGetSetCheckSum() {
        // Arrange 
        String expectedCheckSum = "testCheckSum";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo();

        // Act 
        String actualCheckSum = contentKeyInfo.setChecksum(expectedCheckSum).getCheckSum();

        // Assert
        assertEquals(expectedCheckSum, actualCheckSum);

    }

}
