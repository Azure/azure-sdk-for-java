/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class RuntimeVersionManagerTests {
    @Test
    public void getRuntimeClientForV1CanParseGoalState() {
        RuntimeVersionManager manager = createVersionManagerWithGoalState(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                        + "<GoalState>"
                        + "<Incarnation>1</Incarnation>"
                        + "<ExpectedState>Started</ExpectedState>"
                        + "<RoleEnvironmentPath>envpath</RoleEnvironmentPath>"
                        + "<CurrentStateEndpoint>statepath</CurrentStateEndpoint>"
                        + "<Deadline>2011-03-08T03:27:44.0Z</Deadline>"
                        + "</GoalState>", "2011-03-08");

        RuntimeClient runtimeClient = manager.getRuntimeClient("");
        GoalState goalState = null;

        try {
            goalState = runtimeClient.getCurrentGoalState();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Calendar expectedDeadline = GregorianCalendar.getInstance(TimeZone
                .getTimeZone("GMT+00:00"));

        expectedDeadline.clear();
        expectedDeadline.set(2011, 2, 8, 3, 27, 44);

        assertThat(goalState.getIncarnation(), equalTo(BigInteger.ONE));
        assertThat(goalState.getExpectedState(), equalTo(ExpectedState.STARTED));
        assertThat(goalState.getEnvironmentPath(), equalTo("envpath"));
        assertThat(goalState.getCurrentStateEndpoint(), equalTo("statepath"));
        assertThat(goalState.getDeadline().getTimeInMillis(),
                equalTo(expectedDeadline.getTimeInMillis()));
    }

    @Test
    public void getRuntimeClientThrowsWhenNoSupportedVersionsFound() {
        RuntimeVersionManager manager = createVersionManagerWithGoalState(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                        + "<GoalState>"
                        + "<Incarnation>1</Incarnation>"
                        + "<ExpectedState>Started</ExpectedState>"
                        + "<RoleEnvironmentPath>envpath</RoleEnvironmentPath>"
                        + "<CurrentStateEndpoint>statepath</CurrentStateEndpoint>"
                        + "<Deadline>2011-03-08T03:27:44.0Z</Deadline>"
                        + "</GoalState>", "notSupported");

        try {
            manager.getRuntimeClient("");
        } catch (RuntimeException ex) {
            return;
        }

        fail();
    }

    private RuntimeVersionManager createVersionManagerWithGoalState(
            String goalStateXml, String version) {
        File tempGoalState;

        try {
            tempGoalState = File.createTempFile("tempGoalState", null);
            FileOutputStream output = new FileOutputStream(tempGoalState);

            InputChannel goalStateChannel = new MockInputChannel(
                    new String[] { goalStateXml });
            BufferedInputStream input = new BufferedInputStream(
                    goalStateChannel.getInputStream(""));

            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            input.close();
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InputChannel inputChannel = new MockInputChannel(
                "<?xml version=\"1.0\"?>" + "<RuntimeServerDiscovery>"
                        + "<RuntimeServerEndpoints>"
                        + "<RuntimeServerEndpoint version=\"" + version
                        + "\" path=\"" + tempGoalState.getAbsolutePath()
                        + "\" />" + "</RuntimeServerEndpoints>"
                        + "</RuntimeServerDiscovery>");
        RuntimeVersionProtocolClient protocolClient = new RuntimeVersionProtocolClient(
                inputChannel);
        return new RuntimeVersionManager(protocolClient);
    }
}
