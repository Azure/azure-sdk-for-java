// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.Utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * The type Cosmos item response. This contains the item and response methods
 *
 * @param <T> the type parameter
 */
public class CosmosItemResponse<T> {
    private final Class<T> itemClassType;
    private final ItemDeserializer itemDeserializer;
    byte[] responseBodyAsByteArray;
    //  Converting item to volatile to fix Double-checked locking - https://en.wikipedia.org/wiki/Double-checked_locking
    //  http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    private volatile T item;
    final ResourceResponse<Document> resourceResponse;
    private InternalObjectNode props;

    private AtomicBoolean hasTrackingIdCalculated = new AtomicBoolean(false);

    private boolean hasTrackingId;

    CosmosItemResponse(ResourceResponse<Document> response, Class<T> classType, ItemDeserializer itemDeserializer) {
        this(response, response.getBodyAsByteArray(), classType, itemDeserializer);
    }

    CosmosItemResponse(ResourceResponse<Document> response, byte[] responseBodyAsByteArray, Class<T> classType, ItemDeserializer itemDeserializer) {
        this.itemClassType = classType;
        this.responseBodyAsByteArray = responseBodyAsByteArray;
        this.resourceResponse = response;
        this.itemDeserializer = itemDeserializer;
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
                    item = Utils.parse(responseBodyAsByteArray, itemClassType, itemDeserializer);
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

    CosmosItemResponse<T> withRemappedStatusCode(
        int statusCode,
        double additionalRequestCharge,
        boolean isContentResponseOnWriteEnabled) {

        ResourceResponse<Document> mappedResourceResponse =
            this.resourceResponse.withRemappedStatusCode(statusCode, additionalRequestCharge);

        byte[] payload = null;
        if (isContentResponseOnWriteEnabled) {
            payload = this.responseBodyAsByteArray;
        }

        return new CosmosItemResponse<>(
            mappedResourceResponse, payload, this.itemClassType, this.itemDeserializer);
    }

    boolean hasTrackingId(String candidate) {
        if (this.hasTrackingIdCalculated.compareAndSet(false, true)) {
            SerializationDiagnosticsContext serializationDiagnosticsContext =
                BridgeInternal.getSerializationDiagnosticsContext(this.getDiagnostics());
            Instant serializationStartTime = Instant.now();
            InternalObjectNode itemNode = getProperties();
            Instant serializationEndTime = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics diagnostics =
                new SerializationDiagnosticsContext.SerializationDiagnostics(
                    serializationStartTime,
                    serializationEndTime,
                    SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION
                );
            serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);

            return this.hasTrackingId = (itemNode != null && candidate.equals(itemNode.get(Constants.Properties.TRACKING_ID)));
        } else {
            return this.hasTrackingId;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosItemResponseHelper.setCosmosItemResponseBuilderAccessor(
            new ImplementationBridgeHelpers.CosmosItemResponseHelper.CosmosItemResponseBuilderAccessor() {
                public <T> CosmosItemResponse<T> createCosmosItemResponse(ResourceResponse<Document> response,
                                                                          byte[] contentAsByteArray,
                                                                          Class<T> classType,
                                                                          ItemDeserializer itemDeserializer) {
                    return new CosmosItemResponse<>(response, contentAsByteArray, classType, itemDeserializer);
                }

                @Override
                public <T> CosmosItemResponse<T> withRemappedStatusCode(CosmosItemResponse<T> originalResponse,
                                                                        int newStatusCode,
                                                                        double additionalRequestCharge,
                                                                        boolean isContentResponseOnWriteEnabled) {

                    CosmosItemResponse<T> mappedItemResponse = originalResponse
                        .withRemappedStatusCode(newStatusCode, additionalRequestCharge, isContentResponseOnWriteEnabled);
                    return mappedItemResponse;
                }

                public byte[] getByteArrayContent(CosmosItemResponse<byte[]> response) {
                    return response.responseBodyAsByteArray;
                }

                public void setByteArrayContent(CosmosItemResponse<byte[]> response, byte[] content) {
                    response.responseBodyAsByteArray = content;
                }

                public ResourceResponse<Document> getResourceResponse(CosmosItemResponse<byte[]> response) {
                    return response.resourceResponse;
                }

                @Override
                public boolean hasTrackingId(CosmosItemResponse<?> response, String candidate) {
                    checkNotNull(response, "Argument 'response' must not be null.");
                    checkNotNull(candidate, "Argument 'candidate' must not be null.");

                    return response.hasTrackingId(candidate);
                }
            });
    }

    static { initialize(); }
}
