/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.spellcheck.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.spellcheck.SpellingFlaggedToken;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.spellcheck.Answer;

/**
 * The SpellCheckInner model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("SpellCheck")
public class SpellCheckInner extends Answer {
    /**
     * The flaggedTokens property.
     */
    @JsonProperty(value = "flaggedTokens", required = true)
    private List<SpellingFlaggedToken> flaggedTokens;

    /**
     * Get the flaggedTokens value.
     *
     * @return the flaggedTokens value
     */
    public List<SpellingFlaggedToken> flaggedTokens() {
        return this.flaggedTokens;
    }

    /**
     * Set the flaggedTokens value.
     *
     * @param flaggedTokens the flaggedTokens value to set
     * @return the SpellCheckInner object itself.
     */
    public SpellCheckInner withFlaggedTokens(List<SpellingFlaggedToken> flaggedTokens) {
        this.flaggedTokens = flaggedTokens;
        return this;
    }

}
