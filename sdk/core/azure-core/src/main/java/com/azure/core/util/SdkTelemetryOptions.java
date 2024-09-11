// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.tracing.TracerProvider;

/**
 * The options to configure library-specific information on {@link TracerProvider}
 */
@Fluent
public class SdkTelemetryOptions {
    private String sdkName;
    private String sdkVersion;
    private String rpNamespace;
    private String schemaUrl;

    /**
     * Creates an instance of {@link SdkTelemetryOptions}.
     */
    public SdkTelemetryOptions() {
    }

    /**
     * Sets the client library name.
     *
     * @param sdkName The client library name.
     * @return The updated {@link SdkTelemetryOptions} object.
     */
    public SdkTelemetryOptions setSdkName(String sdkName) {
        this.sdkName = sdkName;
        return this;
    }

    /**
     * Sets the client library version.
     *
     * @param sdkVersion The client library version.
     * @return The updated {@link SdkTelemetryOptions} object.
     */
    public SdkTelemetryOptions setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
        return this;
    }

    /**
     * Sets the Azure namespace.
     *
     * @param rpNamespace The Azure Resource Provider namespace client library communicates with.
     * @return The updated {@link SdkTelemetryOptions} object.
     */
    public SdkTelemetryOptions setResourceProviderNamespace(String rpNamespace) {
        this.rpNamespace = rpNamespace;
        return this;
    }

    /**
     * Sets the schema URL describing specific schema and version of the telemetry
     * the library emits.
     *
     * @param schemaUrl The schema URL.
     * @return The updated {@link SdkTelemetryOptions} object.
     */
    public SdkTelemetryOptions setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
        return this;
    }

    /**
     * Gets the client library name.
     *
     * @return The client library name.
     */
    public String getSdkName() {
        return sdkName;
    }

    /**
     * Gets the client library version.
     *
     * @return The client library version.
     */
    public String getSdkVersion() {
        return sdkVersion;
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
