// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.AadB2cConstants;
import com.azure.spring.cloud.autoconfigure.implementation.aad.RecordingClientHttpRequestFactoryBuilderConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.aad.RecordingClientHttpRequestFactoryBuilderConfiguration.RecordingClientHttpRequestFactoryBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt.AadIssuerJwsKeySelector;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt.AadTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.conditions.AadB2cConditions;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.jwt.AadB2cTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class AadB2cResourceServerAutoConfigurationTests extends AbstractAadB2cOAuth2ClientTestConfigurations {

    private WebApplicationContextRunner getResourceServerContextRunner() {
        return getResourceServerContextRunner(getB2CResourceServerProperties());
    }

    private WebApplicationContextRunner getResourceServerContextRunner(String... propertyValues) {
        return new WebApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(OAuth2LoginAuthenticationFilter.class))
            .withConfiguration(AutoConfigurations.of(
                AzureGlobalPropertiesAutoConfiguration.class,
                WebResourceServerTestApp.class,
                AadB2cResourceServerAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withPropertyValues(propertyValues);
    }

    @Override
    WebApplicationContextRunner getDefaultContextRunner() {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                WebOAuth2ClientTestApp.class,
                AzureGlobalPropertiesAutoConfiguration.class,
                AadB2cResourceServerAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withPropertyValues(getB2CResourceServerProperties());
    }

    private String[] getB2CResourceServerProperties() {
        return new String[] {
            "spring.cloud.azure.active-directory.b2c.enabled=true",
            String.format("%s=%s", AadB2cConstants.BASE_URI, AadB2cConstants.TEST_BASE_URI),
            String.format("%s=%s", AadB2cConstants.TENANT_ID, AadB2cConstants.TEST_TENANT_ID),
            String.format("%s=%s", AadB2cConstants.CLIENT_ID, AadB2cConstants.TEST_CLIENT_ID),
            String.format("%s=%s", AadB2cConstants.APP_ID_URI, AadB2cConstants.TEST_APP_ID_URI),
            String.format("%s.%s=%s", AadB2cConstants.USER_FLOWS, AadB2cConstants.TEST_KEY_SIGN_UP_OR_IN,
                AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME),
        };
    }

    private ContextConsumer<ApplicationContext> b2CAutoConfigurationBean() {
        return (c) -> {
            final AadB2cResourceServerAutoConfiguration autoResourceConfig =
                c.getBean(AadB2cResourceServerAutoConfiguration.class);
            Assertions.assertNotNull(autoResourceConfig);
        };
    }

    private ContextConsumer<ApplicationContext> b2CResourceServerPropertiesBean() {
        return (c) -> {
            final AadB2cProperties properties = c.getBean(AadB2cProperties.class);

            Assertions.assertNotNull(properties);
            Assertions.assertEquals(properties.getProfile().getTenantId(), AadB2cConstants.TEST_TENANT_ID);
            Assertions.assertEquals(properties.getCredential().getClientId(), AadB2cConstants.TEST_CLIENT_ID);
            Assertions.assertEquals(properties.getAppIdUri(), AadB2cConstants.TEST_APP_ID_URI);
        };
    }

    private ContextConsumer<ApplicationContext> b2CResourceServerBean() {
        return (c) -> {
            final JwtDecoder jwtDecoder = c.getBean(JwtDecoder.class);
            final AadIssuerJwsKeySelector jwsKeySelector = c.getBean(AadIssuerJwsKeySelector.class);
            final AadTrustedIssuerRepository issuerRepository = c.getBean(AadTrustedIssuerRepository.class);
            Assertions.assertNotNull(jwtDecoder);
            Assertions.assertNotNull(jwsKeySelector);
            Assertions.assertNotNull(issuerRepository);
        };
    }

    @Test
    void testB2COAuth2ClientAutoConfigurationBean() {
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CAutoConfigurationBean());
    }

    @Test
    void testB2COnlyAutoConfigurationBean() {
        getResourceServerContextRunner().run(b2CAutoConfigurationBean());
    }

    @Test
    void testB2COAuth2ClientResourceServerPropertiesBean() {
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CResourceServerPropertiesBean());
    }

    @Test
    void testB2COnlyResourceServerPropertiesBean() {
        getResourceServerContextRunner().run(b2CResourceServerPropertiesBean());
    }

    @Test
    void testB2COAuth2ClientResourceServerBean() {
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CResourceServerBean());
    }

    @Test
    void testB2COnlyResourceServerBean() {
        getResourceServerContextRunner().run(b2CResourceServerBean());
    }

    @Test
    void testB2CTimeoutDefaultValues() {
        getResourceServerContextRunner()
            .withUserConfiguration(RecordingClientHttpRequestFactoryBuilderConfiguration.class)
            .run(context -> {
                AadB2cProperties properties = context.getBean(AadB2cProperties.class);
                assertThat(properties.getJwtConnectTimeout())
                    .isEqualTo(Duration.ofMillis(JWKSourceBuilder.DEFAULT_HTTP_CONNECT_TIMEOUT));
                assertThat(properties.getJwtReadTimeout())
                    .isEqualTo(Duration.ofMillis(JWKSourceBuilder.DEFAULT_HTTP_READ_TIMEOUT));
                // Verify the default timeouts are applied to the RestTemplate used by the ResourceRetriever
                verifyResourceRetrieverRestTemplateTimeouts(context,
                    JWKSourceBuilder.DEFAULT_HTTP_CONNECT_TIMEOUT,
                    JWKSourceBuilder.DEFAULT_HTTP_READ_TIMEOUT);
            });
    }

    @Test
    void testB2CTimeoutCustomValues() {
        getResourceServerContextRunner()
            .withUserConfiguration(RecordingClientHttpRequestFactoryBuilderConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.active-directory.b2c.jwt-connect-timeout=2000",
                "spring.cloud.azure.active-directory.b2c.jwt-read-timeout=3000")
            .run(context -> {
                AadB2cProperties properties = context.getBean(AadB2cProperties.class);
                assertThat(properties.getJwtConnectTimeout()).isEqualTo(Duration.ofMillis(2000));
                assertThat(properties.getJwtReadTimeout()).isEqualTo(Duration.ofMillis(3000));
                // Verify the custom timeouts are applied to the RestTemplate used by the ResourceRetriever
                verifyResourceRetrieverRestTemplateTimeouts(context, 2000, 3000);
            });
    }

    @Test
    void testResourceServerConditionsIsInvokedWhenAADB2CEnableFileExists() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AadB2cConditions.ClientRegistrationCondition clientRegistrationCondition =
                spy(AadB2cConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AadB2cConditions.ClientRegistrationCondition.class))
                     .thenReturn(clientRegistrationCondition);
            getDefaultContextRunner()
                .withPropertyValues(getAuthorizationClientPropertyValues())
                .run(c -> {
                    verify(clientRegistrationCondition, atLeastOnce()).getMatchOutcome(any(), any());
                });
        }
    }

    @Test
    void testResourceServerConditionsIsNotInvokedWhenAADB2CEnableFileDoesNotExists() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AadB2cConditions.ClientRegistrationCondition clientRegistrationCondition =
                mock(AadB2cConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AadB2cConditions.ClientRegistrationCondition.class))
                     .thenReturn(clientRegistrationCondition);
            new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(WebOAuth2ClientTestApp.class,
                    AadB2cResourceServerAutoConfiguration.class))
                .withPropertyValues(getB2CResourceServerProperties())
                .withPropertyValues(getAuthorizationClientPropertyValues())
                .run(c -> {
                    verify(clientRegistrationCondition, never()).getMatchOutcome(any(), any());
                });
        }
    }

    @Test
    void testExistAADB2CTrustedIssuerRepositoryBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                final AadB2cTrustedIssuerRepository aadb2CTrustedIssuerRepository =
                    context.getBean(AadB2cTrustedIssuerRepository.class);
                assertThat(aadb2CTrustedIssuerRepository).isNotNull();
                assertThat(aadb2CTrustedIssuerRepository).isExactlyInstanceOf(AadB2cTrustedIssuerRepository.class);

                Set<String> trustedIssuers = aadb2CTrustedIssuerRepository.getTrustedIssuers();
                assertThat(trustedIssuers)
                    .noneMatch(issuer -> issuer.startsWith("https://login.microsoftonline.com/"))
                    .noneMatch(issuer -> issuer.startsWith("https://sts.windows.net/"))
                    .noneMatch(issuer -> issuer.startsWith("https://sts.chinacloudapi.cn/"));
            });
    }

    @Test
    void testValidateTenantIdRejectsCommon() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withPropertyValues(String.format("%s=common", AadB2cConstants.TENANT_ID))
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsEmptyString() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withPropertyValues(String.format("%s=", AadB2cConstants.TENANT_ID))
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsOrganizations() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withPropertyValues(String.format("%s=organizations", AadB2cConstants.TENANT_ID))
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsConsumers() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withPropertyValues(String.format("%s=consumers", AadB2cConstants.TENANT_ID))
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    void testValidateTenantIdRejectsReservedValuesWithWhitespaceAndCase() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withPropertyValues(String.format("%s=  COMMON  ", AadB2cConstants.TENANT_ID))
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null, empty, or set to");
            });
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void testExistjwtProcessorBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                JWTProcessor<SecurityContext> jwtProcessor = context.getBean(JWTProcessor.class);
                assertThat(jwtProcessor).isNotNull();
                assertThat(jwtProcessor).isExactlyInstanceOf(DefaultJWTProcessor.class);
            });
    }

    @Test
    void testExistJwtDecoderBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
            });
    }

    @Test
    void testUserProvidedJwtDecoderIsNotReplaced() {
        JwtDecoder customDecoder = mock(JwtDecoder.class);
        getResourceServerContextRunner()
            .withBean(JwtDecoder.class, () -> customDecoder)
            .run(context -> {
                assertThat(context).hasSingleBean(JwtDecoder.class);
                assertThat(context.getBean(JwtDecoder.class)).isSameAs(customDecoder);
            });
    }

    @ParameterizedTest
    @MethodSource("userFlowClaims")
    void testJwtDecoderValidatesConfiguredUserFlows(Map<String, Object> userFlowClaims, boolean valid) throws Exception {
        JWTClaimsSet.Builder builder = validJwtClaims();
        userFlowClaims.forEach(builder::claim);
        JWTClaimsSet claims = builder.build();
        getContextRunnerWithMockJwtProcessor(claims, getB2CResourceServerProperties())
            .run(context -> {
                JwtDecoder decoder = context.getBean(JwtDecoder.class);
                if (valid) {
                    assertThat(decoder.decode(token(claims))).isNotNull();
                } else {
                    assertThatExceptionOfType(JwtValidationException.class)
                        .isThrownBy(() -> decoder.decode(token(claims)))
                        .withMessageContaining("The tfp or acr claim does not match a configured user flow.");
                }
            });
    }

    private static Stream<Arguments> userFlowClaims() {
        String allowedFlow = AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME;
        return Stream.of(
            Arguments.of(Map.of("tfp", allowedFlow), true),
            Arguments.of(Map.of("tfp", "FAKE-SIGN-IN-OR-UP"), true),
            Arguments.of(Map.of("acr", allowedFlow), true),
            Arguments.of(Map.of("acr", "FAKE-SIGN-IN-OR-UP"), true),
            Arguments.of(Map.of("tfp", allowedFlow, "acr", "unconfigured-flow"), true),
            Arguments.of(Map.of(), false),
            Arguments.of(Map.of("tfp", "unconfigured-flow"), false),
            Arguments.of(Map.of("acr", "unconfigured-flow"), false),
            Arguments.of(Map.of("tfp", AadB2cConstants.TEST_KEY_SIGN_UP_OR_IN), false),
            Arguments.of(Map.of("tfp", "unconfigured-flow", "acr", allowedFlow), false),
            Arguments.of(Map.of("tfp", "", "acr", allowedFlow), false),
            Arguments.of(Map.of("tfp", " ", "acr", allowedFlow), false),
            Arguments.of(Map.of("tfp", 123, "acr", allowedFlow), false),
            Arguments.of(Map.of("tfp", Arrays.asList(allowedFlow)), false),
            Arguments.of(Map.of("acr", 123), false),
            Arguments.of(Map.of("acr", Arrays.asList(allowedFlow)), false));
    }

    @Test
    void testJwtDecoderAcceptsAnyConfiguredUserFlowValue() throws Exception {
        JWTClaimsSet claims = validJwtClaims().claim("tfp", "PROFILE_EDIT").build();
        getContextRunnerWithMockJwtProcessor(claims, getB2CResourceServerProperties())
            .withPropertyValues(AadB2cConstants.USER_FLOWS + "." + AadB2cConstants.TEST_KEY_PROFILE_EDIT
                + "=" + AadB2cConstants.TEST_PROFILE_EDIT_NAME)
            .run(context -> assertThat(context.getBean(JwtDecoder.class).decode(token(claims))).isNotNull());
    }

    @ParameterizedTest
    @ValueSource(strings = { "tfp", "acr" })
    void testJwtDecoderRejectsBlankClaimWhenOptionalUserFlowIsEmpty(String claimName) throws Exception {
        JWTClaimsSet claims = validJwtClaims().claim(claimName, "").build();
        getContextRunnerWithMockJwtProcessor(claims, getB2CResourceServerProperties())
            .withPropertyValues(AadB2cConstants.USER_FLOWS + "." + AadB2cConstants.TEST_KEY_PROFILE_EDIT + "=")
            .run(context -> {
                assertThat(context.getBean(AadB2cProperties.class).getUserFlows())
                    .containsEntry(AadB2cConstants.TEST_KEY_PROFILE_EDIT, "");
                assertThatExceptionOfType(JwtValidationException.class)
                    .isThrownBy(() -> context.getBean(JwtDecoder.class).decode(token(claims)))
                    .withMessageContaining("The tfp or acr claim does not match a configured user flow.");
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "fake-sign-in-or-up", "unconfigured-flow" })
    void testJwtDecoderValidatesUserFlowWithPolicySpecificIssuer(String userFlow) throws Exception {
        JWTClaimsSet claims = validJwtClaims()
            .issuer("https://faketenant.b2clogin.com/tfp/fake-tenant-id/fake-sign-in-or-up/v2.0/")
            .claim("tfp", userFlow)
            .build();
        getContextRunnerWithMockJwtProcessor(claims, getB2CResourceServerProperties())
            .run(context -> {
                JwtDecoder decoder = context.getBean(JwtDecoder.class);
                if (AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME.equals(userFlow)) {
                    assertThat(decoder.decode(token(claims))).isNotNull();
                } else {
                    assertThatExceptionOfType(JwtValidationException.class)
                        .isThrownBy(() -> decoder.decode(token(claims)))
                        .withMessageContaining("The tfp or acr claim does not match a configured user flow.");
                }
            });
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testJwtDecoderDoesNotRestrictUserFlowsWhenUnconfigured(boolean includeUserFlowClaim) throws Exception {
        JWTClaimsSet.Builder builder = validJwtClaims();
        if (includeUserFlowClaim) {
            builder.claim("tfp", "unconfigured-flow");
        }
        JWTClaimsSet claims = builder.build();
        String[] propertyValues = Arrays.stream(getB2CResourceServerProperties())
            .filter(property -> !property.startsWith(AadB2cConstants.USER_FLOWS + "."))
            .toArray(String[]::new);
        getContextRunnerWithMockJwtProcessor(claims, propertyValues)
            .run(context -> assertThat(context.getBean(JwtDecoder.class).decode(token(claims))).isNotNull());
    }

    @Test
    void testJwtDecoderRejectsMissingUserFlowClaimWithPolicySpecificIssuer() throws Exception {
        JWTClaimsSet claims = validJwtClaims()
            .issuer("https://faketenant.b2clogin.com/tfp/fake-tenant-id/fake-sign-in-or-up/v2.0/")
            .build();
        getContextRunnerWithMockJwtProcessor(claims, getB2CResourceServerProperties())
            .run(context -> assertThatExceptionOfType(JwtValidationException.class)
                .isThrownBy(() -> context.getBean(JwtDecoder.class).decode(token(claims)))
                .withMessageContaining("The tfp or acr claim does not match a configured user flow."));
    }

    @ParameterizedTest
    @MethodSource("invalidJwtClaims")
    void testJwtDecoderRetainsExistingValidators(String claimName, Object claimValue) throws Exception {
        JWTClaimsSet claims = validJwtClaims()
            .claim("tfp", AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME)
            .claim(claimName, claimValue)
            .build();
        getContextRunnerWithMockJwtProcessor(claims, getB2CResourceServerProperties())
            .run(context -> assertThatExceptionOfType(JwtValidationException.class)
                .isThrownBy(() -> context.getBean(JwtDecoder.class).decode(token(claims))));
    }

    private static Stream<Arguments> invalidJwtClaims() {
        return Stream.of(
            Arguments.of("aud", Arrays.asList("unconfigured-audience")),
            Arguments.of("aud", Arrays.asList()),
            Arguments.of("tid", "unconfigured-tenant"),
            Arguments.of("iss", "https://untrusted.example.com/v2.0/"),
            Arguments.of("exp", Date.from(Instant.now().minusSeconds(300))),
            Arguments.of("nbf", Date.from(Instant.now().plusSeconds(300))));
    }

    private static JWTClaimsSet.Builder validJwtClaims() {
        return new JWTClaimsSet.Builder()
            .subject("fake-subject")
            .issuer("https://faketenant.b2clogin.com/fake-tenant-id/v2.0/")
            .audience(AadB2cConstants.TEST_CLIENT_ID)
            .claim("tid", AadB2cConstants.TEST_TENANT_ID)
            .issueTime(Date.from(Instant.now().minusSeconds(600)))
            .expirationTime(Date.from(Instant.now().plusSeconds(300)));
    }

    private WebApplicationContextRunner getContextRunnerWithMockJwtProcessor(JWTClaimsSet claims,
                                                                            String... propertyValues) throws Exception {
        DefaultJWTProcessor<SecurityContext> processor = spy(new DefaultJWTProcessor<>());
        doReturn(claims).when(processor).process(any(JWT.class), any());
        return getResourceServerContextRunner(propertyValues)
            .withBean("jwtProcessor", DefaultJWTProcessor.class, () -> processor);
    }

    private static String token(JWTClaimsSet claims) {
        return new JWSHeader(JWSAlgorithm.RS256).toBase64URL() + "."
            + Base64URL.encode(claims.toString()) + "." + Base64URL.encode("fake-signature");
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void testExistJWTClaimsSetAwareJWSKeySelectorBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                final JWTClaimsSetAwareJWSKeySelector jwsKeySelector =
                    context.getBean(JWTClaimsSetAwareJWSKeySelector.class);
                assertThat(jwsKeySelector).isNotNull();
                assertThat(jwsKeySelector).isExactlyInstanceOf(AadIssuerJwsKeySelector.class);
            });
    }

    /**
     * Verifies that the RestTemplate used by the ResourceRetriever for JWK retrieval
     * has the expected connect and read timeouts applied to its ClientHttpRequestFactory.
     */
    private static void verifyResourceRetrieverRestTemplateTimeouts(ApplicationContext context,
                                                                     int expectedConnectTimeoutMs,
                                                                     int expectedReadTimeoutMs) {
        com.nimbusds.jose.util.ResourceRetriever resourceRetriever =
            context.getBean(com.nimbusds.jose.util.ResourceRetriever.class);
        assertThat(resourceRetriever).isNotNull();

        // RestOperationsResourceRetriever -> restOperations (RestTemplate)
        Object restOperations = org.springframework.test.util.ReflectionTestUtils
            .getField(resourceRetriever, "restOperations");
        assertThat(restOperations).isInstanceOf(org.springframework.web.client.RestTemplate.class);

        // RestTemplate -> ClientHttpRequestFactory
        org.springframework.http.client.ClientHttpRequestFactory requestFactory =
            ((org.springframework.web.client.RestTemplate) restOperations).getRequestFactory();
        assertThat(requestFactory).isNotNull();

        assertRecordedHttpClientSettings(context, expectedConnectTimeoutMs, expectedReadTimeoutMs);
    }

    private static void assertRecordedHttpClientSettings(ApplicationContext context,
                                                         int expectedConnectTimeoutMs,
                                                         int expectedReadTimeoutMs) {
        HttpClientSettings clientSettings = context.getBean(RecordingClientHttpRequestFactoryBuilder.class)
                                                  .getClientSettings();
        assertThat(clientSettings).isNotNull();
        assertThat(clientSettings.connectTimeout()).isEqualTo(Duration.ofMillis(expectedConnectTimeoutMs));
        assertThat(clientSettings.readTimeout()).isEqualTo(Duration.ofMillis(expectedReadTimeoutMs));
    }
}
