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
import org.junit.Ignore;
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

    private void verifyAssetProperties(String message, String testName, String altId,
            EncryptionOption encryptionOption, AssetState assetState, AssetInfo actualAsset) {
        assertNotNull(message, actualAsset);
        assertEquals(message + " Name", testName, actualAsset.getName());
        assertEquals(message + " AlternateId", altId, actualAsset.getAlternateId());
        assertEquals(message + " Options", encryptionOption, actualAsset.getOptions());
        assertEquals(message + " State", assetState, actualAsset.getState());
    }

    @Before
    public void setupInstance() throws Exception {
        service = MediaService.create(config);
    }

    @Test
    public void createAssetSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "Name";
        CreateAssetOptions options = new CreateAssetOptions().setName(testName);

        // Act
        AssetInfo actualAsset = service.createAsset(options);

        // Assert
        verifyAssetProperties("actualAsset", testName, "", EncryptionOption.None, AssetState.Initialized, actualAsset);
    }

    @Test
    public void createAssetOptionsSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "createAssetOptionsSuccess";
        String altId = "altId";
        EncryptionOption encryptionOption = EncryptionOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;
        CreateAssetOptions options = new CreateAssetOptions().setAlternateId(altId).setOptions(encryptionOption)
                .setState(assetState).setName(testName);

        // Act
        AssetInfo actualAsset = service.createAsset(options);

        // Assert
        verifyAssetProperties("actualAsset", testName, altId, encryptionOption, assetState, actualAsset);
    }

    @Test
    public void createAssetMeanString() throws Exception {
        // Arrange
        String meanString = "'\"(?++\\+&==/&?''$@://   +ne <some><XML></stuff>"
                + "{\"jsonLike\":\"Created\":\"\\/Date(1336368841597)\\/\",\"Name\":null,cksum value\"}}"
                + "Some unicode: \uB2E4\uB974\uB2E4\uB294\u0625 \u064A\u062F\u064A\u0648\u0009\r\n";

        String testName = testAssetPrefix + "createAssetMeanString" + meanString;
        CreateAssetOptions createAssetOptions = new CreateAssetOptions().setName(testName);

        // Act
        AssetInfo actualAsset = service.createAsset(createAssetOptions);

        // Assert
        assertEquals("actualAsset Name", testName, actualAsset.getName());
    }

    @Test
    public void createAssetNullNameSuccess() throws Exception {
        // Arrange

        // Act
        AssetInfo actualAsset = null;
        try {
            actualAsset = service.createAsset(null);
            // Assert
            assertNotNull("actualAsset", actualAsset);
            assertEquals("actualAsset.getName() should be the service default value, the empty string", "",
                    actualAsset.getName());
        }
        finally {
            // Clean up the anonymous asset now while we have the id, because we
            // do not want to delete all anonymous assets in the bulk-cleanup code.
            try {
                if (actualAsset != null) {
                    service.deleteAsset(actualAsset.getId());
                }
            }
            catch (ServiceException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void getAssetSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "GetAssetSuccess";
        String altId = "altId";
        EncryptionOption encryptionOption = EncryptionOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;
        CreateAssetOptions options = new CreateAssetOptions().setAlternateId(altId).setOptions(encryptionOption)
                .setState(assetState).setName(testName);
        AssetInfo assetInfo = service.createAsset(options);

        // Act
        AssetInfo actualAsset = service.getAsset(assetInfo.getId());

        verifyAssetProperties("actualAsset", testName, altId, encryptionOption, assetState, actualAsset);
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
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();
        service.createAsset(createAssetOptions.setName(testAssetPrefix + "assetA"));
        service.createAsset(createAssetOptions.setName(testAssetPrefix + "assetB"));

        // Act
        Collection<AssetInfo> listAssetResult = service.listAssets();

        // Assert
        assertEquals(listAssetResultBaseLine.size() + 2, listAssetResult.size());
    }

    @Ignore
    // Bug https://github.com/WindowsAzure/azure-sdk-for-java-pr/issues/364
    @Test
    public void updateAssetSuccess() throws Exception {
        // Arrange
        String originalTestName = testAssetPrefix + "updateAssetSuccessOriginal";
        CreateAssetOptions originalOptions = new CreateAssetOptions().setAlternateId("altId")
                .setOptions(EncryptionOption.StorageEncrypted).setState(AssetState.Published).setName(originalTestName);
        AssetInfo originalAsset = service.createAsset(originalOptions);

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
        verifyAssetProperties("updatedAsset", updatedTestName, altId, encryptionOption, assetState, updatedAsset);
    }

    @Test
    public void updateAssetNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testAssetPrefix + "updateAssetNoChangesSuccess";
        String altId = "altId";
        EncryptionOption encryptionOption = EncryptionOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;
        CreateAssetOptions options = new CreateAssetOptions().setAlternateId(altId).setOptions(encryptionOption)
                .setState(assetState).setName(originalTestName);
        AssetInfo originalAsset = service.createAsset(options);

        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions();

        // Act
        service.updateAsset(originalAsset.getId(), updateAssetOptions);
        AssetInfo updatedAsset = service.getAsset(originalAsset.getId());

        // Assert
        verifyAssetProperties("updatedAsset", originalTestName, altId, encryptionOption, assetState, updatedAsset);
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
        CreateAssetOptions createAssetOptions = new CreateAssetOptions().setName(assetName);
        AssetInfo assetInfo = service.createAsset(createAssetOptions);
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