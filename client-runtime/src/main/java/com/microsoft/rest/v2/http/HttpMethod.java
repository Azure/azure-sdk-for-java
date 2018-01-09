/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.v2.ExpandableStringEnum;

import java.util.Collection;

/**
 * The different HTTP methods of a HttpRequest.
 */
public class HttpMethod extends ExpandableStringEnum<HttpMethod> {
    /**
     * The HTTP GET method.
     */
    public static final HttpMethod GET = fromString("GET");

    /**
     * The HTTP PUT method.
     */
    public static final HttpMethod PUT = fromString("PUT");

    /**
     * The HTTP POST method.
     */
    public static final HttpMethod POST = fromString("POST");

    /**
     * The HTTP PATCH method.
     */
    public static final HttpMethod PATCH = fromString("PATCH");

    /**
     * The HTTP DELETE method.
     */
    public static final HttpMethod DELETE = fromString("DELETE");

    /**
     * The HTTP HEAD method.
     */
    public static final HttpMethod HEAD = fromString("HEAD");

    /**
     * Creates or finds a HttpMethod from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding HttpMethod.
     */
    @JsonCreator
    public static HttpMethod fromString(String name) {
        return fromString(name, HttpMethod.class);
    }

    /**
     * @return known HttpMethod values.
     */
    public static Collection<HttpMethod> values() {
        return values(HttpMethod.class);
    }
}
