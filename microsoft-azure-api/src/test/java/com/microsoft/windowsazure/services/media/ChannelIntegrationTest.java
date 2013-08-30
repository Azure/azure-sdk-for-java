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

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.Channel;
import com.microsoft.windowsazure.services.media.models.ChannelInfo;
import com.microsoft.windowsazure.services.media.models.ChannelSize;
import com.microsoft.windowsazure.services.media.models.ChannelState;

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

    @Test
    public void createChannelOptionsSuccess() throws Exception {
        // Arrange
        String testName = testChannelPrefix + "createchopt";
        String testDescription = "testDescription";

        ChannelState channelState = ChannelState.Stopped;

        // Act
        ChannelInfo actualChannel = service.create(Channel.create().setName(testName).setDescription(testDescription)
                .setSize(ChannelSize.Large));

        // Assert
        verifyChannelProperties("actualChannel", testName, testDescription, channelState, actualChannel);
    }

    @Test
    public void getChannelSuccess() throws Exception {
        // Arrange
        String testName = testChannelPrefix + "getchannel";
        String testDescription = "testDescription";
        ChannelState channelState = ChannelState.Stopped;
        ChannelSize channelSize = ChannelSize.Large;
        Thread.sleep(1000);

        ChannelInfo channelInfo = service.create(Channel.create().setName(testName).setDescription(testDescription)
                .setState(channelState).setSize(channelSize));
        Thread.sleep(10000);
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
    public void listChannelSuccess() throws ServiceException {
        // Arrange
        String[] ChannelNames = new String[] { testChannelPrefix + "cha", testChannelPrefix + "chb" };
        List<ChannelInfo> expectedChannels = new ArrayList<ChannelInfo>();
        ChannelState channelState = ChannelState.Stopped;
        for (int i = 0; i < ChannelNames.length; i++) {
            String name = ChannelNames[i];
            expectedChannels.add(service.create(Channel.create().setName(name).setState(channelState)
                    .setSize(ChannelSize.Large)));
        }

        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
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
    public void canListChannelsWithOptions() throws ServiceException {
        String[] ChannelNames = new String[] { testChannelPrefix + "channellista", testChannelPrefix + "channellistb",
                testChannelPrefix + "channellistc", testChannelPrefix + "channellistad" };
        List<ChannelInfo> expectedChannels = new ArrayList<ChannelInfo>();
        for (int i = 0; i < ChannelNames.length; i++) {
            String name = ChannelNames[i];
            expectedChannels.add(service.create(Channel.create().setName(name).setSize(ChannelSize.Large)));
        }

        Collection<ChannelInfo> listChannelResult = service.list(Channel.list().setTop(2));

        try {
            Thread.sleep(20000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Assert

        assertEquals(2, listChannelResult.size());
    }

    @Test
    public void updateChannelSuccess() throws Exception {
        // Arrange
        String originalTestName = testChannelPrefix + "updatecho";
        ChannelState originalChannelState = ChannelState.Stopped;
        ChannelInfo originalChannel = service.create(Channel.create().setName(originalTestName)
                .setSize(ChannelSize.Large));

        String updatedTestName = testChannelPrefix + "updatech";

        // Act
        Thread.sleep(10000);
        service.update(Channel.update(originalChannel.getId()).setName(updatedTestName));
        ChannelInfo updatedChannel = service.get(Channel.get(updatedTestName));

        // Assert
        verifyChannelProperties("updatedChannel", updatedTestName, "", originalChannelState, updatedChannel);
    }

    @Test
    public void updateChannelNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testChannelPrefix + "updatechnoch";
        ChannelInfo originalChannel = service.create(Channel.create().setName(originalTestName)
                .setSize(ChannelSize.Large));
        Thread.sleep(10000);

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
        service.update(Channel.update(invalidChannelName));
    }

    @Test
    public void deleteChannelSuccess() throws Exception {
        // Arrange
        String channelName = testChannelPrefix + "deletech";
        ChannelInfo channelInfo = service.create(Channel.create().setName(channelName).setSize(ChannelSize.Large));
        Thread.sleep(10000);
        List<ChannelInfo> listChannelsResult = service.list(Channel.list());
        int ChannelCountBaseline = listChannelsResult.size();

        // Act
        service.delete(Channel.delete(channelInfo.getId()));
        Thread.sleep(10000);

        // Assert
        listChannelsResult = service.list(Channel.list());
        assertEquals("listChannelsResult.size", ChannelCountBaseline - 1, listChannelsResult.size());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Channel.get(channelInfo.getName()));
    }

    @Test
    public void deleteChannelFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(Channel.delete(invalidChannelName));
    }
}
