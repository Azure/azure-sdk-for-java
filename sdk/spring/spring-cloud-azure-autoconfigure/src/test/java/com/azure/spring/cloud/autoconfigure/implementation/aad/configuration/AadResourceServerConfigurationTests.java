// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.implementation.aad.RecordingClientHttpRequestFactoryBuilderConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.aad.RecordingClientHttpRequestFactoryBuilderConfiguration.RecordingClientHttpRequestFactoryBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jose.RestOperationsResourceRetriever;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt.AadJwtIssuerValidator;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadResourceServerHttpSecurityConfigurer;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.restclient.autoconfigure.RestTemplateAutoConfiguration;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.ApplicationContext;
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

import java.time.Duration;
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
    void testJwtDecoderTimeoutDefaultValues() {
        resourceServerContextRunner()
            .withUserConfiguration(RecordingClientHttpRequestFactoryBuilderConfiguration.class)
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertThat(properties.getJwtConnectTimeout())
                    .isEqualTo(Duration.ofMillis(JWKSourceBuilder.DEFAULT_HTTP_CONNECT_TIMEOUT));
                assertThat(properties.getJwtReadTimeout())
                    .isEqualTo(Duration.ofMillis(JWKSourceBuilder.DEFAULT_HTTP_READ_TIMEOUT));
                // Verify the default timeouts are applied to the RestTemplate used by the JwtDecoder
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                verifyJwtDecoderRestTemplateTimeouts(context, jwtDecoder,
                    JWKSourceBuilder.DEFAULT_HTTP_CONNECT_TIMEOUT,
                    JWKSourceBuilder.DEFAULT_HTTP_READ_TIMEOUT);
            });
    }

    @Test
    void testJwtDecoderCacheDefaultValues() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertThat(properties.getJwkSetCacheLifespan()).isEqualTo(Duration.ofMinutes(5));
                assertThat(properties.getJwkSetCacheRefreshTime()).isEqualTo(Duration.ofMinutes(5));

                JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                verifyJwtDecoderCacheDurations(jwtDecoder,
                    Duration.ofMinutes(5).toMillis(),
                    Duration.ofMinutes(5).toMillis());
            });
    }

    @Test
    void testJwtDecoderTimeoutCustomValues() {
        resourceServerContextRunner()
            .withUserConfiguration(RecordingClientHttpRequestFactoryBuilderConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.jwt-connect-timeout=2000",
                "spring.cloud.azure.active-directory.jwt-read-timeout=3000")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertThat(properties.getJwtConnectTimeout()).isEqualTo(Duration.ofMillis(2000));
                assertThat(properties.getJwtReadTimeout()).isEqualTo(Duration.ofMillis(3000));
                // Verify JwtDecoder is still created successfully with custom timeouts
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
                // Verify the configured timeouts are applied to the RestTemplate used by the JwtDecoder
                verifyJwtDecoderRestTemplateTimeouts(context, jwtDecoder, 2000, 3000);
            });
    }

    @Test
    void testJwtDecoderCacheCustomValues() {
        resourceServerContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.jwk-set-cache-lifespan=12m",
                "spring.cloud.azure.active-directory.jwk-set-cache-refresh-time=34s")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertThat(properties.getJwkSetCacheLifespan()).isEqualTo(Duration.ofMinutes(12));
                assertThat(properties.getJwkSetCacheRefreshTime()).isEqualTo(Duration.ofSeconds(34));

                JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                verifyJwtDecoderCacheDurations(jwtDecoder,
                    Duration.ofMinutes(12).toMillis(),
                    Duration.ofSeconds(34).toMillis());
            });
    }

    @Test
    void testNotAudienceDefaultValidator() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id=fake-tenant-id")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                AadResourceServerConfiguration bean = context
                    .getBean(AadResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator(properties);
                assertThat(defaultValidator).isNotNull();
                // No AUD validator (no app-id-uri or client-id configured) + TID + ISS + Timestamp validators
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
                // AUD (from app-id-uri) + TID + ISS + Timestamp validators
                assertThat(defaultValidator).hasSize(4);
            });
    }

    @Test
    void testSingleTenantUsesTrustedIssuerRepository() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                AadResourceServerConfiguration bean = context.getBean(AadResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator(properties);

                AadJwtIssuerValidator issuerValidator = (AadJwtIssuerValidator) defaultValidator.stream()
                    .filter(AadJwtIssuerValidator.class::isInstance)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("AadJwtIssuerValidator not found"));

                assertThat(ReflectionTestUtils.getField(issuerValidator, "trustedIssuerRepo")).isNotNull();
            });
    }

    @Test
    void testValidateTenantIdRejectsCommon() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id=common",
                "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsOrganizations() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id=organizations",
                "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsConsumers() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id=consumers",
                "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsNull() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                // When tenant-id is null, AadAuthenticationProperties.afterPropertiesSet() sets it to "common"
                // Then our validation should reject "common"
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsEmptyString() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id=",
                "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                // When tenant-id is empty string, AadAuthenticationProperties.afterPropertiesSet() sets it to "common"
                // Then our validation should reject "common"
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsWhitespacePaddedReservedValue() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id= common ",
                "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdAcceptsValidGuid() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id=12345678-1234-1234-1234-123456789012",
                "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                assertThat(context).hasNotFailed();
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                AadResourceServerConfiguration bean = context.getBean(AadResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator(properties);

                AadJwtIssuerValidator issuerValidator = (AadJwtIssuerValidator) defaultValidator.stream()
                    .filter(AadJwtIssuerValidator.class::isInstance)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("AadJwtIssuerValidator not found"));

                assertThat(ReflectionTestUtils.getField(issuerValidator, "trustedIssuerRepo")).isNotNull();
            });
    }

    @Test
    void testValidateTenantIdNormalizesUppercaseGuid() {
        // Uppercase GUIDs are valid; the configured value should be normalized to lowercase
        // so that the tid/iss validators can match AAD tokens (which always use lowercase UUIDs).
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id=12345678-ABCD-ABCD-ABCD-123456789012",
                "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                assertThat(context).hasNotFailed();
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                AadResourceServerConfiguration bean = context.getBean(AadResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator(properties);
                // AUD (from app-id-uri) + TID + ISS + Timestamp validators
                assertThat(defaultValidator).hasSize(4);
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
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class, ClientRegistration.class))
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
                "spring.cloud.azure.active-directory.profile.tenant-id=fake-tenant-id",
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
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class, ClientRegistration.class))
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.profile.tenant-id=fake-tenant-id",
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

        @Bean
        SecurityFilterChain testAadResourceServerFilterChain(HttpSecurity http,
                                                             TestJwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) throws Exception {

            http.with(aadResourceServer(), customizer ->
                customizer.jwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter)
            );
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

    /**
     * Verifies that the NimbusJwtDecoder uses the expected JWK retrieval path and that
     * the recorded HTTP client settings contain the expected connect and read timeouts.
     */
    @SuppressWarnings("deprecation")
    private static void verifyJwtDecoderRestTemplateTimeouts(ApplicationContext context,
                                                             JwtDecoder jwtDecoder,
                                                             int expectedConnectTimeoutMs,
                                                             int expectedReadTimeoutMs) {
        RemoteJWKSet<?> remoteJwkSet = getRemoteJwkSet(jwtDecoder);
        Object resourceRetriever = getFieldIfExists(remoteJwkSet, "resourceRetriever");
        if (resourceRetriever == null) {
            resourceRetriever = getFieldIfExists(remoteJwkSet, "jwkSetRetriever");
        }
        assertThat(resourceRetriever).isInstanceOf(RestOperationsResourceRetriever.class);

        Object restOperations = ReflectionTestUtils.getField(resourceRetriever, "restOperations");
        assertThat(restOperations).isInstanceOf(org.springframework.web.client.RestTemplate.class);

        HttpClientSettings clientSettings = context.getBean(RecordingClientHttpRequestFactoryBuilder.class)
                                                  .getClientSettings();
        assertThat(clientSettings).isNotNull();
        assertThat(clientSettings.connectTimeout()).isEqualTo(Duration.ofMillis(expectedConnectTimeoutMs));
        assertThat(clientSettings.readTimeout()).isEqualTo(Duration.ofMillis(expectedReadTimeoutMs));
    }

    @SuppressWarnings("deprecation")
    private static void verifyJwtDecoderCacheDurations(JwtDecoder jwtDecoder,
                                                       long expectedCacheLifespanMs,
                                                       long expectedCacheRefreshTimeMs) {
        JWKSetCache jwkSetCache = getRemoteJwkSet(jwtDecoder).getJWKSetCache();
        assertThat(jwkSetCache).isInstanceOf(DefaultJWKSetCache.class);

        DefaultJWKSetCache defaultJwkSetCache = (DefaultJWKSetCache) jwkSetCache;
        assertThat(defaultJwkSetCache.getLifespan(java.util.concurrent.TimeUnit.MILLISECONDS))
            .isEqualTo(expectedCacheLifespanMs);
        assertThat(defaultJwkSetCache.getRefreshTime(java.util.concurrent.TimeUnit.MILLISECONDS))
            .isEqualTo(expectedCacheRefreshTimeMs);
    }

    @SuppressWarnings("deprecation")
    private static RemoteJWKSet<?> getRemoteJwkSet(JwtDecoder jwtDecoder) {
        Object jwkSource = getJwkSource(jwtDecoder);
        assertThat(jwkSource).isInstanceOf(RemoteJWKSet.class);
        return (RemoteJWKSet<?>) jwkSource;
    }

    private static Object getJwkSource(JwtDecoder jwtDecoder) {
        Object jwtProcessor = ReflectionTestUtils.getField(jwtDecoder, "jwtProcessor");
        assertThat(jwtProcessor).isInstanceOf(com.nimbusds.jwt.proc.DefaultJWTProcessor.class);

        com.nimbusds.jose.proc.JWSKeySelector<?> keySelector =
            ((com.nimbusds.jwt.proc.DefaultJWTProcessor<?>) jwtProcessor).getJWSKeySelector();
        assertThat(keySelector).isInstanceOf(com.nimbusds.jose.proc.JWSVerificationKeySelector.class);

        com.nimbusds.jose.jwk.source.JWKSource<?> jwkSource =
            ((com.nimbusds.jose.proc.JWSVerificationKeySelector<?>) keySelector).getJWKSource();
        return jwkSource;
    }

    private static Object getFieldIfExists(Object target, String name) {
        try {
            return ReflectionTestUtils.getField(target, name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
