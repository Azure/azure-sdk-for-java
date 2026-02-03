// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.utils.CoreUtils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.implementation.Netty4Utility.NETTY_VERSION_PROPERTY;
import static io.clientcore.http.netty4.implementation.Netty4Utility.PROPERTIES_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class Netty4UtilityTests {
    @Test
    public void validateNettyVersionsWithWhatThePomSpecifies() {
        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        Netty4Utility.NettyVersionLogInformation logInformation
            = Netty4Utility.createNettyVersionLogInformation(pomVersions.get(NETTY_VERSION_PROPERTY));

        // Should never have version mismatches when running tests, that would mean either the version properties are
        // wrong or there is a dependency diamond within http-netty4. Either way, it should be fixed.
        assertFalse(logInformation.shouldLog());
        for (String artifactFullName : logInformation.classpathNettyVersions.keySet()) {
            assertTrue(artifactFullName.startsWith("io.netty:netty-"),
                "All artifact information should start with 'io.netty:netty-'");
        }
    }

    @Test
    public void validateNettyVersionsWithJunkVersions() {
        Netty4Utility.NettyVersionLogInformation logInformation
            = Netty4Utility.createNettyVersionLogInformation("4.0.0.Final");

        // Junk versions used should flag for logging.
        assertTrue(logInformation.shouldLog());
        for (String artifactFullName : logInformation.classpathNettyVersions.keySet()) {
            assertTrue(artifactFullName.startsWith("io.netty:netty-"),
                "All artifact information should start with 'io.netty:netty-'");
        }
    }

    @Test
    public void convertNettyHeadersToClientCoreHeaders() {
        HttpHeaders clientCoreHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "42")
            .set(HttpHeaderName.CONTENT_TYPE, "application/json")
            .set(HttpHeaderName.ACCEPT, Arrays.asList("application/json", "text/json"));

        io.netty.handler.codec.http.HttpHeaders nettyHeaders
            = new DefaultHttpHeaders().add(HttpHeaderNames.CONTENT_LENGTH, "42")
                .add(HttpHeaderNames.CONTENT_TYPE, "application/json")
                .add(HttpHeaderNames.ACCEPT, "application/json")
                .add(HttpHeaderNames.ACCEPT, "text/json");

        HttpHeaders convertedHeaders = Netty4Utility.convertHeaders(nettyHeaders);

        assertEquals(clientCoreHeaders.getSize(), convertedHeaders.getSize());
        clientCoreHeaders.stream().forEach(httpHeader -> {
            HttpHeader convertedHeader = convertedHeaders.get(httpHeader.getName());
            assertNotNull(convertedHeader, "Converted headers should contain the header names as the original headers");
            assertLinesMatch(httpHeader.getValues(), convertedHeader.getValues(),
                "Converted headers should have the same values as the original headers");
        });
    }

    @Test
    public void setOrSuppressErrorSetsWhenUnset() {
        AtomicReference<Throwable> unset = new AtomicReference<>();
        RuntimeException toSet = new RuntimeException();

        Netty4Utility.setOrSuppressError(unset, toSet);

        assertSame(toSet, unset.get(), "The error should be set to the runtime exception that was passed.");
    }

    @Test
    public void setOrSuppressErrorNoOpsWhenSameErrorIsSeen() {
        RuntimeException initial = new RuntimeException();
        AtomicReference<Throwable> sameError = new AtomicReference<>(initial);

        Netty4Utility.setOrSuppressError(sameError, initial);

        Throwable setError = sameError.get();
        assertSame(initial, setError, "The error should be set to the runtime exception that was initially set.");
        assertEquals(0, setError.getSuppressed().length,
            "The suppressed error should be empty as the same error was seen.");
    }

    @Test
    public void setOrSuppressErrorSuppressesWhenNewErrorIsSeen() {
        RuntimeException initial = new RuntimeException();
        AtomicReference<Throwable> alreadySet = new AtomicReference<>(initial);
        RuntimeException additional = new RuntimeException();

        Netty4Utility.setOrSuppressError(alreadySet, additional);

        Throwable setError = alreadySet.get();
        assertSame(initial, setError, "The error should be set to the runtime exception that was initially set.");
        assertEquals(1, setError.getSuppressed().length,
            "The suppressed errors should have one as a new error was seen.");
        assertSame(additional, setError.getSuppressed()[0],
            "The suppressed error should be the new error that was seen.");
    }
}
