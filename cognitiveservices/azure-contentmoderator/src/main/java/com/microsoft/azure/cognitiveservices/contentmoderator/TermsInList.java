/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Terms in list Id passed.
 */
public class TermsInList {
    /**
     * Added term details.
     */
    @JsonProperty(value = "Term")
    private String term;

    /**
     * Get the term value.
     *
     * @return the term value
     */
    public String term() {
        return this.term;
    }

    /**
     * Set the term value.
     *
     * @param term the term value to set
     * @return the TermsInList object itself.
     */
    public TermsInList withTerm(String term) {
        this.term = term;
        return this;
    }

}
