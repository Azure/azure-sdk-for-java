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
package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;

public class MediaServiceIntegrationTest extends IntegrationTestBase {
    private static MediaContract service;

    @BeforeClass
    public static void setup() throws Exception {
        // Create all test containers and their content
        config = createConfig();
        service = MediaService.create(config);
        List<AssetInfo> listAssetsResult = service.listAssets(null);
        for (AssetInfo assetInfo : listAssetsResult) {
            service.deleteAsset(assetInfo.getId());
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        // Configuration config = createConfiguration();
        // BlobContract service = BlobService.create(config);

        // deleteContainers(service, testContainersPrefix, testContainers);
        // deleteContainers(service, createableContainersPrefix, creatableContainers);
    }

    private static Configuration createConfig() {
        Configuration config = Configuration.getInstance();
        overrideWithEnv(config, MediaConfiguration.URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_ID);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_SECRET);
        overrideWithEnv(config, MediaConfiguration.OAUTH_SCOPE);
        return config;
    }

    @Test
    public void createAssetSuccess() throws Exception {
        // Arrange
        AssetInfo expectedAsset = new AssetInfo().setName("testAssetName");

        // Act
        AssetInfo actualAsset = service.createAsset(expectedAsset);

        // Assert
        assertEquals(expectedAsset, actualAsset);
    }

    @Test
    public void createAssetMissingNameFailed() {
        // Arrange
        AssetInfo expectedAsset = new AssetInfo();

        // Act
        AssetInfo actualAsset = service.createAsset(expectedAsset);

        // Assert
        assertTrue(false);
    }

    @Test
    public void getAssetSuccess() throws Exception {
        // Arrange
        AssetInfo expectedAsset = new AssetInfo();
        AssetInfo assetInfo = service.createAsset(expectedAsset);

        // Act
        AssetInfo actualAsset = service.getAsset(assetInfo.getId());

        // Assert
        assertEquals(expectedAsset, actualAsset);
    }

    @Test
    public void getAssetFailedWithInvalidId() {
        // Arrange
        AssetInfo expectedAsset = new AssetInfo();
        service.createAsset(expectedAsset);

        // Act
        AssetInfo actualAsset = service.getAsset(expectedAsset.setId("IncorrectAssetId").getId());

        // Assert
        assertTrue(false);

    }

    @Test
    public void listAssetSuccess() {
        // Arrange
        AssetInfo assetA = new AssetInfo();
        AssetInfo assetB = new AssetInfo();
        service.createAsset(assetA);
        service.createAsset(assetB);
        ListAssetsOptions listAssetOptions = new ListAssetsOptions();

        // Act
        Collection<AssetInfo> listAssetResult = service.listAssets(listAssetOptions);
        // Assert        

        assertEquals(2, listAssetResult.size());
    }

    @Test
    public void listAssetFailed() {
        // Arrange
        ListAssetsOptions listAssetsOptions = new ListAssetsOptions();

        // Act
        Collection<AssetInfo> listAssetResult = service.listAssets(listAssetsOptions);

        // Assert
        assertTrue(false);
    }

    @Test
    public void updateAssetSuccess() throws Exception {
        // Arrange
        AssetInfo originalAsset = new AssetInfo();
        service.createAsset(originalAsset);
        AssetInfo updatedAsset = new AssetInfo();

        // Act
        service.updateAsset(updatedAsset);
        AssetInfo actualAsset = service.updateAsset(updatedAsset);

        // Assert
        assertEquals(updatedAsset, actualAsset);

    }

    @Test
    public void updateAssetFailedWithInvalidId() {
        // Arrange
        MediaContract service = MediaService.create(config);
        AssetInfo updatedAsset = new AssetInfo();

        // Act
        service.updateAsset(updatedAsset);

        // Assert
        assertTrue(false);
    }

    @Test
    public void deleteAssetSuccess() throws Exception {
        // Arrange
        AssetInfo asset = new AssetInfo();
        service.createAsset(asset);
        List<AssetInfo> listAssetsResult = service.listAssets(null);
        assertEquals(1, listAssetsResult.size());

        // Act
        service.deleteAsset(asset.getId());

        // Assert
        listAssetsResult = service.listAssets(null);
        assertEquals(0, listAssetsResult.size());
    }

    @Test
    public void deleteAssetFailedWithInvalidId() {
        // Arrange

        // Act
        service.deleteAsset("invalidAssetId");

        // Assert
        assertTrue(false);
    }

}