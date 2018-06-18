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

import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.NotificationEndPointType;
import com.microsoft.windowsazure.services.media.models.NotificationEndPoint.Creator;

/**
 * Tests for the methods and factories of the NotificationEndPoint entity.
 */
public class NotificationEndPointEntityTest {
    static final String sampleNotificationEndPointId = "nb:cid:UUID:1151b8bd-9ada-4e7f-9787-8dfa49968eab";
    private final String expectedUri = String.format(
            "NotificationEndPoints('%s')",
            URLEncoder.encode(sampleNotificationEndPointId, "UTF-8"));
    private final String testNotificationEndPoint = "testNotificationEndPoint";
    private final String testQueueName = "testqueue";

    public NotificationEndPointEntityTest() throws Exception {
    }

    @Test
    public void NotificationEndPointCreateReturnsDefaultCreatePayload()
            throws ServiceException {
        NotificationEndPointType payload = (NotificationEndPointType) NotificationEndPoint
                .create(testNotificationEndPoint, EndPointType.AzureQueue,
                        testQueueName).getRequestContents();

        assertNotNull(payload);
        assertNull(payload.getId());
        assertNull(payload.getCreated());
        assertNotNull(payload.getName());
        assertNotNull(payload.getEndPointAddress());
        assertNotNull(payload.getEndPointType());
    }

    @Test
    public void NotificationEndPointCreateCanSetNotificationEndPointName() {
        String name = "NotificationEndPointCreateCanSetNotificationEndPointName";

        NotificationEndPoint.Creator creator = (Creator) NotificationEndPoint
                .create(name, EndPointType.AzureQueue, testQueueName);

        NotificationEndPointType payload = (NotificationEndPointType) creator
                .getRequestContents();

        assertNotNull(payload);
        assertNull(payload.getId());
        assertNull(payload.getCreated());
        assertEquals(name, payload.getName());
    }

    @Test
    public void NotificationEndPointGetReturnsExpectedUri() throws Exception {
        String expectedUri = String.format("NotificationEndPoints('%s')",
                URLEncoder.encode(sampleNotificationEndPointId, "UTF-8"));

        EntityGetOperation<NotificationEndPointInfo> getter = NotificationEndPoint
                .get(sampleNotificationEndPointId);

        assertEquals(expectedUri, getter.getUri());
    }

    @Test
    public void NotificationEndPointListReturnsExpectedUri() {
        EntityListOperation<NotificationEndPointInfo> lister = NotificationEndPoint
                .list();

        assertEquals("NotificationEndPoints", lister.getUri());
        assertNotNull(lister.getQueryParameters());
        assertEquals(0, lister.getQueryParameters().size());
    }

    @Test
    public void NotificationEndPointListCanTakeQueryParameters() {
        EntityListOperation<NotificationEndPointInfo> lister = NotificationEndPoint
                .list().setTop(10).setSkip(2);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

    @Test
    public void NotificationEndPointListCanTakeQueryParametersChained() {
        EntityListOperation<NotificationEndPointInfo> lister = NotificationEndPoint
                .list().setTop(10).setSkip(2).set("filter", "something");

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals("something", lister.getQueryParameters()
                .getFirst("filter"));
        assertEquals(3, lister.getQueryParameters().size());
    }

    @Test
    public void NotificationEndPointUpdateReturnsExpectedUri() throws Exception {
        EntityUpdateOperation updater = NotificationEndPoint
                .update(sampleNotificationEndPointId);
        assertEquals(expectedUri, updater.getUri());
    }

    @Test
    public void NotificationEndPointUpdateCanSetNameAndAltId() throws Exception {

        String expectedName = "newNotificationEndPointName";

        EntityUpdateOperation updater = NotificationEndPoint.update(
                sampleNotificationEndPointId).setName(expectedName);

        NotificationEndPointType payload = (NotificationEndPointType) updater
                .getRequestContents();

        assertEquals(expectedName, payload.getName());
    }

    @Test
    public void NotificationEndPointDeleteReturnsExpectedUri() throws Exception {
        EntityDeleteOperation deleter = NotificationEndPoint
                .delete(sampleNotificationEndPointId);
        assertEquals(expectedUri, deleter.getUri());
    }
}
