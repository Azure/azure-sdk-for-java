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

    private AssetInfo assetInfo;
    private AccessPolicyInfo accessPolicyInfo;
    private AccessPolicyInfo accessPolicyInfoRead;

    private void setupForLocatorTest() throws ServiceException {
        assetInfo = service.createAsset(new CreateAssetOptions().setName(testAssetPrefix + "ForLocatorTest"));
        accessPolicyInfo = service.createAccessPolicy(testPolicyPrefix + "ForLocatorTest", 5,
                EnumSet.of(AccessPolicyPermission.WRITE));
        accessPolicyInfoRead = service.createAccessPolicy(testPolicyPrefix + "ForLocatorTestRead", 5,
                EnumSet.of(AccessPolicyPermission.READ));
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

    @Test
    public void canListLocatorsWithOptions() throws ServiceException {
        setupForLocatorTest();

        for (int i = 0; i < 5; i++) {
            service.createLocator(accessPolicyInfo.getId(), assetInfo.getId(), LocatorType.SAS);
        }

        ListLocatorsOptions options = new ListLocatorsOptions();
        options.getQueryParameters().add("$top", "2");

        ListLocatorsResult result = service.listLocators(options);

        assertEquals(2, result.getLocatorInfos().size());
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
}
