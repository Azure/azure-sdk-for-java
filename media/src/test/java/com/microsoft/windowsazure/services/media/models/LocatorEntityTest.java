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

package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URLEncoder;
import java.util.Date;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.LocatorRestType;

/**
 * Tests for the Locator entity
 * 
 */
public class LocatorEntityTest {
    private final String exampleAssetId = "nb:cid:UUID:61a5ebbe-d5e0-49a5-b28c-e9535321b6cd";
    private final String exampleAccessPolicyId = "nb:pid:UUID:c82307be-1a81-4554-ba7d-cf6dfa735a5a";
    private final String exampleLocatorId = "nb:lid:UUID:f282b0a1-fb21-4b83-87d6-d4c96d77aef9";
    private final String expectedLocatorUri = String.format("Locators('%s')",
            URLEncoder.encode(exampleLocatorId, "UTF-8"));

    public LocatorEntityTest() throws Exception {

    }

    @Test
    public void createLocatorHasCorrectUrl() throws Exception {
        EntityCreateOperation<LocatorInfo> creator = Locator.create(
                exampleAccessPolicyId, exampleAssetId, LocatorType.SAS);

        assertEquals("Locators", creator.getUri());
    }

    @Test
    public void createLocatorHasCorrectPayload() throws Exception {
        LocatorRestType locatorType = (LocatorRestType) Locator.create(
                exampleAccessPolicyId, exampleAssetId, LocatorType.SAS)
                .getRequestContents();

        assertEquals(exampleAssetId, locatorType.getAssetId());
        assertEquals(exampleAccessPolicyId, locatorType.getAccessPolicyId());
        assertEquals(LocatorType.SAS.getCode(), locatorType.getType()
                .intValue());
        assertNull(locatorType.getStartTime());
        assertNull(locatorType.getExpirationDateTime());
    }

    @Test
    public void createLocatorCanSetStartTime() throws Exception {
        Date now = new Date();

        EntityCreateOperation<LocatorInfo> creator = Locator.create(
                exampleAccessPolicyId, exampleAssetId, LocatorType.SAS)
                .setStartDateTime(now);

        LocatorRestType locatorType = (LocatorRestType) creator
                .getRequestContents();

        assertEquals(exampleAssetId, locatorType.getAssetId());
        assertEquals(exampleAccessPolicyId, locatorType.getAccessPolicyId());
        assertEquals(LocatorType.SAS.getCode(), locatorType.getType()
                .intValue());
        assertEquals(now, locatorType.getStartTime());
    }

    @Test
    public void createLocatorCanSetPath() throws Exception {

        String expectedPath = "testExpectedPath";

        EntityCreateOperation<LocatorInfo> creator = Locator.create(
                exampleAccessPolicyId, exampleAssetId, LocatorType.SAS)
                .setPath(expectedPath);

        LocatorRestType locatorType = (LocatorRestType) creator
                .getRequestContents();

        assertEquals(expectedPath, locatorType.getPath());

    }

    @Test
    public void createLocatorCanSetBaseUri() throws Exception {

        String expectedBaseUri = "testExpectedBaseUri";

        EntityCreateOperation<LocatorInfo> creator = Locator.create(
                exampleAccessPolicyId, exampleAssetId, LocatorType.SAS)
                .setBaseUri(expectedBaseUri);

        LocatorRestType locatorType = (LocatorRestType) creator
                .getRequestContents();

        assertEquals(expectedBaseUri, locatorType.getBaseUri());

    }

    @Test
    public void createLocatorCanSetContentAccessComponent() throws Exception {

        String expectedContentAccessComponent = "testExpectedContentAccessComponent";

        EntityCreateOperation<LocatorInfo> creator = Locator.create(
                exampleAccessPolicyId, exampleAssetId, LocatorType.SAS)
                .setContentAccessComponent(expectedContentAccessComponent);

        LocatorRestType locatorType = (LocatorRestType) creator
                .getRequestContents();

        assertEquals(expectedContentAccessComponent,
                locatorType.getContentAccessComponent());

    }

    @Test
    public void getLocatorGivesExpectedUri() throws Exception {
        assertEquals(expectedLocatorUri, Locator.get(exampleLocatorId).getUri());
    }

    @Test
    public void listLocatorReturnsExpectedUri() {
        EntityListOperation<LocatorInfo> lister = Locator.list();

        assertEquals("Locators", lister.getUri());
        assertNotNull(lister.getQueryParameters());
        assertEquals(0, lister.getQueryParameters().size());
    }

    @Test
    public void listLocatorCanTakeQueryParameters() {

        EntityListOperation<LocatorInfo> lister = Locator.list().setTop(10)
                .setSkip(2);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

    @Test
    public void locatorUpdateReturnsExpectedUri() throws Exception {
        EntityUpdateOperation updater = Locator.update(exampleLocatorId);
        assertEquals(expectedLocatorUri, updater.getUri());
    }

    @Test
    public void locatorUpdateCanSetStarTime() throws Exception {
        Date now = new Date();

        Date tenMinutesAgo = new Date(now.getTime() - 10 * 60 * 1000);

        EntityUpdateOperation updater = Locator.update(exampleLocatorId)
                .setStartDateTime(tenMinutesAgo);

        LocatorRestType payload = (LocatorRestType) updater
                .getRequestContents();

        assertEquals(tenMinutesAgo, payload.getStartTime());
    }

    @Test
    public void locatorDeleteReturnsExpectedUri() throws Exception {
        EntityDeleteOperation deleter = Locator.delete(exampleLocatorId);

        assertEquals(expectedLocatorUri, deleter.getUri());
    }
}
