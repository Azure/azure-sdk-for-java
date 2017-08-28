/**
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

import com.microsoft.azure.storage.core.Utility;

import java.util.Locale;

/**
 * The tier of the block blob on a standard storage account.
 */
public enum StandardBlobTier {
    /**
     * The tier is not recognized by this version of the library.
     */
    UNKNOWN,

    /**
     * The tier is hot storage.
     */
    HOT,

    /**
     * The tier is cool storage.
     */
    COOL,

    /**
     * The tier is archive storage.
     */
    ARCHIVE;

    /**
     * Parses a standard blob tier from the given string.
     *
     * @param standardBlobTierString
     *        A <code>String</code> which represents the tier of the blob tier on a standard storage account.
     *
     * @return A <code>StandardBlobTier</code> value that represents the standard blob tier.
     */
    protected static StandardBlobTier parse(final String standardBlobTierString) {
        if (Utility.isNullOrEmpty(standardBlobTierString)) {
            return UNKNOWN;
        }
        else if ("hot".equals(standardBlobTierString.toLowerCase(Locale.US))) {
            return HOT;
        }
        else if ("cool".equals(standardBlobTierString.toLowerCase(Locale.US))) {
            return COOL;
        }
        else if ("archive".equals(standardBlobTierString.toLowerCase(Locale.US))) {
            return ARCHIVE;
        }
        else {
            return UNKNOWN;
        }
    }
}
