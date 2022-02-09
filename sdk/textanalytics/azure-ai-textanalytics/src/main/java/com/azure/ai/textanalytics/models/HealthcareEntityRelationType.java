// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for {@link HealthcareEntityRelationType}.
 */
@Immutable
public final class HealthcareEntityRelationType extends ExpandableStringEnum<HealthcareEntityRelationType> {
    /** Static value DirectionOfBodyStructure for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType DIRECTION_OF_BODY_STRUCTURE =
        fromString("DirectionOfBodyStructure");

    /** Static value DirectionOfExamination for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType DIRECTION_OF_EXAMINATION = fromString("DirectionOfExamination");

    /** Static value RelationOfExamination for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType RELATION_OF_EXAMINATION = fromString("RelationOfExamination");

    /** Static value TimeOfExamination for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType TIME_OF_EXAMINATION = fromString("TimeOfExamination");

    /** Static value UnitOfExamination for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType UNIT_OF_EXAMINATION = fromString("UnitOfExamination");

    /** Static value ValueOfExamination for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType VALUE_OF_EXAMINATION = fromString("ValueOfExamination");

    /** Static value DirectionOfCondition for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType DIRECTION_OF_CONDITION = fromString("DirectionOfCondition");

    /** Static value QualifierOfCondition for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType QUALIFIER_OF_CONDITION = fromString("QualifierOfCondition");

    /** Static value TimeOfCondition for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType TIME_OF_CONDITION = fromString("TimeOfCondition");

    /** Static value UnitOfCondition for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType UNIT_OF_CONDITION = fromString("UnitOfCondition");

    /** Static value ValueOfCondition for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType VALUE_OF_CONDITION = fromString("ValueOfCondition");

    /** Static value DosageOfMedication for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType DOSAGE_OF_MEDICATION = fromString("DosageOfMedication");

    /** Static value FormOfMedication for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType FORM_OF_MEDICATION = fromString("FormOfMedication");

    /** Static value FrequencyOfMedication for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType FREQUENCY_OF_MEDICATION = fromString("FrequencyOfMedication");

    /** Static value RouteOfMedication for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType ROUTE_OF_MEDICATION = fromString("RouteOfMedication");

    /** Static value TimeOfMedication for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType TIME_OF_MEDICATION = fromString("TimeOfMedication");

    /** Static value DirectionOfTreatment for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType DIRECTION_OF_TREATMENT = fromString("DirectionOfTreatment");

    /** Static value TimeOfTreatment for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType TIME_OF_TREATMENT = fromString("TimeOfTreatment");

    /** Static value FrequencyOfTreatment for HealthcareEntityRelationType. */
    public static final HealthcareEntityRelationType FREQUENCY_OF_TREATMENT = fromString("FrequencyOfTreatment");

    /**
     * Creates or finds a HealthcareEntityRelationType from its string representation.
     * @param name a name to look for
     * @return the corresponding HealthcareEntityRelationType
     */
    public static HealthcareEntityRelationType fromString(String name) {
        return fromString(name, HealthcareEntityRelationType.class);
    }
}
