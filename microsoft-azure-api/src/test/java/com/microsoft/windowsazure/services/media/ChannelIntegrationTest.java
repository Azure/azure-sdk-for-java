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

import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.Channel;
import com.microsoft.windowsazure.services.media.models.ChannelInfo;
import com.microsoft.windowsazure.services.media.models.ChannelSettings;
import com.microsoft.windowsazure.services.media.models.ChannelSize;
import com.microsoft.windowsazure.services.media.models.ChannelState;
import com.microsoft.windowsazure.services.media.models.IngestEndpointSettings;
import com.microsoft.windowsazure.services.media.models.Ipv4;
import com.microsoft.windowsazure.services.media.models.OperationInfo;
import com.microsoft.windowsazure.services.media.models.OperationState;
import com.microsoft.windowsazure.services.media.models.PreviewEndPointSettings;
import com.microsoft.windowsazure.services.media.models.SecuritySettings;

public class ChannelIntegrationTest extends IntegrationTestBase {

    protected String testChannelPrefix = "testchannelprefix";
    protected String invalidChannelName = "invalidChannelName";

    private void verifyInfosEqual(String message, ChannelInfo expected, ChannelInfo actual) {
        verifyChannelProperties(message, expected.getName(), expected.getDescription(), expected.getState(), actual);
    }

    private void verifyChannelProperties(String message, String testName, String testDescription,
            ChannelState channelState, ChannelInfo actualChannel) {
        verifyChannelProperties(message, testName, testDescription, channelState, null, null, actualChannel);
    }

    private void verifyChannelProperties(String message, String testName, String testDescription,
            ChannelState channelState, Date created, Date lastModified, ChannelInfo actualChannel) {
        assertNotNull(message, actualChannel);
        assertEquals(message + " Name", testName, actualChannel.getName());
        assertEquals(message + " Description", testDescription, actualChannel.getDescription());
        assertEquals(message + " State", channelState, actualChannel.getState());

        if (created != null) {
            assertEquals(message + " Created", created, actualChannel.getCreated());
        }
        if (lastModified != null) {
            assertEquals(message + " LastModified", lastModified, actualChannel.getLastModified());
        }
    }

    private ChannelSettings createChannelSettings() {
        ChannelSettings settings = new ChannelSettings();
        IngestEndpointSettings ingestEndPointSettings = new IngestEndpointSettings();
        SecuritySettings securitySettings = new SecuritySettings();
        List<Ipv4> ipV4AllowList = new ArrayList<Ipv4>();
        Ipv4 ipv4 = new Ipv4();
        ipv4.setName("Allow all");
        ipv4.setIp("0.0.0.0/0");
        ipV4AllowList.add(ipv4);
        securitySettings.setIpV4AllowList(ipV4AllowList);
        ingestEndPointSettings.setSecurity(securitySettings);
        settings.setIngest(ingestEndPointSettings);
        return settings;
    }

    @Test
    public void createChannelOptionsSuccess() throws Exception {
        // Arrange
        String testName = testChannelPrefix + "createchopt";
        String testDescription = "testDescription";

        ChannelState channelState = ChannelState.Stopped;
        ChannelSettings channelSettings = createChannelSettings();

        // Act
        ChannelInfo actualChannel = service.create(Channel.create().setName(testName).setDescription(testDescription)
                .setSize(ChannelSize.Large).setSettings(channelSettings));

        // Assert
        verifyChannelProperties("actualChannel", testName, testDescription, channelState, actualChannel);
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void channelCanBeStartedAndStopped() throws Exception {
        // Arrange 
        String testName = testChannelPrefix + "channelsas";
        String testDescription = "testDescription";
        ChannelSettings channelSettings = createChannelSettings();

        // Act
        Future<OperationInfo<ChannelInfo>> actualChannel = service.beginCreate(Channel.create().setName(testName)
                .setDescription(testDescription).setSize(ChannelSize.Large).setSettings(channelSettings));

        ChannelInfo channelInfo = actualChannel.get().getEntity();
        Future<OperationInfo> startFuture = service.beginAction(Channel.start(channelInfo.getId()));
        OperationInfo startOperationInfo = startFuture.get();

        Future<OperationInfo> stopFuture = service.beginAction(Channel.stop(channelInfo.getId()));
        OperationInfo stopOperationInfo = stopFuture.get();

        // Assert

    }

    @Test
    public void getChannelSuccess() throws Exception {
        // Arrange
        String testName = testChannelPrefix + "getchannel";
        String testDescription = "testDescription";
        ChannelState channelState = ChannelState.Stopped;
        ChannelSize channelSize = ChannelSize.Large;
        ChannelSettings settings = createChannelSettings();

        Future<OperationInfo<ChannelInfo>> futureOperationInfo = service.beginCreate(Channel.create().setName(testName)
                .setDescription(testDescription).setState(channelState).setSize(channelSize).setSettings(settings));
        ChannelInfo channelInfo = futureOperationInfo.get().getEntity();
        // Act
        ChannelInfo actualChannelInfo = service.get(Channel.get(channelInfo.getId()));

        // Assert
        verifyInfosEqual("actualChannel", channelInfo, actualChannelInfo);
    }

    @Test
    public void getChannelInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(Channel.get(invalidId));
    }

    @Test
    public void listChannelSuccess() throws ServiceException, InterruptedException, ExecutionException {
        // Arrange
        String[] ChannelNames = new String[] { testChannelPrefix + "cha", testChannelPrefix + "chb" };
        List<Future<OperationInfo<ChannelInfo>>> expectedFutures = new ArrayList<Future<OperationInfo<ChannelInfo>>>();
        List<ChannelInfo> expectedChannels = new ArrayList<ChannelInfo>();
        ChannelSettings settings = createChannelSettings();

        ChannelState channelState = ChannelState.Stopped;
        for (int i = 0; i < ChannelNames.length; i++) {
            String name = ChannelNames[i];
            expectedFutures.add(service.beginCreate(Channel.create().setName(name).setState(channelState)
                    .setSize(ChannelSize.Large).setSettings(settings)));
        }

        // Act
        Collection<ChannelInfo> listChannelResult = service.list(Channel.list());

        // Assert

        verifyListResultContains("listChannels", expectedChannels, listChannelResult, new ComponentDelegate() {
            @Override
            public void verifyEquals(String message, Object expected, Object actual) {
                verifyInfosEqual(message, (ChannelInfo) expected, (ChannelInfo) actual);
            }
        });
    }

    @Test
    public void canListChannelsWithOptions() throws ServiceException, InterruptedException, ExecutionException {
        String[] ChannelNames = new String[] { testChannelPrefix + "channellista", testChannelPrefix + "channellistb",
                testChannelPrefix + "channellistc", testChannelPrefix + "channellistad" };
        List<ChannelInfo> expectedChannels = new ArrayList<ChannelInfo>();
        List<Future<OperationInfo<ChannelInfo>>> expectedFutures = new ArrayList<Future<OperationInfo<ChannelInfo>>>();
        ChannelSettings settings = createChannelSettings();

        for (int i = 0; i < ChannelNames.length; i++) {
            String name = ChannelNames[i];
            expectedFutures.add(service.beginCreate(Channel.create().setName(name).setSize(ChannelSize.Large)
                    .setSettings(settings)));
        }

        for (Future<OperationInfo<ChannelInfo>> futureOperationInfo : expectedFutures) {
            expectedChannels.add(futureOperationInfo.get().getEntity());
        }

        Collection<ChannelInfo> listChannelResult = service.list(Channel.list().setTop(2));

        // Assert

        assertEquals(2, listChannelResult.size());
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Ignore("Due to issue 746")
    @Test
    public void updateChannelAsyncSuccess() throws Exception {
        // Arrange
        String originalTestName = testChannelPrefix + "updatecho";
        ChannelState originalChannelState = ChannelState.Stopped;

        Future<OperationInfo<ChannelInfo>> futureCreateChannel = service.beginCreate(Channel.create()
                .setName(originalTestName).setSize(ChannelSize.Large));
        ChannelInfo originalChannel = futureCreateChannel.get().getEntity();

        String updatedDescription = "description";
        SecuritySettings securitySettings = new SecuritySettings();
        Ipv4 ipv4 = new Ipv4().setName("Allow all").setIp("0.0.0.0/0");
        List<Ipv4> ipV4List = new ArrayList<Ipv4>();
        ipV4List.add(ipv4);
        securitySettings.setIpV4AllowList(ipV4List);
        IngestEndpointSettings ingest = new IngestEndpointSettings().setSecurity(securitySettings);
        PreviewEndPointSettings preview = null;

        ChannelSettings updatedSettings = new ChannelSettings().setIngest(ingest).setPreview(preview);

        // Act
        Future<OperationInfo> futureUpdate = service.beginUpdate(Channel.update(originalChannel.getId()).setSettings(
                updatedSettings));
        OperationInfo operationInfo = futureUpdate.get();

        ChannelInfo updatedChannel = service.get(Channel.get(originalChannel.getId()));

        // Assert
        verifyChannelProperties("updatedChannel", originalTestName, updatedDescription, originalChannelState,
                updatedChannel);
    }

    @Test
    public void updateChannelNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testChannelPrefix + "updatechnoch";
        ChannelSettings settings = createChannelSettings();
        Future<OperationInfo<ChannelInfo>> futureCreateChannel = service.beginCreate(Channel.create()
                .setName(originalTestName).setSize(ChannelSize.Large).setSettings(settings));
        ChannelInfo originalChannel = futureCreateChannel.get().getEntity();

        // Act
        service.update(Channel.update(originalChannel.getId()));
        ChannelInfo updatedChannel = service.get(Channel.get(originalChannel.getId()));

        // Assert
        verifyInfosEqual("updatedChannel", originalChannel, updatedChannel);
    }

    @Test
    public void updateChannelFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.beginUpdate(Channel.update(invalidChannelName));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void deleteChannelAsyncSuccess() throws Exception {
        String channelName = testChannelPrefix + "deletecha";
        ChannelSettings settings = createChannelSettings();
        Future<OperationInfo<ChannelInfo>> createChannelFuture = service.beginCreate(Channel.create()
                .setName(channelName).setSize(ChannelSize.Large).setSettings(settings));
        OperationInfo<ChannelInfo> operationInfo = createChannelFuture.get();
        ChannelInfo channelInfo = operationInfo.getEntity();
        List<ChannelInfo> listChannelsResult = service.list(Channel.list());
        int ChannelCountBaseline = listChannelsResult.size();

        // Act
        Future<OperationInfo> deleteFuture = service.beginDelete(Channel.delete(channelInfo.getId()));
        OperationInfo deleteOperationInfo = deleteFuture.get();

        // Assert

        listChannelsResult = service.list(Channel.list());
        assertEquals("listChannelsResult.size", ChannelCountBaseline - 1, listChannelsResult.size());
        assertEquals(OperationState.Succeeded, deleteOperationInfo.getState());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Channel.get(channelInfo.getId()));
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void deleteChannelAsyncFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        Future<OperationInfo> future = service.beginDelete(Channel.delete(invalidChannelName));
    }

    @Test
    public void deleteChannelFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(Channel.delete(invalidChannelName));
    }
}
