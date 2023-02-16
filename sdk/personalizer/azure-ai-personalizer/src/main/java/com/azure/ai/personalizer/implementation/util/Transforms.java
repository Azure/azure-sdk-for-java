// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.implementation.util;

import com.azure.ai.personalizer.models.ErrorResponseException;
import com.azure.ai.personalizer.models.InternalError;
import com.azure.ai.personalizer.models.PersonalizerError;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.models.ResponseError;

public class Transforms {
    public static Throwable mapToHttpResponseExceptionIfExists(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException errorResponseException = (ErrorResponseException) throwable;
            PersonalizerError error = null;
            if (errorResponseException.getValue() != null && errorResponseException.getValue().getError() != null) {
                error = (errorResponseException.getValue().getError());
            }
            return new HttpResponseException(
                errorResponseException.getMessage(),
                errorResponseException.getResponse(),
                toResponseError(error)
            );
        }
        return throwable;
    }

    private static ResponseError toResponseError(PersonalizerError error) {
        if (error == null) {
            return null;
        }
        InternalError innerError = error.getInnerError();
        String message = error.getMessage();
        StringBuilder errorInformationStringBuilder = new StringBuilder().append(message);

        if (innerError != null) {
            errorInformationStringBuilder.append(", " + "errorCode" + ": [")
                .append(innerError.getCode()).append("]");
        }
        return new ResponseError(error.getCode().toString(), errorInformationStringBuilder.toString());
    }
}
