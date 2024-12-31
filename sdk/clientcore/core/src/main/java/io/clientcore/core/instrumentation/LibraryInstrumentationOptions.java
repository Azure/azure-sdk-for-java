// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.implementation.instrumentation.LibraryInstrumentationOptionsAccessHelper;

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
 * these properties SHOULD follow specific version of <a href="https://github.com/open-telemetry/semantic-conventions">OpenTelemetry Semantic Conventions</a>.
 * And provide the corresponding schema URL.
 * <p>
 * The {@link LibraryInstrumentationOptions} are usually static and shared across all instances of the client.
 * Application developers are not expected to change them.
 */
public final class LibraryInstrumentationOptions {
    private final String libraryName;
    private String libraryVersion;
    private String schemaUrl;
    private boolean disableSpanSuppression;

    static {
        LibraryInstrumentationOptionsAccessHelper
            .setAccessor(new LibraryInstrumentationOptionsAccessHelper.LibraryInstrumentationOptionsAccessor() {
                @Override
                public LibraryInstrumentationOptions disableSpanSuppression(LibraryInstrumentationOptions options) {
                    return options.disableSpanSuppression(true);
                }

                @Override
                public boolean isSpanSuppressionDisabled(LibraryInstrumentationOptions options) {
                    return options.isSpanSuppressionDisabled();
                }
            });
    }

    /**
     * Creates an instance of {@link LibraryInstrumentationOptions}.
     *
     * @param libraryName The client library name.
     */
    public LibraryInstrumentationOptions(String libraryName) {
        this.libraryName = Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");
    }

    /**
     * Sets the client library version.
     *
     * @param libraryVersion The client library version.
     * @return The updated {@link LibraryInstrumentationOptions} object.
     */
    public LibraryInstrumentationOptions setLibraryVersion(String libraryVersion) {
        this.libraryVersion = libraryVersion;
        return this;
    }

    /**
     * Sets the schema URL describing specific schema and version of the telemetry
     * the library emits.
     *
     * @param schemaUrl The schema URL.
     * @return The updated {@link LibraryInstrumentationOptions} object.
     */
    public LibraryInstrumentationOptions setSchemaUrl(String schemaUrl) {
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

    LibraryInstrumentationOptions disableSpanSuppression(boolean disableSpanSuppression) {
        this.disableSpanSuppression = disableSpanSuppression;
        return this;
    }

    boolean isSpanSuppressionDisabled() {
        return disableSpanSuppression;
    }
}
