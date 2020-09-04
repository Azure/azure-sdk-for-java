// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import org.apache.http.HttpStatus;

import java.util.function.Consumer;

public class SamplesUtil {
    public static final Consumer<Throwable> IgnoreNotFoundError = throwable -> {
        if (!(throwable instanceof ErrorResponseException) || ((ErrorResponseException) throwable).getResponse().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
            System.err.println("Error received: " + throwable);
        }
    };

    public static final Consumer<Throwable> IgnoreConflictError = throwable -> {
        if (!(throwable instanceof ErrorResponseException) || ((ErrorResponseException) throwable).getResponse().getStatusCode() != HttpStatus.SC_CONFLICT) {
            System.err.println("Error received: " + throwable);
        }
    };
}
