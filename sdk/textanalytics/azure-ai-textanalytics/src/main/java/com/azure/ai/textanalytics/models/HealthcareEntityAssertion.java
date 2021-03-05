// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareEntityAssertionPropertiesHelper;

/**
 * The {@link HealthcareEntityAssertion} model.
 */
public final class HealthcareEntityAssertion {
    /*
     * Describes any conditionality on the entity.
     */
    private EntityConditionality conditionality;

    /*
     * Describes the entities certainty and polarity.
     */
    private EntityCertainty certainty;

    /*
     * Describes if the entity is the subject of the text or if it describes
     * someone else.
     */
    private EntityAssociation association;

    static {
        HealthcareEntityAssertionPropertiesHelper.setAccessor(
            new HealthcareEntityAssertionPropertiesHelper.HealthcareEntityAssertionAccessor() {
                @Override
                public void setAssociation(HealthcareEntityAssertion assertion, EntityAssociation entityAssociation) {
                    assertion.setAssociation(entityAssociation);
                }

                @Override
                public void setCertainty(HealthcareEntityAssertion assertion, EntityCertainty entityCertainty) {
                    assertion.setCertainty(entityCertainty);
                }

                @Override
                public void setConditionality(HealthcareEntityAssertion assertion,
                    EntityConditionality conditionality) {
                    assertion.setConditionality(conditionality);
                }
            });
    }

    /**
     * Get the conditionality property: Describes any conditionality on the entity.
     *
     * @return the conditionality value.
     */
    public EntityConditionality getConditionality() {
        return this.conditionality;
    }


    /**
     * Get the certainty property: Describes the entities certainty and polarity.
     *
     * @return the certainty value.
     */
    public EntityCertainty getCertainty() {
        return this.certainty;
    }

    /**
     * Get the association property: Describes if the entity is the subject of the text or if it describes someone else.
     *
     * @return the association value.
     */
    public EntityAssociation getAssociation() {
        return this.association;
    }

    private void setAssociation(EntityAssociation entityAssociation) {
        this.association = entityAssociation;
    }

    private void setCertainty(EntityCertainty entityCertainty) {
        this.certainty = entityCertainty;
    }

    private void setConditionality(EntityConditionality conditionality) {
        this.conditionality = conditionality;
    }
}
