// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentAnalysisFeature. */
public final class DocumentAnalysisFeature extends ExpandableStringEnum<DocumentAnalysisFeature> {
    /** Static value ocr.highResolution for DocumentAnalysisFeature. */
    public static final DocumentAnalysisFeature OCR_HIGH_RESOLUTION = fromString("ocr.highResolution");

    /** Static value ocr.formula for DocumentAnalysisFeature. */
    public static final DocumentAnalysisFeature OCR_FORMULA = fromString("ocr.formula");

    /** Static value ocr.font for DocumentAnalysisFeature. */
    public static final DocumentAnalysisFeature OCR_FONT = fromString("ocr.font");

    /** Static value queryFields.premium for DocumentAnalysisFeature. */
    public static final DocumentAnalysisFeature QUERY_FIELDS_PREMIUM = fromString("queryFields.premium");

    /**
     * Creates a new instance of DocumentAnalysisFeature value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public DocumentAnalysisFeature() {}

    /**
     * Creates or finds a DocumentAnalysisFeature from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentAnalysisFeature.
     */
    public static DocumentAnalysisFeature fromString(String name) {
        return fromString(name, DocumentAnalysisFeature.class);
    }

    /**
     * Gets known DocumentAnalysisFeature values.
     *
     * @return known DocumentAnalysisFeature values.
     */
    public static Collection<DocumentAnalysisFeature> values() {
        return values(DocumentAnalysisFeature.class);
    }
}
