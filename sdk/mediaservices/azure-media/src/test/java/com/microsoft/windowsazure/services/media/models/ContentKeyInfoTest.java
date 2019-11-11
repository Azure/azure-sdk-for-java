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

import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyRestType;

public class ContentKeyInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType().setId(expectedId));

        // Act
        String actualId = contentKeyInfo.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetCreated() {
        // Arrange
        Date expectedCreated = new Date();
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType().setCreated(expectedCreated));

        // Act
        Date actualCreated = contentKeyInfo.getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);
    }

    @Test
    public void testGetSetLastModified() {
        // Arrange
        Date expectedLastModified = new Date();
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType().setLastModified(expectedLastModified));

        // Act
        Date actualLastModified = contentKeyInfo.getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetContentKeyType() {
        // Arrange
        ContentKeyType expectedContentKeyType = ContentKeyType.ConfigurationEncryption;
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType()
                        .setContentKeyType(expectedContentKeyType.getCode()));

        // Act
        ContentKeyType actualContentKeyType = contentKeyInfo
                .getContentKeyType();

        // Assert
        assertEquals(expectedContentKeyType, actualContentKeyType);

    }

    @Test
    public void testGetSetEncryptedContentKey() {
        // Arrange
        String expectedEncryptedContentKey = "testX509Certificate";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType()
                        .setEncryptedContentKey(expectedEncryptedContentKey));

        // Act
        String actualEncryptedContentKey = contentKeyInfo
                .getEncryptedContentKey();

        // Assert
        assertEquals(expectedEncryptedContentKey, actualEncryptedContentKey);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "expectedName";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType().setName(expectedName));

        // Act
        String actualName = contentKeyInfo.getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetProtectionKeyId() {
        // Arrange
        String expectedProtectionKeyId = "expectedProtectionKeyId";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType()
                        .setProtectionKeyId(expectedProtectionKeyId));

        // Act
        String actualProtectionKeyId = contentKeyInfo.getProtectionKeyId();

        // Assert
        assertEquals(expectedProtectionKeyId, actualProtectionKeyId);

    }

    @Test
    public void testGetSetProtectionKeyType() {
        // Arrange
        ProtectionKeyType expectedProtectionKeyType = ProtectionKeyType.X509CertificateThumbprint;
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType()
                        .setProtectionKeyType(expectedProtectionKeyType
                                .getCode()));

        // Act
        ProtectionKeyType actualProtectionKeyType = contentKeyInfo
                .getProtectionKeyType();

        // Assert
        assertEquals(expectedProtectionKeyType, actualProtectionKeyType);
    }

    @Test
    public void testGetSetCheckSum() {
        // Arrange
        String expectedCheckSum = "testCheckSum";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType().setChecksum(expectedCheckSum));

        // Act
        String actualCheckSum = contentKeyInfo.getChecksum();

        // Assert
        assertEquals(expectedCheckSum, actualCheckSum);

    }
    
    @Test
    public void testGetSetAuthorizationPolicyId() {
        // Arrange
        String expectedAuthorizationPolicyId = "testAuthorizationPolicyId";
        ContentKeyInfo contentKeyInfo = new ContentKeyInfo(null,
                new ContentKeyRestType().setAuthorizationPolicyId(expectedAuthorizationPolicyId));

        // Act
        String actualAuthorizationPolicyId = contentKeyInfo.getAuthorizationPolicyId();

        // Assert
        assertEquals(expectedAuthorizationPolicyId, actualAuthorizationPolicyId);
    }

}
