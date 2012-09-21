package com.microsoft.windowsazure.services.media.implementation.content;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking a type as a data transfer object
 * to or from Media Services.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MediaServiceDTO {

}
