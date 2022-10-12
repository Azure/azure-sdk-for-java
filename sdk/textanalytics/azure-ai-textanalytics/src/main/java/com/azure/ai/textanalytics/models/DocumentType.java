// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for {@link DocumentType}. */
@Immutable
public final class DocumentType extends ExpandableStringEnum<DocumentType> {
    /** Static value None for DocumentType. */
    public static final DocumentType NONE = fromString("None");

    /** Static value ClinicalTrial for DocumentType. */
    public static final DocumentType CLINICAL_TRIAL = fromString("ClinicalTrial");

    /** Static value DischargeSummary for DocumentType. */
    public static final DocumentType DISCHARGE_SUMMARY = fromString("DischargeSummary");

    /** Static value ProgressNote for DocumentType. */
    public static final DocumentType PROGRESS_NOTE = fromString("ProgressNote");

    /** Static value HistoryAndPhysical for DocumentType. */
    public static final DocumentType HISTORY_AND_PHYSICAL = fromString("HistoryAndPhysical");

    /** Static value Consult for DocumentType. */
    public static final DocumentType CONSULT = fromString("Consult");

    /** Static value Imaging for DocumentType. */
    public static final DocumentType IMAGING = fromString("Imaging");

    /** Static value Pathology for DocumentType. */
    public static final DocumentType PATHOLOGY = fromString("Pathology");

    /** Static value ProcedureNote for DocumentType. */
    public static final DocumentType PROCEDURE_NOTE = fromString("ProcedureNote");

    /**
     * Creates or finds a DocumentType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentType.
     */
    public static DocumentType fromString(String name) {
        return fromString(name, DocumentType.class);
    }

    /**
     * Gets known DocumentType values.
     *
     * @return known DocumentType values.
     */
    public static Collection<DocumentType> values() {
        return values(DocumentType.class);
    }
}
