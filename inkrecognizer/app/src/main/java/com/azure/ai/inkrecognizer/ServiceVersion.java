// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import java.util.HashMap;
import java.util.Map;

public enum ServiceVersion {

    /**
     * Version 1.0.0 of the preview
     */
    PREVIEW_1_0_0("/v1.0-preview/recognize");

    private String serviceVersionString;
    private static final Map<String, ServiceVersion> map = new HashMap<>();

    static {
        for (ServiceVersion serviceVersion : ServiceVersion.values()) {
            map.put(serviceVersion.toString(), serviceVersion);
        }
    }

    ServiceVersion(String serviceVersionString) {
        this.serviceVersionString = serviceVersionString;
    }

    static ServiceVersion getServiceVersionOrDefault(String serviceVersionString) {
        if (map.containsKey(serviceVersionString)) {
            return map.get(serviceVersionString);
        } else {
            return PREVIEW_1_0_0;
        }
    }

    @Override
    public String toString() {
        return serviceVersionString;
    }

}
