// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;

/**
 * The {@link AnalyzeHealthcareEntitiesResult} model.
 */
@Immutable
public final class AnalyzeHealthcareEntitiesResult extends TextAnalyticsResult {
    private DetectedLanguage detectedLanguage;
    private IterableStream<TextAnalyticsWarning> warnings;
    private IterableStream<HealthcareEntity> entities;
    private IterableStream<HealthcareEntityRelation> entityRelations;
    private BinaryData fhirBundle;

    static {
        AnalyzeHealthcareEntitiesResultPropertiesHelper.setAccessor(
            new AnalyzeHealthcareEntitiesResultPropertiesHelper.AnalyzeHealthcareEntitiesResultAccessor() {
                @Override
                public void setDetectedLanguage(AnalyzeHealthcareEntitiesResult entitiesResult,
                    DetectedLanguage detectedLanguage) {
                    entitiesResult.setDetectedLanguage(detectedLanguage);
                }

                @Override
                public void setEntities(AnalyzeHealthcareEntitiesResult entitiesResult,
                    IterableStream<HealthcareEntity> entities) {
                    entitiesResult.setEntities(entities);
                }

                @Override
                public void setWarnings(AnalyzeHealthcareEntitiesResult entitiesResult,
                    IterableStream<TextAnalyticsWarning> warnings) {
                    entitiesResult.setWarnings(warnings);
                }

                @Override
                public void setEntityRelations(AnalyzeHealthcareEntitiesResult entitiesResult,
                    IterableStream<HealthcareEntityRelation> entityRelations) {
                    entitiesResult.setEntityRelations(entityRelations);
                }

                @Override
                public void setFhirBundle(AnalyzeHealthcareEntitiesResult entitiesResult, BinaryData fhirBundle) {
                    entitiesResult.setFhirBundle(fhirBundle);
                }
            });
    }

    /**
     * Creates a {@link AnalyzeHealthcareEntitiesResult} model that describes recognized healthcare entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public AnalyzeHealthcareEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Gets an {@link IterableStream} of {@link HealthcareEntity}.
     *
     * @return An {@link IterableStream} of {@link HealthcareEntity}.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public IterableStream<HealthcareEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    /**
     * Gets the {@link IterableStream} of {@link HealthcareEntityRelation}.
     *
     * @return The {@link IterableStream} of {@link HealthcareEntityRelation}.
     */
    public IterableStream<HealthcareEntityRelation> getEntityRelations() {
        return this.entityRelations;
    }

    /**
     * Gets the value of FHIR Bundle. See more information in https://www.hl7.org/fhir/overview.html.
     *
     * @return The value of FHIR Bundle in BinaryData.
     */
    public BinaryData getFhirBundle() {
        return this.fhirBundle;
    }

    /**
     * Get the detectedLanguage property: If 'language' is set to 'auto' for the document in the request this field will
     * contain an object of the language detected for this document.
     *
     * @return the detectedLanguage value.
     */
    public DetectedLanguage getDetectedLanguage() {
        return this.detectedLanguage;
    }

    private void setDetectedLanguage(DetectedLanguage detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    private void setEntities(IterableStream<HealthcareEntity> entities) {
        this.entities = entities;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }

    private void setEntityRelations(IterableStream<HealthcareEntityRelation> entityRelations) {
        this.entityRelations = entityRelations;
    }

    private void setFhirBundle(BinaryData fhirBundle) {
        this.fhirBundle = fhirBundle;
    }
}
