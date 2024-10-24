// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.query.NonStreamingOrderByBadRequestException;

import java.io.Serial;

public class CosmosNonStreamingOrderByBadRequestException extends NonStreamingOrderByBadRequestException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of the NonStreamingOrderByBadRequestException class.
     *
     * @param statusCode the http status code of the response.
     * @param errorMessage the error message.
     */
    public CosmosNonStreamingOrderByBadRequestException(int statusCode, String errorMessage) {
        super(statusCode, errorMessage);
    }

}
