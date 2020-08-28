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
     * Create an {@link MinedOpinion} model that describes the mined opinions.
     *
     * @param aspect The aspect of a product/service that the opinions is about.
     * @param opinions The opinions of the aspect text.
     */
    public MinedOpinion(AspectSentiment aspect, IterableStream<OpinionSentiment> opinions) {
        this.aspect = aspect;
        this.opinions = opinions;
    }

    /**
     * Get the aspect in text, such as the attributes of products or services. For example, if a customer leaves
     * feedback about a hotel such as "the room was great, but the staff was unfriendly", opinion mining will locate
     * aspects in the text. The "room" and "staff" are two aspects recognized.
     *
     * @return The aspect in text.
     */
    public AspectSentiment getAspect() {
        return this.aspect;
    }

    /**
     * Get the opinions of aspect text.
     *
     * @return The opinions of aspect text.
     */
    public IterableStream<OpinionSentiment> getOpinions() {
        return opinions;
    }
}
