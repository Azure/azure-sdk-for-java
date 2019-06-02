// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.interceptor;

import java.util.LinkedList;

public class RecordedData {
    private LinkedList<NetworkCallRecord> networkCallRecords = new LinkedList();
    private LinkedList<String> variables = new LinkedList();

    public RecordedData() {
    }

    public LinkedList<NetworkCallRecord> getNetworkCallRecords() {
        return this.networkCallRecords;
    }

    public LinkedList<String> getVariables() {
        return this.variables;
    }
}
