// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.RegexFlags;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.RegexFlags} and {@link RegexFlags}.
 */
public final class RegexFlagsConverter {


    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.RegexFlags} to enum {@link RegexFlags}.
     */
    public static RegexFlags map(com.azure.search.documents.implementation.models.RegexFlags obj) {
        if (obj == null) {
            return null;
        }
        return RegexFlags.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link RegexFlags} to enum {@link com.azure.search.documents.implementation.models.RegexFlags}.
     */
    public static com.azure.search.documents.implementation.models.RegexFlags map(RegexFlags obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.implementation.models.RegexFlags.fromString(obj.toString());
    }

    private RegexFlagsConverter() {
    }
}
