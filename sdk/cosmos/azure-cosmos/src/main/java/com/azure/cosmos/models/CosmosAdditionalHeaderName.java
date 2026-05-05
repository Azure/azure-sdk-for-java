// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.HttpConstants;

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
 * <a href="https://azure.github.io/azure-sdk/java_introduction.html#enumerations">Azure SDK Java Guidelines - Enumerations</a>.
 */
public final class CosmosAdditionalHeaderName {

    private final String headerName;

    private CosmosAdditionalHeaderName(String headerName) {
        checkNotNull(headerName, "Argument 'headerName' must not be null.");
        this.headerName = headerName;
    }

    /**
     * The workload ID header ({@code x-ms-cosmos-workload-id}).
     * <p>
     * Valid values: a string representation of an integer (e.g., {@code "15"}).
     * The service accepts values in the range 1-50 for Azure Monitor metrics attribution.
     * The SDK validates that the value is a valid integer but does not enforce range limits -
     * range validation is the backend's responsibility.
     */
    public static final CosmosAdditionalHeaderName WORKLOAD_ID = new CosmosAdditionalHeaderName(
        HttpConstants.HttpHeaders.WORKLOAD_ID);

    /**
     * Gets the canonical HTTP header name string (e.g., {@code "x-ms-cosmos-workload-id"}).
     *
     * @return the header name string
     */
    public String getHeaderName() {
        return this.headerName;
    }

    @Override
    public String toString() {
        return this.headerName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(CosmosAdditionalHeaderName.class, this.headerName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CosmosAdditionalHeaderName)) {
            return false;
        }

        CosmosAdditionalHeaderName other = (CosmosAdditionalHeaderName) obj;
        return Objects.equals(this.headerName, other.headerName);
    }
}

