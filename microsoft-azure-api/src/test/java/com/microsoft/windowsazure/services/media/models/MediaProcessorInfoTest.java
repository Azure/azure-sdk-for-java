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

import org.junit.Test;

public class MediaProcessorInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        MediaProcessorInfo mediaProcessorInfo = new MediaProcessorInfo();

        // Act 
        String actualId = mediaProcessorInfo.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testName";
        MediaProcessorInfo mediaProcessorInfo = new MediaProcessorInfo();

        // Act
        String actualName = mediaProcessorInfo.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetDescription() throws Exception {
        // Arrange
        String expectedDescription = "testDescription";

        MediaProcessorInfo mediaProcessorInfo = new MediaProcessorInfo();

        // Act 
        String actualDescription = mediaProcessorInfo.setDescription(expectedDescription).getDescription();

        // Assert
        assertEquals(expectedDescription, actualDescription);

    }

    @Test
    public void testGetSetSku() throws Exception {
        // Arrange
        String expectedSku = "testSku";
        MediaProcessorInfo mediaProcessorInfo = new MediaProcessorInfo();

        // Act
        String actualSku = mediaProcessorInfo.setSku(expectedSku).getSku();

        // Assert
        assertEquals(expectedSku, actualSku);
    }

    @Test
    public void testGetSetVendor() {
        // Arrange
        String expectedVendor = "testVendor";
        MediaProcessorInfo mediaProcessorInfo = new MediaProcessorInfo();

        // Act
        String actualVendor = mediaProcessorInfo.setVendor(expectedVendor).getVendor();

        // Assert
        assertEquals(expectedVendor, actualVendor);
    }

    @Test
    public void testGetSetVersion() {
        // Arrange
        String expectedVersion = "testVersion";
        MediaProcessorInfo mediaProcessorInfo = new MediaProcessorInfo();

        // Act
        String actualVersion = mediaProcessorInfo.setName(expectedVersion).getVersion();

        // Assert
        assertEquals(expectedVersion, actualVersion);
    }

}
