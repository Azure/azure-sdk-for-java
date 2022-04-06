// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Unit tests for {@link UserAgentUtil}.
 */
public class UserAgentUtilTest {

    @MethodSource("getUserAgentTestCases")
    @ParameterizedTest
    void testUserAgentStringFormat(String expectedUserAgentString, String applicationId, String sdkName, String version,
                                   Configuration configuration) {
        assertEquals(expectedUserAgentString, UserAgentUtil.toUserAgentString(applicationId, sdkName, version, configuration));
    }

    @Test
    public void longAppIdTest() {
        // long app id should be truncated
        assertThrows(IllegalArgumentException.class, () ->
                UserAgentUtil.toUserAgentString("ReallyLongApplicationIdentity", "azure-storage-blob", "12.0.0", null));
    }

    private static Stream<Arguments> getUserAgentTestCases() {
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String platform = new StringBuilder().append("(")
                .append(javaVersion).append("; ")
                .append(osName).append("; ")
                .append(osVersion).append(")")
                .toString();

        return Stream.of(
                Arguments.of("azsdk-java-storage-blob/12.0.0 " + platform, null, "azure-storage-blob", "12.0.0", null),
                Arguments.of("myapp azsdk-java-storage-blob/12.0.0 " + platform, "myapp", "azure-storage-blob",
                        "12.0.0", null),
                Arguments.of("myapp azsdk-java-null/null " + platform, "myapp", null, null, null),
                Arguments.of("azsdk-java-null/null " + platform, null, null, null, null),
                Arguments.of("azsdk-java-storage-blob-azure/12.0.0 " + platform, null, "azure-storage-blob-azure",
                        "12.0.0", null),
                Arguments.of("azsdk-java-aazure-storage-blob/12.0.0 " + platform, null, "aazure-storage-blob",
                        "12.0.0", null),

                // without platform info
                Arguments.of("azsdk-java-storage-blob/12.0.0", null, "azure-storage-blob", "12.0.0",
                        Configuration.getGlobalConfiguration().clone().put("AZURE_TELEMETRY_DISABLED", "true")),
                Arguments.of("myapp azsdk-java-storage-blob/12.0.0", "myapp", "azure-storage-blob", "12.0.0",
                        Configuration.getGlobalConfiguration().clone().put("AZURE_TELEMETRY_DISABLED", "true"))
        );
    }


}
