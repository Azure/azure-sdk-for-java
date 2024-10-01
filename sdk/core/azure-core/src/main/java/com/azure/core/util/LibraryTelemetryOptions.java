// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.metrics.MeterProvider;
import com.azure.core.util.tracing.TracerProvider;

import java.util.Objects;

/**
 * The options to configure library-specific information on {@link TracerProvider}
 * and {@link MeterProvider}.
 */
@Fluent
public final class LibraryTelemetryOptions {
    private final String libraryName;
    private String libraryVersion;
    private String rpNamespace;
    private String schemaUrl;

    /**
     * Creates an instance of {@link LibraryTelemetryOptions}.
     *
     * @param libraryName The client library name.
     */
    public LibraryTelemetryOptions(String libraryName) {
        this.libraryName = Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");
    }

    /**
     * Sets the client library version.
     *
     * @param libraryVersion The client library version.
     * @return The updated {@link LibraryTelemetryOptions} object.
     */
    public LibraryTelemetryOptions setLibraryVersion(String libraryVersion) {
        this.libraryVersion = libraryVersion;
        return this;
    }

    /**
     * Sets the Azure namespace.
     *
     * @param rpNamespace The Azure Resource Provider namespace client library communicates with.
     * @return The updated {@link LibraryTelemetryOptions} object.
     */
    public LibraryTelemetryOptions setResourceProviderNamespace(String rpNamespace) {
        this.rpNamespace = rpNamespace;
        return this;
    }

    /**
     * Sets the schema URL describing specific schema and version of the telemetry
     * the library emits.
     *
     * @param schemaUrl The schema URL.
     * @return The updated {@link LibraryTelemetryOptions} object.
     */
    public LibraryTelemetryOptions setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
        return this;
    }

    /**
     * Gets the client library name.
     *
     * @return The client library name.
     */
    public String getLibraryName() {
        return libraryName;
    }

    /**
     * Gets the client library version.
     *
     * @return The client library version.
     */
    public String getLibraryVersion() {
        return libraryVersion;
    }

    /**
     * Gets the Azure Resource Provider namespace.
     *
     * @return The Azure Resource Provider  namespace.
     */
    public String getResourceProviderNamespace() {
        return rpNamespace;
    }

    /**
     * Gets the schema URL describing specific schema and version of the telemetry
     * the library emits.
     *
     * @return The schema URL.
     */
    public String getSchemaUrl() {
        return schemaUrl;
    }
}
