/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * The IIS handler mappings used to define which handler processes HTTP
 * requests with certain extension.
 * For example it is used to configure php-cgi.exe process to
 * handle all HTTP requests with *.php extension.
 */
public class HandlerMapping {
    /**
     * Requests with this extension will be handled using the specified
     * FastCGI application.
     */
    private String extension;

    /**
     * The absolute path to the FastCGI application.
     */
    private String scriptProcessor;

    /**
     * Command-line arguments to be passed to the script processor.
     */
    private String arguments;

    /**
     * Get the extension value.
     *
     * @return the extension value
     */
    public String extension() {
        return this.extension;
    }

    /**
     * Set the extension value.
     *
     * @param extension the extension value to set
     * @return the HandlerMapping object itself.
     */
    public HandlerMapping withExtension(String extension) {
        this.extension = extension;
        return this;
    }

    /**
     * Get the scriptProcessor value.
     *
     * @return the scriptProcessor value
     */
    public String scriptProcessor() {
        return this.scriptProcessor;
    }

    /**
     * Set the scriptProcessor value.
     *
     * @param scriptProcessor the scriptProcessor value to set
     * @return the HandlerMapping object itself.
     */
    public HandlerMapping withScriptProcessor(String scriptProcessor) {
        this.scriptProcessor = scriptProcessor;
        return this;
    }

    /**
     * Get the arguments value.
     *
     * @return the arguments value
     */
    public String arguments() {
        return this.arguments;
    }

    /**
     * Set the arguments value.
     *
     * @param arguments the arguments value to set
     * @return the HandlerMapping object itself.
     */
    public HandlerMapping withArguments(String arguments) {
        this.arguments = arguments;
        return this;
    }

}
