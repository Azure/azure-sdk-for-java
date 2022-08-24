// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;


import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Storage File supported by this client library.
 */
public enum ShareServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2019-02-02}.
     */
    V2019_02_02("2019-02-02"),

    /**
     * Service version {@code 2019-07-07}.
     */
    V2019_07_07("2019-07-07"),

    /**
     * Service version {@code 2019-12-12}.
     */
    V2019_12_12("2019-12-12"),

    /**
     * Service version {@code 2020-02-10}.
     */
    V2020_02_10("2020-02-10"),

    /**
     * Service version {@code 2020-04-08}.
     */
    V2020_04_08("2020-04-08"),

    /**
     * Service version {@code 2020-06-12}.
     */
    V2020_06_12("2020-06-12"),

    /**
     * Service version {@code 2020-08-04}.
     */
    V2020_08_04("2020-08-04"),

    /**
     * Service version {@code 2020-10-02}.
     */
    V2020_10_02("2020-10-02"),

    /**
     * Service version {@code 2020-12-06}.
     */
    V2020_12_06("2020-12-06"),

    /**
     * Service version {@code 2021-02-12}.
     */
    V2021_02_12("2021-02-12"),

    /**
     * Service version {@code 2021-04-10}.
     */
    V2021_04_10("2021-04-10"),

    /**
     * Service version {@code 2021-06-08}.
     */
    V2021_06_08("2021-06-08"),

    /**
     * Service version {@code 2021-08-06}.
     */
    V2021_08_06("2021-08-06"),

    /**
     * Service version {@code 2021-10-04}.
     */
    V2021_10_04("2021-10-04");

    private final String version;

    ShareServiceVersion(String version) {
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
     * @return the latest {@link ShareServiceVersion}
     */
    public static ShareServiceVersion getLatest() {
        return V2021_10_04;
    }
}
