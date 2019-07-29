// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

public final class CustomHeaders {

    public static final class HttpHeaders {
        // Specify whether to exclude system properties while storing the document
        public static final String EXCLUDE_SYSTEM_PROPERTIES = "x-ms-exclude-system-properties";
    }
}
