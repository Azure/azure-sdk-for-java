// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.exception.HttpResponseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StorageImplUtilsTests {
    private static final String[] URL_SUFFIXES
        = { "", "-secondary", "-dualstack", "-ipv6", "-secondary-dualstack", "-secondary-ipv6" };

    private static final String[] ALL_SUBDOMAINS = {
        Constants.Blob.URI_SUBDOMAIN,
        Constants.File.URI_SUBDOMAIN,
        Constants.Queue.URI_SUBDOMAIN,
        Constants.Table.URI_SUBDOMAIN,
        Constants.Dfs.URI_SUBDOMAIN };

    private static final String[] BLOB_AND_DFS = { Constants.Blob.URI_SUBDOMAIN, Constants.Dfs.URI_SUBDOMAIN };

    @ParameterizedTest
    @MethodSource("exceptionCallables")
    void sendRequestThrowsExceptions(Callable<?> operation, Class<? extends Exception> expectedCauseType) {
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> StorageImplUtils.sendRequest(operation, Duration.ofSeconds(120), HttpResponseException.class));

        assertNotNull(e.getCause());
        assertInstanceOf(expectedCauseType, e.getCause());
    }

    @ParameterizedTest
    @MethodSource("domainNamesSupplier")
    void canGetIpv4AndIpv6AccountNames(URL url, String serviceSubDomain, String expected) throws MalformedURLException {
        assertEquals(expected, StorageImplUtils.getAccountName(url, serviceSubDomain));
    }

    private static Stream<Arguments> exceptionCallables() {
        Callable<Object> timeoutCallable = () -> {
            throw new TimeoutException();
        };

        Callable<Object> runtimeCallable = () -> {
            throw new RuntimeException("rt");
        };

        Callable<Object> executionCallable = () -> {
            throw new ExecutionException("exec", new RuntimeException("inner"));
        };

        Callable<Object> interruptedCallable = () -> {
            throw new InterruptedException("interrupted");
        };

        return Stream.of(Arguments.of(timeoutCallable, TimeoutException.class),
            Arguments.of(runtimeCallable, RuntimeException.class),
            Arguments.of(executionCallable, ExecutionException.class),
            Arguments.of(interruptedCallable, InterruptedException.class));
    }

    private static Stream<Arguments> domainNamesSupplier() throws Exception {
        List<Arguments> args = new ArrayList<>();

        // Standard subdomain variants
        for (String subdomain : ALL_SUBDOMAINS) {
            args.add(generateURL("accountname", subdomain, ""));
        }

        // Secondary, dualstack, and IPv6 suffix variants — blob and dfs only
        for (String subdomain : BLOB_AND_DFS) {
            for (String suffix : URL_SUFFIXES) {
                args.add(generateURL("myaccount", subdomain, suffix));
            }
        }

        // Accounts with natural hyphens — should NOT be stripped
        for (String subdomain : BLOB_AND_DFS) {
            args.add(generateURL("md-d3rqxhqbxbwq", subdomain, ""));
            args.add(generateURL("md-ssd-bndub02px100c21", subdomain, ""));
        }

        return args.stream();
    }

    private static Arguments generateURL(String account, String subdomain, String suffix) throws MalformedURLException {
        return Arguments.of(new URL("https://" + account + suffix + "." + subdomain + ".core.windows.net/"), subdomain,
            account);
    }
}
