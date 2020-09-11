package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.guava25.base.Function;

class OperationResult {
    private String databaseName;

    private String containerName;

    private double ruCharges;

    private Function<Void, String> lazyDiagnostics;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public double getRuCharges() {
        return ruCharges;
    }

    public void setRuCharges(double ruCharges) {
        this.ruCharges = ruCharges;
    }

    public Function<Void, String> getLazyDiagnostics() {
        return lazyDiagnostics;
    }

    public void setLazyDiagnostics(Function<Void, String> lazyDiagnostics) {
        this.lazyDiagnostics = lazyDiagnostics;
    }
}
