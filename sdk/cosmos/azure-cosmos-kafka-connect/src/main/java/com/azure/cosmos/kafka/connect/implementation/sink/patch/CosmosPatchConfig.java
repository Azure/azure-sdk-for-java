// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.patch;

import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosPatchConfig {
    private final KafkaCosmosPatchOperationType defaultPatchOperationType;
    private final Map<String, CosmosPatchJsonPropertyConfig> jsonPropertyConfigMap;
    private final String filter;

    public CosmosPatchConfig(
        KafkaCosmosPatchOperationType defaultPatchOperationType,
        Map<String, CosmosPatchJsonPropertyConfig> jsonPropertyConfigMap,
        String filter) {

        checkNotNull(jsonPropertyConfigMap, "Argument 'jsonPathConfigMap' can not be null");
        this.defaultPatchOperationType = defaultPatchOperationType;
        this.jsonPropertyConfigMap = jsonPropertyConfigMap;
        this.filter = filter;
    }

    public KafkaCosmosPatchOperationType getDefaultPatchOperationType() {
        return defaultPatchOperationType;
    }

    public Map<String, CosmosPatchJsonPropertyConfig> getJsonPropertyConfigMap() {
        return jsonPropertyConfigMap;
    }

    public String getFilter() {
        return filter;
    }
}
