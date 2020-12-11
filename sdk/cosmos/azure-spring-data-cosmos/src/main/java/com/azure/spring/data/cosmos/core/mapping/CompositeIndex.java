package com.azure.spring.data.cosmos.core.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CompositeIndex {

    /**
     * Array of composite index paths
     * @return String
     */
    CompositeIndexPath[] paths() default {};
}
