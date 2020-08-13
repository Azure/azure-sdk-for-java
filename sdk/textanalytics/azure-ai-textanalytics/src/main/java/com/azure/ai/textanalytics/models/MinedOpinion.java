// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.IterableStream;

/**
 * The {@link MinedOpinion} model.
 */
public final class MinedOpinion {
    private final AspectSentiment aspect;
    private final IterableStream<OpinionSentiment> opinions;

    /**
     * Create an {@link MinedOpinion} model that describes mined opinion.
     *
     * @param aspect The aspect of a product/service that this opinion is about.
     * @param opinions The opinions of the aspect text.
     */
    public MinedOpinion(AspectSentiment aspect, IterableStream<OpinionSentiment> opinions) {
        this.aspect = aspect;
        this.opinions = opinions;
    }

    /**
     * Get the opinions of aspect text.
     *
     * @return The opinions of aspect text.
     */
    public IterableStream<OpinionSentiment> getOpinions() {
        return opinions;
    }

    /**
     * Get the aspect of mined opinion.
     *
     * @return The aspect of mined opinion.
     */
    public AspectSentiment getAspect() {
        return this.aspect;
    }
}
