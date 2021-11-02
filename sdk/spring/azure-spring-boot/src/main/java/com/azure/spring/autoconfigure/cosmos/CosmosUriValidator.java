package com.azure.spring.autoconfigure.cosmos;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * CosmosUriValidator class for ValidCosmosUri.
 * @author zhihaoguo
 */
public class CosmosUriValidator implements ConstraintValidator<ValidCosmosUri, String> {

    public static final String URI_REGEX = "http[s]{0,1}://.*.documents.azure.com.*";

    public static final String LOCAL_URI_REGEX = "^(http[s]{0,1}://)*localhost.*|^127(?:\\.[0-9]+){0,2}\\.[0-9]+.*";

    /**
     * whether to validate or not
     */
    private boolean required;

    @Autowired
    CosmosProperties cosmosProperties;

    @Override
    public void initialize(ValidCosmosUri constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String uri, ConstraintValidatorContext constraintValidatorContext) {
        if (!cosmosProperties.isValidateUri()) {
            return true;
        }
        if (Pattern.matches(LOCAL_URI_REGEX, uri)) {
            return true;
        }
        if (Pattern.matches(URI_REGEX, uri)) {
            return true;
        }
        return false;
    }
}
