// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * Control the phrases to be used in the summary.
 */
@Immutable
public final class PhraseControl {
    /*
     * The target phrase to control.
     */
    private final String targetPhrase;

    /*
     * The strategy to use in phrase control.
     */
    private final PhraseControlStrategy strategy;

    /**
     *  Control the phrases to be used in the summary.
     *
     * @param targetPhrase The target phrase to control.
     * @param strategy The strategy to use in phrase control.
     */
    public PhraseControl(String targetPhrase, PhraseControlStrategy strategy) {
        this.targetPhrase = targetPhrase;
        this.strategy = strategy;
    }

    /**
     * Get the targetPhrase property: The target phrase to control.
     *
     * @return the targetPhrase value.
     */
    public String getTargetPhrase() {
        return this.targetPhrase;
    }

    /**
     * Get the strategy property: The strategy to use in phrase control.
     *
     * @return the strategy value.
     */
    public PhraseControlStrategy getStrategy() {
        return this.strategy;
    }
}
