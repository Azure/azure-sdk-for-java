// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for PersonalizerAudience.
 */
public final class PersonalizerAudience extends ExpandableStringEnum<PersonalizerAudience> {
    /**
     * Static value AZURE_RESOURCE_MANAGER_GOVERNMENT for PersonalizerAudience.
     */
    public static final PersonalizerAudience AZURE_RESOURCE_MANAGER_US_GOVERNMENT = fromString("https://cognitiveservices.azure.us");

    /**
     * Static value AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD for PersonalizerAudience.
     */
    public static final PersonalizerAudience AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD = fromString("https://cognitiveservices.azure.com");

    /**
     * Creates a new instance of {@link PersonalizerAudience} with no string value.
     *
     * @deprecated Use {@link #fromString(String)} to get or create an instance instead.
     */
    @Deprecated
    public PersonalizerAudience() {
    }

    /**
     * Creates or finds a PersonalizerAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding PersonalizerAudience.
     */
    public static PersonalizerAudience fromString(String name) {
        return fromString(name, PersonalizerAudience.class);
    }

    /**
     * Gets the known FormRecognizerAudience values.
     *
     * @return known FormRecognizerAudience values.
     */
    public static Collection<PersonalizerAudience> values() {
        return values(PersonalizerAudience.class);
    }
}
