// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;

import java.util.Arrays;
import java.util.Map;

/**
 * Defines the set of additional headers that can be set on a {@link CosmosClientBuilder}
 * via {@link CosmosClientBuilder#additionalHeaders(java.util.Map)}.
 * <p>
 * Only headers with RNTBD encoding support are included in this enum, ensuring consistent
 * behavior across both Gateway mode (HTTP) and Direct mode (RNTBD binary protocol).
 */
public enum CosmosHeaderName {

    /**
     * The workload ID header ({@code x-ms-cosmos-workload-id}).
     * <p>
     * Valid values: a string representation of an integer (e.g., {@code "15"}).
     * The service accepts values in the range 1–50 for Azure Monitor metrics attribution.
     * The SDK validates that the value is a valid integer but does not enforce range limits —
     * range validation is the backend's responsibility.
     */
    WORKLOAD_ID(HttpConstants.HttpHeaders.WORKLOAD_ID);

    private final String headerName;

    CosmosHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * Gets the canonical HTTP header name string (e.g., {@code "x-ms-cosmos-workload-id"}).
     *
     * @return the header name string
     */
    public String getHeaderName() {
        return this.headerName;
    }

    /**
     * Converts a header name string to the corresponding {@link CosmosHeaderName} enum value.
     * <p>
     * This is primarily used by the Spark connector, which parses header names from JSON
     * configuration strings and needs to convert them to enum values before calling
     * {@link CosmosClientBuilder#additionalHeaders(java.util.Map)}.
     *
     * @param headerName the header name string (e.g., {@code "x-ms-cosmos-workload-id"})
     * @return the matching {@link CosmosHeaderName}
     * @throws IllegalArgumentException if the header name does not match any known enum value
     */
    public static CosmosHeaderName fromString(String headerName) {
        for (CosmosHeaderName name : values()) {
            if (name.headerName.equalsIgnoreCase(headerName)) {
                return name;
            }
        }
        throw new IllegalArgumentException(
            "Unknown header: '" + headerName + "'. Allowed headers: " + Arrays.toString(values()));
    }

    /**
     * Validates all entries in an additional-headers map.
     * <p>
     * Each {@link CosmosHeaderName} enum value carries its own validation rules. Currently:
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

            if (WORKLOAD_ID == key && value != null) {
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
}
