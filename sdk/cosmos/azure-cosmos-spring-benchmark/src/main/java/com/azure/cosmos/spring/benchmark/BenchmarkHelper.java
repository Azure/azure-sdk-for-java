// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spring.benchmark;

import java.time.Duration;

public class BenchmarkHelper {
    public static User generateUser(String idString, String dataFieldValue, String partitionKey) {
        User user = new User(idString, partitionKey, dataFieldValue, dataFieldValue, dataFieldValue);
        return user;
    }

    public static boolean shouldContinue(long startTimeMillis, long iterationCount, Configuration configuration) {

        Duration maxDurationTime = configuration.getMaxRunningTimeDuration();
        int maxNumberOfOperations = configuration.getNumberOfOperations();

        if (maxDurationTime == null) {
            return iterationCount < maxNumberOfOperations;
        }

        return startTimeMillis + maxDurationTime.toMillis() > System.currentTimeMillis();
    }
}
