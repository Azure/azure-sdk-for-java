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

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;

public class LocatorIntegrationTests extends IntegrationTestBase {

    private static AssetInfo assetInfo;
    private static AccessPolicyInfo accessPolicyInfo;
    private static AccessPolicyInfo accessPolicyInfoRead;
    private static int minuteInMS = 60 * 1000;
    private static int tenMinutesInMS = 10 * 60 * 1000;

    private void verifyLocatorInfosEqual(String message, LocatorInfo expected,
            LocatorInfo actual) {
        verifyLocatorProperties(message, expected.getAccessPolicyId(),
                expected.getAssetId(), expected.getLocatorType(),
                expected.getStartTime(), expected.getId(), expected.getPath(),
                actual);
    }

    private void verifyLocatorProperties(String message, String accessPolicyId,
            String assetId, LocatorType locatorType, Date startTime,
            LocatorInfo actualLocator) {
        verifyLocatorProperties(message, accessPolicyId, assetId, locatorType,
                startTime, null, null, actualLocator);
    }

    private void verifyLocatorProperties(String message, String accessPolicyId,
            String assetId, LocatorType locatorType, Date startTime, String id,
            String path, LocatorInfo actualLocator) {
        assertNotNull(message, actualLocator);
        assertEquals(message + " accessPolicyId", accessPolicyId,
                actualLocator.getAccessPolicyId());
        assertEquals(message + " assetId", assetId, actualLocator.getAssetId());
        assertEquals(message + " locatorType", locatorType,
                actualLocator.getLocatorType());

        assertDateApproxEquals(message + " startTime", startTime,
                actualLocator.getStartTime());

        if (id == null) {
            assertNotNull(message + " Id", actualLocator.getId());
        } else {
            assertEquals(message + " Id", id, actualLocator.getId());
        }
        if (path == null) {
            assertNotNull(message + " path", actualLocator.getPath());
        } else {
            assertEquals(message + " path", path, actualLocator.getPath());
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        IntegrationTestBase.setup();
        accessPolicyInfo = service
                .create(AccessPolicy.create(
                        testPolicyPrefix + "ForLocatorTest", 5,
                        EnumSet.of(AccessPolicyPermission.WRITE)));
        accessPolicyInfoRead = service.create(AccessPolicy.create(
                testPolicyPrefix + "ForLocatorTestRead", 15,
                EnumSet.of(AccessPolicyPermission.READ)));
    }

    @Before
    public void instanceSetup() throws Exception {
        assetInfo = service.create(Asset.create().setName(
                testAssetPrefix + "ForLocatorTest"));
    }

    @Test
    public void createLocatorSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;

        // Act
        LocatorInfo locatorInfo = service.create(Locator.create(
                accessPolicyInfoRead.getId(), assetInfo.getId(), locatorType));

        // Assert
        verifyLocatorProperties("locatorInfo", accessPolicyInfoRead.getId(),
                assetInfo.getId(), locatorType, null, locatorInfo);
    }

    @Test
    public void createLocatorWithSpecifiedIdSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;

        // Act
        LocatorInfo locatorInfo = service.create(Locator.create(
                accessPolicyInfoRead.getId(), assetInfo.getId(), locatorType)
                .setId(String.format("nb:lid:UUID:%s", UUID.randomUUID()
                        .toString())));

        // Assert
        verifyLocatorProperties("locatorInfo", accessPolicyInfoRead.getId(),
                assetInfo.getId(), locatorType, null, locatorInfo);
    }

    @Test
    public void createLocatorOptionsSetStartTimeSuccess()
            throws ServiceException {
        // Arrange
        Date expectedStartDateTime = new Date();
        expectedStartDateTime.setTime(expectedStartDateTime.getTime()
                + tenMinutesInMS);
        LocatorType locatorType = LocatorType.SAS;

        // Act
        LocatorInfo locatorInfo = service.create(Locator.create(
                accessPolicyInfo.getId(), assetInfo.getId(), locatorType)
                .setStartDateTime(expectedStartDateTime));

        // Assert
        verifyLocatorProperties("locatorInfo", accessPolicyInfo.getId(),
                assetInfo.getId(), locatorType, expectedStartDateTime,
                locatorInfo);
    }

    @Test
    public void getLocatorSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;
        Date expectedStartDateTime = new Date();
        expectedStartDateTime.setTime(expectedStartDateTime.getTime()
                + tenMinutesInMS);

        LocatorInfo expectedLocatorInfo = service.create(Locator.create(
                accessPolicyInfo.getId(), assetInfo.getId(), locatorType)
                .setStartDateTime(expectedStartDateTime));

        // Act
        LocatorInfo actualLocatorInfo = service.get(Locator
                .get(expectedLocatorInfo.getId()));

        // Assert
        verifyLocatorInfosEqual("actualLocatorInfo", expectedLocatorInfo,
                actualLocatorInfo);
    }

    @Test
    public void getLocatorInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(Locator.get(invalidId));
    }

    @Test
    public void getLocatorNonexistId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Locator.get(validButNonexistLocatorId));
    }

    @Test
    public void listLocatorsSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;
        List<LocatorInfo> expectedLocators = new ArrayList<LocatorInfo>();
        for (int i = 0; i < 2; i++) {
            expectedLocators.add(service.create(Locator.create(
                    accessPolicyInfo.getId(), assetInfo.getId(), locatorType)));
        }

        // Act
        ListResult<LocatorInfo> listLocatorsResult = service.list(Locator
                .list());

        // Assert
        assertNotNull(listLocatorsResult);
        verifyListResultContains("listLocatorsResult", expectedLocators,
                listLocatorsResult, new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected,
                            Object actual) {
                        verifyLocatorInfosEqual(message,
                                (LocatorInfo) expected, (LocatorInfo) actual);
                    }
                });
    }

    @Test
    public void listLocatorsWithOptions() throws ServiceException {
        List<LocatorInfo> expectedLocators = new ArrayList<LocatorInfo>();
        for (int i = 0; i < 5; i++) {
            expectedLocators.add(service.create(Locator.create(
                    accessPolicyInfo.getId(), assetInfo.getId(),
                    LocatorType.SAS)));
        }

        ListResult<LocatorInfo> result = service.list(Locator
                .list()
                .setTop(3)
                .set("$filter",
                        "(Id eq '" + expectedLocators.get(1).getId()
                                + "') or (" + "Id eq '"
                                + expectedLocators.get(3).getId() + "')"));

        assertEquals(2, result.size());
    }

    @Test
    public void updateLocatorSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.OnDemandOrigin;
        LocatorInfo locatorInfo = service.create(Locator.create(
                accessPolicyInfoRead.getId(), assetInfo.getId(), locatorType));

        Date startTime = new Date();
        startTime.setTime(startTime.getTime() - tenMinutesInMS);

        // Act
        service.update(Locator.update(locatorInfo.getId()).setStartDateTime(
                startTime));
        LocatorInfo updatedLocatorInfo = service.get(Locator.get(locatorInfo
                .getId()));

        // Assert
        Date expectedExpiration = new Date();
        expectedExpiration.setTime(startTime.getTime()
                + (long) accessPolicyInfoRead.getDurationInMinutes()
                * minuteInMS);

        verifyLocatorProperties("updatedLocatorInfo",
                locatorInfo.getAccessPolicyId(), locatorInfo.getAssetId(),
                locatorInfo.getLocatorType(), startTime, locatorInfo.getId(),
                locatorInfo.getPath(), updatedLocatorInfo);
    }

    @Test
    public void updateLocatorNoChangesSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.OnDemandOrigin;
        Date expirationDateTime = new Date();
        expirationDateTime.setTime(expirationDateTime.getTime()
                + tenMinutesInMS);
        Date startTime = new Date();
        startTime.setTime(startTime.getTime() - tenMinutesInMS);

        LocatorInfo locatorInfo = service.create(Locator.create(
                accessPolicyInfoRead.getId(), assetInfo.getId(), locatorType)
                .setStartDateTime(startTime));

        // Act
        service.update(Locator.update(locatorInfo.getId()));
        LocatorInfo updatedLocatorInfo = service.get(Locator.get(locatorInfo
                .getId()));

        // Assert
        verifyLocatorInfosEqual("updatedLocatorInfo", locatorInfo,
                updatedLocatorInfo);
    }

    @Test
    public void deleteLocatorSuccess() throws ServiceException {
        // Arrange
        LocatorType locatorType = LocatorType.SAS;
        LocatorInfo locatorInfo = service.create(Locator.create(
                accessPolicyInfo.getId(), assetInfo.getId(), locatorType));
        ListResult<LocatorInfo> listLocatorsResult = service.list(Locator
                .list());
        int assetCountBaseline = listLocatorsResult.size();

        // Act
        service.delete(Locator.delete(locatorInfo.getId()));

        // Assert
        listLocatorsResult = service.list(Locator.list());
        assertEquals("listLocatorsResult.size", assetCountBaseline - 1,
                listLocatorsResult.size());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Locator.get(locatorInfo.getId()));
    }

    @Test
    public void deleteLocatorInvalidIdFailed() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(Locator.delete(invalidId));
    }

    @Test
    public void canGetLocatorBackFromAsset() throws Exception {
        LocatorInfo locator = service.create(Locator.create(
                accessPolicyInfo.getId(), assetInfo.getId(), LocatorType.SAS));

        ListResult<LocatorInfo> locators = service.list(Locator.list(assetInfo
                .getLocatorsLink()));

        assertEquals(1, locators.size());
        assertEquals(locator.getId(), locators.get(0).getId());

    }

    @Test
    public void canGetAssetFromLocator() throws Exception {
        LocatorInfo locator = service.create(Locator.create(
                accessPolicyInfo.getId(), assetInfo.getId(), LocatorType.SAS));

        AssetInfo asset = service.get(Asset.get(locator.getAssetLink()));

        assertEquals(assetInfo.getId(), asset.getId());
    }

    @Test
    public void canGetAccessPolicyFromLocator() throws Exception {
        LocatorInfo locator = service.create(Locator.create(
                accessPolicyInfo.getId(), assetInfo.getId(), LocatorType.SAS));

        AccessPolicyInfo accessPolicy = service.get(AccessPolicy.get(locator
                .getAccessPolicyLink()));

        assertEquals(accessPolicyInfo.getId(), accessPolicy.getId());

    }
}
