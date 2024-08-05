// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.testutils;

import com.azure.core.test.TestMode;

import java.time.Instant;

public class Utils {
    public static String generateJobName(String baseName, TestMode testMode) {
        long timestamp = Instant.now().toEpochMilli();
        String jobName = baseName + "-" + timestamp;
        if (testMode == TestMode.RECORD || testMode == TestMode.PLAYBACK) {
            jobName = baseName;
        }
        return jobName;
    }

}
