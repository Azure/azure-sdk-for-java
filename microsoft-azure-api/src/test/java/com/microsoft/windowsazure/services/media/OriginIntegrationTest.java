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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.Ipv4;
import com.microsoft.windowsazure.services.media.models.OperationInfo;
import com.microsoft.windowsazure.services.media.models.OperationState;
import com.microsoft.windowsazure.services.media.models.Origin;
import com.microsoft.windowsazure.services.media.models.OriginInfo;
import com.microsoft.windowsazure.services.media.models.OriginSettings;
import com.microsoft.windowsazure.services.media.models.OriginState;
import com.microsoft.windowsazure.services.media.models.PlaybackEndPointSettings;
import com.microsoft.windowsazure.services.media.models.SecuritySettings;

public class OriginIntegrationTest extends IntegrationTestBase {

    protected String testOriginPrefix = "testoriginprefix";
    protected String invalidOriginName = "invalidOriginName";

    private void verifyInfosEqual(String message, OriginInfo expected, OriginInfo actual) {
        verifyOriginProperties(message, expected.getName(), expected.getDescription(), expected.getState(), actual);
    }

    private void verifyOriginProperties(String message, String testName, String testDescription,
            OriginState OriginState, OriginInfo actualOrigin) {
        verifyOriginProperties(message, testName, testDescription, OriginState, null, null, actualOrigin);
    }

    private void verifyOriginProperties(String message, String testName, String testDescription,
            OriginState OriginState, Date created, Date lastModified, OriginInfo actualOrigin) {
        assertNotNull(message, actualOrigin);
        assertEquals(message + " Name", testName, actualOrigin.getName());
        assertEquals(message + " Description", testDescription, actualOrigin.getDescription());
        assertEquals(message + " State", OriginState, actualOrigin.getState());

        if (created != null) {
            assertEquals(message + " Created", created, actualOrigin.getCreated());
        }
        if (lastModified != null) {
            assertEquals(message + " LastModified", lastModified, actualOrigin.getLastModified());
        }
    }

    private OriginSettings createOriginSettings() {
        OriginSettings settings = new OriginSettings();
        SecuritySettings securitySettings = new SecuritySettings();
        List<Ipv4> ipV4AllowList = new ArrayList<Ipv4>();
        Ipv4 ipv4 = new Ipv4();
        ipv4.setName("Allow all");
        ipv4.setIp("0.0.0.0/0");
        ipV4AllowList.add(ipv4);
        securitySettings.setIpV4AllowList(ipV4AllowList);
        PlaybackEndPointSettings playback = new PlaybackEndPointSettings();
        settings.setPlayback(playback);
        return settings;
    }

    @Test
    public void createOriginOptionsSuccess() throws Exception {
        // Arrange
        String testName = testOriginPrefix + "createchopt";
        String testDescription = "testDescription";

        OriginState originState = OriginState.Stopped;
        OriginSettings OriginSettings = createOriginSettings();

        // Act
        OriginInfo actualOrigin = service.create(Origin.create().setName(testName).setDescription(testDescription)
                .setSettings(OriginSettings));

        // Assert
        verifyOriginProperties("actualOrigin", testName, testDescription, originState, actualOrigin);
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void OriginCanBeStartedAndStopped() throws Exception {
        // Arrange 
        String testName = testOriginPrefix + "Originsas";
        String testDescription = "testDescription";
        OriginSettings OriginSettings = createOriginSettings();

        // Act
        Future<OperationInfo<OriginInfo>> actualOrigin = service.beginCreate(Origin.create().setName(testName)
                .setDescription(testDescription).setSettings(OriginSettings));

        OriginInfo OriginInfo = actualOrigin.get().getEntity();
        Future<OperationInfo> startFuture = service.beginAction(Origin.start(OriginInfo.getId()));
        OperationInfo startOperationInfo = startFuture.get();

        Future<OperationInfo> stopFuture = service.beginAction(Origin.stop(OriginInfo.getId()));
        OperationInfo stopOperationInfo = stopFuture.get();

        // Assert

    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void scaleOriginSuccess() throws Exception {
        // Arrange
        String testName = testOriginPrefix + "scaleOrigin";
        String testDescription = "testDescription";
        OriginState originState = OriginState.Stopped;
        OriginSettings settings = createOriginSettings();
        int reservedUnits = 2;

        Future<OperationInfo<OriginInfo>> futureOperationInfo = service.beginCreate(Origin.create().setName(testName)
                .setDescription(testDescription).setState(originState).setSettings(settings));
        OriginInfo OriginInfo = futureOperationInfo.get().getEntity();
        // Act
        OriginInfo actualOriginInfo = service.get(Origin.get(OriginInfo.getId()));
        Future<OperationInfo> futureScaleOperationInfo = service.beginAction(Origin.scale(actualOriginInfo.getId(),
                reservedUnits));
        OperationInfo operationInfo = futureScaleOperationInfo.get();

        // Assert
        verifyInfosEqual("actualOrigin", OriginInfo, actualOriginInfo);

    }

    @Test
    public void getOriginSuccess() throws Exception {
        // Arrange
        String testName = testOriginPrefix + "getOrigin";
        String testDescription = "testDescription";
        OriginState originState = OriginState.Stopped;
        OriginSettings settings = createOriginSettings();

        Future<OperationInfo<OriginInfo>> futureOperationInfo = service.beginCreate(Origin.create().setName(testName)
                .setDescription(testDescription).setState(originState).setSettings(settings));
        OriginInfo OriginInfo = futureOperationInfo.get().getEntity();
        // Act
        OriginInfo actualOriginInfo = service.get(Origin.get(OriginInfo.getId()));

        // Assert
        verifyInfosEqual("actualOrigin", OriginInfo, actualOriginInfo);
    }

    @Test
    public void getOriginInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(Origin.get(invalidId));
    }

    @Test
    public void listOriginSuccess() throws ServiceException, InterruptedException, ExecutionException {
        // Arrange
        String[] OriginNames = new String[] { testOriginPrefix + "cha", testOriginPrefix + "chb" };
        List<Future<OperationInfo<OriginInfo>>> expectedFutures = new ArrayList<Future<OperationInfo<OriginInfo>>>();
        List<OriginInfo> expectedOrigins = new ArrayList<OriginInfo>();
        OriginSettings settings = createOriginSettings();

        OriginState originState = OriginState.Stopped;
        for (int i = 0; i < OriginNames.length; i++) {
            String name = OriginNames[i];
            expectedFutures.add(service.beginCreate(Origin.create().setName(name).setState(originState)
                    .setSettings(settings)));
        }

        // Act
        Collection<OriginInfo> listOriginResult = service.list(Origin.list());

        // Assert

        verifyListResultContains("listOrigins", expectedOrigins, listOriginResult, new ComponentDelegate() {
            @Override
            public void verifyEquals(String message, Object expected, Object actual) {
                verifyInfosEqual(message, (OriginInfo) expected, (OriginInfo) actual);
            }
        });
    }

    @Test
    public void canListOriginsWithOptions() throws ServiceException, InterruptedException, ExecutionException {
        String[] OriginNames = new String[] { testOriginPrefix + "Originlista", testOriginPrefix + "Originlistb",
                testOriginPrefix + "Originlistc", testOriginPrefix + "Originlistad" };
        List<OriginInfo> expectedOrigins = new ArrayList<OriginInfo>();
        List<Future<OperationInfo<OriginInfo>>> expectedFutures = new ArrayList<Future<OperationInfo<OriginInfo>>>();
        OriginSettings settings = createOriginSettings();

        for (int i = 0; i < OriginNames.length; i++) {
            String name = OriginNames[i];
            expectedFutures.add(service.beginCreate(Origin.create().setName(name).setSettings(settings)));
        }

        for (Future<OperationInfo<OriginInfo>> futureOperationInfo : expectedFutures) {
            expectedOrigins.add(futureOperationInfo.get().getEntity());
        }

        Collection<OriginInfo> listOriginResult = service.list(Origin.list().setTop(2));

        // Assert

        assertEquals(2, listOriginResult.size());
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void updateOriginAsyncSuccess() throws Exception {
        // Arrange
        String originalTestName = testOriginPrefix + "updatecho";
        OriginState originalOriginState = OriginState.Stopped;

        Future<OperationInfo<OriginInfo>> futureCreateOrigin = service.beginCreate(Origin.create().setName(
                originalTestName));
        OriginInfo originalOrigin = futureCreateOrigin.get().getEntity();

        String updatedDescription = "description";
        SecuritySettings securitySettings = new SecuritySettings();
        Ipv4 ipv4 = new Ipv4().setName("Allow all").setIp("0.0.0.0/0");
        List<Ipv4> ipV4List = new ArrayList<Ipv4>();
        ipV4List.add(ipv4);
        securitySettings.setIpV4AllowList(ipV4List);
        PlaybackEndPointSettings playback = new PlaybackEndPointSettings().setSecurity(securitySettings);

        OriginSettings updatedSettings = new OriginSettings().setPlayback(playback);
        Future<OperationInfo> startFuture = service.beginAction(Origin.start(originalOrigin.getId()));
        OperationInfo startOperationInfo = startFuture.get();

        // Act
        Future<OperationInfo> futureUpdate = service.beginUpdate(Origin.update(originalOrigin.getId()).setSettings(
                updatedSettings));
        OperationInfo operationInfo = futureUpdate.get();

        OriginInfo updatedOrigin = service.get(Origin.get(originalOrigin.getId()));

        Future<OperationInfo> stopFuture = service.beginAction(Origin.stop(originalOrigin.getId()));
        OperationInfo stopOperationInfo = stopFuture.get();

        // Assert
        verifyOriginProperties("updatedOrigin", originalTestName, updatedDescription, originalOriginState,
                updatedOrigin);
    }

    @Test
    public void updateOriginNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testOriginPrefix + "updatechnoch";
        OriginSettings settings = createOriginSettings();
        Future<OperationInfo<OriginInfo>> futureCreateOrigin = service.beginCreate(Origin.create()
                .setName(originalTestName).setSettings(settings));
        OriginInfo originalOrigin = futureCreateOrigin.get().getEntity();

        // Act
        service.update(Origin.update(originalOrigin.getId()));
        OriginInfo updatedOrigin = service.get(Origin.get(originalOrigin.getId()));

        // Assert
        verifyInfosEqual("updatedOrigin", originalOrigin, updatedOrigin);
    }

    @Test
    public void updateOriginFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.beginUpdate(Origin.update(invalidOriginName));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void deleteOriginAsyncSuccess() throws Exception {
        String OriginName = testOriginPrefix + "deletecha";
        OriginSettings settings = createOriginSettings();
        Future<OperationInfo<OriginInfo>> createOriginFuture = service.beginCreate(Origin.create().setName(OriginName)
                .setSettings(settings));
        OperationInfo<OriginInfo> operationInfo = createOriginFuture.get();
        OriginInfo OriginInfo = operationInfo.getEntity();
        List<OriginInfo> listOriginsResult = service.list(Origin.list());
        int OriginCountBaseline = listOriginsResult.size();

        // Act
        Future<OperationInfo> deleteFuture = service.beginDelete(Origin.delete(OriginInfo.getId()));
        OperationInfo deleteOperationInfo = deleteFuture.get();

        // Assert

        listOriginsResult = service.list(Origin.list());
        assertEquals("listOriginsResult.size", OriginCountBaseline - 1, listOriginsResult.size());
        assertEquals(OperationState.Succeeded, deleteOperationInfo.getState());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Origin.get(OriginInfo.getId()));
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void deleteOriginAsyncFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        Future<OperationInfo> future = service.beginDelete(Origin.delete(invalidOriginName));
    }

    @Test
    public void deleteOriginFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(Origin.delete(invalidOriginName));
    }
}
