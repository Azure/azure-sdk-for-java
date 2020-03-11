// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.implementation.entities.HttpBinFormDataJSON;
import com.azure.core.test.implementation.entities.HttpBinJSON;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.serializer.JacksonAdapter;
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
import java.net.URL;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public final class RestProxyTestsWireMockServer {
    public static WireMockServer getRestProxyTestsServer() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options()
            .extensions(new RestProxyResponseTransformer())
            .port(21354)
            .disableRequestJournal()
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
        server.stubFor(get(urlPathMatching("/get")));

        return server;
    }

    private static final class RestProxyResponseTransformer extends ResponseDefinitionTransformer {
        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
            FileSource fileSource, Parameters parameters) {
            try {
                return transformImpl(request, responseDefinition);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private ResponseDefinition transformImpl(Request request, ResponseDefinition responseDefinition)
            throws IOException {
            URL url = new URL(request.getAbsoluteUrl());

            if (url.getPath().startsWith("/bytes")) {
                int bodySize = Integer.parseInt(url.getPath().split("/", 3)[2]);

                return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withHeaders(new HttpHeaders().plus(
                        new HttpHeader("Access-Control-Allow-Credentials", "true"),
                        new HttpHeader("X-Processed-Time", String.valueOf(Math.random() * 10)),
                        new HttpHeader("Date", new DateTimeRfc1123(OffsetDateTime.now(ZoneOffset.UTC)).toString()),
                        new HttpHeader("Content-Type", "application/octet-stream"),
                        new HttpHeader("Content-Length", String.valueOf(bodySize))
                    ))
                    .withBody(new SecureRandom().generateSeed(bodySize))
                    .build();
            } else if (url.getPath().startsWith("/anything")) {
                HttpBinJSON responseBody = new HttpBinJSON();
                responseBody.url(url.toString().replace("%20", " "));

                if (request.getHeaders() != null) {
                    responseBody.headers(request.getHeaders().all().stream()
                        .collect(Collectors.toMap(MultiValue::key, MultiValue::firstValue)));
                }

                return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withBody(new JacksonAdapter().serialize(responseBody, SerializerEncoding.JSON))
                    .build();
            } else if (url.getPath().startsWith("/post")) {
                if ("application/x-www-form-urlencoded".equalsIgnoreCase(request.getHeader("Content-Type"))) {
                    HttpBinFormDataJSON formBody = new HttpBinFormDataJSON();
                    HttpBinFormDataJSON.Form form = new HttpBinFormDataJSON.Form();
                    List<String> toppings = new ArrayList<>();

                    for (String formKvp : request.getBodyAsString().split("&")) {
                        String[] kvpPieces = formKvp.split("=");

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
                        .withBody(new JacksonAdapter().serialize(formBody, SerializerEncoding.JSON))
                        .build();
                } else {
                    HttpBinJSON responseBody = new HttpBinJSON();
                    responseBody.data(request.getBodyAsString());

                    return new ResponseDefinitionBuilder()
                        .withStatus(200)
                        .withBody(new JacksonAdapter().serialize(responseBody, SerializerEncoding.JSON))
                        .build();
                }
            } else if (url.getPath().startsWith("/put")) {
                HttpBinJSON responseBody = new HttpBinJSON();
                responseBody.data(request.getBodyAsString());
                responseBody.url(url.toString().replace("%20", " "));

                if (request.getHeaders() != null) {
                    responseBody.headers(request.getHeaders().all().stream()
                        .collect(Collectors.toMap(MultiValue::key, MultiValue::firstValue)));
                }

                return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withHeaders(new HttpHeaders().plus(
                        new HttpHeader("Access-Control-Allow-Credentials", "true"),
                        new HttpHeader("X-Processed-Time", String.valueOf(Math.random() * 10)),
                        new HttpHeader("Date", new DateTimeRfc1123(OffsetDateTime.now(ZoneOffset.UTC)).toString())
                    ))
                    .withBody(new JacksonAdapter().serialize(responseBody, SerializerEncoding.JSON))
                    .build();
            } else if (url.getPath().startsWith("/delete")) {
                HttpBinJSON responseBody = new HttpBinJSON();
                responseBody.data(request.getBodyAsString());

                return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withBody(new JacksonAdapter().serialize(responseBody, SerializerEncoding.JSON))
                    .build();
            } else if (url.getPath().startsWith("/patch")) {
                HttpBinJSON responseBody = new HttpBinJSON();
                responseBody.data(request.getBodyAsString());

                return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withBody(new JacksonAdapter().serialize(responseBody, SerializerEncoding.JSON))
                    .build();
            } else if (url.getPath().startsWith("/get")) {
                HttpBinJSON responseBody = new HttpBinJSON();
                responseBody.url(url.toString().replace("%20", " "));

                return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withBody(new JacksonAdapter().serialize(responseBody, SerializerEncoding.JSON))
                    .build();
            } else if (url.getPath().startsWith("/status")) {
                return new ResponseDefinitionBuilder()
                    .withStatus(Integer.parseInt(url.getPath().split("/", 3)[2]))
                    .build();
            } else {
                return responseDefinition;
            }
        }

        @Override
        public String getName() {
            return "rest-proxy-transformer";
        }
    }
}
