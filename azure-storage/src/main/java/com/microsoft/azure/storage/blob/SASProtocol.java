/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

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

    @Override
    public String toString() {
        return this.protocols;
    }

    /**
     * Parses a {@code String} into a {@code SASProtocl} value if possible.
     *
     * @param str
     *      The value to try to parse.
     * @return
     *      A {@code SASProtocol} value that represents the string if possible.
     */
    public static SASProtocol parse(String str) {
        if (str.equals(Constants.HTTPS)) {
            return SASProtocol.HTTPS_ONLY;
        }
        else if (str.equals(Constants.HTTPS_HTTP)) {
            return SASProtocol.HTTPS_HTTP;
        }
        throw new IllegalArgumentException(String.format(Locale.ROOT,
                "%s could not be parsed into a SASProtocl value.", str));
    }
}
