// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Storage Blob supported by this client library.
 */
public enum BlobServiceVersion implements ServiceVersion {
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
    V2021_10_04("2021-10-04"),

    /**
     * Service version {@code 2021-12-02}.
     */
    V2021_12_02("2021-12-02"),

    /**
     * Service version {@code 2022-11-02}.
     */
    V2022_11_02("2022-11-02"),

    /**
     * Service version {@code 2023-01-03}.
     */
    V2023_01_03("2023-01-03"),

    /**
     * Service version {@code 2023-05-03}.
     */
    V2023_05_03("2023-05-03"),

    /**
     * Service version {@code 2023-08-03}.
     */
    V2023_08_03("2023-08-03"),

    /**
     * Service version {@code 2023-11-03}.
     */
    V2023_11_03("2023-11-03"),

    /**
     * Service version {@code 2024-02-04}.
     */
    V2024_02_04("2024-02-04"),

    /**
     * Service version {@code 2024-05-04}.
     */
    V2024_05_04("2024-05-04"),

    /**
     * Service version {@code 2024-08-04}.
     */
    V2024_08_04("2024-08-04"),

    /**
     * Service version {@code 2024-11-04}.
     */
    V2024_11_04("2024-11-04");

    private final String version;

    BlobServiceVersion(String version) {
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
     * @return the latest {@link BlobServiceVersion}
     */
    public static BlobServiceVersion getLatest() {
        return V2024_11_04;
    }
}
