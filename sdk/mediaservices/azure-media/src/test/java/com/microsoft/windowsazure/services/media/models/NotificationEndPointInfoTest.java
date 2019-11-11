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

import com.microsoft.windowsazure.services.media.implementation.content.NotificationEndPointType;

public class NotificationEndPointInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        NotificationEndPointInfo notificationEndPointInfo = new NotificationEndPointInfo(
                null, new NotificationEndPointType().setId(expectedId));

        // Act
        String actualId = notificationEndPointInfo.getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "notificationEndPointName";
        NotificationEndPointInfo notificationEndPointInfo = new NotificationEndPointInfo(
                null, new NotificationEndPointType().setName(expectedName));

        // Act
        String actualName = notificationEndPointInfo.getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetCreated() throws Exception {
        // Arrange
        Date expectedCreated = new Date();

        NotificationEndPointInfo notificationEndPointInfo = new NotificationEndPointInfo(
                null,
                new NotificationEndPointType().setCreated(expectedCreated));

        // Act
        Date actualCreated = notificationEndPointInfo.getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);

    }

    @Test
    public void testGetSetEndPointType() throws Exception {
        // Arrange
        EndPointType expectedEndPointType = EndPointType.AzureQueue;
        NotificationEndPointInfo notificationEndPointInfo = new NotificationEndPointInfo(
                null,
                new NotificationEndPointType()
                        .setEndPointType(expectedEndPointType.getCode()));

        // Act
        EndPointType actualEndPointType = notificationEndPointInfo
                .getEndPointType();

        // Assert
        assertEquals(expectedEndPointType, actualEndPointType);
    }

    @Test
    public void testGetSetEndPointAddress() {
        // Arrange
        String expectedEndPointAddress = "testGetSetEndPointAddress";
        NotificationEndPointInfo notificationEndPointInfo = new NotificationEndPointInfo(
                null,
                new NotificationEndPointType()
                        .setEndPointAddress(expectedEndPointAddress));

        // Act
        String actualEndPointAddress = notificationEndPointInfo
                .getEndPointAddress();

        // Assert
        assertEquals(expectedEndPointAddress, actualEndPointAddress);
    }
}
