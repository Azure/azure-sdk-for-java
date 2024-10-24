// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

enum OperatingSystem {
    OS_WINDOWS("Windows"),
    OS_LINUX("Linux"),
    // TODO (heya) should we add Mac/OSX?
    OS_UNKNOWN("unknown");

    private final String value;

    OperatingSystem(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }
}
