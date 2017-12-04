/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gets all term Id response properties.
 */
public class TermData {
    /**
     * Language of the terms.
     */
    @JsonProperty(value = "language")
    private String language;

    /**
     * List of terms.
     */
    @JsonProperty(value = "terms")
    private List<TermsInList> terms;

    /**
     * Get Term Status.
     */
    @JsonProperty(value = "status")
    private AddGetRefreshStatus status;

    /**
     * Tracking Id.
     */
    @JsonProperty(value = "trackingId")
    private String trackingId;

    /**
     * Get the language value.
     *
     * @return the language value
     */
    public String language() {
        return this.language;
    }

    /**
     * Set the language value.
     *
     * @param language the language value to set
     * @return the TermData object itself.
     */
    public TermData withLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Get the terms value.
     *
     * @return the terms value
     */
    public List<TermsInList> terms() {
        return this.terms;
    }

    /**
     * Set the terms value.
     *
     * @param terms the terms value to set
     * @return the TermData object itself.
     */
    public TermData withTerms(List<TermsInList> terms) {
        this.terms = terms;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public AddGetRefreshStatus status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the TermData object itself.
     */
    public TermData withStatus(AddGetRefreshStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get the trackingId value.
     *
     * @return the trackingId value
     */
    public String trackingId() {
        return this.trackingId;
    }

    /**
     * Set the trackingId value.
     *
     * @param trackingId the trackingId value to set
     * @return the TermData object itself.
     */
    public TermData withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

}
