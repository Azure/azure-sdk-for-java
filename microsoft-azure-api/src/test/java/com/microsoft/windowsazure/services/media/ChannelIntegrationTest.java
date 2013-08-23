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

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Channel;
import com.microsoft.windowsazure.services.media.models.ChannelInfo;
import com.microsoft.windowsazure.services.media.models.ChannelState;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.LinkInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.ProtectionKey;
import com.microsoft.windowsazure.services.media.models.Task;
import com.microsoft.windowsazure.services.media.models.Task.CreateBatchOperation;

public class ChannelIntegrationTest extends IntegrationTestBase {

    protected String testChannelPrefix = "testChannelPrefix";

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
        verifyChannelProperties("actualChannel", testName, altId, encryptionOption, ChannelState, actualChannel);
    }

    @Test
    public void createChannelNullNameSuccess() throws Exception {
        // Arrange

        // Act
        ChannelInfo actualChannel = null;
        try {
            actualChannel = service.create(Channel.create());
            // Assert
            verifyChannelProperties("actualChannel", "", "", ChannelOption.None, ChannelState.Initialized,
                    actualChannel);
        }
        finally {
            // Clean up the anonymous Channel now while we have the id, because we
            // do not want to delete all anonymous Channels in the bulk-cleanup code.
            try {
                if (actualChannel != null) {
                    service.delete(Channel.delete(actualChannel.getId()));
                }
            }
            catch (ServiceException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void getChannelSuccess() throws Exception {
        // Arrange
        String testName = testChannelPrefix + "GetChannelSuccess";
        String altId = "altId";
        ChannelOption encryptionOption = ChannelOption.StorageEncrypted;
        ChannelState ChannelState = ChannelState.Published;

        ChannelInfo ChannelInfo = service.create(Channel.create().setName(testName).setAlternateId(altId)
                .setOptions(encryptionOption).setState(ChannelState));

        // Act
        ChannelInfo actualChannel = service.get(Channel.get(ChannelInfo.getId()));

        // Assert
        verifyInfosEqual("actualChannel", ChannelInfo, actualChannel);
    }

    @Test
    public void getChannelInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(Channel.get(invalidId));
    }

    @Test
    public void getChannelNonexistId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Channel.get(validButNonexistChannelId));
    }

    @Test
    public void listChannelSuccess() throws ServiceException {
        // Arrange
        String altId = "altId";
        ChannelOption encryptionOption = ChannelOption.StorageEncrypted;
        ChannelState ChannelState = ChannelState.Published;

        String[] ChannelNames = new String[] { testChannelPrefix + "ChannelA", testChannelPrefix + "ChannelB" };
        List<ChannelInfo> expectedChannels = new ArrayList<ChannelInfo>();
        for (int i = 0; i < ChannelNames.length; i++) {
            String name = ChannelNames[i];
            expectedChannels.add(service.create(Channel.create().setName(name).setAlternateId(altId)
                    .setOptions(encryptionOption).setState(ChannelState)));
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
        ChannelOption originalEncryptionOption = ChannelOption.StorageEncrypted;
        ChannelState originalChannelState = ChannelState.Initialized;
        ChannelInfo originalChannel = service.create(Channel.create().setName(originalTestName).setAlternateId("altId")
                .setOptions(originalEncryptionOption));

        String updatedTestName = testChannelPrefix + "updateChannelSuccessUpdated";
        String altId = "otherAltId";

        // Act
        service.update(Channel.update(originalChannel.getId()).setName(updatedTestName).setAlternateId(altId));
        ChannelInfo updatedChannel = service.get(Channel.get(originalChannel.getId()));

        // Assert
        verifyChannelProperties("updatedChannel", updatedTestName, altId, originalEncryptionOption,
                originalChannelState, updatedChannel);
    }

    @Test
    public void updateChannelNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testChannelPrefix + "updateChannelNoChangesSuccess";
        String altId = "altId";
        ChannelInfo originalChannel = service.create(Channel.create().setName(originalTestName).setAlternateId(altId));

        // Act
        service.update(Channel.update(originalChannel.getId()));
        ChannelInfo updatedChannel = service.get(Channel.get(originalChannel.getId()));

        // Assert
        verifyInfosEqual("updatedChannel", originalChannel, updatedChannel);
    }

    @Test
    public void updateChannelFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.update(Channel.update(validButNonexistChannelId));
    }

    @Test
    public void deleteChannelSuccess() throws Exception {
        // Arrange
        String ChannelName = testChannelPrefix + "deleteChannelSuccess";
        ChannelInfo ChannelInfo = service.create(Channel.create().setName(ChannelName));
        List<ChannelInfo> listChannelsResult = service.list(Channel.list());
        int ChannelCountBaseline = listChannelsResult.size();

        // Act
        service.delete(Channel.delete(ChannelInfo.getId()));

        // Assert
        listChannelsResult = service.list(Channel.list());
        assertEquals("listChannelsResult.size", ChannelCountBaseline - 1, listChannelsResult.size());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Channel.get(ChannelInfo.getId()));
    }

    @Test
    public void deleteChannelFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.delete(Channel.delete(validButNonexistChannelId));
    }

    @Test
    public void linkChannelContentKeySuccess() throws ServiceException, URISyntaxException {
        // Arrange
        String originalTestName = testChannelPrefix + "linkChannelContentKeySuccess";
        ChannelInfo ChannelInfo = service.create(Channel.create().setName(originalTestName)
                .setOptions(ChannelOption.StorageEncrypted));

        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(ContentKeyType.StorageEncryption));
        String contentKeyId = String.format("nb:kid:UUID:%s", UUID.randomUUID());
        String encryptedContentKey = "dummyEncryptedContentKey";
        service.create(ContentKey.create(contentKeyId, ContentKeyType.StorageEncryption, encryptedContentKey)
                .setProtectionKeyId(protectionKeyId));

        // Act
        service.action(Channel.linkContentKey(ChannelInfo.getId(), contentKeyId));

        // Assert

        List<ContentKeyInfo> contentKeys = service.list(ContentKey.list(ChannelInfo.getContentKeysLink()));
        assertEquals(1, contentKeys.size());
        assertEquals(contentKeyId, contentKeys.get(0).getId());
    }

    @Test
    public void linkChannelContentKeyInvalidIdFailed() throws ServiceException, URISyntaxException {
        // Arrange

        // Act
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.action(Channel.linkContentKey(validButNonexistChannelId, "nb:kid:UUID:invalidContentKeyId"));

        // Assert
    }

    @Test
    public void canGetParentBackFromChannel() throws ServiceException, InterruptedException {
        // Arrange
        String originalChannelName = testChannelPrefix + "canGetParentBackFromChannel";
        ChannelInfo originalChannel = service.create(Channel.create().setName(originalChannelName));

        int durationInMinutes = 10;
        AccessPolicyInfo accessPolicyInfo = service.create(AccessPolicy.create(testPolicyPrefix
                + "uploadAesPortectedChannelSuccess", durationInMinutes, EnumSet.of(AccessPolicyPermission.WRITE)));

        LocatorInfo locatorInfo = service.create(Locator.create(accessPolicyInfo.getId(), originalChannel.getId(),
                LocatorType.SAS));
        WritableBlobContainerContract blobWriter = service.createBlobWriter(locatorInfo);

        InputStream mpeg4H264InputStream = getClass().getResourceAsStream("/media/MPEG4-H264.mp4");
        blobWriter.createBlockBlob("MPEG4-H264.mp4", mpeg4H264InputStream);
        service.action(ChannelFile.createFileInfos(originalChannel.getId()));

        String jobName = testJobPrefix + "createJobSuccess";
        CreateBatchOperation taskCreator = Task
                .create(MEDIA_ENCODER_MEDIA_PROCESSOR_2_2_0_0_ID,
                        "<taskBody>" + "<inputChannel>JobInputChannel(0)</inputChannel>"
                                + "<outputChannel>JobOutputChannel(0)</outputChannel>" + "</taskBody>")
                .setConfiguration("H.264 256k DSL CBR").setName("My encoding Task");
        JobInfo jobInfo = service.create(Job.create().setName(jobName).addInputMediaChannel(originalChannel.getId())
                .addTaskCreator(taskCreator));

        // Act
        ListResult<ChannelInfo> outputChannels = service.list(Channel.list(jobInfo.getOutputChannelsLink()));
        assertEquals(1, outputChannels.size());
        ChannelInfo childChannel = outputChannels.get(0);

        LinkInfo<ChannelInfo> parentChannelLink = childChannel.getParentChannelsLink();
        ChannelInfo parentChannel = service.get(Channel.get(parentChannelLink));

        // Assert
        assertEquals(originalChannel.getId(), parentChannel.getId());
    }
}
