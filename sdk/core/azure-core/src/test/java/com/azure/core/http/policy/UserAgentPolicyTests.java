// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.Configuration;
import com.azure.core.util.ServiceVersion;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * User Agent Policy Test
 */
public class UserAgentPolicyTests {

    @Test
    public void testUserAgentStringFormat() throws NoSuchFieldException, IllegalAccessException {
        ServiceVersion serviceVersion = Mockito.mock(ServiceVersion.class);
        when(serviceVersion.getVersion()).thenReturn("1.0.0");
        UserAgentPolicy userAgentPolicy = new UserAgentPolicy("application-configuration", "1.0.0-preview.4", Configuration.getGlobalConfiguration(), serviceVersion);

        Field userAgentField = userAgentPolicy.getClass().getDeclaredField("userAgent");
        userAgentField.setAccessible(true);
        String userAgentValue = (String) userAgentField.get(userAgentPolicy);
        // https://azure.github.io/azure-sdk/general_azurecore.html, User-Agent Header format:
        //[<application_id> ]azsdk-<sdk_language>-<package_name>/<package_version> <platform_info>
        // ex: azsdk-java-application-configuration/1.0.0-preview.4 (11.0.4; Windows 10 10.0)


        // for optional application specific string, <application_id>, such as "AzCopy/10.0.4-Preview"
        // currently, java user agent header doesn't handle it.

        // for azsdk-<sdk_language> such as "azsdk-java", use regex "\bazsdk\-java"
        // for <package_name> such as "-application-configuration", use regex "(-[a-z]+)+"
        // for <package_version> such as "1.0.0-preview.4 ", use regex "[0-9a-Z-.]+"
        // for <platform_info> such as  “(NODE-VERSION v4.5.0; Windows_NT 10.0.14393)”, use regex "\([a-zA-Z0-9_;:\/\s.]+\)"
        assertTrue(userAgentValue.matches("\\bazsdk\\-java(-[a-z]+)+\\/[0-9a-z-.]+\\s\\([a-zA-Z0-9_;:\\/\\s.]+\\)"));
    }
}
