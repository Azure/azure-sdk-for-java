// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * The RecognizePiiEntitiesResult model.
 */
@Immutable
public final class RecognizePiiEntitiesResult extends DocumentResult {
    private final List<PiiEntity> piiEntities;

    /**
     * Creates a {@code RecognizePiiEntitiesResult} model that describes recognized entities result
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error
     * @param piiEntities a list of {@link PiiEntity}
     */
    public RecognizePiiEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, List<PiiEntity> piiEntities) {
        super(id, textDocumentStatistics, error);
        this.piiEntities = piiEntities == null ? new ArrayList<>() : piiEntities;
    }

    /**
     * Get a list of PII entities
     *
     * @return a list of {@link PiiEntity}
     */
    public List<PiiEntity> getPiiEntities() {
        throwExceptionIfError();
        return piiEntities;
    }
}
