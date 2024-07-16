// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadResourceServerHttpSecurityConfigurer;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.resourceServerRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.withResourceServerPropertyValues;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadResourceServerHttpSecurityConfigurer.aadResourceServer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AadResourceServerConfigurationTests {

    @Test
    void testNotExistBearerTokenAuthenticationToken() {
        resourceServerContextRunner()
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .run(context -> assertThrows(IllegalStateException.class,
                () -> context.getBean(JWTClaimsSetAwareJWSKeySelector.class)));
    }

    @Test
    void testCreateJwtDecoderByJwkKeySetUri() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
            });
    }

    @Test
    void testNotAudienceDefaultValidator() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                AadResourceServerConfiguration bean = context
                    .getBean(AadResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator(properties);
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }

    @Test
    void testExistAudienceDefaultValidator() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                AadResourceServerConfiguration bean = context
                    .getBean(AadResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator(properties);
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }

    @Test
    void testResourceServerHttpSecurityConfigured() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                SecurityFilterChain filterChain = context.getBean(SecurityFilterChain.class);
                assertThat(filterChain).isNotNull();
                assertEquals(1,
                    filterChain.getFilters()
                               .stream()
                               .filter(filter -> filter.getClass().equals(BearerTokenAuthenticationFilter.class))
                               .count());
            });
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void useCustomJwtGrantedAuthoritiesConverter() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withUserConfiguration(
                AzureGlobalPropertiesAutoConfiguration.class,
                TestAadResourceServerConfiguration.class,
                AadAutoConfiguration.class)
            .withPropertyValues(withResourceServerPropertyValues())
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .run(context -> {
                SecurityFilterChain filterChain = context.getBean(SecurityFilterChain.class);
                assertThat(filterChain).isNotNull();
                TestAadResourceServerConfiguration resourceServerConfiguration = context.getBean(TestAadResourceServerConfiguration.class);
                assertThat(resourceServerConfiguration).isNotNull();

                TestJwtGrantedAuthoritiesConverter authoritiesConverter = context.getBean(TestJwtGrantedAuthoritiesConverter.class);
                assertThat(authoritiesConverter).isNotNull();

                LinkedHashMap<Class, List> configurers = (LinkedHashMap<Class, List>) ReflectionTestUtils.getField(resourceServerConfiguration.getSavedResourceServerHttpSecurity(), "configurers");
                AadResourceServerHttpSecurityConfigurer resourceServerConfigurer =
                    (AadResourceServerHttpSecurityConfigurer) configurers.entrySet()
                                                                         .stream()
                                                                         .filter(entry -> entry.getKey().equals(AadResourceServerHttpSecurityConfigurer.class))
                                                                         .map(Map.Entry::getValue)
                                                                         .flatMap(list -> list.stream())
                                                                         .findFirst()
                                                                         .orElse(null);
                assertThat(resourceServerConfigurer).isNotNull();

                Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthorityConverter =
                    (Converter<Jwt, Collection<GrantedAuthority>>) ReflectionTestUtils.getField(resourceServerConfigurer, "jwtGrantedAuthoritiesConverter");
                assertThat(jwtGrantedAuthorityConverter).isNotNull();
                assertThat(jwtGrantedAuthorityConverter).isEqualTo(authoritiesConverter);
            });
    }

    @Test
    void useDefaultWebSecurityConfigurerAdapter() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SecurityFilterChain.class);
                assertThat(context).hasBean("defaultAadResourceServerFilterChain");
            });
    }

    @Test
    void useCustomSecurityFilterChain() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withUserConfiguration(AzureGlobalPropertiesAutoConfiguration.class,
                TestAadResourceServerConfiguration.class,
                AadAutoConfiguration.class)
            .withInitializer(ConditionEvaluationReportLoggingListener.forLogLevel(LogLevel.INFO))
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SecurityFilterChain.class);
                assertThat(context).hasBean("testAadResourceServerFilterChain");
            });
    }

    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestAadResourceServerConfiguration {

        private HttpSecurity savedResourceServerHttpSecurity;

        @SuppressWarnings({"deprecation", "removal"})
        @Bean
        SecurityFilterChain testAadResourceServerFilterChain(HttpSecurity http,
                                                             TestJwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) throws Exception {
            http.apply(aadResourceServer()
                    .jwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter));
            savedResourceServerHttpSecurity = http;
            return http.build();
        }

        @Bean
        TestJwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
            return mock(TestJwtGrantedAuthoritiesConverter.class);
        }

        HttpSecurity getSavedResourceServerHttpSecurity() {
            return savedResourceServerHttpSecurity;
        }
    }

    class TestJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt source) {
            return null;
        }
    }
}
