// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.health.insights.radiologyinsights.models;

import com.azure.core.annotation.Generated;
import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The type of the patient document, such as 'note' (text document) or 'fhirBundle' (FHIR JSON document).
 */
public final class ClinicalDocumentContentType extends ExpandableStringEnum<ClinicalDocumentContentType> {

    /**
     * Static value note for ClinicalDocumentContentType.
     */
    @Generated
    public static final ClinicalDocumentContentType NOTE = fromString("note");

    /**
     * Static value fhirBundle for ClinicalDocumentContentType.
     */
    @Generated
    public static final ClinicalDocumentContentType FHIR_BUNDLE = fromString("fhirBundle");

    /**
     * Static value dicom for ClinicalDocumentContentType.
     */
    @Generated
    public static final ClinicalDocumentContentType DICOM = fromString("dicom");

    /**
     * Static value genomicSequencing for ClinicalDocumentContentType.
     */
    @Generated
    public static final ClinicalDocumentContentType GENOMIC_SEQUENCING = fromString("genomicSequencing");

    /**
     * Creates a new instance of ClinicalDocumentContentType value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Generated
    @Deprecated
    public ClinicalDocumentContentType() {
    }

    /**
     * Creates or finds a ClinicalDocumentContentType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ClinicalDocumentContentType.
     */
    @Generated
    public static ClinicalDocumentContentType fromString(String name) {
        return fromString(name, ClinicalDocumentContentType.class);
    }

    /**
     * Gets known ClinicalDocumentContentType values.
     *
     * @return known ClinicalDocumentContentType values.
     */
    @Generated
    public static Collection<ClinicalDocumentContentType> values() {
        return values(ClinicalDocumentContentType.class);
    }
}
