// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.models;

/**
 * Represent Options to customize browser view.
 */
public class BrowserCustomizationOptions {
    private String successMessage;
    private String errorMessage;

    /**
     * Creates an instance of BrowserCustomizationOptions.
     */
    public BrowserCustomizationOptions() {
    }

    /**
     * Configures the property to set the success message which the browser will show to the user upon successful
     * authentication.
     *
     * @param successMessage the message to display when user finishes authenticating.
     * @return the updated options.
     */
    public BrowserCustomizationOptions setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
        return this;
    }

    /**
     * Configure the property to set error message which the browser will show to the user upon failed to acquire an
     * access token.
     * You can use a string format e.g.
     * "An error has occurred: {0} details: {1}.", the details will be populated by the library.
     *
     * @param errorMessage the message to display when user finishes authenticating, but an error occurred.
     * @return the updated options.
     */
    public BrowserCustomizationOptions setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Get the configured message which the browser will show to the user when the user
     * finishes authenticating successfully.
     *
     * @return the string message.
     */
    public String getSuccessMessage() {
        return this.successMessage;
    }

    /**
     * Get the configured message which the browser will show to the user when the user
     * finishes authenticating, but an error occurred.
     *
     * @return the string message.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
