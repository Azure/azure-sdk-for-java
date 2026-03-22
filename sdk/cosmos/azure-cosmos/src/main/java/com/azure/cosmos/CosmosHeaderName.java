// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;

import java.util.Map;
import java.util.Objects;

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

    /**
     * Gets the canonical HTTP header name string (e.g., {@code "x-ms-cosmos-workload-id"}).
     *
     * @return the header name string
     */
    public String getHeaderName() {
        return this.headerName;
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
}
