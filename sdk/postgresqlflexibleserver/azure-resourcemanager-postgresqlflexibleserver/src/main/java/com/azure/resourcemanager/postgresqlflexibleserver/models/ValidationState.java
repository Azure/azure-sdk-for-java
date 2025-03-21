// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.postgresqlflexibleserver.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Validation status for migration.
 */
public final class ValidationState extends ExpandableStringEnum<ValidationState> {
    /**
     * Static value Failed for ValidationState.
     */
    public static final ValidationState FAILED = fromString("Failed");

    /**
     * Static value Succeeded for ValidationState.
     */
    public static final ValidationState SUCCEEDED = fromString("Succeeded");

    /**
     * Static value Warning for ValidationState.
     */
    public static final ValidationState WARNING = fromString("Warning");

    /**
     * Creates a new instance of ValidationState value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ValidationState() {
    }

    /**
     * Creates or finds a ValidationState from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding ValidationState.
     */
    public static ValidationState fromString(String name) {
        return fromString(name, ValidationState.class);
    }

    /**
     * Gets known ValidationState values.
     * 
     * @return known ValidationState values.
     */
    public static Collection<ValidationState> values() {
        return values(ValidationState.class);
    }
}
