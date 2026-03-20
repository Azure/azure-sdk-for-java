// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Defines the set of additional headers that can be set on a {@link CosmosClientBuilder}
 * via {@link CosmosClientBuilder#additionalHeaders(java.util.Map)}.
 * <p>
 * Only headers with RNTBD encoding support are included, ensuring consistent
 * behavior across both Gateway mode (HTTP) and Direct mode (RNTBD binary protocol).
 * <p>
 * This class uses the non-exhaustive final class pattern (rather than Java enum) for
 * binary compatibility when new header names are added in future releases. See
 * <a href="https://azure.github.io/azure-sdk/java_introduction.html#enumerations">Azure SDK Java Guidelines — Enumerations</a>.
 */
public final class CosmosHeaderName {

    private final String headerName;

    private CosmosHeaderName(String headerName) {
        checkNotNull(headerName, "Argument 'headerName' must not be null.");
        this.headerName = headerName;
    }

    /**
     * The workload ID header ({@code x-ms-cosmos-workload-id}).
     * <p>
     * Valid values: a string representation of an integer (e.g., {@code "15"}).
     * The service accepts values in the range 1–50 for Azure Monitor metrics attribution.
     * The SDK validates that the value is a valid integer but does not enforce range limits —
     * range validation is the backend's responsibility.
     */
    public static final CosmosHeaderName WORKLOAD_ID = new CosmosHeaderName(
        HttpConstants.HttpHeaders.WORKLOAD_ID);

    // IMPORTANT: ADDITIONAL_HEADERS must be declared AFTER all public static final fields above,
    // because Java initializes static fields in declaration order. If this map were declared
    // before WORKLOAD_ID, the map would contain null values.
    private static final Map<String, CosmosHeaderName> ADDITIONAL_HEADERS = createAdditionalHeadersMap();

    /**
     * Gets the canonical HTTP header name string (e.g., {@code "x-ms-cosmos-workload-id"}).
     *
     * @return the header name string
     */
    public String getHeaderName() {
        return this.headerName;
    }

    /**
     * Converts a header name string to the corresponding {@link CosmosHeaderName} instance.
     * <p>
     * This is primarily used by the Spark connector, which parses header names from JSON
     * configuration strings and needs to convert them to {@link CosmosHeaderName} instances
     * before calling {@link CosmosClientBuilder#additionalHeaders(java.util.Map)}.
     *
     * @param headerName the header name string (e.g., {@code "x-ms-cosmos-workload-id"})
     * @return the matching {@link CosmosHeaderName}
     * @throws IllegalArgumentException if the header name does not match any known value
     */
    public static CosmosHeaderName fromString(String headerName) {
        checkNotNull(headerName, "Argument 'headerName' must not be null.");

        String normalizedName = headerName.trim().toLowerCase(Locale.ROOT);
        CosmosHeaderName result = ADDITIONAL_HEADERS.getOrDefault(normalizedName, null);

        if (result == null) {
            throw new IllegalArgumentException(
                "Unknown header: '" + headerName + "'. Allowed headers: " + getValidValues());
        }

        return result;
    }

    /**
     * Validates all entries in an additional-headers map.
     * <p>
     * Each {@link CosmosHeaderName} instance carries its own validation rules. Currently:
     * <ul>
     *   <li>{@link #WORKLOAD_ID}: value must be a valid integer string</li>
     * </ul>
     * <p>
     * This method is called by {@link CosmosClientBuilder#additionalHeaders(Map)} and
     * by every request-options class's {@code setAdditionalHeaders} method, so the
     * validation logic lives in one place.
     *
     * @param additionalHeaders the map to validate (may be null — no-op in that case)
     * @throws IllegalArgumentException if any header value fails validation
     */
    public static void validateAdditionalHeaders(Map<CosmosHeaderName, String> additionalHeaders) {
        if (additionalHeaders == null) {
            return;
        }
        for (Map.Entry<CosmosHeaderName, String> entry : additionalHeaders.entrySet()) {
            CosmosHeaderName key = entry.getKey();
            String value = entry.getValue();

            if (WORKLOAD_ID.equals(key) && value != null) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Invalid value '" + value + "' for header '" + key.getHeaderName()
                            + "'. The value must be a valid integer.", e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.headerName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(CosmosHeaderName.class, this.headerName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CosmosHeaderName)) {
            return false;
        }

        CosmosHeaderName other = (CosmosHeaderName) obj;
        return Objects.equals(this.headerName, other.headerName);
    }

    private static Map<String, CosmosHeaderName> createAdditionalHeadersMap() {
        Map<String, CosmosHeaderName> map = new HashMap<>();
        map.put(HttpConstants.HttpHeaders.WORKLOAD_ID.toLowerCase(Locale.ROOT), WORKLOAD_ID);
        return Collections.unmodifiableMap(map);
    }

    private static String getValidValues() {
        StringJoiner sj = new StringJoiner(", ");
        for (CosmosHeaderName header : ADDITIONAL_HEADERS.values()) {
            sj.add(header.headerName);
        }
        return sj.toString();
    }
}
