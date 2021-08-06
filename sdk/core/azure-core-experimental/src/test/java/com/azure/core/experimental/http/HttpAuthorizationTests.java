// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DynamicRequest}.
 */
public class HttpAuthorizationTests {
    @Test
    public void nullOrWhiteSpaceParameters()
    {
        Assertions.assertThrows(NullPointerException.class, () -> new HttpAuthorization(null, "parameter"));
        Assertions.assertThrows(NullPointerException.class, () -> new HttpAuthorization("scheme", null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new HttpAuthorization("", "parameter"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new HttpAuthorization("scheme", ""));
    }

    @Test
    public void toStringTest()
    {
        String scheme = "scheme";
        String parameter = "parameter";
        HttpAuthorization httpAuthorization = new HttpAuthorization(scheme, parameter);

        Assertions.assertEquals(String.format("%s %s", scheme, parameter), httpAuthorization.toString());
    }
}
