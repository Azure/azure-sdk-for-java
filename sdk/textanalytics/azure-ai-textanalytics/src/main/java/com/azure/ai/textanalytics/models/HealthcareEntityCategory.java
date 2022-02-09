// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets the healthcare entity category inferred by the text analytics service's healthcare entity recognition model.
 * The list of available categories is described at
 * See <a href="https://docs.microsoft.com/azure/cognitive-services/Text-Analytics/named-entity-types?tabs=health">healthcare entity types</a>.
 */
@Immutable
public final class HealthcareEntityCategory extends ExpandableStringEnum<HealthcareEntityCategory> {
    /** Static value BODY_STRUCTURE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory BODY_STRUCTURE = fromString("BodyStructure");

    /** Static value AGE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory AGE = fromString("Age");

    /** Static value GENDER for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory GENDER = fromString("Gender");

    /** Static value EXAMINATION_NAME for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory EXAMINATION_NAME = fromString("ExaminationName");

    /** Static value DATE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory DATE = fromString("Date");

    /** Static value DIRECTION for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory DIRECTION = fromString("Direction");

    /** Static value FREQUENCY for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory FREQUENCY = fromString("Frequency");

    /** Static value MEASUREMENT_VALUE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEASUREMENT_VALUE = fromString("MeasurementValue");

    /** Static value MEASUREMENT_UNIT for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEASUREMENT_UNIT = fromString("MeasurementUnit");

    /** Static value RELATIONAL_OPERATOR for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory RELATIONAL_OPERATOR = fromString("RelationalOperator");

    /** Static value TIME for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory TIME = fromString("Time");

    /** Static value GENE_OR_PROTEIN for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory GENE_OR_PROTEIN = fromString("GeneOrProtein");

    /** Static value VARIANT for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory VARIANT = fromString("Variant");

    /** Static value ADMINISTRATIVE_EVENT for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory ADMINISTRATIVE_EVENT = fromString("AdministrativeEvent");

    /** Static value CARE_ENVIRONMENT for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory CARE_ENVIRONMENT = fromString("CareEnvironment");

    /** Static value HEALTHCARE_PROFESSION for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory HEALTHCARE_PROFESSION = fromString("HealthcareProfession");

    /** Static value DIAGNOSIS for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory DIAGNOSIS = fromString("Diagnosis");

    /** Static value SYMPTOM_OR_SIGN for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory SYMPTOM_OR_SIGN = fromString("SymptomOrSign");

    /** Static value CONDITION_QUALIFIER for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory CONDITION_QUALIFIER = fromString("ConditionQualifier");

    /** Static value MEDICATION_CLASS for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEDICATION_CLASS = fromString("MedicationClass");

    /** Static value MEDICATION_NAME for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEDICATION_NAME = fromString("MedicationName");

    /** Static value DOSAGE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory DOSAGE = fromString("Dosage");

    /** Static value MEDICATION_FORM for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEDICATION_FORM = fromString("MedicationForm");

    /** Static value MEDICATION_ROUTE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEDICATION_ROUTE = fromString("MedicationRoute");

    /** Static value FAMILY_RELATION for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory FAMILY_RELATION = fromString("FamilyRelation");

    /** Static value TREATMENT_NAME for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory TREATMENT_NAME = fromString("TreatmentName");

    /**
     * Creates or finds a {@link HealthcareEntityCategory} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link HealthcareEntityCategory}.
     */
    @JsonCreator
    public static HealthcareEntityCategory fromString(String name) {
        return fromString(name, HealthcareEntityCategory.class);
    }
}
