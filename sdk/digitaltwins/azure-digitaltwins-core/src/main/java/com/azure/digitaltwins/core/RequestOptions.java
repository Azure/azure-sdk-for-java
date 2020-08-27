// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;

/**
 * General request options that are applicable, but optional, for many APIs.
 */
@Fluent
public class RequestOptions {

    private String ifMatch;

    /**
     * Gets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     */
    public String getIfMatch() {
        return ifMatch;
    }

    /**
     * Sets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     *
     * <p> The request's operation is performed only if this ETag matches the value maintained by the server,
     * indicating that the entity has not been modified since it was last retrieved.
     * To force the operation to execute only if the entity exists, set the ETag to the wildcard character '*'.
     * To force the operation to execute unconditionally, leave this value null.
     * If this value is not set, it defaults to null, and the ifMatch header will not be sent with the request.
     * This means that update and delete will be unconditional and the operation will execute regardless of the existence of the resource. </p>
     *
     * @param ifMatch A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return The RequestOptions object itself.
     */
    public RequestOptions setIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }
}
