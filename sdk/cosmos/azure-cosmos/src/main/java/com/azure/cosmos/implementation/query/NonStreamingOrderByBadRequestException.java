// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.CosmosException;

public class NonStreamingOrderByBadRequestException extends CosmosException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of the NonStreamingOrderByBadRequestException class.
     *
     * @param statusCode the http status code of the response.
     * @param errorMessage the error message.
     */
    public NonStreamingOrderByBadRequestException(int statusCode, String errorMessage) {
        super(statusCode, errorMessage);
    }
}
