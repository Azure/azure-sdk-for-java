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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.AssetState;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.EncryptionOption;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;

public class AssetIntegrationTest extends IntegrationTestBase {

    private void verifyInfosEqual(String message, AssetInfo expected, AssetInfo actual) {
        verifyAssetProperties(message, expected.getName(), expected.getAlternateId(), expected.getOptions(),
                expected.getState(), actual);
    }

    private void verifyAssetProperties(String message, String testName, String altId,
            EncryptionOption encryptionOption, AssetState assetState, AssetInfo actualAsset) {
        verifyAssetProperties(message, testName, altId, encryptionOption, assetState, null, null, null, actualAsset);
    }

    private void verifyAssetProperties(String message, String testName, String altId,
            EncryptionOption encryptionOption, AssetState assetState, String id, Date created, Date lastModified,
            AssetInfo actualAsset) {
        assertNotNull(message, actualAsset);
        assertEquals(message + " Name", testName, actualAsset.getName());
        assertEquals(message + " AlternateId", altId, actualAsset.getAlternateId());
        assertEquals(message + " Options", encryptionOption, actualAsset.getOptions());
        assertEquals(message + " State", assetState, actualAsset.getState());
        if (id != null) {
            assertEquals(message + " Id", id, actualAsset.getId());
        }
        if (created != null) {
            assertEquals(message + " Created", created, actualAsset.getCreated());
        }
        if (lastModified != null) {
            assertEquals(message + " LastModified", lastModified, actualAsset.getLastModified());
        }
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
            actualAsset = service.createAsset();
            // Assert
            verifyAssetProperties("actualAsset", "", "", EncryptionOption.None, AssetState.Initialized, actualAsset);
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

        // Assert
        verifyInfosEqual("actualAsset", assetInfo, actualAsset);
    }

    @Test
    public void getAssetInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));
        service.getAsset(invalidId);
    }

    @Test
    public void getAssetNonexistId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.getAsset(validButNonexistAssetId);
    }

    @Test
    public void listAssetSuccess() throws ServiceException {
        // Arrange
        String altId = "altId";
        EncryptionOption encryptionOption = EncryptionOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;

        String[] assetNames = new String[] { testAssetPrefix + "assetA", testAssetPrefix + "assetB" };
        List<AssetInfo> expectedAssets = new ArrayList<AssetInfo>();
        for (int i = 0; i < assetNames.length; i++) {
            String name = assetNames[i];
            CreateAssetOptions options = new CreateAssetOptions().setName(name).setAlternateId(altId)
                    .setOptions(encryptionOption).setState(assetState);
            expectedAssets.add(service.createAsset(options));
        }

        // Act
        Collection<AssetInfo> listAssetResult = service.listAssets();

        // Assert

        verifyListResultContains("listAssets", expectedAssets, listAssetResult, new ComponentDelegate() {
            @Override
            public void verifyEquals(String message, Object expected, Object actual) {
                verifyInfosEqual(message, (AssetInfo) expected, (AssetInfo) actual);
            }
        });
    }

    @Test
    public void canListAssetsWithOptions() throws ServiceException {
        String[] assetNames = new String[] { testAssetPrefix + "assetListOptionsA",
                testAssetPrefix + "assetListOptionsB", testAssetPrefix + "assetListOptionsC",
                testAssetPrefix + "assetListOptionsD" };
        List<AssetInfo> expectedAssets = new ArrayList<AssetInfo>();
        for (int i = 0; i < assetNames.length; i++) {
            String name = assetNames[i];
            CreateAssetOptions options = new CreateAssetOptions().setName(name);
            expectedAssets.add(service.createAsset(options));
        }

        ListAssetsOptions options = new ListAssetsOptions();
        options.getQueryParameters().add("$top", "2");
        Collection<AssetInfo> listAssetResult = service.listAssets(options);

        // Assert

        assertEquals(2, listAssetResult.size());
    }

    @Test
    public void updateAssetSuccess() throws Exception {
        // Arrange
        String originalTestName = testAssetPrefix + "updateAssetSuccessOriginal";
        EncryptionOption originalEncryptionOption = EncryptionOption.StorageEncrypted;
        AssetState originalAssetState = AssetState.Published;
        CreateAssetOptions originalOptions = new CreateAssetOptions().setAlternateId("altId")
                .setOptions(originalEncryptionOption).setState(originalAssetState).setName(originalTestName);
        AssetInfo originalAsset = service.createAsset(originalOptions);

        String updatedTestName = testAssetPrefix + "updateAssetSuccessUpdated";
        String altId = "otherAltId";
        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions().setName(updatedTestName).setAlternateId(altId);

        // Act
        service.updateAsset(originalAsset.getId(), updateAssetOptions);
        AssetInfo updatedAsset = service.getAsset(originalAsset.getId());

        // Assert
        verifyAssetProperties("updatedAsset", updatedTestName, altId, originalEncryptionOption, originalAssetState,
                updatedAsset);
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
        verifyInfosEqual("updatedAsset", originalAsset, updatedAsset);
    }

    @Test
    public void updateAssetFailedWithInvalidId() throws ServiceException {
        // Arrange
        UpdateAssetOptions updateAssetOptions = new UpdateAssetOptions();

        // Act
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.updateAsset(validButNonexistAssetId, updateAssetOptions);
    }

    @Test
    public void deleteAssetSuccess() throws Exception {
        // Arrange
        String assetName = testAssetPrefix + "deleteAssetSuccess";
        CreateAssetOptions createAssetOptions = new CreateAssetOptions().setName(assetName);
        AssetInfo assetInfo = service.createAsset(createAssetOptions);
        List<AssetInfo> listAssetsResult = service.listAssets();
        int assetCountBaseline = listAssetsResult.size();

        // Act
        service.deleteAsset(assetInfo.getId());

        // Assert
        listAssetsResult = service.listAssets();
        assertEquals("listAssetsResult.size", assetCountBaseline - 1, listAssetsResult.size());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.getAsset(assetInfo.getId());
    }

    @Test
    public void deleteAssetFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.deleteAsset(validButNonexistAssetId);
    }
}
