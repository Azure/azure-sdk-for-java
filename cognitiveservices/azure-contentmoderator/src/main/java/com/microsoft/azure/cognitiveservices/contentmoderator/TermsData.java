/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * All term Id response properties.
 */
public class TermsData {
    /**
     * Language of the terms.
     */
    @JsonProperty(value = "Language")
    private String language;

    /**
     * List of terms.
     */
    @JsonProperty(value = "Terms")
    private List<TermsInList> terms;

    /**
     * Term Status.
     */
    @JsonProperty(value = "Status")
    private Status status;

    /**
     * Tracking Id.
     */
    @JsonProperty(value = "TrackingId")
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
     * @return the TermsData object itself.
     */
    public TermsData withLanguage(String language) {
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
     * @return the TermsData object itself.
     */
    public TermsData withTerms(List<TermsInList> terms) {
        this.terms = terms;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public Status status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the TermsData object itself.
     */
    public TermsData withStatus(Status status) {
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
     * @return the TermsData object itself.
     */
    public TermsData withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

}
