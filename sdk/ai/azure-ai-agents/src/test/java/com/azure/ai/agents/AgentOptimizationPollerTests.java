// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationJobResult;
import com.azure.ai.agents.models.JobStatus;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentOptimizationPollerTests {
    private static final String ENDPOINT = "https://localhost:8080/api/projects/project";
    private static final HttpHeaderName OPERATION_LOCATION = HttpHeaderName.fromString("Operation-Location");
    private static final HttpHeaderName OPERATION_ID = HttpHeaderName.fromString("Operation-Id");

    @Test
    public void syncPollerExposesInitialJobIdAndOperationId() {
        HttpHeaders headers
            = new HttpHeaders().set(OPERATION_LOCATION, ENDPOINT + "/agent_optimization_jobs/optimization-job-sync");
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(201, headers, jobJson("optimization-job-sync", "queued"))
                .enqueueJson(200, jobJson("optimization-job-sync", "queued"));
        BetaAgentsClient client = createBuilder(httpClient).beta().buildBetaAgentsClient();

        SyncPoller<AgentOptimizationJob, AgentOptimizationJobResult> poller
            = client.beginCreateOptimizationJob(new AgentOptimizationJob(), "operation-sync");
        PollResponse<AgentOptimizationJob> response = poller.poll();

        assertNotNull(response.getValue());
        assertEquals("optimization-job-sync", response.getValue().getId());
        assertEquals(JobStatus.QUEUED, response.getValue().getStatus());
        assertEquals(2, httpClient.getRequests().size());
        HttpRequest request = httpClient.getRequest(0);
        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertTrue(request.getUrl().getPath().endsWith("/agent_optimization_jobs"));
        assertEquals("operation-sync", request.getHeaders().getValue(OPERATION_ID));
    }

    @Test
    public void asyncPollerExposesInitialJobIdAndOperationId() {
        HttpHeaders headers
            = new HttpHeaders().set(OPERATION_LOCATION, ENDPOINT + "/agent_optimization_jobs/optimization-job-async");
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(201, headers, jobJson("optimization-job-async", "queued"))
                .enqueueJson(200, jobJson("optimization-job-async", "queued"));
        BetaAgentsAsyncClient client = createBuilder(httpClient).beta().buildBetaAgentsAsyncClient();

        PollerFlux<AgentOptimizationJob, AgentOptimizationJobResult> poller
            = client.beginCreateOptimizationJob(new AgentOptimizationJob(), "operation-async");

        StepVerifier.create(poller.next())
            .assertNext(response -> assertInitialAsyncResponse(response, "optimization-job-async"))
            .verifyComplete();

        assertEquals(2, httpClient.getRequests().size());
        HttpRequest request = httpClient.getRequest(0);
        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals("operation-async", request.getHeaders().getValue(OPERATION_ID));
    }

    private static void assertInitialAsyncResponse(
        AsyncPollResponse<AgentOptimizationJob, AgentOptimizationJobResult> response, String expectedJobId) {
        assertNotNull(response.getValue());
        assertEquals(expectedJobId, response.getValue().getId());
        assertEquals(JobStatus.QUEUED, response.getValue().getStatus());
    }

    private static AgentsClientBuilder createBuilder(DeterministicHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    private static String jobJson(String jobId, String status) {
        return "{\"id\":\"" + jobId + "\",\"status\":\"" + status + "\",\"created_at\":1,\"updated_at\":1}";
    }
}
