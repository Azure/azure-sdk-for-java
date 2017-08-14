/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class contains all of the details necessary for reacting to a HTTP response from a
 * HttpResponse.
 */
public interface HttpResponse {
    /**
     * Get whether or not this response object has a body.
     * @return Whether or not this response object has a body.
     */
    boolean hasBody();

    /**
     * Get this response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     * @return This response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     */
    InputStream getBodyAsInputStream();

    /**
     * Get this response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     * @throws IOException On network issues.
     */
    byte[] getBodyAsByteArray() throws IOException;

    /**
     * Get this response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     * @throws IOException On network issues.
     */
    String getBodyAsString() throws IOException;
}
