// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.jwt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith(OutputCaptureExtension.class)
class AadB2cJwtUserFlowValidatorTests {

    private final AadB2cJwtUserFlowValidator validator =
        new AadB2cJwtUserFlowValidator(Arrays.asList("allowed-flow", "second-flow"));

    @ParameterizedTest
    @CsvSource({ "tfp,allowed-flow", "tfp,ALLOWED-FLOW", "acr,allowed-flow", "acr,ALLOWED-FLOW",
        "tfp,second-flow" })
    void testAcceptsConfiguredUserFlow(String claimName, String userFlow, CapturedOutput output) {
        assertThat(validator.validate(jwt(Map.of(claimName, userFlow))).hasErrors()).isFalse();
        assertThat(output.getAll()).contains(
            "Azure AD B2C user flow validation succeeded using the '" + claimName + "' claim.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "tfp", "acr" })
    void testRejectsUnconfiguredUserFlow(String claimName, CapturedOutput output) {
        assertInvalidUserFlow(validator.validate(jwt(Map.of(claimName, "unconfigured-flow"))));
        assertThat(output.getAll()).contains("Azure AD B2C user flow validation failed: the '"
            + claimName + "' claim does not match a configured user flow.");
    }

    @ParameterizedTest
    @MethodSource("blankUserFlowClaims")
    void testRejectsBlankClaimEvenIfConfigured(String claimName, String blank, CapturedOutput output) {
        AadB2cJwtUserFlowValidator blankFlowValidator =
            new AadB2cJwtUserFlowValidator(Arrays.asList("allowed-flow", blank));
        Map<String, Object> claims = "tfp".equals(claimName)
            ? Map.of("tfp", blank, "acr", "allowed-flow") : Map.of("acr", blank);

        assertInvalidUserFlow(blankFlowValidator.validate(jwt(claims)));
        assertThat(output.getAll()).contains(
            "Azure AD B2C user flow validation failed: the '" + claimName + "' claim is blank.")
            .doesNotContain("sensitive-token-value", "allowed-flow");
        assertThat(blankFlowValidator.validate(jwt(Map.of(claimName, "allowed-flow"))).hasErrors()).isFalse();
    }

    private static Stream<Arguments> blankUserFlowClaims() {
        return Stream.of("", " ", "\t", "\r\n", "\u2003")
            .flatMap(blank -> Stream.of(Arguments.of("tfp", blank), Arguments.of("acr", blank)));
    }

    @Test
    void testRejectsMissingUserFlowClaims(CapturedOutput output) {
        assertInvalidUserFlow(validator.validate(jwt(Collections.emptyMap())));
        assertThat(output.getAll()).contains(
            "Azure AD B2C user flow validation failed: the 'acr' claim is missing or is not a string.");
    }

    @ParameterizedTest
    @MethodSource("invalidTfpClaims")
    void testDoesNotFallBackFromInvalidTfp(Object tfp, CapturedOutput output) {
        Jwt jwt = Jwt.withTokenValue("sensitive-token-value")
            .header("alg", "RS256")
            .claim("acr", "allowed-flow")
            .claims(claims -> claims.put("tfp", tfp))
            .build();
        assertInvalidUserFlow(validator.validate(jwt));
        assertThat(output.getAll()).contains("Azure AD B2C user flow validation failed: the 'tfp' claim");
    }

    private static Stream<Arguments> invalidTfpClaims() {
        return Stream.of(
            Arguments.of((Object) null),
            Arguments.of(""),
            Arguments.of(" "),
            Arguments.of("unconfigured-flow"),
            Arguments.of(123),
            Arguments.of(Arrays.asList("allowed-flow")),
            Arguments.of(Map.of("policy", "allowed-flow")));
    }

    @Test
    void testPrefersValidTfpOverUnconfiguredAcr() {
        assertThat(validator.validate(jwt(Map.of("tfp", "allowed-flow", "acr", "unconfigured-flow"))).hasErrors())
            .isFalse();
    }

    @Test
    void testRetainsConfiguredNamesSnapshot() {
        List<String> userFlows = new ArrayList<>(Collections.singletonList("allowed-flow"));
        AadB2cJwtUserFlowValidator snapshotValidator = new AadB2cJwtUserFlowValidator(userFlows);
        userFlows.clear();
        assertThat(snapshotValidator.validate(jwt(Map.of("tfp", "allowed-flow"))).hasErrors()).isFalse();
    }

    @Test
    void testRejectsNullConfiguration() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AadB2cJwtUserFlowValidator(null));
    }

    @Test
    void testRejectsEmptyConfiguration() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new AadB2cJwtUserFlowValidator(Collections.emptyList()));
    }

    @Test
    void testRejectsNullToken() {
        assertThatIllegalArgumentException().isThrownBy(() -> validator.validate(null));
    }

    @Test
    void testLogsValidationDecisionsWithoutSensitiveValues(CapturedOutput output) {
        assertThat(validator.validate(jwt(Map.of("tfp", "allowed-flow"))).hasErrors()).isFalse();
        assertInvalidUserFlow(validator.validate(jwt(Map.of("tfp", "sensitive-unconfigured-flow"))));
        assertInvalidUserFlow(validator.validate(jwt(Collections.emptyMap())));

        assertThat(output.getAll())
            .contains("Azure AD B2C user flow validation succeeded using the 'tfp' claim.",
                "Azure AD B2C user flow validation failed: the 'tfp' claim does not match a configured user flow.",
                "Azure AD B2C user flow validation failed: the 'acr' claim is missing or is not a string.")
            .doesNotContain("sensitive-token-value", "allowed-flow", "second-flow", "sensitive-unconfigured-flow",
                "sensitive-client-secret", "sensitive-response-body", "sensitive-pkcs12", "-----BEGIN PRIVATE KEY-----");
    }

    private static Jwt jwt(Map<String, Object> userFlowClaims) {
        return Jwt.withTokenValue("sensitive-token-value")
            .header("alg", "RS256")
            .claim("sub", "fake-subject")
            .claim("client_secret", "sensitive-client-secret")
            .claim("payload", "sensitive-response-body")
            .claim("pkcs12", "sensitive-pkcs12")
            .claim("private_key", "-----BEGIN PRIVATE KEY-----")
            .claims(claims -> claims.putAll(userFlowClaims))
            .build();
    }

    private static void assertInvalidUserFlow(OAuth2TokenValidatorResult result) {
        assertThat(result.getErrors()).singleElement().satisfies(error -> {
            assertThat(error.getErrorCode()).isEqualTo(OAuth2ErrorCodes.INVALID_TOKEN);
            assertThat(error.getDescription())
                .isEqualTo("The tfp or acr claim does not match a configured user flow.");
        });
    }
}
