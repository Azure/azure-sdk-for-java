// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.implementation.instrumentation.SdkInstrumentationOptionsAccessHelper;

import java.util.Objects;

/**
 * Options for configuring library-specific telemetry settings.
 *
 * <p><strong>This class is intended to be used by the client libraries only. Library options must not be provided or modified
 * by application code</strong></p>
 *
 * Library options describe the client library - it's name, version, and schema URL.
 * Schema URL describes telemetry schema and version.
 * <p>
 * If your client library adds any attributes (links, events, etc.) to the spans,
 * these properties SHOULD follow specific version of <a href="https://github.com/open-telemetry/semantic-conventions">OpenTelemetry Semantic Conventions</a>
 * and provide the corresponding schema URI.
 * <p>
 * The {@link SdkInstrumentationOptions} are usually static and shared across all instances of the client.
 * Application developers are not expected to change them.
 */
@Metadata(properties = MetadataProperties.FLUENT)
public final class SdkInstrumentationOptions {
    private final String sdkName;
    private String sdkVersion;
    private String schemaUrl;
    private boolean disableSpanSuppression;
    private String serviceEndpoint;

    static {
        SdkInstrumentationOptionsAccessHelper
            .setAccessor(new SdkInstrumentationOptionsAccessHelper.SdkInstrumentationOptionsAccessor() {
                @Override
                public SdkInstrumentationOptions disableSpanSuppression(SdkInstrumentationOptions options) {
                    return options.disableSpanSuppression(true);
                }

                @Override
                public boolean isSpanSuppressionDisabled(SdkInstrumentationOptions options) {
                    return options.isSpanSuppressionDisabled();
                }
            });
    }

    /**
     * Creates an instance of {@link SdkInstrumentationOptions}.
     *
     * @param sdkName The client library name.
     */
    public SdkInstrumentationOptions(String sdkName) {
        this.sdkName = Objects.requireNonNull(sdkName, "'sdkName' cannot be null.");
    }

    /**
     * Sets the client library version.
     *
     * @param sdkVersion The client library version.
     * @return The updated {@link SdkInstrumentationOptions} object.
     */
    public SdkInstrumentationOptions setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
        return this;
    }

    /**
     * Sets the schema URL describing specific schema and version of the telemetry
     * the library emits.
     *
     * @param schemaUrl The schema URL.
     * @return The updated {@link SdkInstrumentationOptions} object.
     */
    public SdkInstrumentationOptions setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
        return this;
    }

    /**
     * Sets the service endpoint.
     *
     * @param endpoint The service endpoint.
     * @return The updated {@link SdkInstrumentationOptions} object.
     */
    public SdkInstrumentationOptions setEndpoint(String endpoint) {
        this.serviceEndpoint = endpoint;
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
     * Gets the schema URL describing specific schema and version of the telemetry
     * the library emits.
     *
     * @return The schema URL.
     */
    public String getSchemaUrl() {
        return schemaUrl;
    }

    /**
     * Gets the service endpoint.
     *
     * @return The service endpoint.
     */
    public String getEndpoint() {
        return serviceEndpoint;
    }

    SdkInstrumentationOptions disableSpanSuppression(boolean disableSpanSuppression) {
        this.disableSpanSuppression = disableSpanSuppression;
        return this;
    }

    boolean isSpanSuppressionDisabled() {
        return disableSpanSuppression;
    }
}
