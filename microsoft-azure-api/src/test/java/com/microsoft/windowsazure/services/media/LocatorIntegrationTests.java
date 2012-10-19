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
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.CreateLocatorOptions;
import com.microsoft.windowsazure.services.media.models.ListLocatorsOptions;
import com.microsoft.windowsazure.services.media.models.ListLocatorsResult;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.UpdateLocatorOptions;

public class LocatorIntegrationTests extends IntegrationTestBase {

    private static AssetInfo assetInfo;
    private static AccessPolicyInfo accessPolicyInfo;
    private static AccessPolicyInfo accessPolicyInfoRead;

    private Date calculateDefaultExpectedExpDate(AccessPolicyInfo accessPolicy, AssetInfo asset) {
        return new Date(asset.getLastModified().getTime() + (long) accessPolicy.getDurationInMinutes() * 60 * 1000);
    }

    private void verifyLocatorInfosEqual(String message, LocatorInfo expected, LocatorInfo actual) {
        verifyLocatorProperties(message, expected.getAccessPolicyId(), expected.getAssetId(),
                expected.getLocatorType(), expected.getStartTime(), expected.getExpirationDateTime(), expected.getId(),
                expected.getPath(), actual);
    }

    private void verifyLocatorProperties(String message, String accessPolicyId, String assetId,
            LocatorType locatorType, Date startTime, Date expirationDateTime, LocatorInfo actualLocator) {
        verifyLocatorProperties(message, accessPolicyId, assetId, locatorType, startTime, expirationDateTime, null,
                null, actualLocator);
    }

    private void verifyLocatorProperties(String message, String accessPolicyId, String assetId,
            LocatorType locatorType, Date startTime, Date expirationDateTime, String id, String path,
            LocatorInfo actualLocator) {
        assertNotNull(message, actualLocator);
        assertEquals(message + " accessPolicyId", accessPolicyId, actualLocator.getAccessPolicyId());
        assertEquals(message + " assetId", assetId, actualLocator.getAssetId());
        assertEquals(message + " locatorType", locatorType, actualLocator.getLocatorType());

        assertDateApproxEquals(message + " startTime", startTime, actualLocator.getStartTime());
        assertDateApproxEquals(message + " expirationDateTime", expirationDateTime,
                actualLocator.getExpirationDateTime());

        if (id == null) {
            assertNotNull(message + " Id", actualLocator.getId());
        }
        else {
            assertEquals(message + " Id", id, actualLocator.getId());
        }
        if (path == null) {
            assertNotNull(message + " path", actualLocator.getPath());
        }
        else {
            assertEquals(message + " path", path, actualLocator.getPath());
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        IntegrationTestBase.setup();
        accessPolicyInfo = service.createAccessPolicy(testPolicyPrefix + "ForLocatorTest", 5,
                EnumSet.of(AccessPolicyPermission.WRITE));
        accessPolicyInfoRead = service.createAccessPolicy(testPolicyPrefix + "ForLocatorTestRead", 5,
                EnumSet.of(AccessPolicyPermission.READ));
    }

    @Before
    public void instanceSetup() throws Exception {
        assetInfo = service.createAsset(new CreateAssetOptions().setName(testAssetPrefix + "ForLocatorTest"));
    }

    @Test
    public void createLocatorSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.Origin;
        Date expectedExpirationDateTime = calculateDefaultExpectedExpDate(accessPolicyInfo, assetInfo);

        // Act
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType);

        // Assert
        verifyLocatorProperties("locatorInfo", accessPolicyInfo.getId(), assetInfo.getId(), locatorType, null,
                expectedExpirationDateTime, locatorInfo);
    }

    @Ignore("due to media service bug 596240")
    @Test
    public void createLocatorOptionsSetExpirationDateTimeSuccess() throws ServiceException {
        // Arrange
        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        Date expectedExpirationDateTime = new Date();
        expectedExpirationDateTime.setTime(expectedExpirationDateTime.getTime() + 10000);
        createLocatorOptions.setExpirationDateTime(expectedExpirationDateTime);
        LocatorType locatorType = LocatorType.SAS;

        // Act
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType,
                createLocatorOptions);

        // Assert 
        verifyLocatorProperties("locatorInfo", accessPolicyInfo.getId(), assetInfo.getId(), locatorType, null,
                expectedExpirationDateTime, locatorInfo);
    }

    @Test
    public void createLocatorOptionsSetStartTimeSuccess() throws ServiceException {
        // Arrange
        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        Date expectedStartDateTime = new Date();
        expectedStartDateTime.setTime(expectedStartDateTime.getTime() + 10000);
        createLocatorOptions.setStartTime(expectedStartDateTime);
        LocatorType locatorType = LocatorType.SAS;

        // Act
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType,
                createLocatorOptions);

        // Assert 
        verifyLocatorProperties("locatorInfo", accessPolicyInfo.getId(), assetInfo.getId(), locatorType,
                expectedStartDateTime, null, locatorInfo);
    }

    @Ignore("due to media service bug 596238")
    @Test
    public void getLocatorSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;
        Date expectedStartDateTime = new Date();
        expectedStartDateTime.setTime(expectedStartDateTime.getTime() + 10000);
        Date expectedExpirationDateTime = new Date(expectedStartDateTime.getTime() + 10000);

        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        createLocatorOptions.setStartTime(expectedStartDateTime);
        createLocatorOptions.setExpirationDateTime(expectedExpirationDateTime);
        LocatorInfo expectedLocatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(),
                locatorType, createLocatorOptions);

        // Act
        LocatorInfo actualLocatorInfo = service.getLocator(expectedLocatorInfo.getId());

        // Assert
        verifyLocatorInfosEqual("actualLocatorInfo", expectedLocatorInfo, actualLocatorInfo);
    }

    @Test
    public void getLocatorInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.getAsset(invalidId);
    }

    @Test
    public void getLocatorNonexistId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.getAsset(validButNonexistLocatorId);
    }

    @Test
    public void listLocatorsSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;
        List<LocatorInfo> expectedLocators = new ArrayList<LocatorInfo>();
        for (int i = 0; i < 2; i++) {
            expectedLocators.add(service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType));
        }

        // Act
        ListLocatorsResult listLocatorsResult = service.listLocators();

        // Assert
        assertNotNull(listLocatorsResult);
        verifyListResultContains("listLocatorsResult", expectedLocators, listLocatorsResult.getLocatorInfos(),
                new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected, Object actual) {
                        verifyLocatorInfosEqual(message, (LocatorInfo) expected, (LocatorInfo) actual);
                    }
                });
    }

    @Test
    public void listLocatorsQuerySuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;
        List<LocatorInfo> expectedLocators = new ArrayList<LocatorInfo>();
        for (int i = 0; i < 2; i++) {
            expectedLocators.add(service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType));
        }
        ListLocatorsOptions options = new ListLocatorsOptions();
        options.getQueryParameters().add("$query", "id eq " + expectedLocators.get(0).getId());

        // Act
        ListLocatorsResult listLocatorsResult = service.listLocators(options);

        // Assert
        assertNotNull(listLocatorsResult);
        verifyListResultContains("listLocatorsResult", expectedLocators, listLocatorsResult.getLocatorInfos(),
                new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected, Object actual) {
                        verifyLocatorInfosEqual(message, (LocatorInfo) expected, (LocatorInfo) actual);
                    }
                });
    }

    @Ignore("due to media service bug 596264")
    @Test
    public void updateLocatorSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.Origin;
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfoRead.getId(), assetInfo.getId(), locatorType);

        Date expirationDateTime = new Date();
        expirationDateTime.setTime(expirationDateTime.getTime() + 1000);
        Date startTime = new Date();
        startTime.setTime(startTime.getTime() - 1000);
        UpdateLocatorOptions updateLocatorOptions = new UpdateLocatorOptions()
                .setExpirationDateTime(expirationDateTime).setStartTime(startTime);

        // Act
        service.updateLocator(locatorInfo.getId(), updateLocatorOptions);
        LocatorInfo updatedLocatorInfo = service.getLocator(locatorInfo.getId());

        // Assert
        verifyLocatorProperties("updatedLocatorInfo", locatorInfo.getAccessPolicyId(), locatorInfo.getAssetId(),
                locatorInfo.getLocatorType(), startTime, expirationDateTime, locatorInfo.getId(),
                locatorInfo.getPath(), updatedLocatorInfo);
    }

    @Test
    public void updateLocatorNoChangesSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.Origin;
        Date expirationDateTime = new Date();
        expirationDateTime.setTime(expirationDateTime.getTime() + 1000);
        Date startTime = new Date();
        startTime.setTime(startTime.getTime() - 1000);
        CreateLocatorOptions options = new CreateLocatorOptions().setExpirationDateTime(expirationDateTime)
                .setStartTime(startTime);

        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfoRead.getId(), assetInfo.getId(), locatorType,
                options);

        // Act
        service.updateLocator(locatorInfo.getId(), null);
        LocatorInfo updatedLocatorInfo = service.getLocator(locatorInfo.getId());

        // Assert
        verifyLocatorInfosEqual("updatedLocatorInfo", locatorInfo, updatedLocatorInfo);
    }

    @Test
    public void deleteLocatorSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;
        CreateLocatorOptions createLocatorOptions = new CreateLocatorOptions();
        LocatorInfo locatorInfo = service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), locatorType,
                createLocatorOptions);
        ListLocatorsResult listLocatorsResult = service.listLocators();
        int assetCountBaseline = listLocatorsResult.getLocatorInfos().size();

        // Act
        service.deleteLocator(locatorInfo.getId());

        // Assert
        listLocatorsResult = service.listLocators();
        assertEquals("listLocatorsResult.size", assetCountBaseline - 1, listLocatorsResult.getLocatorInfos().size());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.getLocator(locatorInfo.getId());
    }

    @Test
    public void deleteLocatorInvalidIdFailed() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));
        service.deleteLocator(invalidId);
    }
}
