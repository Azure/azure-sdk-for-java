// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for {@link HealthcareDocumentType}. */
@Immutable
public final class HealthcareDocumentType extends ExpandableStringEnum<HealthcareDocumentType> {
    /** Static value None for HealthcareDocumentType. */
    public static final HealthcareDocumentType NONE = fromString("None");

    /** Static value ClinicalTrial for HealthcareDocumentType. */
    public static final HealthcareDocumentType CLINICAL_TRIAL = fromString("ClinicalTrial");

    /** Static value DischargeSummary for HealthcareDocumentType. */
    public static final HealthcareDocumentType DISCHARGE_SUMMARY = fromString("DischargeSummary");

    /** Static value ProgressNote for HealthcareDocumentType. */
    public static final HealthcareDocumentType PROGRESS_NOTE = fromString("ProgressNote");

    /** Static value HistoryAndPhysical for HealthcareDocumentType. */
    public static final HealthcareDocumentType HISTORY_AND_PHYSICAL = fromString("HistoryAndPhysical");

    /** Static value Consult for HealthcareDocumentType. */
    public static final HealthcareDocumentType CONSULT = fromString("Consult");

    /** Static value Imaging for HealthcareDocumentType. */
    public static final HealthcareDocumentType IMAGING = fromString("Imaging");

    /** Static value Pathology for HealthcareDocumentType. */
    public static final HealthcareDocumentType PATHOLOGY = fromString("Pathology");

    /** Static value ProcedureNote for HealthcareDocumentType. */
    public static final HealthcareDocumentType PROCEDURE_NOTE = fromString("ProcedureNote");

    /**
     * Creates or finds a HealthcareDocumentType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding HealthcareDocumentType.
     */
    public static HealthcareDocumentType fromString(String name) {
        return fromString(name, HealthcareDocumentType.class);
    }

    /**
     * Gets known HealthcareDocumentType values.
     *
     * @return known HealthcareDocumentType values.
     */
    public static Collection<HealthcareDocumentType> values() {
        return values(HealthcareDocumentType.class);
    }
}
