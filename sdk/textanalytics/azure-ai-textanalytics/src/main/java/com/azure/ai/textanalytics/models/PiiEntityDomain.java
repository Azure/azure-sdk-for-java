// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for {@code PiiEntityDomain}.
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
     * Creates a new instance of {@code PiiEntityDomain} value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public PiiEntityDomain() {
    }

    /**
     * Creates or finds a {@code PiiEntityDomain} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@code PiiEntityDomain}.
     */
    public static PiiEntityDomain fromString(String name) {
        return fromString(name, PiiEntityDomain.class);
    }

    /**
     * All known PiiEntityDomain values.
     *
     * @return known PiiEntityDomain values.
     */
    public static Collection<PiiEntityDomain> values() {
        return values(PiiEntityDomain.class);
    }
}
