// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for RequestMethodOperator. */
public final class RequestMethodOperator extends ExpandableStringEnum<RequestMethodOperator> {
    /** Static value Equal for RequestMethodOperator. */
    public static final RequestMethodOperator EQUAL = fromString("Equal");

    /**
     * Creates or finds a RequestMethodOperator from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RequestMethodOperator.
     */
    @JsonCreator
    public static RequestMethodOperator fromString(String name) {
        return fromString(name, RequestMethodOperator.class);
    }

    /** @return known RequestMethodOperator values. */
    public static Collection<RequestMethodOperator> values() {
        return values(RequestMethodOperator.class);
    }
}
