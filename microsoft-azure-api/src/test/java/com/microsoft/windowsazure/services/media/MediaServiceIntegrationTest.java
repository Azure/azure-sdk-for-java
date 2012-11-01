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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBElement;

import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.implementation.atom.IdType;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.AssetState;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyOptions;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.CreateJobOptions;
import com.microsoft.windowsazure.services.media.models.CreateLocatorOptions;
import com.microsoft.windowsazure.services.media.models.CreateTaskOptions;
import com.microsoft.windowsazure.services.media.models.EncryptionOption;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;
import com.microsoft.windowsazure.services.media.models.ListJobsResult;
import com.microsoft.windowsazure.services.media.models.ListLocatorsResult;
import com.microsoft.windowsazure.services.media.models.ListMediaProcessorsOptions;
import com.microsoft.windowsazure.services.media.models.ListMediaProcessorsResult;
import com.microsoft.windowsazure.services.media.models.ListTasksOptions;
import com.microsoft.windowsazure.services.media.models.ListTasksResult;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;
import com.microsoft.windowsazure.services.media.models.UpdateLocatorOptions;

public class MediaServiceIntegrationTest extends IntegrationTestBase {

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
            actualAsset = service.createAsset();
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

        assertEquals("Id", assetInfo.getId(), actualAsset.getId());
        verifyAssetProperties("actualAsset", testName, altId, encryptionOption, assetState, actualAsset);
    }

    @Test
    public void getAssetFailedWithInvalidId() throws ServiceException {
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
    public void listTopThreeAssetsSuccess() throws ServiceException {
        // Arrange
        Collection<AssetInfo> listAssetResultBaseLine = service.listAssets();
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();
        service.createAsset(createAssetOptions.setName(testAssetPrefix + "assetA"));
        service.createAsset(createAssetOptions.setName(testAssetPrefix + "assetB"));
        service.createAsset(createAssetOptions.setName(testAssetPrefix + "assetC"));
        service.createAsset(createAssetOptions.setName(testAssetPrefix + "assetD"));
        ListAssetsOptions listAssetsOptions = new ListAssetsOptions();
        listAssetsOptions.getQueryParameters().add("$top", "3");

        // Act
        Collection<AssetInfo> listAssetResult = service.listAssets(listAssetsOptions);

        // Assert
        assertNotNull("listAssetResult", listAssetResult);
        assertEquals("listAssetResult.size", 3, listAssetResult.size());

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
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.updateAsset(validButNonexistAssetId, updateAssetOptions);
    }

    @Test
    public void deleteAssetSuccess() throws Exception {
        // Arrange
        String assetName = "deleteAssetSuccess";
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

    // End of Asset tests

    // Start of Locator tests

    private AssetInfo assetInfo;
    private AccessPolicyInfo accessPolicyInfo;
    private AccessPolicyInfo accessPolicyInfoRead;

    private void setupForLocatorTest() throws ServiceException {
        assetInfo = service.createAsset(new CreateAssetOptions().setName(testAssetPrefix + "ForLocatorTest"));
        accessPolicyInfo = service.createAccessPolicy(testPolicyPrefix + "ForLocatorTest", 5);
        accessPolicyInfoRead = service.createAccessPolicy(testPolicyPrefix + "ForLocatorTestRead", 5,
                new CreateAccessPolicyOptions().addPermissions(AccessPolicyPermission.READ));
    }

    @Test
    public void createLocatorSuccess() throws ServiceException {
        // Arrange 
        setupForLocatorTest();
        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        LocatorType locatorType = LocatorType.SAS;

        // Act
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType,
                createLocatorOptions);

        // Assert 
        assertNotNull(locatorInfo);
        assertNotNull(locatorInfo.getId());

    }

    @Ignore("due to media service bug 596240")
    @Test
    public void createLocatorSetExpirationDateTimeSuccess() throws ServiceException {
        // Arrange 
        setupForLocatorTest();

        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        Date expectedExpirationDateTime = new Date();
        expectedExpirationDateTime.setTime(expectedExpirationDateTime.getTime() + 1000);
        createLocatorOptions.setExpirationDateTime(expectedExpirationDateTime);
        LocatorType locatorType = LocatorType.SAS;

        // Act
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType,
                createLocatorOptions);

        // Assert 
        assertNotNull(locatorInfo);
        assertNotNull(locatorInfo.getId());
        assertEquals(expectedExpirationDateTime, locatorInfo.getExpirationDateTime());

    }

    @Test
    public void createLocatorSetStartTimeSuccess() throws ServiceException {
        // Arrange 
        setupForLocatorTest();

        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        Date expectedStartDateTime = new Date();
        expectedStartDateTime.setTime(expectedStartDateTime.getTime() + 1000);
        createLocatorOptions.setStartTime(expectedStartDateTime);
        LocatorType locatorType = LocatorType.SAS;

        // Act
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType,
                createLocatorOptions);

        // Assert 
        assertNotNull(locatorInfo);
        assertNotNull(locatorInfo.getId());
        assertEquals(expectedStartDateTime, locatorInfo.getStartTime());

    }

    @Ignore("due to media service bug 596238")
    @Test
    public void getLocatorSuccess() throws ServiceException {
        // Arrange
        setupForLocatorTest();
        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        LocatorType locatorType = LocatorType.SAS;
        LocatorInfo expectedLocatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(),
                locatorType, createLocatorOptions);

        // Act
        LocatorInfo actualLocatorInfo = service.getLocator(expectedLocatorInfo.getId());

        // Assert
        assertNotNull(actualLocatorInfo);
        assertEquals(expectedLocatorInfo.getAccessPolicyId(), actualLocatorInfo.getAccessPolicyId());
        assertEquals(expectedLocatorInfo.getAssetId(), actualLocatorInfo.getAssetId());
        assertEquals(expectedLocatorInfo.getExpirationDateTime(), actualLocatorInfo.getExpirationDateTime());
        assertEquals(expectedLocatorInfo.getId(), actualLocatorInfo.getId());
        assertEquals(expectedLocatorInfo.getLocatorType(), actualLocatorInfo.getLocatorType());
        assertEquals(expectedLocatorInfo.getPath(), actualLocatorInfo.getPath());
        assertEquals(expectedLocatorInfo.getStartTime(), actualLocatorInfo.getStartTime());

    }

    @Test
    public void listLocatorsSuccess() throws ServiceException {
        // Arrange
        setupForLocatorTest();
        LocatorType locatorType = LocatorType.SAS;
        List<LocatorInfo> expectedLocators = new ArrayList<LocatorInfo>();
        for (int i = 0; i < 2; i++) {
            expectedLocators.add(service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType));
        }

        // Act
        ListLocatorsResult listLocatorsResult = service.listLocators();

        // Assert
        assertNotNull(listLocatorsResult);
        verifyListResultContains("listLocators", expectedLocators, listLocatorsResult.getLocatorInfos(), null);
    }

    @Ignore("due to media service bug 596264")
    @Test
    public void updateLocatorSuccess() throws ServiceException {
        // Arrange
        setupForLocatorTest();

        LocatorType locatorTypeExepcted = LocatorType.Origin;
        LocatorType locatorType = LocatorType.Origin;

        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfoRead.getId(), assetInfo.getId(), locatorType,
                createLocatorOptions);
        Date expirationDateTime = new Date();
        expirationDateTime.setTime(expirationDateTime.getTime() + 1000);

        // Act
        UpdateLocatorOptions updateLocatorOptions = new UpdateLocatorOptions()
                .setExpirationDateTime(expirationDateTime);
        service.updateLocator(locatorInfo.getId(), updateLocatorOptions);

        // Assert
        LocatorInfo locatorInfoActual = service.getLocator(locatorInfo.getId());
        assertEquals(locatorTypeExepcted, locatorInfoActual.getLocatorType());
        assertEquals(expirationDateTime, locatorInfoActual.getExpirationDateTime());

    }

    @Test
    public void deleteLocatorSuccess() throws ServiceException {
        // Arrange
        setupForLocatorTest();
        LocatorType locatorType = LocatorType.SAS;
        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType,
                createLocatorOptions);

        // Act
        service.deleteLocator(locatorInfo.getId());

        // Assert
        LocatorInfo locatorInfoResult = null;
        try {
            locatorInfoResult = service.getLocator(locatorInfo.getId());
        }
        catch (ServiceException e) {
            // swallow
        }
        assertNull(locatorInfoResult);
    }

    @Test(expected = ServiceException.class)
    public void deleteLocatorInvalidIdFailed() throws ServiceException {
        // Arrange 

        // Act
        service.deleteLocator("invalidLocatorId");

        // Assert
        assertTrue(false);
    }

    @Test
    public void listMediaProcessorsSuccess() throws ServiceException {
        // Arrange 

        // Act
        ListMediaProcessorsResult listMediaProcessorsResult = service.listMediaProcessors();

        // Assert
        assertNotNull(listMediaProcessorsResult);
        assertTrue(listMediaProcessorsResult.getMediaProcessorInfos().size() > 0);
    }

    @Test
    public void listMediaProcessorWithOptionSuccess() throws ServiceException {
        // Arrange
        ListMediaProcessorsOptions listMediaProcessorsOptions = new ListMediaProcessorsOptions();

        // Act
        ListMediaProcessorsResult listMediaProcessorsResult = service.listMediaProcessors(listMediaProcessorsOptions);

        // Assert
        assertNotNull(listMediaProcessorsResult);
        assertTrue(listMediaProcessorsResult.getMediaProcessorInfos().size() > 0);
    }

    @Test
    public void createJobWithTaskSuccess() throws ServiceException {
        // Arrange
        AssetInfo assetInfo = service.createAsset();
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<Object> children = assetInfo.getChildren();
        String uri = "";
        for (Object object : children) {

            JAXBElement<IdType> jaxbIdType = null;

            try {
                jaxbIdType = (JAXBElement<IdType>) object;
            }
            catch (ClassCastException e) {

            }
            if (jaxbIdType != null) {
                IdType idtype = null;
                try {
                    idtype = jaxbIdType.getValue();
                }
                catch (ClassCastException e) {

                }
                if (idtype != null) {
                    uri = idtype.getValue();
                }
            }
        }
        URI inputMediaAsset = UriBuilder.fromPath(uri).build();
        createJobOptions.addInputMediaAsset(inputMediaAsset);
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();
        CreateTaskOptions createTaskOptionsInstance = new CreateTaskOptions();
        createTaskOptions.add(createTaskOptionsInstance);

        // Act 
        JobInfo jobInfo = service.createJob(createJobOptions, createTaskOptions);

        // Assert
        assertNotNull(jobInfo);
    }

    @Test(expected = ServiceException.class)
    public void createJobWithoutTaskFailed() throws ServiceException {
        // Arrange
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();

        // Act 
        JobInfo jobInfo = service.createJob(createJobOptions, createTaskOptions);

        // Assert
        assertNotNull(jobInfo);
    }

    @Test
    public void listJobsSuccess() throws ServiceException {
        // Arrange
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();

        JobInfo jobInfoA = service.createJob(createJobOptions, createTaskOptions);
        JobInfo jobInfoB = service.createJob(createJobOptions, createTaskOptions);

        // Act 
        ListJobsResult listJobsResult = service.listJobs();

        // Assert
        assertNotNull(listJobsResult);
    }

    @Test
    public void listTopJobsSuccess() throws ServiceException {
        // Arrange
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();
        JobInfo jobInfoA = service.createJob(createJobOptions, createTaskOptions);
        JobInfo jobInfoB = service.createJob(createJobOptions, createTaskOptions);

        // Act 
        ListJobsResult listJobsResult = service.listJobs();

        // Assert
        assertNotNull(listJobsResult);

    }

    @Test
    public void cancelJobSuccess() throws ServiceException {
        // Arrange 
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();
        JobInfo jobInfo = service.createJob(createJobOptions, createTaskOptions);

        // Act
        service.cancelJob(jobInfo.getId());

        // Assert

    }

    @Test(expected = ServiceException.class)
    public void cancelInvalidJobFailed() throws ServiceException {
        // Arrange 

        // Act 
        service.cancelJob("invalidJobId");

        // Assert
    }

    @Test
    public void listTasksSuccess() throws ServiceException {
        // Arrange
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();
        service.createJob(createJobOptions, createTaskOptions);

        // Act
        ListTasksResult listTasksResult = service.listTasks();

        // Assert
        assertNotNull(listTasksResult);
        assertEquals(1, listTasksResult.getTaskInfos().size());

    }

    @Test
    public void listTasksWithOptionsSuccess() throws ServiceException {
        // Arrange
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();
        service.createJob(createJobOptions, createTaskOptions);
        ListTasksOptions listTasksOptions = new ListTasksOptions();
        listTasksOptions.getQueryParameters().add("$top", "1");

        // Act
        ListTasksResult listTasksResult = service.listTasks(listTasksOptions);

        // Assert
        assertNotNull(listTasksResult);
        assertEquals(1, listTasksResult.getTaskInfos().size());
    }

    @Test
    public void listJobTasksSuccess() throws ServiceException {
        // Arrange
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();
        JobInfo jobInfo = service.createJob(createJobOptions, createTaskOptions);

        // Act 
        ListTasksResult listTasksResult = service.listJobTasks(jobInfo.getId());

        // Assert
        assertNotNull(listTasksResult);
    }

    @Test
    public void listJobTasksInvalidIdFailed() throws ServiceException {
        // Arrange

        // Act 
        ListTasksResult listTasksResult = service.listJobTasks("InvalidJobId");

        // Assert
        assertNull(listTasksResult);

    }

    @Test
    public void listJobTasksSuccessWithOptionsSuccess() throws ServiceException {
        // Arrange
        CreateJobOptions createJobOptions = new CreateJobOptions();
        List<CreateTaskOptions> createTaskOptions = new ArrayList<CreateTaskOptions>();
        JobInfo jobInfo = service.createJob(createJobOptions, createTaskOptions);
        ListTasksOptions listTasksOptions = new ListTasksOptions();

        // Act 
        ListTasksResult listTasksResult = service.listJobTasks(jobInfo.getId());

        // Assert
        assertNotNull(listTasksResult);
    }

}
