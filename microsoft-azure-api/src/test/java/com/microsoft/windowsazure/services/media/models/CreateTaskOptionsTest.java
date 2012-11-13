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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class CreateTaskOptionsTest {

    @Test
    public void testGetSetConfiguration() {
        // Arrange
        String expectedTaskConfiguration = "testGetSetConfiguration";
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act
        String actualTaskConfiguration = createTaskOptions.setName(expectedTaskConfiguration).getConfiguration();

        // Assert
        assertEquals(expectedTaskConfiguration, actualTaskConfiguration);
    }

    @Test
    public void testGetSetMediaProcessorId() {
        // Arrange
        String expectedMediaProcessorId = "testGetSetMediaProcessorId";
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act
        String actualMediaProcessorId = createTaskOptions.setMediaProcessorId(expectedMediaProcessorId)
                .getMediaProcessorId();

        // Assert
        assertEquals(expectedMediaProcessorId, actualMediaProcessorId);

    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testGetSetName";
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act 
        String actualName = createTaskOptions.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetPriority() {
        // Arrange
        Integer expectedPriority = 3;
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act
        Integer actualPriority = createTaskOptions.setPriority(expectedPriority).getPriority();

        // Assert
        assertEquals(expectedPriority, actualPriority);
    }

    @Test
    public void testGetSetStartTime() {
        // Arrange
        Date expectedStartTime = new Date();
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act
        Date actualStartTime = createTaskOptions.setStartTime(expectedStartTime).getStartTime();

        // Assert
        assertEquals(expectedStartTime, actualStartTime);
    }

    @Test
    public void testGetSetTaskBody() {
        // Arrange
        String expectedTaskBody = "testExpectedTaskBody";
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act 
        String actualTaskBody = createTaskOptions.setTaskBody(expectedTaskBody).getTaskBody();

        // Assert
        assertEquals(expectedTaskBody, actualTaskBody);
    }

    @Test
    public void testGetSetEncryptionKeyId() {
        // Arrange
        String expectedEncryptionKeyId = "testGetSetEncryptionKeyId";
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act
        String actualEncryptionKeyId = createTaskOptions.setEncryptionKeyId(expectedEncryptionKeyId)
                .getEncryptionKeyId();

        // Assert
        assertEquals(expectedEncryptionKeyId, actualEncryptionKeyId);
    }

    @Test
    public void testGetSetEncryptionScheme() {
        // Arrange 
        String expectedEncryptionScheme = "testEncryptionScheme";
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act 
        String actualEncryptionScheme = createTaskOptions.setEncryptionScheme(expectedEncryptionScheme)
                .getEncryptionScheme();

        // Assert
        assertEquals(expectedEncryptionScheme, actualEncryptionScheme);
    }

    @Test
    public void testGetSetEncryptionVersion() {
        // Arrange 
        String expectedEncryptionVersion = "testEncryptionVersion";
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act 
        String actualEncryptionVersion = createTaskOptions.setEncryptionVersion(expectedEncryptionVersion)
                .getEncryptionVersion();

        // Assert
        assertEquals(expectedEncryptionVersion, actualEncryptionVersion);
    }

    @Test
    public void testGetSetInitializationVector() {
        // Arrange
        String expectedInitializationVector = "testInitializatonVector";
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act 
        String actualInitializationVector = createTaskOptions.setInitializationVector(expectedInitializationVector)
                .getInitializationVector();

        // Assert
        assertEquals(expectedInitializationVector, actualInitializationVector);
    }

    @Test
    public void testGetSetInputMediaAssets() {
        // Arrange
        String expectedInputMediaAsset = "http://www.contoso.com/asset(123)";
        List<String> expectedInputMediaAssets = new ArrayList<String>();
        expectedInputMediaAssets.add(expectedInputMediaAsset);
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act
        List<String> actualInputMediaAssets = createTaskOptions.addInputMediaAsset(expectedInputMediaAsset)
                .getInputMediaAssets();

        // Assert
        assertEquals(expectedInputMediaAssets, actualInputMediaAssets);
    }

    @Test
    public void testGetSetOutputMediaAssets() {
        // Arrange
        String expectedOutputMediaAsset = "http://www.contoso.com/asset(123)";
        List<String> expectedOutputMediaAssets = new ArrayList<String>();
        expectedOutputMediaAssets.add(expectedOutputMediaAsset);
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();

        // Act
        List<String> actualOutputMediaAssets = createTaskOptions.addOutputMediaAsset(expectedOutputMediaAsset)
                .getOutputMediaAssets();

        // Assert
        assertEquals(expectedOutputMediaAssets, actualOutputMediaAssets);
    }
}
