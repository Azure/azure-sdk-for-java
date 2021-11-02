package com.azure.spring.cloud.autoconfigure.validation;

import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Created by zhihao.guo on 2021/11/2.
 */
public class AzureEndpointValidator implements ConstraintValidator<AzureEndpoint, String> {

    public static final String COSMOS_URI_REGEX = "http[s]{0,1}://.*.documents.azure.com.*";
    public static final String COSMOS_LOCAL_URI_REGEX = "^(http[s]{0,1}://)*localhost.*|^127(?:\\.[0-9]+){0,2}\\.[0-9]+.*";

    /**
     */
    AzureEndpoint.AzureService[] azureServices;

    @Autowired(required = false)
    AzureCosmosProperties azureCosmosProperties;

    @Override
    public void initialize(AzureEndpoint constraintAnnotation) {
        this.azureServices = constraintAnnotation.azureServices();
    }

    @Override
    public boolean isValid(String uri, ConstraintValidatorContext constraintValidatorContext) {
        if (azureServices == null) {
            return true;
        }
        for (AzureEndpoint.AzureService azureService : azureServices) {
            if (validate(uri, azureService)) {
                return true;
            }
        }
        return false;
    }

    private boolean validate(String uri, AzureEndpoint.AzureService azureService) {
        if (azureService.equals(AzureEndpoint.AzureService.COSMOS_CORE)) {
            // @todo
            //if (!azureCosmosProperties()) {
            //    return true;
            //}
            if (Pattern.matches(COSMOS_LOCAL_URI_REGEX, uri)) {
                return true;
            }
            if (Pattern.matches(COSMOS_URI_REGEX, uri)) {
                return true;
            }
            return false;
        }
        return false;
    }
}
