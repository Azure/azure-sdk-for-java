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
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;

public class MediaServiceIntegrationTest extends IntegrationTestBase {
    private static MediaContract service;

    @BeforeClass
    public static void setup() throws Exception {
        // Create all test containers and their content
        config = createConfig();
        service = MediaService.create(config);
        List<AssetInfo> listAssetsResult = service.listAssets(null);
        for (AssetInfo assetInfo : listAssetsResult) {
            try {
                service.deleteAsset(assetInfo.getId());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
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
        AssetInfo actualAsset = service.createAsset("testAssetName");

        // Assert
        assertEquals(expectedAsset.getName(), actualAsset.getName());
    }

    @Test
    public void createAssetNullNameSuccess() throws ServiceException {
        // Arrange

        // Act
        AssetInfo actualAsset = service.createAsset(null);

        // Assert
        assertNotNull(actualAsset);
    }

    @Test
    public void getAssetSuccess() throws Exception {
        // Arrange
        AssetInfo expectedAsset = new AssetInfo().setName("testGetAssetSuccess");
        AssetInfo assetInfo = service.createAsset("testGetAssetSuccess");

        // Act
        AssetInfo actualAsset = service.getAsset(assetInfo.getId());

        // Assert
        assertEquals(expectedAsset.getName(), actualAsset.getName());
    }

    @Test(expected = ServiceException.class)
    public void getAssetFailedWithInvalidId() throws ServiceException {
        // Arrange
        AssetInfo expectedAsset = new AssetInfo().setId("IncorrectAssetId");

        // Act
        AssetInfo actualAsset = service.getAsset(expectedAsset.getId());

        // Assert
        assertTrue(false);

    }

    @Test
    public void listAssetSuccess() throws ServiceException {
        // Arrange
        Collection<AssetInfo> listAssetResultBaseLine = service.listAssets();
        AssetInfo assetA = new AssetInfo();
        AssetInfo assetB = new AssetInfo();
        service.createAsset("assetA");
        service.createAsset("assetB");

        // Act
        Collection<AssetInfo> listAssetResult = service.listAssets();
        // Assert        

        assertEquals(listAssetResultBaseLine.size() + 2, listAssetResult.size());
    }

    @Test
    public void updateAssetSuccess() throws Exception {
        // Arrange

        AssetInfo updatedAsset = service.createAsset("updateAssetSuccess");
        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions().setName("updateAssetSuccessResult");
        updatedAsset.setName("updateAssetSuccessResult");

        // Act
        service.updateAsset(updatedAsset.getId(), updateAssetOptions);
        AssetInfo actualAsset = service.getAsset(updatedAsset.getId());

        // Assert
        assertEquals(updatedAsset.getName(), actualAsset.getName());

    }

    @Test(expected = ServiceException.class)
    public void updateAssetFailedWithInvalidId() throws ServiceException {
        // Arrange
        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions();

        // Act
        service.updateAsset("updateAssetFailedWithInvalidId", updateAssetOptions);

        // Assert
        assertTrue(false);
    }

    @Test
    public void deleteAssetSuccess() throws Exception {
        // Arrange
        String assetName = "deleteAssetSuccess";
        AssetInfo assetInfo = service.createAsset(assetName);
        List<AssetInfo> listAssetsResult = service.listAssets(null);
        int assetCountBaseline = listAssetsResult.size();

        // Act
        service.deleteAsset(assetInfo.getId());

        // Assert
        listAssetsResult = service.listAssets(null);
        assertEquals(assetCountBaseline - 1, listAssetsResult.size());
    }

    @Test(expected = ServiceException.class)
    public void deleteAssetFailedWithInvalidId() throws ServiceException {
        // Arrange

        // Act
        service.deleteAsset("invalidAssetId");

        // Assert
        assertTrue(false);
    }

}