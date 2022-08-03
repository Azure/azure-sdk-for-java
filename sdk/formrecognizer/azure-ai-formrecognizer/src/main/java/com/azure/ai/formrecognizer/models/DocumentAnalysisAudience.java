// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentAnalysisAudience. */
public class DocumentAnalysisAudience extends ExpandableStringEnum<DocumentAnalysisAudience> {
    /** Static value AZURE_RESOURCE_MANAGER_CHINA for DocumentAnalysisAudience. */
    public static final DocumentAnalysisAudience AZURE_RESOURCE_MANAGER_CHINA = fromString("https://cognitiveservices.azure.cn");

    /** Static value AZURE_RESOURCE_MANAGER_GOVERNMENT for DocumentAnalysisAudience. */
    public static final DocumentAnalysisAudience AZURE_RESOURCE_MANAGER_US_GOVERNMENT = fromString("https://cognitiveservices.azure.us");

    /** Static value AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD for DocumentAnalysisAudience. */
    public static final DocumentAnalysisAudience AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD = fromString("https://cognitiveservices.azure.com");

    /**
     * Creates or finds a DocumentAnalysisAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentAnalysisAudience.
     */
    public static DocumentAnalysisAudience fromString(String name) {
        return fromString(name, DocumentAnalysisAudience.class);
    }

    /** @return known DocumentAnalysisAudience values. */
    public static Collection<DocumentAnalysisAudience> values() {
        return values(DocumentAnalysisAudience.class);
    }
}
