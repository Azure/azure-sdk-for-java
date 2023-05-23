// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for {@link PiiEntityDomain}.
 */
@Immutable
public final class PiiEntityDomain extends ExpandableStringEnum<PiiEntityDomain> {
    /**
     * Protected health information (PHI) as the PiiEntityDomain.
     */
    public static final PiiEntityDomain PROTECTED_HEALTH_INFORMATION = fromString("PHI");

    /**
     * None as the PiiEntityDomain.
     */
    public static final PiiEntityDomain NONE = fromString("none");

    /**
     * Creates or finds a {@link PiiEntityDomain} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link PiiEntityDomain}.
     */
    public static PiiEntityDomain fromString(String name) {
        return fromString(name, PiiEntityDomain.class);
    }
}
