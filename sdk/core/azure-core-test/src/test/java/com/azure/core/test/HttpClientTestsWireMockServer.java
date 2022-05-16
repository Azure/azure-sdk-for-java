// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.http.HttpClientTests;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;

/**
 * WireMock server used when running {@link HttpClientTests}.
 */
public class HttpClientTestsWireMockServer {
    private static final String PLAIN_RESPONSE = "/plainBytesNoHeader";
    private static final String HEADER_RESPONSE = "/plainBytesWithHeader";
    private static final String INVALID_HEADER_RESPONSE = "/plainBytesInvalidHeader";
    private static final String UTF_8_BOM_RESPONSE = "/utf8BomBytes";
    private static final String UTF_16BE_BOM_RESPONSE = "/utf16BeBomBytes";
    private static final String UTF_16LE_BOM_RESPONSE = "/utf16LeBomBytes";
    private static final String UTF_32BE_BOM_RESPONSE = "/utf32BeBomBytes";
    private static final String UTF_32LE_BOM_RESPONSE = "/utf32LeBomBytes";
    private static final String BOM_WITH_SAME_HEADER = "/bomBytesWithSameHeader";
    private static final String BOM_WITH_DIFFERENT_HEADER = "/bomBytesWithDifferentHeader";
    private static final String ECHO_RESPONSE = "/echo";

    private static final byte[] UTF_8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final byte[] UTF_16BE_BOM = {(byte) 0xFE, (byte) 0xFF};
    private static final byte[] UTF_16LE_BOM = {(byte) 0xFF, (byte) 0xFE};
    private static final byte[] UTF_32BE_BOM = {(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
    private static final byte[] UTF_32LE_BOM = {(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};

    private static final byte[] RETURN_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);

    public static WireMockServer getHttpClientTestsServer() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options()
            .extensions(new HttpClientResponseTransformer())
            .dynamicPort()
            .disableRequestJournal()
            .gzipDisabled(true));

        // Basic bytes response.
        server.stubFor(get(PLAIN_RESPONSE).willReturn(aResponse().withBody(RETURN_BYTES)));

        // Basic bytes with 'Content-Encoding' header.
        server.stubFor(get(HEADER_RESPONSE).willReturn(aResponse().withBody(RETURN_BYTES)
            .withHeader("Content-Type", "charset=UTF-16BE")));

        // Basic bytes with invalid 'Content-Encoding' header.
        server.stubFor(get(INVALID_HEADER_RESPONSE).willReturn(aResponse().withBody(RETURN_BYTES)
            .withHeader("Content-Type", "charset=invalid")));

        // Bytes with leading UTF-8 BOM.
        server.stubFor(get(UTF_8_BOM_RESPONSE).willReturn(aResponse().withBody(addBom(UTF_8_BOM))));

        // Bytes with leading UTF-16 BE BOM.
        server.stubFor(get(UTF_16BE_BOM_RESPONSE).willReturn(aResponse().withBody(addBom(UTF_16BE_BOM))));

        // Bytes with leading UTF-16 LE BOM.
        server.stubFor(get(UTF_16LE_BOM_RESPONSE).willReturn(aResponse().withBody(addBom(UTF_16LE_BOM))));

        // Bytes with leading UTF-32 BE BOM.
        server.stubFor(get(UTF_32BE_BOM_RESPONSE).willReturn(aResponse().withBody(addBom(UTF_32BE_BOM))));

        // Bytes with leading UTF-32 LE BOM.
        server.stubFor(get(UTF_32LE_BOM_RESPONSE).willReturn(aResponse().withBody(addBom(UTF_32LE_BOM))));

        // Bytes with leading UTF-8 BOM and matching 'Content-Encoding' header.
        server.stubFor(get(BOM_WITH_SAME_HEADER).willReturn(aResponse()
            .withBody(addBom(UTF_8_BOM)).withHeader("Content-Type", "charset=UTF-8")));

        // Bytes with leading UTF-8 BOM and differing 'Content-Encoding' header.
        server.stubFor(get(BOM_WITH_DIFFERENT_HEADER).willReturn(aResponse()
            .withBody(addBom(UTF_8_BOM)).withHeader("Content-Type", "charset=UTF-16")));

        // Echoes request body
        server.stubFor(put(ECHO_RESPONSE)
            .willReturn(aResponse()
                .withTransformers(HttpClientResponseTransformer.NAME)));

        return server;
    }

    private static byte[] addBom(byte[] arr1) {
        byte[] mergedArray = new byte[arr1.length + RETURN_BYTES.length];

        System.arraycopy(arr1, 0, mergedArray, 0, arr1.length);
        System.arraycopy(RETURN_BYTES, 0, mergedArray, arr1.length, RETURN_BYTES.length);

        return mergedArray;
    }

    private static final class HttpClientResponseTransformer extends ResponseDefinitionTransformer {

        public static final String NAME = "http-client-transformer";

        @Override
        public ResponseDefinition transform(
            Request request, ResponseDefinition responseDefinition, FileSource fileSource, Parameters parameters) {
            try {
                URL requestUrl = new URL(request.getAbsoluteUrl());
                String path = requestUrl.getPath();
                if (ECHO_RESPONSE.equals(path)) {
                    return aResponse()
                        .withBody(request.getBody())
                        .build();
                } else {
                    return responseDefinition;
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getName() {
            return NAME;
        }
    }
}
