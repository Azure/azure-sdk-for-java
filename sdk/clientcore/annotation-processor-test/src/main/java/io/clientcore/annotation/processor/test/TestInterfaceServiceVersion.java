// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.core.http.models.ServiceVersion;

/**
 * Service version of OpenAIClient.
 */
public enum TestInterfaceServiceVersion implements ServiceVersion {
    /**
     * Enum value 2022-12-01.
     */
    V_TEST_VALUE("test-value");

    private final String version;

    TestInterfaceServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library.
     *
     * @return The latest {@link TestInterfaceServiceVersion}.
     */
    public static TestInterfaceServiceVersion getLatest() {
        return V_TEST_VALUE;
    }
}
