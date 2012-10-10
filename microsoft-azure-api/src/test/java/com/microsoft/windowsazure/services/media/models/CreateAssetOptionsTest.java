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

import org.junit.Test;

public class CreateAssetOptionsTest {

    @Test
    public void testGetSetState() {
        // Arrange
        AssetState expectedState = AssetState.Published;
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();

        // Act
        AssetState actualState = createAssetOptions.setState(expectedState).getState();

        // Assert
        assertEquals(expectedState, actualState);
    }

    @Test
    public void testGetSetAlternateId() {
        // Arrange
        String expectedAlternateId = "testAlternateId";
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();

        // Act
        String actualAlternateId = createAssetOptions.setAlternateId(expectedAlternateId).getAlternateId();

        // Assert
        assertEquals(expectedAlternateId, actualAlternateId);
    }

    @Test
    public void testGetSetOptions() {
        // Arrange
        EncryptionOption expectedOptions = EncryptionOption.None;
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();

        // Act
        EncryptionOption actualOptions = createAssetOptions.setOptions(expectedOptions).getOptions();

        // Assert
        assertEquals(expectedOptions, actualOptions);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testGetSetName";
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();

        // Act
        String actualName = createAssetOptions.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

}
