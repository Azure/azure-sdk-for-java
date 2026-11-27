// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.models.SearchPagedResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgenticSearchSampleTests {
    @Test
    public void directSampleRequestsDiagnosticsAndReadsRenamedUsage() {
        SearchClient client = client("{\"value\":[{\"id\":\"hotel-ca-1\",\"@search.score\":1}],"
            + "\"@search.debug\":{\"agentic\":{\"terminationReason\":\"completed\","
            + "\"usage\":{\"agenticPlanningTokens\":3,\"reasoningTokens\":7}}}}");
        SearchPagedResponse page
            = client.search(AgenticSearchPreviewExample.directOptions()).iterableByPage().iterator().next();
        AgenticSearchPreviewExample.inspectDirectResult(page);
        assertEquals(7, page.getDebugInfo().getAgentic().getUsage().getReasoningTokens());
    }

    @Test
    public void directSampleRejectsMissingResultsOrUsage() {
        for (String body : new String[] { "{\"value\":[]}", "{\"value\":[{\"id\":\"hotel-ca-1\"}]}" }) {
            SearchPagedResponse page
                = client(body).search(AgenticSearchPreviewExample.directOptions()).iterableByPage().iterator().next();
            assertThrows(IllegalStateException.class, () -> AgenticSearchPreviewExample.inspectDirectResult(page));
        }
    }

    private static SearchClient client(String body) {
        return new SearchClientBuilder().endpoint("https://example.search.windows.net")
            .indexName("owned")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                Map<?, ?> options = request.getBodyAsBinaryData().toObject(Map.class);
                assertEquals("agentic", options.get("queryType"));
                assertEquals("agentic", options.get("debug"));
                assertEquals(30, ((Map<?, ?>) options.get("agenticOptions")).get("maxRuntimeInSeconds"));
                return Mono.just(new MockHttpResponse(request, 200, BinaryData.fromString(body).toBytes()));
            })
            .buildClient();
    }
}
