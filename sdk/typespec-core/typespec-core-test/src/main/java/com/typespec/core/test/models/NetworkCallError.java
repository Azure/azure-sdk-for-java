// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.models;

import com.typespec.core.exception.UnexpectedLengthException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.UnknownHostException;

/**
 * This class represents a caught throwable during a network call. It is used to serialize exceptions that were thrown
 * during the pipeline and deserialize them back into their actual throwable class when running in playback mode.
 */
public class NetworkCallError {
    @JsonProperty("ClassName")
    private String className;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    private Throwable throwable;

    /**
     * Empty constructor used by deserialization.
     */
    public NetworkCallError() {
    }

    /**
     * Constructs the class setting the throwable and its class name.
     *
     * @param throwable Throwable thrown during a network call.
     */
    public NetworkCallError(Throwable throwable) {
        this.throwable = throwable;
        this.className = throwable.getClass().getName();
        this.errorMessage = throwable.getMessage();
    }

    /**
     * @return the thrown throwable as the class it was thrown as by converting is using its class name.
     */
    public Throwable get() {
        switch (className) {
            case "java.lang.NullPointerException":
                return new NullPointerException(this.errorMessage);

            case "java.lang.IndexOutOfBoundsException":
                return new IndexOutOfBoundsException(this.errorMessage);

            case "java.net.UnknownHostException":
                return new UnknownHostException(this.errorMessage);

            case "com.azure.core.exception.UnexpectedLengthException":
                return new UnexpectedLengthException(this.errorMessage, 0L, 0L);

            default:
                return throwable;
        }
    }

    /**
     * Sets the throwable that was thrown during a network call.
     *
     * @param throwable Throwable that was thrown.
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * Sets the name of the class of the throwable. This is used during deserialization the construct the throwable
     * as the actual class that was thrown.
     *
     * @param className Class name of the throwable.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Sets the error message of the class of the throwable. This is used during deserialization the construct the
     * throwable as the actual class that was thrown.
     *
     * @param errorMessage Error msg from the exception.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
