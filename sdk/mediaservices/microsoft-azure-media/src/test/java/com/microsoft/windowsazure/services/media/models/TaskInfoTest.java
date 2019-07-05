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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.ErrorDetailType;
import com.microsoft.windowsazure.services.media.implementation.content.TaskType;

public class TaskInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        TaskInfo taskInfo = new TaskInfo(null, new TaskType().setId(expectedId));

        // Act
        String actualId = taskInfo.getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetConfiguration() {
        // Arrange
        String expectedConfiguration = "expectedConfiguration";
        TaskInfo taskInfo = new TaskInfo(null,
                new TaskType().setConfiguration(expectedConfiguration));

        // Act
        String actualConfiguration = taskInfo.getConfiguration();

        // Assert
        assertEquals(expectedConfiguration, actualConfiguration);
    }

    @Test
    public void testGetSetEndTime() throws Exception {
        // Arrange
        Date expectedEndTime = new Date();

        TaskInfo taskInfo = new TaskInfo(null,
                new TaskType().setEndTime(expectedEndTime));

        // Act
        Date actualEndTime = taskInfo.getEndTime();

        // Assert
        assertEquals(expectedEndTime, actualEndTime);

    }

    @Test
    public void testGetSetErrorDetails() throws Exception {
        // Arrange
        List<ErrorDetail> expectedErrorDetails = new ArrayList<ErrorDetail>();
        List<ErrorDetailType> expectedErrorDetailsType = new ArrayList<ErrorDetailType>();
        for (ErrorDetailType errorDetailType : expectedErrorDetailsType) {
            ErrorDetail errorDetail = new ErrorDetail(
                    errorDetailType.getCode(), errorDetailType.getMessage());
            expectedErrorDetails.add(errorDetail);
        }
        TaskInfo taskInfo = new TaskInfo(null,
                new TaskType().setErrorDetails(expectedErrorDetailsType));

        // Act
        List<ErrorDetail> actualErrorDetails = taskInfo.getErrorDetails();

        // Assert
        assertEquals(expectedErrorDetails, actualErrorDetails);
    }

    @Test
    public void testGetSetMediaProcessorId() {
        // Arrange
        String expectedName = "testName";
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setName(expectedName));

        // Act
        String actualName = TaskInfo.getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetName() {
        // Arrange

        TaskOption expectedOptions = TaskOption.None;
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setOptions(expectedOptions.getCode()));

        // Act
        TaskOption actualOptions = TaskInfo.getOptions();

        // Assert
        assertEquals(expectedOptions, actualOptions);
    }

    @Test
    public void testGetSetPerfMessage() {
        // Arrange
        String expectedPerfMessage = "testGetSetPerfMessage";
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setPerfMessage(expectedPerfMessage));

        // Act
        String actualPerfMessage = TaskInfo.getPerfMessage();

        // Assert
        assertEquals(expectedPerfMessage, actualPerfMessage);
    }

    @Test
    public void testGetSetPriority() {
        // Arrange
        int expectedPriority = 3;
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setPriority(expectedPriority));

        // Act
        int actualPriority = TaskInfo.getPriority();

        // Assert
        assertEquals(expectedPriority, actualPriority);
    }

    @Test
    public void testGetSetProgress() {
        // Arrange
        double expectedProgress = 3;
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setProgress(expectedProgress));

        // Act
        double actualProgress = TaskInfo.getProgress();

        // Assert
        assertEquals(expectedProgress, actualProgress, 0.00001);
    }

    @Test
    public void testGetSetRunningDuration() {
        // Arrange
        double expectedRunningDuration = 3;
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setRunningDuration(expectedRunningDuration));

        // Act
        double actualRunningDuration = TaskInfo.getRunningDuration();

        // Assert
        assertEquals(expectedRunningDuration, actualRunningDuration, 0.00001);
    }

    @Test
    public void testGetSetStartTime() {
        // Arrange
        Date expectedStartTime = new Date();
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setStartTime(expectedStartTime));

        // Act
        Date actualStartTime = TaskInfo.getStartTime();

        // Assert
        assertEquals(expectedStartTime, actualStartTime);
    }

    @Test
    public void testGetSetState() {
        // Arrange
        TaskState expectedState = TaskState.Completed;
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setState(expectedState.getCode()));

        // Act
        TaskState actualState = TaskInfo.getState();

        // Assert
        assertEquals(expectedState, actualState);
    }

    @Test
    public void testGetSetTaskBody() {
        // Arrange
        String expectedTaskBody = "getSetTaskBody";
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setTaskBody(expectedTaskBody));

        // Act
        String actualTaskBody = TaskInfo.getTaskBody();

        // Assert
        assertEquals(expectedTaskBody, actualTaskBody);
    }

    @Test
    public void testGetSetOptions() {
        // Arrange
        TaskOption expectedTaskOption = TaskOption.ProtectedConfiguration;
        TaskInfo TaskInfo = new TaskInfo(null,
                new TaskType().setOptions(expectedTaskOption.getCode()));

        // Act
        TaskOption actualTaskOption = TaskInfo.getOptions();

        // Assert
        assertEquals(expectedTaskOption, actualTaskOption);
    }

    @Test
    public void testGetSetEncryptionKeyId() {
        // Arrange
        String expectedEncryptionKeyId = "getSetEncryptionKeyId";
        TaskInfo taskInfo = new TaskInfo(null,
                new TaskType().setEncryptionKeyId(expectedEncryptionKeyId));

        // Act
        String actualEncryptionKeyId = taskInfo.getEncryptionKeyId();

        // Assert
        assertEquals(expectedEncryptionKeyId, actualEncryptionKeyId);
    }

    @Test
    public void testGetSetEncryptionScheme() {
        // Arrange
        String expectedEncryptionScheme = "getSetEncryptionScheme";
        TaskInfo taskInfo = new TaskInfo(null,
                new TaskType().setEncryptionScheme(expectedEncryptionScheme));

        // Act
        String actualEncryptionScheme = taskInfo.getEncryptionScheme();

        // Assert
        assertEquals(expectedEncryptionScheme, actualEncryptionScheme);
    }

    @Test
    public void testGetSetEncryptionVersion() {
        // Arrange
        String expectedEncryptionVersion = "1.5";
        TaskInfo taskInfo = new TaskInfo(null,
                new TaskType().setEncryptionVersion(expectedEncryptionVersion));

        // Act
        String actualEncryptionVersion = taskInfo.getEncryptionVersion();

        // Assert
        assertEquals(expectedEncryptionVersion, actualEncryptionVersion);
    }

    @Test
    public void testGetSetInitializationVector() {
        // Arrange
        String expectedInitializationVector = "testInitializationVector";
        TaskInfo taskInfo = new TaskInfo(null,
                new TaskType()
                        .setEncryptionVersion(expectedInitializationVector));

        // Act
        String actualInitializationVector = taskInfo.getEncryptionVersion();

        // Assert
        assertEquals(expectedInitializationVector, actualInitializationVector);
    }

}
