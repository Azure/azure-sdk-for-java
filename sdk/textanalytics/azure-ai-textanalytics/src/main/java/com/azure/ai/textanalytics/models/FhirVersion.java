// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for FhirVersion.
 */
@Immutable
public final class FhirVersion extends ExpandableStringEnum<FhirVersion> {
    /** Static value 4.0.1 for FhirVersion. */
    public static final FhirVersion V4_0_1 = fromString("4.0.1");

    /**
     * Creates or finds a FhirVersion from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding FhirVersion.
     */
    @JsonCreator
    public static FhirVersion fromString(String name) {
        return fromString(name, FhirVersion.class);
    }

    /** @return known FhirVersion values. */
    public static Collection<FhirVersion> values() {
        return values(FhirVersion.class);
    }
}
