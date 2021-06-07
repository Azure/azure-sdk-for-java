// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Exception details of the exception in a chain. */
@Fluent
public final class TelemetryExceptionDetails {
    /*
     * In case exception is nested (outer exception contains inner one), the id
     * and outerId properties are used to represent the nesting.
     */
    @JsonProperty(value = "id")
    private Integer id;

    /*
     * The value of outerId is a reference to an element in ExceptionDetails
     * that represents the outer exception
     */
    @JsonProperty(value = "outerId")
    private Integer outerId;

    /*
     * Exception type name.
     */
    @JsonProperty(value = "typeName")
    private String typeName;

    /*
     * Exception message.
     */
    @JsonProperty(value = "message", required = true)
    private String message;

    /*
     * Indicates if full exception stack is provided in the exception. The
     * stack may be trimmed, such as in the case of a StackOverflow exception.
     */
    @JsonProperty(value = "hasFullStack")
    private Boolean hasFullStack;

    /*
     * Text describing the stack. Either stack or parsedStack should have a
     * value.
     */
    @JsonProperty(value = "stack")
    private String stack;

    /*
     * List of stack frames. Either stack or parsedStack should have a value.
     */
    @JsonProperty(value = "parsedStack")
    private List<StackFrame> parsedStack;

    /**
     * Get the id property: In case exception is nested (outer exception contains inner one), the id and outerId
     * properties are used to represent the nesting.
     *
     * @return the id value.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Set the id property: In case exception is nested (outer exception contains inner one), the id and outerId
     * properties are used to represent the nesting.
     *
     * @param id the id value to set.
     * @return the TelemetryExceptionDetails object itself.
     */
    public TelemetryExceptionDetails setId(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Get the outerId property: The value of outerId is a reference to an element in ExceptionDetails that represents
     * the outer exception.
     *
     * @return the outerId value.
     */
    public Integer getOuterId() {
        return this.outerId;
    }

    /**
     * Set the outerId property: The value of outerId is a reference to an element in ExceptionDetails that represents
     * the outer exception.
     *
     * @param outerId the outerId value to set.
     * @return the TelemetryExceptionDetails object itself.
     */
    public TelemetryExceptionDetails setOuterId(Integer outerId) {
        this.outerId = outerId;
        return this;
    }

    /**
     * Get the typeName property: Exception type name.
     *
     * @return the typeName value.
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * Set the typeName property: Exception type name.
     *
     * @param typeName the typeName value to set.
     * @return the TelemetryExceptionDetails object itself.
     */
    public TelemetryExceptionDetails setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    /**
     * Get the message property: Exception message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message property: Exception message.
     *
     * @param message the message value to set.
     * @return the TelemetryExceptionDetails object itself.
     */
    public TelemetryExceptionDetails setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the hasFullStack property: Indicates if full exception stack is provided in the exception. The stack may be
     * trimmed, such as in the case of a StackOverflow exception.
     *
     * @return the hasFullStack value.
     */
    public Boolean isHasFullStack() {
        return this.hasFullStack;
    }

    /**
     * Set the hasFullStack property: Indicates if full exception stack is provided in the exception. The stack may be
     * trimmed, such as in the case of a StackOverflow exception.
     *
     * @param hasFullStack the hasFullStack value to set.
     * @return the TelemetryExceptionDetails object itself.
     */
    public TelemetryExceptionDetails setHasFullStack(Boolean hasFullStack) {
        this.hasFullStack = hasFullStack;
        return this;
    }

    /**
     * Get the stack property: Text describing the stack. Either stack or parsedStack should have a value.
     *
     * @return the stack value.
     */
    public String getStack() {
        return this.stack;
    }

    /**
     * Set the stack property: Text describing the stack. Either stack or parsedStack should have a value.
     *
     * @param stack the stack value to set.
     * @return the TelemetryExceptionDetails object itself.
     */
    public TelemetryExceptionDetails setStack(String stack) {
        this.stack = stack;
        return this;
    }

    /**
     * Get the parsedStack property: List of stack frames. Either stack or parsedStack should have a value.
     *
     * @return the parsedStack value.
     */
    public List<StackFrame> getParsedStack() {
        return this.parsedStack;
    }

    /**
     * Set the parsedStack property: List of stack frames. Either stack or parsedStack should have a value.
     *
     * @param parsedStack the parsedStack value to set.
     * @return the TelemetryExceptionDetails object itself.
     */
    public TelemetryExceptionDetails setParsedStack(List<StackFrame> parsedStack) {
        this.parsedStack = parsedStack;
        return this;
    }
}
