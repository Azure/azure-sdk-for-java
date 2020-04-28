package com.azure.search.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that indicates the field is to be ignored by converting to SearchField.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldIgnore {
    /**
     * Optional argument that defines whether this annotation is active or not.
     *
     * @return True if annotation is enabled (by default); false if it is to be ignored.
     */
    boolean value() default true;
}
