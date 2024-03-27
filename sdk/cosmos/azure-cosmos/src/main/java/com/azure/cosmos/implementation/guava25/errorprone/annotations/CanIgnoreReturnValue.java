package com.azure.cosmos.implementation.guava25.errorprone.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that the return value of the annotated API is ignorable.
 *
 * <p>This is the opposite of {@link CheckReturnValue}. It can be used inside classes or packages
 * annotated with {@code @CheckReturnValue} to exempt specific APIs from the default.
 */
@Documented
// Note: annotating a type with @CanIgnoreReturnValue is discouraged (and banned inside of Google)
@Target({METHOD, CONSTRUCTOR, TYPE})
@Retention(CLASS)
public @interface CanIgnoreReturnValue {}
