// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.IllegalEntityException;

import org.junit.Assume;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.fail;

public class SadPathTests extends TestBase {
    @Test
    public void noSuchEventHubNamespaceTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NoSuchEventHubNamespace");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        Assume.assumeFalse(TestUtilities.isRunningOnAzure());
        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (EventHubException e) {
            TestBase.logInfo("Got expected EventHubException");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void noSuchEventHubTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NoSuchEventHub");
        settings.inHasSenders = false;
        settings.inSkipIfNoEventHubConnectionString = true;
        settings.inoutEPHConstructorArgs.setEHPath("thereisnoeventhubwiththisname", PerTestSettings.EPHConstructorArgs.EH_PATH_OVERRIDE_AND_REPLACE);
        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (ExecutionException e) {
            Throwable inner = e.getCause();
            if ((inner != null) && (inner instanceof IllegalEntityException)) {
                TestBase.logInfo("Got expected IllegalEntityException");
            } else {
                throw e;
            }
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    // Turned off -- we cannot detect no such consumer group at register time
    //@Test
    public void noSuchConsumerGroupTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NoSuchConsumerGroup");
        settings.inHasSenders = false;
        settings.inoutEPHConstructorArgs.setConsumerGroupName("thereisnoconsumergroupwiththisname");
        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (ExecutionException e) {
            Throwable inner = e.getCause();
            if ((inner != null) && (inner instanceof IllegalEntityException)) {
                TestBase.logInfo("Got expected IllegalEntityException");
            } else {
                throw e;
            }
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void secondRegisterFailsTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("SecondRegisterFails");
        settings.inHasSenders = false;
        settings = testSetup(settings);

        try {
            settings.outHost.registerEventProcessorFactory(settings.outProcessorFactory, settings.inOptions).get();
            Thread.sleep(10000);
            fail("No exception occurred");
        } catch (IllegalStateException e) {
            if ((e.getMessage() != null) && (e.getMessage().compareTo("Register has already been called on this EventProcessorHost") == 0)) {
                TestBase.logInfo("Got expected exception");
            } else {
                fail("Got IllegalStateException but text is wrong");
            }
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void reregisterFailsTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("ReregisterFails");
        settings.inHasSenders = false;
        settings = testSetup(settings);

        try {
            Thread.sleep(15000);
            settings.outHost.unregisterEventProcessor().get();

            settings.outHost.registerEventProcessorFactory(settings.outProcessorFactory, settings.inOptions).get();
            Thread.sleep(10000);
            fail("No exception occurred");
        } catch (IllegalStateException e) {
            if ((e.getMessage() != null) && (e.getMessage().compareTo("Register cannot be called on an EventProcessorHost after unregister. Please create a new EventProcessorHost instance.") == 0)) {
                TestBase.logInfo("Got expected exception");
            } else {
                fail("Got IllegalStateException but text is wrong");
            }
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    // Turned off -- requires special setup beyond just having a real event hub
    // @Test
    public void badEventHubNameTest() throws Exception {
        // This case requires an eventhub with a bad name (not legal as storage container name).
        // Within EPH the validation of the name occurs after other operations that fail if the eventhub
        // doesn't exist, so this case can't use arbitrary bad names.
        PerTestSettings settings = new PerTestSettings("BadEventHubName");
        settings.inHasSenders = false;
        try {
            settings.inoutEPHConstructorArgs.setStorageContainerName(null); // otherwise test framework creates unique storage container name
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if ((message != null) && message.startsWith("EventHub names must conform to the following rules")) {
                TestBase.logInfo("Got expected IllegalArgumentException");
            } else {
                throw e;
            }
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }
}
