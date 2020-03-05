// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.applicationinsights.query;

import com.azure.core.test.models.NetworkCallRecord;

import java.util.LinkedList;

/**
 * From:
 * https://github.com/Azure/autorest-clientruntime-for-java/blob/master/azure-arm-client-runtime/src/test/java/com/microsoft/azure/arm/core/RecordedData.java
 */
class RecordedData {
    private final LinkedList<NetworkCallRecord> networkCallRecords = new LinkedList<>();
    private final LinkedList<String> variables = new LinkedList<>();

    public LinkedList<NetworkCallRecord> getNetworkCallRecords() {
        return networkCallRecords;
    }

    public LinkedList<String> getVariables() {
        return variables;
    }
}
