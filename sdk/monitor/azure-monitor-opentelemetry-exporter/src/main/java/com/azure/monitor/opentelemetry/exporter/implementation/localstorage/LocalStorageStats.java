// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

// this interface exists just to break the cycle between local storage and statsbeat
// TODO (trask) revisit this once statsbeat is pulled over into azure-monitor-opentelemetry-exporter
public interface LocalStorageStats {

    void incrementReadFailureCount();

    void incrementWriteFailureCount();

    static LocalStorageStats noop() {
        return NoopLocalStorageStats.INSTANCE;
    }
}
