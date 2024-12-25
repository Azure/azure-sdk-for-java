// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry;

import io.clientcore.core.implementation.telemetry.LibraryTelemetryOptionsAccessHelper;

import java.util.Objects;

/**
 * Options for configuring library-specific telemetry settings.
 * <p>
 *
 * It's provided by the client library and is not intended to be used directly by the end users.
 */
public final class LibraryTelemetryOptions {
    private final String libraryName;
    private String libraryVersion;
    private String schemaUrl;
    private boolean disableSpanSuppression;

    static {
        LibraryTelemetryOptionsAccessHelper
            .setAccessor(new LibraryTelemetryOptionsAccessHelper.LibraryTelemetryOptionsAccessor() {
                @Override
                public LibraryTelemetryOptions disableSpanSuppression(LibraryTelemetryOptions options) {
                    return options.disableSpanSuppression(true);
                }

                @Override
                public boolean isSpanSuppressionDisabled(LibraryTelemetryOptions options) {
                    return options.isSpanSuppressionDisabled();
                }
            });
    }

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
     * Gets the schema URL describing specific schema and version of the telemetry
     * the library emits.
     *
     * @return The schema URL.
     */
    public String getSchemaUrl() {
        return schemaUrl;
    }

    LibraryTelemetryOptions disableSpanSuppression(boolean disableSpanSuppression) {
        this.disableSpanSuppression = disableSpanSuppression;
        return this;
    }

    boolean isSpanSuppressionDisabled() {
        return disableSpanSuppression;
    }
}
