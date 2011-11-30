/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An attribute used to describe a method that will make a request to the storage service.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoesServiceRequest {
    // No attributes
}
