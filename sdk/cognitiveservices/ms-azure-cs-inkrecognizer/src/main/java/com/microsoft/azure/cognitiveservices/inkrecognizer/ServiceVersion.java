// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum ServiceVersion {

    /**
     * Version 1.0.0 of the preview
     */
    PREVIEW_1_0_0("/v1.0-preview/recognize");

    private String serviceVersionString;
    private static final Map<String, ServiceVersion> MAP = new HashMap<>();

    static {
        for (ServiceVersion serviceVersion : ServiceVersion.values()) {
            MAP.put(serviceVersion.toString().toLowerCase(Locale.getDefault()), serviceVersion);
        }
    }

    ServiceVersion(String serviceVersionString) {
        this.serviceVersionString = serviceVersionString;
    }

    static ServiceVersion getServiceVersionOrDefault(String serviceVersionString) {
        if (serviceVersionString != null && MAP.containsKey(serviceVersionString.toLowerCase(Locale.getDefault()))) {
            return MAP.get(serviceVersionString.toLowerCase(Locale.getDefault()));
        } else {
            return PREVIEW_1_0_0;
        }
    }

    @Override
    public String toString() {
        return serviceVersionString;
    }

}
