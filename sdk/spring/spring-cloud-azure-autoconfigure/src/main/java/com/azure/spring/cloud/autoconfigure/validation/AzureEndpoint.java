package com.azure.spring.cloud.autoconfigure.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 *
 * @author zhihaoguo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Constraint(validatedBy = AzureEndpointValidator.class)
public @interface AzureEndpoint {

    String message() default "the uri is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    AzureEndpoint.AzureService[] azureServices() default {AzureService.COSMOS_CORE,AzureService.STORAGE_BLOB};

    public static enum AzureService {
        COSMOS_CORE(1),
        STORAGE_BLOB(2);

        private final int value;

        private AzureService(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
