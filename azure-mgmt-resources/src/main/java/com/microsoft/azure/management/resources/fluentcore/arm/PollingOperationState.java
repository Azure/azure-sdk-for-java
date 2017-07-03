/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.PollingState;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * The base type for polling operation state.
 *
 * @param <InnerResultT> polling operation result inner type.
 * @param <ResultT> polling operation result fluent type.
 */
public abstract class PollingOperationState<InnerResultT, ResultT> {
    private final Type innerResourceType;
    private PollingState<InnerResultT> innerPollingState;

    /**
     * Creates  PollingOperationState.
     *
     * @param innerResourceType polling operation result inner java.lang.reflect.Type type
     */
    public PollingOperationState(Type innerResourceType) {
        this.innerResourceType = innerResourceType;
    }

    /**
     * @return the java.lang.reflect.Type of polling operation result inner type
     */
    public Type innerResourceType() {
        return this.innerResourceType;
    }

    /**
     * @return the inner polling state
     */
    public PollingState<InnerResultT> innerPollingState() {
        return this.innerPollingState;
    }

    /**
     * Sets the inner polling state.
     *
     * @param innerPollingState the inner polling state
     */
    public void setInnerPollingState(PollingState<InnerResultT> innerPollingState) {
        this.innerPollingState = innerPollingState;
    }

    /**
     * @return the polling state as json string format
     */
    public String serialize() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this.innerPollingState);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Deserialize the given json string format polling state.
     *
     * @param serializedPollingState the json polling state
     * @return deserialized value
     */
    protected static PollingState<?> deserializePollingState(String serializedPollingState) {
        ObjectMapper mapper = new ObjectMapper();
        PollingState<?> pollingState;
        try {
            pollingState = mapper.readValue(serializedPollingState, PollingState.class);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        return pollingState;
    }

    /**
     * @return the inner result i.e. the result of long running operation
     */
    protected InnerResultT innerResult() {
        if (this.innerPollingState() != null
                && this.innerPollingState().resource() != null) {
            return this.innerPollingState().resource();
        }
        return null;
    }

    /**
     * @return the result of long running operation
     */
    public abstract ResultT result();
}
