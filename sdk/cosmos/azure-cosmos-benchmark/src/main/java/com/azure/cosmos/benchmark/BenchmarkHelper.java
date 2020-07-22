// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import java.util.Map;

public class BenchmarkHelper {
    static PojoizedJson generateDocument(String idString, String dataFieldValue, String partitionKey, int dataFieldCount) {
        PojoizedJson instance = new PojoizedJson();
        Map<String, String> properties = instance.getInstance();
        properties.put("id", idString);
        properties.put(partitionKey, idString);
        for (int i = 0; i < dataFieldCount; i++) {
            properties.put("dataField" + i, dataFieldValue);
        }

        return instance;
    }
}
