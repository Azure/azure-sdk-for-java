// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.condition;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class ConditionalOnAnyPropertyTests {

    private ConfigurableApplicationContext context;

    private final ConfigurableEnvironment environment = new StandardEnvironment();

    @AfterEach
    void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    void allPropertiesAreDefined() {
        load(MultiplePropertiesConfiguration.class, "property1=value1", "property2=value2");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void notAllPropertiesAreDefined() {
        load(MultiplePropertiesConfiguration.class, "property1=value1");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void propertyValueEqualsFalse() {
        load(MultiplePropertiesConfiguration.class, "property1=false");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void propertyValueEqualsFALSE() {
        load(MultiplePropertiesConfiguration.class, "property1=FALSE");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void relaxedNamePrefix() {
        load(RelaxedPropertiesConfigurationPrefix.class, "spring.cloud.azure.theRelaxedProperty=value1");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void relaxedNamePrefixesFirst() {
        load(RelaxedPropertiesConfigurationPrefixes.class, "spring.cloud.azure.property1.theRelaxedProperty=value1");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void relaxedNamePrefixesSecond() {
        load(RelaxedPropertiesConfigurationPrefixes.class, "spring.cloud.azure.property2.theRelaxedProperty=value1");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void relaxedNamePrefixes() {
        load(RelaxedPropertiesConfigurationPrefixes.class, "spring.cloud.azure.property1.theRelaxedProperty=value1, spring.cloud.azure.property2.theRelaxedProperty=value1");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void prefixWithoutPeriod() {
        load(RelaxedPropertiesRequiredConfigurationWithShortPrefix.class, "spring.cloud.azure.property=value1");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void prefixesWithoutPeriod() {
        load(RelaxedPropertiesRequiredConfigurationWithShortPrefixes.class, "spring.cloud.azure.property=value1");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void enabledIfNotConfiguredOtherwisePrefix() {
        load(EnabledIfNotConfiguredOtherwiseConfigPrefix.class);
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void enabledIfNotConfiguredOtherwiseWithConfigPrefix() {
        load(EnabledIfNotConfiguredOtherwiseConfigPrefix.class, "spring.cloud.azure.myProperty:false");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void enabledIfNotConfiguredOtherwiseWithConfigDifferentCasePrefix() {
        load(EnabledIfNotConfiguredOtherwiseConfigPrefix.class, "spring.cloud.azure.my-property:FALSE");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void enabledIfNotConfiguredOtherwisePrefixes() {
        load(EnabledIfNotConfiguredOtherwiseConfigPrefixes.class);
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void enabledIfNotConfiguredOtherwiseWithConfigPrefixes() {
        load(EnabledIfNotConfiguredOtherwiseConfigPrefixes.class, "spring.cloud.azure.myProperty:false");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void enabledIfNotConfiguredOtherwiseWithConfigDifferentCasePrefixes() {
        load(EnabledIfNotConfiguredOtherwiseConfigPrefixes.class, "spring.cloud.azure.my-property:FALSE");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void disableIfNotConfiguredOtherwisePrefix() {
        load(DisabledIfNotConfiguredOtherwiseConfigPrefix.class);
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void disableIfNotConfiguredOtherwiseWithConfigPrefix() {
        load(DisabledIfNotConfiguredOtherwiseConfigPrefix.class, "spring.cloud.azure.property:true");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void disableIfNotConfiguredOtherwiseWithConfigDifferentCasePrefix() {
        load(DisabledIfNotConfiguredOtherwiseConfigPrefix.class, "spring.cloud.azure.property:TrUe");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void disableIfNotConfiguredOtherwisePrefixes() {
        load(DisabledIfNotConfiguredOtherwiseConfigPrefixes.class);
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void disableIfNotConfiguredOtherwiseWithConfigPrefixes() {
        load(DisabledIfNotConfiguredOtherwiseConfigPrefixes.class, "spring.cloud.azure.property:true");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void disableIfNotConfiguredOtherwiseWithConfigDifferentCasePrefixes() {
        load(DisabledIfNotConfiguredOtherwiseConfigPrefixes.class, "spring.cloud.azure.property:TrUe");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void simpleValueIsSetPrefix() {
        load(SimpleValueConfigPrefix.class, "spring.cloud.azure.myProperty:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void caseInsensitivePrefix() {
        load(SimpleValueConfigPrefix.class, "spring.cloud.azure.myProperty:BaR");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void simpleValueIsSetPrefixes() {
        load(SimpleValueConfigPrefixes.class, "spring.cloud.azure.myProperty:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void caseInsensitivePrefixes() {
        load(SimpleValueConfigPrefixes.class, "spring.cloud.azure.myProperty:BaR");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void defaultValueIsSet() {
        load(DefaultValueConfig.class, "spring.cloud.azure.myProperty:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void defaultValueIsNotSet() {
        load(DefaultValueConfig.class);
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void defaultValueIsSetDifferentValue() {
        load(DefaultValueConfig.class, "spring.cloud.azure.myProperty:another");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void relaxedEnabledByDefaultPrefix() {
        load(PrefixValueConfig.class, "spring.cloud.azure.myProperty:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void relaxedEnabledByDefaultPrefixes() {
        load(PrefixesValueConfig.class, "spring.cloud.azure.myProperty:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void multiValuesAllSetPrefix() {
        load(MultiValuesConfigPrefix.class, "spring.cloud.azure.my-property:bar", "spring.cloud.azure.my-another-property:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void multiValuesAllSetPrefixes() {
        load(MultiValuesConfigPrefixes.class, "spring.cloud.azure.first.my-property:bar", "spring.cloud.azure.second.my-another-property:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void multiValuesOnlyOneSetPrefix() {
        load(MultiValuesConfigPrefix.class, "spring.cloud.azure.my-property:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void multiValuesOnlyOneSetPrefixesFirst() {
        load(MultiValuesConfigPrefixes.class, "spring.cloud.azure.first.my-property:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void multiValuesOnlyOneSetPrefixesSecond() {
        load(MultiValuesConfigPrefixes.class, "spring.cloud.azure.second.my-property:bar");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void usingValueAttribute() {
        load(ValueAttribute.class, "spring.cloud.azure.property");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void nameOrValueMustBeSpecified() {
        assertThatIllegalStateException().isThrownBy(() -> load(NoNameOrValueAttribute.class, "spring.cloud.azure.property"))
            .satisfies(causeMessageContaining(
                "The name or value attribute of @ConditionalOnAnyProperty must be specified"));
    }

    @Test
    void nameAndValueMustNotBeSpecified() {
        assertThatIllegalStateException().isThrownBy(() -> load(NameAndValueAttribute.class, "spring.cloud.azure.property"))
            .satisfies(causeMessageContaining(
                "The name and value attributes of @ConditionalOnAnyProperty are exclusive"));
    }

    private <T extends Exception> Consumer<T> causeMessageContaining(String message) {
        return (ex) -> assertThat(ex.getCause()).hasMessageContaining(message);
    }

    @Test
    void metaAnnotationConditionMatchesWhenPropertyIsSet() {
        load(MetaAnnotation.class, "spring.cloud.azure.feature.enabled=true");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void metaAnnotationConditionDoesNotMatchWhenPropertyIsNotSet() {
        load(MetaAnnotation.class);
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void metaAndDirectAnnotationConditionDoesNotMatchWhenOnlyDirectPropertyIsSet() {
        load(MetaAnnotationAndDirectAnnotation.class, "spring.cloud.azure.other.feature.enabled=true");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void metaAndDirectAnnotationConditionDoesNotMatchWhenOnlyMetaPropertyIsSet() {
        load(MetaAnnotationAndDirectAnnotation.class, "spring.cloud.azure.feature.enabled=true");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void metaAndDirectAnnotationConditionDoesNotMatchWhenNeitherPropertyIsSet() {
        load(MetaAnnotationAndDirectAnnotation.class);
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void metaAndDirectAnnotationConditionMatchesWhenBothPropertiesAreSet() {
        load(MetaAnnotationAndDirectAnnotation.class, "spring.cloud.azure.feature.enabled=true", "spring.cloud.azure.other.feature.enabled=true");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    private void load(Class<?> config, String... environment) {
        TestPropertyValues.of(environment).applyTo(this.environment);
        this.context = new SpringApplicationBuilder(config).environment(this.environment).web(WebApplicationType.NONE)
            .run();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(name = {"property1", "property2"})
    static class MultiplePropertiesConfiguration {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure", name = "the-relaxed-property")
    static class RelaxedPropertiesConfigurationPrefix {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefixes = {"spring.cloud.azure.property1", "spring.cloud.azure.property2"}, name = "the-relaxed-property")
    static class RelaxedPropertiesConfigurationPrefixes {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure", name = "property")
    static class RelaxedPropertiesRequiredConfigurationWithShortPrefix {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefixes = {"spring.cloud.azure"}, name = "property")
    static class RelaxedPropertiesRequiredConfigurationWithShortPrefixes {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure", name = "my-property", havingValue = "true", matchIfMissing = true)
    static class EnabledIfNotConfiguredOtherwiseConfigPrefix {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefixes = {"spring.cloud.azure"}, name = "my-property", havingValue = "true", matchIfMissing = true)
    static class EnabledIfNotConfiguredOtherwiseConfigPrefixes {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure", name = "property", havingValue = "true")
    static class DisabledIfNotConfiguredOtherwiseConfigPrefix {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefixes = {"spring.cloud.azure"}, name = "property", havingValue = "true")
    static class DisabledIfNotConfiguredOtherwiseConfigPrefixes {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure", name = "my-property", havingValue = "bar")
    static class SimpleValueConfigPrefix {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefixes = {"spring.cloud.azure"}, name = "my-property", havingValue = "bar")
    static class SimpleValueConfigPrefixes {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(name = "spring.cloud.azure.myProperty", havingValue = "bar", matchIfMissing = true)
    static class DefaultValueConfig {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure", name = "my-property", havingValue = "bar")
    static class PrefixValueConfig {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefixes = {"spring.cloud.azure"}, name = "my-property", havingValue = "bar")
    static class PrefixesValueConfig {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure", name = {"my-property", "my-another-property"}, havingValue = "bar")
    static class MultiValuesConfigPrefix {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(prefixes = {"spring.cloud.azure.first", "spring.cloud.azure.second"}, name = {"my-property", "my-another-property"}, havingValue = "bar")
    static class MultiValuesConfigPrefixes {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty("spring.cloud.azure.property")
    static class ValueAttribute {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty
    static class NoNameOrValueAttribute {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAnyProperty(value = "x", name = "y")
    static class NameAndValueAttribute {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMyFeature
    static class MetaAnnotation {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMyFeature
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.other.feature", name = "enabled", havingValue = "true")
    static class MetaAnnotationAndDirectAnnotation {

        @Bean
        String foo() {
            return "foo";
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.feature", name = "enabled", havingValue = "true")
    @interface ConditionalOnMyFeature {

    }

}

