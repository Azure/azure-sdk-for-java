// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.http.ContentType;
import com.azure.core.test.implementation.entities.HttpBinFormDataJSON;
import com.azure.core.test.implementation.entities.HttpBinJSON;
import com.azure.core.test.utils.MessageDigestUtils;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public final class RestProxyTestsWireMockServer {
    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final byte[] BUFFER;

    static {
        BUFFER = new byte[1024];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {

                BUFFER[i * 32 + j] = (byte) (j + 64);
            }
        }
    }

    public static WireMockServer getRestProxyTestsServer() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options()
            .extensions(new RestProxyResponseTransformer())
            .dynamicPort()
            .dynamicHttpsPort()
            .disableRequestJournal()
            .containerThreads(50)
            .gzipDisabled(true));

        // Stub that will return a response with a body containing the passed number of bytes.
        server.stubFor(get(urlPathMatching("/bytes/\\d+")));

        // Stub that will return a response with a body that contains the URL string as-is.
        server.stubFor(get(urlPathMatching("/anything.*")));

        // Stub that will return a response with the passed status code.
        server.stubFor(get(urlPathMatching("/status/\\d+")));

        // Simple stubs that will return what is passed.
        server.stubFor(post("post"));
        server.stubFor(put("put"));
        server.stubFor(head(urlPathMatching("/anything")));
        server.stubFor(delete("delete"));
        server.stubFor(patch(urlPathMatching("/patch")));
        server.stubFor(get("/get"));

        return server;
    }

    private static final class RestProxyResponseTransformer extends ResponseDefinitionTransformer {
        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
            FileSource fileSource, Parameters parameters) {
            UrlBuilder urlBuilder = UrlBuilder.parse(request.getAbsoluteUrl());
            String path = urlBuilder.getPath();
            if (path.startsWith("/bytes")) {
                return createBytesResponse(path);
            } else if (path.startsWith("/status")) {
                return createStatusResponse(path);
            } else if (path.startsWith("/post")) {
                try {
                    if ("application/x-www-form-urlencoded".equalsIgnoreCase(request.getHeader("Content-Type"))) {
                        return createFormResponse(request);
                    } else {
                        return createSimpleHttpBinResponse(request, urlBuilder);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (path.startsWith("/anything") || path.startsWith("/put") || path.startsWith("/delete")
                || path.startsWith("/patch") || path.startsWith("/get")) {
                try {
                    return createSimpleHttpBinResponse(request, urlBuilder);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }  else {
                return responseDefinition;
            }
        }

        private static ResponseDefinition createBytesResponse(String urlPath) {
            int bodySize = Integer.parseInt(urlPath.split("/", 3)[2]);
            Map<String, String> rawHeaders = getBaseHttpHeaders();
            rawHeaders.put("Content-Type", ContentType.APPLICATION_OCTET_STREAM);
            rawHeaders.put("Content-Length", String.valueOf(bodySize));

            byte[] body = new byte[bodySize];
            int count = bodySize / 1024;
            int remainder = bodySize % 1024;
            for (int i = 0; i < count; i++) {
                System.arraycopy(BUFFER, 0, body, i * 1024, 1024);
            }

            if (remainder > 0) {
                System.arraycopy(BUFFER, 0, body, count * 1024, remainder);
            }

            rawHeaders.put("ETag", MessageDigestUtils.md5(body));

            return new ResponseDefinitionBuilder().withStatus(200)
                .withHeaders(toWireMockHeaders(rawHeaders))
                .withBody(body)
                .build();
        }

        private static ResponseDefinition createSimpleHttpBinResponse(Request request, UrlBuilder urlBuilder)
            throws IOException {
            HttpBinJSON responseBody = new HttpBinJSON();
            responseBody.url(cleanseUrl(urlBuilder));
            responseBody.data(request.getBodyAsString());

            if (request.getHeaders() != null) {
                responseBody.headers(request.getHeaders().all().stream()
                    .collect(Collectors.toMap(MultiValue::key, MultiValue::values)));
            }

            return new ResponseDefinitionBuilder().withStatus(200)
                .withHeaders(toWireMockHeaders(getBaseHttpHeaders()))
                .withBody(SERIALIZER_ADAPTER.serialize(responseBody, SerializerEncoding.JSON))
                .build();
        }

        private static ResponseDefinition createStatusResponse(String urlPath) {
            return new ResponseDefinitionBuilder().withStatus(Integer.parseInt(urlPath.split("/", 3)[2])).build();
        }

        private static ResponseDefinition createFormResponse(Request request) throws IOException {
            HttpBinFormDataJSON formBody = new HttpBinFormDataJSON();
            HttpBinFormDataJSON.Form form = new HttpBinFormDataJSON.Form();
            List<String> toppings = new ArrayList<>();

            for (String formKvp : request.getBodyAsString().split("&")) {
                String[] kvpPieces = formKvp.split("=", 2);

                switch (kvpPieces[0]) {
                    case "custname":
                        form.customerName(kvpPieces[1]);
                        break;
                    case "custtel":
                        form.customerTelephone(kvpPieces[1]);
                        break;
                    case "custemail":
                        form.customerEmail(kvpPieces[1]);
                        break;
                    case "size":
                        form.pizzaSize(HttpBinFormDataJSON.PizzaSize.valueOf(kvpPieces[1]));
                        break;
                    case "toppings":
                        toppings.add(kvpPieces[1]);
                        break;
                    default:
                        break;
                }
            }

            form.toppings(toppings);
            formBody.form(form);

            return new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody(SERIALIZER_ADAPTER.serialize(formBody, SerializerEncoding.JSON))
                .build();
        }

        private static String cleanseUrl(UrlBuilder urlBuilder) {
            StringBuilder builder = new StringBuilder();
            builder.append(urlBuilder.getScheme())
                .append("://")
                .append(urlBuilder.getHost())
                .append(urlBuilder.getPath().replace("%20", " "));

            String queryString = urlBuilder.getQueryString();
            if (!CoreUtils.isNullOrEmpty(queryString)) {
                builder.append(queryString.replace("%20", " "));
            }

            return builder.toString();
        }

        private static Map<String, String> getBaseHttpHeaders() {
            Map<String, String> baseHeaders = new HashMap<>();
            baseHeaders.put("Date", new DateTimeRfc1123(OffsetDateTime.now(ZoneOffset.UTC)).toString());
            baseHeaders.put("Connection", "keep-alive");
            baseHeaders.put("X-Processed-Time", String.valueOf(Math.random() * 10));
            baseHeaders.put("Access-Control-Allow-Credentials", "true");
            baseHeaders.put("Content-Type", "application/json");

            return baseHeaders;
        }

        private static HttpHeaders toWireMockHeaders(Map<String, String> rawHeaders) {
            return new HttpHeaders(rawHeaders.entrySet().stream()
                .map(kvp -> new HttpHeader(kvp.getKey(), kvp.getValue()))
                .collect(Collectors.toList()));
        }

        @Override
        public String getName() {
            return "rest-proxy-transformer";
        }
    }
}
