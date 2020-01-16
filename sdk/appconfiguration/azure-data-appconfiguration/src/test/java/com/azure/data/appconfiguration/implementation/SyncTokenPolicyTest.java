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
import reactor.test.StepVerifier;

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
        final SyncToken syncToken = SyncToken.parseSyncToken(syncTokenString);
        syncTokenEquals(syncToken, ID, VALUE, SEQUENCE_NUMBER);
    }

    @Test
    public void singleSyncTokenTest() throws Exception {
        // Arrange
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();
        final String firstSyncToken = constructSyncTokenString(ID, VALUE, SEQUENCE_NUMBER);
        final HttpPipeline pipeline = customizedPipeline(firstSyncToken, syncTokenPolicy);

        final String secondSyncToken = constructSyncTokenString(ID, UPDATED_VALUE, UPDATED_SEQUENCE_NUMBER);
        final HttpPipeline pipeline2 = customizedPipeline(secondSyncToken, syncTokenPolicy);

        // Act and Assert
        // At first request, the request header should have empty sync-token value
        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))))
            .assertNext(response -> {
                final String firstRequestHeader = response.getRequest().getHeaders().getValue(SYNC_TOKEN);
                assertEquals("", firstRequestHeader);
                final String firstResponseHeader = response.getHeaders().getValue(SYNC_TOKEN);
                assertEquals(firstSyncToken, firstResponseHeader);
            })
            .verifyComplete();

        // Verify the new sync token value is from the concurrent map
        StepVerifier.create(pipeline2.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))))
            .assertNext(response -> assertEquals(constructSyncTokenStringWithoutSeqNumber(ID, VALUE),
                response.getRequest().getHeaders().getValue(SYNC_TOKEN)))
            .verifyComplete();

        // Verify the updated cached sync-token value
        StepVerifier.create(pipeline2.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))))
            .assertNext(response -> assertEquals(constructSyncTokenStringWithoutSeqNumber(ID, UPDATED_VALUE),
                response.getRequest().getHeaders().getValue(SYNC_TOKEN)))
            .verifyComplete();
    }

    @Test
    public void multipleSyncTokenTest() throws Exception {
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();
        // Arrange
        final String syncTokens = constructSyncTokenString(ID, VALUE, SEQUENCE_NUMBER)
            + "," + constructSyncTokenString(NEW_ID, UPDATED_VALUE, UPDATED_SEQUENCE_NUMBER);
        final HttpPipeline pipeline = customizedPipeline(syncTokens, syncTokenPolicy);

        final String secondSyncToken = constructSyncTokenString(ID, UPDATED_VALUE, UPDATED_SEQUENCE_NUMBER);
        final HttpPipeline pipeline2 = customizedPipeline(secondSyncToken, syncTokenPolicy);

        // Act and Assert
        // At first request, the request header should have empty sync-token value
        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))))
            .assertNext(response -> {
                assertEquals("", response.getRequest().getHeaders().getValue(SYNC_TOKEN));
                assertEquals(syncTokens, response.getHeaders().getValue(SYNC_TOKEN));
            })
            .verifyComplete();

        // Verify the new sync token value from the concurrent map
        StepVerifier.create(pipeline2.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))))
            .assertNext(response -> {
                final String secondRequestHeaderExpected = constructSyncTokenStringWithoutSeqNumber(ID, VALUE)
                    + "," + constructSyncTokenStringWithoutSeqNumber(NEW_ID, UPDATED_VALUE);
                assertEquals(secondRequestHeaderExpected, response.getRequest().getHeaders().getValue(SYNC_TOKEN));
            })
            .verifyComplete();

        // Verify the updated cached sync-token value
        StepVerifier.create(pipeline2.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))))
            .assertNext(response -> {
                final String thirdRequestHeaderExpected = constructSyncTokenStringWithoutSeqNumber(ID, UPDATED_VALUE)
                    + "," + constructSyncTokenStringWithoutSeqNumber(NEW_ID, UPDATED_VALUE);
                assertEquals(thirdRequestHeaderExpected, response.getRequest().getHeaders().getValue(SYNC_TOKEN));
            }).verifyComplete();
    }

    private void syncTokenEquals(SyncToken syncToken, String id, String value, long sn) {
        assertEquals(id, syncToken.getId());
        assertEquals(value, syncToken.getValue());
        assertEquals(sn, syncToken.getSequenceNumber());
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
        return id + "=" + value + ";sn=" + sn;
    }

    private String constructSyncTokenStringWithoutSeqNumber(String id, String value) {
        return id + "=" + value;
    }
}
