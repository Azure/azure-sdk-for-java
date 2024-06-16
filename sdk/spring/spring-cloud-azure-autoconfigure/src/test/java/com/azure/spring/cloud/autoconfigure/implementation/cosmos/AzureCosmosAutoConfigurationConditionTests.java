package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import com.azure.cosmos.CosmosClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureCosmosAutoConfigurationConditionTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void allSatisfied() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.cosmos.enabled=true",
                "spring.cloud.azure.cosmos.endpoint=not-used"
            )
            .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void classpathNotSatisfy() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withClassLoader(new FilteredClassLoader(CosmosClientBuilder.class))
            .withPropertyValues(
                "spring.cloud.azure.cosmos.enabled=true",
                "spring.cloud.azure.cosmos.endpoint=not-used"
            )
            .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
            .run(match(false));
    }

    @Test
    void enabledPropertyNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.cosmos.endpoint=not-used"
            )
            .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void enabledPropertySetToFalse() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.cosmos.enabled=false",
                "spring.cloud.azure.cosmos.endpoint=not-used"
            )
            .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
            .run(match(false));
    }

    @Test
    void onlyEndPointNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.cosmos.enabled=true"
            )
            .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void onlyBeanNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.cosmos.enabled=true",
                "spring.cloud.azure.cosmos.endpoint=not-used"
            )
            .run(match(true));
    }

    @Test
    void neitherEndPointNorBeanSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.cosmos.enabled=true"
            )
            .run(match(false));
    }

    private ContextConsumer<AssertableApplicationContext> match(boolean expected) {
        return (context) -> {
            if (expected) {
                assertThat(context).hasBean(Config.TEST_BEAN_NAME);
            }
            else {
                assertThat(context).doesNotHaveBean(Config.TEST_BEAN_NAME);
            }
        };
    }

    @Conditional(AzureCosmosAutoConfigurationCondition.class)
    private static class Config {
        public static String TEST_BEAN_NAME = "testBean";
        @Bean
        String testBean() {
            return TEST_BEAN_NAME;
        }
    }
}
