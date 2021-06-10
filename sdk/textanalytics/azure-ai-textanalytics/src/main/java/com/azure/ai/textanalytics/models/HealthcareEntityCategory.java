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
    public static final HealthcareEntityCategory BODY_STRUCTURE = fromString("BODY_STRUCTURE");

    /** Static value AGE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory AGE = fromString("AGE");

    /** Static value GENDER for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory GENDER = fromString("GENDER");

    /** Static value EXAMINATION_NAME for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory EXAMINATION_NAME = fromString("EXAMINATION_NAME");

    /** Static value DATE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory DATE = fromString("DATE");

    /** Static value DIRECTION for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory DIRECTION = fromString("DIRECTION");

    /** Static value FREQUENCY for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory FREQUENCY = fromString("FREQUENCY");

    /** Static value MEASUREMENT_VALUE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEASUREMENT_VALUE = fromString("MEASUREMENT_VALUE");

    /** Static value MEASUREMENT_UNIT for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEASUREMENT_UNIT = fromString("MEASUREMENT_UNIT");

    /** Static value RELATIONAL_OPERATOR for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory RELATIONAL_OPERATOR = fromString("RELATIONAL_OPERATOR");

    /** Static value TIME for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory TIME = fromString("TIME");

    /** Static value GENE_OR_PROTEIN for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory GENE_ORPROTEIN = fromString("GENE_OR_PROTEIN");

    /** Static value VARIANT for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory VARIANT = fromString("VARIANT");

    /** Static value ADMINISTRATIVE_EVENT for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory ADMINISTRATIVE_EVENT = fromString("ADMINISTRATIVE_EVENT");

    /** Static value CARE_ENVIRONMENT for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory CARE_ENVIRONMENT = fromString("CARE_ENVIRONMENT");

    /** Static value HEALTHCARE_PROFESSION for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory HEALTHCARE_PROFESSION = fromString("HEALTHCARE_PROFESSION");

    /** Static value DIAGNOSIS for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory DIAGNOSIS = fromString("DIAGNOSIS");

    /** Static value SYMPTOM_OR_SIGN for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory SYMPTOM_OR_SIGN = fromString("SYMPTOM_OR_SIGN");

    /** Static value CONDITION_QUALIFIER for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory CONDITION_QUALIFIER = fromString("CONDITION_QUALIFIER");

    /** Static value MEDICATION_CLASS for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEDICATION_CLASS = fromString("MEDICATION_CLASS");

    /** Static value MEDICATION_NAME for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEDICATION_NAME = fromString("MEDICATION_NAME");

    /** Static value DOSAGE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory DOSAGE = fromString("DOSAGE");

    /** Static value MEDICATION_FORM for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEDICATION_FORM = fromString("MEDICATION_FORM");

    /** Static value MEDICATION_ROUTE for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory MEDICATION_ROUTE = fromString("MEDICATION_ROUTE");

    /** Static value FAMILY_RELATION for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory FAMILY_RELATION = fromString("FAMILY_RELATION");

    /** Static value TREATMENT_NAME for HealthcareEntityCategory. */
    public static final HealthcareEntityCategory TREATMENT_NAME = fromString("TREATMENT_NAME");

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
