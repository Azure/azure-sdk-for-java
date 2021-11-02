package com.azure.spring.autoconfigure.cosmos;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Constraint(validatedBy = CosmosUriValidator.class)
public @interface ValidCosmosUri {

    /**
     * whether to validate or not
     */
    boolean required() default true;

    /**
     * Error message when validation is failed.
     *
     * @return the error message
     */
    String message() default "the uri's pattern specified in 'azure.cosmos.uri' is not supported, \"\n" +
        "                + \"only sql/core api is supported, please check https://docs.microsoft.com/en-us/azure/cosmos-db/ \"\n" +
        "                + \"for more info.\"";

    /**
     *
     * @return groups
     */
    Class<?>[] groups() default {};

    /**
     *
     * @return playload
     */
    Class<? extends Payload>[] payload() default {};
}
