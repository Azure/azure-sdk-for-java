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
        TestProxySanitizer[] sanitizers = CostControlLifecycleTests.createValueSanitizers(value, replacement);
        Assertions.assertEquals(2, sanitizers.length);
        Assertions.assertEquals(TestProxySanitizerType.URL, sanitizers[0].getType());
        Assertions.assertEquals(TestProxySanitizerType.BODY_REGEX, sanitizers[1].getType());

        String resourceId = "/subscriptions/test-subscription/resourceGroups/" + value
            + "/providers/Microsoft.CognitiveServices/accounts/test-account";
        String sanitizedResourceId = resourceId.replace(value, replacement);
        for (TestProxySanitizer sanitizer : sanitizers) {
            Assertions.assertEquals(expectedRegex, sanitizer.getRegex());
            Assertions.assertEquals(replacement, sanitizer.getRedactedValue());
            Pattern pattern = Pattern.compile(sanitizer.getRegex());
            Assertions.assertEquals("https://management.azure.com" + sanitizedResourceId,
                pattern.matcher("https://management.azure.com" + resourceId).replaceAll(sanitizer.getRedactedValue()));
            Assertions.assertEquals("{\"id\":\"" + sanitizedResourceId + "\"}",
                pattern.matcher("{\"id\":\"" + resourceId + "\"}").replaceAll(sanitizer.getRedactedValue()));
        }
    }

    @ParameterizedTest
    @CsvSource({ "rg.prod,rg-prod", "rg(test),rgtest" })
    public void doesNotSanitizeDifferentIdentifiers(String value, String otherValue) {
        for (TestProxySanitizer sanitizer : CostControlLifecycleTests.createValueSanitizers(value, "sanitized")) {
            String resourceId = "/resourceGroups/" + otherValue + "/providers/Microsoft.CognitiveServices";
            Assertions.assertEquals(resourceId,
                Pattern.compile(sanitizer.getRegex()).matcher(resourceId).replaceAll(sanitizer.getRedactedValue()));
        }
    }
}
