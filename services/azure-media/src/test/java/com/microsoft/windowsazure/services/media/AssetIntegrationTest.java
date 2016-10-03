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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.microsoft.windowsazure.core.utils.Base64;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetDeliveryPolicy;
import com.microsoft.windowsazure.services.media.models.AssetDeliveryPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AssetDeliveryPolicyType;
import com.microsoft.windowsazure.services.media.models.AssetDeliveryProtocol;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.AssetOption;
import com.microsoft.windowsazure.services.media.models.AssetState;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.LinkInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.ProtectionKey;
import com.microsoft.windowsazure.services.media.models.Task;
import com.microsoft.windowsazure.services.media.models.Task.CreateBatchOperation;

public class AssetIntegrationTest extends IntegrationTestBase {

    private void verifyInfosEqual(String message, AssetInfo expected,
            AssetInfo actual) {
        verifyAssetProperties(message, expected.getName(),
                expected.getAlternateId(), expected.getOptions(),
                expected.getState(), actual);
    }

    private void verifyAssetProperties(String message, String testName,
            String altId, AssetOption encryptionOption, AssetState assetState,
            AssetInfo actualAsset) {
        verifyAssetProperties(message, testName, altId, encryptionOption,
                assetState, null, null, null, actualAsset);
    }

    private void verifyAssetProperties(String message, String testName,
            String altId, AssetOption encryptionOption, AssetState assetState,
            String id, Date created, Date lastModified, AssetInfo actualAsset) {
        assertNotNull(message, actualAsset);
        assertEquals(message + " Name", testName, actualAsset.getName());
        assertEquals(message + " AlternateId", altId,
                actualAsset.getAlternateId());
        assertEquals(message + " Options", encryptionOption,
                actualAsset.getOptions());
        assertEquals(message + " State", assetState, actualAsset.getState());
        if (id != null) {
            assertEquals(message + " Id", id, actualAsset.getId());
        }
        if (created != null) {
            assertEquals(message + " Created", created,
                    actualAsset.getCreated());
        }
        if (lastModified != null) {
            assertEquals(message + " LastModified", lastModified,
                    actualAsset.getLastModified());
        }
    }

    @Test
    public void createAssetOptionsSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "createAssetOptionsSuccess";
        String altId = "altId";
        AssetOption encryptionOption = AssetOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;

        // Act
        AssetInfo actualAsset = service.create(Asset.create()
                .setAlternateId(altId).setOptions(encryptionOption)
                .setState(assetState).setName(testName));

        // Assert
        verifyAssetProperties("actualAsset", testName, altId, encryptionOption,
                assetState, actualAsset);
    }

    @Test
    public void createAssetMeanString() throws Exception {
        // Arrange
        String meanString = "'\"(?++\\+&==/&?''$@://   +ne <some><XML></stuff>"
                + "{\"jsonLike\":\"Created\":\"\\/Date(1336368841597)\\/\",\"Name\":null,cksum value\"}}"
                + "Some unicode: \uB2E4\uB974\uB2E4\uB294\u0625 \u064A\u062F\u064A\u0648\u0009\r\n";

        String testName = testAssetPrefix + "createAssetMeanString"
                + meanString;

        // Act
        AssetInfo actualAsset = service
                .create(Asset.create().setName(testName));

        // Assert
        assertEquals("actualAsset Name", testName, actualAsset.getName());
    }

    @Test
    public void createAssetNullNameSuccess() throws Exception {
        // Arrange

        // Act
        AssetInfo actualAsset = null;
        try {
            actualAsset = service.create(Asset.create());
            // Assert
            verifyAssetProperties("actualAsset", "", "", AssetOption.None,
                    AssetState.Initialized, actualAsset);
        } finally {
            // Clean up the anonymous asset now while we have the id, because we
            // do not want to delete all anonymous assets in the bulk-cleanup
            // code.
            try {
                if (actualAsset != null) {
                    service.delete(Asset.delete(actualAsset.getId()));
                }
            } catch (ServiceException ex) {
                // ex.printStackTrace();
            }
        }
    }

    @Test
    public void getAssetSuccess() throws Exception {
        // Arrange
        String testName = testAssetPrefix + "GetAssetSuccess";
        String altId = "altId";
        AssetOption encryptionOption = AssetOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;

        AssetInfo assetInfo = service.create(Asset.create().setName(testName)
                .setAlternateId(altId).setOptions(encryptionOption)
                .setState(assetState));

        // Act
        AssetInfo actualAsset = service.get(Asset.get(assetInfo.getId()));

        // Assert
        verifyInfosEqual("actualAsset", assetInfo, actualAsset);
    }

    @Test
    public void getAssetInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(Asset.get(invalidId));
    }

    @Test
    public void getAssetNonexistId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Asset.get(validButNonexistAssetId));
    }

    @Test
    public void listAssetSuccess() throws ServiceException {
        // Arrange
        String altId = "altId";
        AssetOption encryptionOption = AssetOption.StorageEncrypted;
        AssetState assetState = AssetState.Published;

        String[] assetNames = new String[] { testAssetPrefix + "assetA",
                testAssetPrefix + "assetB" };
        List<AssetInfo> expectedAssets = new ArrayList<AssetInfo>();
        for (int i = 0; i < assetNames.length; i++) {
            String name = assetNames[i];
            expectedAssets.add(service.create(Asset.create().setName(name)
                    .setAlternateId(altId).setOptions(encryptionOption)
                    .setState(assetState)));
        }

        // Act
        Collection<AssetInfo> listAssetResult = service.list(Asset.list());

        // Assert

        verifyListResultContains("listAssets", expectedAssets, listAssetResult,
                new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected,
                            Object actual) {
                        verifyInfosEqual(message, (AssetInfo) expected,
                                (AssetInfo) actual);
                    }
                });
    }

    @Test
    public void canListAssetsWithOptions() throws ServiceException {
        String[] assetNames = new String[] {
                testAssetPrefix + "assetListOptionsA",
                testAssetPrefix + "assetListOptionsB",
                testAssetPrefix + "assetListOptionsC",
                testAssetPrefix + "assetListOptionsD" };
        List<AssetInfo> expectedAssets = new ArrayList<AssetInfo>();
        for (int i = 0; i < assetNames.length; i++) {
            String name = assetNames[i];
            expectedAssets.add(service.create(Asset.create().setName(name)));
        }

        Collection<AssetInfo> listAssetResult = service.list(Asset.list()
                .setTop(2));

        // Assert

        assertEquals(2, listAssetResult.size());
    }

    @Test
    public void updateAssetSuccess() throws Exception {
        // Arrange
        String originalTestName = testAssetPrefix
                + "updateAssetSuccessOriginal";
        AssetOption originalEncryptionOption = AssetOption.StorageEncrypted;
        AssetState originalAssetState = AssetState.Initialized;
        AssetInfo originalAsset = service.create(Asset.create()
                .setName(originalTestName).setAlternateId("altId")
                .setOptions(originalEncryptionOption));

        String updatedTestName = testAssetPrefix + "updateAssetSuccessUpdated";
        String altId = "otherAltId";

        // Act
        service.update(Asset.update(originalAsset.getId())
                .setName(updatedTestName).setAlternateId(altId));
        AssetInfo updatedAsset = service.get(Asset.get(originalAsset.getId()));

        // Assert
        verifyAssetProperties("updatedAsset", updatedTestName, altId,
                originalEncryptionOption, originalAssetState, updatedAsset);
    }

    @Test
    public void updateAssetNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testAssetPrefix
                + "updateAssetNoChangesSuccess";
        String altId = "altId";
        AssetInfo originalAsset = service.create(Asset.create()
                .setName(originalTestName).setAlternateId(altId));

        // Act
        service.update(Asset.update(originalAsset.getId()));
        AssetInfo updatedAsset = service.get(Asset.get(originalAsset.getId()));

        // Assert
        verifyInfosEqual("updatedAsset", originalAsset, updatedAsset);
    }

    @Test
    public void updateAssetFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.update(Asset.update(validButNonexistAssetId));
    }

    @Test
    public void deleteAssetSuccess() throws Exception {
        // Arrange
        String assetName = testAssetPrefix + "deleteAssetSuccess";
        AssetInfo assetInfo = service.create(Asset.create().setName(assetName));
        List<AssetInfo> listAssetsResult = service.list(Asset.list());
        int assetCountBaseline = listAssetsResult.size();

        // Act
        service.delete(Asset.delete(assetInfo.getId()));

        // Assert
        listAssetsResult = service.list(Asset.list());
        assertEquals("listAssetsResult.size", assetCountBaseline - 1,
                listAssetsResult.size());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Asset.get(assetInfo.getId()));
    }

    @Test
    public void deleteAssetFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.delete(Asset.delete(validButNonexistAssetId));
    }

    @Test
    public void linkAssetContentKeySuccess() throws ServiceException,
            URISyntaxException, CertificateException {
        // Arrange
        String originalTestName = testAssetPrefix
                + "linkAssetContentKeySuccess";
        AssetInfo assetInfo = service.create(Asset.create()
                .setName(originalTestName)
                .setOptions(AssetOption.StorageEncrypted));

        String protectionKeyId = service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.StorageEncryption));
        String contentKeyId = String
                .format("nb:kid:UUID:%s", UUID.randomUUID());

        // Create a new ContentKey (secure random)
        byte[] contentKeyData = new byte[16];
        EncryptionUtils.eraseKey(contentKeyData);
        String protectionKey = service.action(ProtectionKey.getProtectionKey(protectionKeyId));
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(Base64.decode(protectionKey)));

        byte[] encryptedContentKeyBin = EncryptionUtils.encryptSymmetricKeyData(certificate, contentKeyData);
        String encryptedContentKey = Base64.encode(encryptedContentKeyBin);

        service.create(ContentKey.create(contentKeyId,
                ContentKeyType.StorageEncryption, encryptedContentKey)
                .setProtectionKeyId(protectionKeyId));

        // Act
        service.action(Asset.linkContentKey(assetInfo.getId(), contentKeyId));

        // Assert

        List<ContentKeyInfo> contentKeys = service.list(ContentKey
                .list(assetInfo.getContentKeysLink()));
        assertEquals(1, contentKeys.size());
        assertEquals(contentKeyId, contentKeys.get(0).getId());
    }

    @Test
    public void linkAssetContentKeyInvalidIdFailed() throws ServiceException,
            URISyntaxException {
        // Arrange

        // Act
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.action(Asset.linkContentKey(validButNonexistAssetId,
                "nb:kid:UUID:invalidContentKeyId"));

        // Assert
    }

    @Test
    public void canGetParentBackFromAsset() throws ServiceException,
            InterruptedException {
        // Arrange
        String originalAssetName = testAssetPrefix
                + "canGetParentBackFromAsset";
        AssetInfo originalAsset = service.create(Asset.create().setName(
                originalAssetName));

        int durationInMinutes = 10;
        AccessPolicyInfo accessPolicyInfo = service.create(AccessPolicy.create(
                testPolicyPrefix + "uploadAesPortectedAssetSuccess",
                durationInMinutes, EnumSet.of(AccessPolicyPermission.WRITE)));

        LocatorInfo locatorInfo = service.create(Locator.create(
                accessPolicyInfo.getId(), originalAsset.getId(),
                LocatorType.SAS));
        WritableBlobContainerContract blobWriter = service
                .createBlobWriter(locatorInfo);

        InputStream mpeg4H264InputStream = getClass().getResourceAsStream(
                "/media/MPEG4-H264.mp4");
        blobWriter.createBlockBlob("MPEG4-H264.mp4", mpeg4H264InputStream);
        service.action(AssetFile.createFileInfos(originalAsset.getId()));

        String jobName = testJobPrefix + "createJobSuccess";
        CreateBatchOperation taskCreator = Task
                .create(MEDIA_ENCODER_MEDIA_PROCESSOR_ID,
                        "<taskBody>"
                                + "<inputAsset>JobInputAsset(0)</inputAsset>"
                                + "<outputAsset>JobOutputAsset(0)</outputAsset>"
                                + "</taskBody>")
                .setConfiguration("H.264 256k DSL CBR")
                .setName("My encoding Task");
        JobInfo jobInfo = service.create(Job.create().setName(jobName)
                .addInputMediaAsset(originalAsset.getId())
                .addTaskCreator(taskCreator));

        // Act
        ListResult<AssetInfo> outputAssets = service.list(Asset.list(jobInfo
                .getOutputAssetsLink()));
        assertEquals(1, outputAssets.size());
        AssetInfo childAsset = outputAssets.get(0);

        LinkInfo<AssetInfo> parentAssetLink = childAsset.getParentAssetsLink();
        AssetInfo parentAsset = service.get(Asset.get(parentAssetLink));

        // Assert
        assertEquals(originalAsset.getId(), parentAsset.getId());
    }
    
    @Test
    public void unlinkAssetContentKeySuccess() throws ServiceException,
            URISyntaxException, CertificateException {
        // Arrange
        String originalTestName = testAssetPrefix
                + "linkAssetContentKeySuccess";
        AssetInfo assetInfo = service.create(Asset.create()
                .setName(originalTestName)
                .setOptions(AssetOption.StorageEncrypted));

        String protectionKeyId = service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.StorageEncryption));
        String contentKeyId = String
                .format("nb:kid:UUID:%s", UUID.randomUUID());

        // Create a new ContentKey (secure random)
        byte[] contentKeyData = new byte[16];
        EncryptionUtils.eraseKey(contentKeyData);
        String protectionKey = service.action(ProtectionKey.getProtectionKey(protectionKeyId));
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(Base64.decode(protectionKey)));

        byte[] encryptedContentKeyBin = EncryptionUtils.encryptSymmetricKeyData(certificate, contentKeyData);
        String encryptedContentKey = Base64.encode(encryptedContentKeyBin);

        service.create(ContentKey.create(contentKeyId,
                ContentKeyType.StorageEncryption, encryptedContentKey)
                .setProtectionKeyId(protectionKeyId));
        service.action(Asset.linkContentKey(assetInfo.getId(), contentKeyId));

        // Act
        service.delete(Asset.unlinkContentKey(assetInfo.getId(), contentKeyId));

        // Assert
        List<ContentKeyInfo> contentKeys = service.list(ContentKey
                .list(assetInfo.getContentKeysLink()));
        assertEquals(0, contentKeys.size());
    }
    
    @Test
    public void linkAssetDeliveryPolicySuccess() throws ServiceException,
            URISyntaxException, CertificateException {
        // Arrange
        String originalTestName = testAssetPrefix
                + "linkAssetContentKeySuccess";
        AssetDeliveryPolicyInfo adpInfo = service.create(AssetDeliveryPolicy.create()
                .setName(originalTestName)
                .setAssetDeliveryPolicyType(AssetDeliveryPolicyType.NoDynamicEncryption)
                .setAssetDeliveryProtocol(EnumSet.of(AssetDeliveryProtocol.SmoothStreaming)));
        
        AssetInfo assetInfo = service.create(Asset.create()
                .setName(originalTestName)
                .setOptions(AssetOption.StorageEncrypted));
        
        String protectionKeyId = service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.StorageEncryption));
        String contentKeyId = String
                .format("nb:kid:UUID:%s", UUID.randomUUID());

        // Create a new ContentKey (secure random)
        byte[] contentKeyData = new byte[16];
        EncryptionUtils.eraseKey(contentKeyData);
        String protectionKey = service.action(ProtectionKey.getProtectionKey(protectionKeyId));
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(Base64.decode(protectionKey)));

        byte[] encryptedContentKeyBin = EncryptionUtils.encryptSymmetricKeyData(certificate, contentKeyData);
        String encryptedContentKey = Base64.encode(encryptedContentKeyBin);

        service.create(ContentKey.create(contentKeyId,
                ContentKeyType.StorageEncryption, encryptedContentKey)
                .setProtectionKeyId(protectionKeyId));        
        service.action(Asset.linkContentKey(assetInfo.getId(), contentKeyId));
        
        // Act
        service.action(Asset.linkDeliveryPolicy(assetInfo.getId(), adpInfo.getId()));
        
        // Assert
        AssetInfo assetInfo2 = service.get(Asset.get(assetInfo.getId()));
        ListResult<AssetDeliveryPolicyInfo> listResult = service.list(AssetDeliveryPolicy.list(assetInfo2.getDeliveryPoliciesLink()));
        assertNotNull(listResult);
        assertEquals(listResult.size(), 1);
        assertEquals(listResult.get(0).getName(), originalTestName);
        assertEquals(listResult.get(0).getAssetDeliveryPolicyType(), AssetDeliveryPolicyType.NoDynamicEncryption);
    }
    
    @Test
    public void unlinkAssetDeliveryPolicySuccess() throws ServiceException,
            URISyntaxException, CertificateException {
        // Arrange
        String originalTestName = testAssetPrefix
                + "unlinkAssetContentKeySuccess";
        AssetDeliveryPolicyInfo adpInfo = service.create(AssetDeliveryPolicy.create()
                .setName(originalTestName)
                .setAssetDeliveryPolicyType(AssetDeliveryPolicyType.NoDynamicEncryption)
                .setAssetDeliveryProtocol(EnumSet.of(AssetDeliveryProtocol.SmoothStreaming)));
        
        AssetInfo assetInfo = service.create(Asset.create()
                .setName(originalTestName)
                .setOptions(AssetOption.StorageEncrypted));
        
        String protectionKeyId = service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.StorageEncryption));
        String contentKeyId = String
                .format("nb:kid:UUID:%s", UUID.randomUUID());

        // Create a new ContentKey (secure random)
        byte[] contentKeyData = new byte[16];
        EncryptionUtils.eraseKey(contentKeyData);
        String protectionKey = service.action(ProtectionKey.getProtectionKey(protectionKeyId));
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(Base64.decode(protectionKey)));

        byte[] encryptedContentKeyBin = EncryptionUtils.encryptSymmetricKeyData(certificate, contentKeyData);
        String encryptedContentKey = Base64.encode(encryptedContentKeyBin);

        service.create(ContentKey.create(contentKeyId,
                ContentKeyType.StorageEncryption, encryptedContentKey)
                .setProtectionKeyId(protectionKeyId));        
        service.action(Asset.linkContentKey(assetInfo.getId(), contentKeyId));
        service.action(Asset.linkDeliveryPolicy(assetInfo.getId(), adpInfo.getId()));
        
        // Act
        service.delete(Asset.unlinkDeliveryPolicy(assetInfo.getId(), adpInfo.getId()));
        
        // Assert
        AssetInfo assetInfo2 = service.get(Asset.get(assetInfo.getId()));
        ListResult<AssetDeliveryPolicyInfo> listResult = service.list(AssetDeliveryPolicy.list(assetInfo2.getDeliveryPoliciesLink()));
        assertNotNull(listResult);
        assertEquals(listResult.size(), 0);
    }
}
