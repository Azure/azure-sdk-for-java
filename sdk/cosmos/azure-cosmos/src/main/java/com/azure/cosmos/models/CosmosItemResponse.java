// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * The type Cosmos item response. This contains the item and response methods
 *
 * @param <T> the type parameter
 */
public class CosmosItemResponse<T> {
    private final Class<T> itemClassType;
    private final CosmosItemSerializer itemSerializer;

    //  Converting item to volatile to fix Double-checked locking - https://en.wikipedia.org/wiki/Double-checked_locking
    //  http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    private volatile T item;
    private volatile JsonNode itemBodyOverride;
    final ResourceResponse<Document> resourceResponse;
    private InternalObjectNode props;

    private final AtomicBoolean hasTrackingIdCalculated = new AtomicBoolean(false);

    private boolean hasTrackingId;

    private final Supplier<Boolean> hasPayload;

    CosmosItemResponse(ResourceResponse<Document> response, Class<T> classType, CosmosItemSerializer itemSerializer) {
        this.itemClassType = classType;
        this.resourceResponse = response;
        this.itemSerializer = itemSerializer;
        this.item = null;
        this.hasPayload = response::hasPayload;
        this.itemBodyOverride = null;
    }

    private CosmosItemResponse(ResourceResponse<Document> response, T item, JsonNode itemBodyOverride, Class<T> classType, CosmosItemSerializer itemSerializer) {
        this.itemClassType = classType;
        this.resourceResponse = response;
        this.itemSerializer = itemSerializer;
        this.item = item;
        this.itemBodyOverride = itemBodyOverride;
        boolean hasPayloadStaticValue = item != null;
        this.hasPayload = () -> hasPayloadStaticValue;
    }

    /**
     * Gets the resource.
     *
     * @return the resource
     */
    @SuppressWarnings("unchecked") // Casting getProperties() to T is safe given T is of InternalObjectNode.
    private byte[] getItemAsByteArray() {
        if (item != null && this.itemClassType == Utils.byteArrayClass) {
            return (byte[])item;
        }

        JsonNode effectiveJson = this.itemBodyOverride != null
            ? this.itemBodyOverride
            : this.resourceResponse.getBody();

        if (effectiveJson == null) {
            return null;
        }

        return effectiveJson.toString().getBytes(StandardCharsets.UTF_8);
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

        if (item == null) {
            synchronized (this) {
                if (item == null && hasPayload.get()) {
                    if (this.itemClassType == Utils.byteArrayClass) {
                        Instant serializationStartTime = Instant.now();
                        JsonNode json = this.resourceResponse.getBody();
                        item = (T) json.toString().getBytes(StandardCharsets.UTF_8);
                        Instant serializationEndTime = Instant.now();
                        SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                            serializationStartTime,
                            serializationEndTime,
                            SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION
                        );
                        serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
                        return item;
                    } else if (this.itemClassType == String.class) {
                        Instant serializationStartTime = Instant.now();
                        JsonNode json = this.resourceResponse.getBody();
                        item = (T) json.toString();
                        Instant serializationEndTime = Instant.now();
                        SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                            serializationStartTime,
                            serializationEndTime,
                            SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION
                        );
                        serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
                        return item;
                    } else if (this.itemClassType == InternalObjectNode.class) {
                        Instant serializationStartTime = Instant.now();
                        item = (T) getProperties();
                        Instant serializationEndTime = Instant.now();
                        SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                            serializationStartTime,
                            serializationEndTime,
                            SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION
                        );
                        serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
                        return item;
                    } else {
                        Instant serializationStartTime = Instant.now();
                        item = Utils.parse((ObjectNode)this.resourceResponse.getBody(), itemClassType, itemSerializer);

                        Instant serializationEndTime = Instant.now();
                        SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                            serializationStartTime,
                            serializationEndTime,
                            SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION
                        );
                        serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
                    }

                    return item;
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

    int getResponsePayloadLength() {
        return this.resourceResponse.getResponsePayloadLength();
    }

    private void ensureInternalObjectNodeInitialized() {
        synchronized (this) {
            if (!this.resourceResponse.hasPayload()) {
                props = null;
            } else {
                props = new InternalObjectNode((ObjectNode)this.resourceResponse.getBody());
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

        T payload = null;
        JsonNode itemBodyOverride = null;
        if (isContentResponseOnWriteEnabled) {
            payload = this.getItem();
            itemBodyOverride = this.itemBodyOverride;
        }

        return new CosmosItemResponse<>(
            mappedResourceResponse, payload, itemBodyOverride, this.itemClassType, this.itemSerializer);
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
                public <T> CosmosItemResponse<T> createCosmosItemResponse(CosmosItemResponse<byte[]> response,
                                                                          Class<T> classType,
                                                                          CosmosItemSerializer serializer) {
                    return new CosmosItemResponse<>(
                        response.resourceResponse,
                        Utils.parse(response.getItemAsByteArray(), classType, serializer),
                        response.itemBodyOverride,
                        classType,
                        serializer);
                }

                @Override
                public <T> CosmosItemResponse<T> createCosmosItemResponse(ResourceResponse<Document> response, Class<T> classType, CosmosItemSerializer serializer) {
                    return new CosmosItemResponse<>(
                        response,
                        classType,
                        serializer);
                }

                @Override
                public <T> CosmosItemResponse<T> withRemappedStatusCode(CosmosItemResponse<T> originalResponse,
                                                                        int newStatusCode,
                                                                        double additionalRequestCharge,
                                                                        boolean isContentResponseOnWriteEnabled) {

                    return originalResponse
                        .withRemappedStatusCode(newStatusCode, additionalRequestCharge, isContentResponseOnWriteEnabled);
                }

                public byte[] getByteArrayContent(CosmosItemResponse<byte[]> response) {
                    return response.getItemAsByteArray();
                }

                public void setByteArrayContent(CosmosItemResponse<byte[]> response, Pair<byte[], JsonNode> content) {
                    response.item = content.getLeft();
                    response.itemBodyOverride = content.getRight();
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
