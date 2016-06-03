/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * Data Lake Store filesystem exception based on the WebHDFS definition for
 * RemoteExceptions.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "exception")
@JsonTypeName("AdlsRemoteException")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "IllegalArgumentException", value = AdlsIllegalArgumentException.class),
    @JsonSubTypes.Type(name = "UnsupportedOperationException", value = AdlsUnsupportedOperationException.class),
    @JsonSubTypes.Type(name = "SecurityException", value = AdlsSecurityException.class),
    @JsonSubTypes.Type(name = "IOException", value = AdlsIOException.class),
    @JsonSubTypes.Type(name = "FileNotFoundException", value = AdlsFileNotFoundException.class),
    @JsonSubTypes.Type(name = "RuntimeException", value = AdlsRuntimeException.class),
    @JsonSubTypes.Type(name = "AccessControlException", value = AdlsAccessControlException.class)
})
public class AdlsRemoteException {
    /**
     * Gets the full class package name for the exception thrown, such as
     * 'java.lang.IllegalArgumentException'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String javaClassName;

    /**
     * Gets the message associated with the exception that was thrown, such as
     * 'Invalid value for webhdfs parameter "permission":...'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * Get the javaClassName value.
     *
     * @return the javaClassName value
     */
    public String javaClassName() {
        return this.javaClassName;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String message() {
        return this.message;
    }

}
