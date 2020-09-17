// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTests {

    @Test
    public void testWebAppPrivateRegistryImage() throws Exception {
        // completion
        Assertions
            .assertEquals(
                "weidxuregistry.azurecr.io/az-func-java:v1",
                Utils
                    .smartCompletionPrivateRegistryImage(
                        "weidxuregistry.azurecr.io/az-func-java:v1", "https://weidxuregistry.azurecr.io"));

        // completion
        Assertions
            .assertEquals(
                "weidxuregistry.azurecr.io/az-func-java:v1",
                Utils.smartCompletionPrivateRegistryImage("az-func-java:v1", "https://weidxuregistry.azurecr.io"));

        // completion
        Assertions
            .assertEquals(
                "weidxuregistry.azurecr.io/weidxu/az-func-java:v1",
                Utils
                    .smartCompletionPrivateRegistryImage(
                        "weidxu/az-func-java:v1", "https://weidxuregistry.azurecr.io"));

        // completion
        Assertions
            .assertEquals(
                "weidxuregistry.azurecr.io:5000/weidxu/az-func-java:v1",
                Utils
                    .smartCompletionPrivateRegistryImage(
                        "weidxu/az-func-java:v1", "https://weidxuregistry.azurecr.io:5000"));

        // completion
        Assertions
            .assertEquals(
                "weidxuregistry.azurecr.io/weidxu/az-func-java:v1",
                Utils
                    .smartCompletionPrivateRegistryImage(
                        "az-func-java:v1", "https://weidxuregistry.azurecr.io/weidxu"));

        // completion not happen due to possible host
        Assertions
            .assertEquals(
                "host.name/az-func-java:v1",
                Utils
                    .smartCompletionPrivateRegistryImage(
                        "host.name/az-func-java:v1", "https://weidxuregistry.azurecr.io"));

        // completion not happen due to possible port
        Assertions
            .assertEquals(
                "host:port/az-func-java:v1",
                Utils
                    .smartCompletionPrivateRegistryImage(
                        "host:port/az-func-java:v1", "https://weidxuregistry.azurecr.io"));

        // completion not happen due to no idea what it is
        Assertions
            .assertEquals(
                "/az-func-java:v1",
                Utils.smartCompletionPrivateRegistryImage("/az-func-java:v1", "https://weidxuregistry.azurecr.io"));

        // completion not happen due to incorrect serviceUrl
        Assertions
            .assertEquals(
                "az-func-java:v1",
                Utils.smartCompletionPrivateRegistryImage("az-func-java:v1", "weidxuregistry.azurecr.io"));
    }

    @Test
    public void testBase16() {
        testBase16Encoding("", "");
        testBase16Encoding("f", "66");
        testBase16Encoding("fo", "666F");
        testBase16Encoding("foo", "666F6F");
        testBase16Encoding("foob", "666F6F62");
        testBase16Encoding("fooba", "666F6F6261");
        testBase16Encoding("foobar", "666F6F626172");
    }

    private static void testBase16Encoding(String decoded, String encoded) {
        Assertions.assertEquals(Utils.base16Encode(decoded.getBytes(StandardCharsets.UTF_8)), encoded);
    }

    @Test
    public void testBase64Url() {
        String encoded =
            "eyJuYmYiOjE1ODI2OTM0NTIsImV4cCI6MTU4MjY5Mzc1MiwiaWF0IjoxNTgyNjkzNDUyLCJpc3MiOiJodHRwczovL3dhMS13ZWlkeHUuc2NtLmF6dXJld2Vic2l0ZXMubmV0IiwiYXVkIjoiaHR0cHM6Ly93YTEtd2VpZHh1LmF6dXJld2Vic2l0ZXMubmV0L2F6dXJlZnVuY3Rpb25zIn0";
        String decoded =
            "{\"nbf\":1582693452,\"exp\":1582693752,\"iat\":1582693452,\"iss\":\"https://wa1-weidxu.scm.azurewebsites.net\",\"aud\":\"https://wa1-weidxu.azurewebsites.net/azurefunctions\"}";
        Assertions.assertEquals(decoded, new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8));
    }
}
