/**
 * Copyright 2011 Microsoft Corporation
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

import org.junit.Test;

public class AssetTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        Asset asset = new Asset();

        // Act 
        String actualId = asset.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetState() {
        // Arrange
        AssetState expectedState = AssetState.Published;
        Asset asset = new Asset();

        // Act
        AssetState actualState = asset.setState(expectedState).getState();

        // Assert
        assertEquals(expectedState, actualState);
    }

    @Test
    public void testGetSetCreated() {
        // Arrange
        Date expectedCreated = new Date();
        Asset asset = new Asset();

        // Act 
        Date actualCreated = asset.setCreate(expectedCreated).getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);

    }

    @Test
    public void testGetSetLastModified() {
        // Arrange
        Date expectedLastModified = new Date();
        Asset asset = new Asset();

        // Act
        Date actualLastModified = asset.setLastModified(expectedLastModified).getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetAlternateId() {
        // Arrange
        String expectedAlternateId = "testAlternateId";
        Asset asset = new Asset();

        // Act
        String actualAlternateId = asset.setAlternateId(expectedAlternateId).getAlternateId();

        // Assert
        assertEquals(expectedAlternateId, actualAlternateId);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testName";
        Asset asset = new Asset();

        // Act
        String actualName = asset.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetOptions() {
        // Arrange
        EncryptionOption expectedOptions = EncryptionOption.None;
        Asset asset = new Asset();

        // Act
        EncryptionOption actualOptions = asset.setOptions(expectedOptions).getOptions();

        // Assert
        assertEquals(expectedOptions, actualOptions);
    }

    @Test
    public void testGetSetLocators() {
        // Arrange
        Iterable<Locator> expectedLocators = new ArrayList<Locator>();
        Asset asset = new Asset();

        // Act
        Iterable<Locator> actualLocators = asset.setLocators(expectedLocators).getLocators();

        // Assert
        assertEquals(expectedLocators, actualLocators);
    }

    @Test
    public void testGetSetContentKeys() {
        // Arrange
        Iterable<ContentKey> expectedContentKeys = new ArrayList<ContentKey>();
        Asset asset = new Asset();

        // Act
        Iterable<ContentKey> actualContentKeys = asset.setContentKeys(expectedContentKeys).getContentKeys();

        // Assert
        assertEquals(expectedContentKeys, actualContentKeys);
    }

    @Test
    public void testGetSetFiles() {
        // Arrange 
        Iterable<File> expectedFiles = new ArrayList<File>();
        Asset asset = new Asset();

        // Act 
        Iterable<File> actualFiles = asset.setFiles(expectedFiles).getFiles();

        // Assert
        assertEquals(expectedFiles, actualFiles);
    }

    @Test
    public void testGetSetParentAsset() {
        // Arrange
        Iterable<Asset> expectedParentAssets = new ArrayList<Asset>();
        Asset asset = new Asset();

        // Act
        Iterable<Asset> actualAssets = asset.setParentAssets(expectedParentAssets).getParentAssets();

        // Assert
        assertEquals(expectedParentAssets, actualAssets);
    }
}
