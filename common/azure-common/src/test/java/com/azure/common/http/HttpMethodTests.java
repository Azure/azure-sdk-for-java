// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpMethodTests {
    @Test
    public void getTest() {
        assertEquals("GET", HttpMethod.GET.toString());
    }

    @Test
    public void putTest() {
        assertEquals("PUT", HttpMethod.PUT.toString());
    }

    @Test
    public void postTest() {
        assertEquals("POST", HttpMethod.POST.toString());
    }

    @Test
    public void patchTest() {
        assertEquals("PATCH", HttpMethod.PATCH.toString());
    }

    @Test
    public void deleteTest() {
        assertEquals("DELETE", HttpMethod.DELETE.toString());
    }

    @Test
    public void headTest() {
        assertEquals("HEAD", HttpMethod.HEAD.toString());
    }
}
