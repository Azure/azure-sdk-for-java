// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Sync Token
 */
public class SyncTokenPolicyTest {
    private static final long SEQUENCE_NUMBER = 28;
    private static final long UPDATED_SEQUENCE_NUMBER = 30;
    private static final String COMMA = ",";
    private static final String EQUAL = "=";
    private static final String ID = "jtqGc1I4";
    private static final String LOCAL_HOST = "http://localhost/";
    private static final String NEW_ID = "newID";
    private static final String SEMICOLON = ";";
    private static final String SN_NAME = "sn";
    private static final String SYNC_TOKEN = "Sync-Token";
    private static final String UPDATED_VALUE = "UpdatedValue";
    private static final String VALUE = "MDoyOA==";

    @Mock
    private HttpPipelineCallContext httpPipelineCallContext;

    @Mock
    private HttpPipelineNextPolicy httpPipelineNextPolicy;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Parsing valid sync token
     */
    @Test
    public void parseSyncTokenString() {
        final SyncToken syncToken = new SyncToken(constructSyncTokenString(ID, VALUE, SN_NAME, SEQUENCE_NUMBER));
        syncTokenEquals(syncToken, ID, VALUE, SEQUENCE_NUMBER);
    }

    /**
     * Parsing invalid null sync token
     */
    @Test
    public void parseNullSyncTokenString() {
        assertThrows(IllegalArgumentException.class, () -> new SyncToken(null));
    }

    /**
     * Parsing invalid empty sync token
     */
    @Test
    public void parseEmptySyncTokenString() {
        assertThrows(IllegalArgumentException.class, () -> new SyncToken(""));
    }

    /**
     * Parsing invalid sync token with missing sequence number section
     */
    @Test
    public void parseMissingSectionSyncToken() {
        assertThrows(IllegalArgumentException.class, () -> new SyncToken(constructSyncTokenString(ID, VALUE, SN_NAME, null)));
    }

    /**
     * Parsing invalid sync token with missing id name
     */
    @Test
    public void parseWrongIdentifierNameSyncToken() {
        assertThrows(IllegalArgumentException.class, () -> new SyncToken(constructSyncTokenString(null, VALUE, SN_NAME, SEQUENCE_NUMBER)));
    }

    /**
     * Parsing invalid sync token with missing id's value
     */
    @Test
    public void parseMissingIdentifierValueSyncToken() {
        assertThrows(IllegalArgumentException.class, () -> new SyncToken(constructSyncTokenString(ID, null, SN_NAME, SEQUENCE_NUMBER)));
    }

    /**
     * Parsing invalid sync token with missing sequence number name
     */
    @Test
    public void parseMissingSequenceNumberNameSyncToken() {
        assertThrows(IllegalArgumentException.class, () -> new SyncToken(constructSyncTokenString(ID, VALUE, null, SEQUENCE_NUMBER)));
    }

    /**
     * Parsing invalid sync token with missing sequence number
     */
    @Test
    public void parseMissingSequenceNumberSyncToken() {
        assertThrows(IllegalArgumentException.class, () -> new SyncToken(constructSyncTokenString(ID, VALUE, SN_NAME, null)));
    }

    /**
     * Parsing invalid sync token with invalid sequence number
     */
    @Test
    public void parseInvalidSequenceNumberSyncToken() {
        assertThrows(IllegalArgumentException.class, () -> new SyncToken(constructSyncTokenString(ID, VALUE, SN_NAME, SEQUENCE_NUMBER) + "ABC"));
    }

    /**
     * Test for sync token policy with a single valid sync token
     *
     * @throws Exception URL exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void singleSyncTokenTest() throws MalformedURLException {
        // Arrange
        final String firstResponseHeadersExpected = constructSyncTokenString(ID, VALUE, SN_NAME, SEQUENCE_NUMBER);
        final String secondResponseHeaderExpected = constructSyncTokenString(ID, UPDATED_VALUE, SN_NAME, UPDATED_SEQUENCE_NUMBER);
        final HttpRequest firstRequest = new HttpRequest(HttpMethod.GET, new URL(LOCAL_HOST), new HttpHeaders(), null);
        final HttpRequest secondRequest = new HttpRequest(HttpMethod.GET, new URL(LOCAL_HOST),
            new HttpHeaders().put(SYNC_TOKEN, firstResponseHeadersExpected), null);

        final String secondResponseRequestHeaderExpected = constructSyncTokenString(ID, VALUE, null, null);

        // Mock
        when(httpPipelineCallContext.getHttpRequest()).thenReturn(firstRequest, secondRequest);
        when(httpPipelineNextPolicy.process()).thenReturn(
            Mono.just(new MockHttpResponse(firstRequest, 200, new HttpHeaders().put(SYNC_TOKEN, firstResponseHeadersExpected))),
            Mono.just(new MockHttpResponse(secondRequest, 200, new HttpHeaders().put(SYNC_TOKEN, secondResponseHeaderExpected))));

        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();

        // Act
        final HttpResponse firstResponse = syncTokenPolicy.process(httpPipelineCallContext, httpPipelineNextPolicy).block();
        final HttpResponse secondResponse = syncTokenPolicy.process(httpPipelineCallContext, httpPipelineNextPolicy).block();

        // Assertion
        // verify the first response's request headers is empty
        assertTrue(firstResponse.getRequest().getHeaders().getValue(SYNC_TOKEN).isEmpty());
        // verify the first response headers
        assertEquals(firstResponseHeadersExpected, firstResponse.getHeaders().getValue(SYNC_TOKEN));
        // verify the second response's request header
        assertEquals(
            // Because the limitation of constructSyncTokenString() always return the string
            // ending with semicolon, adding semicolon to the next line is only for validation
            secondResponseRequestHeaderExpected.substring(0, secondResponseRequestHeaderExpected.length() - 1),
            secondResponse.getRequest().getHeaders().getValue(SYNC_TOKEN));
        // verify second response header
        assertEquals(secondResponseHeaderExpected, secondResponse.getHeaders().getValue(SYNC_TOKEN));
    }

    /**
     * Test for multiple sync tokens
     *
     * @throws Exception URL exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void multipleSyncTokensTest() throws Exception {
        // Arrange
        final String firstResponseHeadersExpected = constructSyncTokenString(ID, VALUE, SN_NAME, SEQUENCE_NUMBER) + COMMA
                                                        + constructSyncTokenString(NEW_ID, UPDATED_VALUE, SN_NAME, UPDATED_SEQUENCE_NUMBER);
        final String secondResponseHeaderExpected = constructSyncTokenString(ID, UPDATED_VALUE, SN_NAME, UPDATED_SEQUENCE_NUMBER);

        final HttpRequest firstRequest = new HttpRequest(HttpMethod.GET, new URL(LOCAL_HOST), new HttpHeaders(), null);
        final HttpRequest secondRequest = new HttpRequest(HttpMethod.GET, new URL(LOCAL_HOST),
            new HttpHeaders().put(SYNC_TOKEN, firstResponseHeadersExpected), null);

        final String secondResponseRequestHeaderExpectedPart1 = constructSyncTokenString(ID, VALUE, null, null);
        final String secondResponseRequestHeaderExpectedPart2 = constructSyncTokenString(NEW_ID, UPDATED_VALUE, null, null);
        // Because the limitation of constructSyncTokenString() always return the string ending with semicolon
        final String secondResponseRequestHeaderExpected =
            secondResponseRequestHeaderExpectedPart1.substring(0, secondResponseRequestHeaderExpectedPart1.length() - 1)
                + COMMA + secondResponseRequestHeaderExpectedPart2.substring(0, secondResponseRequestHeaderExpectedPart2.length() - 1);

        // Mock
        when(httpPipelineCallContext.getHttpRequest()).thenReturn(firstRequest, secondRequest);
        when(httpPipelineNextPolicy.process()).thenReturn(
            Mono.just(new MockHttpResponse(firstRequest, 200, new HttpHeaders().put(SYNC_TOKEN, firstResponseHeadersExpected))),
            Mono.just(new MockHttpResponse(secondRequest, 200, new HttpHeaders().put(SYNC_TOKEN, secondResponseHeaderExpected))));

        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();

        // Act
        final HttpResponse firstResponse = syncTokenPolicy.process(httpPipelineCallContext, httpPipelineNextPolicy).block();
        final HttpResponse secondResponse = syncTokenPolicy.process(httpPipelineCallContext, httpPipelineNextPolicy).block();

        // Assertion
        // verify the first response's request headers is empty
        assertTrue(firstResponse.getRequest().getHeaders().getValue(SYNC_TOKEN).isEmpty());
        // verify the first response headers
        assertEquals(firstResponseHeadersExpected, firstResponse.getHeaders().getValue(SYNC_TOKEN));
        // verify the second response's request header
        assertEquals(secondResponseRequestHeaderExpected, secondResponse.getRequest().getHeaders().getValue(SYNC_TOKEN));
        // verify second response header
        assertEquals(secondResponseHeaderExpected, secondResponse.getHeaders().getValue(SYNC_TOKEN));
    }

    private void syncTokenEquals(SyncToken syncToken, String id, String value, Long sn) {
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
