// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareEntityCollectionPropertiesHelper;
import com.azure.core.util.IterableStream;

/**
 * The {@link HealthcareEntityCollection} model.
 */
public final class HealthcareEntityCollection extends IterableStream<HealthcareEntity> {
    private IterableStream<TextAnalyticsWarning> warnings;
    private IterableStream<HealthcareEntityRelation> entityRelations;

    static {
        HealthcareEntityCollectionPropertiesHelper.setAccessor(
            new HealthcareEntityCollectionPropertiesHelper.HealthcareEntityCollectionAccessor() {
                @Override
                public void setWarnings(HealthcareEntityCollection healthcareEntityCollection,
                    IterableStream<TextAnalyticsWarning> warnings) {
                    healthcareEntityCollection.setWarnings(warnings);
                }

                @Override
                public void setEntityRelations(HealthcareEntityCollection healthcareEntityCollection,
                    IterableStream<HealthcareEntityRelation> entityRelations) {
                    healthcareEntityCollection.setEntityRelations(entityRelations);
                }
            }
        );
    }

    /**
     * Creates a {@link HealthcareEntityCollection} model that describes a healthcare entities collection including
     * warnings.
     *
     * @param entities An {@link IterableStream} of {@link HealthcareEntity healthcare entities}.
     */
    public HealthcareEntityCollection(IterableStream<HealthcareEntity> entities) {
        super(entities);
    }

    /**
     * Get the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    /**
     * Get the {@link IterableStream} of {@link HealthcareEntityRelation}.
     *
     * @return {@link IterableStream} of {@link HealthcareEntityRelation}.
     */
    public IterableStream<HealthcareEntityRelation> getEntityRelations() {
        return this.entityRelations;
    }

    /**
     * The private setter to set the warnings property
     * via {@link HealthcareEntityCollectionPropertiesHelper.HealthcareEntityCollectionAccessor}.
     *
     * @param warnings {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }

    /**
     * The private setter to set the entityRelations property
     * via {@link HealthcareEntityCollectionPropertiesHelper.HealthcareEntityCollectionAccessor}.
     *
     * @param entityRelations {@link IterableStream} of {@link HealthcareEntityRelation}.
     */
    private void setEntityRelations(IterableStream<HealthcareEntityRelation> entityRelations) {
        this.entityRelations = entityRelations;
    }
}
