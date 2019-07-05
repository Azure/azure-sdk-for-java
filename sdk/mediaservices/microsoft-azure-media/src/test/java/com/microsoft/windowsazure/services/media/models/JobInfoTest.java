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

import com.microsoft.windowsazure.services.media.implementation.content.JobNotificationSubscriptionType;
import com.microsoft.windowsazure.services.media.implementation.content.JobType;

public class JobInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        JobInfo JobInfo = new JobInfo(null, new JobType().setId(expectedId));

        // Act
        String actualId = JobInfo.getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testGetSetName";
        JobInfo JobInfo = new JobInfo(null, new JobType().setName(expectedName));

        // Act
        String actualName = JobInfo.getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetCreated() throws Exception {
        // Arrange
        Date expectedCreated = new Date();

        JobInfo JobInfo = new JobInfo(null,
                new JobType().setCreated(expectedCreated));

        // Act
        Date actualCreated = JobInfo.getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);

    }

    @Test
    public void testGetSetLastModified() throws Exception {
        // Arrange
        Date expectedLastModified = new Date();
        JobInfo JobInfo = new JobInfo(null,
                new JobType().setLastModified(expectedLastModified));

        // Act
        Date actualLastModified = JobInfo.getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetPriority() {
        // Arrange
        int expectedPriority = 3;
        JobInfo JobInfo = new JobInfo(null,
                new JobType().setPriority(expectedPriority));

        // Act
        int actualPriority = JobInfo.getPriority();

        // Assert
        assertEquals(expectedPriority, actualPriority);
    }

    @Test
    public void testGetSetRunningDuration() {
        // Arrange
        Double expectedRunningDuration = 1234.5;
        JobInfo JobInfo = new JobInfo(null,
                new JobType().setRunningDuration(expectedRunningDuration));

        // Act
        Double actualRunningDuration = JobInfo.getRunningDuration();

        // Assert
        assertEquals(expectedRunningDuration, actualRunningDuration);
    }

    @Test
    public void testGetSetStartTime() {
        // Arrange
        Date expectedStartTime = new Date();
        JobInfo JobInfo = new JobInfo(null,
                new JobType().setLastModified(expectedStartTime));

        // Act
        Date actualStartTime = JobInfo.getLastModified();

        // Assert
        assertEquals(expectedStartTime, actualStartTime);
    }

    @Test
    public void testGetSetState() {
        // Arrange
        JobState expectedJobState = JobState.Finished;
        JobInfo JobInfo = new JobInfo(null,
                new JobType().setState(expectedJobState.getCode()));

        // Act
        JobState actualJobState = JobInfo.getState();

        // Assert
        assertEquals(expectedJobState, actualJobState);
    }

    @Test
    public void testGetSetNotificationEndPoint() {
        // Arrange
        String expectedNotificationEndPointId = "testNotificationEndPointId";
        JobNotificationSubscription expectedJobNotificationSubscription = new JobNotificationSubscription(
                expectedNotificationEndPointId, TargetJobState.All);
        JobNotificationSubscriptionType expectedJobNotificationSubscriptionType = new JobNotificationSubscriptionType();
        JobType expectedJobType = new JobType();
        expectedJobType
                .addJobNotificationSubscriptionType(expectedJobNotificationSubscriptionType
                        .setNotificationEndPointId(
                                expectedNotificationEndPointId)
                        .setTargetJobState(TargetJobState.All.getCode()));
        JobInfo jobInfo = new JobInfo(null, expectedJobType);

        // Act
        JobNotificationSubscription actualJobNotificationSubscription = jobInfo
                .getJobNotificationSubscriptions().get(0);

        // Assert
        assertEquals(
                expectedJobNotificationSubscription.getNotificationEndPointId(),
                actualJobNotificationSubscription.getNotificationEndPointId());
        assertEquals(expectedJobNotificationSubscription.getTargetJobState(),
                actualJobNotificationSubscription.getTargetJobState());
    }
}
