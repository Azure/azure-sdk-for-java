package com.azure.spring.cloud.autoconfigure.validation;

import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

/**
 */
public class AzureEndpointValidator implements Validator {

    public static final String COSMOS_URI_REGEX = "http[s]{0,1}://.*.documents.azure.com.*";
    public static final String COSMOS_LOCAL_URI_REGEX = "^(http[s]{0,1}://)*localhost.*|^127(?:\\.[0-9]+){0,2}\\.[0-9]+.*";

    @Override
    public boolean supports(Class<?> aClass) {
        return AzureCosmosProperties.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof AzureEndpointValidator) {
            AzureCosmosProperties azureCosmosProperties = (AzureCosmosProperties) target;
            if (!azureCosmosProperties.isNeedvalidate()) {
                return;
            }
            String endpoint = azureCosmosProperties.getEndpoint();

            if (Pattern.matches(COSMOS_LOCAL_URI_REGEX, endpoint)) {
                return ;
            }
            if (Pattern.matches(COSMOS_URI_REGEX, endpoint)) {
                return ;
            }
            errors.rejectValue("uri", "field.domain.required",
                                      "cosmos uri not valid");
        }


    }
}
