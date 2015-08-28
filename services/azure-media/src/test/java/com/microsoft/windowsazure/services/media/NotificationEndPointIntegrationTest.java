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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.EndPointType;
import com.microsoft.windowsazure.services.media.models.NotificationEndPoint;
import com.microsoft.windowsazure.services.media.models.NotificationEndPointInfo;

public class NotificationEndPointIntegrationTest extends IntegrationTestBase {
    private final String validButNonexistNotificationEndPointId = "notificationEndPointId";
    private final String testEndPointAddress = "testendpointaddress";

    private void verifyNotificationEndPointInfosEqual(String message,
            NotificationEndPointInfo expected, NotificationEndPointInfo actual) {
        verifyNotificationEndPointProperties(message, expected.getName(),
                expected.getEndPointType(), expected.getEndPointAddress(),
                actual);
    }

    private void verifyNotificationEndPointProperties(String message,
            String name, EndPointType endPointType, String endPointAddress,
            NotificationEndPointInfo notificationEndPointInfo) {
        assertNotNull(message, notificationEndPointInfo);
        assertEquals(message + " Name", name,
                notificationEndPointInfo.getName());
        assertEquals(message + " EndPointType", endPointType,
                notificationEndPointInfo.getEndPointType());
        assertEquals(message + " EndPointAddress", endPointAddress,
                notificationEndPointInfo.getEndPointAddress());
        assertNotNull(message + " Created",
                notificationEndPointInfo.getCreated());
        assertNotNull(message + " Id", notificationEndPointInfo.getId());
    }

    @Test
    public void canCreateNotificationEndPoint() throws Exception {
        String testName = testNotificationEndPointPrefix + "CanCreate";

        NotificationEndPointInfo actualNotificationEndPoint = service
                .create(NotificationEndPoint.create(testName,
                        EndPointType.AzureQueue, testEndPointAddress));

        verifyNotificationEndPointProperties("notification end point ",
                testName, EndPointType.AzureQueue, testEndPointAddress,
                actualNotificationEndPoint);
    }

    @Test
    public void canCreateNotificationEndPointWithReadPermissions()
            throws Exception {
        String testName = testNotificationEndPointPrefix + "CanCreate";

        NotificationEndPointInfo actualNotificationEndPoint = service
                .create(NotificationEndPoint.create(testName,
                        EndPointType.AzureQueue, testEndPointAddress));

        verifyNotificationEndPointProperties("notification end point",
                testName, EndPointType.AzureQueue, testEndPointAddress,
                actualNotificationEndPoint);
    }

    @Test
    public void canGetSingleNotificationEndPointById() throws Exception {
        String expectedName = testNotificationEndPointPrefix + "GetOne";
        NotificationEndPointInfo expectedNotificationEndPointInfo = service
                .create(NotificationEndPoint.create(expectedName,
                        EndPointType.AzureQueue, testEndPointAddress));

        NotificationEndPointInfo actualNotificationEndPointInfo = service
                .get(NotificationEndPoint.get(expectedNotificationEndPointInfo
                        .getId()));

        assertEquals(expectedNotificationEndPointInfo.getId(),
                actualNotificationEndPointInfo.getId());
        verifyNotificationEndPointProperties("notification end point",
                expectedName, EndPointType.AzureQueue, testEndPointAddress,
                actualNotificationEndPointInfo);
    }

    @Test
    public void canGetSingleNotificationEndPointByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(NotificationEndPoint.get(invalidId));
    }

    @Test
    public void cannotGetSingleNotificationEndPointByNonexistId()
            throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(NotificationEndPoint
                .get(validButNonexistNotificationEndPointId));
    }

    @Test
    public void canRetrieveListOfNotificationEndPoints() throws Exception {
        String[] notificationEndPointNames = new String[] {
                testNotificationEndPointPrefix + "ListOne",
                testNotificationEndPointPrefix + "ListTwo" };

        List<NotificationEndPointInfo> expectedNotificationEndPoints = new ArrayList<NotificationEndPointInfo>();
        for (int i = 0; i < notificationEndPointNames.length; i++) {
            NotificationEndPointInfo notificationEndPointInfo = service
                    .create(NotificationEndPoint.create(
                            notificationEndPointNames[i],
                            EndPointType.AzureQueue, testEndPointAddress));
            expectedNotificationEndPoints.add(notificationEndPointInfo);
        }

        List<NotificationEndPointInfo> actualAccessPolicies = service
                .list(NotificationEndPoint.list());

        verifyListResultContains("listNotificationEndPoints",
                expectedNotificationEndPoints, actualAccessPolicies,
                new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected,
                            Object actual) {
                        verifyNotificationEndPointInfosEqual(message,
                                (NotificationEndPointInfo) expected,
                                (NotificationEndPointInfo) actual);
                    }
                });
    }

    @Test
    public void canUseQueryParametersWhenListingNotificationEndPoints()
            throws Exception {
        String[] notificationEndPointNames = new String[] {
                testNotificationEndPointPrefix + "ListThree",
                testNotificationEndPointPrefix + "ListFour",
                testNotificationEndPointPrefix + "ListFive",
                testNotificationEndPointPrefix + "ListSix",
                testNotificationEndPointPrefix + "ListSeven" };

        List<NotificationEndPointInfo> expectedNotificationEndPointInfos = new ArrayList<NotificationEndPointInfo>();
        for (int i = 0; i < notificationEndPointNames.length; i++) {
            NotificationEndPointInfo notificationEndPointInfo = service
                    .create(NotificationEndPoint.create(
                            notificationEndPointNames[i],
                            EndPointType.AzureQueue, testEndPointAddress));
            expectedNotificationEndPointInfos.add(notificationEndPointInfo);
        }

        List<NotificationEndPointInfo> actualNotificationEndPointInfos = service
                .list(NotificationEndPoint.list().setTop(2));

        assertEquals(2, actualNotificationEndPointInfos.size());
    }

    @Test
    public void canDeleteNotificationEndPointById() throws Exception {
        String testNotificationEndPointName = testNotificationEndPointPrefix
                + "ToDelete";
        NotificationEndPointInfo notificationEndPointToBeDeleted = service
                .create(NotificationEndPoint.create(
                        testNotificationEndPointName, EndPointType.AzureQueue,
                        testEndPointAddress));
        List<NotificationEndPointInfo> listNotificationEndPointsResult = service
                .list(NotificationEndPoint.list());
        int notificationEndPointBaseline = listNotificationEndPointsResult.size();

        service.delete(NotificationEndPoint
                .delete(notificationEndPointToBeDeleted.getId()));

        listNotificationEndPointsResult = service.list(NotificationEndPoint.list());
        assertEquals("listNotificationEndPointResult.size", notificationEndPointBaseline - 1,
                listNotificationEndPointsResult.size());

        for (NotificationEndPointInfo policy : service
                .list(NotificationEndPoint.list())) {
            assertFalse(notificationEndPointToBeDeleted.getId().equals(
                    policy.getId()));
        }

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(NotificationEndPoint.get(notificationEndPointToBeDeleted
                .getId()));
    }

    @Test
    public void canDeleteNotificationEndPointByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(NotificationEndPoint.delete(invalidId));
    }

    @Test
    public void cannotDeleteNotificationEndPointByNonexistId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(NotificationEndPoint
                .delete(validButNonexistNotificationEndPointId));
    }

}
