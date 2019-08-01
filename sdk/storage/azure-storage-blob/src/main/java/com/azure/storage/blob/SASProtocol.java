// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import java.util.Locale;

/**
 * Specifies the set of possible permissions for a shared access signature protocol. Values of this type can be used
 * to set the fields on the {@link AccountSASSignatureValues} and {@link ServiceSASSignatureValues} types.
 */
public enum SASProtocol {
    /**
     * Permission to use SAS only through https granted.
     */
    HTTPS_ONLY(Constants.HTTPS),

    /**
     * Permission to use SAS only through https or http granted.
     */
    HTTPS_HTTP(Constants.HTTPS_HTTP);

    private final String protocols;

    SASProtocol(String p) {
        this.protocols = p;
    }

    /**
     * Parses a {@code String} into a {@code SASProtocl} value if possible.
     *
     * @param str
     *         The value to try to parse.
     *
     * @return A {@code SASProtocol} value that represents the string if possible.
     * @throws IllegalArgumentException If {@code str} doesn't equal "https" or "https,http"
     */
    public static SASProtocol parse(String str) {
        if (str.equals(Constants.HTTPS)) {
            return SASProtocol.HTTPS_ONLY;
        } else if (str.equals(Constants.HTTPS_HTTP)) {
            return SASProtocol.HTTPS_HTTP;
        }
        throw new IllegalArgumentException(String.format(Locale.ROOT,
                "%s could not be parsed into a SASProtocl value.", str));
    }

    @Override
    public String toString() {
        return this.protocols;
    }
}
