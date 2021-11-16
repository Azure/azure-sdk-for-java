// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.spring.cloud.autoconfigure.cosmos.properties.AzureCosmosProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

class AzureCosmosPropertiesTest {

    static final String TEST_ENDPOINT_HTTPS = "https://test.https.documents.azure.com:443/";
    static final String TEST_ENDPOINT_HTTP = "http://test.http.documents.azure.com:443/";

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testEmptySettings() {
        AzureCosmosProperties cosmosProperties = new AzureCosmosProperties();

        Set<ConstraintViolation<AzureCosmosProperties>> violations = validator.validate(cosmosProperties);
        Assertions.assertEquals(1, violations.size());
    }

    @Test
    void testWithHttpEndpointPattern() {
        AzureCosmosProperties cosmosProperties = new AzureCosmosProperties();
        cosmosProperties.setEndpoint(TEST_ENDPOINT_HTTP);
        cosmosProperties.setKey("test-key");


        Set<ConstraintViolation<AzureCosmosProperties>> violations = validator.validate(cosmosProperties);
        Assertions.assertTrue(violations.isEmpty());
    }

}

