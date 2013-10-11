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
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.Channel;
import com.microsoft.windowsazure.services.media.models.ChannelInfo;
import com.microsoft.windowsazure.services.media.models.ChannelSize;
import com.microsoft.windowsazure.services.media.models.IngestEndpointSettings;
import com.microsoft.windowsazure.services.media.models.Ipv4;
import com.microsoft.windowsazure.services.media.models.OperationInfo;
import com.microsoft.windowsazure.services.media.models.OperationState;
import com.microsoft.windowsazure.services.media.models.PreviewEndPointSettings;
import com.microsoft.windowsazure.services.media.models.Program;
import com.microsoft.windowsazure.services.media.models.ProgramInfo;
import com.microsoft.windowsazure.services.media.models.ProgramState;
import com.microsoft.windowsazure.services.media.models.SecuritySettings;

public class ProgramIntegrationTest extends IntegrationTestBase {

    protected String testProgramPrefix = "testProgramprefix";
    protected String invalidProgramName = "invalidProgramName";

    private void verifyInfosEqual(String message, ProgramInfo expected, ProgramInfo actual) {
        verifyProgramProperties(message, expected.getName(), expected.getDescription(), expected.getState(), actual);
    }

    private void verifyProgramProperties(String message, String testName, String testDescription,
            ProgramState ProgramState, ProgramInfo actualProgram) {
        verifyProgramProperties(message, testName, testDescription, ProgramState, null, null, actualProgram);
    }

    private void verifyProgramProperties(String message, String testName, String testDescription,
            ProgramState ProgramState, Date created, Date lastModified, ProgramInfo actualProgram) {
        assertNotNull(message, actualProgram);
        assertEquals(message + " Name", testName, actualProgram.getName());
        assertEquals(message + " Description", testDescription, actualProgram.getDescription());
        assertEquals(message + " State", ProgramState, actualProgram.getState());

        if (created != null) {
            assertEquals(message + " Created", created, actualProgram.getCreated());
        }
        if (lastModified != null) {
            assertEquals(message + " LastModified", lastModified, actualProgram.getLastModified());
        }
    }

    @Test
    public void createProgramOptionsSuccess() throws Exception {
        // Arrange
        String testName = testProgramPrefix + "createchopt";
        String testDescription = "testDescription";
        ProgramState programState = ProgramState.Stopped;
        AssetInfo assetInfo = service.create(Asset.create());
        Future<OperationInfo<ChannelInfo>> futureOperationChannelInfo = service.beginCreate(Channel.create()
                .setSize(ChannelSize.Large).setName(testName));
        ChannelInfo channelInfo = futureOperationChannelInfo.get().getEntity();
        int estimatedDurationSeconds = 3600;
        int dvrWindowLengthSeconds = 60;

        // Act
        Future<OperationInfo<ProgramInfo>> futureOperationProgram = service.beginCreate(Program.create()
                .setName(testName).setDescription(testDescription).setAssetId(assetInfo.getId())
                .setChannelId(channelInfo.getId()).setEstimatedDurationSeconds(estimatedDurationSeconds)
                .setDvrWindowLengthSeconds(dvrWindowLengthSeconds));
        ProgramInfo actualProgram = futureOperationProgram.get().getEntity();

        // Assert
        verifyProgramProperties("actualProgram", testName, testDescription, programState, actualProgram);
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void ProgramCanBeStartedAndStopped() throws Exception {
        // Arrange 
        String testName = testProgramPrefix + "Programsas";
        String testDescription = "testDescription";

        // Act
        Future<OperationInfo<ProgramInfo>> actualProgram = service.beginCreate(Program.create().setName(testName)
                .setDescription(testDescription));

        ProgramInfo ProgramInfo = actualProgram.get().getEntity();
        Future<OperationInfo> startFuture = service.beginAction(Program.start(ProgramInfo.getId()));
        OperationInfo startOperationInfo = startFuture.get();

        Future<OperationInfo> stopFuture = service.beginAction(Program.stop(ProgramInfo.getId()));
        OperationInfo stopOperationInfo = stopFuture.get();

        // Assert

    }

    @Test
    public void getProgramSuccess() throws Exception {
        // Arrange
        String testName = testProgramPrefix + "getProgram";
        String testDescription = "testDescription";
        ProgramState programState = ProgramState.Stopped;

        Future<OperationInfo<ProgramInfo>> futureOperationInfo = service.beginCreate(Program.create().setName(testName)
                .setDescription(testDescription).setState(programState));
        ProgramInfo ProgramInfo = futureOperationInfo.get().getEntity();
        // Act
        ProgramInfo actualProgramInfo = service.get(Program.get(ProgramInfo.getId()));

        // Assert
        verifyInfosEqual("actualProgram", ProgramInfo, actualProgramInfo);
    }

    @Test
    public void getProgramInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(Program.get(invalidId));
    }

    @Test
    public void listProgramSuccess() throws ServiceException, InterruptedException, ExecutionException {
        // Arrange
        String[] ProgramNames = new String[] { testProgramPrefix + "cha", testProgramPrefix + "chb" };
        List<Future<OperationInfo<ProgramInfo>>> expectedFutures = new ArrayList<Future<OperationInfo<ProgramInfo>>>();
        List<ProgramInfo> expectedPrograms = new ArrayList<ProgramInfo>();

        ProgramState programState = ProgramState.Stopped;
        for (int i = 0; i < ProgramNames.length; i++) {
            String name = ProgramNames[i];
            expectedFutures.add(service.beginCreate(Program.create().setName(name).setState(programState)));
        }

        // Act
        Collection<ProgramInfo> listProgramResult = service.list(Program.list());

        // Assert

        verifyListResultContains("listPrograms", expectedPrograms, listProgramResult, new ComponentDelegate() {
            @Override
            public void verifyEquals(String message, Object expected, Object actual) {
                verifyInfosEqual(message, (ProgramInfo) expected, (ProgramInfo) actual);
            }
        });
    }

    @Test
    public void canListProgramsWithOptions() throws ServiceException, InterruptedException, ExecutionException {
        String[] ProgramNames = new String[] { testProgramPrefix + "Programlista", testProgramPrefix + "Programlistb",
                testProgramPrefix + "Programlistc", testProgramPrefix + "Programlistad" };
        List<ProgramInfo> expectedPrograms = new ArrayList<ProgramInfo>();
        List<Future<OperationInfo<ProgramInfo>>> expectedFutures = new ArrayList<Future<OperationInfo<ProgramInfo>>>();

        for (int i = 0; i < ProgramNames.length; i++) {
            String name = ProgramNames[i];
            expectedFutures.add(service.beginCreate(Program.create().setName(name)));
        }

        for (Future<OperationInfo<ProgramInfo>> futureOperationInfo : expectedFutures) {
            expectedPrograms.add(futureOperationInfo.get().getEntity());
        }

        Collection<ProgramInfo> listProgramResult = service.list(Program.list().setTop(2));

        // Assert

        assertEquals(2, listProgramResult.size());
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void updateProgramAsyncSuccess() throws Exception {
        // Arrange
        String originalTestName = testProgramPrefix + "updatecho";
        ProgramState originalProgramState = ProgramState.Stopped;

        Future<OperationInfo<ProgramInfo>> futureCreateProgram = service.beginCreate(Program.create().setName(
                originalTestName));
        ProgramInfo originalProgram = futureCreateProgram.get().getEntity();

        String updatedDescription = "description";
        SecuritySettings securitySettings = new SecuritySettings();
        Ipv4 ipv4 = new Ipv4().setName("Allow all").setIp("0.0.0.0/0");
        List<Ipv4> ipV4List = new ArrayList<Ipv4>();
        ipV4List.add(ipv4);
        securitySettings.setIpV4AllowList(ipV4List);
        IngestEndpointSettings ingest = new IngestEndpointSettings().setSecurity(securitySettings);
        PreviewEndPointSettings preview = null;

        // Act
        Future<OperationInfo> futureUpdate = service.beginUpdate(Program.update(originalProgram.getId()));
        OperationInfo operationInfo = futureUpdate.get();

        ProgramInfo updatedProgram = service.get(Program.get(originalProgram.getId()));

        // Assert
        verifyProgramProperties("updatedProgram", originalTestName, updatedDescription, originalProgramState,
                updatedProgram);
    }

    @Test
    public void updateProgramNoChangesSuccess() throws Exception {
        // Arrange
        String originalTestName = testProgramPrefix + "updatechnoch";
        Future<OperationInfo<ProgramInfo>> futureCreateProgram = service.beginCreate(Program.create().setName(
                originalTestName));
        ProgramInfo originalProgram = futureCreateProgram.get().getEntity();

        // Act
        service.update(Program.update(originalProgram.getId()));
        ProgramInfo updatedProgram = service.get(Program.get(originalProgram.getId()));

        // Assert
        verifyInfosEqual("updatedProgram", originalProgram, updatedProgram);
    }

    @Test
    public void updateProgramFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.beginUpdate(Program.update(invalidProgramName));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void deleteProgramAsyncSuccess() throws Exception {
        String ProgramName = testProgramPrefix + "deletecha";
        Future<OperationInfo<ProgramInfo>> createProgramFuture = service.beginCreate(Program.create().setName(
                ProgramName));
        OperationInfo<ProgramInfo> operationInfo = createProgramFuture.get();
        ProgramInfo ProgramInfo = operationInfo.getEntity();
        List<ProgramInfo> listProgramsResult = service.list(Program.list());
        int ProgramCountBaseline = listProgramsResult.size();

        // Act
        Future<OperationInfo> deleteFuture = service.beginDelete(Program.delete(ProgramInfo.getId()));
        OperationInfo deleteOperationInfo = deleteFuture.get();

        // Assert

        listProgramsResult = service.list(Program.list());
        assertEquals("listProgramsResult.size", ProgramCountBaseline - 1, listProgramsResult.size());
        assertEquals(OperationState.Succeeded, deleteOperationInfo.getState());

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(Program.get(ProgramInfo.getId()));
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    @Test
    public void deleteProgramAsyncFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        Future<OperationInfo> future = service.beginDelete(Program.delete(invalidProgramName));
    }

    @Test
    public void deleteProgramFailedWithInvalidId() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(Program.delete(invalidProgramName));
    }
}
