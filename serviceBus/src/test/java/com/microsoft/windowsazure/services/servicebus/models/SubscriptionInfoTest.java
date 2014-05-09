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
import com.microsoft.windowsazure.services.servicebus.implementation.RuleDescription;

public class SubscriptionInfoTest {

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
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Duration actualLockDuration = SubscriptionInfo.setLockDuration(
                expectedLockDuration).getLockDuration();

        // Assert
        assertEquals(expectedLockDuration, actualLockDuration);

    }

    @Test
    public void testGetSetRequiresSession() {
        // Arrange
        Boolean expectedRequiresSession = true;
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Boolean actualRequiresSession = SubscriptionInfo.setRequiresSession(
                expectedRequiresSession).isRequiresSession();

        // Assert
        assertEquals(expectedRequiresSession, actualRequiresSession);
    }

    @Test
    public void testGetSetDefaultMessageTimeToLive() {
        // Arrange
        Duration expectedDefaultMessageTimeToLive = createDuration(100);
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Duration actualDefaultMessageTimeToLive = SubscriptionInfo
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
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Boolean actualDeadLetteringOnMessageExpiration = SubscriptionInfo
                .setDeadLetteringOnMessageExpiration(
                        expectedDeadLetteringOnMessageExpiration)
                .isDeadLetteringOnMessageExpiration();

        // Assert
        assertEquals(expectedDeadLetteringOnMessageExpiration,
                actualDeadLetteringOnMessageExpiration);
    }

    @Test
    public void testGetSetDeadLetteringOnFilterEvaluationExceptions() {
        // Arrange
        Boolean expectedDeadLetteringOnFilterEvaluationExceptions = true;
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Boolean actualDeadLetteringOnFilterEvaluationExceptions = SubscriptionInfo
                .setDeadLetteringOnFilterEvaluationExceptions(
                        expectedDeadLetteringOnFilterEvaluationExceptions)
                .isDeadLetteringOnFilterEvaluationExceptions();

        // Assert
        assertEquals(expectedDeadLetteringOnFilterEvaluationExceptions,
                actualDeadLetteringOnFilterEvaluationExceptions);
    }

    @Test
    public void testGetSetDefaultRuleDescription() {
        // Arrange
        RuleDescription expectedDefaultRuleDescription = new RuleDescription();
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        RuleDescription actualDefaultRuleDescription = SubscriptionInfo
                .setDefaultRuleDescription(expectedDefaultRuleDescription)
                .getDefaultRuleDescription();

        // Assert
        assertEquals(expectedDefaultRuleDescription,
                actualDefaultRuleDescription);
    }

    @Test
    public void testGetSetMessageCount() {
        // Arrange
        Long expectedMessageCount = 1024L;
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Long actualMessageCount = SubscriptionInfo.setMessageCount(
                expectedMessageCount).getMessageCount();

        // Assert
        assertEquals(expectedMessageCount, actualMessageCount);
    }

    @Test
    public void testGetSetMaxDeliveryCount() {
        // Arrange
        Integer expectedMaxDeliveryCount = 1024;
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Integer actualMaxDeliveryCount = SubscriptionInfo.setMaxDeliveryCount(
                expectedMaxDeliveryCount).getMaxDeliveryCount();

        // Assert
        assertEquals(expectedMaxDeliveryCount, actualMaxDeliveryCount);
    }

    @Test
    public void testGetSetEnableBatchedOperations() {
        // Arrange
        Boolean expectedEnableBatchedOperations = true;
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Boolean actualEnableBatchedOperations = SubscriptionInfo
                .setEnableBatchedOperations(expectedEnableBatchedOperations)
                .isEnableBatchedOperations();

        // Assert
        assertEquals(expectedEnableBatchedOperations,
                actualEnableBatchedOperations);
    }

    @Test
    public void testGetSetStatus() {
        // Arrange
        EntityStatus expectedEntityStatus = EntityStatus.ACTIVE;
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        EntityStatus actualEntityStatus = SubscriptionInfo.setStatus(
                expectedEntityStatus).getStatus();

        // Assert
        assertEquals(expectedEntityStatus, actualEntityStatus);
    }

    @Test
    public void testGetSetCreatedAt() {
        // Arrange
        Calendar expectedCreatedAt = Calendar.getInstance();
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Calendar actualCreatedAt = SubscriptionInfo.setCreatedAt(
                expectedCreatedAt).getCreatedAt();

        // Assert
        assertEquals(expectedCreatedAt, actualCreatedAt);
    }

    @Test
    public void testGetSetUpdatedAt() {
        // Arrange
        Calendar expectedUpdatedAt = Calendar.getInstance();
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Calendar actualUpdatedAt = SubscriptionInfo.setUpdatedAt(
                expectedUpdatedAt).getUpdatedAt();

        // Assert
        assertEquals(expectedUpdatedAt, actualUpdatedAt);
    }

    @Test
    public void testGetSetAccessedAt() {
        // Arrange
        Calendar expectedAccessedAt = Calendar.getInstance();
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Calendar actualAccessedAt = SubscriptionInfo.setAccessedAt(
                expectedAccessedAt).getAccessedAt();

        // Assert
        assertEquals(expectedAccessedAt, actualAccessedAt);
    }

    @Test
    public void testGetSetUserMetadata() {
        // Arrange
        String expectedUserMetadata = "expectedUserMetaData";
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        String actualUserMetadata = SubscriptionInfo.setUserMetadata(
                expectedUserMetadata).getUserMetadata();

        // Assert
        assertEquals(expectedUserMetadata, actualUserMetadata);
    }

    @Test
    public void testGetSetAutoDeleteOnIdle() {
        // Arrange
        Duration expectedIsAutoDeleteOnIdle = createDuration(100);
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        Duration actualIsAutoDeleteOnIdle = SubscriptionInfo
                .setAutoDeleteOnIdle(expectedIsAutoDeleteOnIdle)
                .getAutoDeleteOnIdle();

        // Assert
        assertEquals(expectedIsAutoDeleteOnIdle, actualIsAutoDeleteOnIdle);
    }

    @Test
    public void testGetSetEntityAvailabilityStatus() {
        // Arrange
        EntityAvailabilityStatus expectedEntityAvailabilityStatus = EntityAvailabilityStatus.AVAILABLE;
        SubscriptionInfo SubscriptionInfo = new SubscriptionInfo();

        // Act
        EntityAvailabilityStatus actualEntityAvailabilityStatus = SubscriptionInfo
                .setEntityAvailabilityStatus(expectedEntityAvailabilityStatus)
                .getEntityAvailabilityStatus();

        // Assert
        assertEquals(expectedEntityAvailabilityStatus,
                actualEntityAvailabilityStatus);
    }

}
