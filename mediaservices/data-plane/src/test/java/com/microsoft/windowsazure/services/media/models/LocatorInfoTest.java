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

import com.microsoft.windowsazure.services.media.implementation.content.LocatorRestType;

public class LocatorInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "testId";
        LocatorInfo locator = new LocatorInfo(null,
                new LocatorRestType().setId(expectedId));

        // Act
        String actualId = locator.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetSetExpirationDateTime() {
        // Arrange
        Date expectedExpirationDateTime = new Date();
        LocatorInfo locatorInfo = new LocatorInfo(null,
                new LocatorRestType()
                        .setExpirationDateTime(expectedExpirationDateTime));

        // Act
        Date actualExpirationDateTime = locatorInfo.getExpirationDateTime();

        // Assert
        assertEquals(expectedExpirationDateTime, actualExpirationDateTime);
    }

    @Test
    public void testGetSetType() {
        // Arrange
        LocatorType expectedLocatorType = LocatorType.SAS;
        LocatorInfo locatorInfo = new LocatorInfo(null,
                new LocatorRestType().setType(expectedLocatorType.getCode()));

        // Act
        LocatorType actualLocatorType = locatorInfo.getLocatorType();

        // Assert
        assertEquals(expectedLocatorType, actualLocatorType);
    }

    @Test
    public void testGetSetPath() {
        // Arrange
        String expectedPath = "testPath";
        LocatorInfo locatorInfo = new LocatorInfo(null,
                new LocatorRestType().setPath(expectedPath));

        // Act
        String actualPath = locatorInfo.getPath();

        // Assert
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testGetSetAccessPolicyId() {
        // Arrange
        String expectedAccessPolicyId = "testAccessPolicyId";
        LocatorInfo locatorInfo = new LocatorInfo(null,
                new LocatorRestType().setAccessPolicyId(expectedAccessPolicyId));

        // Act
        String actualAccessPolicyId = locatorInfo.getAccessPolicyId();

        // Assert
        assertEquals(expectedAccessPolicyId, actualAccessPolicyId);
    }

    @Test
    public void testGetSetAssetId() {
        // Arrange
        String expectedAssetId = "testAssetId";
        LocatorInfo locatorInfo = new LocatorInfo(null,
                new LocatorRestType().setAssetId(expectedAssetId));

        // Act
        String actualAssetId = locatorInfo.getAssetId();

        // Assert
        assertEquals(expectedAssetId, actualAssetId);
    }

    @Test
    public void testGetSetStartTime() {
        // Arrange
        Date expectedStartTime = new Date();
        LocatorInfo locatorInfo = new LocatorInfo(null,
                new LocatorRestType().setStartTime(expectedStartTime));

        // Act
        Date actualStartTime = locatorInfo.getStartTime();

        // Assert
        assertEquals(expectedStartTime, actualStartTime);
    }

    @Test
    public void testGetSetBaseUri() {
        // Arrange
        String expectedBaseUri = "testBaseUri";
        LocatorInfo locatorInfo = new LocatorInfo(null,
                new LocatorRestType().setBaseUri(expectedBaseUri));

        // Act
        String actualBaseUri = locatorInfo.getBaseUri();

        // Assert
        assertEquals(expectedBaseUri, actualBaseUri);
    }

    @Test
    public void testGetSetContentAccessComponent() {
        // Arrange
        String expectedContentAccessComponent = "testContentAccessToken";
        LocatorInfo locatorInfo = new LocatorInfo(
                null,
                new LocatorRestType()
                        .setContentAccessComponent(expectedContentAccessComponent));

        // Act
        String actualContentAccessComponent = locatorInfo
                .getContentAccessToken();

        // Assert
        assertEquals(expectedContentAccessComponent,
                actualContentAccessComponent);
    }
}
