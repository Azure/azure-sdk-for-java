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

import org.junit.Test;

public class AssetInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        AssetInfo assetInfo = new AssetInfo();

        // Act 
        String actualId = assetInfo.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetState() {
        // Arrange
        AssetState expectedState = AssetState.Published;
        AssetInfo assetInfo = new AssetInfo();

        // Act
        AssetState actualState = assetInfo.setState(expectedState).getState();

        // Assert
        assertEquals(expectedState, actualState);
    }

    @Test
    public void testGetSetCreated() {
        // Arrange
        Date expectedCreated = new Date();
        AssetInfo assetInfo = new AssetInfo();

        // Act 
        Date actualCreated = assetInfo.setCreate(expectedCreated).getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);

    }

    @Test
    public void testGetSetLastModified() {
        // Arrange
        Date expectedLastModified = new Date();
        AssetInfo assetInfo = new AssetInfo();

        // Act
        Date actualLastModified = assetInfo.setLastModified(expectedLastModified).getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetAlternateId() {
        // Arrange
        String expectedAlternateId = "testAlternateId";
        AssetInfo assetInfo = new AssetInfo();

        // Act
        String actualAlternateId = assetInfo.setAlternateId(expectedAlternateId).getAlternateId();

        // Assert
        assertEquals(expectedAlternateId, actualAlternateId);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testName";
        AssetInfo assetInfo = new AssetInfo();

        // Act
        String actualName = assetInfo.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetOptions() {
        // Arrange
        EncryptionOption expectedOptions = EncryptionOption.None;
        AssetInfo assetInfo = new AssetInfo();

        // Act
        EncryptionOption actualOptions = assetInfo.setOptions(expectedOptions).getOptions();

        // Assert
        assertEquals(expectedOptions, actualOptions);
    }

    @Test
    public void testGetSetLocators() {
        // Arrange
        Iterable<LocatorInfo> expectedLocators = new ArrayList<LocatorInfo>();
        AssetInfo assetInfo = new AssetInfo();

        // Act
        Iterable<LocatorInfo> actualLocators = assetInfo.setLocators(expectedLocators).getLocators();

        // Assert
        assertEquals(expectedLocators, actualLocators);
    }

    @Test
    public void testGetSetContentKeys() {
        // Arrange
        Iterable<ContentKeyInfo> expectedContentKeys = new ArrayList<ContentKeyInfo>();
        AssetInfo assetInfo = new AssetInfo();

        // Act
        Iterable<ContentKeyInfo> actualContentKeys = assetInfo.setContentKeys(expectedContentKeys).getContentKeys();

        // Assert
        assertEquals(expectedContentKeys, actualContentKeys);
    }

    @Test
    public void testGetSetFiles() {
        // Arrange 
        Iterable<FileInfo> expectedFiles = new ArrayList<FileInfo>();
        AssetInfo assetInfo = new AssetInfo();

        // Act 
        Iterable<FileInfo> actualFiles = assetInfo.setFiles(expectedFiles).getFiles();

        // Assert
        assertEquals(expectedFiles, actualFiles);
    }

    @Test
    public void testGetSetParentAsset() {
        // Arrange
        Iterable<AssetInfo> expectedParentAssets = new ArrayList<AssetInfo>();
        AssetInfo asset = new AssetInfo();

        // Act
        Iterable<AssetInfo> actualAssets = asset.setParentAssets(expectedParentAssets).getParentAssets();

        // Assert
        assertEquals(expectedParentAssets, actualAssets);
    }
}
