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

public class CreateJobOptionsTest {

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedJobName = "testGetSetNameJobName";
        CreateJobOptions createJobOptions = new CreateJobOptions();

        // Act
        String actualJobName = createJobOptions.setName(expectedJobName).getName();

        // Assert
        assertEquals(expectedJobName, actualJobName);
    }

    @Test
    public void testGetSetPriority() {
        // Arrange
        Integer expectedPriority = 3;
        CreateJobOptions createJobOptions = new CreateJobOptions();

        // Act
        Integer actualPriority = createJobOptions.setPriority(expectedPriority).getPriority();

        // Assert
        assertEquals(expectedPriority, actualPriority);
    }

    @Test
    public void testGetSetStartTime() {
        // Arrange
        Date expectedStartTime = new Date();
        CreateJobOptions createJobOptions = new CreateJobOptions();

        // Act
        Date actualStartTime = createJobOptions.setStartTime(expectedStartTime).getStartTime();

        // Assert
        assertEquals(expectedStartTime, actualStartTime);
    }

    @Test
    public void testGetSetInputMediaAssets() {
        // Arrange
        String expectedInputMediaAssets = "testGetSetInputMediaAssets";
        CreateJobOptions createJobOptions = new CreateJobOptions();

        // Act
        String actualInputMediaAssets = createJobOptions.setInputMediaAssets(expectedInputMediaAssets)
                .getInputMediaAssets();

        // Assert
        assertEquals(expectedInputMediaAssets, actualInputMediaAssets);
    }

    @Test
    public void testGetSetOutputMediaAssets() {
        // Arrange
        String expectedOutputMediaAssets = "testGetSetOutputMediaAssets";
        CreateJobOptions createJobOptions = new CreateJobOptions();

        // Act
        String actualOutputMediaAssets = createJobOptions.setOutputMediaAssets(expectedOutputMediaAssets)
                .getOutputMediaAssets();

        // Assert
        assertEquals(expectedOutputMediaAssets, actualOutputMediaAssets);
    }

}
