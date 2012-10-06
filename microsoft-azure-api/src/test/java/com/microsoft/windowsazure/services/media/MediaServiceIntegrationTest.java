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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.AssetState;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.EncryptionOption;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;

public class MediaServiceIntegrationTest extends IntegrationTestBase {
    private MediaContract service;
    private static String testAssetPrefix = "testAsset";
    private static String fakeAssetId = "nb:cid:UUID:00000000-0000-4a00-0000-000000000000";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
                if (assetInfo.getName().startsWith(testAssetPrefix) || assetInfo.getName().equals("")) {
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
        assertEquals("actualAsset Name", testName, actualAsset.getName());
        assertEquals("actualAsset AlternateId", "", actualAsset.getAlternateId());
        assertEquals("actualAsset Options", EncryptionOption.None, actualAsset.getOptions());
        assertEquals("actualAsset State", AssetState.Initialized, actualAsset.getState());
    }

    @Test
    public void createAssetOptionsSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "createAssetOptionsSuccess";
        String altId = "altId";
        EncryptionOption encryptionOption = EncryptionOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;
        CreateAssetOptions options = new CreateAssetOptions().setAlternateId(altId).setOptions(encryptionOption)
                .setState(assetState);

        // Act
        AssetInfo actualAsset = service.createAsset(testName, options);

        // Assert
        assertEquals("actualAsset Name", testName, actualAsset.getName());
        assertEquals("actualAsset AlternateId", altId, actualAsset.getAlternateId());
        assertEquals("actualAsset Options", encryptionOption, actualAsset.getOptions());
        assertEquals("actualAsset State", assetState, actualAsset.getState());
    }

    @Test
    public void createAssetMeanString() throws Exception {
        // Arrange
        String meanString = "'\"(?++\\+&==/&?''$@://   +ne <some><XML></stuff>"
                + "{\"jsonLike\":\"Created\":\"\\/Date(1336368841597)\\/\",\"Name\":null,cksum value\"}}"
                + "Some unicode: \uB2E4\uB974\uB2E4\uB294\u0625 \u064A\u062F\u064A\u0648\u0009\r\n";

        String testName = testAssetPrefix + "createAssetMeanString" + meanString;

        // Act
        AssetInfo actualAsset = service.createAsset(testName);

        // Assert
        assertEquals("actualAsset Name", testName, actualAsset.getName());
    }

    @Test
    public void createAssetNullNameSuccess() throws ServiceException {
        // Arrange

        // Act
        AssetInfo actualAsset = service.createAsset(null);

        // Assert
        assertNotNull("actualAsset", actualAsset);
        assertEquals("actualAsset.getName() should be the service default value, the empty string", "",
                actualAsset.getName());
    }

    @Test
    public void getAssetSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "GetAssetSuccess";
        String altId = "altId";
        EncryptionOption encryptionOption = EncryptionOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;
        CreateAssetOptions options = new CreateAssetOptions().setAlternateId(altId).setOptions(encryptionOption)
                .setState(assetState);
        AssetInfo assetInfo = service.createAsset(testName, options);

        // Act
        AssetInfo actualAsset = service.getAsset(assetInfo.getId());

        // Assert
        assertEquals("actualAsset Name", testName, actualAsset.getName());
        assertEquals("actualAsset AlternateId", altId, actualAsset.getAlternateId());
        assertEquals("actualAsset Options", encryptionOption, actualAsset.getOptions());
        assertEquals("actualAsset State", assetState, actualAsset.getState());
    }

    @Test
    public void getAssetFailedWithInvalidId() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expect(new ServiceExceptionMatcher(404));
        service.getAsset(fakeAssetId);
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
        String originalTestName = testAssetPrefix + "updateAssetSuccessOriginal";
        CreateAssetOptions originalOptions = new CreateAssetOptions().setAlternateId("altId")
                .setOptions(EncryptionOption.StorageEncrypted).setState(AssetState.Published);
        AssetInfo originalAsset = service.createAsset(originalTestName, originalOptions);

        String updatedTestName = testAssetPrefix + "updateAssetSuccessUpdated";
        String altId = "otherAltId";
        EncryptionOption encryptionOption = EncryptionOption.None;
        AssetState assetState = AssetState.Initialized;
        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions().setName(updatedTestName).setAlternateId(altId)
                .setOptions(encryptionOption).setState(assetState);

        // Act
        service.updateAsset(originalAsset.getId(), updateAssetOptions);
        AssetInfo updatedAsset = service.getAsset(originalAsset.getId());

        // Assert
        assertEquals("updatedAsset Name", updatedTestName, updatedAsset.getName());
        assertEquals("updatedAsset AlternateId", altId, updatedAsset.getAlternateId());
        assertEquals("updatedAsset Options", encryptionOption, updatedAsset.getOptions());
        assertEquals("updatedAsset State", assetState, updatedAsset.getState());
    }

    @Test
    public void updateAssetNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testAssetPrefix + "updateAssetNoChangesSuccess";
        String altId = "altId";
        EncryptionOption encryptionOption = EncryptionOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;
        CreateAssetOptions options = new CreateAssetOptions().setAlternateId(altId).setOptions(encryptionOption)
                .setState(assetState);
        AssetInfo originalAsset = service.createAsset(originalTestName, options);

        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions();

        // Act
        service.updateAsset(originalAsset.getId(), updateAssetOptions);
        AssetInfo updatedAsset = service.getAsset(originalAsset.getId());

        // Assert
        assertEquals("updatedAsset Name", originalTestName, updatedAsset.getName());
        assertEquals("updatedAsset AlternateId", altId, updatedAsset.getAlternateId());
        assertEquals("updatedAsset Options", encryptionOption, updatedAsset.getOptions());
        assertEquals("updatedAsset State", assetState, updatedAsset.getState());
    }

    @Test
    public void updateAssetFailedWithInvalidId() throws ServiceException {
        // Arrange
        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions();

        // Act
        thrown.expect(ServiceException.class);
        thrown.expect(new ServiceExceptionMatcher(404));
        service.updateAsset(fakeAssetId, updateAssetOptions);
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
        listAssetsResult = service.listAssets();
        assertEquals(assetCountBaseline - 1, listAssetsResult.size());

        thrown.expect(ServiceException.class);
        thrown.expect(new ServiceExceptionMatcher(404));
        service.getAsset(assetInfo.getId());
    }

    @Test
    public void deleteAssetFailedWithInvalidId() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expect(new ServiceExceptionMatcher(404));
        service.deleteAsset(fakeAssetId);
    }
}