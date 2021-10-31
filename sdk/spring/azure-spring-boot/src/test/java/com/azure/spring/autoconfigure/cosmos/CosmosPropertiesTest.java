// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.cosmos;


import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.NOT_VALIDATE_URI;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.PROPERTY_URI;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.PROPERTY_VALIDATE_URI;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.TEST_URI_FAIL;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.TEST_URI_HTTP;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.TEST_URI_HTTPS;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.configureCosmosProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CosmosPropertiesTest {

    @Test
    public void uriPatternFail() {
        Exception exception = null;

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        configureCosmosProperties(context);
        addInlinedPropertiesToEnvironment(
            context,
            PROPERTY_URI + "=" + TEST_URI_FAIL
        );
        context.register(Config.class);

        try {
            context.refresh();
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
        Assert.assertTrue(exception instanceof BeanCreationException);
        Assert.assertNotNull(exception.getCause());
        Assert.assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void testValidateUri() {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        configureCosmosProperties(context);
        addInlinedPropertiesToEnvironment(
            context,
            PROPERTY_URI + "=" + TEST_URI_FAIL,
            PROPERTY_VALIDATE_URI + "=" + NOT_VALIDATE_URI
        );
        context.register(Config.class);
        context.refresh();
    }

    @ParameterizedTest
    @MethodSource("localURIMatchedProvider")
    public void uriPatternLocalMatched(ArgumentsAccessor arguments) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            configureCosmosProperties(context);
            addInlinedPropertiesToEnvironment(
                context,
                PROPERTY_URI + "=" + arguments.getString(0)
            );
            context.register(Config.class);
            context.refresh();
            final CosmosProperties properties = context.getBean(CosmosProperties.class);
            assertThat(properties.getUri()).matches(CosmosProperties.LOCAL_URI_REGEX);
        }
    }

    static Stream<Arguments> localURIMatchedProvider() {
        return Stream.of(
            arguments("localhost"),
            arguments("127.0.0.1"),
            arguments("127.0.0.1:8081"),
            arguments("127.000.000.001"),
            arguments("localhost:8080"),
            arguments("localhost:8081"),
            arguments("http://localhost:443"),
            arguments("http://localhost:8081")
        );
    }


    @ParameterizedTest
    @MethodSource("localURIUnMatchedProvider")
    public void uriPatternLocalUnMatched(ArgumentsAccessor arguments) {
        Exception exception = null;

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        configureCosmosProperties(context);
        addInlinedPropertiesToEnvironment(
            context,
            PROPERTY_URI + "=" + arguments.getString(0)
        );
        context.register(Config.class);
        try {
            context.refresh();
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
        Assert.assertNotNull(exception.getCause());
        Assert.assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    static Stream<Arguments> localURIUnMatchedProvider() {
        return Stream.of(
            arguments("www.google.com"),
            arguments("http://github.com:443"),
            arguments("https://microsoft.com:8081")
        );
    }


    @Test
    public void uriPatternHttp() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            configureCosmosProperties(context);
            addInlinedPropertiesToEnvironment(
                context,
                PROPERTY_URI + "=" + TEST_URI_HTTP
            );
            context.register(Config.class);
            context.refresh();
            final CosmosProperties properties = context.getBean(CosmosProperties.class);
            assertThat(properties.getUri()).matches(CosmosProperties.URI_REGEX);
        }
    }

    @Test
    public void uriPatternHttps() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            configureCosmosProperties(context);
            addInlinedPropertiesToEnvironment(
                context,
                PROPERTY_URI + "=" + TEST_URI_HTTPS
            );
            context.register(Config.class);
            context.refresh();
            final CosmosProperties properties = context.getBean(CosmosProperties.class);
            assertThat(properties.getUri()).matches(CosmosProperties.URI_REGEX);
        }
    }

    @Test
    public void canSetAllProperties() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            configureCosmosProperties(context);
            context.register(Config.class);
            context.refresh();
            final CosmosProperties properties = context.getBean(CosmosProperties.class);

            assertThat(properties.getUri()).isEqualTo(PropertySettingUtil.URI);
            assertThat(properties.getKey()).isEqualTo(PropertySettingUtil.KEY);
            assertThat(properties.getConsistencyLevel()).isEqualTo(PropertySettingUtil.CONSISTENCY_LEVEL);
            assertThat(properties.isPopulateQueryMetrics()).isEqualTo(PropertySettingUtil.POPULATE_QUERY_METRICS);
            assertThat(properties.getConnectionMode()).isEqualTo(PropertySettingUtil.CONNECTION_MODE);
        }
    }

    @Test
    public void emptySettingNotAllowed() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            Exception exception = null;

            context.register(Config.class);

            try {
                context.refresh();
            } catch (Exception e) {
                exception = e;
            }

            assertThat(exception).isNotNull();
            assertThat(exception).isExactlyInstanceOf(ConfigurationPropertiesBindException.class);

            final BindValidationException bindException = (BindValidationException) exception.getCause().getCause();
            final List<ObjectError> errors = bindException.getValidationErrors().getAllErrors();
            final List<String> errorStrings = errors.stream().map(e -> e.toString()).collect(Collectors.toList());

            Collections.sort(errorStrings);

            final List<String> errorStringsExpected = Arrays.asList(
                "Field error in object 'azure.cosmos' on field 'database': rejected value [null];",
                "Field error in object 'azure.cosmos' on field 'key': rejected value [null];",
                "Field error in object 'azure.cosmos' on field 'uri': rejected value [null];"
            );

            assertThat(errorStrings.size()).isEqualTo(errorStringsExpected.size());

            for (int i = 0; i < errorStrings.size(); i++) {
                assertThat(errorStrings.get(i)).contains(errorStringsExpected.get(i));
            }
        }
    }

    @Configuration
    @EnableConfigurationProperties(CosmosProperties.class)
    static class Config {
    }
}

