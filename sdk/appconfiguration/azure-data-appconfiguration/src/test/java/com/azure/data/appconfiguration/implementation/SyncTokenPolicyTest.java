// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestBase;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SyncTokenPolicyTest extends TestBase {

    private static final String SYNC_TOKEN = "Sync-Token";
    private static final String ID = "jtqGc1I4";
    private static final String NEW_ID = "newID";
    private static final String VALUE = "MDoyOA==";
    private static final String UPDATED_VALUE = "UpdatedValue";
    private static final long SEQUENCE_NUMBER = 28;
    private static final long UPDATED_SEQUENCE_NUMBER = 30;

    @Test
    public void parseSyncToken() {
        String syncTokenString = constructSyncTokenString(ID, VALUE, SEQUENCE_NUMBER);
        final SyncToken syncToken = SyncToken.fromSyncTokenString(syncTokenString);
        syncTokenEquals(syncToken, ID, VALUE, SEQUENCE_NUMBER);
    }

    @Test
    public void singleSyncTokenTest() throws Exception {
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();

        final String firstSyncToken = constructSyncTokenString(ID, VALUE, SEQUENCE_NUMBER);
        final HttpPipeline pipeline = customizedPipeline(firstSyncToken, syncTokenPolicy);
        final HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))).block();
        final String firstRequestHeader = response.getRequest().getHeaders().getValue(SYNC_TOKEN);
        // At first request, the request header should have empty sync-token value
        assertEquals("", firstRequestHeader);
        final String firstResponseHeader = response.getHeaders().getValue(SYNC_TOKEN);
        assertEquals(firstSyncToken, firstResponseHeader);

        final String secondSyncToken = constructSyncTokenString(ID, UPDATED_VALUE, UPDATED_SEQUENCE_NUMBER);
        final HttpPipeline pipeline2 = customizedPipeline(secondSyncToken, syncTokenPolicy);
        final HttpResponse response2 = pipeline2.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))).block();
        // verify the new sync token value from the concurrent map
        final String secondRequestHeader = response2.getRequest().getHeaders().getValue(SYNC_TOKEN);
        assertEquals(constructSyncTokenStringWithoutSeqNumber(ID, VALUE), secondRequestHeader);

        // verify the updated cached sync-token value
        final HttpResponse response3 = pipeline2.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))).block();
        final String thirdRequestHeader = response3.getRequest().getHeaders().getValue(SYNC_TOKEN);
        assertEquals(constructSyncTokenStringWithoutSeqNumber(ID, UPDATED_VALUE), thirdRequestHeader);

    }

    @Test
    public void multipleSyncTokenTest() throws Exception {
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();

        final String syncTokens = constructSyncTokenString(ID, VALUE, SEQUENCE_NUMBER) + "," + constructSyncTokenString(NEW_ID, UPDATED_VALUE, UPDATED_SEQUENCE_NUMBER);
        final HttpPipeline pipeline = customizedPipeline(syncTokens, syncTokenPolicy);
        final HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))).block();
        // At first request, the request header should have empty sync-token value
        final String firstRequestHeader = response.getRequest().getHeaders().getValue(SYNC_TOKEN);
        assertEquals("", firstRequestHeader);
        final String firstResponseHeader = response.getHeaders().getValue(SYNC_TOKEN);
        assertEquals(syncTokens, firstResponseHeader);

        final String secondSyncToken = constructSyncTokenString(ID, UPDATED_VALUE, UPDATED_SEQUENCE_NUMBER);
        final HttpPipeline pipeline2 = customizedPipeline(secondSyncToken, syncTokenPolicy);
        final HttpResponse response2 = pipeline2.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))).block();
        // verify the new sync token value from the concurrent map
        final String secondRequestHeader = response2.getRequest().getHeaders().getValue(SYNC_TOKEN);
        final String secondRequestHeaderExpected = constructSyncTokenStringWithoutSeqNumber(ID, VALUE) + "," + constructSyncTokenStringWithoutSeqNumber(NEW_ID, UPDATED_VALUE);
        assertEquals(secondRequestHeaderExpected, secondRequestHeader);

        // verify the updated cached sync-token value
        final HttpResponse response3 = pipeline2.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))).block();
        final String thirdRequestHeader = response3.getRequest().getHeaders().getValue(SYNC_TOKEN);
        final String thirdRequestHeaderExpected = constructSyncTokenStringWithoutSeqNumber(ID, UPDATED_VALUE) + "," + constructSyncTokenStringWithoutSeqNumber(NEW_ID, UPDATED_VALUE);
        assertEquals(thirdRequestHeaderExpected, thirdRequestHeader);
    }

    private boolean syncTokenEquals(SyncToken syncToken, String id, String value, long sn) {
        assertEquals(id, syncToken.getId());
        assertEquals(value, syncToken.getValue());
        assertEquals(sn, syncToken.getSequenceNumber());
        return true;
    }

    private HttpPipeline customizedPipeline(String syncToken, SyncTokenPolicy syncTokenPolicy) {

        return new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders().put(SYNC_TOKEN, syncToken)));
                }
            })
            .policies(syncTokenPolicy)
            .build();
    }


    private String constructSyncTokenString(String id, String value, long sn) {
        return String.format(id + "=" + value + ";sn=" + sn);
    }

    private String constructSyncTokenStringWithoutSeqNumber(String id, String value) {
        return String.format(id + "=" + value);
    }
}
