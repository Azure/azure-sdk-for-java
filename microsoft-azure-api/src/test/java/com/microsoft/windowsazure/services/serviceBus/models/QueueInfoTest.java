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
package com.microsoft.windowsazure.services.serviceBus.models;

import static org.junit.Assert.*;

import java.util.Calendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Test;

import com.microsoft.windowsazure.services.serviceBus.implementation.AuthorizationRules;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntityAvailabilityStatus;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntityStatus;
import com.microsoft.windowsazure.services.serviceBus.implementation.MessageCountDetails;
import com.microsoft.windowsazure.services.serviceBus.implementation.PartitioningPolicy;

public class QueueInfoTest {

    private Duration createDuration(int milliSeconds) {
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        return datatypeFactory.newDuration(milliSeconds);
    }

    @Test
    public void testGetSetLockDuration() {
        // Arrange
        Duration expectedLockDuration = createDuration(100);
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Duration actualLockDuration = QueueInfo.setLockDuration(expectedLockDuration).getLockDuration();

        // Assert
        assertEquals(expectedLockDuration, actualLockDuration);

    }

    @Test
    public void testGetSetMaxSizeInMegabytes() {
        // Arrange
        Long expectedMaxSizeInMegabytes = 1024L;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Long actualMaxSizeInMegabytes = QueueInfo.setMaxSizeInMegabytes(expectedMaxSizeInMegabytes)
                .getMaxSizeInMegabytes();

        // Assert
        assertEquals(expectedMaxSizeInMegabytes, actualMaxSizeInMegabytes);

    }

    @Test
    public void testGetSetRequiresDuplicateDetection() {
        // Arrange
        Boolean expectedRequiresDuplicateDetection = true;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Boolean actualRequiresDuplicateDetection = QueueInfo.setRequiresDuplicateDetection(
                expectedRequiresDuplicateDetection).isRequiresDuplicateDetection();

        // Assert
        assertEquals(expectedRequiresDuplicateDetection, actualRequiresDuplicateDetection);

    }

    @Test
    public void testGetSetRequiresSession() {
        // Arrange
        Boolean expectedRequiresSession = true;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Boolean actualRequiresSession = QueueInfo.setRequiresSession(expectedRequiresSession).isRequiresSession();

        // Assert
        assertEquals(expectedRequiresSession, actualRequiresSession);
    }

    @Test
    public void testGetSetDefaultMessageTimeToLive() {
        // Arrange
        Duration expectedDefaultMessageTimeToLive = createDuration(100);
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Duration actualDefaultMessageTimeToLive = QueueInfo.setDefaultMessageTimeToLive(
                expectedDefaultMessageTimeToLive).getDefaultMessageTimeToLive();

        // Assert
        assertEquals(expectedDefaultMessageTimeToLive, actualDefaultMessageTimeToLive);
    }

    @Test
    public void testGetSetDeadLetteringOnMessageExpiration() {
        // Arrange
        Boolean expectedDeadLetteringOnMessageExpiration = true;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Boolean actualDeadLetteringOnMessageExpiration = QueueInfo.setDeadLetteringOnMessageExpiration(
                expectedDeadLetteringOnMessageExpiration).isDeadLetteringOnMessageExpiration();

        // Assert
        assertEquals(expectedDeadLetteringOnMessageExpiration, actualDeadLetteringOnMessageExpiration);
    }

    @Test
    public void testGetSetDuplicateDetectionHistoryTimeWindow() {
        // Arrange
        Duration expectedDefaultMessageTimeToLive = createDuration(100);
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Duration actualDefaultMessageTimeToLive = QueueInfo.setDefaultMessageTimeToLive(
                expectedDefaultMessageTimeToLive).getDefaultMessageTimeToLive();

        // Assert
        assertEquals(expectedDefaultMessageTimeToLive, actualDefaultMessageTimeToLive);
    }

    @Test
    public void testGetSetMaxDeliveryCount() {
        // Arrange
        Integer expectedMaxDeliveryCount = 1024;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Integer actualMaxDeliveryCount = QueueInfo.setMaxDeliveryCount(expectedMaxDeliveryCount).getMaxDeliveryCount();

        // Assert
        assertEquals(expectedMaxDeliveryCount, actualMaxDeliveryCount);
    }

    @Test
    public void testGetSetEnableBatchedOperations() {
        // Arrange
        Boolean expectedEnableBatchedOperations = true;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Boolean actualEnableBatchedOperations = QueueInfo.setEnableBatchedOperations(expectedEnableBatchedOperations)
                .isEnableBatchedOperations();

        // Assert
        assertEquals(expectedEnableBatchedOperations, actualEnableBatchedOperations);
    }

    @Test
    public void testGetSetSizeInBytes() {
        // Arrange
        Long expectedSizeInBytes = 1024L;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Long actualSizeInBytes = QueueInfo.setSizeInBytes(expectedSizeInBytes).getSizeInBytes();

        // Assert
        assertEquals(expectedSizeInBytes, actualSizeInBytes);
    }

    @Test
    public void testGetSetMessageCount() {
        // Arrange
        Long expectedMessageCount = 1024L;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Long actualMessageCount = QueueInfo.setMessageCount(expectedMessageCount).getMessageCount();

        // Assert
        assertEquals(expectedMessageCount, actualMessageCount);
    }

    @Test
    public void testGetSetIsAnonymousAccessible() {
        // Arrange
        Boolean expectedIsAnonymousAccessible = true;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Boolean actualIsAnonymousAccessible = QueueInfo.setIsAnonymousAccessible(expectedIsAnonymousAccessible)
                .isAnonymousAccessible();

        // Assert
        assertEquals(expectedIsAnonymousAccessible, actualIsAnonymousAccessible);
    }

    @Test
    public void testGetSetAuthorization() {
        // Arrange
        AuthorizationRules expectedAuthorizationRules = new AuthorizationRules();
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        AuthorizationRules actualAuthorizationRules = QueueInfo.setAuthorization(expectedAuthorizationRules)
                .getAuthorization();

        // Assert
        assertEquals(expectedAuthorizationRules, actualAuthorizationRules);
    }

    @Test
    public void testGetSetStatus() {
        // Arrange
        EntityStatus expectedEntityStatus = EntityStatus.ACTIVE;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        EntityStatus actualEntityStatus = QueueInfo.setStatus(expectedEntityStatus).getStatus();

        // Assert
        assertEquals(expectedEntityStatus, actualEntityStatus);
    }

    @Test
    public void testGetSetForwardTo() {
        // Arrange
        String expectedForwardTo = "forwardTo";
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        String actualForwardTo = QueueInfo.setForwardTo(expectedForwardTo).getForwardTo();

        // Assert
        assertEquals(expectedForwardTo, actualForwardTo);
    }

    @Test
    public void testGetSetCreatedAt() {
        // Arrange
        Calendar expectedCreatedAt = Calendar.getInstance();
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Calendar actualCreatedAt = QueueInfo.setCreatedAt(expectedCreatedAt).getCreatedAt();

        // Assert
        assertEquals(expectedCreatedAt, actualCreatedAt);
    }

    @Test
    public void testGetSetUpdatedAt() {
        // Arrange
        Calendar expectedUpdatedAt = Calendar.getInstance();
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Calendar actualUpdatedAt = QueueInfo.setUpdatedAt(expectedUpdatedAt).getUpdatedAt();

        // Assert
        assertEquals(expectedUpdatedAt, actualUpdatedAt);
    }

    @Test
    public void testGetSetAccessedAt() {
        // Arrange
        Calendar expectedAccessedAt = Calendar.getInstance();
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Calendar actualAccessedAt = QueueInfo.setAccessedAt(expectedAccessedAt).getAccessedAt();

        // Assert
        assertEquals(expectedAccessedAt, actualAccessedAt);
    }

    @Test
    public void testGetSetUserMetadata() {
        // Arrange
        Calendar expectedAccessedAt = Calendar.getInstance();
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Calendar actualAccessedAt = QueueInfo.setAccessedAt(expectedAccessedAt).getAccessedAt();

        // Assert
        assertEquals(expectedAccessedAt, actualAccessedAt);
    }

    @Test
    public void testGetSetSupportOrdering() {
        // Arrange
        Boolean expectedIsSupportOrdering = true;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Boolean actualIsSupportOrdering = QueueInfo.setSupportOrdering(expectedIsSupportOrdering).isSupportOrdering();

        // Assert
        assertEquals(expectedIsSupportOrdering, actualIsSupportOrdering);
    }

    @Test
    public void testGetSetCountDetails() {
        // Arrange
        MessageCountDetails expectedCountDetails = new MessageCountDetails();
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        MessageCountDetails actualCountDetails = QueueInfo.setCountDetails(expectedCountDetails).getCountDetails();

        // Assert
        assertEquals(expectedCountDetails, actualCountDetails);
    }

    @Test
    public void testGetSetAutoDeleteOnIdle() {
        // Arrange
        Duration expectedIsAutoDeleteOnIdle = createDuration(100);
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        Duration actualIsAutoDeleteOnIdle = QueueInfo.setAutoDeleteOnIdle(expectedIsAutoDeleteOnIdle)
                .getAutoDeleteOnIdle();

        // Assert
        assertEquals(expectedIsAutoDeleteOnIdle, actualIsAutoDeleteOnIdle);
    }

    @Test
    public void testGetSetPartioningPolicy() {
        // Arrange
        PartitioningPolicy expectedPartitioningPolicy = PartitioningPolicy.NO_PARTITIONING;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        PartitioningPolicy actualPartitioningPolicy = QueueInfo.setPartitioningPolicy(expectedPartitioningPolicy)
                .getPartitioningPolicy();

        // Assert
        assertEquals(expectedPartitioningPolicy, actualPartitioningPolicy);
    }

    @Test
    public void testGetSetEntityAvailabilityStatus() {
        // Arrange
        EntityAvailabilityStatus expectedEntityAvailabilityStatus = EntityAvailabilityStatus.AVAILABLE;
        QueueInfo QueueInfo = new QueueInfo();

        // Act 
        EntityAvailabilityStatus actualEntityAvailabilityStatus = QueueInfo.setEntityAvailabilityStatus(
                expectedEntityAvailabilityStatus).getEntityAvailabilityStatus();

        // Assert
        assertEquals(expectedEntityAvailabilityStatus, actualEntityAvailabilityStatus);
    }

}
