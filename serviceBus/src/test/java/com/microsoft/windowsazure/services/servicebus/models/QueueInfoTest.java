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
package com.microsoft.windowsazure.services.servicebus.models;

import static org.junit.Assert.*;

import java.util.Calendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Test;

import com.microsoft.windowsazure.services.servicebus.implementation.EntityAvailabilityStatus;
import com.microsoft.windowsazure.services.servicebus.implementation.EntityStatus;
import com.microsoft.windowsazure.services.servicebus.implementation.PartitioningPolicy;

public class QueueInfoTest {

    private Duration createDuration(int milliSeconds) {
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        return datatypeFactory.newDuration(milliSeconds);
    }

    @Test
    public void testGetSetLockDuration() {
        // Arrange
        Duration expectedLockDuration = createDuration(100);
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Duration actualLockDuration = queueInfo.setLockDuration(
                expectedLockDuration).getLockDuration();

        // Assert
        assertEquals(expectedLockDuration, actualLockDuration);

    }

    @Test
    public void testGetSetMaxSizeInMegabytes() {
        // Arrange
        Long expectedMaxSizeInMegabytes = 1024L;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Long actualMaxSizeInMegabytes = queueInfo.setMaxSizeInMegabytes(
                expectedMaxSizeInMegabytes).getMaxSizeInMegabytes();

        // Assert
        assertEquals(expectedMaxSizeInMegabytes, actualMaxSizeInMegabytes);

    }

    @Test
    public void testGetSetRequiresDuplicateDetection() {
        // Arrange
        Boolean expectedRequiresDuplicateDetection = true;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Boolean actualRequiresDuplicateDetection = queueInfo
                .setRequiresDuplicateDetection(
                        expectedRequiresDuplicateDetection)
                .isRequiresDuplicateDetection();

        // Assert
        assertEquals(expectedRequiresDuplicateDetection,
                actualRequiresDuplicateDetection);

    }

    @Test
    public void testGetSetRequiresSession() {
        // Arrange
        Boolean expectedRequiresSession = true;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Boolean actualRequiresSession = queueInfo.setRequiresSession(
                expectedRequiresSession).isRequiresSession();

        // Assert
        assertEquals(expectedRequiresSession, actualRequiresSession);
    }

    @Test
    public void testGetSetDefaultMessageTimeToLive() {
        // Arrange
        Duration expectedDefaultMessageTimeToLive = createDuration(100);
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Duration actualDefaultMessageTimeToLive = queueInfo
                .setDefaultMessageTimeToLive(expectedDefaultMessageTimeToLive)
                .getDefaultMessageTimeToLive();

        // Assert
        assertEquals(expectedDefaultMessageTimeToLive,
                actualDefaultMessageTimeToLive);
    }

    @Test
    public void testGetSetDeadLetteringOnMessageExpiration() {
        // Arrange
        Boolean expectedDeadLetteringOnMessageExpiration = true;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Boolean actualDeadLetteringOnMessageExpiration = queueInfo
                .setDeadLetteringOnMessageExpiration(
                        expectedDeadLetteringOnMessageExpiration)
                .isDeadLetteringOnMessageExpiration();

        // Assert
        assertEquals(expectedDeadLetteringOnMessageExpiration,
                actualDeadLetteringOnMessageExpiration);
    }

    @Test
    public void testGetSetDuplicateDetectionHistoryTimeWindow() {
        // Arrange
        Duration expectedDefaultMessageTimeToLive = createDuration(100);
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Duration actualDefaultMessageTimeToLive = queueInfo
                .setDefaultMessageTimeToLive(expectedDefaultMessageTimeToLive)
                .getDefaultMessageTimeToLive();

        // Assert
        assertEquals(expectedDefaultMessageTimeToLive,
                actualDefaultMessageTimeToLive);
    }

    @Test
    public void testGetSetMaxDeliveryCount() {
        // Arrange
        Integer expectedMaxDeliveryCount = 1024;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Integer actualMaxDeliveryCount = queueInfo.setMaxDeliveryCount(
                expectedMaxDeliveryCount).getMaxDeliveryCount();

        // Assert
        assertEquals(expectedMaxDeliveryCount, actualMaxDeliveryCount);
    }

    @Test
    public void testGetSetEnableBatchedOperations() {
        // Arrange
        Boolean expectedEnableBatchedOperations = true;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Boolean actualEnableBatchedOperations = queueInfo
                .setEnableBatchedOperations(expectedEnableBatchedOperations)
                .isEnableBatchedOperations();

        // Assert
        assertEquals(expectedEnableBatchedOperations,
                actualEnableBatchedOperations);
    }

    @Test
    public void testGetSetSizeInBytes() {
        // Arrange
        Long expectedSizeInBytes = 1024L;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Long actualSizeInBytes = queueInfo.setSizeInBytes(expectedSizeInBytes)
                .getSizeInBytes();

        // Assert
        assertEquals(expectedSizeInBytes, actualSizeInBytes);
    }

    @Test
    public void testGetSetMessageCount() {
        // Arrange
        Long expectedMessageCount = 1024L;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Long actualMessageCount = queueInfo.setMessageCount(
                expectedMessageCount).getMessageCount();

        // Assert
        assertEquals(expectedMessageCount, actualMessageCount);
    }

    @Test
    public void testGetSetIsAnonymousAccessible() {
        // Arrange
        Boolean expectedIsAnonymousAccessible = true;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Boolean actualIsAnonymousAccessible = queueInfo
                .setIsAnonymousAccessible(expectedIsAnonymousAccessible)
                .isAnonymousAccessible();

        // Assert
        assertEquals(expectedIsAnonymousAccessible, actualIsAnonymousAccessible);
    }

    @Test
    public void testGetSetStatus() {
        // Arrange
        EntityStatus expectedEntityStatus = EntityStatus.ACTIVE;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        EntityStatus actualEntityStatus = queueInfo.setStatus(
                expectedEntityStatus).getStatus();

        // Assert
        assertEquals(expectedEntityStatus, actualEntityStatus);
    }

    @Test
    public void testGetSetCreatedAt() {
        // Arrange
        Calendar expectedCreatedAt = Calendar.getInstance();
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Calendar actualCreatedAt = queueInfo.setCreatedAt(expectedCreatedAt)
                .getCreatedAt();

        // Assert
        assertEquals(expectedCreatedAt, actualCreatedAt);
    }

    @Test
    public void testGetSetUpdatedAt() {
        // Arrange
        Calendar expectedUpdatedAt = Calendar.getInstance();
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Calendar actualUpdatedAt = queueInfo.setUpdatedAt(expectedUpdatedAt)
                .getUpdatedAt();

        // Assert
        assertEquals(expectedUpdatedAt, actualUpdatedAt);
    }

    @Test
    public void testGetSetAccessedAt() {
        // Arrange
        Calendar expectedAccessedAt = Calendar.getInstance();
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Calendar actualAccessedAt = queueInfo.setAccessedAt(expectedAccessedAt)
                .getAccessedAt();

        // Assert
        assertEquals(expectedAccessedAt, actualAccessedAt);
    }

    @Test
    public void testGetSetUserMetadata() {
        // Arrange
        String expectedUserMetadata = "expectedUserMetaData";
        QueueInfo queueInfo = new QueueInfo();

        // Act
        String actualUserMetadata = queueInfo.setUserMetadata(
                expectedUserMetadata).getUserMetadata();

        // Assert
        assertEquals(expectedUserMetadata, actualUserMetadata);
    }

    @Test
    public void testGetSetSupportOrdering() {
        // Arrange
        Boolean expectedIsSupportOrdering = true;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Boolean actualIsSupportOrdering = queueInfo.setSupportOrdering(
                expectedIsSupportOrdering).isSupportOrdering();

        // Assert
        assertEquals(expectedIsSupportOrdering, actualIsSupportOrdering);
    }

    @Test
    public void testGetSetAutoDeleteOnIdle() {
        // Arrange
        Duration expectedIsAutoDeleteOnIdle = createDuration(100);
        QueueInfo queueInfo = new QueueInfo();

        // Act
        Duration actualIsAutoDeleteOnIdle = queueInfo.setAutoDeleteOnIdle(
                expectedIsAutoDeleteOnIdle).getAutoDeleteOnIdle();

        // Assert
        assertEquals(expectedIsAutoDeleteOnIdle, actualIsAutoDeleteOnIdle);
    }

    @Test
    public void testGetSetPartioningPolicy() {
        // Arrange
        PartitioningPolicy expectedPartitioningPolicy = PartitioningPolicy.NO_PARTITIONING;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        PartitioningPolicy actualPartitioningPolicy = queueInfo
                .setPartitioningPolicy(expectedPartitioningPolicy)
                .getPartitioningPolicy();

        // Assert
        assertEquals(expectedPartitioningPolicy, actualPartitioningPolicy);
    }

    @Test
    public void testGetSetEntityAvailabilityStatus() {
        // Arrange
        EntityAvailabilityStatus expectedEntityAvailabilityStatus = EntityAvailabilityStatus.AVAILABLE;
        QueueInfo queueInfo = new QueueInfo();

        // Act
        EntityAvailabilityStatus actualEntityAvailabilityStatus = queueInfo
                .setEntityAvailabilityStatus(expectedEntityAvailabilityStatus)
                .getEntityAvailabilityStatus();

        // Assert
        assertEquals(expectedEntityAvailabilityStatus,
                actualEntityAvailabilityStatus);
    }

}
