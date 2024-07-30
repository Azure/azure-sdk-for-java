// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.exception;

import io.clientcore.core.util.ExpandableEnum;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents exception types for HTTP requests and responses.
 */
public final class HttpExceptionType implements ExpandableEnum<String> {
    private static final Map<String, HttpExceptionType> VALUES = new ConcurrentHashMap<>();

    private final String caseSensitive;
    private final String caseInsensitive;

    private HttpExceptionType(String name) {
        this.caseSensitive = name;
        this.caseInsensitive = name.toLowerCase();
    }

    @Override
    public String getValue() {
        return caseSensitive;
    }

    /**
     * Gets all known {@link HttpExceptionType} values.
     *
     * @return The known {@link HttpExceptionType} values.
     */
    public static Collection<HttpExceptionType> values() {
        return VALUES.values();
    }

    /**
     * Creates or finds a {@link HttpExceptionType} for the passed {@code name}.
     *
     * <p>{@code null} will be returned if {@code name} is {@code null}.</p>
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link HttpExceptionType} of the provided name, or {@code null} if {@code name} was
     * {@code null}.
     */
    public static HttpExceptionType fromString(String name) {
        if (name == null) {
            return null;
        }

        HttpExceptionType exceptionType = VALUES.get(name);

        if (exceptionType != null) {
            return exceptionType;
        }

        return VALUES.computeIfAbsent(name, HttpExceptionType::new);
    }

    @Override
    public String toString() {
        return caseSensitive;
    }

    @Override
    public int hashCode() {
        return caseInsensitive.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof HttpExceptionType)) {
            return false;
        }

        HttpExceptionType other = (HttpExceptionType) obj;

        return Objects.equals(caseInsensitive, other.caseInsensitive);
    }

    /**
     * The exception thrown when failing to authenticate the HTTP request with status code of {@code 4XX}, typically
     * {@code 401 Unauthorized}.
     *
     * <p>A runtime exception indicating request authorization failure caused by one of the following scenarios:</p>
     * <ul>
     *     <li>A client did not send the required authorization credentials to access the requested resource, i.e.
     *     Authorization HTTP header is missing in the request</li>
     *     <li>If the request contains the HTTP Authorization header, then the exception indicates that authorization
     *     has been refused for the credentials contained in the request header.</li>
     * </ul>
     */
    public static final HttpExceptionType CLIENT_AUTHENTICATION = fromString("CLIENT_AUTHENTICATION");

    /**
     * The exception thrown when the HTTP request tried to create an already existing resource and received a status
     * code {@code 4XX}, typically {@code 412 Conflict}.
     */
    public static final HttpExceptionType RESOURCE_EXISTS = fromString("RESOURCE_EXISTS");

    /**
     * The exception thrown for invalid resource modification with status code of {@code 4XX}, typically
     * {@code 409 Conflict}.
     */
    public static final HttpExceptionType RESOURCE_MODIFIED = fromString("RESOURCE_MODIFIED");

    /**
     * The exception thrown when receiving an error response with status code {@code 412 response} (for update) or
     * {@code 404 Not Found} (for get/post).
     */
    public static final HttpExceptionType RESOURCE_NOT_FOUND = fromString("RESOURCE_NOT_FOUND");

    /**
     * This exception thrown when an HTTP request has reached the maximum number of redirect attempts with a status code
     * of {@code 3XX}.
     */
    public static final HttpExceptionType TOO_MANY_REDIRECTS = fromString("TOO_MANY_REDIRECTS");
}
