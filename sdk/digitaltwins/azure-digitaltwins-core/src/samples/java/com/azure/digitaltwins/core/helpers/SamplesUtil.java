// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.helpers;

import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;

import java.util.function.Consumer;

import static javax.net.ssl.HttpsURLConnection.HTTP_CONFLICT;
import static javax.net.ssl.HttpsURLConnection.HTTP_NOT_FOUND;

public class SamplesUtil {
    public static final Consumer<Throwable> IGNORE_NOT_FOUND_ERROR = throwable -> {
        if (!(throwable instanceof ErrorResponseException) || ((ErrorResponseException) throwable).getResponse().getStatusCode() != HTTP_NOT_FOUND) {
            ConsoleLogger.printFatal("Error received: " + throwable);
        }
    };

    public static final Consumer<Throwable> IGNORE_CONFLICT_ERROR = throwable -> {
        if (!(throwable instanceof ErrorResponseException) || ((ErrorResponseException) throwable).getResponse().getStatusCode() != HTTP_CONFLICT) {
            ConsoleLogger.printFatal("Error received: " + throwable);
        }
    };
}
