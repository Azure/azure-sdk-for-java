// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.notificationhubs.models;

import java.util.List;
import java.util.Map;

/**
 * Represents a Template Registration Description type.
 */
public interface TemplateRegistrationDescription {

    /**
     * Gets the body template.
     * @return The body template.
     */
    String getBodyTemplate();

    /**
     * Sets the body template.
     * @param value The body template to set.
     */
    void setBodyTemplate(String value);

    /**
     * Gets the HTTP headers for the template.
     * @return The HTTP headers for the template.
     */
    Map<String, String> getHeaders();

    /**
     * Adds a header to the HTTP headers collection.
     * @param headerName The HTTP header name.
     * @param headerValue The HTTP header value.
     */
    void addHeader(String headerName, String headerValue);

    /**
     * Removes an HTTP header from the collection.
     * @param headerName The name of the HTTP header to remove.
     */
    void removeHeader(String headerName);

    /**
     * Clears the HTTP headers for the template registration description.
     */
    void clearHeaders();
}
