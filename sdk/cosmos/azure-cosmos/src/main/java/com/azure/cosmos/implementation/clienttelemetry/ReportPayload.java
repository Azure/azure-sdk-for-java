// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;

import static com.azure.cosmos.implementation.Utils.as;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportPayload {
    private String regionsContacted;
    private Boolean greaterThan1Kb;
    private ConsistencyLevel consistency;
    private String databaseName;
    private String containerName;
    private OperationType operation;
    private ResourceType resource;
    private Integer statusCode;
    private String operationId;
    private MetricInfo metricInfo;

    public ReportPayload(String metricInfoName, String unitName) {
        metricInfo = new MetricInfo(metricInfoName, unitName);
    }

    public String getRegionsContacted() {
        return regionsContacted;
    }

    public void setRegionsContacted(String regionsContacted) {
        this.regionsContacted = regionsContacted;
    }

    public Boolean getGreaterThan1Kb() {
        return greaterThan1Kb;
    }

    public void setGreaterThan1Kb(Boolean greaterThan1Kb) {
        this.greaterThan1Kb = greaterThan1Kb;
    }

    public ConsistencyLevel getConsistency() {
        return consistency;
    }

    public void setConsistency(ConsistencyLevel consistency) {
        this.consistency = consistency;
    }

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

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public ResourceType getResource() {
        return resource;
    }

    public void setResource(ResourceType resource) {
        this.resource = resource;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public MetricInfo getMetricInfo() {
        return metricInfo;
    }

    public void setMetricInfo(MetricInfo metricInfo) {
        this.metricInfo = metricInfo;
    }

    @Override
    public boolean equals(Object obj) {
        ReportPayload reportPayload = as(obj, ReportPayload.class);
        if (reportPayload == null) {
            return false;
        }
        if (((reportPayload.regionsContacted == null && this.regionsContacted == null) || (reportPayload.regionsContacted != null && reportPayload.regionsContacted.equals(this.regionsContacted))) &&
            ((reportPayload.greaterThan1Kb == null && this.greaterThan1Kb == null) || (reportPayload.greaterThan1Kb != null && reportPayload.greaterThan1Kb.equals(this.greaterThan1Kb))) &&
            ((reportPayload.consistency == null && this.consistency == null) || (reportPayload.consistency != null && reportPayload.consistency.equals(this.consistency))) &&
            ((reportPayload.databaseName == null && this.databaseName == null) || (reportPayload.databaseName != null && reportPayload.databaseName.equals(this.databaseName))) &&
            ((reportPayload.containerName == null && this.containerName == null) || (reportPayload.containerName != null && reportPayload.containerName.equals(this.containerName))) &&
            ((reportPayload.operation == null && this.operation == null) || (reportPayload.operation != null && reportPayload.operation.equals(this.operation))) &&
            ((reportPayload.resource == null && this.resource == null) || (reportPayload.resource != null && reportPayload.resource.equals(this.resource))) &&
            ((reportPayload.statusCode == null && this.statusCode == null) || (reportPayload.statusCode != null && reportPayload.statusCode.equals(this.statusCode))) &&
            ((reportPayload.metricInfo.getMetricsName() == null && this.metricInfo.getMetricsName() == null) || (reportPayload.metricInfo.getMetricsName() != null && reportPayload.metricInfo.getMetricsName().equals(this.metricInfo.getMetricsName())))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (hash * 397) ^ (this.regionsContacted == null ? 0 : this.regionsContacted.hashCode());
        hash = (hash * 397) ^ (this.greaterThan1Kb == null ? 0 : this.greaterThan1Kb.hashCode());
        hash = (hash * 397) ^ (this.consistency == null ? 0 : this.consistency.hashCode());
        hash = (hash * 397) ^ (this.databaseName == null ? 0 : this.databaseName.hashCode());
        hash = (hash * 397) ^ (this.containerName == null ? 0 : this.containerName.hashCode());
        hash = (hash * 397) ^ (this.operation == null ? 0 : this.operation.hashCode());
        hash = (hash * 397) ^ (this.resource == null ? 0 : this.resource.hashCode());
        hash = (hash * 397) ^ (this.metricInfo == null ? 0 : this.metricInfo.getMetricsName() == null ? 0 :
            this.metricInfo.getMetricsName().hashCode());
        return hash;
    }
}
