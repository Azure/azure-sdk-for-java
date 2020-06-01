// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.models;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosItemResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Internal implementation of {@link com.azure.cosmos.models.CosmosItemResponse}.
 *
 * @param <T> The type parameter.
 */
public class CosmosItemResponseImpl<T> implements CosmosItemResponse<T> {
    private final Class<T> itemClassType;
    private final JsonSerializer jsonSerializer;
    private final byte[] responseBodyAsByteArray;
    private T item;
    private boolean itemSet;
    private final ResourceResponse<Document> resourceResponse;
    private CosmosItemProperties props;
    private boolean propsSet;

    private CosmosItemResponseImpl(ResourceResponse<Document> response, Class<T> classType,
        JsonSerializer jsonSerializer) {
        this.itemClassType = classType;
        this.jsonSerializer = jsonSerializer;
        this.responseBodyAsByteArray = response.getBodyAsByteArray();
        this.resourceResponse = response;
    }

    public static <T> CosmosItemResponse<T> fromResponse(ResourceResponse<Document> response, Class<T> classType,
        JsonSerializer jsonSerializer) {
        return new CosmosItemResponseImpl<>(response, classType, jsonSerializer);
    }

    public static CosmosItemProperties getProperties(CosmosItemResponse<?> itemResponse) {
        return ((CosmosItemResponseImpl<?>) itemResponse).getProperties();
    }

    @Override
    @SuppressWarnings("unchecked") // Casting getProperties() to T is safe given T is of CosmosItemProperties.
    public T getItem() {
        synchronized (this) {
            if (itemSet) {
                return item;
            }

            itemSet = true;
            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal
                .getSerializationDiagnosticsContext(this.getDiagnostics());


            if (this.itemClassType == CosmosItemProperties.class) {
                Instant serializationStartTime = Instant.now();
                item = (T) getProperties();
                Instant serializationEndTime = Instant.now();
                SerializationDiagnosticsContext.SerializationDiagnostics diagnostics =
                    new SerializationDiagnosticsContext.SerializationDiagnostics(serializationStartTime,
                        serializationEndTime,
                        SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION);
                serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
                return item;
            } else if (!Utils.isEmpty(responseBodyAsByteArray)) {
                Instant serializationStartTime = Instant.now();
                item = jsonSerializer.deserialize(responseBodyAsByteArray, itemClassType);
                Instant serializationEndTime = Instant.now();
                SerializationDiagnosticsContext.SerializationDiagnostics diagnostics =
                    new SerializationDiagnosticsContext.SerializationDiagnostics(serializationStartTime,
                        serializationEndTime,
                        SerializationDiagnosticsContext.SerializationType.ITEM_DESERIALIZATION);
                serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
            }

            return item;
        }
    }

    /**
     * Gets the itemProperties
     *
     * @return the itemProperties
     */
    public CosmosItemProperties getProperties() {
        synchronized (this) {
            if (!propsSet) {
                propsSet = true;
                if (Utils.isEmpty(responseBodyAsByteArray)) {
                    props = null;
                } else {
                    props = new CosmosItemProperties(responseBodyAsByteArray);
                }
            }

            return props;
        }
    }

    @Override
    public String getMaxResourceQuota() {
        return resourceResponse.getMaxResourceQuota();
    }

    @Override
    public String getCurrentResourceQuotaUsage() {
        return resourceResponse.getCurrentResourceQuotaUsage();
    }

    @Override
    public String getActivityId() {
        return resourceResponse.getActivityId();
    }

    @Override
    public double getRequestCharge() {
        return resourceResponse.getRequestCharge();
    }

    @Override
    public int getStatusCode() {
        return resourceResponse.getStatusCode();
    }

    @Override
    public String getSessionToken() {
        return resourceResponse.getSessionToken();
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        return resourceResponse.getResponseHeaders();
    }

    @Override
    public CosmosDiagnostics getDiagnostics() {
        return resourceResponse.getDiagnostics();
    }

    @Override
    public Duration getDuration() {
        return resourceResponse.getDuration();
    }

    @Override
    public String getETag() {
        return resourceResponse.getETag();
    }
}
