// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.Utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * The type Cosmos item response. This contains the item and response methods
 *
 * @param <T> the type parameter
 */
public class CosmosItemResponse<T> {
    private final Class<T> itemClassType;
    private final byte[] responseBodyAsByteArray;
    private T item;
    private final ResourceResponse<Document> resourceResponse;
    private InternalObjectNode props;

    CosmosItemResponse(ResourceResponse<Document> response, Class<T> classType) {
        this.itemClassType = classType;
        this.responseBodyAsByteArray = response.getBodyAsByteArray();
        this.resourceResponse = response;
    }

    /**
     * Gets the resource.
     *
     * @return the resource
     */
    @SuppressWarnings("unchecked") // Casting getProperties() to T is safe given T is of InternalObjectNode.
    public T getItem() {
        if (item != null) {
            return item;
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(this.getDiagnostics());
        if (item == null && this.itemClassType == InternalObjectNode.class) {
            Instant serializationStartTime = Instant.now();
            item =(T) getProperties();
            Instant serializationEndTime = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTime,
                serializationEndTime,
                SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION
            );
            serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
            return item;
        }

        if (item == null) {
            synchronized (this) {
                if (item == null && !Utils.isEmpty(responseBodyAsByteArray)) {
                    Instant serializationStartTime = Instant.now();
                    item = Utils.parse(responseBodyAsByteArray, itemClassType);
                    Instant serializationEndTime = Instant.now();
                    SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                        serializationStartTime,
                        serializationEndTime,
                        SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION
                    );
                    serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
                }
            }
        }

        return item;
    }

    /**
     * Gets the itemProperties
     *
     * @return the itemProperties
     */
    InternalObjectNode getProperties() {
        ensureInternalObjectNodeInitialized();
        return props;
    }

    private void ensureInternalObjectNodeInitialized() {
        synchronized (this) {
            if (Utils.isEmpty(responseBodyAsByteArray)) {
                props = null;
            } else {
                props = new InternalObjectNode(responseBodyAsByteArray);
            }

        }
    }

    /**
     * Gets the maximum size limit for this entity (in megabytes (MB) for server resources and in count for master
     * resources).
     *
     * @return the max resource quota.
     */
    public String getMaxResourceQuota() {
        return resourceResponse.getMaxResourceQuota();
    }

    /**
     * Gets the current size of this entity (in megabytes (MB) for server resources and in count for master resources)
     *
     * @return the current resource quota usage.
     */
    public String getCurrentResourceQuotaUsage() {
        return resourceResponse.getCurrentResourceQuotaUsage();
    }

    /**
     * Gets the Activity ID for the request.
     *
     * @return the activity getId.
     */
    public String getActivityId() {
        return resourceResponse.getActivityId();
    }

    /**
     * Gets the request charge as request units (RU) consumed by the operation.
     * <p>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
        return resourceResponse.getRequestCharge();
    }

    /**
     * Gets the HTTP status code associated with the response.
     *
     * @return the status code.
     */
    public int getStatusCode() {
        return resourceResponse.getStatusCode();
    }

    /**
     * Gets the token used for managing client's consistency requirements.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return resourceResponse.getSessionToken();
    }

    /**
     * Gets the headers associated with the response.
     *
     * @return the response headers.
     */
    public Map<String, String> getResponseHeaders() {
        return resourceResponse.getResponseHeaders();
    }

    /**
     * Gets the diagnostics information for the current request to Azure Cosmos DB service.
     *
     * @return diagnostics information for the current request to Azure Cosmos DB service.
     */
    public CosmosDiagnostics getDiagnostics() {
        return resourceResponse.getDiagnostics();
    }

    /**
     * Gets the end-to-end request latency for the current request to Azure Cosmos DB service.
     *
     * @return end-to-end request latency for the current request to Azure Cosmos DB service.
     */
    public Duration getDuration() {
        return resourceResponse.getDuration();
    }

    /**
     * Gets the ETag from the response headers.
     * This is only relevant when getting response from the server.
     *
     * Null in case of delete operation.
     *
     * @return ETag
     */
    public String getETag() {
        return resourceResponse.getETag();
    }
}
