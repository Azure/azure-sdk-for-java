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

public class ContentKeyTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        ContentKey contentKey = new ContentKey();

        // Act 
        String actualId = contentKey.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetCreated() {
        // Arrange
        Date expectedCreated = new Date();
        ContentKey contentKey = new ContentKey();

        // Act
        Date actualCreated = contentKey.setCreated(expectedCreated).getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);
    }

    @Test
    public void testGetSetLastModified() {
        // Arrange
        Date expectedLastModified = new Date();
        ContentKey contentKey = new ContentKey();

        // Act
        Date actualLastModified = contentKey.setLastModified(expectedLastModified).getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetContentKeyType() {
        // Arrange
        ContentKeyType expectedContentKeyType = ContentKeyType.ConfigurationEncryption;
        ContentKey contentKey = new ContentKey();

        // Act 
        ContentKeyType actualContentKeyType = contentKey.setContentKeyType(expectedContentKeyType).getContentKeyType();

        // Assert
        assertEquals(expectedContentKeyType, actualContentKeyType);

    }

    @Test
    public void testGetSetEncryptedContentKey() {
        // Arrange 
        String expectedEncryptedContentKey = "testX509Certificate";
        ContentKey contentKey = new ContentKey();

        // Act
        String actualEncryptedContentKey = contentKey.setEncryptedContentKey(expectedEncryptedContentKey)
                .getEncryptedContentKey();

        // Assert
        assertEquals(expectedEncryptedContentKey, actualEncryptedContentKey);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "expectedName";
        ContentKey contentKey = new ContentKey();

        // Act
        String actualName = contentKey.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetProtectionKeyId() {
        // Arrange 
        String expectedProtectionKeyId = "expectedProtectionKeyId";
        ContentKey contentKey = new ContentKey();

        // Act
        String actualProtectionKeyId = contentKey.setProtectionKeyId(expectedProtectionKeyId).getProtectionKeyId();

        // Assert 
        assertEquals(expectedProtectionKeyId, actualProtectionKeyId);

    }

    @Test
    public void testGetSetProtectionKeyType() {
        // Arrange
        ProtectionKeyType expectedProtectionKeyType = ProtectionKeyType.X509CertificateThumbprint;
        ContentKey contentKey = new ContentKey();

        // Act
        ProtectionKeyType actualProtectionKeyType = contentKey.setProtectionKeyType(expectedProtectionKeyType)
                .getProtectionKeyType();

        // Assert
        assertEquals(expectedProtectionKeyType, actualProtectionKeyType);
    }

    @Test
    public void testGetSetCheckSum() {
        // Arrange 
        String expectedCheckSum = "testCheckSum";
        ContentKey contentKey = new ContentKey();

        // Act 
        String actualCheckSum = contentKey.setCheckSum(expectedCheckSum).getCheckSum();

        // Assert
        assertEquals(expectedCheckSum, actualCheckSum);

    }

}
