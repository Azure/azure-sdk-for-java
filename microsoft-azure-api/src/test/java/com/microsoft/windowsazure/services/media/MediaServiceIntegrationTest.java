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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.EncryptionOption;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;

public class MediaServiceIntegrationTest extends IntegrationTestBase {
    private MediaContract service;
    private static String testAssetPrefix = "testAsset";
    private static String fakeAssetId = "nb:cid:UUID:00000000-0000-4a00-0000-000000000000";

    @BeforeClass
    public static void setup() throws Exception {
        cleanupEnvironment();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        cleanupEnvironment();
    }

    private static void cleanupEnvironment() {
        config = createConfig();
        MediaContract service = MediaService.create(config);
        try {
            List<AssetInfo> listAssetsResult = service.listAssets();
            for (AssetInfo assetInfo : listAssetsResult) {
                if (assetInfo.getName().startsWith(testAssetPrefix)) {
                    service.deleteAsset(assetInfo.getId());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    @Before
    public void setupInstance() throws Exception {
        service = MediaService.create(config);
    }

    @Test
    public void createAssetSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "Name";

        // Act
        AssetInfo actualAsset = service.createAsset(testName);

        // Assert
        assertEquals("testName and actualAsset name", testName, actualAsset.getName());
    }

    @Test
    public void createAssetNullNameSuccess() throws ServiceException {
        // Arrange

        EncryptionOption encryptionOption = EncryptionOption.StorageEncrypted;
        CreateAssetOptions options = new CreateAssetOptions().setOptions(encryptionOption);
        // Act
        AssetInfo actualAsset = service.createAsset(null, options);

        // Assert
        assertNotNull("actualAsset", actualAsset);
        assertEquals("actualAsset.getName() should be the service default value, the empty string", "",
                actualAsset.getName());
    }

    @Test
    public void getAssetSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "GetAssetSuccess";
        AssetInfo assetInfo = service.createAsset(testName);

        // Act
        AssetInfo actualAsset = service.getAsset(assetInfo.getId());

        // Assert
        assertEquals(testName, actualAsset.getName());
    }

    @Test
    public void getAssetFailedWithInvalidId() throws ServiceException {
        // Arrange

        // Act
        try {
            service.getAsset(fakeAssetId);
            // Should not get here

            // Assert
            fail();
        }
        catch (ServiceException ex) {
            // Assert
            assertEquals("Error code should be not found, because asset does not exist to update", 404,
                    ex.getHttpStatusCode());
        }
    }

    @Test
    public void listAssetSuccess() throws ServiceException {
        // Arrange
        Collection<AssetInfo> listAssetResultBaseLine = service.listAssets();
        service.createAsset(testAssetPrefix + "assetA");
        service.createAsset(testAssetPrefix + "assetB");

        // Act
        Collection<AssetInfo> listAssetResult = service.listAssets();

        // Assert
        assertEquals(listAssetResultBaseLine.size() + 2, listAssetResult.size());
    }

    @Test
    public void updateAssetSuccess() throws Exception {
        // Arrange
        String originalTestName = testAssetPrefix + "UpdateAssetSuccess";
        String updatedTestName = testAssetPrefix + "UpdateAssetSuccess";

        AssetInfo originalAsset = service.createAsset(originalTestName);
        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions().setName(updatedTestName);

        // Act
        service.updateAsset(originalAsset.getId(), updateAssetOptions);
        AssetInfo updatedAsset = service.getAsset(originalAsset.getId());

        // Assert
        assertEquals(updatedTestName, updatedAsset.getName());
    }

    @Test
    public void updateAssetFailedWithInvalidId() throws ServiceException {
        // Arrange
        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions();

        // Act

        try {
            service.updateAsset(fakeAssetId, updateAssetOptions);

            // Assert
            fail();
        }
        catch (ServiceException ex) {
            // Assert
            assertEquals("Error code should be not found, because asset does not exist to update", 404,
                    ex.getHttpStatusCode());
        }
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

    @Test
    public void deleteAssetFailedWithInvalidId() throws ServiceException {
        // Arrange

        // Act
        try {
            service.deleteAsset(fakeAssetId);

            // Assert
            fail();
        }
        catch (ServiceException ex) {
            // Assert
            assertEquals("Error code should be not found, because asset does not exist to update", 404,
                    ex.getHttpStatusCode());
        }
    }
}