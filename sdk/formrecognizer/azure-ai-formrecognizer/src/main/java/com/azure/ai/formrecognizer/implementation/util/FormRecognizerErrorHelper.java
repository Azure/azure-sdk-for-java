// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;
import com.azure.ai.formrecognizer.administration.models.InnerError;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link FormRecognizerError} instance.
 */
public final class FormRecognizerErrorHelper {
    private static FormRecognizerErrorAccessor accessor;

    private FormRecognizerErrorHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link FormRecognizerError} instance.
     */
    public interface FormRecognizerErrorAccessor {
        void setCode(FormRecognizerError formRecognizerError, String errorCode);
        void setMessage(FormRecognizerError formRecognizerError, String message);

        void setTarget(FormRecognizerError formRecognizerError, String target);

        void setDetails(FormRecognizerError formRecognizerError, List<FormRecognizerError> details);

        void setInnerError(FormRecognizerError formRecognizerError, InnerError innerError);
    }

    /**
     * The method called from {@link FormRecognizerError} to set it's accessor.
     *
     * @param formRecognizerErrorAccessor The accessor.
     */
    public static void setAccessor(final FormRecognizerErrorAccessor formRecognizerErrorAccessor) {
        accessor = formRecognizerErrorAccessor;
    }

    static void setCode(FormRecognizerError formRecognizerError, String errorCode) {
        accessor.setCode(formRecognizerError, errorCode);
    }

    static void setMessage(FormRecognizerError formRecognizerError, String message) {
        accessor.setMessage(formRecognizerError, message);
    }

    static void setTarget(FormRecognizerError formRecognizerError, String target) {
        accessor.setTarget(formRecognizerError, target);
    }

    static void setDetails(FormRecognizerError formRecognizerError, List<FormRecognizerError> details) {
        accessor.setDetails(formRecognizerError, details);
    }

    static void setInnerError(FormRecognizerError formRecognizerError, InnerError innerError) {
        accessor.setInnerError(formRecognizerError, innerError);
    }
}
