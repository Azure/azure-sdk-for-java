// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Phone Number Admin Service supported by this client library.
 */
public enum PhoneNumbersServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2021-03-07}.
     */
    V2021_03_07("2021-03-07"),

    /**
     * Number Lookup GA {@code 2025-02-11}
     */
    V2025_02_11("2025-02-11"),

    /**
     * Cherry Picker GA {@code 2025-04-01}
     */
    V2025_04_01("2025-04-01"),

    /**
     * Mobile Numbers GA {@code 2025-06-01}
     */
    V2025_06_01("2025-06-01");

    private final String version;

    PhoneNumbersServiceVersion(String version) {

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
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link PhoneNumbersServiceVersion}
     */
    public static PhoneNumbersServiceVersion getLatest() {

        return V2025_06_01;
    }
}
