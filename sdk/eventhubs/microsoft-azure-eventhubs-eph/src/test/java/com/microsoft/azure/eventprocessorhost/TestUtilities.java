// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import org.junit.Assume;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

final class TestUtilities {
    static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    static void skipIfAppveyor() {
        String appveyor = System.getenv("APPVEYOR"); // Set to "true" by Appveyor
        if (appveyor != null) {
            TestBase.logInfo("SKIPPING - APPVEYOR DETECTED");
        }
        Assume.assumeTrue(appveyor == null);
    }

    static String getStorageConnectionString() {
        TestUtilities.skipIfAppveyor();

        String retval = System.getenv("EPHTESTSTORAGE");

        // if EPHTESTSTORAGE is not set - we cannot run integration tests
        if (retval == null) {
            TestBase.logInfo("SKIPPING - NO STORAGE CONNECTION STRING");
        }
        Assume.assumeTrue(retval != null);

        return ((retval != null) ? retval : "");
    }

    static Boolean isRunningOnAzure() {
        return (System.getenv("EVENT_HUB_CONNECTION_STRING") != null);
    }
}
