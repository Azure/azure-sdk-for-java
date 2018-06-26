/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import org.junit.Assume;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class TestUtilities {
    static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    static String getStorageConnectionString() {
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
