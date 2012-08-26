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

public class LocatorTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "testId";
        Locator locator = new Locator();

        // Act
        String actualId = locator.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetExpirationDateTime() {
        // Arrange
        Date expectedExpirationDateTime = new Date();
        Locator locator = new Locator();

        // Act
        Date actualExpirationDateTime = locator.setExpirationDateTime(expectedExpirationDateTime)
                .getExpirationDateTime();

        // Assert
        assertEquals(expectedExpirationDateTime, actualExpirationDateTime);
    }

    @Test
    public void testGetSetType() {
        // Arrange
        LocatorType expectedLocatorType = LocatorType.WindowsAzureCDN;
        Locator locator = new Locator();

        // Act 
        LocatorType actualLocatorType = locator.setLocatorType(expectedLocatorType).getLocatorType();

        // Assert 
        assertEquals(expectedLocatorType, actualLocatorType);
    }

    @Test
    public void testGetSetPath() {
        // Arrange
        String expectedPath = "testPath";
        Locator locator = new Locator();

        // Act
        String actualPath = locator.setPath(expectedPath).getPath();

        // Assert
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testGetSetAccessPolicyId() {
        // Arrange
        String expectedAccessPolicyId = "testAccessPolicyId";
        Locator locator = new Locator();

        // Act
        String actualAccessPolicyId = locator.setAccessPolicyId(expectedAccessPolicyId).getAccessPolicyId();

        // Assert
        assertEquals(expectedAccessPolicyId, actualAccessPolicyId);
    }

    @Test
    public void testGetSetAssetId() {
        // Arrange
        String expectedAssetId = "testAssetId";
        Locator locator = new Locator();

        // Act
        String actualAssetId = locator.setAssetId(expectedAssetId).getAssetId();

        // Assert
        assertEquals(expectedAssetId, actualAssetId);
    }

    @Test
    public void testGetSetStartTime() {
        // Arrange
        Date expectedStartTime = new Date();
        Locator locator = new Locator();

        // Act
        Date actualStartTime = locator.setStartTime(expectedStartTime).getStartTime();

        // Assert 
        assertEquals(expectedStartTime, actualStartTime);
    }

}
