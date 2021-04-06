// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Sync Token
 */
public class SyncTokenPolicyTest {
    private static final long SEQUENCE_NUMBER = 28;
    private static final String EQUAL = "=";
    private static final String ID = "jtqGc1I4";
    private static final String SEMICOLON = ";";
    private static final String SN_NAME = "sn";
    private static final String SYNC_TOKEN = "Sync-Token";
    private static final String VALUE = "MDoyOA==";
    private static final String SYNC_TOKEN_VALUE = "syncToken1=val1";
    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final String REQUEST_ID = "requestId";
    private static final String LOCAL_HOST = "http://localhost";

    /**
     * Parsing valid sync token
     */
    @Test
    public void parseSyncTokenString() {
        final SyncToken syncToken =
            SyncToken.createSyncToken(constructSyncTokenString(ID, VALUE, SN_NAME, SEQUENCE_NUMBER));
        syncTokenEquals(syncToken, ID, VALUE, SEQUENCE_NUMBER);
    }

    /**
     * Parsing invalid null sync token
     */
    @Test
    public void parseNullSyncTokenString() {
        assertThrows(IllegalArgumentException.class, () -> SyncToken.createSyncToken(null));
    }

    /**
     * Parsing invalid empty sync token
     */
    @Test
    public void parseEmptySyncTokenString() {
        assertThrows(IllegalArgumentException.class, () -> SyncToken.createSyncToken(""));
    }

    /**
     * Parsing invalid sync token with missing sequence number section
     */
    @Test
    public void parseMissingSectionSyncToken() {
        assertThrows(IllegalArgumentException.class,
            () -> SyncToken.createSyncToken(constructSyncTokenString(ID, VALUE, SN_NAME, null)));
    }

    /**
     * Parsing invalid sync token with missing id name
     */
    @Test
    public void parseWrongIdentifierNameSyncToken() {
        assertThrows(IllegalArgumentException.class,
            () -> SyncToken.createSyncToken(constructSyncTokenString(null, VALUE, SN_NAME, SEQUENCE_NUMBER)));
    }

    /**
     * Parsing invalid sync token with missing id's value
     */
    @Test
    public void parseMissingIdentifierValueSyncToken() {
        assertThrows(IllegalArgumentException.class,
            () -> SyncToken.createSyncToken(constructSyncTokenString(ID, null, SN_NAME, SEQUENCE_NUMBER)));
    }

    /**
     * Parsing invalid sync token with missing sequence number name
     */
    @Test
    public void parseMissingSequenceNumberNameSyncToken() {
        assertThrows(IllegalArgumentException.class,
            () -> SyncToken.createSyncToken(constructSyncTokenString(ID, VALUE, null, SEQUENCE_NUMBER)));
    }

    /**
     * Parsing invalid sync token with missing sequence number
     */
    @Test
    public void parseMissingSequenceNumberSyncToken() {
        assertThrows(IllegalArgumentException.class,
            () -> SyncToken.createSyncToken(constructSyncTokenString(ID, VALUE, SN_NAME, null)));
    }

    /**
     * Parsing invalid sync token with invalid sequence number
     */
    @Test
    public void parseInvalidSequenceNumberSyncToken() {
        assertThrows(IllegalArgumentException.class,
            () -> SyncToken.createSyncToken(constructSyncTokenString(ID, VALUE, SN_NAME, SEQUENCE_NUMBER) + "ABC"));
    }

    @Test
    public void setSyncTokenPolicyProcessTest() throws MalformedURLException {
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String headerValue = context.getHttpRequest().getHeaders().getValue(SYNC_TOKEN);
            final String requestId = context.getHttpRequest().getHeaders().getValue(REQUEST_ID);
            if (requestId.equals(FIRST)) {
                assertEquals("", headerValue);
            } else if (requestId.equals(SECOND)) {
                assertEquals(SYNC_TOKEN_VALUE, headerValue);
            } else {
                // do nothing
                assertTrue(true);
            }
            // Sequence number, sn is not used in the request header. It is included in the response header.
            return next.process();
        };

        final HttpPipeline pipeline =
            new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        return Mono.just(new MockHttpResponse(request, 200,
                            new HttpHeaders().set(SYNC_TOKEN, SYNC_TOKEN_VALUE + ";sn=1")));
                    }
                })
                .policies(syncTokenPolicy, auditorPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(LOCAL_HOST));
        request.getHeaders().set(REQUEST_ID, FIRST);
        pipeline.send(request).block();
        request.getHeaders().set(REQUEST_ID, SECOND);
        pipeline.send(request).block();
    }

    @Test
    public void externalSyncTokenIsSentWithRequestText() throws MalformedURLException {
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();

        syncTokenPolicy.updateSyncToken(SYNC_TOKEN_VALUE + ";sn=1");

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String headerValue = context.getHttpRequest().getHeaders().getValue(SYNC_TOKEN);
            // Sequence number, sn is not used in the request header. It is included in the response header.
            assertEquals(SYNC_TOKEN_VALUE, headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(syncTokenPolicy, auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(LOCAL_HOST));
        pipeline.send(request).block();
    }

    @Test
    public void externalSyncTokensFollowRulesWhenAddedTest() throws MalformedURLException {
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();

        syncTokenPolicy.updateSyncToken("syncToken1=val1;sn=1");
        syncTokenPolicy.updateSyncToken("syncToken1=val2;sn=2,syncToken2=val3;sn=2");
        syncTokenPolicy.updateSyncToken("syncToken2=val1;sn=1");

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String headerValue = context.getHttpRequest().getHeaders().getValue(SYNC_TOKEN);
            // Sequence number, sn is not used in the request header. It is included in the response header.
            assertTrue(headerValue.contains("syncToken2=val3"));
            assertTrue(headerValue.contains("syncToken1=val2"));
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
                                          .policies(syncTokenPolicy, auditorPolicy)
                                          .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(LOCAL_HOST));
        pipeline.send(request).block();
    }

    private void syncTokenEquals(SyncToken syncToken, String id, String value, long sn) {
        assertEquals(id, syncToken.getId());
        assertEquals(value, syncToken.getValue());
        assertEquals(sn, syncToken.getSequenceNumber());
    }

    // This helper function construct an sync token string by
    private String constructSyncTokenString(String id, String value, String snName, Long sn) {
        final StringBuilder sb = new StringBuilder();
        // identifier name
        if (id != null) {
            sb.append(id);
        }

        if (id != null || value != null) {
            sb.append(EQUAL);
        }

        // identifier's value
        if (value != null) {
            sb.append(value);
        }

        // Always have ";" if any of given value exist
        if (id != null || value != null || snName != null || sn != null) {
            sb.append(SEMICOLON);
        }

        // sequence number name
        if (snName != null) {
            sb.append(snName);
        }

        if (snName != null || sn != null) {
            sb.append(EQUAL);
        }

        // sequence number value
        if (sn != null) {
            sb.append(sn);
        }

        return sb.toString();
    }
}
