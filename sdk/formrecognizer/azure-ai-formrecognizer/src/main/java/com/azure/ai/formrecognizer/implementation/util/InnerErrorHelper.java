// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.InnerError;

/**
 * The helper class to set the non-public properties of an {@link InnerError} instance.
 */
public final class InnerErrorHelper {
    private static InnerErrorAccessor accessor;

    private InnerErrorHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link InnerError} instance.
     */
    public interface InnerErrorAccessor {
        void setCode(InnerError innerError, String errorCode);
        void setMessage(InnerError innerError, String message);
        void setInnerError(InnerError innerError, InnerError serviceInnerError);
    }

    /**
     * The method called from {@link InnerError} to set it's accessor.
     *
     * @param innerErrorAccessor The accessor.
     */
    public static void setAccessor(final InnerErrorAccessor innerErrorAccessor) {
        accessor = innerErrorAccessor;
    }

    static void setCode(InnerError innerError, String errorCode) {
        accessor.setCode(innerError, errorCode);
    }

    static void setMessage(InnerError innerError, String message) {
        accessor.setMessage(innerError, message);
    }

    static void setInnerError(InnerError innerError, InnerError serviceInnerError) {
        accessor.setInnerError(innerError, serviceInnerError);
    }
}
