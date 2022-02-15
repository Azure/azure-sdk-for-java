package com.azure.sdk.build.tool.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Objects;

public class AnnotatedMethodCallerResult {
    private final Class<? extends Annotation> annotation;
    private final Method annotatedMethod;
    private final Member callingMember;

    public AnnotatedMethodCallerResult(final Class<? extends Annotation> annotation,
                                       final Method annotatedMethod,
                                       final Member callingMember) {
        this.annotation = Objects.requireNonNull(annotation);
        this.annotatedMethod = Objects.requireNonNull(annotatedMethod);
        this.callingMember = Objects.requireNonNull(callingMember);
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
