// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.guava25.base.Function;

public class OperationResult {
    private String containerName;
    private String databaseName;
    private Function<Void, String> lazyDiagnostics;
    private double ruCharges;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Function<Void, String> getLazyDiagnostics() {
        return lazyDiagnostics;
    }

    public void setLazyDiagnostics(Function<Void, String> lazyDiagnostics) {
        this.lazyDiagnostics = lazyDiagnostics;
    }

    public double getRuCharges() {
        return ruCharges;
    }

    public void setRuCharges(double ruCharges) {
        this.ruCharges = ruCharges;
    }
}
