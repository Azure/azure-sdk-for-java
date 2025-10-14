// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The service tier used for processing the request.
 */
public final class ServiceTier extends ExpandableStringEnum<ServiceTier> {

    /**
     * Service tier option for scale.
     */
    public static final ServiceTier SCALE = fromString("scale");

    /**
     * Service tier option for default.
     */
    public static final ServiceTier DEFAULT = fromString("default");

    /**
     * Creates a new instance of ServiceTier value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ServiceTier() {
    }

    /**
     * Creates or finds a ServiceTier from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ServiceTier.
     */
    public static ServiceTier fromString(String name) {
        return fromString(name, ServiceTier.class);
    }

    /**
     * Get known ServiceTier values.
     *
     * @return a collection of known ServiceTier values.
     */
    public static Collection<ServiceTier> values() {
        return values(ServiceTier.class);
    }
}
