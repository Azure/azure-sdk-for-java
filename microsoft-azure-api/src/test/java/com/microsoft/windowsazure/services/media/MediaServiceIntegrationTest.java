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
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;

public class MediaServiceIntegrationTest extends IntegrationTestBase {
    private static MediaContract service;

    @BeforeClass
    public static void setup() throws Exception {
        // Create all test containers and their content
        config = createConfig();
        service = MediaService.create(config);
        List<Asset> listAssetsResult = service.listAssets(null);
        for (Asset asset : listAssetsResult) {
            service.deleteAsset(asset.getId());
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
        Asset expectedAsset = new Asset().setName("testAssetName");

        // Act
        Asset actualAsset = service.createAsset(expectedAsset);

        // Assert
        assertEquals(expectedAsset, actualAsset);
    }

    @Test
    public void createAssetMissingNameFailed() {
        // Arrange
        Asset expectedAsset = new Asset();

        // Act
        Asset actualAsset = service.createAsset(expectedAsset);

        // Assert
        assertTrue(false);
    }

    @Test
    public void getAssetSuccess() throws Exception {
        // Arrange
        Asset expectedAsset = new Asset();
        service.createAsset(expectedAsset);

        // Act
        Asset actualAsset = service.getAsset(expectedAsset);

        // Assert
        assertEquals(expectedAsset, actualAsset);
    }

    @Test
    public void getAssetFailedWithInvalidId() {
        // Arrange
        Asset expectedAsset = new Asset();
        service.createAsset(expectedAsset);

        // Act
        Asset actualAsset = service.getAsset(expectedAsset.setName("IncorrectAssetName"));

        // Assert
        assertTrue(false);

    }

    @Test
    public void listAssetSuccess() {
        // Arrange
        Asset assetA = new Asset();
        Asset assetB = new Asset();
        service.createAsset(assetA);
        service.createAsset(assetB);
        ListAssetsOptions listAssetOptions = new ListAssetsOptions();

        // Act
        Collection<Asset> listAssetResult = service.listAssets(listAssetOptions);
        // Assert        

        assertEquals(2, listAssetResult.size());
    }

    @Test
    public void listAssetFailed() {
        // Arrange
        ListAssetsOptions listAssetsOptions = new ListAssetsOptions();

        // Act
        Collection<Asset> listAssetResult = service.listAssets(listAssetsOptions);

        // Assert
        assertTrue(false);
    }

    @Test
    public void updateAssetSuccess() throws Exception {
        // Arrange
        Asset originalAsset = new Asset();
        service.createAsset(originalAsset);
        Asset updatedAsset = new Asset();

        // Act
        service.updateAsset(updatedAsset);
        Asset actualAsset = service.updateAsset(updatedAsset);

        // Assert
        assertEquals(updatedAsset, actualAsset);

    }

    @Test
    public void updateAssetFailedWithInvalidId() {
        // Arrange
        MediaContract service = MediaService.create(config);
        Asset updatedAsset = new Asset();

        // Act
        service.updateAsset(updatedAsset);

        // Assert
        assertTrue(false);
    }

    @Test
    public void deleteAssetSuccess() throws Exception {
        // Arrange
        Asset asset = new Asset();
        service.createAsset(asset);
        List<Asset> listAssetsResult = service.listAssets(null);
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