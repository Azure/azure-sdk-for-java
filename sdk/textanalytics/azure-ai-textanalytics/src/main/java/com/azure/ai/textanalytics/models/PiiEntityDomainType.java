// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets the PII entity domain type. It is provided by service v3.1-preview.2 and later.
 */
@Immutable
public final class PiiEntityDomainType extends ExpandableStringEnum<PiiEntityDomainType> {
    /**
     * Protected health information (PHI) as the PiiEntityDomainType.
     */
    public static final PiiEntityDomainType PROTECTED_HEALTH_INFORMATION = fromString("PHI");

    /**
     * Creates or finds a {@link EntityCategory} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link EntityCategory}.
     */
    @JsonCreator
    public static PiiEntityDomainType fromString(String name) {
        return fromString(name, PiiEntityDomainType.class);
    }
}
