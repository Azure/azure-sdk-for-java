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

import com.microsoft.windowsazure.services.media.implementation.content.AssetType;

public class AssetInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        AssetInfo assetInfo = new AssetInfo(null,
                new AssetType().setId(expectedId));

        // Act
        String actualId = assetInfo.getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetState() {
        // Arrange
        AssetState expectedState = AssetState.Published;
        AssetInfo assetInfo = new AssetInfo(null,
                new AssetType().setState(expectedState.getCode()));

        // Act
        AssetState actualState = assetInfo.getState();

        // Assert
        assertEquals(expectedState, actualState);
    }

    @Test
    public void testGetSetCreated() throws Exception {
        // Arrange
        Date expectedCreated = new Date();

        AssetInfo assetInfo = new AssetInfo(null,
                new AssetType().setCreated(expectedCreated));

        // Act
        Date actualCreated = assetInfo.getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);

    }

    @Test
    public void testGetSetLastModified() throws Exception {
        // Arrange
        Date expectedLastModified = new Date();
        AssetInfo assetInfo = new AssetInfo(null,
                new AssetType().setLastModified(expectedLastModified));

        // Act
        Date actualLastModified = assetInfo.getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetAlternateId() {
        // Arrange
        String expectedAlternateId = "testAlternateId";
        AssetInfo assetInfo = new AssetInfo(null,
                new AssetType().setAlternateId(expectedAlternateId));

        // Act
        String actualAlternateId = assetInfo.getAlternateId();

        // Assert
        assertEquals(expectedAlternateId, actualAlternateId);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testName";
        AssetInfo assetInfo = new AssetInfo(null,
                new AssetType().setName(expectedName));

        // Act
        String actualName = assetInfo.getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetOptions() {
        // Arrange

        AssetOption expectedOptions = AssetOption.None;
        AssetInfo assetInfo = new AssetInfo(null,
                new AssetType().setOptions(expectedOptions.getCode()));

        // Act
        AssetOption actualOptions = assetInfo.getOptions();

        // Assert
        assertEquals(expectedOptions, actualOptions);
    }

}
