// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.notificationhubs.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Map;

public class BaiduTemplateRegistrationDescription extends RegistrationDescription implements TemplateRegistrationDescription {

    @JacksonXmlProperty(localName = "BodyTemplate")
    private String bodyTemplate;

    /**
     * Gets the body template.
     *
     * @return The body template.
     */
    @Override
    public String getBodyTemplate() {
        return this.bodyTemplate;
    }

    /**
     * Sets the body template.
     *
     * @param value The body template to set.
     */
    @Override
    public void setBodyTemplate(String value) {
        this.bodyTemplate = value;
    }

    /**
     * Gets the HTTP headers for the template.
     *
     * @return The HTTP headers for the template.
     */
    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    /**
     * Adds a header to the HTTP headers collection.
     *
     * @param headerName  The HTTP header name.
     * @param headerValue The HTTP header value.
     */
    @Override
    public void addHeader(String headerName, String headerValue) {

    }

    /**
     * Removes an HTTP header from the collection.
     *
     * @param headerName The name of the HTTP header to remove.
     */
    @Override
    public void removeHeader(String headerName) {

    }

    /**
     * Clears the HTTP headers for the template registration description.
     */
    @Override
    public void clearHeaders() {

    }
}
