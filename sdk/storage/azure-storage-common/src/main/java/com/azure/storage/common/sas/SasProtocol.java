// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.sas;

import com.azure.storage.common.implementation.Constants;

import java.util.Locale;

/**
 * Specifies the set of possible permissions for a shared access signature protocol.
 */
public enum SasProtocol {
    /**
     * Permission to use SAS only through https granted.
     */
    HTTPS_ONLY(Constants.HTTPS),

    /**
     * Permission to use SAS only through https or http granted.
     */
    HTTPS_HTTP(Constants.HTTPS_HTTP);

    private final String protocols;

    SasProtocol(String p) {
        this.protocols = p;
    }

    /**
     * Parses a {@code String} into a {@code SASProtocl} value if possible.
     *
     * @param str The value to try to parse.
     *
     * @return A {@code SasProtocol} value that represents the string if possible.
     * @throws IllegalArgumentException If {@code str} doesn't equal "https" or "https,http"
     */
    public static SasProtocol parse(String str) {
        if (str.equals(Constants.HTTPS)) {
            return SasProtocol.HTTPS_ONLY;
        } else if (str.equals(Constants.HTTPS_HTTP)) {
            return SasProtocol.HTTPS_HTTP;
        }
        throw new IllegalArgumentException(String.format(Locale.ROOT,
            "%s could not be parsed into a SasProtocol value.", str));
    }

    @Override
    public String toString() {
        return this.protocols;
    }
}
