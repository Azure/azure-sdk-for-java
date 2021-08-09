// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for NamespaceClassification. */
public final class NamespaceClassification extends ExpandableStringEnum<NamespaceClassification> {
    /** Static value Platform for NamespaceClassification. */
    public static final NamespaceClassification PLATFORM = fromString("Platform");

    /** Static value Custom for NamespaceClassification. */
    public static final NamespaceClassification CUSTOM = fromString("Custom");

    /** Static value Qos for NamespaceClassification. */
    public static final NamespaceClassification QOS = fromString("Qos");

    /**
     * Creates or finds a NamespaceClassification from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding NamespaceClassification.
     */
    @JsonCreator
    public static NamespaceClassification fromString(String name) {
        return fromString(name, NamespaceClassification.class);
    }

    /** @return known NamespaceClassification values. */
    public static Collection<NamespaceClassification> values() {
        return values(NamespaceClassification.class);
    }
}
