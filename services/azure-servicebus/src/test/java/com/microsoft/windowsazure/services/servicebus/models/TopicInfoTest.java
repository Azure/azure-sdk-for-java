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

public class TopicInfoTest {

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
    public void testGetSetDefaultMessageTimeToLive() {
        // Arrange
        Duration expectedDefaultMessageTimeToLive = createDuration(1024);
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Duration actualDefaultMessageTimeToLive = topicInfo
                .setDefaultMessageTimeToLive(expectedDefaultMessageTimeToLive)
                .getDefaultMessageTimeToLive();

        // Assert
        assertEquals(expectedDefaultMessageTimeToLive,
                actualDefaultMessageTimeToLive);

    }

    @Test
    public void testGetSetMaxSizeInMegabytes() {
        // Arrange
        Long expectedMaxSizeInMegabytes = 1024L;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Long actualMaxSizeInMegabytes = topicInfo.setMaxSizeInMegabytes(
                expectedMaxSizeInMegabytes).getMaxSizeInMegabytes();

        // Assert
        assertEquals(expectedMaxSizeInMegabytes, actualMaxSizeInMegabytes);

    }

    @Test
    public void testGetSetRequiresDuplicateDetection() {
        // Arrange
        Boolean expectedRequiresDuplicateDetection = true;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Boolean actualRequiresDuplicateDetection = topicInfo
                .setRequiresDuplicateDetection(
                        expectedRequiresDuplicateDetection)
                .isRequiresDuplicateDetection();

        // Assert
        assertEquals(expectedRequiresDuplicateDetection,
                actualRequiresDuplicateDetection);

    }

    @Test
    public void testGetSetDuplicateDetectionHistoryTimeWindow() {
        // Arrange
        Duration expectedDefaultMessageTimeToLive = createDuration(100);
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Duration actualDefaultMessageTimeToLive = topicInfo
                .setDefaultMessageTimeToLive(expectedDefaultMessageTimeToLive)
                .getDefaultMessageTimeToLive();

        // Assert
        assertEquals(expectedDefaultMessageTimeToLive,
                actualDefaultMessageTimeToLive);
    }

    @Test
    public void testGetSetEnableBatchedOperations() {
        // Arrange
        Boolean expectedEnableBatchedOperations = true;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Boolean actualEnableBatchedOperations = topicInfo
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
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Long actualSizeInBytes = topicInfo.setSizeInBytes(expectedSizeInBytes)
                .getSizeInBytes();

        // Assert
        assertEquals(expectedSizeInBytes, actualSizeInBytes);
    }

    @Test
    public void testGetSetFilteringMessageBeforePublishing() {
        // Arrange
        Boolean expectedFilteringMessageBeforePublishing = true;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Boolean actualFilteringMessageBeforePublishing = topicInfo
                .setFilteringMessageBeforePublishing(
                        expectedFilteringMessageBeforePublishing)
                .isFilteringMessageBeforePublishing();

        // Assert
        assertEquals(expectedFilteringMessageBeforePublishing,
                actualFilteringMessageBeforePublishing);
    }

    @Test
    public void testGetSetAnonymousAccessible() {
        // Arrange
        Boolean expectedAnonymousAccessible = true;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Boolean actualAnonymousAccessible = topicInfo.setAnonymousAccessible(
                expectedAnonymousAccessible).isAnonymousAccessible();

        // Assert
        assertEquals(expectedAnonymousAccessible, actualAnonymousAccessible);
    }

    @Test
    public void testGetSetStatus() {
        // Arrange
        EntityStatus expectedEntityStatus = EntityStatus.ACTIVE;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        EntityStatus actualEntityStatus = topicInfo.setStatus(
                expectedEntityStatus).getStatus();

        // Assert
        assertEquals(expectedEntityStatus, actualEntityStatus);
    }

    @Test
    public void testGetSetCreatedAt() {
        // Arrange
        Calendar expectedCreatedAt = Calendar.getInstance();
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Calendar actualCreatedAt = topicInfo.setCreatedAt(expectedCreatedAt)
                .getCreatedAt();

        // Assert
        assertEquals(expectedCreatedAt, actualCreatedAt);
    }

    @Test
    public void testGetSetUpdatedAt() {
        // Arrange
        Calendar expectedUpdatedAt = Calendar.getInstance();
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Calendar actualUpdatedAt = topicInfo.setUpdatedAt(expectedUpdatedAt)
                .getUpdatedAt();

        // Assert
        assertEquals(expectedUpdatedAt, actualUpdatedAt);
    }

    @Test
    public void testGetSetAccessedAt() {
        // Arrange
        Calendar expectedAccessedAt = Calendar.getInstance();
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Calendar actualAccessedAt = topicInfo.setAccessedAt(expectedAccessedAt)
                .getAccessedAt();

        // Assert
        assertEquals(expectedAccessedAt, actualAccessedAt);
    }

    @Test
    public void testGetSetUserMetadata() {
        // Arrange
        String expectedUserMetadata = "expectedUserMetaData";
        TopicInfo topicInfo = new TopicInfo();

        // Act
        String actualUserMetadata = topicInfo.setUserMetadata(
                expectedUserMetadata).getUserMetadata();

        // Assert
        assertEquals(expectedUserMetadata, actualUserMetadata);
    }

    @Test
    public void testGetSetSupportOrdering() {
        // Arrange
        Boolean expectedIsSupportOrdering = true;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Boolean actualIsSupportOrdering = topicInfo.setSupportOrdering(
                expectedIsSupportOrdering).isSupportOrdering();

        // Assert
        assertEquals(expectedIsSupportOrdering, actualIsSupportOrdering);
    }

    @Test
    public void testGetSetSubscriptionCount() {
        // Arrange
        Integer expectedSubscriptionCount = 1024;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Integer actualSubscriptionCount = topicInfo.setSubscriptionCount(
                expectedSubscriptionCount).getSubscriptionCount();

        // Assert
        assertEquals(expectedSubscriptionCount, actualSubscriptionCount);
    }

    @Test
    public void testGetSetAutoDeleteOnIdle() {
        // Arrange
        Duration expectedIsAutoDeleteOnIdle = createDuration(100);
        TopicInfo topicInfo = new TopicInfo();

        // Act
        Duration actualIsAutoDeleteOnIdle = topicInfo.setAutoDeleteOnIdle(
                expectedIsAutoDeleteOnIdle).getAutoDeleteOnIdle();

        // Assert
        assertEquals(expectedIsAutoDeleteOnIdle, actualIsAutoDeleteOnIdle);
    }

    @Test
    public void testGetSetPartioningPolicy() {
        // Arrange
        PartitioningPolicy expectedPartitioningPolicy = PartitioningPolicy.NO_PARTITIONING;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        PartitioningPolicy actualPartitioningPolicy = topicInfo
                .setPartitioningPolicy(expectedPartitioningPolicy)
                .getPartitioningPolicy();

        // Assert
        assertEquals(expectedPartitioningPolicy, actualPartitioningPolicy);
    }

    @Test
    public void testGetSetEntityAvailabilityStatus() {
        // Arrange
        EntityAvailabilityStatus expectedEntityAvailabilityStatus = EntityAvailabilityStatus.AVAILABLE;
        TopicInfo topicInfo = new TopicInfo();

        // Act
        EntityAvailabilityStatus actualEntityAvailabilityStatus = topicInfo
                .setEntityAvailabilityStatus(expectedEntityAvailabilityStatus)
                .getEntityAvailabilityStatus();

        // Assert
        assertEquals(expectedEntityAvailabilityStatus,
                actualEntityAvailabilityStatus);
    }

}
