// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.util;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility to create the application/json-patch+json operations payload required for update operations.
 */
@Fluent
public class UpdateOperationUtility {
    private static final String ADD = "add";
    private static final String REPLACE = "replace";
    private static final String REMOVE = "remove";

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<UpdateOperation> operations = new ArrayList<>();

    /**
     * Gets the application/json-patch+json operations payload required for update operations.
     * @return The application/json-patch+json operations payload required for update operations.
     */
    public List<Object> getUpdateOperations() {
        return operations.stream().map(op -> mapper.convertValue(op, Object.class)).collect(Collectors.toList());
    }

    /**
     * Include an add operation.
     * @param path The path to the property to be added.
     * @param value The value to update to.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendAddOperation(String path, Object value) {
        operations.add(
            new UpdateOperation()
                .setOperation(ADD)
                .setPath(path)
                .setValue(value));

        return this;
    }

    /**
     * Include a replace operation.
     * @param path The path to the property to be updated.
     * @param value The value to update to.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendReplaceOperation(String path, Object value) {
        operations.add(
            new UpdateOperation()
                .setOperation(REPLACE)
                .setPath(path)
                .setValue(value));

        return this;
    }

    /**
     * Include a remove operation.
     * @param path The path to the property to be added.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendRemoveOperation(String path) {
        operations.add(
            new UpdateOperation()
                .setOperation(REMOVE)
                .setPath(path));

        return this;
    }

    @Fluent
    static class UpdateOperation {
        @JsonProperty(value = "op")
        private String operation;

        @JsonProperty(value = "path")
        private String path;

        @JsonProperty(value = "value")
        private Object value;

        public String getOperation() {
            return operation;
        }

        public UpdateOperation setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        public String getPath() {
            return path;
        }

        public UpdateOperation setPath(String path) {
            this.path = path;
            return this;
        }

        public Object getValue() {
            return value;
        }

        public UpdateOperation setValue(Object value) {
            this.value = value;
            return this;
        }
    }

}
