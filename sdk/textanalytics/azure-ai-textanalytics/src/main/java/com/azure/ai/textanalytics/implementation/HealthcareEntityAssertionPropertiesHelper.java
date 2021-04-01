// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.EntityCertainty;
import com.azure.ai.textanalytics.models.EntityConditionality;
import com.azure.ai.textanalytics.models.EntityAssociation;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;

/**
 * The helper class to set the non-public properties of an {@link HealthcareEntityAssertion} instance.
 */
public final class HealthcareEntityAssertionPropertiesHelper {
    private static HealthcareEntityAssertionAccessor accessor;

    private HealthcareEntityAssertionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HealthcareEntityAssertion} instance.
     */
    public interface HealthcareEntityAssertionAccessor {
        void setAssociation(HealthcareEntityAssertion assertion,
            EntityAssociation entityAssociation);
        void setCertainty(HealthcareEntityAssertion assertion,
            EntityCertainty entityCertainty);
        void setConditionality(HealthcareEntityAssertion assertion,
            EntityConditionality conditionality);
    }

    /**
     * The method called from {@link HealthcareEntityAssertion} to set it's accessor.
     *
     * @param assertionAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityAssertionAccessor assertionAccessor) {
        accessor = assertionAccessor;
    }

    public static void setAssociation(HealthcareEntityAssertion assertion,
        EntityAssociation entityAssociation) {
        accessor.setAssociation(assertion, entityAssociation);
    }

    public static void setCertainty(HealthcareEntityAssertion assertion,
        EntityCertainty entityCertainty) {
        accessor.setCertainty(assertion, entityCertainty);
    }

    public static void setConditionality(HealthcareEntityAssertion assertion,
        EntityConditionality conditionality) {
        accessor.setConditionality(assertion, conditionality);
    }
}
