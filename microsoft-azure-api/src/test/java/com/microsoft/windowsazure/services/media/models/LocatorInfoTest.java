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

public class LocatorInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "testId";
        LocatorInfo locator = new LocatorInfo();

        // Act
        String actualId = locator.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetExpirationDateTime() {
        // Arrange
        Date expectedExpirationDateTime = new Date();
        LocatorInfo locatorInfo = new LocatorInfo();

        // Act
        Date actualExpirationDateTime = locatorInfo.setExpirationDateTime(expectedExpirationDateTime)
                .getExpirationDateTime();

        // Assert
        assertEquals(expectedExpirationDateTime, actualExpirationDateTime);
    }

    @Test
    public void testGetSetType() {
        // Arrange
        LocatorType expectedLocatorType = LocatorType.WindowsAzureCDN;
        LocatorInfo locatorInfo = new LocatorInfo();

        // Act 
        LocatorType actualLocatorType = locatorInfo.setLocatorType(expectedLocatorType).getLocatorType();

        // Assert 
        assertEquals(expectedLocatorType, actualLocatorType);
    }

    @Test
    public void testGetSetPath() {
        // Arrange
        String expectedPath = "testPath";
        LocatorInfo locatorInfo = new LocatorInfo();

        // Act
        String actualPath = locatorInfo.setPath(expectedPath).getPath();

        // Assert
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testGetSetAccessPolicyId() {
        // Arrange
        String expectedAccessPolicyId = "testAccessPolicyId";
        LocatorInfo locatorInfo = new LocatorInfo();

        // Act
        String actualAccessPolicyId = locatorInfo.setAccessPolicyId(expectedAccessPolicyId).getAccessPolicyId();

        // Assert
        assertEquals(expectedAccessPolicyId, actualAccessPolicyId);
    }

    @Test
    public void testGetSetAssetId() {
        // Arrange
        String expectedAssetId = "testAssetId";
        LocatorInfo locatorInfo = new LocatorInfo();

        // Act
        String actualAssetId = locatorInfo.setAssetId(expectedAssetId).getAssetId();

        // Assert
        assertEquals(expectedAssetId, actualAssetId);
    }

    @Test
    public void testGetSetStartTime() {
        // Arrange
        Date expectedStartTime = new Date();
        LocatorInfo locatorInfo = new LocatorInfo();

        // Act
        Date actualStartTime = locatorInfo.setStartTime(expectedStartTime).getStartTime();

        // Assert 
        assertEquals(expectedStartTime, actualStartTime);
    }

}
