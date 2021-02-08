// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.sms.models.SendMessageRequest;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SmsTestBase {

    static final String PROTOCOL = "https://";
    static final String ENDPOINT = "localhost";
    static final String PATH = "/sms";
    static final String ACCESSKEY = "VGhpcyBpcyBhIHRlc3Q="; // Base64 encoded "This is a test"
    private ObjectMapper mapper;

    public SmsTestBase() {
        mapper = new ObjectMapper();
    }

    public SmsClientBuilder getTestSmsClientBuilder(PhoneNumberIdentifier from, List<PhoneNumberIdentifier> to, String body) {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);
        return getTestSmsClientBuilder(from, to, body, smsOptions, null);
    }

    public SmsClientBuilder getTestSmsClientBuilder(PhoneNumberIdentifier from, List<PhoneNumberIdentifier> to, String body, SendSmsOptions smsOptions) {
        return getTestSmsClientBuilder(from, to, body, smsOptions, null);
    }

    public SmsClientBuilder getTestSmsClientBuilder(PhoneNumberIdentifier from, List<PhoneNumberIdentifier> to, String body, SendSmsOptions smsOptions, HttpPipelinePolicy policy) {

        HttpClient httpClient = getHttpClient(from, to, body, smsOptions);

        SmsClientBuilder builder = new SmsClientBuilder();

        builder.endpoint(PROTOCOL + ENDPOINT)
            .accessKey(ACCESSKEY)
            .httpClient(httpClient);

        if (policy != null) {
            builder.addPolicy(policy);
        }
        return builder;
    }

    public HttpClient getHttpClient(PhoneNumberIdentifier from, List<PhoneNumberIdentifier> to, String body, SendSmsOptions smsOptions) {
        return new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                SendMessageRequest messageRequest = null;

                try {
                    ByteBuffer bodyBuffer = request.getBody().blockFirst();
                    messageRequest = mapper.readValue(bodyBuffer.array(), SendMessageRequest.class);
                } catch (JsonProcessingException e) {
                    fail("JsonProcessingException: " + e.getMessage());
                } catch (IOException e) {
                    fail("IOException: " + e.getMessage());
                }

                assertNotNull(messageRequest, "No SmsMessageRequest");
                assertEquals(body, messageRequest.getMessage(), "body incorrect");
                assertEquals(from.getValue(), messageRequest.getFrom(), "from incorrect");
                assertEquals(ENDPOINT, request.getUrl().getHost());
                assertEquals(PATH, request.getUrl().getPath());

                Map<String, String> headers = request.getHeaders().toMap();

                // It would be very difficult to test the actual header
                // values without re-creating the HMAC Policy. We will
                // just make sure they are present and have values.
                assertTrue(headers.containsKey("Authorization"));
                assertTrue(headers.containsKey("User-Agent"));                
                assertTrue(headers.containsKey("x-ms-content-sha256"));
                assertNotNull(headers.get("Authorization"));
                assertNotNull(headers.get("x-ms-content-sha256"));

                Stream<String> numberStream = to.stream().map(n -> n.getValue());
                List<String> numberStrings = numberStream.collect(Collectors.toList());
                for (String t : messageRequest.getTo()) {
                    if (!numberStrings.contains(t)) {
                        fail("To value " + t + " was missing");
                    }
                }

                SendSmsOptions options = messageRequest.getSendSmsOptions();
                assertEquals(smsOptions.isEnableDeliveryReport(), options.isEnableDeliveryReport());

                HttpResponse response = new HttpResponse(request) {

                    @Override
                    public int getStatusCode() {
                        return 200;
                    }

                    @Override
                    public String getHeaderValue(String name) {
                        return "";
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        return new HttpHeaders();
                    }

                    @Override
                    public Flux<ByteBuffer> getBody() {
                        return Flux.empty();
                    }

                    @Override
                    public Mono<byte[]> getBodyAsByteArray() {
                        return Mono.empty();
                    }

                    @Override
                    public Mono<String> getBodyAsString() {
                        return Mono.empty();
                    }

                    @Override
                    public Mono<String> getBodyAsString(Charset charset) {
                        return Mono.empty();
                    }
                        
                };

                return Mono.just(response);
            }
        };
    }  
}
