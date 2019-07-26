// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Conflict;
import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.Strings;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public class CosmosConflictProperties extends Resource {

    /**
     * Initialize a conflict object.
     */
    CosmosConflictProperties() {
        super();
    }

    /**
     * Initialize a conflict object from json string.
     *
     * @param jsonString the json string that represents the conflict.
     */
    CosmosConflictProperties(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the operation kind.
     *
     * @return the operation kind.
     */
    public String operationKind() {
        return super.getString(Constants.Properties.OPERATION_TYPE);
    }

    /**
     * Gets the type of the conflicting resource.
     *
     * @return the resource type.
     */
    public String resourceType() {
        return super.getString(Constants.Properties.RESOURCE_TYPE);
    }

    /**
     * Gets the resource ID for the conflict in the Azure Cosmos DB service.
     * @return resource Id for the conflict.
     */
    String sourceResourceId() {
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

    static List<CosmosConflictProperties> getFromV2Results(List<Conflict> results) {
        return results.stream().map(conflict -> new CosmosConflictProperties(conflict.toJson()))
                .collect(Collectors.toList());
    }
}