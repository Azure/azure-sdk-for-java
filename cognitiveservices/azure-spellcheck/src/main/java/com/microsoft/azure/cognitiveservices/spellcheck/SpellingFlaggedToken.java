/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.spellcheck;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The SpellingFlaggedToken model.
 */
public class SpellingFlaggedToken {
    /**
     * The offset property.
     */
    @JsonProperty(value = "offset", required = true)
    private int offset;

    /**
     * The token property.
     */
    @JsonProperty(value = "token", required = true)
    private String token;

    /**
     * Possible values include: 'UnknownToken', 'RepeatedToken'.
     */
    @JsonProperty(value = "type", required = true)
    private ErrorType type;

    /**
     * The suggestions property.
     */
    @JsonProperty(value = "suggestions", access = JsonProperty.Access.WRITE_ONLY)
    private List<SpellingTokenSuggestion> suggestions;

    /**
     * The pingUrlSuffix property.
     */
    @JsonProperty(value = "pingUrlSuffix", access = JsonProperty.Access.WRITE_ONLY)
    private String pingUrlSuffix;

    /**
     * Get the offset value.
     *
     * @return the offset value
     */
    public int offset() {
        return this.offset;
    }

    /**
     * Set the offset value.
     *
     * @param offset the offset value to set
     * @return the SpellingFlaggedToken object itself.
     */
    public SpellingFlaggedToken withOffset(int offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Get the token value.
     *
     * @return the token value
     */
    public String token() {
        return this.token;
    }

    /**
     * Set the token value.
     *
     * @param token the token value to set
     * @return the SpellingFlaggedToken object itself.
     */
    public SpellingFlaggedToken withToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public ErrorType type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the SpellingFlaggedToken object itself.
     */
    public SpellingFlaggedToken withType(ErrorType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the suggestions value.
     *
     * @return the suggestions value
     */
    public List<SpellingTokenSuggestion> suggestions() {
        return this.suggestions;
    }

    /**
     * Get the pingUrlSuffix value.
     *
     * @return the pingUrlSuffix value
     */
    public String pingUrlSuffix() {
        return this.pingUrlSuffix;
    }

}
