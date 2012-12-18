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

import com.microsoft.windowsazure.services.media.implementation.content.TaskType;

public class TaskInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        TaskInfo TaskInfo = new TaskInfo(null, new TaskType().setId(expectedId));

        // Act 
        String actualId = TaskInfo.getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetConfiguration() {
        // Arrange
        String expectedConfiguration = "expectedConfiguration";
        TaskInfo TaskInfo = new TaskInfo(null, new TaskType().setState(expectedState.getCode()));

        // Act
        TaskState actualState = TaskInfo.getState();

        // Assert
        assertEquals(expectedState, actualState);
    }

    @Test
    public void testGetSetEndTime() throws Exception {
        // Arrange
        Date expectedCreated = new Date();

        TaskInfo TaskInfo = new TaskInfo(null, new TaskType().setCreated(expectedCreated));

        // Act 
        Date actualCreated = TaskInfo.getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);

    }

    @Test
    public void testGetSetErrorDetails() throws Exception {
        // Arrange
        Date expectedLastModified = new Date();
        TaskInfo TaskInfo = new TaskInfo(null, new TaskType().setLastModified(expectedLastModified));

        // Act
        Date actualLastModified = TaskInfo.getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetHistoricalEvents() {
        // Arrange
        String expectedAlternateId = "testAlternateId";
        TaskInfo TaskInfo = new TaskInfo(null, new TaskType().setAlternateId(expectedAlternateId));

        // Act
        String actualAlternateId = TaskInfo.getAlternateId();

        // Assert
        assertEquals(expectedAlternateId, actualAlternateId);
    }

    @Test
    public void testGetSetMediaProcessorId() {
        // Arrange
        String expectedName = "testName";
        TaskInfo TaskInfo = new TaskInfo(null, new TaskType().setName(expectedName));

        // Act
        String actualName = TaskInfo.getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetName() {
        // Arrange

        TaskOption expectedOptions = TaskOption.None;
        TaskInfo TaskInfo = new TaskInfo(null, new TaskType().setOptions(expectedOptions.getCode()));

        // Act
        TaskOption actualOptions = TaskInfo.getOptions();

        // Assert
        assertEquals(expectedOptions, actualOptions);
    }

    public void testGetSetPerfMessage() {
        // Arrange

        String expectedPerfMessage = "testGetSetPerfMessage";
        TaskInfo TaskInfo = new TaskInfo(null, new TaskType().setPerfMessage(expectedPerfMessage));

        // Act
        String actualPerfMessage = TaskInfo.getPerfMessage();

        // Assert
        assertEquals(expectedPerfMessage, actualPerfMessage);
    }

    public void testGetSetPriority() {

    }

    public void testGetSetProgress() {

    }

    public void testGetSetRunningDuration() {

    }

    public void testGetSetStartTime() {

    }

    public void testGetSetState() {

    }

    public void testGetSetTaskBody() {

    }

    public void testGetSetOptions() {

    }

    public void testGetSetEncryptionKeyId() {

    }

    public void testGetSetEncryptionScheme() {

    }

    public void testGetSetEncryptionVersion() {

    }

    public void testGetSetInitializationVector() {

    }

    public void testGetSetOutputMediaAssets() {

    }

    public void testGetSetInputMediaAssets() {

    }
}
