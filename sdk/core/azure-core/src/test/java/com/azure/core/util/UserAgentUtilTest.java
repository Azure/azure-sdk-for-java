// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserAgentUtil}.
 */
public class UserAgentUtilTest {

    @Test
    void testUserAgentStringFormat() {
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String plaform = new StringBuilder().append("(")
            .append(javaVersion).append("; ")
            .append(osName).append("; ")
            .append(osVersion).append(")")
            .toString();

        // with platform info
        assertEquals("azsdk-java-azure-storage-blob/12.0.0 " + plaform,
            UserAgentUtil.toUserAgentString(null, "azure-storage-blob", "12.0.0", null));
        assertEquals("myapp azsdk-java-azure-storage-blob/12.0.0 " + plaform,
            UserAgentUtil.toUserAgentString("myapp", "azure-storage-blob", "12.0.0", null));

        // without platform info
        assertEquals("azsdk-java-azure-storage-blob/12.0.0",
            UserAgentUtil.toUserAgentString(null, "azure-storage-blob", "12.0.0",
                Configuration.getGlobalConfiguration().clone().put("AZURE_TELEMETRY_DISABLED", "true")));
        assertEquals("myapp azsdk-java-azure-storage-blob/12.0.0",
            UserAgentUtil.toUserAgentString("myapp", "azure-storage-blob", "12.0.0",
                Configuration.getGlobalConfiguration().clone().put("AZURE_TELEMETRY_DISABLED", "true")));

        // long app id should be truncated
        assertThrows(IllegalArgumentException.class, () ->
            UserAgentUtil.toUserAgentString("ReallyLongApplicationIdentity", "azure-storage-blob", "12.0.0", null));

        // null sdk name and version
        assertEquals("myapp azsdk-java-null/null " + plaform,
            UserAgentUtil.toUserAgentString("myapp", null, null, null));

    }
}
