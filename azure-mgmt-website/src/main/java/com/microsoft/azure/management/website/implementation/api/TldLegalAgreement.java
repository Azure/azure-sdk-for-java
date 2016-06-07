/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Represents a legal agreement for top level domain.
 */
public class TldLegalAgreement {
    /**
     * Unique identifier for the agreement.
     */
    private String agreementKey;

    /**
     * Agreement title.
     */
    private String title;

    /**
     * Agreement details.
     */
    private String content;

    /**
     * Url where a copy of the agreement details is hosted.
     */
    private String url;

    /**
     * Get the agreementKey value.
     *
     * @return the agreementKey value
     */
    public String agreementKey() {
        return this.agreementKey;
    }

    /**
     * Set the agreementKey value.
     *
     * @param agreementKey the agreementKey value to set
     * @return the TldLegalAgreement object itself.
     */
    public TldLegalAgreement withAgreementKey(String agreementKey) {
        this.agreementKey = agreementKey;
        return this;
    }

    /**
     * Get the title value.
     *
     * @return the title value
     */
    public String title() {
        return this.title;
    }

    /**
     * Set the title value.
     *
     * @param title the title value to set
     * @return the TldLegalAgreement object itself.
     */
    public TldLegalAgreement withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the content value.
     *
     * @return the content value
     */
    public String content() {
        return this.content;
    }

    /**
     * Set the content value.
     *
     * @param content the content value to set
     * @return the TldLegalAgreement object itself.
     */
    public TldLegalAgreement withContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the TldLegalAgreement object itself.
     */
    public TldLegalAgreement withUrl(String url) {
        this.url = url;
        return this;
    }

}
