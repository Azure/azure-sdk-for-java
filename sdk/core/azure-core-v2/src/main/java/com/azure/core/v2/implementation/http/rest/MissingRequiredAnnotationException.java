// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation.http.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * An exception thrown when a Swagger interface is parsed and it is missing required annotations.
 */
public class MissingRequiredAnnotationException extends RuntimeException {
    /**
     * Create a new MissingRequiredAnnotationException for the provided missing required annotation on the provided
     * swaggerInterface.
     *
     * @param requiredAnnotation The annotation that is required.
     * @param swaggerInterface The swagger interface that is missing the required annotation.
     */
    public MissingRequiredAnnotationException(Class<? extends Annotation> requiredAnnotation,
        Class<?> swaggerInterface) {
        super("A " + getAnnotationName(requiredAnnotation) + " annotation must be defined on "
            + swaggerInterface.getName() + ".");
    }

    /**
     * Create a new MissingRequiredAnnotationException for the provided missing required annotation options on the
     * provided swaggerInterface method.
     *
     * @param requiredAnnotationOptions The options for the annotation that is required.
     * @param swaggerInterfaceMethod The swagger interface method that is missing the required annotation.
     */
    public MissingRequiredAnnotationException(List<Class<? extends Annotation>> requiredAnnotationOptions,
        Method swaggerInterfaceMethod) {
        super("Either " + optionsToString(requiredAnnotationOptions) + " annotation must be defined on the method "
            + methodFullName(swaggerInterfaceMethod) + ".");
    }

    private static String getAnnotationName(Class<? extends Annotation> annotation) {
        return annotation.getSimpleName();
    }

    private static String optionsToString(List<Class<? extends Annotation>> requiredAnnotationOptions) {
        final StringBuilder result = new StringBuilder();

        final int optionCount = requiredAnnotationOptions.size();
        for (int i = 0; i < optionCount; ++i) {
            if (1 <= i) {
                result.append(", ");
            }
            if (i == optionCount - 1) {
                result.append("or ");
            }
            result.append(getAnnotationName(requiredAnnotationOptions.get(i)));
        }

        return result.toString();
    }

    private static String methodFullName(Method swaggerInterfaceMethod) {
        return swaggerInterfaceMethod.getDeclaringClass().getName() + "." + swaggerInterfaceMethod.getName() + "()";
    }
}
