package com.azure.cosmos.models;

public class FaultInjectionCondition {
    private final FaultInjectionEndpoints endpoints;
    private final FaultInjectionOperationType operationType;
    private final FaultInjectionRequestProtocol protocol;
    private final String region;

    public FaultInjectionCondition(
        FaultInjectionOperationType operationType,
        FaultInjectionRequestProtocol protocol,
        String region,
        FaultInjectionEndpoints endpoints) {
        this.operationType = operationType;
        this.protocol = protocol;
        this.region = region;
        this.endpoints = endpoints;
    }

    public FaultInjectionEndpoints getEndpoints() {
        return endpoints;
    }

    public FaultInjectionOperationType getOperationType() {
        return operationType;
    }

    public FaultInjectionRequestProtocol getProtocol() {
        return protocol;
    }

    public String getRegion() {
        return region;
    }
}
