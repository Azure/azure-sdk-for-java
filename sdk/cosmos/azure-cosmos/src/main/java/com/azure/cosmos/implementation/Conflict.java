// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;

/**
 * Represents a conflict in the version of a particular resource in the Azure Cosmos DB database service.
 * <p>
 * During rare failure scenarios, conflicts are generated for the documents in transit. Clients can inspect the
 * respective conflict instances  for resources and operations in conflict.
 */
public final class Conflict extends Resource {
    private final static ObjectMapper mapper = Utils.getSimpleObjectMapper();
    /**
     * Initialize a conflict object.
     */
    public Conflict() {
        super();
    }

    /**
     * Initialize a conflict object from json string.
     *
     * @param jsonString the json string that represents the conflict.
     */
    public Conflict(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the operation kind.
     *
     * @return the operation kind.
     */
    public OperationKind getOperationKind() {
        return OperationKind.fromServiceSerializedFormat(super.getString(Constants.Properties.OPERATION_TYPE));
    }

    /**
     * Gets the type of the conflicting resource.
     *
     * @return the resource type.
     */
    public String getResourceType() {
        return super.getString(Constants.Properties.RESOURCE_TYPE);
    }

    /**
     * Gets the resource ID for the conflict in the Azure Cosmos DB service.
     * @return resource Id for the conflict.
     */
    public String getSourceResourceId() {
        return super.getString(Constants.Properties.SOURCE_RESOURCE_ID);
    }

    /**
     * Gets the conflicting resource in the Azure Cosmos DB service.
     * @param <T> the type of the object.
     * @param klass The returned type of conflicting resource.
     * @return The conflicting resource.
     */
    public <T extends Resource> T getResource(Class<T> klass) {
        String resourceAsString = super.getString(Constants.Properties.CONTENT);

        if (!Strings.isNullOrEmpty(resourceAsString)) {
            try {
                return klass.getConstructor(String.class).newInstance(resourceAsString);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException("Failed to instantiate class object.", e);
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the conflicting resource in the Azure Cosmos DB service.
     * @param <T> the type of the object.
     * @param klass The returned type of conflicting resource.
     * @return The conflicting resource.
     */
    public <T > T getItem(Class<T> klass) {
        String resourceAsString = super.getString(Constants.Properties.CONTENT);
        if (!Strings.isNullOrEmpty(resourceAsString)) {
            try {
                return mapper.readValue(resourceAsString, klass);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("Failed to deserialize  class object.", ex);
            }
        } else {
            return null;
        }
    }
}
