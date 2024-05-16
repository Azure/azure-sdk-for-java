// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import org.junit.Test;

import static org.junit.Assert.fail;

public class EPHConstructorTests extends TestBase {
    @Test
    public void conflictingEventHubPathsTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("ConflictingEventHubPaths");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setEHPath("thisisdifferentfromtheconnectionstring", PerTestSettings.EPHConstructorArgs.EH_PATH_OVERRIDE);

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            if ((e.getMessage() != null) && (e.getMessage().compareTo("Provided EventHub path in eventHubPath parameter conflicts with the path in provided EventHub connection string") == 0)) {
                TestBase.logInfo("Got expected exception");
            } else {
                throw e;
            }
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void missingEventHubPathTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("MissingEventHubPath");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.removePathFromEHConnection();
        settings.inoutEPHConstructorArgs.setEHPath(null, PerTestSettings.EPHConstructorArgs.EH_PATH_OVERRIDE);

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            if ((e.getMessage() != null) && (e.getMessage().compareTo("Provide EventHub entity path in either eventHubPath argument or in eventHubConnectionString") == 0)) {
                TestBase.logInfo("Got expected exception");
            } else {
                throw e;
            }
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void nullHostNameTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NullHostName");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setHostName(null);

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void emptyHostNameTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("EmptyHostName");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setHostName("");

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void nullConsumerGroupNameTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NullConsumerGroupName");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setConsumerGroupName(null);

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void emptyConsumerGroupNameTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("EmptyConsumerGroupName");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setConsumerGroupName("");

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void nullEHConnectionStringTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NullEHConnectionString");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setEHConnection(null);

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void emptyEHConnectionStringTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("EmptyEHConnectionString");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setEHConnection("");

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void ehPathOnlySeparateTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("EHPathOnlySeparate");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.removePathFromEHConnection();

        try {
            settings = testSetup(settings);
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void ehPathOnlyInConnStringTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("EHPathOnlyInConnString");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setEHPath(null, PerTestSettings.EPHConstructorArgs.EH_PATH_OVERRIDE);

        try {
            settings = testSetup(settings);
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void nullCheckpointManagerTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NullCheckpointManager");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setCheckpointManager(null);

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (NullPointerException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void nullLeaseManagerTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NullLeaseManager");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;
        settings.inoutEPHConstructorArgs.dummyStorageConnection();

        settings.inoutEPHConstructorArgs.setLeaseManager(null);

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (NullPointerException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void nullStorageConnectionStringTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("NullStorageConnectionString");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;

        settings.inoutEPHConstructorArgs.setStorageConnection(null);
        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    @Test
    public void emptyStorageConnectionStringTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("EmptyStorageConnectionString");
        settings.inHasSenders = false;
        settings.inEventHubDoesNotExist = true;

        settings.inoutEPHConstructorArgs.setStorageConnection("");

        try {
            settings = testSetup(settings);
            fail("No exception occurred");
        } catch (IllegalArgumentException e) {
            TestBase.logInfo("Got expected exception");
        } finally {
            testFinish(settings, NO_CHECKS);
        }
    }

    // TODO
    // @Test
    // public void verifyStorageContainerNameTest() throws Exception
    // Uses Storage APIs to check that the expected container has been created.

    // TODO
    // @Test
    // public void verifyStorageBlobPrefixTest() throws Exception
    // Uses Storage APIs to check that the blobs have the expected prefix in their names
}
