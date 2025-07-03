// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.models;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link HttpRequestContext} class.
 */
public class HttpRequestContextTest {
    @Test
    void testAddMultipleQueryParamValues() {
        HttpRequestContext ctx = new HttpRequestContext();
        ctx.addQueryParam("foo", "bar", true, false, false);
        ctx.addQueryParam("foo", "baz", true, false, false);

        List<String> values = ctx.getQueryParams().get("foo").getValues();
        assertEquals(2, values.size());
        assertTrue(values.contains("bar"));
        assertTrue(values.contains("baz"));
    }

    @Test
    void testAddSingleHeader() {
        HttpRequestContext ctx = new HttpRequestContext();
        ctx.addHeader("X-Test", "value1");
        Map<String, List<String>> headers = ctx.getHeaders();
        assertTrue(headers.containsKey("X-Test"));
        assertEquals(1, headers.get("X-Test").size());
        assertEquals("value1", headers.get("X-Test").get(0));
    }

    @Test
    void testAddMultipleHeaderValues() {
        HttpRequestContext ctx = new HttpRequestContext();
        ctx.addHeader("X-Multi", "v1");
        ctx.addHeader("X-Multi", "v2");
        List<String> values = ctx.getHeaders().get("X-Multi");
        assertEquals(2, values.size());
        assertTrue(values.contains("v1"));
        assertTrue(values.contains("v2"));
    }

    @Test
    void testAddStaticHeaders() {
        HttpRequestContext ctx = new HttpRequestContext();
        ctx.addStaticHeaders(new String[] { "X-Static:foo,bar", "X-Empty:" });
        assertTrue(ctx.getHeaders().containsKey("X-Static"));
        List<String> staticValues = ctx.getHeaders().get("X-Static");
        assertEquals(2, staticValues.size());
        assertTrue(staticValues.contains("foo"));
        assertTrue(staticValues.contains("bar"));
        assertTrue(ctx.getHeaders().containsKey("X-Empty"));
        assertTrue(ctx.getHeaders().get("X-Empty").isEmpty());
    }

    @Test
    void testAddStaticQueryParams() {
        HttpRequestContext ctx = new HttpRequestContext();
        ctx.addStaticQueryParams(new String[] { "a=1", "b=2", "c=" });
        assertEquals("1", ctx.getQueryParams().get("a").getValues().get(0));
        assertEquals("2", ctx.getQueryParams().get("b").getValues().get(0));
        assertEquals("", ctx.getQueryParams().get("c").getValues().get(0));
    }

    @Test
    void testAddDuplicateSubstitutionThrows() {
        HttpRequestContext ctx = new HttpRequestContext();
        Substitution sub1 = new Substitution("host", "hostParam", false);
        Substitution sub2 = new Substitution("host", "hostParam", true);
        ctx.addSubstitution(sub1);
        assertThrows(IllegalArgumentException.class, () -> ctx.addSubstitution(sub2));
    }

    @Test
    void testExpectedStatusCodes() {
        HttpRequestContext ctx = new HttpRequestContext();
        ctx.setExpectedStatusCodes(new int[] { 404, 200, 201 });
        List<Integer> codes = ctx.getExpectedStatusCodes();
        assertEquals(3, codes.size());
        assertTrue(codes.contains(200));
        assertTrue(codes.contains(201));
        assertTrue(codes.contains(404));
    }

    @Test
    void testBodySetAndGet() {
        HttpRequestContext.Body body = new HttpRequestContext.Body("application/json", null, "param");
        HttpRequestContext ctx = new HttpRequestContext();
        ctx.setBody(body);
        assertEquals(body, ctx.getBody());
        assertEquals("application/json", ctx.getBody().getContentType());
        assertEquals("param", ctx.getBody().getParameterName());
    }
}
