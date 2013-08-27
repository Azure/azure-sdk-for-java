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
        String testName = testChannelPrefix + "createChannelOptionsSuccess";
        String testDescription = "testDescription";

        ChannelState channelState = ChannelState.Starting;

        // Act
        ChannelInfo actualChannel = service.create(Channel.create().setName(testName).setDescription(testDescription));

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
        Thread.sleep(5000);
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
        String[] ChannelNames = new String[] { testChannelPrefix + "ChannelA", testChannelPrefix + "ChannelB" };
        List<ChannelInfo> expectedChannels = new ArrayList<ChannelInfo>();
        ChannelState channelState = ChannelState.Stopped;
        for (int i = 0; i < ChannelNames.length; i++) {
            String name = ChannelNames[i];
            expectedChannels.add(service.create(Channel.create().setName(name).setState(channelState)));
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
        String[] ChannelNames = new String[] { testChannelPrefix + "ChannelListOptionsA",
                testChannelPrefix + "ChannelListOptionsB", testChannelPrefix + "ChannelListOptionsC",
                testChannelPrefix + "ChannelListOptionsD" };
        List<ChannelInfo> expectedChannels = new ArrayList<ChannelInfo>();
        for (int i = 0; i < ChannelNames.length; i++) {
            String name = ChannelNames[i];
            expectedChannels.add(service.create(Channel.create().setName(name)));
        }

        Collection<ChannelInfo> listChannelResult = service.list(Channel.list().setTop(2));

        // Assert

        assertEquals(2, listChannelResult.size());
    }

    @Test
    public void updateChannelSuccess() throws Exception {
        // Arrange
        String originalTestName = testChannelPrefix + "updateChannelSuccessOriginal";
        ChannelState originalChannelState = ChannelState.Stopped;
        ChannelInfo originalChannel = service.create(Channel.create().setName(originalTestName));

        String updatedTestName = testChannelPrefix + "updateChannelSuccessUpdated";

        // Act
        service.update(Channel.update(originalChannel.getName()).setName(updatedTestName));
        ChannelInfo updatedChannel = service.get(Channel.get(updatedTestName));

        // Assert
        verifyChannelProperties("updatedChannel", updatedTestName, "", originalChannelState, updatedChannel);
    }

    @Test
    public void updateChannelNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testChannelPrefix + "updateChannelNoChangesSuccess";
        ChannelInfo originalChannel = service.create(Channel.create().setName(originalTestName));

        // Act
        service.update(Channel.update(originalChannel.getName()));
        ChannelInfo updatedChannel = service.get(Channel.get(originalChannel.getName()));

        // Assert
        verifyInfosEqual("updatedChannel", originalChannel, updatedChannel);
    }

    @Test
    public void updateChannelFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.update(Channel.update(invalidChannelName));
    }

    @Test
    public void deleteChannelSuccess() throws Exception {
        // Arrange
        String channelName = testChannelPrefix + "deleteChannelSuccess";
        ChannelInfo channelInfo = service.create(Channel.create().setName(channelName));
        List<ChannelInfo> listChannelsResult = service.list(Channel.list());
        int ChannelCountBaseline = listChannelsResult.size();

        // Act
        service.delete(Channel.delete(channelInfo.getName()));

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
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.delete(Channel.delete(invalidChannelName));
    }
}
