// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareEntityRelationPropertiesHelper;

/** The HealthcareEntityRelation model. */
public final class HealthcareEntityRelation {

    /*
     * Type of relation. Examples include: `DosageOfMedication` or
     * 'FrequencyOfMedication', etc.
     */
    private String relationType;

    /*
     * If true the relation between the entities is bidirectional, otherwise
     * directionality is source to target.
     */
    private boolean bidirectional;

    /*
     * Reference link to the source entity.
     */
    private String sourceLink;

    /*
     * Reference link to the target entity.
     */
    private String targetLink;

    static {
        HealthcareEntityRelationPropertiesHelper.setAccessor(
            new HealthcareEntityRelationPropertiesHelper.HealthcareEntityRelationAccessor() {
                @Override
                public void setRelationType(HealthcareEntityRelation healthcareEntityRelation, String relationType) {
                    healthcareEntityRelation.setRelationType(relationType);
                }

                @Override
                public void setBidirectional(HealthcareEntityRelation healthcareEntityRelation, boolean bidirectional) {
                    healthcareEntityRelation.setBidirectional(bidirectional);
                }

                @Override
                public void setSourceLink(HealthcareEntityRelation healthcareEntityRelation, String sourceLink) {
                    healthcareEntityRelation.setSourceLink(sourceLink);
                }

                @Override
                public void setTargetLink(HealthcareEntityRelation healthcareEntityRelation, String targetLink) {
                    healthcareEntityRelation.setTargetLink(targetLink);
                }
            });
    }

    /**
     * Get the relationType property: Type of relation. Examples include: `DosageOfMedication` or
     * 'FrequencyOfMedication', etc.
     *
     * @return the relationType value.
     */
    public String getRelationType() {
        return this.relationType;
    }

    /**
     * Get the bidirectional property: If true the relation between the entities is bidirectional, otherwise
     * directionality is source to target.
     *
     * @return the bidirectional value.
     */
    public boolean isBidirectional() {
        return this.bidirectional;
    }

    /**
     * Get the source property: Reference link to the source entity.
     *
     * @return the source reference link value.
     */
    public String getSourceLink() {
        return this.sourceLink;
    }

    /**
     * Get the target property: Reference link to the target entity.
     *
     * @return the target reference link value.
     */
    public String getTargetLink() {
        return this.targetLink;
    }

    private void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    private void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    private void setSourceLink(String sourceLink) {
        this.sourceLink = sourceLink;
    }

    private void setTargetLink(String targetLink) {
        this.targetLink = targetLink;
    }
}
