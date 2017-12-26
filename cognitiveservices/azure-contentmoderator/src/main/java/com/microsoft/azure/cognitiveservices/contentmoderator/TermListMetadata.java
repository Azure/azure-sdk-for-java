/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Term list metadata.
 */
public class TermListMetadata {
    /**
     * Optional Key value pair to describe your list.
     */
    @JsonProperty(value = "Key One")
    private String keyOne;

    /**
     * Optional Key value pair to describe your list.
     */
    @JsonProperty(value = "Key Two")
    private String keyTwo;

    /**
     * Get the keyOne value.
     *
     * @return the keyOne value
     */
    public String keyOne() {
        return this.keyOne;
    }

    /**
     * Set the keyOne value.
     *
     * @param keyOne the keyOne value to set
     * @return the TermListMetadata object itself.
     */
    public TermListMetadata withKeyOne(String keyOne) {
        this.keyOne = keyOne;
        return this;
    }

    /**
     * Get the keyTwo value.
     *
     * @return the keyTwo value
     */
    public String keyTwo() {
        return this.keyTwo;
    }

    /**
     * Set the keyTwo value.
     *
     * @param keyTwo the keyTwo value to set
     * @return the TermListMetadata object itself.
     */
    public TermListMetadata withKeyTwo(String keyTwo) {
        this.keyTwo = keyTwo;
        return this;
    }

}
