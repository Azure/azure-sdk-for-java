// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * A long-running create or delete operation on an External Key Manager (EKM) proxy private endpoint.
 */
@Immutable
public final class KeyVaultEkmPrivateEndpointOperation {
    private final String operationId;
    private final String privateEndpointName;
    private final KeyVaultEkmPrivateEndpointOperationType operationType;
    private final KeyVaultEkmPrivateEndpointOperationStatus status;
    private final String statusDetails;
    private final KeyVaultAdministrationError error;
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    /**
     * Creates an object containing the details of a {@link KeyVaultEkmPrivateEndpointOperation}.
     *
     * @param operationId The identifier of the operation.
     * @param privateEndpointName The name of the private endpoint the operation applies to.
     * @param operationType The type of the operation.
     * @param status The status of the operation.
     * @param statusDetails The status details of the operation.
     * @param error The error encountered, if any, during the operation.
     * @param startTime The start time of the operation.
     * @param endTime The end time of the operation.
     */
    public KeyVaultEkmPrivateEndpointOperation(String operationId, String privateEndpointName,
        KeyVaultEkmPrivateEndpointOperationType operationType, KeyVaultEkmPrivateEndpointOperationStatus status,
        String statusDetails, KeyVaultAdministrationError error, OffsetDateTime startTime, OffsetDateTime endTime) {
        this.operationId = operationId;
        this.privateEndpointName = privateEndpointName;
        this.operationType = operationType;
        this.status = status;
        this.statusDetails = statusDetails;
        this.error = error;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Get the identifier of the operation. It can be used to retrieve the operation's status later.
     *
     * @return The operation identifier.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Get the name of the private endpoint the operation applies to.
     *
     * @return The private endpoint name.
     */
    public String getPrivateEndpointName() {
        return privateEndpointName;
    }

    /**
     * Get the type of the operation.
     *
     * @return The operation type.
     */
    public KeyVaultEkmPrivateEndpointOperationType getOperationType() {
        return operationType;
    }

    /**
     * Get the status of the operation.
     *
     * @return The operation status.
     */
    public KeyVaultEkmPrivateEndpointOperationStatus getStatus() {
        return status;
    }

    /**
     * Get the status details of the operation.
     *
     * @return The status details.
     */
    public String getStatusDetails() {
        return statusDetails;
    }

    /**
     * Get the error encountered, if any, during the operation.
     *
     * @return The error, or {@code null} if no error occurred.
     */
    public KeyVaultAdministrationError getError() {
        return error;
    }

    /**
     * Get the start time of the operation, in UTC.
     *
     * @return The start time.
     */
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    /**
     * Get the end time of the operation, in UTC.
     *
     * @return The end time, or {@code null} if the operation has not finished.
     */
    public OffsetDateTime getEndTime() {
        return endTime;
    }
}
