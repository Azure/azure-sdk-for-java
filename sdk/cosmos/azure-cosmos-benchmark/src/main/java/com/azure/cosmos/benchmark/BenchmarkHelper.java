// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import java.time.Duration;
import java.util.Map;

public class BenchmarkHelper {
    public static PojoizedJson generateDocument(String idString, String dataFieldValue, String partitionKey,
                                         int dataFieldCount) {
        PojoizedJson instance = new PojoizedJson();
        Map<String, Object> properties = instance.getInstance();
        properties.put("id", idString);
        properties.put(partitionKey, idString);
        for (int i = 0; i < dataFieldCount; i++) {
            properties.put("dataField" + i, dataFieldValue);
        }

        return instance;
    }

    public static boolean shouldContinue(long startTimeMillis, long iterationCount, Configuration configuration) {

        Duration maxDurationTime = configuration.getMaxRunningTimeDuration();
        int maxNumberOfOperations = configuration.getNumberOfOperations();

        if (maxDurationTime == null) {
            return iterationCount < maxNumberOfOperations;
        }

        if (startTimeMillis + maxDurationTime.toMillis() < System.currentTimeMillis()) {
            return false;
        }

        if (maxNumberOfOperations < 0) {
            return true;
        }

        return iterationCount < maxNumberOfOperations;
    }
}
