// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosDiagnostics;

public interface DiagnosticsClientContext {

    String getConfig();
    int getNumberOfClients();
    int clientId();

    CosmosDiagnostics createDiagnostics();
}
