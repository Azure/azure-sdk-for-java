// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Represents the result of all the calls to methods annotated with a given annotation.
 */
public class AnnotatedMethodCallerResult {
    private final Class<? extends Annotation> annotation;
    private final Method annotatedMethod;
    private final Member callingMember;

    /**
     * Creates an instance of {@link AnnotatedMethodCallerResult}.
     *
     * @param annotation The annotation that was used to find the annotated method.
     * @param annotatedMethod The method that was annotated with the annotation.
     * @param callingMember The member that called the annotated method.
     */
    public AnnotatedMethodCallerResult(final Class<? extends Annotation> annotation,
                                       final Method annotatedMethod,
                                       final Member callingMember) {
        this.annotation = Objects.requireNonNull(annotation);
        this.annotatedMethod = Objects.requireNonNull(annotatedMethod);
        this.callingMember = Objects.requireNonNull(callingMember);
    }

    /**
     * Returns the method that was annotated with the annotation.
     * @return The method that was annotated with the annotation.
     */
    public Method getAnnotatedMethod() {
        return annotatedMethod;
    }

    @Override
    public String toString() {
        return "Method " + annotatedMethod + " is annotated with @" + annotation.getSimpleName() + " and called by " + callingMember;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AnnotatedMethodCallerResult that = (AnnotatedMethodCallerResult) o;
        return annotation.getSimpleName().equals(that.annotation.getSimpleName())
                   && annotatedMethod.getName().equals(that.annotatedMethod.getName())
                   && callingMember.getName().equals(that.callingMember.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotation.getSimpleName(), annotatedMethod.getName(), callingMember.getName());
    }
}
