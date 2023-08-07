// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Represent Options to customize browser view.
 */
public class BrowserCustomizationOptions {
    private String htmlMessageSuccess;
    private String htmlMessageError;

    /**
     * Configures the property to set HtmlMessageSuccess which the browser will show to the user when the user
     * finishes authenticating successfully.
     *
     * @param htmlMessageSuccess the message to display when user finishes authenticating.
     * @return the updated options.
     */
    public BrowserCustomizationOptions setHtmlMessageSuccess(String htmlMessageSuccess) {
        this.htmlMessageSuccess = htmlMessageSuccess;
        return this;
    }

    /**
     * Configure the property to set HtmlMessageError which the browser will show to the user when the user
     * finishes authenticating, but an error occurred. You can use a string format e.g.
     * "An error has occurred: {0} details: {1}."
     *
     * @param htmlMessageError the message to display when user finishes authenticating, but an error occurred.
     * @return the updated options.
     */
    public BrowserCustomizationOptions setHtmlMessageError(String htmlMessageError) {
        this.htmlMessageError = htmlMessageError;
        return this;
    }

    /**
     * Get the configured message which the browser will show to the user when the user
     * finishes authenticating successfully.
     *
     * @return the string message.
     */
    public String getHtmlMessageSuccess() {
        return this.htmlMessageSuccess;
    }

    /**
     * Get the configured message which the browser will show to the user when the user
     * finishes authenticating successfully, but an error occurred.
     *
     * @return the string message.
     */
    public String getHtmlMessageError() {
        return this.htmlMessageError;
    }
}
