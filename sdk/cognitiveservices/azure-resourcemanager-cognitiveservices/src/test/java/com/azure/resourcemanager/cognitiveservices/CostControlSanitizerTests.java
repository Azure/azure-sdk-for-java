// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cognitiveservices;

import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public final class CostControlSanitizerTests {
    @ParameterizedTest
    @CsvSource({
        "rg(test),rg\\(test\\)",
        "rg.prod,rg\\.prod",
        "rg(,rg\\(",
        "rg),rg\\)",
        "rg(test).prod,rg\\(test\\)\\.prod",
        "rg-test,rg-test" })
    public void sanitizesLiteralIdentifiersInUrlsAndBodies(String value, String expectedRegex) {
        String replacement = "sanitized-resource-group";
        TestProxySanitizer[] sanitizers
            = CostControlLifecycleTests.createValueSanitizers("resourceGroups", value, replacement);
        Assertions.assertEquals(2, sanitizers.length);
        Assertions.assertEquals(TestProxySanitizerType.URL, sanitizers[0].getType());
        Assertions.assertEquals(TestProxySanitizerType.BODY_REGEX, sanitizers[1].getType());

        String resourceId = "/subscriptions/test-subscription/resourceGroups/" + value
            + "/providers/Microsoft.CognitiveServices/accounts/test-account";
        String sanitizedResourceId = resourceId.replace(value, replacement);
        for (TestProxySanitizer sanitizer : sanitizers) {
            Assertions.assertEquals("(?<=/resourceGroups/)" + expectedRegex + "(?=/|[?\"#]|$)", sanitizer.getRegex());
            Assertions.assertEquals(replacement, sanitizer.getRedactedValue());
            Pattern pattern = Pattern.compile(sanitizer.getRegex());
            Assertions.assertEquals("https://management.azure.com" + sanitizedResourceId,
                pattern.matcher("https://management.azure.com" + resourceId).replaceAll(sanitizer.getRedactedValue()));
            Assertions.assertEquals("{\"id\":\"" + sanitizedResourceId + "\"}",
                pattern.matcher("{\"id\":\"" + resourceId + "\"}").replaceAll(sanitizer.getRedactedValue()));
        }
    }

    @ParameterizedTest
    @CsvSource({ "rg.prod,rg-prod", "rg(test),rgtest", "rg,rg-prod" })
    public void doesNotSanitizeDifferentIdentifiers(String value, String otherValue) {
        for (TestProxySanitizer sanitizer : CostControlLifecycleTests.createValueSanitizers("resourceGroups", value,
            "sanitized")) {
            String resourceId = "/resourceGroups/" + otherValue + "/providers/Microsoft.CognitiveServices";
            Assertions.assertEquals(resourceId,
                Pattern.compile(sanitizer.getRegex()).matcher(resourceId).replaceAll(sanitizer.getRedactedValue()));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "shared-account,shared-account",
        "shared,shared-account",
        "shared-account,shared",
        "rg(test).prod,rg(test).prod" })
    public void sanitizesEqualAndOverlappingNamesIndependently(String resourceGroup, String account) {
        String resourceId = "/subscriptions/test-subscription/resourceGroups/" + resourceGroup
            + "/providers/Microsoft.CognitiveServices/accounts/" + account;
        String expected = "/subscriptions/test-subscription/resourceGroups/sanitized-resource-group"
            + "/providers/Microsoft.CognitiveServices/accounts/sanitized-cognitive-account";
        for (int typeIndex = 0; typeIndex < 2; typeIndex++) {
            TestProxySanitizer groupSanitizer = CostControlLifecycleTests.createValueSanitizers("resourceGroups",
                resourceGroup, "sanitized-resource-group")[typeIndex];
            TestProxySanitizer accountSanitizer = CostControlLifecycleTests.createValueSanitizers("accounts", account,
                "sanitized-cognitive-account")[typeIndex];
            for (String suffix : new String[] { "", "/costControls/test-control", "?api-version=test", "#fragment" }) {
                String input = typeIndex == 0
                    ? "https://management.azure.com" + resourceId + suffix
                    : "{\"id\":\"" + resourceId + suffix + "\",\"displayName\":\"" + resourceGroup + "\"}";
                String expectedOutput = typeIndex == 0
                    ? "https://management.azure.com" + expected + suffix
                    : "{\"id\":\"" + expected + suffix + "\",\"displayName\":\"" + resourceGroup + "\"}";
                String sanitized = Pattern.compile(groupSanitizer.getRegex())
                    .matcher(input)
                    .replaceAll(groupSanitizer.getRedactedValue());
                sanitized = Pattern.compile(accountSanitizer.getRegex())
                    .matcher(sanitized)
                    .replaceAll(accountSanitizer.getRedactedValue());
                Assertions.assertEquals(expectedOutput, sanitized);
            }
        }
    }
}
