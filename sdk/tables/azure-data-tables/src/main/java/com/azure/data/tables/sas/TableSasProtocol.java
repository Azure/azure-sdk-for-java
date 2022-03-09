// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.sas;

import com.azure.data.tables.implementation.StorageConstants;

import java.util.Locale;

/**
 * Specifies the set of possible permissions for Shared Access Signature protocol.
 */
public enum TableSasProtocol {
    /**
     * Permission to use SAS only through https granted.
     */
    HTTPS_ONLY(StorageConstants.HTTPS),

    /**
     * Permission to use SAS only through https or http granted.
     */
    HTTPS_HTTP(StorageConstants.HTTPS_HTTP);

    private final String protocols;

    TableSasProtocol(String p) {
        this.protocols = p;
    }

    /**
     * Parses a {@code String} into a {@link TableSasProtocol} value if possible.
     *
     * @param str The value to try to parse.
     *
     * @return A {@code SasProtocol} value that represents the string if possible.
     * @throws IllegalArgumentException If {@code str} doesn't equal "https" or "https,http".
     */
    public static TableSasProtocol parse(String str) {
        if (str.equals(StorageConstants.HTTPS)) {
            return TableSasProtocol.HTTPS_ONLY;
        } else if (str.equals(StorageConstants.HTTPS_HTTP)) {
            return TableSasProtocol.HTTPS_HTTP;
        }

        throw new IllegalArgumentException(String.format(Locale.ROOT,
            "%s could not be parsed into a SasProtocol value.", str));
    }

    @Override
    public String toString() {
        return this.protocols;
    }
}
