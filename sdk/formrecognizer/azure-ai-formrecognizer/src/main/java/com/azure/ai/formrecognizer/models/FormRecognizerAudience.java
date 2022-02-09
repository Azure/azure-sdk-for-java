// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for FormRecognizerAudience. */
public class FormRecognizerAudience extends ExpandableStringEnum<FormRecognizerAudience> {
    /** Static value AZURE_RESOURCE_MANAGER_CHINA for FormRecognizerAudience. */
    public static final FormRecognizerAudience AZURE_RESOURCE_MANAGER_CHINA = fromString("https://cognitiveservices.azure.cn");

    /** Static value AZURE_RESOURCE_MANAGER_GOVERNMENT for FormRecognizerAudience. */
    public static final FormRecognizerAudience AZURE_RESOURCE_MANAGER_US_GOVERNMENT = fromString("https://cognitiveservices.azure.us");

    /** Static value AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD for FormRecognizerAudience. */
    public static final FormRecognizerAudience AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD = fromString("https://cognitiveservices.azure.com");

    /**
     * Creates or finds a FormRecognizerAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding FormRecognizerAudience.
     */
    public static FormRecognizerAudience fromString(String name) {
        return fromString(name, FormRecognizerAudience.class);
    }

    /** @return known FormRecognizerAudience values. */
    public static Collection<FormRecognizerAudience> values() {
        return values(FormRecognizerAudience.class);
    }
}
