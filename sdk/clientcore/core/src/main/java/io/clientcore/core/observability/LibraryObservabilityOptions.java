package io.clientcore.core.observability;

import java.util.Objects;

public final class LibraryObservabilityOptions {
    private final String libraryName;
    private String libraryVersion;
    private String schemaUrl;

    /**
     * Creates an instance of {@link LibraryObservabilityOptions}.
     *
     * @param libraryName The client library name.
     */
    public LibraryObservabilityOptions(String libraryName) {
        this.libraryName = Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");
    }

    /**
     * Sets the client library version.
     *
     * @param libraryVersion The client library version.
     * @return The updated {@link LibraryObservabilityOptions} object.
     */
    public LibraryObservabilityOptions setLibraryVersion(String libraryVersion) {
        this.libraryVersion = libraryVersion;
        return this;
    }

    /**
     * Sets the schema URL describing specific schema and version of the telemetry
     * the library emits.
     *
     * @param schemaUrl The schema URL.
     * @return The updated {@link LibraryObservabilityOptions} object.
     */
    public LibraryObservabilityOptions setSchemaUrl(String schemaUrl) {
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
}
