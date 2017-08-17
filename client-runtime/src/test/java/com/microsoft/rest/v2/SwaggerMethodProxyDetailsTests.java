package com.microsoft.rest.v2;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class SwaggerMethodProxyDetailsTests {

    @Test
    public void setMethod() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.setHttpMethod("METHOD");
        assertEquals("METHOD", details.httpMethod());
    }

    @Test
    public void setRelativePath() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.setRelativePath("RELATIVE_PATH");
        assertEquals("RELATIVE_PATH", details.relativePath());
    }

    @Test
    public void setMethodAndRelativePath() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.setMethodAndRelativePath("A", "B");
        assertEquals("A", details.httpMethod());
        assertEquals("B", details.relativePath());
    }

    @Test
    public void addHostSubstitutionWithNoPlaceholders() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.addHostSubstitution("hostParam", 0, false);
        assertEquals("I'm a host", details.applyHostSubstitutions("I'm a host", new Object[]{"host sub"}));
    }

    @Test
    public void addHostSubstitutionWithNoMatchingPlaceholderSubstitutions() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.addHostSubstitution("hostParam", 0, false);
        assertEquals("I'm a {host}", details.applyHostSubstitutions("I'm a {host}", new Object[]{"host sub"}));
    }

    @Test
    public void addHostSubstitutionWithMatchingEncodedPlaceholderSubstitution() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.addHostSubstitution("hostParam", 0, false);
        assertEquals("I'm a host sub", details.applyHostSubstitutions("I'm a {hostParam}", new Object[]{"host sub"}));
    }

    @Test
    public void addHostSubstitutionWithMatchingNotEncodedPlaceholderSubstitution() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.addHostSubstitution("hostParam", 0, true);
        assertEquals("I'm a host+sub", details.applyHostSubstitutions("I'm a {hostParam}", new Object[]{"host sub"}));
    }

    @Test
    public void addPathSubstitutionWithNoPlaceholders() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.setRelativePath("relative/path/to/index.html");
        details.addPathSubstitution("pathParam", 0, false);
        assertEquals("relative/path/to/index.html", details.getSubstitutedPath(new Object[]{"index.html"}));
    }

    @Test
    public void addPathSubstitutionWithNoMatchingPlaceholderSubstitutions() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.setRelativePath("relative/path/to/{fileName}");
        details.addPathSubstitution("pathParam", 0, false);
        assertEquals("relative/path/to/{fileName}", details.getSubstitutedPath(new Object[]{"index.html"}));
    }

    @Test
    public void addPathSubstitutionWithMatchingEncodedPlaceholderSubstitution() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.setRelativePath("relative/path/to/{fileName}");
        details.addPathSubstitution("fileName", 0, false);
        assertEquals("relative/path/to/index.html", details.getSubstitutedPath(new Object[]{"index.html"}));
    }

    @Test
    public void addPathSubstitutionWithMatchingNotEncodedPlaceholderSubstitution() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.setRelativePath("relative/path/to/{fileName}");
        details.addPathSubstitution("fileName", 1, true);
        assertEquals("relative/path/to/index.html", details.getSubstitutedPath(new Object[]{"", "index.html"}));
    }

    @Test
    public void addQuerySubstitution() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.addQuerySubstitution("A", 0, true);
        final Iterator<EncodedParameter> encodedQueryParameters = details.getEncodedQueryParameters(new Object[]{"B"}).iterator();
        assertEquals(new EncodedParameter("A", "B"), encodedQueryParameters.next());
        assertFalse(encodedQueryParameters.hasNext());
    }

    @Test
    public void addHeaderSubstitution() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.addHeaderSubstitution("C", 1);
        final Iterator<EncodedParameter> encodedHeaderParameters = details.getEncodedHeaderParameters(new Object[]{"Z", "Y"}).iterator();
        assertEquals(new EncodedParameter("C", "Y"), encodedHeaderParameters.next());
        assertFalse(encodedHeaderParameters.hasNext());
    }

    @Test
    public void setBodyContentMethodParameterIndex() {
        final SwaggerMethodProxyDetails details = new SwaggerMethodProxyDetails("method.name");
        assertEquals("method.name", details.fullyQualifiedMethodName());
        details.setBodyContentMethodParameterIndex(17);
        assertEquals(new Integer(17), details.bodyContentMethodParameterIndex());
    }

    @Test
    public void encodeWithBadEncoding() {
        assertEquals(" ", SwaggerMethodProxyDetails.encode(" ", "myBadEncoding"));
    }

    @Test
    public void encodeWithDefaultEncoding() {
        assertEquals("+", SwaggerMethodProxyDetails.encode(" "));
    }
}
